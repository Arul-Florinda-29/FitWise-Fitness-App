package data;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** HTTP client for the optional local FitWise REST backend. */
public final class BackendClient {
    private static final String BASE = System.getProperty("fitwise.backend.url", "http://127.0.0.1:8080");
    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(2)).build();
    private static volatile String token;
    private BackendClient() {}

    public static boolean isAvailable() {
        try { HttpRequest r=HttpRequest.newBuilder(URI.create(BASE+"/api/health")).timeout(java.time.Duration.ofSeconds(2)).GET().build();return CLIENT.send(r,HttpResponse.BodyHandlers.ofString()).statusCode()==200; }
        catch(Exception e){return false;}
    }
    public static String register(Map<String,String> fields) throws Exception { return post("/api/auth/register",fields,false); }
    public static String login(String email,String password) throws Exception { return post("/api/auth/login",Map.of("email",email,"password",password),false); }
    public static Map<String,String> getProfile() throws Exception {
        if (token == null) throw new IllegalStateException("Please sign in to the backend first.");
        HttpRequest r = HttpRequest.newBuilder(URI.create(BASE+"/api/profile")).timeout(java.time.Duration.ofSeconds(8)).header("Authorization","Bearer "+token).GET().build();
        HttpResponse<String> response = CLIENT.send(r,HttpResponse.BodyHandlers.ofString());
        if (response.statusCode()<200 || response.statusCode()>=300 || !response.body().contains("\"ok\":true"))
            throw new IllegalStateException(extractMessage(response.body(),"Could not load your saved profile."));
        String json = response.body(); int start=json.indexOf("\"profile\":{");
        if(start<0) throw new IllegalStateException("The backend returned an invalid profile response.");
        start += 11; int end=json.indexOf('}',start); if(end<0) throw new IllegalStateException("The backend profile is incomplete.");
        String section=json.substring(start,end);
        Pattern pattern=Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"");
        Matcher m=pattern.matcher(section); Map<String,String> result=new LinkedHashMap<>();
        while(m.find()) result.put(unescape(m.group(1)),unescape(m.group(2)));
        return result;
    }
    private static String unescape(String s) { return s.replace("\\\"","\"").replace("\\n","\n").replace("\\r","\r").replace("\\t","\t").replace("\\\\","\\"); }

    public static String updateProfile(Map<String,String> fields) throws Exception {
        if (token == null) throw new IllegalStateException("Please sign in to the backend first.");
        return sendProfileUpdate(fields);
    }
    private static String sendProfileUpdate(Map<String,String> fields) throws Exception {
        StringBuilder body=new StringBuilder();
        for(Map.Entry<String,String> e:fields.entrySet()){if(body.length()>0)body.append('&');body.append(enc(e.getKey())).append('=').append(enc(e.getValue()));}
        HttpRequest r=HttpRequest.newBuilder(URI.create(BASE+"/api/profile")).timeout(java.time.Duration.ofSeconds(8))
            .header("Authorization","Bearer "+token).header("Content-Type","application/x-www-form-urlencoded; charset=UTF-8")
            .POST(HttpRequest.BodyPublishers.ofString(body.toString())).build();
        HttpResponse<String> response=CLIENT.send(r,HttpResponse.BodyHandlers.ofString());
        if(response.statusCode()<200||response.statusCode()>=300||!response.body().contains("\"ok\":true"))
            throw new IllegalStateException(extractMessage(response.body(),"Could not save your profile."));
        return response.body();
    }

    public static String saveExperience(String type,String value,String date,String notes) throws Exception {
        if(token==null)throw new IllegalStateException("Please sign in to the backend first.");
        return post("/api/experience",Map.of("type",type,"value",value,"date",date,"notes",notes),true);
    }
    private static String post(String path,Map<String,String> fields,boolean auth)throws Exception {
        StringBuilder body=new StringBuilder();for(Map.Entry<String,String> e:fields.entrySet()){if(body.length()>0)body.append('&');body.append(enc(e.getKey())).append('=').append(enc(e.getValue()));}
        HttpRequest.Builder b=HttpRequest.newBuilder(URI.create(BASE+path)).timeout(java.time.Duration.ofSeconds(8)).header("Content-Type","application/x-www-form-urlencoded; charset=UTF-8").POST(HttpRequest.BodyPublishers.ofString(body.toString()));
        if(auth){if(token==null)throw new IllegalStateException("Backend session missing");b.header("Authorization","Bearer "+token);}
        HttpResponse<String> r=CLIENT.send(b.build(),HttpResponse.BodyHandlers.ofString());String response=r.body();
        if(r.statusCode()<200||r.statusCode()>=300)throw new IllegalStateException(extractMessage(response,"Backend request failed (HTTP "+r.statusCode()+")."));
        if(!response.contains("\"ok\":true"))throw new IllegalStateException(extractMessage(response,"Backend request failed."));
        if(response.contains("\"token\":")){int i=response.indexOf("\"token\":\"")+9;int j=response.indexOf('"',i);if(i>=9&&j>i)token=response.substring(i,j);}
        return response;
    }
    private static String extractMessage(String s,String fallback){try{int i=s.indexOf("\"message\":\"")+11;int j=s.indexOf('"',i);if(i>=11&&j>i)return s.substring(i,j);}catch(Exception ignored){}return fallback;}
    private static String enc(String s){return URLEncoder.encode(s==null?"":s,StandardCharsets.UTF_8);}
}
