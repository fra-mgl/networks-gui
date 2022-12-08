package base;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.util.*;

public class Network {

    /* GUI Elements */
    private double width;
    private double height;
    public StackPane netStack;
    private AnchorPane graphAnchor;
    private AnchorPane connectionsAnchor;
    private AnchorPane itemsAnchor;
    private Circle graphStage;
    private static final double graphPadding = 50.0;
    private static double circleRadius;
    private static double hostRadius;
    private static double switchRadius;
    private static double routerRadius;
    private double centerX;
    private double centerY;


    // serve avere una collection di ogni elemento?
    // sì se facciamo dispay delle singole info
    // o anche no, gestiamo sull'immagine che accede direttamente alle properties dell'elemento

    public List<Host> hostList;
    public Map<Integer,Switch> switchList;
    public Map<Integer,Router> routerList;
    public List<LinkJson> linkList;

//    public List<NotHost> notHostList;


    public Network(double w, double h) {
        /* GRAPH */
        this.width = w;
        this.height = h;
        netStack = new StackPane();
        graphAnchor = new AnchorPane();
        connectionsAnchor = new AnchorPane();
        itemsAnchor = new AnchorPane();
        netStack.getChildren().addAll(graphAnchor, connectionsAnchor, itemsAnchor);

//        hostRadius = width/2.0 - graphPadding;
        circleRadius = width / 2.0;
        graphStage = new Circle(circleRadius, Color.CADETBLUE);
        /* reduce a bit hostRadius to let hosts to be inside the circle */
        hostRadius = circleRadius - circleRadius * 0.15;
        switchRadius = circleRadius * 0.66;
        routerRadius = circleRadius * 0.33;
        graphStage.setCenterX(width / 2.0);
        graphStage.setCenterY(height / 2.0);

        Circle tmpCenter = new Circle(10.0, Color.RED);
        tmpCenter.setCenterX(width / 2.0);
        tmpCenter.setCenterY(height / 2.0);

        this.centerX = graphStage.getCenterX();
        this.centerY = graphStage.getCenterY();
//        graphAnchor.getChildren().add(graphStage);
        graphAnchor.getChildren().addAll(graphStage, tmpCenter);
        AnchorPane.setTopAnchor(graphStage, graphStage.getCenterY() - graphStage.getRadius());
        AnchorPane.setLeftAnchor(graphStage, graphStage.getCenterX() - graphStage.getRadius());

        hostList = new ArrayList<>();
        switchList = new TreeMap<>();
        routerList = new TreeMap<>();
        linkList = new ArrayList<>();
//        notHostList = new ArrayList<>();

        System.out.println("W+H: " + width + " " + height);
        System.out.println("CX+CY: " + centerX + " " + centerY);
        System.out.println("Switch radius: " + switchRadius);
        System.out.println("Host radius: " + hostRadius);
    }


    //make private ?
//    public boolean displayAlgorithm(){
//        itemsAnchor.getChildren().clear();
//        connectionsAnchor.getChildren().clear();
//
//        Host tmpHost;
//        Switch tmpSwitch;
//        Router tmpRouter;
//        double deltaAlpha = 360.0 / routerList.size();
//        double iAlpha;
//        double startAlpha;
//        double deltaSwitchAlpha;
//        double jSwitchAngle;
//
//        /* for each router */
//        for (int i=0; i<routerList.size();i++){
//            tmpRouter = routerList.get(i);
//            iAlpha = i * deltaAlpha;
//            tmpRouter.setAngle(iAlpha);
//            tmpRouter.computeCoords(iAlpha, routerRadius, centerX, centerY);
//
//            /* for each switch linked to that router, compute coordinates */
//            startAlpha = iAlpha - deltaAlpha/2.0;
//            deltaSwitchAlpha = deltaAlpha / (tmpRouter.getSwitchLinkNumber() * 2);
////            System.out.println("switch i list number:" + switchList.get(i).getHostLinkNumber());
//            for (int j = 0; j < tmpRouter.getSwitchLinkNumber(); j++){
//                tmpSwitch = tmpRouter.getSwitchFromLink(j);
//                jSwitchAngle = startAlpha + ((2*j)+1) * deltaSwitchAlpha;
//                tmpSwitch.setAngle(jSwitchAngle);
//                tmpSwitch.computeCoords(jSwitchAngle, hostRadius, centerX, centerY);
////                System.out.println("\tHOST: " + tmpHost.getCenterX() + " " + tmpHost.getCenterY());
//
//                /* add link between this pair host-switch */
//                linkList.add(new Link(tmpRouter.getCenterX(), tmpSwitch.getCenterY(), tmpSwitch.getCenterX(), tmpSwitch.getCenterY()));
//            }
//        }
//
//        return true;
//    }
}



class Link extends Line{

    private NetItem source;
    private Network destination;
    private double anchorX;
    private double anchorY;

    public Link(double v, double v1, double v2, double v3) {
        super(v, v1, v2, v3);

        this.setStrokeWidth(3.0);
        computeAnchors();
    }

    private void computeAnchors(){
        anchorX = Math.min(this.getStartX(), this.getEndX());
        anchorY = Math.min(this.getStartY(), this.getEndY());
    }

    public double getAnchorX() {
        return anchorX;
    }

    public double getAnchorY() {
        return anchorY;
    }
}

