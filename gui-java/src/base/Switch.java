package base;

import com.google.gson.annotations.Expose;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Switch extends NetItem{

    private String name;
    private int id_s;
    @Expose private String dpid;
    @Expose private List<Port> ports;

    static final Image icon = new Image("/base/images/switch_edit.png");

    private List<Host> hostLinkList;
    public List<Switch> switchLinkList;


    public Switch() {
        super(icon);
        ports = new ArrayList<>();
        hostLinkList = new ArrayList<>();
        switchLinkList = new ArrayList<>();
    }

    public Switch(int s){
        super(icon);
        this.id_s = s;
    }

    public void addHostLink(Host i){
        hostLinkList.add(i);
    }
    public void addSwitchLink(Switch i){
        switchLinkList.add(i);
    }
    public int getHostLinkNumber(){
        return hostLinkList.size();
    }
    public int getSwitchLinkNumber(){
        return switchLinkList.size();
    }
    public Host getHostFromLink(int i){
        return hostLinkList.get(i);
    }
    public Switch getSwitchFromLink(int i){
        return switchLinkList.get(i);
    }
    public boolean searchInSwitchLinks(int dpid){
        if(switchLinkList.contains(new Switch(dpid))){
            return true;
        }else{
            return false;
        }
    }
    public int getPortNumber(){return ports.size();}

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("Switch{\n" +
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
        out.append("}");
        return out.toString();
    }

    public void setName(){
        try{
            int number = Integer.parseInt(dpid, 16);
            this.name = "S"+number;
        }
        catch (NumberFormatException ex){
            ex.printStackTrace();
        }
    }

    public void setID(){
        try{
            int number = Integer.parseInt(dpid, 16);
            this.id_s = number;
        }
        catch (NumberFormatException ex){
            ex.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Switch aSwitch = (Switch) o;
        return id_s == aSwitch.id_s;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_s);
    }

    public String getName() {
        return name;
    }

    public String getDpid() {
        return dpid;
    }

    public int getIdS() {
        return id_s;
    }

    public List<Port> getPorts() {
        return ports;
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
}

