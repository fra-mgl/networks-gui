package base;

import com.google.gson.annotations.Expose;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

public class Host extends NetItem{

    static final Image icon = new Image("/base/images/host_edit.png");

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
        ipv4 = new ArrayList<>();
        ipv6 = new ArrayList<>();
    }


    @Override
    public String toString() {
        try{
            return ipv4.get(0);
        }catch(IndexOutOfBoundsException e){
            return null;
        }
    }

    public void setSwitch(){
        try{
            int number = Integer.parseInt(this.port.getDpid(), 16);
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

    public String getIPv4(){
        try{
            return ipv4.get(0);
        }catch(IndexOutOfBoundsException e){
            return null;
        }
    }
}
