package base;

import javafx.scene.image.Image;

public class Router extends NetItem{

    static final Image icon = new Image("/media/lan_FILL0_wght500_GRAD200_opsz48.png");

    public Router(String name) {
        super(name, icon);
    }
}
