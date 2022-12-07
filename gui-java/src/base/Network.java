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
    private static final double graphPadding = 100.0;
    private static double hostRadius;
    private static double routerRadius;
    private double centerX;
    private double centerY;


    // serve avere una collection di ogni elemento?
    // sì se facciamo dispay delle singole info
    // o anche no, gestiamo sull'immagine che accede direttamente alle properties dell'elemento

    public List<Host> hostList;
    public List<Router> routerList;
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

        hostRadius = width/2.0 - graphPadding;
        routerRadius = hostRadius - 1.75*graphPadding;
        graphStage = new Circle(hostRadius, Color.CADETBLUE);
        /* reduce a bit hostRadius to let hosts to be inside the circle */
        hostRadius -= 40.0;
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
        routerList = new ArrayList<>();
        linkList = new ArrayList<>();

//        System.out.println("W+H: " + width + " " + height);
//        System.out.println("CX+CY: " + centerX + " " + centerY);
//        System.out.println("Router radius: " + routerRadius);
//        System.out.println("Host radius: " + hostRadius);
    }

    public boolean addHost(Host i){
        // gli passo una stringa, creo metodo da stringa (json) a host e poi lo aggiungo
//        itemsAnchor.getChildren().add(i);
//        AnchorPane.setTopAnchor(i, i.getAnchorY());
//        AnchorPane.setLeftAnchor(i, i.getAnchorX());
//        assert(routerList.contains(r));
        hostList.add(i);
//        r.addHostLink(i);
        return true;
    }
    public boolean addRouter(Router i){
        routerList.add(i);
        return true;
    }

    //make private ?
    public boolean displayAlgorithm(){
        /* clean all children from panes' lists */
        itemsAnchor.getChildren().clear();
        connectionsAnchor.getChildren().clear();

        /* for each router, calculate coordinates */
        double deltaAlpha = 360.0 / routerList.size();
        double iAlpha;
        double startAlpha;
        Host tmpHost;
        Router tmpRouter;
        double deltaHostAlpha;
        double jHostAngle;
        for (int i = 0; i < routerList.size(); i++){
            tmpRouter = routerList.get(i);
            iAlpha = i * deltaAlpha;
//            System.out.println("SWITCH:" + i);
            tmpRouter.setAngle(iAlpha);
            tmpRouter.computeCoords(iAlpha, routerRadius, centerX, centerY);
//            System.out.println("COORDS: " + routerList.get(i).getCenterX() + " " + routerList.get(i).getCenterY());
            //System.out.println(routerList.get(i).getAnchorX() + " " + routerList.get(i).getAnchorY());

            /* for each host linked to that router, calculate coordinates */
            startAlpha = iAlpha - deltaAlpha/2.0;
            deltaHostAlpha = deltaAlpha / (tmpRouter.getHostLinkNumber() * 2);
//            System.out.println("router i list number:" + routerList.get(i).getHostLinkNumber());
            for (int j = 0; j < tmpRouter.getHostLinkNumber(); j++){
                tmpHost = tmpRouter.getHostFromLink(j);
                jHostAngle = startAlpha + ((2*j)+1) * deltaHostAlpha;
                tmpHost.setAngle(jHostAngle);
                tmpHost.computeCoords(jHostAngle, hostRadius, centerX, centerY);
//                System.out.println("\tHOST: " + tmpHost.getCenterX() + " " + tmpHost.getCenterY());

                /* add link between this pair host-router */
                linkList.add(new Link(tmpRouter.getCenterX(), tmpRouter.getCenterY(), tmpHost.getCenterX(), tmpHost.getCenterY()));
            }
        }
        for (int i = 0; i < routerList.size(); i++){
            tmpRouter = routerList.get(i);
            /* for each router, compute links between routers */
            for(int j = 0; j < tmpRouter.getRouterLinkNumber(); j++){
//                System.out.println("Router number: " + tmpRouter.getRouterLinkNumber());
//                t = tmpRouter.getRouterFromLink(j);
//                System.out.println("Router link: " + t);
//                System.out.println(tmpRouter.getCenterX() + " " +  tmpRouter.getCenterY()+ " " + tmpRouter.getRouterFromLink(j).getCenterX()+ " " + tmpRouter.getRouterFromLink(j).getCenterY());
                linkList.add(new Link(tmpRouter.getCenterX(), tmpRouter.getCenterY(), tmpRouter.getRouterFromLink(j).getCenterX(), tmpRouter.getRouterFromLink(j).getCenterY()));
            }
        }

        /* compute all connections
        * between routers
        * between routers and hosts*/


        /* add all elements to corresponding stackpane */
        //connectionsAnchor.getChildren().addAll()
//        System.out.println("SIZE: " + hostList.size());
        itemsAnchor.getChildren().addAll(hostList);
        itemsAnchor.getChildren().addAll(routerList);

        for (Router router : routerList) {
            AnchorPane.setTopAnchor(router, router.getAnchorY());
            AnchorPane.setLeftAnchor(router, router.getAnchorX());
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
