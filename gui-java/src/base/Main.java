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



public class Main extends Application {

    private static final double windowHeight = 800.0 ;
    private static final double windowWidth = 600.0;
    private double netGuiHeight;
    private double netGuiWidht = windowWidth;
    private static final int HBoxPadding = 20;
    private static final double graphPadding = 50.0;

    private double graph_X;
    private double graph_Y;

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
        netGuiHeight = windowHeight - buttonsBox.getPrefHeight();

        Host prova = new Host("prova");

        /* GRAPH */
        StackPane netStack = new StackPane();
        layout.setCenter(netStack);
        AnchorPane graphAnchor = new AnchorPane();
        AnchorPane connectionsAnchor = new AnchorPane();
        AnchorPane itemsAnchor = new AnchorPane();
        netStack.getChildren().addAll(graphAnchor, connectionsAnchor, itemsAnchor);


        Circle graphStage = new Circle((netGuiWidht/2.0) - graphPadding, Color.CADETBLUE);
        graphStage.setCenterX(netGuiWidht/2.0);
        graphStage.setCenterY(netGuiHeight/2.0);
        graphAnchor.getChildren().add(graphStage);
        AnchorPane.setTopAnchor(graphStage, graphStage.getCenterY() - graphStage.getRadius());
        AnchorPane.setLeftAnchor(graphStage, graphStage.getCenterX() - graphStage.getRadius());

        itemsAnchor.getChildren().add(prova);
        AnchorPane.setTopAnchor(prova, 150.0);
        AnchorPane.setLeftAnchor(prova, 150.0);


        System.out.println("graph: " + graphStage.getCenterX() + " - " + graphStage.getCenterY());


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
