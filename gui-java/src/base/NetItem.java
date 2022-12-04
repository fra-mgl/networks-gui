package base;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

public class NetItem extends ImageView {
    private String name;

    public NetItem(String name, Image icon) {
        this.setImage(icon);
//        Rectangle2D viewportRect = new Rectangle2D(50, 50, 50, 50);
//        this.setViewport(viewportRect);
        this.setFitWidth(50);
        this.setFitHeight(50);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void showItem(){

    }
}
