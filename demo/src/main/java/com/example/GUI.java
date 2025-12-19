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

        Scene scene = new Scene(root, 1200, 800);
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
        editButton.setOnAction(event -> openEditDialog());
        deleteButton.setOnAction(event -> deleteSelectedItem());

        VBox navigation = new VBox(10, navTitle, addButton, editButton, deleteButton);
        navigation.setPadding(new Insets(12));
        navigation.setPrefWidth(180);
        navigation.setStyle("-fx-background-color: #7c7a7aff;");

        return navigation;
    }

    private VBox buildContentArea() {
        Label coursesLabel = new Label("Courses");
        Button importCoursesButton = new Button("Import");
        importCoursesButton.setOnAction(e -> importCourses());
        HBox coursesHeader = new HBox(8, coursesLabel, importCoursesButton);
        coursesTable = buildCoursesTable();
        VBox coursesBox = new VBox(6, coursesHeader, coursesTable);
        coursesBox.setPrefWidth(300);

        Label classroomsLabel = new Label("Classrooms");
        Button importClassroomsButton = new Button("Import");
        importClassroomsButton.setOnAction(e -> importClassrooms());
        HBox classroomsHeader = new HBox(8, classroomsLabel, importClassroomsButton);
        classroomsTable = buildClassroomsTable();
        VBox classroomsBox = new VBox(6, classroomsHeader, classroomsTable);
        classroomsBox.setPrefWidth(250);

        Label studentsLabel = new Label("Students");
        Button importStudentsButton = new Button("Import");
        importStudentsButton.setOnAction(e -> importStudents());
        HBox studentsHeader = new HBox(8, studentsLabel, importStudentsButton);
        studentsTable = buildStudentsTable();
        VBox studentsBox = new VBox(6, studentsHeader, studentsTable);
        studentsBox.setPrefWidth(220);

        VBox searchBox = new VBox(6);
        Label searchLabel = new Label("Search");
        TextField searchField = new TextField();
        searchField.setPromptText("Type to filter (not wired yet)");
        searchBox.getChildren().addAll(searchLabel, searchField);
        searchBox.setPrefWidth(200);

        HBox tablesRow = new HBox(12, coursesBox, classroomsBox, studentsBox, searchBox);
        tablesRow.setPadding(new Insets(12, 12, 12, 12));
        tablesRow.setStyle("-fx-background-color: white; -fx-border-color: #b3b3b3; -fx-border-width: 1;");

        VBox wrapper = new VBox(tablesRow);
        wrapper.setPadding(new Insets(0));
        return wrapper;
    }

    private TableView<Course> buildCoursesTable() {
        TableView<Course> table = new TableView<>();
        table.setPlaceholder(new Label("No courses to display yet."));
        table.setTableMenuButtonVisible(false);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setItems(courses);

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
                }),
                tableColumn("Classrooms", value -> {
                    ArrayList<Classroom> rooms = value.getExamClass();
                    if (rooms == null || rooms.isEmpty())
                        return "-";
                    return rooms.stream()
                            .map(Classroom::getName)
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
        table.setItems(classrooms);

        table.getColumns().setAll(
                tableColumn("Room", value -> String.valueOf(value.getName())),
                tableColumn("Capacity", value -> String.valueOf(value.getCapacity())),
                tableColumn("Time Blocks", value -> String.valueOf(value.getBlocks().length)),
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
        table.setItems(students);

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
        statusBar.setStyle("-fx-background-color: #8b8b8bff;");
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
            showCourses();
            showStudents();
            statusLabel.setText("Imported attendance from " + file.getName());
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
        switch (view) {
            case COURSES -> statusLabel.setText("Showing courses (" + courses.size() + ")");
            case CLASSROOMS -> statusLabel.setText("Showing classrooms (" + classrooms.size() + ")");
            case STUDENTS -> statusLabel.setText("Showing students (" + students.size() + ")");
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
