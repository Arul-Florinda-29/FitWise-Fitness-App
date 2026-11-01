package backend;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;

/** Lightweight JDK-only REST backend for the FitWise desktop prototype. */
public class FitWiseBackend {
    private static final File DB = new File(System.getProperty("user.home"), ".fitwise-backend/backend.ser");
    private static final SecureRandom RANDOM = new SecureRandom();
    private static Store store = load();

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/api/health", x -> respond(x, 200, "{\"ok\":true,\"service\":\"FitWise backend\"}"));
        server.createContext("/api/auth/register", FitWiseBackend::register);
        server.createContext("/api/auth/login", FitWiseBackend::login);
        server.createContext("/api/profile", FitWiseBackend::profile);
        server.createContext("/api/experience", FitWiseBackend::experience);
        server.createContext("/api/experience/history", FitWiseBackend::history);
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        System.out.println("FitWise backend running at http://127.0.0.1:" + port);
        System.out.println("Persistent database: " + DB.getAbsolutePath());
    }

    private static synchronized void register(HttpExchange x) throws IOException {
        if (!method(x, "POST")) { respond(x,405,"{\"ok\":false,\"message\":\"POST required\"}"); return; }
        Map<String,String> f = form(x);
        String email = lower(f.get("email")), password = f.get("password"), name = f.get("name");
        if (!validEmail(email) || password == null || password.length() < 8 || name == null || name.isBlank()) {
            respond(x,400,"{\"ok\":false,\"message\":\"Enter a valid email, name, and password of at least 8 characters.\"}"); return;
        }
        if (store.users.containsKey(email)) { respond(x,409,"{\"ok\":false,\"message\":\"Email is already registered on the backend.\"}"); return; }
        try {
            String salt = randomHex(16);
            Map<String,String> profile = new LinkedHashMap<>(f);
            profile.remove("password");
            store.users.put(email, new Account(email, name, salt, hash(password, salt), profile));
            String token = issueToken(email);
            save();
            respond(x,201,"{\"ok\":true,\"token\":"+q(token)+",\"message\":\"Account created\"}");
        } catch (Exception e) { respond(x,500,"{\"ok\":false,\"message\":\"Could not create account\"}"); }
    }

    private static synchronized void login(HttpExchange x) throws IOException {
        if (!method(x, "POST")) { respond(x,405,"{\"ok\":false,\"message\":\"POST required\"}"); return; }
        Map<String,String> f = form(x); String email = lower(f.get("email")); String password = f.get("password");
        Account a = store.users.get(email);
        if (a == null || password == null) { respond(x,401,"{\"ok\":false,\"message\":\"Invalid backend credentials\"}"); return; }
        try {
            if (!constantTimeEquals(a.passwordHash, hash(password,a.salt))) { respond(x,401,"{\"ok\":false,\"message\":\"Invalid backend credentials\"}"); return; }
            String token = issueToken(email); save();
            respond(x,200,"{\"ok\":true,\"token\":"+q(token)+",\"message\":\"Login successful\"}");
        } catch (Exception e) { respond(x,500,"{\"ok\":false,\"message\":\"Login failed\"}"); }
    }

    private static synchronized void profile(HttpExchange x) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(x.getRequestMethod())) { respond(x,204,""); return; }
        String email = authenticatedEmail(x);
        if (email == null) { respond(x,401,"{\"ok\":false,\"message\":\"Bearer token required\"}"); return; }
        Account a = store.users.get(email);
        if (a == null) { respond(x,404,"{\"ok\":false,\"message\":\"Profile not found\"}"); return; }
        if (method(x,"POST") || method(x,"PUT")) {
            Map<String,String> updates = form(x);
            String updatedName = updates.getOrDefault("name", a.name).trim();
            if (updatedName.isEmpty()) { respond(x,400,"{\"ok\":false,\"message\":\"Name is required.\"}"); return; }
            // Account email and credentials are immutable through profile editing.
            updates.remove("email"); updates.remove("password");
            a.name = updatedName;
            for (Map.Entry<String,String> entry : updates.entrySet())
                if (!"name".equals(entry.getKey())) a.profile.put(entry.getKey(), entry.getValue());
            save(); respond(x,200,"{\"ok\":true,\"message\":\"Profile updated\"}"); return;
        }
        if (!method(x,"GET")) { respond(x,405,"{\"ok\":false,\"message\":\"GET, POST or PUT required\"}"); return; }
        if (a == null) { respond(x,404,"{\"ok\":false,\"message\":\"Profile not found\"}"); return; }
        StringBuilder b = new StringBuilder("{\"ok\":true,\"profile\":{");
        Map<String,String> fields = new LinkedHashMap<>(a.profile);
        fields.put("name", a.name); fields.put("email", a.email);
        int i=0; for (Map.Entry<String,String> e : fields.entrySet()) {
            if (i++>0) b.append(','); b.append(q(e.getKey())).append(':').append(q(e.getValue()));
        }
        b.append("}}"); respond(x,200,b.toString());
    }

    private static synchronized void experience(HttpExchange x) throws IOException {
        if (!method(x,"POST")) { respond(x,405,"{\"ok\":false,\"message\":\"POST required\"}"); return; }
        String email = authenticatedEmail(x);
        if (email == null) { respond(x,401,"{\"ok\":false,\"message\":\"Bearer token required\"}"); return; }
        Map<String,String> f = form(x);
        String type=f.getOrDefault("type","general"), value=f.getOrDefault("value",""), date=f.getOrDefault("date",Instant.now().toString()), notes=f.getOrDefault("notes","");
        ExperienceRecord r = new ExperienceRecord(email,type,value,date,notes,Instant.now().toString());
        store.experiences.computeIfAbsent(email,k->new ArrayList<>()).add(r); save();
        respond(x,201,"{\"ok\":true,\"message\":\"Experience saved\"}");
    }

    private static synchronized void history(HttpExchange x) throws IOException {
        if (!method(x,"GET")) { respond(x,405,"{\"ok\":false,\"message\":\"GET required\"}"); return; }
        String email=authenticatedEmail(x);
        if(email==null){respond(x,401,"{\"ok\":false,\"message\":\"Bearer token required\"}");return;}
        StringBuilder b=new StringBuilder("{\"ok\":true,\"records\":[");
        List<ExperienceRecord> rs=store.experiences.getOrDefault(email,Collections.emptyList());
        for(int i=0;i<rs.size();i++){ExperienceRecord r=rs.get(i);if(i>0)b.append(',');b.append("{\"type\":").append(q(r.type)).append(",\"value\":").append(q(r.value)).append(",\"date\":").append(q(r.date)).append(",\"notes\":").append(q(r.notes)).append(",\"savedAt\":").append(q(r.savedAt)).append('}');}
        b.append("]}"); respond(x,200,b.toString());
    }

    private static String authenticatedEmail(HttpExchange x) {
        String h=x.getRequestHeaders().getFirst("Authorization");
        if(h==null||!h.startsWith("Bearer "))return null;
        return store.tokens.get(h.substring(7).trim());
    }
    private static String issueToken(String email){String token=randomHex(32);store.tokens.put(token,email);return token;}
    private static boolean method(HttpExchange x,String m){return m.equalsIgnoreCase(x.getRequestMethod());}
    private static Map<String,String> form(HttpExchange x) throws IOException {
        String body=new String(x.getRequestBody().readAllBytes(),StandardCharsets.UTF_8); Map<String,String> m=new HashMap<>();
        for(String part:body.split("&")){int i=part.indexOf('=');if(i>=0)m.put(URLDecoder.decode(part.substring(0,i),StandardCharsets.UTF_8),URLDecoder.decode(part.substring(i+1),StandardCharsets.UTF_8));}
        return m;
    }
    private static void respond(HttpExchange x,int status,String body)throws IOException{
        String origin=x.getRequestHeaders().getFirst("Origin");
        if(origin!=null && (origin.equals("http://127.0.0.1") || origin.equals("http://localhost")))
            x.getResponseHeaders().set("Access-Control-Allow-Origin",origin);
        x.getResponseHeaders().set("Vary","Origin");
        x.getResponseHeaders().set("Access-Control-Allow-Methods","GET, POST, PUT, OPTIONS");
        x.getResponseHeaders().set("Access-Control-Allow-Headers","Content-Type, Authorization");
        x.getResponseHeaders().set("Content-Type","application/json; charset=utf-8");
        if("OPTIONS".equalsIgnoreCase(x.getRequestMethod())) { x.getResponseHeaders().set("Access-Control-Max-Age","600"); x.sendResponseHeaders(204,-1); x.close(); return; }
        byte[] payload=body.getBytes(StandardCharsets.UTF_8);x.sendResponseHeaders(status,payload.length);try(OutputStream o=x.getResponseBody()){o.write(payload);}finally{x.close();}
    }
    private static String lower(String s){return s==null?"":s.trim().toLowerCase(Locale.ROOT);}
    private static boolean validEmail(String email) {
        return email != null && email.length() <= 254 && email.matches("(?i)^[A-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?(?:\\.[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?)+$");
    }
    private static String randomHex(int n){byte[] b=new byte[n];RANDOM.nextBytes(b);StringBuilder s=new StringBuilder();for(byte v:b)s.append(String.format("%02x",v));return s.toString();}
    private static String hash(String password,String salt)throws Exception{byte[] sb=HexFormat.of().parseHex(salt);KeySpec spec=new PBEKeySpec(password.toCharArray(),sb,120000,256);byte[] out=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();return HexFormat.of().formatHex(out);}
    private static boolean constantTimeEquals(String a,String b){return a!=null&&b!=null&&java.security.MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8),b.getBytes(StandardCharsets.UTF_8));}
    private static String q(String s){if(s==null)return "null";return "\""+s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r")+"\"";}
    private static synchronized void save(){try{File p=DB.getParentFile();if(!p.exists())p.mkdirs();try(ObjectOutputStream o=new ObjectOutputStream(new FileOutputStream(DB))){o.writeObject(store);}}catch(Exception e){System.err.println("Backend database save failed: "+e.getMessage());}}
    private static Store load(){if(DB.exists())try(ObjectInputStream i=new ObjectInputStream(new FileInputStream(DB))){Object o=i.readObject();if(o instanceof Store)return (Store)o;}catch(Exception e){System.err.println("Starting fresh backend store: "+e.getMessage());}return new Store();}

    private static class Store implements Serializable {private static final long serialVersionUID=1L; Map<String,Account> users=new HashMap<>();Map<String,String> tokens=new HashMap<>();Map<String,List<ExperienceRecord>> experiences=new HashMap<>();}
    private static class Account implements Serializable {private static final long serialVersionUID=1L;String email,name,salt,passwordHash;Map<String,String> profile;Account(String e,String n,String s,String h,Map<String,String> p){email=e;name=n;salt=s;passwordHash=h;profile=p;}}
    private static class ExperienceRecord implements Serializable {private static final long serialVersionUID=1L;String email,type,value,date,notes,savedAt;ExperienceRecord(String e,String t,String v,String d,String n,String s){email=e;type=t;value=v;date=d;notes=n;savedAt=s;}}
}
