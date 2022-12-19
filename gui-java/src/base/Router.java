package base;

import com.google.gson.annotations.Expose;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Router extends NetItem{
    private String name;
    private int id_r;
    @Expose private String dpid;
    @Expose private List<Port> ports;

    static final Image icon = new Image("/base/images/router_edit.png");
    private List<Switch> switchLinkList;
    private List<Router> routerLinkList;

    public Router() {
        super(icon);
        ports = new ArrayList<>();
        routerLinkList = new ArrayList<>();
        switchLinkList = new ArrayList<>();
    }
    public Router(int i){
        super(icon);
        id_r = i;
        ports = new ArrayList<>();
        routerLinkList = new ArrayList<>();
        switchLinkList = new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("Router{\n" +
                " name='" + name + '\'' +
                " dpid='" + dpid + '\'' +
                ",\n portArray=\n");
        for(int i = 0; i < ports.size(); i++){
            out.append("\t").append(ports.get(i).toString()).append("\n");
        }
        out.append(",\n Link with Switches=\n");
        for(int i = 0; i < switchLinkList.size(); i++){
            out.append("\t").append(switchLinkList.get(i).getIdS()).append("\n");
        }
        out.append(",\n Link with Routers=\n");
        for(int i = 0; i < routerLinkList.size(); i++){
            out.append("\t").append(routerLinkList.get(i).getIdR()).append("\n");
        }
        out.append("}");
        return out.toString();
    }

    public void setName(){
        try{
            int number = Integer.parseInt(dpid, 16);
            this.name = "R"+number;
        }
        catch (NumberFormatException ex){
            ex.printStackTrace();
        }
    }
    public void setID(){
        try{
            int number = Integer.parseInt(dpid, 16);
            this.id_r = number;
        }
        catch (NumberFormatException ex){
            ex.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Router router = (Router) o;
        return id_r == router.id_r;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_r);
    }

    public int getSwitchLinkNumber(){
       return switchLinkList.size();
    }
    public Switch getSwitchFromLink(int i){
        return switchLinkList.get(i);
    }

    public int getRouterLinkNumber(){
        return routerLinkList.size();
    }
    public Router getRouterFromLink(int i){
        return routerLinkList.get(i);
    }
    public int getIdR() {
        return id_r;
    }

    public int getIdFromDpid(){
        /* used when id cannot be set*/
        try{
            int number = Integer.parseInt(dpid, 16);
            return number;
        }
        catch (NumberFormatException ex){
            ex.printStackTrace();
            return -1;
        }
    }

    public String getName() {
        return name;
    }

    public String getDpid() {
        return dpid;
    }

    public List<Port> getPorts() {
        return ports;
    }

    public void addRouterLink(Router i){
        this.routerLinkList.add(i);
    }
    public void addSwitchLink(Switch i){
        this.switchLinkList.add(i);
    }

    public boolean searchInSRouterLinks(int dpid){
        if(routerLinkList.contains(new Router(dpid))){
            return true;
        }else{
            return false;
        }
    }
}
