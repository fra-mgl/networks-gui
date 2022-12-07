package base;

import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

public class Host extends NetItem{

    static final Image icon = new Image("/media/desktop_mac_FILL0_wght500_GRAD200_opsz48.png");

    public Host(String name) {
        super(name, icon);
//        System.out.println("Host " + this.getName() + " created.");
    }


}
