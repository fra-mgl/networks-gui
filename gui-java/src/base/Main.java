package base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Main extends Application {

    private static final double windowHeight = 850.0 ;
    private static final double windowWidth = 800.0;
    private static final int HBoxPadding = 20;

    public void addGuiElement(Pane pane, NetItem item){
        pane.getChildren().add((item));
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Button bAddHost = new Button("Add host");
        Button bAddSwitch = new Button("Add switch");
        Button bRefresh = new Button("Refresh");
//        button.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent actionEvent) {
//                System.out.println("Hello GUI!");
//            }
//        });
        BorderPane layout = new BorderPane();

        /* BUTTONS */
        HBox buttonsBox = new HBox(bRefresh, bAddHost, bAddSwitch);
        buttonsBox.setBackground(new Background(new BackgroundFill(Color.CADETBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        layout.setBottom(buttonsBox);
        buttonsBox.setAlignment(Pos.CENTER);
        BorderPane.setAlignment(buttonsBox, Pos.CENTER);
        buttonsBox.setPadding(new Insets(HBoxPadding, 0, HBoxPadding, 0));
        buttonsBox.setSpacing(50);
        buttonsBox.setPrefHeight(50.0);


        Network network = new Network(windowWidth, windowHeight - buttonsBox.getPrefHeight());
        layout.setCenter(network.netStack);
//        prova.setCoordinates(100.0, 50.0);
//        network.addHost(prova);

//        List<Router> p = new ArrayList<>();
//        p.add(new Router("1", 10.0));
//        p.add(new Router("2", 20.0));
//        p.get(0).setAngle(30.0);
//        System.out.println(p.get(0).getAngle());

//        String s = "{'dpid': '0000000000000007', 'ports': [{'dpid': '0000000000000007', 'port_no': '00000001', 'hw_addr': '96:10:5e:db:0c:d3', 'name': 's7-eth1'}, {'dpid': '0000000000000007', 'port_no': '00000002', 'hw_addr': 'c6:e5:b1:06:38:85', 'name': 's7-eth2'}, {'dpid': '0000000000000007', 'port_no': '00000003', 'hw_addr': 'ae:99:21:7c:e0:bb', 'name': 's7-eth3'}]}";
//
//        Gson gson = new GsonBuilder()
//                .excludeFieldsWithoutExposeAnnotation()
//                .create();
//        Router r0 = gson.fromJson(s, Router.class);
//        Router r0 = RestAPI.getRouter();
        List<Router> list = Arrays.asList(RestAPI.getRouter());
        Router r0 = list.get(0);
        Router r1 = new Router();
        Router r2 = new Router();
        Router r3 = new Router();
        System.out.println(r0);

        network.addRouter(r0);
        network.addRouter(r1);
        network.addRouter(r2);
        network.addRouter(r3);

        network.addHost(new Host("H1"), r0);
        network.addHost(new Host("H1"), r1);
        network.addHost(new Host("H1"), r2);
        network.addHost(new Host("H1"), r2);
        network.addHost(new Host("H1"), r3);
        network.addHost(new Host("H1"), r3);
        network.addHost(new Host("H1"), r3);

        r0.addRouterLink(r1);
        r1.addRouterLink(r2);
        r1.addRouterLink(r3);
        r2.addRouterLink(r3);

        network.displayAlgorithm();

        /* STAGE */
        primaryStage.setResizable(false);
        primaryStage.setTitle("Networks GUI");
        primaryStage.setScene(new Scene(layout, windowWidth, windowHeight));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
//        String s = "{'dpid': '0000000000000007', 'ports': [{'dpid': '0000000000000007', 'port_no': '00000001', 'hw_addr': '96:10:5e:db:0c:d3', 'name': 's7-eth1'}, {'dpid': '0000000000000007', 'port_no': '00000002', 'hw_addr': 'c6:e5:b1:06:38:85', 'name': 's7-eth2'}, {'dpid': '0000000000000007', 'port_no': '00000003', 'hw_addr': 'ae:99:21:7c:e0:bb', 'name': 's7-eth3'}]}";
//        Gson gson = new GsonBuilder()
//                .excludeFieldsWithoutExposeAnnotation()
//                .create();

//        Gson gson = new GsonBuilder()
//                .excludeFieldsWithModifiers(Modifier.STATIC,
//                        Modifier.TRANSIENT,
//                        Modifier.VOLATILE)
//                .create();
//        Gson gson = new Gson();
//        SwitchJson switchJson = new SwitchJson();
//        switchJson.setCiaoo(12);
//        System.out.println(switchJson);
//        SwitchJson switchJson = gson.fromJson(s, SwitchJson.class);
//        System.out.println(switchJson);
    }
}
