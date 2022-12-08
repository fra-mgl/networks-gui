package base;

import com.google.gson.annotations.Expose;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

public class Host extends NetItem{

    static final Image icon = new Image("/media/desktop2_mac_FILL0_wght500_GRAD200_opsz48.png");

    @Expose
    private String mac;
    @Expose
    private List<String> ipv4;
    @Expose
    private List<String> ipv6;
    @Expose
    private Port port;

    private StringBuilder switch_l;


    public Host() {
        super(icon);
//        System.out.println("Host " + this.getName() + " created.");
        switch_l = new StringBuilder();
        ipv4 = new ArrayList<>();
        ipv6 = new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("Host{\n" +
                " mac='" + mac + '\'' +
                " switch='" + switch_l + '\'' +
                ",\n portArray= " + port +"\n");
        out.append("ipv4=\n");
        for(int i = 0; i < ipv4.size(); i++){
            out.append("\t").append(ipv4.get(i)).append("\n");
        }
        out.append("ipv6=\n");
        for(int i = 0; i < ipv6.size(); i++){
            out.append("\t").append(ipv6.get(i)).append("\n");
        }
        out.append("}");
        return out.toString();
    }

    public void setSwitch(){
        try{
            int number = Integer.parseInt(this.port.getDpid());
            this.switch_l.append("s"+number);
        }
        catch (NumberFormatException ex){
            ex.printStackTrace();
        }
    }

    public String getSwitch() {
        return switch_l .toString();
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
