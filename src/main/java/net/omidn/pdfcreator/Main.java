package net.omidn.pdfcreator;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.management.StandardEmitterMBean;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class Main extends Application {

    // view controls
    Menu fileMenu;
    VBox rootContainer;
    MenuItem addFilesMenuItem;
    MenuItem saveAsMenuItem;
    MenuItem quitMenuItem;
    Menu helpMenu;
    MenuItem aboutMenuItem;
    MenuBar appMenuBar;
    HBox mainAreaHBox;

    ListView<String> fileListView;
    ObservableList<String> currentFileInList = FXCollections.observableArrayList();

    Button addFilesBtn;

    VBox btnContainer;
    Button removeBtn;
    Button moveUpBtn;
    Button moveDownBtn;
    Button saveAsBtn;
    Scene scene;
    EventHandler<ActionEvent> addFilesActionEvent;
    EventHandler<ActionEvent> saveAsActionEvent;

    @Override
    public void start(Stage primaryStage) throws Exception {
        rootContainer = new VBox();
        // menu bar
        fileMenu = new Menu("File");
        addFilesMenuItem = new MenuItem("Add Files...");
        saveAsMenuItem = new MenuItem("Save As...");
        quitMenuItem = new MenuItem("Quit");
        fileMenu.getItems().addAll(addFilesMenuItem, saveAsMenuItem, quitMenuItem);

        // help menu
        helpMenu = new Menu("Help");
        aboutMenuItem = new MenuItem("About the App");
        helpMenu.getItems().add(aboutMenuItem);

        appMenuBar = new MenuBar(fileMenu, helpMenu);


        // mainArea
        mainAreaHBox = new HBox();
        VBox.setVgrow(mainAreaHBox, Priority.ALWAYS);

        //ListView
        fileListView = new ListView<>();
//        fileListView.getItems().add("Omid");
        fileListView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        fileListView.setItems(currentFileInList);
        fileListView.setCellFactory(lv -> new FilesListCell());
        addFilesBtn = new Button("Add Files...");
        StackPane listStackPane = new StackPane(fileListView, addFilesBtn);
        listStackPane.setAlignment(Pos.CENTER);


        HBox.setHgrow(listStackPane, Priority.ALWAYS);
        // adding list editing buttons
        btnContainer = new VBox();
        removeBtn = new Button("Remove");
        moveUpBtn = new Button("Move Up");
        moveDownBtn = new Button("Move Down");
        saveAsBtn = new Button("Save As...");


        Pane _space = new Pane();
        btnContainer.getChildren().addAll(removeBtn, moveUpBtn, moveDownBtn, _space, saveAsBtn);
        btnContainer.setSpacing(1);
        VBox.setVgrow(_space, Priority.ALWAYS);

        removeBtn.setMaxWidth(Double.MAX_VALUE);
        moveDownBtn.setMaxWidth(Double.MAX_VALUE);
        moveUpBtn.setMaxWidth(Double.MAX_VALUE);
        saveAsBtn.setMaxWidth(Double.MAX_VALUE);
        // add margins to buttons
        btnContainer.setPadding(new Insets(15));


        // ========== Event Handling ===========

        // add files action
        addFilesActionEvent = event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            fileChooser.setTitle("Choose images to convert to PDF: ");
            FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Image (*.jpg, *.png)", "*.jpg", "*.png");
            fileChooser.getExtensionFilters().add(extensionFilter);
            List<File> chosenFiles = fileChooser.showOpenMultipleDialog(primaryStage);
            if (chosenFiles != null) {
                currentFileInList.addAll(chosenFiles.stream().map(File::getAbsolutePath).collect(Collectors.toList()));
                addFilesBtn.setVisible(false);
            }
        };
        addFilesMenuItem.setOnAction(addFilesActionEvent);
        addFilesBtn.setOnAction(addFilesActionEvent);

        // remove button action
        removeBtn.setOnAction(event -> {
            currentFileInList.removeAll(fileListView.getSelectionModel().getSelectedItems());
            if (currentFileInList.size() == 0) {
                addFilesBtn.setVisible(true);
            }
        });

        // move up button action
        moveUpBtn.setOnAction(event -> {
            ObservableList<Integer> items;
            if ((items = fileListView.getSelectionModel().getSelectedIndices()) != null && currentFileInList.size() > 0) {

                int itemIndex = items.get(0);
                if (itemIndex != 0) {
                    String temp = currentFileInList.get(itemIndex - 1);
                    currentFileInList.set(itemIndex - 1, currentFileInList.get(itemIndex));
                    currentFileInList.set(itemIndex, temp);
                    fileListView.getSelectionModel().select(itemIndex - 1);
                }
            }
        });

        moveDownBtn.setOnAction(event -> {
            ObservableList<Integer> items;
            if ((items = fileListView.getSelectionModel().getSelectedIndices()) != null && currentFileInList.size() > 0) {

                int itemIndex = items.get(0);
                if (itemIndex != currentFileInList.size() - 1) {
                    String temp = currentFileInList.get(itemIndex + 1);
                    currentFileInList.set(itemIndex + 1, currentFileInList.get(itemIndex));
                    currentFileInList.set(itemIndex, temp);
                    fileListView.getSelectionModel().select(itemIndex + 1);
                }
            }
        });


        // quite action on menu item
        quitMenuItem.setOnAction(event -> {
            primaryStage.close();
            Platform.exit();
            System.exit(0);
        });


        // save as action:
        saveAsActionEvent = event -> {
            FileChooser destFileChooser = new FileChooser();
            destFileChooser.setInitialFileName("untitled.pdf");
            destFileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            destFileChooser.setTitle("Choose your destination PDF file: ");
            destFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF file(*.pdf)", "*.pdf"));
            File destFile = destFileChooser.showSaveDialog(primaryStage);
            if (destFile != null) {
                try {
                    JPEG2PDFCore.saveFileAsPDF(currentFileInList, destFile);
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "The file was saved successfully :)");
                    successAlert.show();
                } catch (IOException e) {
                    Alert errAlert = new Alert(Alert.AlertType.ERROR, "File couldn't be saved :(");
                    errAlert.show();
                }
            }

        };

        saveAsBtn.setOnAction(saveAsActionEvent);
        saveAsMenuItem.setOnAction(saveAsActionEvent);
        aboutMenuItem.setOnAction(event -> {
            showAboutDialog();
        });

        // setting style IDs on views
        removeBtn.setId("remove-btn");
        moveDownBtn.setId("move-down-btn");
        moveUpBtn.setId("move-up-btn");
        saveAsBtn.setId("save-btn");
        addFilesBtn.setId("add-btn");

        mainAreaHBox.getChildren().add(listStackPane);
        mainAreaHBox.getChildren().add(btnContainer);

        rootContainer.getChildren().addAll(appMenuBar, mainAreaHBox);
        scene = new Scene(rootContainer, 700, 400);

        // adding style
        scene.getStylesheets().add(getClass().getClassLoader().getResource("style.css").toString());
        primaryStage.setScene(scene);
        primaryStage.setTitle("JPEG to pdf converter");
        primaryStage.show();
    }


    private void showAboutDialog(){
        Stage stage = new Stage();

        VBox vBox=new VBox();
        vBox.setPadding(new Insets(20, 10, 20, 8));
        vBox.setSpacing(10);
        Label aboutLabel = new Label("Converting images to PDF files.\nDeveloped by Omid Nejadabbasi.\n\n" +
                "© Open source under Apache License v2");

        aboutLabel.setStyle("-fx-font-size: 18px; -fx-color: #222;");

        Hyperlink githubLink = new Hyperlink("View source on Github");
        githubLink.setOnAction(event -> {
            getHostServices().showDocument("https://github.com/OmidNejadabbasi/jpeg2pdf");
        });
        vBox.getChildren().addAll(aboutLabel, githubLink);
        Scene scene = new Scene(vBox);
        stage.setScene(scene);
        stage.setTitle("About!");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
