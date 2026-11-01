package data;

import model.*;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Central local data store. Data is persisted to:
 *   <user.home>/.fitwise/fitwise_data.ser
 *
 * All logs are keyed by user and date so one user's information never appears
 * in another account and daily trackers reset naturally on a new day.
 */
public class DataStore {
    private static final List<User> users = new ArrayList<>();
    private static final List<Food> foods = new ArrayList<>();
    private static final List<Progress> progressEntries = new ArrayList<>();
    private static final Map<Integer,WeeklyAdaptation> weeklyAdaptations = new HashMap<>();
    private static final Map<Integer,List<WeeklyAdaptation>> weeklyAdaptationHistory = new HashMap<>();

    // Legacy/current-day mirrors retained for compatibility with older screens.
    private static final Map<Integer,Integer> water = new HashMap<>();
    private static final Map<Integer,Integer> steps = new HashMap<>();
    private static final Map<Integer,Double> sleep = new HashMap<>();
    private static final Map<Integer,Set<String>> workouts = new HashMap<>();
    private static final Map<Integer,Set<String>> challenges = new HashMap<>();
    private static final Map<Integer,Map<String,Boolean>> notifications = new HashMap<>();
    private static final Map<Integer,Set<String>> workoutDays = new HashMap<>();

    // New date-aware trackers.
    private static final Map<Integer,Map<String,Integer>> dailyWater = new HashMap<>();
    private static final Map<Integer,Map<String,Integer>> dailySteps = new HashMap<>();
    private static final Map<Integer,Map<String,Double>> dailySleep = new HashMap<>();
    // user -> date -> meal type -> status (PENDING/EATEN/SKIPPED)
    private static final Map<Integer,Map<String,Map<String,String>>> mealStatus = new HashMap<>();
    // user -> date -> meal type -> selected meal description
    private static final Map<Integer,Map<String,Map<String,String>>> mealChoices = new HashMap<>();
    // user -> date -> workout names completed
    private static final Map<Integer,Map<String,Set<String>>> dailyWorkouts = new HashMap<>();

    private static int uid=1,pid=1;

    static {
        seedFood();
        if(!loadFromDisk()){
            saveToDisk();
        } else {
            repairLoadedMaps();
        }
    }

    // ---------- users/auth ----------
    public static boolean emailExists(String e){
        if(e==null)return false;
        for(User u:users) if(u.getEmail()!=null && u.getEmail().equalsIgnoreCase(e)) return true;
        return false;
    }
    public static void addUser(User u){
        u.setUserId(uid++);
        users.add(u);
        init(u.getUserId());
        saveToDisk();
    }
    private static void init(int id){
        water.putIfAbsent(id,0); steps.putIfAbsent(id,0); sleep.putIfAbsent(id,0.0);
        workouts.putIfAbsent(id,new HashSet<>()); challenges.putIfAbsent(id,new HashSet<>());
        workoutDays.putIfAbsent(id,new HashSet<>());
        notifications.putIfAbsent(id,new LinkedHashMap<>());
        if(notifications.get(id).isEmpty()){
            notifications.get(id).put("Water reminder",true);
            notifications.get(id).put("Workout reminder",true);
            notifications.get(id).put("Meal reminder",true);
            notifications.get(id).put("Sleep reminder",true);
        }
        dailyWater.putIfAbsent(id,new LinkedHashMap<>());
        dailySteps.putIfAbsent(id,new LinkedHashMap<>());
        dailySleep.putIfAbsent(id,new LinkedHashMap<>());
        mealStatus.putIfAbsent(id,new LinkedHashMap<>());
        mealChoices.putIfAbsent(id,new LinkedHashMap<>());
        dailyWorkouts.putIfAbsent(id,new LinkedHashMap<>());
    }
    private static String today(){ return LocalDate.now().toString(); }

    public static User findByCredentials(String e,String p){
        for(User u:users) if(u.getEmail().equalsIgnoreCase(e) && u.getPassword().equals(p)) return u;
        return null;
    }
    public static User findByEmail(String e){
        if(e==null)return null;
        for(User u:users) if(u.getEmail()!=null && u.getEmail().equalsIgnoreCase(e)) return u;
        return null;
    }
    public static void saveUserChanges(){saveToDisk();}

    // ---------- food ----------
    public static List<Food> getAllFoods(){return foods;}

    // ---------- progress ----------
    public static void addProgress(Progress p){
        p.setProgressId(pid++); progressEntries.add(p); saveToDisk();
    }
    public static void saveWeeklyAdaptation(WeeklyAdaptation a){
        weeklyAdaptations.put(a.getUserId(),a);
        weeklyAdaptationHistory.computeIfAbsent(a.getUserId(),k->new ArrayList<>()).add(a);
        saveToDisk();
    }
    public static WeeklyAdaptation getWeeklyAdaptation(int id){ return weeklyAdaptations.get(id); }
    public static List<WeeklyAdaptation> getWeeklyAdaptationHistory(int id){
        return new ArrayList<>(weeklyAdaptationHistory.getOrDefault(id,Collections.emptyList()));
    }

    // Per-user online-learning state is stored separately so existing serialized
    // FitWise data files remain compatible across this update.
    public static synchronized model.ExperienceLearningModel getExperienceLearningModel(int id) {
        File f = learningFile(id);
        if (!f.exists()) return new model.ExperienceLearningModel();
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            Object o = in.readObject();
            if (o instanceof model.ExperienceLearningModel) return (model.ExperienceLearningModel)o;
        } catch (Exception e) { System.err.println("FitWise: starting fresh learning model for user " + id + " (" + e.getMessage() + ")"); }
        return new model.ExperienceLearningModel();
    }
    public static synchronized void saveExperienceLearningModel(int id, model.ExperienceLearningModel model) {
        File f = learningFile(id);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f))) { out.writeObject(model); }
        catch (IOException e) { System.err.println("FitWise: could not save learning model (" + e.getMessage() + ")"); }
    }
    private static File learningFile(int id) {
        File dir = new File(System.getProperty("user.home"), ".fitwise");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, "experience_model_user_" + id + ".ser");
    }

    public static List<Progress> getProgressForUser(int id){
        List<Progress> r=new ArrayList<>();
        for(Progress p:progressEntries)if(p.getUserId()==id)r.add(p);
        r.sort(Comparator.comparing(Progress::getDate)); return r;
    }

    // ---------- water ----------
    public static int getWaterConsumed(int id){return getWaterConsumed(id,today());}
    public static int getWaterConsumed(int id,String date){
        init(id); return dailyWater.get(id).getOrDefault(date,0);
    }
    public static void addWater(int id,int ml){addWater(id,today(),ml);}
    public static void addWater(int id,String date,int ml){
        init(id); dailyWater.get(id).put(date,Math.max(0,getWaterConsumed(id,date)+ml));
        if(today().equals(date))water.put(id,getWaterConsumed(id,date));
        saveToDisk();
    }
    public static void resetWater(int id){resetWater(id,today());}
    public static void resetWater(int id,String date){
        init(id); dailyWater.get(id).put(date,0);
        if(today().equals(date))water.put(id,0);
        saveToDisk();
    }
    public static Map<String,Integer> getWaterHistory(int id){init(id);return new LinkedHashMap<>(dailyWater.get(id));}

    // ---------- steps ----------
    public static int getSteps(int id){return getSteps(id,today());}
    public static int getSteps(int id,String date){init(id);return dailySteps.get(id).getOrDefault(date,0);}
    public static void addSteps(int id,int n){addSteps(id,today(),n);}
    public static void addSteps(int id,String date,int n){
        init(id);dailySteps.get(id).put(date,Math.max(0,getSteps(id,date)+n));
        if(today().equals(date))steps.put(id,getSteps(id,date));
        saveToDisk();
    }
    public static void resetSteps(int id){resetSteps(id,today());}
    public static void resetSteps(int id,String date){
        init(id);dailySteps.get(id).put(date,0);
        if(today().equals(date))steps.put(id,0);
        saveToDisk();
    }

    public static Map<String,Integer> getStepsHistory(int id){init(id);return new LinkedHashMap<>(dailySteps.get(id));}

    // ---------- sleep ----------
    public static double getSleep(int id){return getSleep(id,today());}
    public static double getSleep(int id,String date){init(id);return dailySleep.get(id).getOrDefault(date,0.0);}
    public static void setSleep(int id,double hours){setSleep(id,today(),hours);}
    public static void setSleep(int id,String date,double hours){
        init(id);dailySleep.get(id).put(date,Math.max(0,hours));
        if(today().equals(date))sleep.put(id,Math.max(0,hours));
        saveToDisk();
    }
    public static Map<String,Double> getSleepHistory(int id){init(id);return new LinkedHashMap<>(dailySleep.get(id));}

    // ---------- meals ----------
    public static String getMealStatus(int id,String date,String mealType){
        init(id);
        return mealStatus.get(id).getOrDefault(date,Collections.emptyMap()).getOrDefault(mealType,"PENDING");
    }
    public static void setMealStatus(int id,String date,String mealType,String status){
        init(id);
        mealStatus.get(id).computeIfAbsent(date,k->new LinkedHashMap<>()).put(mealType,status);
        saveToDisk();
    }
    public static String getMealChoice(int id,String date,String mealType){
        init(id);
        return mealChoices.get(id).getOrDefault(date,Collections.emptyMap()).get(mealType);
    }
    public static void setMealChoice(int id,String date,String mealType,String choice){
        init(id);
        mealChoices.get(id).computeIfAbsent(date,k->new LinkedHashMap<>()).put(mealType,choice);
        saveToDisk();
    }
    public static int getMealsEaten(int id,String date){
        init(id); int n=0;
        for(String s:mealStatus.get(id).getOrDefault(date,Collections.emptyMap()).values()) if("EATEN".equals(s))n++;
        return n;
    }
    public static int getMealsLogged(int id,String date){
        init(id); return mealStatus.get(id).getOrDefault(date,Collections.emptyMap()).size();
    }
    public static Map<String,String> getMealStatuses(int id,String date){
        init(id); return new LinkedHashMap<>(mealStatus.get(id).getOrDefault(date,Collections.emptyMap()));
    }

    // ---------- workouts ----------
    public static boolean isWorkoutCompleted(int id,String w){return isWorkoutCompleted(id,today(),w);}
    public static boolean isWorkoutCompleted(int id,String date,String w){
        init(id); return dailyWorkouts.get(id).getOrDefault(date,Collections.emptySet()).contains(w);
    }
    public static void setWorkoutCompleted(int id,String w,boolean done){setWorkoutCompleted(id,today(),w,done);}
    public static void setWorkoutCompleted(int id,String date,String w,boolean done){
        init(id);
        Set<String> set=dailyWorkouts.get(id).computeIfAbsent(date,k->new HashSet<>());
        if(done)set.add(w);else set.remove(w);
        if(today().equals(date)){
            workouts.put(id,new HashSet<>(set));
        }
        setWorkoutDayStatus(id,date,!set.isEmpty());
        // setWorkoutDayStatus saves too; avoid a second conceptual write.
    }
    public static void setWorkoutDayStatus(int id,String date,boolean done){
        workoutDays.putIfAbsent(id,new HashSet<>());
        if(done)workoutDays.get(id).add(date);else workoutDays.get(id).remove(date);
        saveToDisk();
    }
    public static Set<String> getWorkoutDoneDates(int id){
        init(id);return new HashSet<>(workoutDays.getOrDefault(id,Collections.emptySet()));
    }
    public static int getWorkoutCompletedCount(int id,String date){init(id);return dailyWorkouts.get(id).getOrDefault(date,Collections.emptySet()).size();}

    // ---------- challenges/notifications ----------
    public static boolean isChallengeCompleted(int id,String c){return challenges.getOrDefault(id,Collections.emptySet()).contains(c);}
    public static void setChallengeCompleted(int id,String c,boolean d){
        init(id);if(d)challenges.get(id).add(c);else challenges.get(id).remove(c);saveToDisk();
    }
    public static Map<String,Boolean> getNotificationSettings(int id){init(id);return notifications.get(id);}
    public static void setNotification(int id,String k,boolean v){init(id);notifications.get(id).put(k,v);saveToDisk();}

    // ---------- seed ----------
    private static void seedFood(){
        String[][] a={
            {"Oats","150","5","27","3","Vegan"},{"Egg","78","6","1","5","Eggetarian"},
            {"Banana","105","1","27","0","Vegan"},{"Brown Rice","216","5","45","2","Vegan"},
            {"Chicken Breast","165","31","0","4","Non-Vegetarian"},{"Paneer","265","18","6","20","Vegetarian"},
            {"Greek Yogurt","100","10","6","0.4","Vegetarian"},{"Almonds","164","6","6","14","Vegan"},
            {"Mixed Vegetable Salad","50","2","10","0.5","Vegan"},{"Grilled Fish","206","22","0","12","Non-Vegetarian"},
            {"Peanut Butter","190","8","6","16","Vegan"},{"Milk","103","8","12","2.4","Vegetarian"},
            {"Sweet Potato","112","2","26","0.1","Vegan"},{"Broccoli","55","4","11","0.6","Vegan"},
            {"Tofu","144","17","3","9","Vegan"},{"Lentils (Dal)","230","18","40","0.8","Vegan"}
        };
        for(String[] x:a)foods.add(new Food(x[0],Double.parseDouble(x[1]),Double.parseDouble(x[2]),Double.parseDouble(x[3]),Double.parseDouble(x[4]),x[5]));
    }


    // ---------- persistence ----------
    private static File storeFile(){
        File dir=new File(System.getProperty("user.home"),".fitwise");if(!dir.exists())dir.mkdirs();
        return new File(dir,"fitwise_data.ser");
    }
    private static class Snapshot implements Serializable{
        private static final long serialVersionUID=1L;
        List<User> users;List<Progress> progressEntries; Map<Integer,WeeklyAdaptation> weeklyAdaptations; Map<Integer,List<WeeklyAdaptation>> weeklyAdaptationHistory;
        Map<Integer,Integer> water,steps;Map<Integer,Double> sleep;
        Map<Integer,Set<String>> workouts,challenges,workoutDays;
        Map<Integer,Map<String,Boolean>> notifications;
        Map<Integer,Map<String,Integer>> dailyWater,dailySteps;
        Map<Integer,Map<String,Double>> dailySleep;
        Map<Integer,Map<String,Map<String,String>>> mealStatus,mealChoices;
        Map<Integer,Map<String,Set<String>>> dailyWorkouts;
        int uid,pid;
    }
    private static synchronized void saveToDisk(){
        Snapshot s=new Snapshot();s.users=new ArrayList<>(users);s.progressEntries=new ArrayList<>(progressEntries);s.weeklyAdaptations=new HashMap<>(weeklyAdaptations);s.weeklyAdaptationHistory=new HashMap<>(weeklyAdaptationHistory);
        s.water=new HashMap<>(water);s.steps=new HashMap<>(steps);s.sleep=new HashMap<>(sleep);
        s.workouts=new HashMap<>(workouts);s.challenges=new HashMap<>(challenges);s.workoutDays=new HashMap<>(workoutDays);
        s.notifications=new HashMap<>(notifications);s.dailyWater=new HashMap<>(dailyWater);s.dailySteps=new HashMap<>(dailySteps);
        s.dailySleep=new HashMap<>(dailySleep);s.mealStatus=new HashMap<>(mealStatus);s.mealChoices=new HashMap<>(mealChoices);
        s.dailyWorkouts=new HashMap<>(dailyWorkouts);s.uid=uid;s.pid=pid;
        try(ObjectOutputStream out=new ObjectOutputStream(new FileOutputStream(storeFile()))){out.writeObject(s);}
        catch(IOException e){System.err.println("FitWise: could not save data ("+e.getMessage()+")");}
    }
    private static synchronized boolean loadFromDisk(){
        File f=storeFile();if(!f.exists())return false;
        try(ObjectInputStream in=new ObjectInputStream(new FileInputStream(f))){
            Snapshot s=(Snapshot)in.readObject();
            if(s.users!=null)users.addAll(s.users);if(s.progressEntries!=null)progressEntries.addAll(s.progressEntries);if(s.weeklyAdaptations!=null)weeklyAdaptations.putAll(s.weeklyAdaptations);if(s.weeklyAdaptationHistory!=null)weeklyAdaptationHistory.putAll(s.weeklyAdaptationHistory);
            if(s.water!=null)water.putAll(s.water);if(s.steps!=null)steps.putAll(s.steps);if(s.sleep!=null)sleep.putAll(s.sleep);
            if(s.workouts!=null)workouts.putAll(s.workouts);if(s.challenges!=null)challenges.putAll(s.challenges);
            if(s.workoutDays!=null)workoutDays.putAll(s.workoutDays);if(s.notifications!=null)notifications.putAll(s.notifications);
            if(s.dailyWater!=null)dailyWater.putAll(s.dailyWater);if(s.dailySteps!=null)dailySteps.putAll(s.dailySteps);
            if(s.dailySleep!=null)dailySleep.putAll(s.dailySleep);if(s.mealStatus!=null)mealStatus.putAll(s.mealStatus);
            if(s.mealChoices!=null)mealChoices.putAll(s.mealChoices);if(s.dailyWorkouts!=null)dailyWorkouts.putAll(s.dailyWorkouts);
            uid=s.uid;pid=s.pid;return true;
        }catch(Exception e){System.err.println("FitWise: could not read saved data, starting fresh ("+e.getMessage()+")");return false;}
    }
    private static void repairLoadedMaps(){
        // Remove the retired public demo account from older local installations.
        users.removeIf(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase("student@test.com"));
        for(User u:users){
            init(u.getUserId());
            // Migrate old single-value trackers into today's bucket when present.
            String d=today();
            if(dailyWater.get(u.getUserId()).isEmpty() && water.getOrDefault(u.getUserId(),0)>0)
                dailyWater.get(u.getUserId()).put(d,water.get(u.getUserId()));
            if(dailySteps.get(u.getUserId()).isEmpty() && steps.getOrDefault(u.getUserId(),0)>0)
                dailySteps.get(u.getUserId()).put(d,steps.get(u.getUserId()));
            if(dailySleep.get(u.getUserId()).isEmpty() && sleep.getOrDefault(u.getUserId(),0.0)>0)
                dailySleep.get(u.getUserId()).put(d,sleep.get(u.getUserId()));
        }
        saveToDisk();
    }
}
