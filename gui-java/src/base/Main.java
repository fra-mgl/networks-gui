package base;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.ArrayList;
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

        Router r1 = new Router("1");
        Router r2 = new Router("1");
        Router r3 = new Router("1");
        Router r4 = new Router("1");
        Router r5 = new Router("1");
        Router r6 = new Router("1");
        Router r7 = new Router("1");
        Router r8 = new Router("1");
        Router r9 = new Router("1");
        Router r10 = new Router("1");
        Router r11 = new Router("1");
        Router r12 = new Router("1");
        Router r13 = new Router("1");
        Router r14 = new Router("1");

        network.addRouter(r1);
        network.addRouter(r2);
        network.addRouter(r3);
        network.addRouter(r4);
        network.addRouter(r5);
        network.addRouter(r6);
        network.addRouter(r7);
        network.addRouter(r8);
        network.addRouter(r9);
        network.addRouter(r10);
        network.addRouter(r11);
        network.addRouter(r12);
        network.addRouter(r13);
        network.addRouter(r14);
        network.addHost(new Host("H1"), r1);
        network.addHost(new Host("H1"), r1);
        network.addHost(new Host("H1"), r2);
        network.addHost(new Host("H1"), r2);
        network.addHost(new Host("H1"), r3);
        network.addHost(new Host("H1"), r3);
        network.addHost(new Host("H1"), r3);
        network.addHost(new Host("H1"), r5);
        network.addHost(new Host("H1"), r14);
        network.addHost(new Host("H1"), r14);
        network.addHost(new Host("H1"), r14);
        network.addHost(new Host("H1"), r8);
        network.addHost(new Host("H1"), r9);
        network.addHost(new Host("H1"), r11);
        network.addHost(new Host("H1"), r5);
        network.addHost(new Host("H1"), r3);
        network.addHost(new Host("H1"), r3);

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
    }
}
