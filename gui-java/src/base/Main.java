package base;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Main extends Application {

    /**/ private Pane p0;
    private static final double networkDim = 800.0;
    private static final double textWidth = 350.0;
    private static final double buttonsHeight = 40.0;
    private static final double windowWidth = networkDim + textWidth;
    private static final double windowHeight = networkDim;
    private static final int HBoxPadding = 20;

    private Network network;

    private Text field0;
    private Text field1;
    private Text field2;
    private Text field3;
    private Text field4;
    private Text tableText;
    private VBox specs;

    private GridPane rightSide;
    private GridPane specsBox;
    private GridPane exploreBox;
    private ChoiceBox src;
    private ChoiceBox dst;
    private Text explorePath;

    private Button bSpecs;
    private Button bExplore;
    static private Button bRefresh;

    private Host exploreSrc;
    private Host exploreDst;
    private Button bExpStart;
    private boolean isExplore;



    @Override
    public void start(Stage primaryStage) throws Exception {

        /* BUTTONS */
        bSpecs = new Button("Specs");
        bExplore = new Button("Explore");
        bRefresh = new Button("Refresh");

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
        tableText = new Text();
        StackPane tablePane = new StackPane(tableText);
        tablePane.setPadding(new Insets(HBoxPadding, HBoxPadding, HBoxPadding, HBoxPadding));

        ScrollPane specsScroll = new ScrollPane(specs);
//        specs.setAlignment(Pos.CENTER_LEFT);
        ScrollPane tableScroll = new ScrollPane(tablePane);
//        tablePane.setAlignment(Pos.CENTER);


        p0 = new Pane();
        p0.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
        Pane p1 = new Pane();
        p1.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
        Pane p2 = new Pane();
        p2.setBackground(new Background(new BackgroundFill(Color.YELLOW, CornerRadii.EMPTY, Insets.EMPTY)));
        Pane p3 = new Pane();
        p3.setBackground(new Background(new BackgroundFill(Color.VIOLET, CornerRadii.EMPTY, Insets.EMPTY)));
        Pane p4 = new Pane();
        p4.setBackground(new Background(new BackgroundFill(Color.BLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        Pane p5 = new Pane();
        p5.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));

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
        rightSide.getRowConstraints().addAll(rowDX1, rowDX2);

        /* SPECSBOX */
        specsBox = new GridPane();
        specsBox.getColumnConstraints().addAll(col2);
        RowConstraints rowSpecs1 = new RowConstraints();
        rowSpecs1.setMinHeight(rowDX1.getMaxHeight() / 2.0);
        rowSpecs1.setMaxHeight(rowDX1.getMaxHeight() / 2.0);
        RowConstraints rowSpecs2 = new RowConstraints();
        rowSpecs2.setMinHeight(rowDX1.getMaxHeight() / 2.0);
        rowSpecs2.setMaxHeight(rowDX1.getMaxHeight() / 2.0);
        specsBox.getRowConstraints().addAll(rowSpecs1, rowSpecs2);
        specsBox.add(specsScroll, 0, 0);
        specsBox.add(tableScroll, 0, 1);

        /* EXPLORE */
        exploreBox = new GridPane();
        ColumnConstraints colExp = new ColumnConstraints();
        colExp.setMinWidth(textWidth);
        colExp.setMaxWidth(textWidth);
        RowConstraints r1Exp = new RowConstraints();
        RowConstraints r2Exp = new RowConstraints();
        RowConstraints r3Exp = new RowConstraints();
//        RowConstraints r4Exp = new RowConstraints();
        r1Exp.setMinHeight((networkDim - buttonsHeight) * 0.4);
        r1Exp.setMaxHeight((networkDim - buttonsHeight) * 0.4);
        r2Exp.setMinHeight((networkDim - buttonsHeight) * 0.2);
        r2Exp.setMaxHeight((networkDim - buttonsHeight) * 0.2);
        r3Exp.setMinHeight((networkDim - buttonsHeight) * 0.4);
        r3Exp.setMaxHeight((networkDim - buttonsHeight) * 0.4);
//        r4Exp.setMinHeight((networkDim - buttonsHeight) * 0.1);
//        r4Exp.setMaxHeight((networkDim - buttonsHeight) * 0.1);
        exploreBox.getColumnConstraints().add(colExp);
        exploreBox.getRowConstraints().addAll(r1Exp, r2Exp, r3Exp);
        Text titleExp = new Text("EXPLORE PACKETS' PATH!");
        StackPane titleExpPane = new StackPane(titleExp);
        titleExpPane.setAlignment(Pos.CENTER);
//        titleExp.setTextAlignment(TextAlignment.CENTER);
        Text textSrc = new Text("Choose source");
        src = new ChoiceBox();
        VBox srcBox = new VBox(textSrc, src);
        srcBox.setAlignment(Pos.CENTER);
        srcBox.setSpacing(15);
        Text textDst = new Text("Choose destination");
        dst = new ChoiceBox();
        VBox dstBox = new VBox(textDst, dst);
        dstBox.setSpacing(15);
        dstBox.setAlignment(Pos.CENTER);
        VBox titleExpBox = new VBox(titleExpPane, srcBox, dstBox);
        titleExpBox.setPadding(new Insets(30.0, 0, 0, 0));
        titleExpBox.setSpacing(50);
        bExpStart = new Button("Explore!");
        Button bExpClean = new Button("Clean");
        HBox bExpButtons = new HBox(bExpStart, bExpClean);
        bExpButtons.setAlignment(Pos.CENTER);
        bExpButtons.setSpacing(30);
        explorePath = new Text("");
        VBox expPathBox = new VBox(new Text("PATH"), explorePath);
        expPathBox.setAlignment(Pos.TOP_CENTER);
        expPathBox.setPadding(new Insets(HBoxPadding, 0, 0, 0));
        expPathBox.setSpacing(30);
        bExpClean.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                resetExplore();
            }
        });
        src.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object o, Object h) {
                exploreSrc = (Host) h;
                List<Host> tmp = new ArrayList<>(network.hostList);
                tmp.remove(h);
                dst.getItems().clear();
                dst.getItems().addAll(tmp);
                dst.setDisable(false);
                src.setDisable(true);
            }
        });
        dst.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object o, Object h) {
                exploreDst = (Host) h;
                dst.setDisable(true);
                bExpStart.setDisable(false);
            }
        });
        bExpStart.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                bExpStart.setDisable(true);
                explorePath.setText("GIVEN PATH");

                /* URASE GLI HOST SALVATI E QUELLO CHE VIENE
                RITORNATO PER CREARE IL PATH E TROVARE I LINK
                DA COLORARE
                 */

            }
        });
        exploreBox.add(titleExpBox, 0, 0);
        exploreBox.add(bExpButtons, 0, 1);
        exploreBox.add(expPathBox, 0, 2);
//        exploreBox.add(bExpStart, 0,3);
//        exploreBox.add(p1, 0,0);
//        exploreBox.add(p2, 0,1);
//        exploreBox.add(p3, 0,2);
//        exploreBox.add(p4, 0,3);


        /* INIT */
        network = new Network(networkDim, networkDim);
        layout.add(network.netStack, 0, 0);
        layout.add(rightSide, 1, 0);
        rightSide.add(buttonsBox, 0, 1); // buttons

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



        /* PRINT NETITEM */
        for (int i : network.routerList.keySet()) {
            System.out.println(network.routerList.get(i));
        }
        for (int i : network.switchList.keySet()) {
            System.out.println(network.switchList.get(i));
        }
        for (int i = 0; i < network.hostList.size(); i++) {
            System.out.println(network.hostList.get(i));
        }


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
        Thread serverThread = new Thread(()->{
            Server server = new Server(7777);
            server.runServer();
        });
        Thread sentinel = new Thread(()->{
            while(true){
                Sem.sWait();
                Platform.runLater(Main::receiveUpdate);
                Sem.tPost();
            }
        });
        serverThread.start();
        sentinel.start();

        launch(args);
    }

    public void refreshNetwork() {

        /* ADD ITEMS IN NETWORK */
        try {
            network.hostList.clear();
            network.hostList.addAll(Arrays.asList(RestAPI.getHosts()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            network.switchList.clear();
            for (Switch s : Arrays.asList(RestAPI.getSwitch())) {
                network.switchList.put(s.getIdFromDpid(), s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            network.routerList.clear();
            for (Router r : Arrays.asList(RestAPI.getRouter())) {
                network.routerList.put(r.getIdFromDpid(), r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // finto router - da sistempare per rendere api compliant
//        Router r0 = new Router(99);
//        Router r1 = new Router(98);
//        List<Router> l = new ArrayList<>();
//        l.add(r0);
//        l.add(r1);

//        try {
//            network.routerList.clear();
//            for(Router r : l){
//                network.routerList.put(r.getIdR(), r); // sistemare come prende la chiave
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        /* SET ROUTERS */
        for (int i : network.routerList.keySet()) {
            network.routerList.get(i).setName();
            network.routerList.get(i).setID();
            /* set eventHandler to dispaly specs */
            int finalI = i;
            network.routerList.get(i).setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (!isExplore) {
                        specs.getChildren().clear();
                        field0.setText("ROUTER");
                        field1.setText("NAME:\t" + network.routerList.get(finalI).getName());
                        field2.setText("DPID:\t" + network.routerList.get(finalI).getDpid());
                        StringBuilder str = new StringBuilder("PORTS:\n");
                        for (Port p : network.routerList.get(finalI).getPorts()) {
                            str.append("\t" + p.toStringRouter() + "\n");
                        }

                        /* test scroll */
//                    for (Port p: network.switchList.get(finalI).getPorts()) {
//                        str.append("\t"+p.toString()+"\n");
//                    }

                        field3.setText(str.toString());
                        specs.getChildren().addAll(field0, field1, field2, field3);
                        /* retrieve iptable */
                        StringBuilder ip = new StringBuilder("IP TABLE\n\n");
                        List<TableEntrySwitch> l = new ArrayList<>();
                        try {
                            for (TableEntryRouter e : Arrays.asList(RestAPI.getIPTable(network.routerList.get(finalI).getDpid()))) {
//                                System.out.println(e);
                                ip.append(e);
                                ip.append("\n\n");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        tableText.setText(ip.toString());
                    }
                }
            });

        }
        /* SET SWITCHES */
        for (int i : network.switchList.keySet()) {
            network.switchList.get(i).setName();
            network.switchList.get(i).setID();

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

                        /* retrieve mactable */
                        StringBuilder mac = new StringBuilder("MAC TABLE\n\n");
                        List<TableEntrySwitch> l = new ArrayList<>();
                        try {
                            for (TableEntrySwitch e : Arrays.asList(RestAPI.getMacTable(network.switchList.get(finalI).getDpid()))) {
//                                System.out.println(e);
                                mac.append("\t");
                                mac.append(e);
                                mac.append("\n\n");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        tableText.setText(mac.toString());
                    }
                }
            });

        }

        /*SET HOSTS */
        int index;
        for (int i = 0; i < network.hostList.size(); i++) {
            network.hostList.get(i).setSwitch();
            index = network.hostList.get(i).getSwitch();
            /* add each host to its switch */
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
                            str = new StringBuilder("IPv4:");
                        } else {
                            str = new StringBuilder("IP4:\n");
                            for (String p : network.hostList.get(finalI).getIpv4()) {
                                str.append("\t" + p + "\n");
                            }

                        }
                        field2.setText(str.toString());
                        if (network.hostList.get(finalI).getIpv6().size() == 0) {
                            str = new StringBuilder("IPv6:");
                        } else {
                            str = new StringBuilder("IPv6:\n");
                            for (String p : network.hostList.get(finalI).getIpv6()) {
                                if (p.equals("::")) {
                                    continue;
                                } else {
                                    str.append("\t" + p + "\n");
                                }
                            }

                        }
                        field3.setText(str.toString());
                        field4.setText("PORT:\n\t" + network.hostList.get(finalI).getPort().toString() + "\n");
                        specs.getChildren().addAll(field0, field1, field2, field3, field4);
                        tableText.setText("");
                    }
                }
            });
        }

        /* SETTING COMPLETED */

        /* ADD LINKS BETWEEN NOT-HOSTS */
        try {
            network.linkJsonList.clear();
            network.linkJsonList.addAll(Arrays.asList(RestAPI.getLinks()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (LinkJson ll : network.linkJsonList) {
            int dpid1 = ll.getSrc();
            int dpid2 = ll.getDst();
            // routers' and switches' equals() is based on the id
            if (network.routerList.containsKey((Integer) dpid1)) {
                if (network.routerList.containsKey((Integer) dpid2)) {
                    // HERE: src=router dst=router
                    checkLinkRR(network.routerList.get(dpid1), network.routerList.get(dpid2));
                } else {
                    // HERE: src=router dst=switch
                    // always saved
                    network.routerList.get(dpid1).addSwitchLink(network.switchList.get((Integer) dpid2));
                }
            } else if (network.routerList.containsKey((Integer) dpid2)) {
                if (network.routerList.containsKey((Integer) dpid1)) {
                    // HERE: src=router dst=router
                    checkLinkRR(network.routerList.get(dpid1), network.routerList.get(dpid2));
                } else {
                    // HERE: src=switch dst=router
                    // ignore link
                    continue;
                }
            } else {
                // HERE src=switch dst=switch
                checkLinkSS(network.switchList.get(dpid1), network.switchList.get(dpid2));
            }
        }

//        /*TEST AGGIUNGO DEI LINK*/
//        network.routerList.get(99).addRouterLink(network.routerList.get((Integer) 98));
//        network.routerList.get(99).addSwitchLink(network.switchList.get((Integer) 1));
//        network.routerList.get(98).addSwitchLink(network.switchList.get((Integer) 2));
//        network.routerList.get(98).addSwitchLink(network.switchList.get((Integer) 3));
//        network.routerList.get(99).addSwitchLink(network.switchList.get((Integer) 4));
//        network.routerList.get(99).addSwitchLink(network.switchList.get((Integer) 5));
//        network.routerList.get(98).addSwitchLink(network.switchList.get((Integer) 6));
//        network.routerList.get(98).addSwitchLink(network.switchList.get((Integer) 7));
        /* ALL LINKS ARE PROCESSED */


        network.displayAlgorithm();

        if (isExplore) {
            switchToSpecsBox();
        } else {
            resetSpecs();
        }

    }

    private void resetSpecs() {
        specs.getChildren().clear();
        field0.setText("Click on an item to show its statistics!");
        tableText.setText("");
        specs.getChildren().add(field0);
        // reset table
    }

    private void resetExplore() {
        src.getItems().clear();
        dst.getItems().clear();
        explorePath.setText("");
        src.setDisable(false);
        dst.setDisable(true);
        bExpStart.setDisable(true);
        double w = 200.0;
        src.setMinWidth(w);
        src.setMaxWidth(w);
        dst.setMinWidth(w);
        dst.setMaxWidth(w);
        src.getItems().addAll(network.hostList);


    }

    private void switchToSpecsBox() {
        if (isExplore) {
            rightSide.getChildren().remove(exploreBox);
            rightSide.add(specsBox, 0, 0);
            resetSpecs();
            isExplore = false;
            bSpecs.setDisable(true);
            bExplore.setDisable(false);
            bExplore.requestFocus();
        }
    }

    private void switchToExploreBox() {
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

    private void checkLinkRR(Router src, Router dst) {
        if (src.getRouterFromLink(dst.getIdR()) == null && dst.getRouterFromLink(src.getIdR()) == null) {
            // link has never been saved
            network.routerList.get(src).addRouterLink(network.routerList.get(dst.getIdR()));
        }
    }

    private void checkLinkSS(Switch src, Switch dst) {
        if (src.getSwitchFromLink(dst.getIdS()) == null && dst.getSwitchFromLink(src.getIdS()) == null) {
            // link has never been saved
            network.switchList.get(src).addSwitchLink(network.switchList.get(dst.getIdS()));
        }
    }

    static public void receiveUpdate() {
        System.out.println("Update receive\n");
        Stage refreshStage = new Stage();
        Text text = new Text("Network has been modified!\nUpdate now!");
        text.setTextAlignment(TextAlignment.CENTER);
        Button bRef = new Button("Refresh!");
        VBox refresh = new VBox(text, bRef);
        refresh.setSpacing(50);
        refresh.setAlignment(Pos.CENTER);
        bRef.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                bRefresh.fireEvent(new ActionEvent());
                refreshStage.close();
            }
        });

        refreshStage.setTitle("Update");
        refreshStage.setScene(new Scene(refresh));
        refreshStage.setMinHeight(300);
        refreshStage.setMaxHeight(300);
        refreshStage.setMinWidth(300);
        refreshStage.setMaxWidth(300);
        refreshStage.initStyle(StageStyle.UNDECORATED);
        refreshStage.show();
    }
}
