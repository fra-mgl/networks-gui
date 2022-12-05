package base;

import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

public class Router extends NetItem{

    static final Image icon = new Image("/media/lan_FILL0_wght500_GRAD200_opsz48.png");

    private List<Host> hostLinkList;
    private List<Router> routerLinkList;


    public Router(String name) {
        super(name, icon);
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
}
