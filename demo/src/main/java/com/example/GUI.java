package com.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GUI extends Application {

    private Label statusLabel;
    private VBox contentArea;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Exam Scheduler");

        statusLabel = new Label("Ready");
        contentArea = buildContentArea();

        MenuBar menuBar = buildMenuBar();
        VBox navigation = buildNavigationPanel();
        HBox statusBar = buildStatusBar();

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setLeft(navigation);
        root.setCenter(contentArea);
        root.setBottom(statusBar);

        Scene scene = new Scene(root, 900, 600);
        stage.setScene(scene);
        stage.show();
    }

    private MenuBar buildMenuBar() {
        Menu fileMenu = new Menu("File");
        MenuItem importData = new MenuItem("Import data");
        MenuItem exportSchedule = new MenuItem("Export schedule");
        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(event -> Platform.exit());
        fileMenu.getItems().addAll(importData, exportSchedule, new SeparatorMenuItem(), exit);

        Menu manageMenu = new Menu("Manage");
        MenuItem editCourses = new MenuItem("Courses");
        MenuItem editClassrooms = new MenuItem("Classrooms");
        MenuItem editStudents = new MenuItem("Students");
        editCourses.setOnAction(event -> EditCourses());
        editClassrooms.setOnAction(event -> EditClassrooms());
        editStudents.setOnAction(event -> EditStudents());
        manageMenu.getItems().addAll(editCourses, editClassrooms, editStudents);

        Menu helpMenu = new Menu("Help");
        MenuItem about = new MenuItem("About");
        
        helpMenu.getItems().add(about);

        return new MenuBar(fileMenu, manageMenu, helpMenu);
    }

    private VBox buildNavigationPanel() {
        Label navTitle = new Label("Actions");
        Button coursesButton = new Button("Courses");
        Button classroomsButton = new Button("Classrooms");
        Button studentsButton = new Button("Students");
        

        coursesButton.setMaxWidth(Double.MAX_VALUE);
        classroomsButton.setMaxWidth(Double.MAX_VALUE);
        studentsButton.setMaxWidth(Double.MAX_VALUE);
       

        coursesButton.setOnAction(event -> EditCourses());
        classroomsButton.setOnAction(event -> EditClassrooms());
        studentsButton.setOnAction(event -> EditStudents());

        VBox navigation = new VBox(10, navTitle, coursesButton, classroomsButton, studentsButton);
        navigation.setPadding(new Insets(12));
        navigation.setPrefWidth(180);
        navigation.setStyle("-fx-background-color: #7c7a7aff;");

        return navigation;
    }

    private VBox buildContentArea() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(18));
        return box;
    }

    private HBox buildStatusBar() {
        HBox statusBar = new HBox(statusLabel);
        statusBar.setPadding(new Insets(6, 12, 6, 12));
        statusBar.setStyle("-fx-background-color: #8b8b8bff;");
        return statusBar;
    }

    

    // placeholders kept for future logic wiring
    public static void EditCourses() { }

    public static void EditClassrooms() { }

    public static void EditStudents() { }

    public static void DeleteCourse() { }

    public static void DeleteClasroom() { }

    public static void DeleteStudent() { }

    public static void Search() { }

    public static void main(String[] args) {
        launch(args);
    }
}
