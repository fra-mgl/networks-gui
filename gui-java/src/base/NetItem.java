package base;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class NetItem extends ImageView {

    static final int SIZE = 64;
    static final int HALF_SIZE = SIZE/2;

//    transient protected String name;
    transient private double angle;

//    static public NetItem fromString(String json){
//
//    }

    /* centerPoint = (centerX, centerY) -> represents the center of the frame
    *  anchorPoint = (anchorX, anchorY) -> coordinates when set position in anchor frame
    */
    private double centerX;
    private double centerY;
    private double anchorX;
    private double anchorY;

    public NetItem(Image icon) {
        this.setImage(icon);
//        Rectangle2D viewportRect = new Rectangle2D(50, 50, 50, 50);
//        this.setViewport(viewportRect);
        this.setFitWidth(SIZE);
        this.setFitHeight(SIZE);
        this.angle = 0.0;
    }

    protected void setCoordinates(double cx, double cy){
        this.centerX = cx;
        this.anchorX = cx - HALF_SIZE;
        this.centerY = cy;
        this.anchorY = cy - HALF_SIZE;
    }

    boolean computeCoords(double angle, double radius, double x, double y){
        double tmpX, tmpY;
        double angleInRadians = Math.toRadians(angle);
        tmpX = x + Math.cos(angleInRadians)*radius;
        tmpY = x - Math.sin(angleInRadians)*radius;
        this.setCoordinates(tmpX, tmpY);
        return true;
    }

    public double getCenterX() {
        return centerX;
    }

    public double getCenterY() {
        return centerY;
    }

    public double getAnchorX() {
        return anchorX;
    }

    public double getAnchorY() {
        return anchorY;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }
}
