package com.example;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.io.File;

public class GUI extends Application {

    private Label statusLabel;
    private VBox contentArea;
    private TableView<Course> coursesTable;
    private TableView<Classroom> classroomsTable;
    private TableView<Student> studentsTable;
    private Stage primaryStage;
    private TextField searchField;

    private View currentView = View.COURSES;

    private final ObservableList<Student> students = FXCollections.observableArrayList();
    private final ObservableList<Course> courses = FXCollections.observableArrayList();
    private final ObservableList<Classroom> classrooms = FXCollections.observableArrayList();
    private final FilteredList<Student> filteredStudents = new FilteredList<>(students, s -> true);
    private final FilteredList<Course> filteredCourses = new FilteredList<>(courses, c -> true);
    private final FilteredList<Classroom> filteredClassrooms = new FilteredList<>(classrooms, r -> true);

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
        loadInitialData();

        contentArea = buildContentArea();

        MenuBar menuBar = buildMenuBar();
        VBox navigation = buildNavigationPanel();
        HBox statusBar = buildStatusBar();

        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-shell");
        root.setTop(menuBar);
        root.setLeft(navigation);
        root.setCenter(contentArea);
        root.setBottom(statusBar);

        Scene scene = new Scene(root, 1200, 800);
        attachStyles(scene);
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
        Button editButton = new Button("Edit");
        Button deleteButton = new Button("Delete");
        Label searchLabel = new Label("Search");
        searchField = new TextField();
        searchField.setPromptText("Type to filter");
        searchField.setMaxWidth(Double.MAX_VALUE);
        searchField.textProperty().addListener((obs, oldText, newText) -> applySearchFilter(newText));

        editButton.setMaxWidth(Double.MAX_VALUE);
        deleteButton.setMaxWidth(Double.MAX_VALUE);

        editButton.setOnAction(event -> openEditDialog());
        deleteButton.setOnAction(event -> deleteSelectedItem());

        VBox navigation = new VBox(10, navTitle, editButton, deleteButton, searchLabel, searchField);
        navigation.setPadding(new Insets(12));
        navigation.setPrefWidth(180);
        navigation.getStyleClass().add("nav-pane");

        return navigation;
    }

    private VBox buildContentArea() {
        Label coursesLabel = new Label("Courses");
        Button courseScheduleButton = new Button("Exam schedule");
        courseScheduleButton.setOnAction(e -> {
            Main.calculate(new ArrayList<>(classrooms), new ArrayList<>(courses), new ArrayList<>(students));
            Course selected = coursesTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showCourseSchedule(selected);
            } else {
                statusLabel.setText("Select a course to view its schedule.");
            }
        });
        HBox coursesHeader = new HBox(8, coursesLabel, courseScheduleButton);
        coursesTable = buildCoursesTable();
        VBox coursesBox = new VBox(6, coursesHeader, coursesTable);
        VBox.setVgrow(coursesTable, Priority.ALWAYS);
        VBox.setVgrow(coursesBox, Priority.ALWAYS);

        Label classroomsLabel = new Label("Classrooms");
        Button classroomScheduleButton = new Button("Exam schedule");
        classroomScheduleButton.setOnAction(e -> {
            Classroom selected = classroomsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showClassroomSchedule(selected);
            } else {
                statusLabel.setText("Select a classroom to view its schedule.");
            }
        });
        HBox classroomsHeader = new HBox(8, classroomsLabel, classroomScheduleButton);
        classroomsTable = buildClassroomsTable();
        VBox classroomsBox = new VBox(6, classroomsHeader, classroomsTable);
        VBox.setVgrow(classroomsTable, Priority.ALWAYS);
        VBox.setVgrow(classroomsBox, Priority.ALWAYS);

        Label studentsLabel = new Label("Students");
        Button studentScheduleButton = new Button("Exam schedule");
        studentScheduleButton.setOnAction(e -> {
            Student selected = studentsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                statusLabel.setText("Select a student to view their schedule.");
                return;
            }

            Main.calculate(new ArrayList<>(classrooms), new ArrayList<>(courses), new ArrayList<>(students));
            showStudentSchedule(selected);

        });

        HBox studentsHeader = new HBox(8, studentsLabel, studentScheduleButton);
        studentsTable = buildStudentsTable();
        VBox studentsBox = new VBox(6, studentsHeader, studentsTable);
        VBox.setVgrow(studentsTable, Priority.ALWAYS);
        VBox.setVgrow(studentsBox, Priority.ALWAYS);

        HBox tablesRow = new HBox(12, coursesBox, classroomsBox, studentsBox);
        HBox.setHgrow(coursesBox, Priority.ALWAYS);
        HBox.setHgrow(classroomsBox, Priority.ALWAYS);
        HBox.setHgrow(studentsBox, Priority.ALWAYS);
        tablesRow.setFillHeight(true);
        tablesRow.setPadding(new Insets(12, 12, 12, 12));
        tablesRow.getStyleClass().add("content-panel");

        VBox wrapper = new VBox(tablesRow);
        VBox.setVgrow(tablesRow, Priority.ALWAYS);
        wrapper.setPadding(new Insets(0));
        wrapper.getStyleClass().add("content-area");
        return wrapper;
    }

    private TableView<Course> buildCoursesTable() {
        TableView<Course> table = new TableView<>();
        table.setPlaceholder(new Label("No courses to display yet."));
        table.setTableMenuButtonVisible(false);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setMaxWidth(Double.MAX_VALUE);
        SortedList<Course> sortedCourses = new SortedList<>(filteredCourses);
        sortedCourses.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedCourses);

        table.getColumns().setAll(
                tableColumn("Code", value -> String.valueOf(value.getCode())),
                tableColumn("Duration (min)", value -> {
                    int d = value.getExamDuration();
                    return d == 0 ? "-" : String.valueOf(d);
                }),
                tableColumn("Students", value -> {
                    Student[] arr = value.getAttendees();
                    if (arr == null || arr.length == 0)
                        return "-";
                    return Arrays.stream(arr)
                            .map(Student::getID)
                            .map(String::valueOf)
                            .collect(Collectors.joining(", "));
                }));

        table.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                currentView = View.COURSES;
                deleteSelectedItem();
            }
        });
        table.setOnMouseClicked(event -> setCurrentView(View.COURSES));
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                setCurrentView(View.COURSES);
            }
        });

        return table;
    }

    private TableView<Classroom> buildClassroomsTable() {
        TableView<Classroom> table = new TableView<>();
        table.setPlaceholder(new Label("No classrooms to display yet."));
        table.setTableMenuButtonVisible(false);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setMaxWidth(Double.MAX_VALUE);
        SortedList<Classroom> sortedClassrooms = new SortedList<>(filteredClassrooms);
        sortedClassrooms.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedClassrooms);

        table.getColumns().setAll(
                tableColumn("Room", value -> String.valueOf(value.getName())),
                tableColumn("Capacity", value -> String.valueOf(value.getCapacity())),
                tableColumn("Booked", value -> String.valueOf(Arrays.stream(value.getBlocks())
                        .filter(Objects::nonNull)
                        .count())));

        table.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                currentView = View.CLASSROOMS;
                deleteSelectedItem();
            }
        });
        table.setOnMouseClicked(event -> setCurrentView(View.CLASSROOMS));
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                setCurrentView(View.CLASSROOMS);
            }
        });

        return table;
    }

    private TableView<Student> buildStudentsTable() {
        TableView<Student> table = new TableView<>();
        table.setPlaceholder(new Label("No students to display yet."));
        table.setTableMenuButtonVisible(false);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setMaxWidth(Double.MAX_VALUE);
        SortedList<Student> sortedStudents = new SortedList<>(filteredStudents);
        sortedStudents.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedStudents);

        table.getColumns().setAll(
                tableColumn("Student ID", value -> String.valueOf(value.getID())),
                tableColumn("Courses", value -> String.valueOf(courses.stream()
                        .filter(course -> Arrays.stream(course.getAttendees())
                                .anyMatch(student -> student.getID() == value.getID()))
                        .count())));

        table.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                currentView = View.STUDENTS;
                deleteSelectedItem();
            }
        });
        table.setOnMouseClicked(event -> setCurrentView(View.STUDENTS));
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                setCurrentView(View.STUDENTS);
            }
        });

        return table;
    }

    private HBox buildStatusBar() {
        HBox statusBar = new HBox(statusLabel);
        statusBar.setPadding(new Insets(6, 12, 6, 12));
        statusBar.getStyleClass().add("status-bar");
        return statusBar;
    }

    private void showCourses() {
        setCurrentView(View.COURSES);
        if (coursesTable != null) {
            coursesTable.refresh();
        }
    }

    private void showClassrooms() {
        setCurrentView(View.CLASSROOMS);
        if (classroomsTable != null) {
            classroomsTable.refresh();
        }
    }

    private void showStudents() {
        setCurrentView(View.STUDENTS);
        if (studentsTable != null) {
            studentsTable.refresh();
        }
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
            Main.calculate(new ArrayList<>(classrooms), new ArrayList<>(courses), new ArrayList<>(students));
            showCourses();
            showStudents();
            statusLabel.setText("Imported attendance from " + file.getName());
            FileManager.exportAttendance(
                new ArrayList<>(courses),
                "data/sampleData_AllAttendanceLists.csv"
);

        } catch (Exception e) {
            statusLabel.setText("Import failed: ");
        }
    }

    private <T> TableColumn<T, String> tableColumn(String title, Function<T, String> mapper) {
        TableColumn<T, String> col = new TableColumn<>(title);
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
        Object selected = getSelectionFor(currentView);
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

    private Object getSelectionFor(View view) {
        return switch (view) {
            case COURSES -> coursesTable != null ? coursesTable.getSelectionModel().getSelectedItem() : null;
            case CLASSROOMS -> classroomsTable != null ? classroomsTable.getSelectionModel().getSelectedItem() : null;
            case STUDENTS -> studentsTable != null ? studentsTable.getSelectionModel().getSelectedItem() : null;
        };
    }

    private void refreshCurrentView() {
        refreshAllTables();
        switch (currentView) {
            case COURSES -> showCourses();
            case CLASSROOMS -> showClassrooms();
            case STUDENTS -> showStudents();
        }
    }

    private void refreshAllTables() {
        if (coursesTable != null) {
            coursesTable.refresh();
        }
        if (classroomsTable != null) {
            classroomsTable.refresh();
        }
        if (studentsTable != null) {
            studentsTable.refresh();
        }
    }

    private void setCurrentView(View view) {
        currentView = view;
        updateStatusWithCounts(searchField != null ? searchField.getText() : "");
    }

    private void applySearchFilter(String rawQuery) {
        String query = rawQuery == null ? "" : rawQuery.trim().toLowerCase();

        filteredCourses.setPredicate(course -> matchesCourse(course, query));
        filteredClassrooms.setPredicate(classroom -> matchesClassroom(classroom, query));
        filteredStudents.setPredicate(student -> matchesStudent(student, query));

        updateStatusWithCounts(rawQuery);
    }

    private void updateStatusWithCounts(String query) {
        if (statusLabel == null) {
            return;
        }

        String trimmed = query == null ? "" : query.trim();
        int visible = switch (currentView) {
            case COURSES -> filteredCourses.size();
            case CLASSROOMS -> filteredClassrooms.size();
            case STUDENTS -> filteredStudents.size();
        };
        String label = switch (currentView) {
            case COURSES -> "courses";
            case CLASSROOMS -> "classrooms";
            case STUDENTS -> "students";
        };
        if (trimmed.isEmpty()) {
            statusLabel.setText("Showing " + label + " (" + visible + ")");
        } else {
            statusLabel.setText("Filtered " + label + " (" + visible + ")");
        }
    }

    private boolean matchesCourse(Course course, String query) {
        if (course == null) {
            return false;
        }
        if (query == null || query.isEmpty()) {
            return true;
        }
        if (String.valueOf(course.getCode()).toLowerCase().contains(query)) {
            return true;
        }
        if (String.valueOf(course.getExamDuration()).toLowerCase().contains(query)) {
            return true;
        }

        Student[] attendees = course.getAttendees();
        if (attendees != null) {
            for (Student student : attendees) {
                if (student != null && String.valueOf(student.getID()).toLowerCase().contains(query)) {
                    return true;
                }
            }
        }

        if (course.getExamClass() != null) {
            for (Classroom room : course.getExamClass()) {
                if (room != null && String.valueOf(room.getName()).toLowerCase().contains(query)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean matchesClassroom(Classroom classroom, String query) {
        if (classroom == null) {
            return false;
        }
        if (query == null || query.isEmpty()) {
            return true;
        }
        return String.valueOf(classroom.getName()).toLowerCase().contains(query)
                || String.valueOf(classroom.getCapacity()).toLowerCase().contains(query);
    }

    private boolean matchesStudent(Student student, String query) {
        if (student == null) {
            return false;
        }
        if (query == null || query.isEmpty()) {
            return true;
        }
        return String.valueOf(student.getID()).toLowerCase().contains(query);
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
            FileManager.exportAttendance(
                new ArrayList<>(courses),
                "data/sampleData_AllAttendanceLists.csv"
);

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

    private void openEditDialog() {
        Object selected = getSelectionFor(currentView);
        if (selected == null) {
            statusLabel.setText("Select a row to edit.");
            return;
        }

        switch (currentView) {
            case STUDENTS -> showEditStudentDialog((Student) selected);
            case CLASSROOMS -> showEditClassroomDialog((Classroom) selected);
            case COURSES -> showEditCourseDialog((Course) selected);
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

    private void showEditStudentDialog(Student student) {
        Stage dialog = createDialog("Edit student");

        GridPane form = new GridPane();
        form.setPadding(new Insets(12));
        form.setHgap(8);
        form.setVgap(8);

        Label idLabel = new Label("Student ID:");
        TextField idField = new TextField(String.valueOf(student.getID()));
        form.addRow(0, idLabel, idField);

        Label feedback = new Label();
        Button save = new Button("Save changes");

        save.setOnAction(event -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                student.setID(id);
                dialog.close();
                refreshCurrentView();
                statusLabel.setText("Student updated.");
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

    private void showEditClassroomDialog(Classroom classroom) {
        Stage dialog = createDialog("Edit classroom");

        GridPane form = new GridPane();
        form.setPadding(new Insets(12));
        form.setHgap(8);
        form.setVgap(8);

        Label nameLabel = new Label("Room number:");
        TextField nameField = new TextField(String.valueOf(classroom.getName()));
        Label capacityLabel = new Label("Capacity:");
        TextField capacityField = new TextField(String.valueOf(classroom.getCapacity()));

        form.addRow(0, nameLabel, nameField);
        form.addRow(1, capacityLabel, capacityField);

        Label feedback = new Label();
        Button save = new Button("Save changes");

        save.setOnAction(event -> {
            try {
                int name = Integer.parseInt(nameField.getText().trim());
                int capacity = Integer.parseInt(capacityField.getText().trim());
                classroom.setName(name);
                classroom.setCapacity(capacity);
                dialog.close();
                refreshCurrentView();
                statusLabel.setText("Classroom updated.");
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

        Course empty = new Course(0, new Student[0], new ArrayList<>(), 0);
        buildCourseForm(dialog, empty, false);
    }

    private void showEditCourseDialog(Course course) {
        Stage dialog = createDialog("Edit course");
        buildCourseForm(dialog, course, true);
    }

    private void buildCourseForm(Stage dialog, Course course, boolean isEdit) {
        String dialogTitle = isEdit ? "Edit course" : "Add course";
        dialog.setTitle(dialogTitle);

        GridPane form = new GridPane();
        form.setPadding(new Insets(12));
        form.setHgap(8);
        form.setVgap(8);

        Label codeLabel = new Label("Course code:");
        TextField codeField = new TextField(isEdit ? String.valueOf(course.getCode()) : "");
        Label durationLabel = new Label("Duration (minutes):");
        TextField durationField = new TextField(isEdit ? String.valueOf(course.getExamDuration()) : "");
        Label studentIdsLabel = new Label("Students (check to include):");
        ListView<Student> studentsList = new ListView<>(students);
        studentsList.setPrefHeight(200);
        Map<Student, BooleanProperty> selectedStudents = new LinkedHashMap<>();
        studentsList.setCellFactory(CheckBoxListCell.forListView(student -> {
            BooleanProperty prop = selectedStudents.get(student);
            if (prop == null) {
                prop = new SimpleBooleanProperty(false);
                selectedStudents.put(student, prop);
            }
            return prop;
        }, new StringConverter<Student>() {
            @Override
            public String toString(Student student) {
                return student == null ? "" : String.valueOf(student.getID());
            }

            @Override
            public Student fromString(String string) {
                return null; // not used
            }
        }));
        Label classroomIdsLabel = new Label("Classroom numbers (comma-separated):");
        TextField classroomIdsField = new TextField();

        // pre-select existing students if editing
        if (isEdit && course.getAttendees() != null) {
            Set<Integer> currentIds = Arrays.stream(course.getAttendees())
                    .filter(Objects::nonNull)
                    .map(Student::getID)
                    .collect(Collectors.toSet());

            students.forEach(st -> {
                if (currentIds.contains(st.getID())) {
                    BooleanProperty prop = selectedStudents.computeIfAbsent(st, s -> new SimpleBooleanProperty(false));
                    prop.set(true);
                }
            });
        }

        // pre-fill classroom list if editing
        if (isEdit && course.getExamClass() != null && !course.getExamClass().isEmpty()) {
            String existingRooms = course.getExamClass().stream()
                    .map(room -> String.valueOf(room.getName()))
                    .collect(Collectors.joining(", "));
            classroomIdsField.setText(existingRooms);
        }

        form.addRow(0, codeLabel, codeField);
        form.addRow(1, durationLabel, durationField);
        form.addRow(2, studentIdsLabel, studentsList);
        form.addRow(3, classroomIdsLabel, classroomIdsField);

        Label feedback = new Label();
        Button save = new Button("Save and add");

        save.setOnAction(event -> {
            try {
                int code = Integer.parseInt(codeField.getText().trim());
                int duration = Integer.parseInt(durationField.getText().trim());
                Student[] attendees = selectedStudents.entrySet().stream()
                        .filter(entry -> entry.getValue().get())
                        .map(Map.Entry::getKey)
                        .toArray(Student[]::new);
                ArrayList<Classroom> examRooms = findClassroomsByIds(classroomIdsField.getText());

                if (isEdit) {
                    course.setCode(code);
                    course.setExamDuration(duration);
                    course.setAttendees(attendees);
                    course.setExamClass(examRooms);
                    statusLabel.setText("Course updated.");
                } else {
                    Course newCourse = new Course(code, attendees, examRooms, duration);
                    courses.add(newCourse);
                    statusLabel.setText("Course added.");
                }
                FileManager.exportAttendance(
                    new ArrayList<>(courses),
                    "data/sampleData_AllAttendanceLists.csv"
);


                dialog.close();
                refreshCurrentView();
            } catch (NumberFormatException ex) {
                feedback.setText("Code and duration must be numeric.");
            }
        });

        VBox layout = new VBox(10, form, save, feedback);
        layout.setPadding(new Insets(12));
        dialog.setScene(new Scene(layout));
        dialog.showAndWait();
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
        toastLabel.setStyle(
                "-fx-background-color: #323232; -fx-text-fill: white; -fx-padding: 8 12 8 12; -fx-background-radius: 6;");

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

    private void attachStyles(Scene scene) {
        if (scene == null) {
            return;
        }
        String found = null;
        var css = GUI.class.getResource("/ui.css");
        if (css == null) {
            css = GUI.class.getResource("ui.css");
        }
        if (css != null) {
            found = css.toExternalForm();
        } else {
            File fallback = new File("demo/src/main/resources/ui.css");
            if (fallback.exists()) {
                found = fallback.toURI().toString();
            }
        }
        if (found != null) {
            scene.getStylesheets().add(found);
        }
    }

    private enum View {
        COURSES,
        CLASSROOMS,
        STUDENTS
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

    public void showStudentSchedule(Student student) {
        System.out.println("POPUP ÇAĞRILDI (STUDENT)");
        if (student == null) {
            statusLabel.setText("No student selected.");
            return;
        }

        // Öğrencinin girdiği sınavları bul
        ArrayList<Course> studentCourses = new ArrayList<>();
        for (Course c : courses) {
            if (c.getAttendees() != null) {
                for (Student s : c.getAttendees()) {
                    if (s != null && s.getID() == student.getID()) {
                        studentCourses.add(c);
                        break;
                    }
                }
            }
        }

        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Exam Schedule for Student " + student.getID());

        TableView<Course> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tableView.setPlaceholder(new Label("No scheduled exams found for this student."));

        TableColumn<Course, String> colStudent = new TableColumn<>("Student");
        colStudent.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(student.getID())));

        TableColumn<Course, String> colCourse = new TableColumn<>("Course");
        colCourse.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCode())));

        TableColumn<Course, String> colClassroom = new TableColumn<>("Classroom");
        colClassroom.setCellValueFactory(c -> {
            if (c.getValue().getExamClass() == null || c.getValue().getExamClass().isEmpty())
                return new SimpleStringProperty("-");
            String rooms = c.getValue().getExamClass()
                    .stream()
                    .map(r -> String.valueOf(r.getName()))
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(rooms);
        });

        TableColumn<Course, String> colTime = new TableColumn<>("Time");
        colTime.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTimeOfExam() == null ? "-"
                        : c.getValue().getTimeOfExam() + " - " + c.getValue().getEndOfExam()));

        TableColumn<Course, String> colDuration = new TableColumn<>("Duration (min)");
        colDuration.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getExamDuration())));

        tableView.getColumns().addAll(
                colStudent, colCourse, colClassroom, colTime, colDuration);

        tableView.getItems().addAll(studentCourses);

        VBox root = new VBox(10, tableView);
        root.setPadding(new Insets(10));

        dialog.setScene(new Scene(root, 650, 400));
        dialog.showAndWait();
    }

    public void showCourseSchedule(Course course) {
        System.out.println("POPUP ÇAĞRILDI (COURSE)");
        if (course == null) {
            statusLabel.setText("No course selected.");
            return;
        }

        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Course Schedule - " + course.getCode());

        TableView<Course> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Course, String> c1 = new TableColumn<>("Course");
        c1.setCellValueFactory(s -> new SimpleStringProperty(String.valueOf(course.getCode())));

        TableColumn<Course, String> c2 = new TableColumn<>("Classroom");
        c2.setCellValueFactory(s -> new SimpleStringProperty(
                course.getExamClass() == null || course.getExamClass().isEmpty()
                        ? "-"
                        : course.getExamClass().stream()
                                .map(r -> String.valueOf(r.getName()))
                                .collect(Collectors.joining(", "))));

        TableColumn<Course, String> c3 = new TableColumn<>("Time");
        c3.setCellValueFactory(s -> new SimpleStringProperty(
                course.getTimeOfExam() == null ? "-" : course.getTimeOfExam() + " - " + course.getEndOfExam()));

        TableColumn<Course, String> c4 = new TableColumn<>("Duration");
        c4.setCellValueFactory(s -> new SimpleStringProperty(String.valueOf(course.getExamDuration())));

        table.getColumns().addAll(c1, c2, c3, c4);
        table.getItems().add(course);

        VBox root = new VBox(10, table);
        root.setPadding(new Insets(10));

        dialog.setScene(new Scene(root, 600, 350));
        dialog.showAndWait();
    }

    public void showClassroomSchedule(Classroom classroom) {
        System.out.println("POPUP ÇAĞRILDI (CLASS)");
        if (classroom == null) {
            statusLabel.setText("No classroom selected.");
            return;
        }

        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Schedule for Classroom " + classroom.getName());

        TableView<Course> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No scheduled exams found for this classroom."));

        // Classroom column
        TableColumn<Course, String> colRoom = new TableColumn<>("Classroom");
        colRoom.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(classroom.getName())));

        // Course column
        TableColumn<Course, String> colCourse = new TableColumn<>("Course");
        colCourse.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCode())));

        // Time column
        TableColumn<Course, String> colTime = new TableColumn<>("Time");
        colTime.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTimeOfExam() == null ? "-"
                        : c.getValue().getTimeOfExam() + " - " + c.getValue().getEndOfExam()));

        // Duration column
        TableColumn<Course, String> colDuration = new TableColumn<>("Duration (min)");
        colDuration.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getExamDuration())));

        // Students count column
        TableColumn<Course, String> colStudents = new TableColumn<>("Students");
        colStudents.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getAttendees() == null ? "0" : String.valueOf(c.getValue().getAttendees().length)));

        table.getColumns().addAll(colRoom, colCourse, colTime, colDuration, colStudents);

        // ---- ŞİMDİ BU CLASSROOM'DA OLAN SINAVLARI BUL ----
        ArrayList<Course> classroomCourses = new ArrayList<>();

        for (Course c : courses) {
            if (c.getExamClass() != null) {
                for (Classroom r : c.getExamClass()) {
                    if (r != null && r.getName() == classroom.getName()) {
                        classroomCourses.add(c);
                        break;
                    }
                }
            }
        }

        table.getItems().addAll(classroomCourses);

        VBox root = new VBox(10, table);
        root.setPadding(new Insets(10));

        dialog.setScene(new Scene(root, 650, 400));
        dialog.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
