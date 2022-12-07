package base;

import com.google.gson.annotations.Expose;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

public class Router extends NetItem{

    @Expose private String dpid;
    @Expose private List<Port> ports;

    static final Image icon = new Image("/media/lan_FILL0_wght500_GRAD200_opsz48.png");

    private List<Host> hostLinkList;
    private List<Router> routerLinkList;


    public Router() {
        super("R", icon);
        ports = new ArrayList<>();
        hostLinkList = new ArrayList<>();
        routerLinkList = new ArrayList<>();
    }

    public void addHostLink(Host i){
        hostLinkList.add(i);
    }
    public void addRouterLink(Router i){
        routerLinkList.add(i);
    }
    public int getHostLinkNumber(){
        return hostLinkList.size();
    }
    public int getRouterLinkNumber(){
        return routerLinkList.size();
    }
    public Host getHostFromLink(int i){
        return hostLinkList.get(i);
    }
    public Router getRouterFromLink(int i){
        return routerLinkList.get(i);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("SwitchJson{\n" +
                " dpid='" + dpid + '\'' +
                ",\n portArray=\n");
        for(int i = 0; i < ports.size(); i++){
            out.append("\t").append(ports.get(i).toString()).append("\n");
        }
        out.append("}");
        return out.toString();
    }
}

class Port{
    @Expose private String dpid;
    @Expose private String port_no;
    @Expose private String hw_addr;
    @Expose private String name;

    @Override
    public String toString() {
        return "Port{" +
                "dpid='" + dpid + '\'' +
                ", port_no='" + port_no + '\'' +
                ", hw_addr='" + hw_addr + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}