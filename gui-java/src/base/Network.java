package base;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.List;

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
    public List<Switch> switchList;
    private List<Link> linkList;


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
        circleRadius = width/2.0;
        graphStage = new Circle(circleRadius, Color.CADETBLUE);
        /* reduce a bit hostRadius to let hosts to be inside the circle */
        hostRadius = circleRadius -  circleRadius * 0.15;
        switchRadius = circleRadius*0.66;
        routerRadius = circleRadius*0.33;
        graphStage.setCenterX(width/2.0);
        graphStage.setCenterY(height/2.0);

        Circle tmpCenter = new Circle(10.0, Color.RED);
        tmpCenter.setCenterX(width/2.0);
        tmpCenter.setCenterY(height/2.0);

        this.centerX = graphStage.getCenterX();
        this.centerY = graphStage.getCenterY();
//        graphAnchor.getChildren().add(graphStage);
        graphAnchor.getChildren().addAll(graphStage, tmpCenter);
        AnchorPane.setTopAnchor(graphStage, graphStage.getCenterY() - graphStage.getRadius());
        AnchorPane.setLeftAnchor(graphStage, graphStage.getCenterX() - graphStage.getRadius());

        hostList = new ArrayList<>();
        switchList = new ArrayList<>();
        linkList = new ArrayList<>();

        System.out.println("W+H: " + width + " " + height);
        System.out.println("CX+CY: " + centerX + " " + centerY);
        System.out.println("Switch radius: " + switchRadius);
        System.out.println("Host radius: " + hostRadius);
    }

    public boolean addHost(Host i){
        // gli passo una stringa, creo metodo da stringa (json) a host e poi lo aggiungo
//        itemsAnchor.getChildren().add(i);
//        AnchorPane.setTopAnchor(i, i.getAnchorY());
//        AnchorPane.setLeftAnchor(i, i.getAnchorX());
//        assert(switchList.contains(r));
        hostList.add(i);
//        r.addHostLink(i);
        return true;
    }
    public boolean addSwitch(Switch i){
        switchList.add(i);
        return true;
    }

    //make private ?
    public boolean displayAlgorithm(){
        /* clean all children from panes' lists */
        itemsAnchor.getChildren().clear();
        connectionsAnchor.getChildren().clear();

        /* for each switch, calculate coordinates */
        double deltaAlpha = 360.0 / switchList.size();
        double iAlpha;
        double startAlpha;
        Host tmpHost;
        Switch tmpSwitch;
        double deltaHostAlpha;
        double jHostAngle;
        for (int i = 0; i < switchList.size(); i++){
            tmpSwitch = switchList.get(i);
            iAlpha = i * deltaAlpha;
//            System.out.println("SWITCH:" + i);
            tmpSwitch.setAngle(iAlpha);
            tmpSwitch.computeCoords(iAlpha, switchRadius, centerX, centerY);
//            System.out.println("COORDS: " + switchList.get(i).getCenterX() + " " + switchList.get(i).getCenterY());
            //System.out.println(switchList.get(i).getAnchorX() + " " + switchList.get(i).getAnchorY());

            /* for each host linked to that switch, calculate coordinates */
            startAlpha = iAlpha - deltaAlpha/2.0;
            deltaHostAlpha = deltaAlpha / (tmpSwitch.getHostLinkNumber() * 2);
//            System.out.println("switch i list number:" + switchList.get(i).getHostLinkNumber());
            for (int j = 0; j < tmpSwitch.getHostLinkNumber(); j++){
                tmpHost = tmpSwitch.getHostFromLink(j);
                jHostAngle = startAlpha + ((2*j)+1) * deltaHostAlpha;
                tmpHost.setAngle(jHostAngle);
                tmpHost.computeCoords(jHostAngle, hostRadius, centerX, centerY);
//                System.out.println("\tHOST: " + tmpHost.getCenterX() + " " + tmpHost.getCenterY());

                /* add link between this pair host-switch */
                linkList.add(new Link(tmpSwitch.getCenterX(), tmpSwitch.getCenterY(), tmpHost.getCenterX(), tmpHost.getCenterY()));
            }
        }
        for (int i = 0; i < switchList.size(); i++){
            tmpSwitch = switchList.get(i);
            /* for each switch, compute links between switchs */
            for(int j = 0; j < tmpSwitch.getSwitchLinkNumber(); j++){
//                System.out.println("Switch number: " + tmpSwitch.getSwitchLinkNumber());
//                t = tmpSwitch.getSwitchFromLink(j);
//                System.out.println("Switch link: " + t);
//                System.out.println(tmpSwitch.getCenterX() + " " +  tmpSwitch.getCenterY()+ " " + tmpSwitch.getSwitchFromLink(j).getCenterX()+ " " + tmpSwitch.getSwitchFromLink(j).getCenterY());
                linkList.add(new Link(tmpSwitch.getCenterX(), tmpSwitch.getCenterY(), tmpSwitch.getSwitchFromLink(j).getCenterX(), tmpSwitch.getSwitchFromLink(j).getCenterY()));
            }
        }

        /* compute all connections
        * between switchs
        * between switchs and hosts*/


        /* add all elements to corresponding stackpane */
        //connectionsAnchor.getChildren().addAll()
//        System.out.println("SIZE: " + hostList.size());
        itemsAnchor.getChildren().addAll(hostList);
        itemsAnchor.getChildren().addAll(switchList);

        for (Switch switch_i : switchList) {
            AnchorPane.setTopAnchor(switch_i, switch_i.getAnchorY());
            AnchorPane.setLeftAnchor(switch_i, switch_i.getAnchorX());
        }
        for (Host host : hostList) {
            AnchorPane.setTopAnchor(host, host.getAnchorY());
            AnchorPane.setLeftAnchor(host, host.getAnchorX());
        }
        for (Link link : linkList) {
////            System.out.println("LINE:" + link);
            connectionsAnchor.getChildren().add(link);
            AnchorPane.setTopAnchor(link, link.getAnchorY());
            AnchorPane.setLeftAnchor(link, link.getAnchorX());
        }

        return true;
    }
}

class Link extends Line{

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
