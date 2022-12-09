package base;

import com.google.gson.annotations.Expose;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

public class Host extends NetItem{

    static final Image icon = new Image("/media/host_edit.png");

    @Expose
    private String mac;
    @Expose
    private List<String> ipv4;
    @Expose
    private List<String> ipv6;
    @Expose
    private Port port;

    private int switch_l;


    public Host() {
        super(icon);
//        System.out.println("Host " + this.getName() + " created.");
        ipv4 = new ArrayList<>();
        ipv6 = new ArrayList<>();
    }

//    @Override
//    public String toString() {
//        StringBuilder out = new StringBuilder("Host{\n" +
//                " mac='" + mac + '\'' +
//                " switch='" + switch_l + '\'' +
//                ",\n portArray= " + port +"\n");
//        out.append("ipv4=\n");
//        for(int i = 0; i < ipv4.size(); i++){
//            out.append("\t").append(ipv4.get(i)).append("\n");
//        }
//        out.append("ipv6=\n");
//        for(int i = 0; i < ipv6.size(); i++){
//            out.append("\t").append(ipv6.get(i)).append("\n");
//        }
//        out.append("}");
//        return out.toString();
//    }


    @Override
    public String toString() {
        return mac;
    }

    public void setSwitch(){
        try{
            int number = Integer.parseInt(this.port.getDpid());
            switch_l = number;
        }
        catch (NumberFormatException ex){
            ex.printStackTrace();
        }
    }

    public int getSwitch() {
        return switch_l;
    }

    public String getMac() {
        return mac;
    }

    public List<String> getIpv4() {
        return ipv4;
    }

    public List<String> getIpv6() {
        return ipv6;
    }

    public Port getPort() {
        return port;
    }
}
