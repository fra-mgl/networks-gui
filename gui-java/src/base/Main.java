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
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Main extends Application {

    private static final double networkDim = 750.0;
    private static final double textWidth = 350.0;
    private static final double buttonsHeight = 50.0;
    private static final double windowWidth = networkDim +  textWidth;
    private static final double windowHeight = networkDim + buttonsHeight;
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
//        BorderPane layout = new BorderPane();

        /* BUTTONS */
        HBox buttonsBox = new HBox(bRefresh, bAddHost, bAddSwitch);
        buttonsBox.setBackground(new Background(new BackgroundFill(Color.CADETBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setSpacing(50);
        buttonsBox.setMinHeight(buttonsHeight);
        buttonsBox.setMaxHeight(buttonsHeight);

        /* SPECS TEXT BOX */
        Text codice = new Text("cia");
        Text materiale = new Text("ciao");
        Text costo = new Text("ciao");
        Text dimensione = new Text();
        Text sceltaColore = new Text();
        VBox texts = new VBox();
        texts.getChildren().addAll(codice, materiale, costo);
//        texts.setBackground(new Background(new BackgroundFill(Color.CADETBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        texts.setAlignment(Pos.CENTER);
        texts.setPadding(new Insets(HBoxPadding, 0, HBoxPadding, 0));
        texts.setSpacing(50);

        /* LAYOUT */
        GridPane layou = new GridPane();
        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col1.setMinWidth(networkDim);
        col1.setMaxWidth(networkDim);
        col2.setMinWidth(textWidth);
        col2.setMaxWidth(textWidth);
        layou.getColumnConstraints().addAll(col1, col2);
        RowConstraints row1 = new RowConstraints();
        RowConstraints row2 = new RowConstraints();
        row1.setMinHeight(networkDim);
        row1.setMaxHeight(networkDim);
        row2.setMinHeight(buttonsHeight);
        row2.setMaxHeight(buttonsHeight);
        layou.getRowConstraints().addAll(row1, row2);
        Network network = new Network(networkDim, networkDim);
        layou.add(network.netStack, 0,0);
        layou.add(texts, 1,0);
        layou.add(buttonsBox, 0,1);



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
//        List<Router> listRouter = Arrays.asList(RestAPI.getRouter());
//        if (listRouter != null) {
//            for (int i = 0; i < listRouter.size(); i++) {
//                network.addRouter(listRouter.get(i));
//            }
//        }else{
//            return 1;
//        }

//        System.out.println(network.routerList);

//        Router r0 = list.get(0);
//        Router r1 = new Router();
//        Router r2 = new Router();
//        Router r3 = new Router();
//        System.out.println(r0);
//
//        network.addRouter(r0);
//        network.addRouter(r1);
//        network.addRouter(r2);
//        network.addRouter(r3);
//
//        network.addHost(new Host("H1"), r0);
//        network.addHost(new Host("H1"), r1);
//        network.addHost(new Host("H1"), r2);
//        network.addHost(new Host("H1"), r2);
//        network.addHost(new Host("H1"), r3);
//        network.addHost(new Host("H1"), r3);
//        network.addHost(new Host("H1"), r3);
//
//        r0.addRouterLink(r1);
//        r1.addRouterLink(r2);
//        r1.addRouterLink(r3);
//        r2.addRouterLink(r3);

//        Router r0 = network.routerList.get(0);
//        Router r1 = network.routerList.get(1);
//        network.addHost(new Host("H1"), r0);
//        r0.addRouterLink(r1);

        /* NETWORK INITIALIZATION */
        try{
            network.routerList.addAll(Arrays.asList(RestAPI.getRouter()));
        } catch (Exception e){
            e.printStackTrace();
        }
        try{
            network.hostList.addAll(Arrays.asList(RestAPI.getHosts()));
        } catch (Exception e){
            e.printStackTrace();
        }

        for(int i=0;i< network.routerList.size(); i++){
            network.routerList.get(i).setName();
        }
        int index;
        for(int i=0;i< network.hostList.size(); i++){
            network.hostList.get(i).setRouter();
            /* add each host to its router */
            index = network.routerList.indexOf(new Router(network.hostList.get(i).getRouter()));
            network.routerList.get(index).addHostLink(network.hostList.get(i));
        }

        /* PRINT NETITEM */
        for(int i=0;i< network.routerList.size(); i++){
            System.out.println(network.routerList.get(i));
        }
        for(int i=0;i< network.hostList.size(); i++){
            System.out.println(network.hostList.get(i));
        }
//        System.out.println(network.routerList.contains(new Router(network.hostList.get(1).getRouter())));
        network.displayAlgorithm();

        /* STAGE */
        primaryStage.setResizable(false);
        primaryStage.setTitle("Networks GUI");
        primaryStage.setScene(new Scene(layou, windowWidth, windowHeight));
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
