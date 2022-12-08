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

    private StringBuilder router;


    public Host() {
        super(icon);
//        System.out.println("Host " + this.getName() + " created.");
        router = new StringBuilder();
        ipv4 = new ArrayList<>();
        ipv6 = new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("Host{\n" +
                " mac='" + mac + '\'' +
                " router='" + router + '\'' +
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

    public void setRouter(){
        String s = this.port.getName();
        this.router.append(s.charAt(0));
        this.router.append(s.charAt(1));
    }

    public String getRouter() {
        return router.toString();
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
