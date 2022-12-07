package base;



import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class SwitchJson {
    @Expose private String dpid;
    @Expose private List<Port> ports = new ArrayList<>();;
    private int ciaoo;

    public SwitchJson() {
        ciaoo = 19;
    }

    public void setCiaoo(int ciaoo) {
        this.ciaoo = ciaoo;
    }

    public String getDpid() {
        return dpid;
    }

    public Port getPortArray(int i) {
        return ports.get(i);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("SwitchJson{\n" +
                " dpid='" + dpid + '\'' +
                ",\n portArray=\n");
        for(int i = 0; i < ports.size(); i++){
            out.append("\t").append(ports.get(i).toString()).append("\n");
        }
        out.append("\n ciaoo = " + ciaoo + "\n}");
        return out.toString();
    }
}



