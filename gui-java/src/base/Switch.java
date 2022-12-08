package base;

import com.google.gson.annotations.Expose;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Switch extends NetItem{

    private String name;
    @Expose private String dpid;
    @Expose private List<Port> ports;

    static final Image icon = new Image("/media/lan2_FILL0_wght500_GRAD200_opsz48.png");

    private List<Host> hostLinkList;
    private List<Switch> switchLinkList;


    public Switch() {
        super(icon);
        ports = new ArrayList<>();
        hostLinkList = new ArrayList<>();
        switchLinkList = new ArrayList<>();
    }

    public Switch(String s){
        super(icon);
        this.name = s;
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

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("Switch{\n" +
                " name='" + name + '\'' +
                " dpid='" + dpid + '\'' +
                ",\n portArray=\n");
        for(int i = 0; i < ports.size(); i++){
            out.append("\t").append(ports.get(i).toString()).append("\n");
        }
        out.append("}");
        return out.toString();
    }

    public void setName(){
        try{
            int number = Integer.parseInt(dpid);
//            System.out.println(number);
            this.name = "s"+number;
        }
        catch (NumberFormatException ex){
            ex.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Switch switch_i = (Switch) o;
        return name.equals(switch_i.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
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
}

