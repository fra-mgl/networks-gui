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
    public AnchorPane connectionsAnchor;
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
    public List<LinkJson> linkJsonList;
    public List<Link> linkList;
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
        switchRadius = circleRadius * 0.60;
        routerRadius = circleRadius * 0.30;
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
        linkJsonList = new ArrayList<>();
//        notHostList = new ArrayList<>();

        System.out.println("W+H: " + width + " " + height);
        System.out.println("CX+CY: " + centerX + " " + centerY);
        System.out.println("Switch radius: " + switchRadius);
        System.out.println("Host radius: " + hostRadius);
    }


    //make private ?
    public void displayAlgorithm(){
        /* clean all children from panes' lists */
        itemsAnchor.getChildren().clear();
        connectionsAnchor.getChildren().clear();
        linkList.clear();

        double deltaRouterAlpha;
        double iRouterAngle;
        double startBeta;
        double deltaSwitchBeta;
        double jSwitchAngle;
        double startGamma;
        double deltaHostGamma;
        double kHostAngle;
        Host tmpHost;
        Switch tmpSwitch;
        Router tmpRouter;
        int counter = 0;

        boolean fakeRouter = false;
        if (routerList.size() == 0){
            fakeRouter = true;
            Router fakeR = new Router(0);

            for (Map.Entry<Integer, Switch> entry : switchList.entrySet()) {
                fakeR.addSwitchLink(entry.getValue());
            }
            routerList.put(0, fakeR);
        }
        deltaRouterAlpha = 360.0 / routerList.size();
        for(Integer i : routerList.keySet()){
            /* compute coords for each router */
            tmpRouter = routerList.get(i);
            iRouterAngle = counter * deltaRouterAlpha;
            counter += 1;
            tmpRouter.setAngle(iRouterAngle);
            if(routerList.size() == 1){
                tmpRouter.computeCoords(iRouterAngle, 0, centerX, centerY);
            }else {
                tmpRouter.computeCoords(iRouterAngle, routerRadius, centerX, centerY);
            }

            /* for each router, check linked switch and compute coords */
            startBeta = iRouterAngle - deltaRouterAlpha/2.0; // set in the middle
            deltaSwitchBeta = deltaRouterAlpha / (tmpRouter.getSwitchLinkNumber()*2);
            for(int j = 0; j < tmpRouter.getSwitchLinkNumber(); j++){
                tmpSwitch = tmpRouter.getSwitchFromLink(j);
                jSwitchAngle = startBeta + ((2*j)+1) * deltaSwitchBeta;
                tmpSwitch.setAngle(jSwitchAngle);
                tmpSwitch.computeCoords(jSwitchAngle, switchRadius, centerX, centerY);
                if(!fakeRouter) {
                    linkList.add(new Link(tmpRouter.getCenterX(), tmpRouter.getCenterY(), tmpSwitch.getCenterX(), tmpSwitch.getCenterY()));
                }
                /* now this switch is linked to hosts - let's compute all coords and links */
                startGamma = jSwitchAngle - deltaSwitchBeta; // set in the middle
                deltaHostGamma = deltaSwitchBeta / (tmpSwitch.getHostLinkNumber());
                for(int k = 0; k < tmpSwitch.getHostLinkNumber(); k++){
                    tmpHost = tmpSwitch.getHostFromLink(k);
                    kHostAngle = startGamma + ((2*k)+1) * deltaHostGamma;
                    tmpHost.setAngle(kHostAngle);
                    tmpHost.computeCoords(kHostAngle, hostRadius, centerX, centerY);
                    linkList.add(new Link(tmpSwitch.getCenterX(), tmpSwitch.getCenterY(), tmpHost.getCenterX(), tmpHost.getCenterY()));
                }
            }
        }

        /*now we can compute links between routers - because their coords are already set*/
        if (!fakeRouter) {
            for (Integer i : routerList.keySet()) {
                tmpRouter = routerList.get(i);
                for (int j = 0; j < tmpRouter.getRouterLinkNumber(); j++) {
                    linkList.add(new Link(tmpRouter.getCenterX(), tmpRouter.getCenterY(), tmpRouter.getRouterFromLink(j).getCenterX(), tmpRouter.getRouterFromLink(j).getCenterY()));
                }
            }
        }
        /*now we can compute links between switches - because their coords are already set*/
        for(Integer i : switchList.keySet()){
            tmpSwitch = switchList.get(i);
            for(int j=0; j<tmpSwitch.getSwitchLinkNumber();j++){
                linkList.add(new Link(tmpSwitch.getCenterX(), tmpSwitch.getCenterY(), tmpSwitch.getSwitchFromLink(j).getCenterX(), tmpSwitch.getSwitchFromLink(j).getCenterY()));
            }
        }

        /* add all elements to corresponding stackpane */
        itemsAnchor.getChildren().addAll(hostList);
        itemsAnchor.getChildren().addAll(switchList.values());

        if(!fakeRouter) {
            itemsAnchor.getChildren().addAll(routerList.values());
            for (int i : routerList.keySet()) {
                AnchorPane.setTopAnchor(routerList.get(i), routerList.get(i).getAnchorY());
                AnchorPane.setLeftAnchor(routerList.get(i), routerList.get(i).getAnchorX());
            }
        }
        for (int i : switchList.keySet()) {
            AnchorPane.setTopAnchor(switchList.get(i), switchList.get(i).getAnchorY());
            AnchorPane.setLeftAnchor(switchList.get(i), switchList.get(i).getAnchorX());
        }
        for (Host host : hostList) {
            AnchorPane.setTopAnchor(host, host.getAnchorY());
            AnchorPane.setLeftAnchor(host, host.getAnchorX());
        }
        connectionsAnchor.getChildren().clear();
        for (Link link : linkList) {
            connectionsAnchor.getChildren().add(link);
            AnchorPane.setTopAnchor(link, link.getAnchorY());
            AnchorPane.setLeftAnchor(link, link.getAnchorX());
        }
    }
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

    public Link(double v, double v1, double v2, double v3, Color c) {
        super(v, v1, v2, v3);
        this.setStroke(c);
        this.setStrokeWidth(5.0);
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

