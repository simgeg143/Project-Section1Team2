package com.example;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
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
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.io.File;

public class GUI extends Application {

    private Label statusLabel;
    private VBox contentArea;
    private TableView<Object> dataTable;
    private TabPane dataTabs;
    private Stage primaryStage;

    private View currentView = View.COURSES;

    private final ObservableList<Student> students = FXCollections.observableArrayList();
    private final ObservableList<Course> courses = FXCollections.observableArrayList();
    private final ObservableList<Classroom> classrooms = FXCollections.observableArrayList();

    private void loadInitialData() {

        System.out.println("Working dir = " + System.getProperty("user.dir"));

        students.setAll(
                FileManager.readStudents("data/sampleData_AllStudents.csv"));

        classrooms.setAll(
                FileManager.readClassrooms("data/sampleData_AllClassroomsAndTheirCapacities.csv"));

        courses.setAll(
                FileManager.readCourses(
                        "data/sampleData_AllCourses.csv",
                        new ArrayList<>(students),
                        new ArrayList<>(classrooms)));
        FileManager.readAttendance(
                "data/sampleData_AllAttendanceLists.csv",
                new ArrayList<>(students),
                new ArrayList<>(courses));

        System.out.println("Loaded students = " + students.size());
        System.out.println("Loaded classrooms = " + classrooms.size());
        System.out.println("Loaded courses = " + courses.size());
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Exam Scheduler");
        this.primaryStage = stage;

        statusLabel = new Label("Ready");

        loadInitialData(); // 🔥 BU SATIR ŞART
        contentArea = buildContentArea();

        MenuBar menuBar = buildMenuBar();
        VBox navigation = buildNavigationPanel();
        HBox statusBar = buildStatusBar();

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setLeft(navigation);
        root.setCenter(contentArea);
        root.setBottom(statusBar);

        Scene scene = new Scene(root, 900, 800);
        stage.setScene(scene);
        stage.show();

        showCourses(); // default view
    }

    private MenuBar buildMenuBar() {
        Menu fileMenu = new Menu("File");
        MenuItem importStudents = new MenuItem("Import students");
        MenuItem importClassrooms = new MenuItem("Import classrooms");
        MenuItem importCourses = new MenuItem("Import courses");
        MenuItem importAttendance = new MenuItem("Import attendance");
        MenuItem exportSchedule = new MenuItem("Export schedule");
        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(event -> Platform.exit());
        importStudents.setOnAction(event -> importStudents());
        importClassrooms.setOnAction(event -> importClassrooms());
        importCourses.setOnAction(event -> importCourses());
        importAttendance.setOnAction(event -> importAttendance());
        fileMenu.getItems().addAll(importStudents, importClassrooms, importCourses, importAttendance, exportSchedule,
                new SeparatorMenuItem(), exit);

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

        addButton.setOnAction(event -> openAddDialog());
        editButton.setOnAction(event -> handleAction("Edit"));
        deleteButton.setOnAction(event -> deleteSelectedItem());

        VBox navigation = new VBox(10, navTitle, addButton, editButton, deleteButton);
        navigation.setPadding(new Insets(12));
        navigation.setPrefWidth(180);
        navigation.setStyle("-fx-background-color: #7c7a7aff;");

        return navigation;
    }

    private VBox buildContentArea() {
        VBox box = new VBox();
        box.setSpacing(0);
        box.setPadding(new Insets(12, 12, 12, 12));

        dataTabs = new TabPane();
        dataTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        dataTabs.setStyle("-fx-background-color: white;"
                + "-fx-border-color: #b3b3b3 #b3b3b3 white #b3b3b3;"
                + "-fx-border-width: 1 1 0 1;"
                + "-fx-padding: 4 8 0 8;");
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
        dataTable.setTableMenuButtonVisible(false);
        dataTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        dataTable.setPrefHeight(520);
        dataTable.setStyle("-fx-border-color: #b3b3b3; -fx-border-width: 1; -fx-background-insets: 0;");
        dataTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                deleteSelectedItem();
            }
        });

        box.setStyle("-fx-background-color: white; -fx-border-color: #b3b3b3; -fx-border-width: 1;");
        box.getChildren().addAll(dataTabs, dataTable);
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
                column("Duration (min)", value -> {
                    int d = ((Course) value).getExamDuration();
                    return d == 0 ? "-" : String.valueOf(d);
                }),
                column("Students", value -> {
                    Student[] arr = ((Course) value).getAttendees();
                    if (arr == null || arr.length == 0)
                        return "-";
                    return Arrays.stream(arr)
                            .map(Student::getID)
                            .map(String::valueOf)
                            .collect(Collectors.joining(", "));
                }),
                column("Classrooms", value -> {
                    ArrayList<Classroom> rooms = ((Course) value).getExamClass();
                    if (rooms == null || rooms.isEmpty())
                        return "-";
                    return rooms.stream()
                            .map(Classroom::getName)
                            .map(String::valueOf)
                            .collect(Collectors.joining(", "));
                }));
        dataTable.getItems().setAll(courses);
        statusLabel.setText("Showing courses (" + courses.size() + ")");
        fitColumns(4);
    }

    private void showClassrooms() {
        currentView = View.CLASSROOMS;
        dataTable.getColumns().setAll(
                column("Room", value -> String.valueOf(((Classroom) value).getName())),
                column("Capacity", value -> String.valueOf(((Classroom) value).getCapacity())),
                column("Time Blocks", value -> String.valueOf(((Classroom) value).getBlocks().length)),
                column("Booked", value -> String.valueOf(Arrays.stream(((Classroom) value).getBlocks())
                        .filter(Objects::nonNull)
                        .count())));
        dataTable.getItems().setAll(classrooms);
        statusLabel.setText("Showing classrooms (" + classrooms.size() + ")");
        fitColumns(4);
    }

    private void showStudents() {
        currentView = View.STUDENTS;
        dataTable.getColumns().setAll(
                column("Student ID", value -> String.valueOf(((Student) value).getID())),
                column("Courses", value -> String.valueOf(courses.stream()
                        .filter(course -> Arrays.stream(course.getAttendees())
                                .anyMatch(student -> student.getID() == ((Student) value).getID()))
                        .count())));
        dataTable.getItems().setAll(students);
        statusLabel.setText("Showing students (" + students.size() + ")");
        fitColumns(2);
    }

    private File chooseCsvFile(String title) {
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().clear();
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Data Files", "*.csv", "*.txt"));

        File dataDir = new File("demo/data");
        if (dataDir.exists() && dataDir.isDirectory()) {
            chooser.setInitialDirectory(dataDir);
        }

        return chooser.showOpenDialog(stage);
    }

    private void importStudents() {
        File file = chooseCsvFile("Import students");
        if (file == null)
            return;
        try {
            students.setAll(FileManager.readStudents(file.getAbsolutePath()));
            showStudents();
            statusLabel.setText("Imported students from " + file.getName());
        } catch (Exception e) {
            statusLabel.setText("Import failed: ");
        }
    }

    private void importClassrooms() {
        File file = chooseCsvFile("Import classrooms");
        if (file == null)
            return;
        try {
            classrooms.setAll(FileManager.readClassrooms(file.getAbsolutePath()));
            showClassrooms();
            statusLabel.setText("Imported classrooms from " + file.getName());
        } catch (Exception e) {
            statusLabel.setText("Import failed: ");
        }
    }

    private void importCourses() {
        File file = chooseCsvFile("Import courses");
        if (file == null)
            return;
        try {
            courses.setAll(FileManager.readCourses(file.getAbsolutePath(), new ArrayList<>(students),
                    new ArrayList<>(classrooms)));
            showCourses();
            statusLabel.setText("Imported courses from " + file.getName());
        } catch (Exception e) {
            statusLabel.setText("Import failed: ");
        }
    }

    private void importAttendance() {
        File file = chooseCsvFile("Import attendance");
        if (file == null)
            return;
        try {
            FileManager.readAttendance(file.getAbsolutePath(), new ArrayList<>(students), new ArrayList<>(courses));
            showCourses();
            showStudents();
            statusLabel.setText("Imported attendance from " + file.getName());
        } catch (Exception e) {
            statusLabel.setText("Import failed: ");
        }
    }

    private TableColumn<Object, String> column(String title, Function<Object, String> mapper) {
        TableColumn<Object, String> col = new TableColumn<>(title);
        col.setCellValueFactory(cell -> new SimpleStringProperty(mapper.apply(cell.getValue())));
        return col;
    }

    private void seedSampleData() { // we will use this method to get the data into the tables in the furute when
                                    // importing is working
        // Student alice = new Student(1001);
        // Student bob = new Student(1002);
        // Student charlie = new Student(1003);
        // students.setAll(alice, bob, charlie);

        // Classroom roomA = new Classroom(101, 40);
        // Classroom roomB = new Classroom(202, 25);
        // Classroom roomC = new Classroom(303, 30);
        // classrooms.setAll(roomA, roomB, roomC);

        // Course math = new Course(501, new Student[]{alice, bob}, new
        // Classroom[]{roomA}, 90);
        // Course cs = new Course(502, new Student[]{charlie}, new Classroom[]{roomB},
        // 60);
        // Course physics = new Course(503, new Student[]{alice, charlie}, new
        // Classroom[]{roomA, roomC}, 120);
        // courses.setAll(math, cs, physics);
    }

    // private ArrayList<Integer> defaultHours() {
    // ArrayList<Integer> hours = new ArrayList<>();
    // for (int i = 0; i < 24; i++) {
    // hours.add(0);
    // }
    // return hours;
    // }

    private void handleAction(String action) {
        String target = switch (currentView) {
            case COURSES -> "courses";
            case CLASSROOMS -> "classrooms";
            case STUDENTS -> "students";
        };
        statusLabel.setText(action + " " + target + " (not wired yet)");
    }

    private void deleteSelectedItem() {
        Object selected = dataTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a row to delete.");
            return;
        }

        boolean removed = switch (currentView) {
            case COURSES -> courses.remove(selected);
            case CLASSROOMS -> classrooms.remove(selected);
            case STUDENTS -> removeStudent((Student) selected);
        };

        if (removed) {
            refreshCurrentView();
            statusLabel.setText("Deleted selected entry.");
            if (currentView == View.STUDENTS) {
                showToast(primaryStage, "Student deleted successfully!");
            }
        } else {
            statusLabel.setText("Could not delete the selected entry.");
        }
    }

    private void refreshCurrentView() {
        switch (currentView) {
            case COURSES -> showCourses();
            case CLASSROOMS -> showClassrooms();
            case STUDENTS -> showStudents();
        }
    }

    private boolean removeStudent(Student student) {
        boolean removedFromList = students.remove(student);
        if (!removedFromList) {
            return false;
        }

        for (Course course : courses) {
            Student[] attendees = course.getAttendees();
            if (attendees == null || attendees.length == 0) {
                continue;
            }

            ArrayList<Student> filtered = new ArrayList<>();
            for (Student attendee : attendees) {
                if (attendee != null && attendee.getID() != student.getID()) {
                    filtered.add(attendee);
                }
            }

            if (filtered.size() != attendees.length) {
                course.setAttendees(filtered.toArray(new Student[0]));
            }
        }

        return true;
    }

    private void openAddDialog() {
        switch (currentView) {
            case STUDENTS -> showAddStudentDialog();
            case CLASSROOMS -> showAddClassroomDialog();
            case COURSES -> showAddCourseDialog();
        }
    }

    private Stage createDialog(String title) {
        Stage dialog = new Stage();
        dialog.setTitle(title);
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        return dialog;
    }

    private void showAddStudentDialog() {
        Stage dialog = createDialog("Add student");

        GridPane form = new GridPane();
        form.setPadding(new Insets(12));
        form.setHgap(8);
        form.setVgap(8);

        Label idLabel = new Label("Student ID:");
        TextField idField = new TextField();
        form.addRow(0, idLabel, idField);

        Label feedback = new Label();
        Button save = new Button("Save and add");

        save.setOnAction(event -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                students.add(new Student(id));
                dialog.close();
                refreshCurrentView();
                statusLabel.setText("Student added.");
            } catch (NumberFormatException ex) {
                feedback.setText("Enter a valid numeric ID.");
            }
        });

        VBox layout = new VBox(10, form, save, feedback);
        layout.setPadding(new Insets(12));
        dialog.setScene(new Scene(layout));
        dialog.showAndWait();
    }

    private void showAddClassroomDialog() {
        Stage dialog = createDialog("Add classroom");

        GridPane form = new GridPane();
        form.setPadding(new Insets(12));
        form.setHgap(8);
        form.setVgap(8);

        Label nameLabel = new Label("Room number:");
        TextField nameField = new TextField();
        Label capacityLabel = new Label("Capacity:");
        TextField capacityField = new TextField();

        form.addRow(0, nameLabel, nameField);
        form.addRow(1, capacityLabel, capacityField);

        Label feedback = new Label();
        Button save = new Button("Save and add");

        save.setOnAction(event -> {
            try {
                int name = Integer.parseInt(nameField.getText().trim());
                int capacity = Integer.parseInt(capacityField.getText().trim());
                classrooms.add(new Classroom(name, capacity));
                dialog.close();
                refreshCurrentView();
                statusLabel.setText("Classroom added.");
            } catch (NumberFormatException ex) {
                feedback.setText("Enter valid numeric values.");
            }
        });

        VBox layout = new VBox(10, form, save, feedback);
        layout.setPadding(new Insets(12));
        dialog.setScene(new Scene(layout));
        dialog.showAndWait();
    }

    private void showAddCourseDialog() {
        Stage dialog = createDialog("Add course");

        GridPane form = new GridPane();
        form.setPadding(new Insets(12));
        form.setHgap(8);
        form.setVgap(8);

        Label codeLabel = new Label("Course code:");
        TextField codeField = new TextField();
        Label durationLabel = new Label("Duration (minutes):");
        TextField durationField = new TextField();
        Label studentIdsLabel = new Label("Student IDs (comma-separated):");
        TextField studentIdsField = new TextField();
        Label classroomIdsLabel = new Label("Classroom numbers (comma-separated):");
        TextField classroomIdsField = new TextField();

        form.addRow(0, codeLabel, codeField);
        form.addRow(1, durationLabel, durationField);
        form.addRow(2, studentIdsLabel, studentIdsField);
        form.addRow(3, classroomIdsLabel, classroomIdsField);

        Label feedback = new Label();
        Button save = new Button("Save and add");

        save.setOnAction(event -> {
            try {
                int code = Integer.parseInt(codeField.getText().trim());
                int duration = Integer.parseInt(durationField.getText().trim());
                Student[] attendees = findStudentsByIds(studentIdsField.getText());
                ArrayList<Classroom> examRooms = findClassroomsByIds(classroomIdsField.getText());

                Course course = new Course(code, attendees, examRooms, duration);
                courses.add(course);
                dialog.close();
                refreshCurrentView();
                statusLabel.setText("Course added.");
            } catch (NumberFormatException ex) {
                feedback.setText("Code and duration must be numeric.");
            }
        });

        VBox layout = new VBox(10, form, save, feedback);
        layout.setPadding(new Insets(12));
        dialog.setScene(new Scene(layout));
        dialog.showAndWait();
    }

    private Student[] findStudentsByIds(String text) {
        if (text == null || text.isBlank()) {
            return new Student[0];
        }
        ArrayList<Student> matched = new ArrayList<>();
        Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(idStr -> {
                    try {
                        int id = Integer.parseInt(idStr);
                        students.stream()
                                .filter(s -> s.getID() == id)
                                .findFirst()
                                .ifPresent(matched::add);
                    } catch (NumberFormatException ignored) {
                    }
                });
        return matched.toArray(new Student[0]);
    }

    private ArrayList<Classroom> findClassroomsByIds(String text) {
        ArrayList<Classroom> matched = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return matched;
        }
        Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(idStr -> {
                    try {
                        int id = Integer.parseInt(idStr);
                        classrooms.stream()
                                .filter(c -> c.getName() == id)
                                .findFirst()
                                .ifPresent(matched::add);
                    } catch (NumberFormatException ignored) {
                    }
                });
        return matched;
    }

    private void showToast(Stage owner, String message) {
        if (owner == null) {
            return;
        }

        Popup popup = new Popup();
        Label toastLabel = new Label(message);
        toastLabel.setStyle("-fx-background-color: #323232; -fx-text-fill: white; -fx-padding: 8 12 8 12; -fx-background-radius: 6;");

        popup.getContent().add(toastLabel);
        popup.setAutoFix(true);
        popup.setAutoHide(true);

        popup.show(owner);

        Platform.runLater(() -> {
            double x = owner.getX() + (owner.getWidth() - toastLabel.getWidth()) / 2;
            double y = owner.getY() + owner.getHeight() - 80;
            popup.setX(x);
            popup.setY(y);
        });

        PauseTransition delay = new PauseTransition(Duration.seconds(2.0));
        delay.setOnFinished(e -> {
            FadeTransition fade = new FadeTransition(Duration.seconds(0.8), toastLabel);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setOnFinished(ev -> popup.hide());
            fade.play();
        });
        delay.play();
    }

    private enum View {
        COURSES,
        CLASSROOMS,
        STUDENTS
    }

    private void fitColumns(int count) {
        double padding = 20; // scroll bar wiggle room
        dataTable.getColumns().forEach(col -> {
            col.prefWidthProperty().unbind();
            col.prefWidthProperty().bind(dataTable.widthProperty().subtract(padding).divide(count));
        });
    }

    // placeholders kept for future logic wiring
    public static void EditCourses() {
    }

    public static void EditClassrooms() {
    }

    public static void EditStudents() {
    }

    public static void DeleteCourse() {
    }

    public static void DeleteClasroom() {
    }

    public static void DeleteStudent() {
    }

    public static void Search() {
    }

    public static void main(String[] args) {
        launch(args);
    }
}
