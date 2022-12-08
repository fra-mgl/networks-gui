package base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Main extends Application {

    private static final double networkDim = 800.0;
    private static final double textWidth = 350.0;
    private static final double buttonsHeight = 40.0;
    private static final double windowWidth = networkDim +  textWidth;
    private static final double windowHeight = networkDim;
    private static final int HBoxPadding = 20;

    private Network network;

    private Text field0;
    private Text field1;
    private Text field2;
    private Text field3;
    private Text field4;
    private VBox specs;

    private GridPane rightSide;
    private GridPane specsBox;
    private Pane exploreBox;

    private Button bSpecs;
    private Button bExplore;


    private boolean isExplore;


    @Override
    public void start(Stage primaryStage) throws Exception{

        /* BUTTONS */
        bSpecs = new Button("Specs");
        bExplore = new Button("Explore");
        Button bRefresh = new Button("Refresh");

        HBox buttonsBox = new HBox(bSpecs, bExplore, bRefresh);
        buttonsBox.setBackground(new Background(new BackgroundFill(Color.CADETBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setSpacing(50);
        buttonsBox.setMinHeight(buttonsHeight);
        buttonsBox.setMaxHeight(buttonsHeight);

        /* SPECS TEXT BOX */
        field0 = new Text();
        field1 = new Text();
        field2 = new Text();
        field3 = new Text();
        field4 = new Text();
//        Text field5 = new Text();
        specs = new VBox();
//        title.getChildren().add(field0);
//        specs.getChildren().addAll(field1, field2, field3, field4, field5);
//        specs.setBackground(new Background(new BackgroundFill(Color.CADETBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
//        field0.setFont(new Font());
        specs.setPadding(new Insets(HBoxPadding, HBoxPadding, HBoxPadding, HBoxPadding));
        specs.setSpacing(10);

        /* TABLES TEXT BOX */
        Text tableText = new Text("tabella");

        ScrollPane specsScroll = new ScrollPane(specs);
        specs.setAlignment(Pos.CENTER_LEFT);
        ScrollPane tableScroll = new ScrollPane(tableText);


        Pane p1 = new Pane();
        p1.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
        Pane p2 = new Pane();
        p2.setBackground(new Background(new BackgroundFill(Color.YELLOW, CornerRadii.EMPTY, Insets.EMPTY)));
        Pane p3 = new Pane();
        p3.setBackground(new Background(new BackgroundFill(Color.VIOLET, CornerRadii.EMPTY, Insets.EMPTY)));
        Pane p4 = new Pane();
        p4.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        exploreBox = new Pane();
        exploreBox.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));

        /* LAYOUT */
        GridPane layout = new GridPane();
        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col1.setMinWidth(networkDim);
        col1.setMaxWidth(networkDim);
        col2.setMinWidth(textWidth);
        col2.setMaxWidth(textWidth);
        layout.getColumnConstraints().addAll(col1, col2);
        RowConstraints row1 = new RowConstraints();
        row1.setMinHeight(networkDim);
        row1.setMaxHeight(networkDim);
        layout.getRowConstraints().add(row1);

        rightSide = new GridPane();
        rightSide.getColumnConstraints().addAll(col2);
        RowConstraints rowDX1 = new RowConstraints();
        rowDX1.setMinHeight(networkDim - buttonsHeight);
        rowDX1.setMaxHeight(networkDim - buttonsHeight);
        RowConstraints rowDX2 = new RowConstraints();
        rowDX2.setMinHeight(buttonsHeight);
        rowDX2.setMaxHeight(buttonsHeight);
        rightSide.getRowConstraints().addAll(rowDX1,rowDX2);

        specsBox = new GridPane();
        specsBox.getColumnConstraints().addAll(col2);
        RowConstraints rowSpecs1 = new RowConstraints();
        rowSpecs1.setMinHeight(rowDX1.getMaxHeight() / 2.0);
        rowSpecs1.setMaxHeight(rowDX1.getMaxHeight() / 2.0);
        RowConstraints rowSpecs2 = new RowConstraints();
        rowSpecs2.setMinHeight(rowDX1.getMaxHeight() / 2.0);
        rowSpecs2.setMaxHeight(rowDX1.getMaxHeight() / 2.0);
        specsBox.getRowConstraints().addAll(rowSpecs1,rowSpecs2);

        network = new Network(networkDim, networkDim);
        layout.add(network.netStack, 0,0);
        layout.add(rightSide, 1,0);
        rightSide.add(buttonsBox,0,1); // buttons
        // init specsBox //
        specsBox.add(specsScroll, 0,0);
        specsBox.add(tableScroll, 0,1);
        /* explore */
//        rightSide.add(exploreBox,0,0);
        /* specs */
//        rightSide.add(specsBox, 0, 0); // internal grid

        /* init right side */
        rightSide.add(specsBox, 0, 0);
        resetSpecs();
        isExplore = false;
        bSpecs.setDisable(true);
        bExplore.setDisable(false);
        bExplore.requestFocus();

        /* NETWORK INITIALIZATION */
        refreshNetwork();



        /* BUTTONS' EVENT HANDLERS */
        bSpecs.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                switchToSpecsBox();
            }
        });
        bExplore.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                switchToExploreBox();
            }
        });
        bRefresh.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                refreshNetwork();
            }
        });


        /* TEST */
//        layout.add(network.netStack, 0,0);
//        System.out.println(network.netStack.getHeight());
//        layout.add(specs, 1,0);



//        prova.setCoordinates(100.0, 50.0);
//        network.addHost(prova);

//        List<Switch> p = new ArrayList<>();
//        p.add(new Switch("1", 10.0));
//        p.add(new Switch("2", 20.0));
//        p.get(0).setAngle(30.0);
//        System.out.println(p.get(0).getAngle());

//        String s = "{'dpid': '0000000000000007', 'ports': [{'dpid': '0000000000000007', 'port_no': '00000001', 'hw_addr': '96:10:5e:db:0c:d3', 'name': 's7-eth1'}, {'dpid': '0000000000000007', 'port_no': '00000002', 'hw_addr': 'c6:e5:b1:06:38:85', 'name': 's7-eth2'}, {'dpid': '0000000000000007', 'port_no': '00000003', 'hw_addr': 'ae:99:21:7c:e0:bb', 'name': 's7-eth3'}]}";
//
//        Gson gson = new GsonBuilder()
//                .excludeFieldsWithoutExposeAnnotation()
//                .create();
//        Switch r0 = gson.fromJson(s, Switch.class);
//        Switch r0 = RestAPI.getSwitch();
//        List<Switch> listSwitch = Arrays.asList(RestAPI.getSwitch());
//        if (listSwitch != null) {
//            for (int i = 0; i < listSwitch.size(); i++) {
//                network.addSwitch(listSwitch.get(i));
//            }
//        }else{
//            return 1;
//        }

//        System.out.println(network.switchList);

//        Switch r0 = list.get(0);
//        Switch r1 = new Switch();
//        Switch r2 = new Switch();
//        Switch r3 = new Switch();
//        System.out.println(r0);
//
//        network.addSwitch(r0);
//        network.addSwitch(r1);
//        network.addSwitch(r2);
//        network.addSwitch(r3);
//
//        network.addHost(new Host("H1"), r0);
//        network.addHost(new Host("H1"), r1);
//        network.addHost(new Host("H1"), r2);
//        network.addHost(new Host("H1"), r2);
//        network.addHost(new Host("H1"), r3);
//        network.addHost(new Host("H1"), r3);
//        network.addHost(new Host("H1"), r3);
//
//        r0.addSwitchLink(r1);
//        r1.addSwitchLink(r2);
//        r1.addSwitchLink(r3);
//        r2.addSwitchLink(r3);

//        Switch r0 = network.switchList.get(0);
//        Switch r1 = network.switchList.get(1);
//        network.addHost(new Host("H1"), r0);
//        r0.addSwitchLink(r1);

        /* PRINT NETITEM */
//        for(int i=0;i< network.switchList.size(); i++){
//            System.out.println(network.switchList.get(i));
//        }
//        for(int i=0;i< network.hostList.size(); i++){
//            System.out.println(network.hostList.get(i));
//        }
//        System.out.println(network.switchList.contains(new Switch(network.hostList.get(1).getSwitch())));


        /* STAGE */
        primaryStage.setResizable(false);
        primaryStage.setTitle("Networks GUI");
        primaryStage.setScene(new Scene(layout));
        primaryStage.setMinHeight(windowHeight);
        primaryStage.setMaxHeight(windowHeight);
        primaryStage.setMinWidth(windowWidth);
        primaryStage.setMaxWidth(windowWidth);
        System.out.println(layout.getHeight());
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

    public void refreshNetwork(){
        try{
            network.switchList.clear();
            network.switchList.addAll(Arrays.asList(RestAPI.getSwitch()));
        } catch (Exception e){
            e.printStackTrace();
        }
        try{
            network.hostList.clear();
            network.hostList.addAll(Arrays.asList(RestAPI.getHosts()));
        } catch (Exception e){
            e.printStackTrace();
        }

        for(int i=0;i< network.switchList.size(); i++){
            network.switchList.get(i).setName();

            /* set eventHandler to dispaly specs */
            int finalI = i;
            network.switchList.get(i).setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (!isExplore) {
                        specs.getChildren().clear();
                        field0.setText("SWITCH");
                        field1.setText("NAME:\t" + network.switchList.get(finalI).getName());
                        field2.setText("DPID:\t" + network.switchList.get(finalI).getDpid());
                        StringBuilder str = new StringBuilder("PORTS:\n");
                        for (Port p : network.switchList.get(finalI).getPorts()) {
                            str.append("\t" + p.toString() + "\n");
                        }

                        /* test scroll */
//                    for (Port p: network.switchList.get(finalI).getPorts()) {
//                        str.append("\t"+p.toString()+"\n");
//                    }

                        field3.setText(str.toString());
                        specs.getChildren().addAll(field0, field1, field2, field3);
                    }
                }
            });
        }
        int index;
        for(int i=0;i< network.hostList.size(); i++){
            network.hostList.get(i).setSwitch();
            /* add each host to its switch */
            index = network.switchList.indexOf(new Switch(network.hostList.get(i).getSwitch()));
            network.switchList.get(index).addHostLink(network.hostList.get(i));

            /* set eventHandler to dispaly specs */
            int finalI = i;
            network.hostList.get(i).setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (!isExplore) {
                        specs.getChildren().clear();
                        field0.setText("HOST");
                        field1.setText("MAC:\t" + network.hostList.get(finalI).getMac());
                        StringBuilder str;
                        if (network.hostList.get(finalI).getIpv4().size() == 0) {
                            str = new StringBuilder("IPv4:\n");
                        } else {
                            str = new StringBuilder("IPv4:\n\n");
                            for (String p : network.hostList.get(finalI).getIpv4()) {
                                str.append("\t" + p + "\n");
                            }

                        }
                        field2.setText(str.toString());
                        if (network.hostList.get(finalI).getIpv6().size() == 0) {
                            str = new StringBuilder("IPv6:\n");
                        } else {
                            str = new StringBuilder("IPv6:\n\n");
                            for (String p : network.hostList.get(finalI).getIpv6()) {
                                str.append("\t" + p + "\n");
                            }

                        }
                        field3.setText(str.toString());
                        field4.setText("PORT:\t" + network.hostList.get(finalI).getPort().toString() + "\n");
                        specs.getChildren().addAll(field0, field1, field2, field3, field4);
                    }
                }
            });
        }
        network.displayAlgorithm();

        if(isExplore){
            switchToSpecsBox();
        }else{
            resetSpecs();
        }
    }
    
    private void resetSpecs(){
        specs.getChildren().clear();
        field0.setText("Click on an item to show its statistics!");
        specs.getChildren().add(field0);
        // reset table
    }
    private void resetExplore(){
        
    }

    private void switchToSpecsBox(){
        if(isExplore) {
            rightSide.getChildren().remove(exploreBox);
            rightSide.add(specsBox, 0, 0);
            resetSpecs();
            isExplore = false;
            bSpecs.setDisable(true);
            bExplore.setDisable(false);
            bExplore.requestFocus();
        }
    }
    private void switchToExploreBox(){
        if (!isExplore) {
            rightSide.getChildren().remove(specsBox);
            rightSide.add(exploreBox, 0, 0);
            resetExplore();
            isExplore = true;
            bSpecs.setDisable(false);
            bExplore.setDisable(true);
            bSpecs.requestFocus();
        }
    }
}
