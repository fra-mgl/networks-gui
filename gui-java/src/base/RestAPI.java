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

    static final String URL = "http://localhost:8080/topology/";
    static Switch[] getSwitch() throws Exception{
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(URL + "switches"))
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
    static Host[] getHosts() throws Exception{
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(URL + "hosts"))
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

    public String getName() {
        return name;
    }

    public String getDpid() {
        return dpid;
    }
}
