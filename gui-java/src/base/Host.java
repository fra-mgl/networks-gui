package base;

import javafx.scene.image.Image;

public class Host extends NetItem{

    static final Image icon = new Image("/media/desktop_mac_FILL0_wght500_GRAD200_opsz48.png");

    public Host(String name) {
        super(name, icon);
        System.out.println("Host " + this.getName() + " created.");
    }
}
