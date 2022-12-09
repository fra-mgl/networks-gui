package base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

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
//            System.out.println(response.statusCode());
            if(response.statusCode() != 200){
                System.err.println("EXEP- RestAPI - ERROR: not 200");
                throw new Exception("Error - not 200");
            }
            String json = response.body();
//            System.out.println(json);
            return gson.fromJson(json, Switch[].class);
        } catch (Exception e){
            System.err.println("EXEP- RestAPI");
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
//            System.out.println(response.statusCode());
            if(response.statusCode() != 200){
                System.err.println("EXEP- RestAPI - ERROR: not 200");
                throw new Exception("Error - not 200");
            }
            String json = response.body();
//            System.out.println(json);
            return gson.fromJson(json, Router[].class);
        } catch (Exception e){
            System.err.println("EXEP- RestAPI");
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
//            System.out.println(response.statusCode());
            if(response.statusCode() != 200){
                System.err.println("EXEP- RestAPI - ERROR: not 200");
                throw new Exception("Error - not 200");
            }
            String json = response.body();
            System.out.println(json);
            return gson.fromJson(json, Host[].class);
        } catch (Exception e){
            System.err.println("EXEP- RestAPI");
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
//            System.out.println(response.statusCode());
//            System.out.println(response.body());
            if(response.statusCode() != 200){
                System.err.println("EXEP- RestAPI - ERROR: not 200");
                throw new Exception("Error - not 200");
            }
            String json = response.body();
//            System.out.println(json);
            return gson.fromJson(json, LinkJson[].class);
        } catch (Exception e){
            System.err.println("EXEP- RestAPI");
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
//            System.out.println(response.statusCode());
//            System.out.println(response.body());
            if(response.statusCode() != 200){
                System.err.println("EXEP- RestAPI - ERROR: not 200");
                throw new Exception("Error - not 200");
            }
            String json = response.body();
//            System.out.println(json);
            return gson.fromJson(json, TableEntrySwitch[].class);
        } catch (Exception e){
            System.err.println("EXEP- RestAPI");
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
//            System.out.println(response.statusCode());
//            System.out.println(response.body());
            if(response.statusCode() != 200){
                System.err.println("EXEP- RestAPI - ERROR: not 200");
                throw new Exception("Error - not 200");
            }
            String json = response.body();
//            System.out.println(json);
            return gson.fromJson(json, TableEntryRouter[].class);
        } catch (Exception e){
            System.err.println("EXEP- RestAPI");
            e.printStackTrace();
            return null;
        }
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
                ",\n\t\thw_addr='" + hw_addr + '\'' +
                ",\n\t\tname='" + name + '\'';
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
            int number = Integer.parseInt(src.getDpid());
            return number;
        }
        catch (NumberFormatException ex){
            ex.printStackTrace();
            return -1;
        }
    }
    public int getDst(){
        try{
            int number = Integer.parseInt(dst.getDpid());
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
