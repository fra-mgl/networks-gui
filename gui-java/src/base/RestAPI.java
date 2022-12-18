package base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class RestAPI {

    static public Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    static final String URL = "http://localhost:8080/";
    static Switch[] getSwitch() throws Exception{
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(URL + "topology/l2switches"))
                    .GET().build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() != 200){
                System.err.println("EXC - RestAPI(getSwitch) - ERROR: not 200");
                throw new Exception("Error - not 200");
            }
            String json = response.body();
            return gson.fromJson(json, Switch[].class);
        } catch (Exception e){
            System.err.println("EXC - RestAPI(getSwitch)");
            e.printStackTrace();
            return null;
        }
    }
    static Router[] getRouter() throws Exception{
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(URL + "topology/l3switches"))
                    .GET().build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() != 200){
                System.err.println("EXC - RestAPI(getRouter) - ERROR: not 200");
                throw new Exception("Error - not 200");
            }
            String json = response.body();
            return gson.fromJson(json, Router[].class);
        } catch (Exception e){
            System.err.println("EXC - RestAPI(getRouter)");
            e.printStackTrace();
            return null;
        }
    }
    static Host[] getHosts() throws Exception{
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(URL + "topology/hosts"))
                    .GET().build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() != 200){
                System.err.println("EXC - RestAPI(getHosts) - ERROR: not 200");
                throw new Exception("Error - not 200");
            }
            String json = response.body();
            return gson.fromJson(json, Host[].class);
        } catch (Exception e){
            System.err.println("EXC - RestAPI(getHosts)");
            e.printStackTrace();
            return null;
        }
    }
    static LinkJson[] getLinks() throws Exception{
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(URL + "topology/links"))
                    .GET().build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() != 200){
                System.err.println("EXC - RestAPI(getLinks) - ERROR: not 200");
                throw new Exception("Error - not 200");
            }
            String json = response.body();
            return gson.fromJson(json, LinkJson[].class);
        } catch (Exception e){
            System.err.println("EXC - RestAPI(getLinks)");
            e.printStackTrace();
            return null;
        }
    }

    static TableEntrySwitch[] getMacTable(String dpid){
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(URL + "mactable/" + dpid))
                    .GET().build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() != 200){
                System.err.println("EXC - RestAPI(getMAcTable) - ERROR: not 200");
                throw new Exception("Error - not 200");
            }
            String json = response.body();
            return gson.fromJson(json, TableEntrySwitch[].class);
        } catch (Exception e){
            System.err.println("EXC - RestAPI(getMacTable)");
            e.printStackTrace();
            return null;
        }
    }
    static TableEntryRouter[] getIPTable(String dpid){
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(URL + "iptable/" + dpid))
                    .GET().build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() != 200){
                System.err.println("EXC - RestAPI(getIPTable) - ERROR: not 200");
                throw new Exception("Error - not 200");
            }
            String json = response.body();
            return gson.fromJson(json, TableEntryRouter[].class);
        } catch (Exception e){
            System.err.println("EXC - RestAPI(getIPTable)");
            e.printStackTrace();
            return null;
        }
    }
    static int postNetConf(String requestBody){
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4000/netConf"))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() != 200){
                System.err.println("EXC - RestAPI(postNetConf) - ERROR: not 200");
                System.err.println(response.statusCode());
                System.err.println(response.body());
                throw new Exception("Error - not 200");
            }
            return response.statusCode();
        } catch (Exception e){
            System.err.println("EXC - RestAPI(postNetConf)");
            e.printStackTrace();
            return -1;
        }
    }

    static int validateNetConf(String conf, Map<Integer, Switch> switches){
        List<ConfigItem> l = new ArrayList<>();
        try{
            l.addAll(Arrays.asList(gson.fromJson(conf, ConfigItem[].class)));
        }catch (Exception e){
            System.err.println("EXC - gson: json format not valid");
            return -1;
        }

        int dpid;
        Map<Integer, Switch> routers = new TreeMap<>();
        for (ConfigItem ci: l) {
            dpid = ci.getDpid();
            routers.put(dpid, switches.get(dpid));
        }
        Map<Integer,Integer> portCounter = new TreeMap<>();
        // mapping : switch's dpid, number of ports
        for (Map.Entry<Integer, Switch> entry : routers.entrySet()) {
            portCounter.put(entry.getKey(), 0);
        }

        for (ConfigItem i: l) {
            dpid = i.getDpid();
            portCounter.put(dpid, portCounter.get((dpid)) +1);
        }
        boolean valid = true;

        for (Map.Entry<Integer, Integer> entry : portCounter.entrySet()) {
            if(portCounter.get(entry.getKey()) != routers.get(entry.getKey()).getPortNumber()){
                valid = false;
                break;
            }
        }

        if (valid) {
            return 1;
        }else {
            return -1;
        }
    }

    static ExplorePath getPath(String src, String dst){
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8080/explore/" + src + "/" + dst))
                    .GET().build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() != 200){
                System.err.println("EXC - RestAPI(getPath) - ERROR: not 200");
                throw new Exception("Error - not 200");
            }
            String json = response.body();
            return gson.fromJson(json, ExplorePath.class);
        } catch (Exception e){
            System.err.println("EXC - RestAPI(getPath)");
            e.printStackTrace();
            return null;
        }
    }
    
    
}

class ConfigItem{
    /* EXAMPLE
        "dpid": "0000000000000001",
        "port_no": "00000001",
        "ip": "10.0.1.254/24"
     */
    @Expose private String dpid;
    @Expose private String port_no;
    @Expose private String ip;

    public int getDpid(){
        return Integer.parseInt(dpid, 16);
    }
}

class Port{
    @Expose
    private String dpid;
    @Expose private String port_no;
    @Expose private String hw_addr;
    @Expose private String name;

    @Override
    public String toString() {
        return "Port" +
                "\n\t\tdpid='" + dpid + '\'' +
                ",\n\t\tport_no='" + port_no + '\'' +
                ",\n\t\thw_addr='" + hw_addr + '\'';
    }

    public String toStringRouter() {
        return "Port" +
                "\n\t\tdpid='" + dpid + '\'' +
                ",\n\t\tip_addr='" + port_no + '\'' +
                ",\n\t\tname='" + name + '\'';
    }

    public String getName() {
        return name;
    }

    public String getDpid() {
        return dpid;
    }
}

class LinkJson{
    @Expose private Port src;
    @Expose private Port dst;

    public LinkJson() {}

    public int getSrc(){
        try{
            int number = Integer.parseInt(src.getDpid(), 16);
            return number;
        }
        catch (NumberFormatException ex){
            ex.printStackTrace();
            return -1;
        }
    }
    public int getDst(){
        try{
            int number = Integer.parseInt(dst.getDpid(), 16);
            return number;
        }
        catch (NumberFormatException ex){
            ex.printStackTrace();
            return -1;
        }
    }
}

class TableEntrySwitch {
    @Expose private String mac;
    @Expose private int port;


    @Override
    public String toString() {
        return "mac: " + mac + "\tport: " + port;
    }
}

class TableEntryRouter{
    @Expose String destination;
    @Expose String gateway;

    @Override
    public String toString() {
        return "destination: " + destination + "\tgateway: " + gateway;
    }
}

class ExplorePath{
    @Expose List<Integer> path = new ArrayList<>();

    public List<Integer> getList(){
        return path;
    }
}