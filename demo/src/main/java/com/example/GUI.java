package com.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GUI extends Application {

    private Label statusLabel;
    private VBox contentArea;
    private Label tableTitle;
    private TableView<Object> dataTable;
    private TabPane dataTabs;

    private View currentView = View.COURSES;

    private final ObservableList<Student> students = FXCollections.observableArrayList();
    private final ObservableList<Course> courses = FXCollections.observableArrayList();
    private final ObservableList<Classroom> classrooms = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Exam Scheduler");

        statusLabel = new Label("Ready");
        seedSampleData();
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

        showCourses(); // default view on launch
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
        editCourses.setOnAction(event -> showCourses());
        editClassrooms.setOnAction(event -> showClassrooms());
        editStudents.setOnAction(event -> showStudents());
        manageMenu.getItems().addAll(editCourses, editClassrooms, editStudents);

        Menu helpMenu = new Menu("Help");
        MenuItem about = new MenuItem("About");
        
        helpMenu.getItems().add(about);

        return new MenuBar(fileMenu, manageMenu, helpMenu);
    }

    private VBox buildNavigationPanel() {
        Label navTitle = new Label("Actions");
        Button addButton = new Button("Add");
        Button editButton = new Button("Edit");
        Button deleteButton = new Button("Delete");
        

        addButton.setMaxWidth(Double.MAX_VALUE);
        editButton.setMaxWidth(Double.MAX_VALUE);
        deleteButton.setMaxWidth(Double.MAX_VALUE);
       

        addButton.setOnAction(event -> handleAction("Add"));
        editButton.setOnAction(event -> handleAction("Edit"));
        deleteButton.setOnAction(event -> handleAction("Delete"));

        VBox navigation = new VBox(10, navTitle, addButton, editButton, deleteButton);
        navigation.setPadding(new Insets(12));
        navigation.setPrefWidth(180);
        navigation.setStyle("-fx-background-color: #7c7a7aff;");

        return navigation;
    }

    private VBox buildContentArea() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(18));

        tableTitle = new Label("Select a tab to load data.");

        dataTabs = new TabPane();
        dataTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        Tab coursesTab = new Tab("Courses");
        Tab classroomsTab = new Tab("Classrooms");
        Tab studentsTab = new Tab("Students");
        dataTabs.getTabs().addAll(coursesTab, classroomsTab, studentsTab);
        dataTabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == coursesTab) {
                showCourses();
            } else if (newTab == classroomsTab) {
                showClassrooms();
            } else if (newTab == studentsTab) {
                showStudents();
            }
        });

        dataTable = new TableView<>();
        dataTable.setPlaceholder(new Label("No data to display yet."));
        dataTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        dataTable.setPrefHeight(520);

        box.getChildren().addAll(tableTitle, dataTabs, dataTable);
        return box;
    }

    private HBox buildStatusBar() {
        HBox statusBar = new HBox(statusLabel);
        statusBar.setPadding(new Insets(6, 12, 6, 12));
        statusBar.setStyle("-fx-background-color: #8b8b8bff;");
        return statusBar;
    }

    private void showCourses() {
        currentView = View.COURSES;
        dataTable.getColumns().setAll(
                column("Code", value -> String.valueOf(((Course) value).getCode())),
                column("Duration (min)", value -> String.valueOf(((Course) value).getExamDuration())),
                column("Students", value -> Arrays.stream(((Course) value).getAttendees())
                        .map(Student::getID)
                        .map(String::valueOf)
                        .collect(Collectors.joining(", "))),
                column("Classrooms", value -> Arrays.stream(((Course) value).getExamClass())
                        .map(Classroom::getName)
                        .map(String::valueOf)
                        .collect(Collectors.joining(", ")))
        );
        dataTable.getItems().setAll(courses);
        tableTitle.setText("Courses");
        statusLabel.setText("Showing courses (" + courses.size() + ")");
    }

    private void showClassrooms() {
        currentView = View.CLASSROOMS;
        dataTable.getColumns().setAll(
                column("Room", value -> String.valueOf(((Classroom) value).getName())),
                column("Capacity", value -> String.valueOf(((Classroom) value).getCapacity())),
                column("Time Blocks", value -> String.valueOf(((Classroom) value).getHours().size())),
                column("Booked", value -> String.valueOf(((Classroom) value).getHours().stream()
                        .filter(slot -> slot != null && slot == 1)
                        .count()))
        );
        dataTable.getItems().setAll(classrooms);
        tableTitle.setText("Classrooms");
        statusLabel.setText("Showing classrooms (" + classrooms.size() + ")");
    }

    private void showStudents() {
        currentView = View.STUDENTS;
        dataTable.getColumns().setAll(
                column("Student ID", value -> String.valueOf(((Student) value).getID())),
                column("Courses", value -> String.valueOf(courses.stream()
                        .filter(course -> Arrays.stream(course.getAttendees())
                                .anyMatch(student -> student.getID() == ((Student) value).getID()))
                        .count()))
        );
        dataTable.getItems().setAll(students);
        tableTitle.setText("Students");
        statusLabel.setText("Showing students (" + students.size() + ")");
    }

    private TableColumn<Object, String> column(String title, Function<Object, String> mapper) {
        TableColumn<Object, String> col = new TableColumn<>(title);
        col.setCellValueFactory(cell -> new SimpleStringProperty(mapper.apply(cell.getValue())));
        return col;
    }

    private void seedSampleData() {                              //we will use this method to get the data into the tables in the furute when importing is working
        Student alice = new Student(1001);
        Student bob = new Student(1002);
        Student charlie = new Student(1003);
        students.setAll(alice, bob, charlie);

        Classroom roomA = new Classroom(101, 40, defaultHours());
        Classroom roomB = new Classroom(202, 25, defaultHours());
        Classroom roomC = new Classroom(303, 30, defaultHours());
        classrooms.setAll(roomA, roomB, roomC);

        Course math = new Course(501, new Student[]{alice, bob}, new Classroom[]{roomA}, 90);
        Course cs = new Course(502, new Student[]{charlie}, new Classroom[]{roomB}, 60);
        Course physics = new Course(503, new Student[]{alice, charlie}, new Classroom[]{roomA, roomC}, 120);
        courses.setAll(math, cs, physics);
    }

    private ArrayList<Integer> defaultHours() {
        ArrayList<Integer> hours = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hours.add(0);
        }
        return hours;
    }

    private void handleAction(String action) {
        String target = switch (currentView) {
            case COURSES -> "courses";
            case CLASSROOMS -> "classrooms";
            case STUDENTS -> "students";
        };
        statusLabel.setText(action + " " + target + " (not added yet)");
    }

    private enum View {
        COURSES,
        CLASSROOMS,
        STUDENTS
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
