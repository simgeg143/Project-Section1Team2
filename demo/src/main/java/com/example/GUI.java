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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ListView;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableRow;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GUI extends Application {

    private Label statusLabel;
    private VBox contentArea;
    private TableView<Course> coursesTable;
    private TableView<Classroom> classroomsTable;
    private TableView<Student> studentsTable;
    private Stage primaryStage;
    private TextField searchField;
    private Image appIcon;

    private View currentView = View.COURSES;

    private final ObservableList<Student> students = FXCollections.observableArrayList();
    private final ObservableList<Course> courses = FXCollections.observableArrayList();
    private final ObservableList<Classroom> classrooms = FXCollections.observableArrayList();
    private final FilteredList<Student> filteredStudents = new FilteredList<>(students, s -> true);
    private final FilteredList<Course> filteredCourses = new FilteredList<>(courses, c -> true);
    private final FilteredList<Classroom> filteredClassrooms = new FilteredList<>(classrooms, r -> true);

    private enum DataFileType {
        STUDENTS,
        CLASSROOMS,
        COURSES,
        ATTENDANCE,
        UNKNOWN
    }

    private void loadInitialData() {
        System.out.println("No auto data loading. Waiting for user import.");
    }

    @Override
    public void start(Stage stage) {

        stage.setTitle("Exam Planner");
        this.primaryStage = stage;
        appIcon = loadAppIcon();
        applyAppIcon(stage);

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
        MenuItem importAll = new MenuItem("Import all (auto-detect)");
        MenuItem importStudents = new MenuItem("Import students");
        MenuItem importClassrooms = new MenuItem("Import classrooms");
        MenuItem importCourses = new MenuItem("Import courses");
        MenuItem importAttendance = new MenuItem("Import attendance");

        // export butonları
        MenuItem exportAll = new MenuItem("Export all");
        MenuItem exportCourses = new MenuItem("Export courses");
        MenuItem exportClassrooms = new MenuItem("Export classrooms");
        MenuItem exportStudents = new MenuItem("Export students");
        MenuItem exportAttendance = new MenuItem("Export attendance");

        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(event -> Platform.exit());
        importAll.setOnAction(event -> importAllData());
        importStudents.setOnAction(event -> importStudents());
        importClassrooms.setOnAction(event -> importClassrooms());
        importCourses.setOnAction(event -> importCourses());
        importAttendance.setOnAction(event -> importAttendance());

        exportAll.setOnAction(event -> exportAllData());
        exportCourses.setOnAction(event -> exportCoursesAction());
        exportClassrooms.setOnAction(event -> exportClassroomsAction());
        exportStudents.setOnAction(event -> exportStudentsAction());
        exportAttendance.setOnAction(event -> exportAttendanceAction());

        fileMenu.getItems().addAll(
                importAll,
                new SeparatorMenuItem(),
                importStudents,
                importClassrooms,
                importCourses,
                importAttendance,

                new SeparatorMenuItem(),
                // export için
                exportAll,
                exportCourses,
                exportClassrooms,
                exportStudents,
                exportAttendance,
                new SeparatorMenuItem(),
                exit);

        Menu helpMenu = new Menu("Help");
        MenuItem about = new MenuItem("About");

        helpMenu.getItems().add(about);

        return new MenuBar(fileMenu, helpMenu);
    }

    private void exportAllData() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Directory to Save Files");
        File selectedDirectory = directoryChooser.showDialog(primaryStage);

        if (selectedDirectory != null) {
            String path = selectedDirectory.getAbsolutePath();

            try {
                // Saves all files to the selected directory with updated names
                FileManager.exportCourses(new ArrayList<>(courses), path + "/updated_courses.csv");
                FileManager.exportClassrooms(new ArrayList<>(classrooms), path + "/updated_classrooms.csv");
                FileManager.exportStudents(new ArrayList<>(students), path + "/updated_students.csv");
                FileManager.exportAttendance(new ArrayList<>(courses), path + "/updated_attendance.csv");

                statusLabel.setText("All files successfully saved to the selected directory.");
            } catch (Exception e) {
                statusLabel.setText("An error occurred during export: " + e.getMessage());
            }
        }
    }

    private void exportCoursesAction() {
        File file = chooseSaveFile("Export Courses");
        if (file != null) {
            FileManager.exportCourses(new ArrayList<>(courses), file.getAbsolutePath());
            statusLabel.setText("Courses exported to: " + file.getName());
        }
    }

    private void exportClassroomsAction() {
        File file = chooseSaveFile("Export Classrooms");
        if (file != null) {
            FileManager.exportClassrooms(new ArrayList<>(classrooms), file.getAbsolutePath());
            statusLabel.setText("Classrooms exported to: " + file.getName());
        }
    }

    private void exportStudentsAction() {
        File file = chooseSaveFile("Export Students");
        if (file != null) {
            FileManager.exportStudents(new ArrayList<>(students), file.getAbsolutePath());
            statusLabel.setText("Students exported to: " + file.getName());
        }
    }

    private void exportAttendanceAction() {
        File file = chooseSaveFile("Export Attendance");
        if (file != null) {
            // Not: attendance listenizin adının 'attendances' veya benzeri olduğunu
            // varsayıyorum
            FileManager.exportAttendance(new ArrayList<>(courses), file.getAbsolutePath());
            statusLabel.setText("Attendance exported to: " + file.getName());
        }
    }

    private VBox buildNavigationPanel() {
        Label navTitle = new Label("Actions");
        Button editButton = new Button("Edit");
        Button deleteButton = new Button("Delete");
        Button rescheduleButton = new Button("Reschedule Exams");
        rescheduleButton.setMaxWidth(Double.MAX_VALUE);
        rescheduleButton.setOnAction(e -> rescheduleAll());

        Label searchLabel = new Label("Search");
        searchField = new TextField();
        Button dayTimeButton = new Button("Day – Time Exam Schedule");
        dayTimeButton.setMaxWidth(Double.MAX_VALUE);
        dayTimeButton.setOnAction(e -> showDayTimeSchedule());

        Button dayTimeExportButton = new Button("Export Day-Time Schedule");
        dayTimeExportButton.setMaxWidth(Double.MAX_VALUE);
        dayTimeExportButton.setOnAction(e -> {
            if (courses.isEmpty()) {
                statusLabel.setText("No exams to export.");
                return;
            }

            File file = chooseSaveFile("Export Day – Time Schedule");
            if (file != null) {
                FileManager.exportDayTimeSchedule(new ArrayList<>(courses), file.getAbsolutePath());
                statusLabel.setText("Day–Time schedule exported to: " + file.getName());
            }
        });

        searchField.setPromptText("Type to filter");
        searchField.setMaxWidth(Double.MAX_VALUE);
        searchField.textProperty().addListener((obs, oldText, newText) -> applySearchFilter(newText));

        editButton.setMaxWidth(Double.MAX_VALUE);
        deleteButton.setMaxWidth(Double.MAX_VALUE);

        editButton.setOnAction(event -> openEditDialog());
        deleteButton.setOnAction(event -> deleteSelectedItem());

        VBox navigation = new VBox(10, navTitle, editButton, deleteButton, rescheduleButton, dayTimeButton,
                dayTimeExportButton, searchLabel,
                searchField);

        navigation.setPadding(new Insets(12));
        navigation.setPrefWidth(180);
        navigation.getStyleClass().add("nav-pane");

        return navigation;
    }

    public void showDayTimeSchedule() {
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        applyAppIcon(dialog);
        dialog.setTitle("Day – Time Schedule");

        TableView<DayTimeRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No schedules to display."));

        TableColumn<DayTimeRow, String> colDay = new TableColumn<>("Day");
        colDay.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().day)));

        TableColumn<DayTimeRow, String> colTime = new TableColumn<>("Time");
        colTime.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().time));

        TableColumn<DayTimeRow, String> colRoom = new TableColumn<>("Classroom");
        colRoom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().room));

        TableColumn<DayTimeRow, String> colCourse = new TableColumn<>("Course");
        colCourse.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().course)));

        TableColumn<DayTimeRow, String> colStudents = scrollableColumn("Students", r -> r.students);

        table.getColumns().addAll(colDay, colTime, colRoom, colCourse, colStudents);

        ArrayList<DayTimeRow> rows = new ArrayList<>();

        for (Course c : courses) {
            if (c == null)
                continue;

            String timeText = (c.getTimeOfExam() == null)
                    ? "-"
                    : c.getTimeOfExam() + " - " + c.getEndOfExam();

            String studentList = (c.getAttendees() == null)
                    ? "-"
                    : Arrays.stream(c.getAttendees())
                            .filter(Objects::nonNull)
                            .map(s -> String.valueOf(s.getID()))
                            .collect(Collectors.joining(", "));

            List<Classroom> examRooms = c.getExamClass() == null
                    ? new ArrayList<>()
                    : c.getExamClass().stream()
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

            if (examRooms.isEmpty())
                continue;

            String roomNames = examRooms.isEmpty()
                    ? "-"
                    : examRooms.stream()
                            .map(r -> String.valueOf(r.getName()))
                            .collect(Collectors.joining(", "));

            rows.add(new DayTimeRow(
                    c.getExamDay(),
                    timeText,
                    roomNames,
                    c.getCode(),
                    studentList));
        }

        rows.sort((a, b) -> {
            // önce güne göre sırala
            int dayCompare = Integer.compare(a.day, b.day);
            if (dayCompare != 0)
                return dayCompare;

            // sonra saatine göre sırala
            try {
                String startA = a.time.split("-")[0].trim();
                String startB = b.time.split("-")[0].trim();

                java.time.LocalTime t1 = java.time.LocalTime.parse(startA);
                java.time.LocalTime t2 = java.time.LocalTime.parse(startB);

                return t1.compareTo(t2);
            } catch (Exception e) {
                return 0;
            }
        });

        VBox root = buildScheduleWithSearch(
                table,
                rows,
                r -> r == null ? "" : (r.day + " " + r.time),
                "Search by day or time");

        dialog.setScene(buildStyledDialogScene(root, 900, 500));
        dialog.showAndWait();
    }

    private static class DayTimeRow {
        int day;
        String time;
        String room;
        int course;
        String students;

        DayTimeRow(int day, String time, String room, int course, String students) {
            this.day = day;
            this.time = time;
            this.room = room;
            this.course = course;
            this.students = students;
        }
    }

    public void showAllCoursesSchedule() {
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        applyAppIcon(dialog);
        dialog.setTitle("All Courses Exam Schedule");

        TableView<CourseRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No exams to display."));

        TableColumn<CourseRow, String> colCourse = new TableColumn<>("Course");
        colCourse.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().courseCode)));

        TableColumn<CourseRow, String> colRoom = new TableColumn<>("Classroom");
        colRoom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().room));

        TableColumn<CourseRow, String> colCap = new TableColumn<>("Capacity");
        colCap.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().capacity)));

        TableColumn<CourseRow, String> colDay = new TableColumn<>("Day");
        colDay.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().day)));

        TableColumn<CourseRow, String> colTime = new TableColumn<>("Time");
        colTime.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().time));

        TableColumn<CourseRow, String> colStudents = scrollableColumn("Students", r -> r.students);

        table.getColumns().addAll(colCourse, colRoom, colCap, colDay, colTime, colStudents);
        table.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        ArrayList<CourseRow> rows = new ArrayList<>();

        for (Course c : courses) {
            if (c == null)
                continue;

            String timeText = (c.getTimeOfExam() == null)
                    ? "-"
                    : c.getTimeOfExam() + " - " + c.getEndOfExam();

            String studentList = (c.getAttendees() == null)
                    ? "-"
                    : Arrays.stream(c.getAttendees())
                            .filter(Objects::nonNull)
                            .map(s -> String.valueOf(s.getID()))
                            .collect(Collectors.joining(", "));

            List<Classroom> examRooms = c.getExamClass() == null
                    ? new ArrayList<>()
                    : c.getExamClass().stream()
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

            String roomNames = examRooms.isEmpty()
                    ? "-"
                    : examRooms.stream()
                            .map(r -> String.valueOf(r.getName()))
                            .collect(Collectors.joining(", "));

            int totalCapacity = examRooms.stream()
                    .mapToInt(Classroom::getCapacity)
                    .sum();

            rows.add(new CourseRow(
                    c.getCode(),
                    roomNames,
                    totalCapacity,
                    c.getExamDay(),
                    timeText,
                    studentList));
        }

        VBox root = buildScheduleWithSearch(
                table,
                rows,
                r -> r == null ? "" : String.valueOf(r.courseCode),
                "Search by course code");

        dialog.setScene(buildStyledDialogScene(root, 900, 600));
        dialog.showAndWait();
    }

    private static class CourseRow {
        int courseCode;
        String room;
        int capacity;
        int day;
        String time;
        String students;

        CourseRow(int courseCode, String room, int capacity, int day, String time, String students) {
            this.courseCode = courseCode;
            this.room = room;
            this.capacity = capacity;
            this.day = day;
            this.time = time;
            this.students = students;
        }
    }

    public void showAllClassroomSchedules() {
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        applyAppIcon(dialog);
        dialog.setTitle("All Classroom Exam Schedules");

        TableView<ClassroomRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No schedules to display."));

        TableColumn<ClassroomRow, String> colRoom = new TableColumn<>("Classroom");
        colRoom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().room));

        TableColumn<ClassroomRow, String> colCap = new TableColumn<>("Capacity");
        colCap.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().capacity)));

        TableColumn<ClassroomRow, String> colCourse = new TableColumn<>("Course");
        colCourse.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().courseCode)));

        TableColumn<ClassroomRow, String> colDay = new TableColumn<>("Day");
        colDay.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().day)));

        TableColumn<ClassroomRow, String> colTime = new TableColumn<>("Time");
        colTime.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().time));

        TableColumn<ClassroomRow, String> colStudents = scrollableColumn("Students", r -> r.students);

        table.getColumns().addAll(colRoom, colCap, colCourse, colDay, colTime, colStudents);

        ArrayList<ClassroomRow> rows = new ArrayList<>();

        for (Classroom classroom : classrooms) {
            if (classroom == null)
                continue;

            for (Course c : courses) {
                if (c == null || c.getExamClass() == null)
                    continue;

                boolean usesRoom = c.getExamClass().stream()
                        .anyMatch(r -> r != null && r.getName() == classroom.getName());

                if (!usesRoom)
                    continue;

                String timeText = (c.getTimeOfExam() == null)
                        ? "-"
                        : c.getTimeOfExam() + " - " + c.getEndOfExam();

                String studentList = (c.getAttendees() == null)
                        ? "-"
                        : Arrays.stream(c.getAttendees())
                                .filter(Objects::nonNull)
                                .map(s -> String.valueOf(s.getID()))
                                .collect(Collectors.joining(", "));

                rows.add(new ClassroomRow(
                        String.valueOf(classroom.getName()),
                        classroom.getCapacity(),
                        c.getCode(),
                        c.getExamDay(),
                        timeText,
                        studentList));
            }
        }

        VBox root = buildScheduleWithSearch(
                table,
                rows,
                r -> r == null ? "" : r.room,
                "Search by classroom");

        dialog.setScene(buildStyledDialogScene(root, 900, 500));
        dialog.showAndWait();
    }

    private static class ClassroomRow {
        String room;
        int capacity;
        int courseCode;
        int day;
        String time;
        String students;

        ClassroomRow(String room, int capacity, int courseCode, int day, String time, String students) {
            this.room = room;
            this.capacity = capacity;
            this.courseCode = courseCode;
            this.day = day;
            this.time = time;
            this.students = students;
        }
    }

    public void showAllStudentsSchedule() {
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        applyAppIcon(dialog);
        dialog.setTitle("All Students Exam Schedule");

        TableView<CourseStudentRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No exams to display."));

        TableColumn<CourseStudentRow, String> colStudent = new TableColumn<>("Student");
        colStudent.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().studentId)));

        TableColumn<CourseStudentRow, String> colCourse = new TableColumn<>("Course");
        colCourse.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().courseCode)));

        TableColumn<CourseStudentRow, String> colRoom = new TableColumn<>("Classroom");
        colRoom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().room));

        TableColumn<CourseStudentRow, String> colDay = new TableColumn<>("Day");
        colDay.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().day)));

        TableColumn<CourseStudentRow, String> colTime = new TableColumn<>("Time");
        colTime.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().time));

        table.getColumns().addAll(colStudent, colCourse, colRoom, colDay, colTime);

        // ---- TABLOYU DOLDUR ----
        ArrayList<CourseStudentRow> rows = new ArrayList<>();

        for (Course c : courses) {
            if (c.getAttendees() == null)
                continue;

            String roomNames = "-";
            if (c.getExamClass() != null && !c.getExamClass().isEmpty()) {
                roomNames = c.getExamClass()
                        .stream()
                        .map(r -> String.valueOf(r.getName()))
                        .collect(Collectors.joining(", "));
            }

            for (Student s : c.getAttendees()) {
                if (s == null)
                    continue;

                rows.add(new CourseStudentRow(
                        s.getID(),
                        c.getCode(),
                        roomNames,
                        c.getExamDay(),
                        (c.getTimeOfExam() == null ? "-" : c.getTimeOfExam() + " - " + c.getEndOfExam())));
            }
        }
        rows.sort((a, b) -> Integer.compare(a.studentId, b.studentId));

        VBox root = buildScheduleWithSearch(
                table,
                rows,
                r -> r == null ? "" : String.valueOf(r.studentId),
                "Search by student ID");

        dialog.setScene(buildStyledDialogScene(root, 800, 500));
        dialog.showAndWait();
    }

    private static class CourseStudentRow {
        int studentId;
        int courseCode;
        String room;
        int day;
        String time;

        CourseStudentRow(int studentId, int courseCode, String room, int day, String time) {
            this.studentId = studentId;
            this.courseCode = courseCode;
            this.room = room;
            this.day = day;
            this.time = time;
        }
    }

    private VBox buildContentArea() {
        Label coursesLabel = new Label("Courses");
        Button courseScheduleButton = new Button("Exam schedule");
        Button courseExportButton = new Button("Exam Schedule Export");

        courseExportButton.setOnAction(e -> {
            Course selected = coursesTable.getSelectionModel().getSelectedItem();
            File file = chooseSaveFile("Export Course Exam Schedule");

            if (file == null)
                return;

            if (selected != null) {
                FileManager.exportCourseExamSchedule(selected, file.getAbsolutePath());
            } else {
                FileManager.exportAllCourseExamSchedules(
                        new ArrayList<>(courses),
                        file.getAbsolutePath());
            }

            statusLabel.setText("Course schedule exported.");

        });
        courseScheduleButton.setOnAction(e -> {
            Course selected = coursesTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showCourseSchedule(selected);
            } else {
                showAllCoursesSchedule();
            }
        });

        HBox coursesHeader = new HBox(8, coursesLabel, courseScheduleButton, courseExportButton);
        coursesTable = buildCoursesTable();
        VBox coursesBox = new VBox(6, coursesHeader, coursesTable);
        VBox.setVgrow(coursesTable, Priority.ALWAYS);
        VBox.setVgrow(coursesBox, Priority.ALWAYS);

        Label classroomsLabel = new Label("Classrooms");
        Button classroomScheduleButton = new Button("Exam schedule");
        // classroom için export buttonları eklenme yeri
        Button classroomExportButton = new Button("Exam schedule Export");

        classroomExportButton.setOnAction(e -> {
            Classroom selected = classroomsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                ArrayList<Course> relatedCourses = courses.stream()
                        .filter(c -> c.getExamClass() != null
                                && c.getExamClass().stream().anyMatch(r -> r.getName() == selected.getName()))
                        .collect(Collectors.toCollection(ArrayList::new));
                File file = chooseSaveFile("Export_Schedule_Room_" + selected.getName());
                if (file != null) {
                    FileManager.exportClassroomExamSchedule(selected, relatedCourses, file.getAbsolutePath());
                }
            } else {
                File file = chooseSaveFile("Export All Classroom Exam Schedules");
                if (file != null) {
                    FileManager.exportAllClassroomExamSchedules(
                            new ArrayList<>(classrooms),
                            new ArrayList<>(courses),
                            file.getAbsolutePath());
                }
            }
        });

        classroomScheduleButton.setOnAction(e -> {
            Classroom selected = classroomsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showClassroomSchedule(selected);
            } else {
                showAllClassroomSchedules();
            }
        });
        HBox classroomsHeader = new HBox(8, classroomsLabel, classroomScheduleButton, classroomExportButton);
        classroomsTable = buildClassroomsTable();
        VBox classroomsBox = new VBox(6, classroomsHeader, classroomsTable);
        VBox.setVgrow(classroomsTable, Priority.ALWAYS);
        VBox.setVgrow(classroomsBox, Priority.ALWAYS);

        Label studentsLabel = new Label("Students");
        Button studentScheduleButton = new Button("Exam schedule");
        // export button for student
        Button studentExportButton = new Button("Exam schedule Export");

        studentExportButton.setOnAction(e -> {
            Student selected = studentsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                ArrayList<Course> relatedCourses = courses.stream()
                        .filter(c -> c.getAttendees() != null
                                && Arrays.stream(c.getAttendees()).anyMatch(s -> s.getID() == selected.getID()))
                        .collect(Collectors.toCollection(ArrayList::new));
                File file = chooseSaveFile("Export_Schedule_Student_" + selected.getID());
                if (file != null) {
                    FileManager.exportStudentExamSchedule(selected, relatedCourses, file.getAbsolutePath());
                }
            } else {
                File file = chooseSaveFile("Export All Student Exam Schedules");
                if (file != null) {
                    FileManager.exportAllStudentExamSchedules(
                            new ArrayList<>(students),
                            new ArrayList<>(courses),
                            file.getAbsolutePath());
                }
            }

        });
        studentScheduleButton.setOnAction(e -> {
            Student selected = studentsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showStudentSchedule(selected);
            } else {
                showAllStudentsSchedule();
            }

        });

        HBox studentsHeader = new HBox(8, studentsLabel, studentScheduleButton, studentExportButton);
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
                scrollableColumn("Students", value -> {
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
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                setCurrentView(View.COURSES);
            }
        });

        enableRowDeselection(table, View.COURSES);
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
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                setCurrentView(View.CLASSROOMS);
            }
        });

        enableRowDeselection(table, View.CLASSROOMS);
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
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                setCurrentView(View.STUDENTS);
            }
        });

        enableRowDeselection(table, View.STUDENTS);
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

    private List<File> chooseCsvFiles(String title) {
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

        return chooser.showOpenMultipleDialog(stage);
    }

    private DataFileType detectFileType(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return DataFileType.UNKNOWN;
        }

        String first = null;
        String second = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;
                if (first == null) {
                    first = line;
                } else {
                    second = line;
                    break;
                }
            }
        } catch (IOException e) {
            return DataFileType.UNKNOWN;
        }

        String lowerFirst = first == null ? "" : first.toLowerCase();
        String lowerSecond = second == null ? "" : second.toLowerCase();

        boolean firstHasCourseCode = lowerFirst.startsWith("coursecode_") || lowerFirst.contains("coursecode");
        boolean secondHasCourseCode = lowerSecond.startsWith("coursecode_") || lowerSecond.contains("coursecode");
        boolean looksLikeAttendanceList = lowerSecond.startsWith("[") || lowerSecond.contains("std_id_");

        if (lowerFirst.contains("classroom") || lowerFirst.contains("capacity") || lowerFirst.contains(";")
                || lowerSecond.contains(";")) {
            return DataFileType.CLASSROOMS;
        }

        // attendance uses course codes followed by a list of Std_ID entries
        if (firstHasCourseCode && looksLikeAttendanceList) {
            return DataFileType.ATTENDANCE;
        }

        if (lowerFirst.contains("course") && secondHasCourseCode) {
            return DataFileType.COURSES;
        }

        if (firstHasCourseCode) {
            return DataFileType.COURSES;
        }

        if (secondHasCourseCode) {
            return DataFileType.COURSES;
        }

        if (lowerFirst.contains("student") || lowerFirst.contains("std_id_") || lowerSecond.contains("std_id_")) {
            return DataFileType.STUDENTS;
        }

        return DataFileType.UNKNOWN;
    }

    private void importAllData() {
        List<File> files = chooseCsvFiles("Select data files (students, classrooms, courses, attendance)");
        if (files == null || files.isEmpty()) {
            return;
        }

        Map<DataFileType, File> detected = new EnumMap<>(DataFileType.class);
        ArrayList<String> unknown = new ArrayList<>();
        ArrayList<String> duplicates = new ArrayList<>();

        for (File file : files) {
            DataFileType type = detectFileType(file);
            if (type == DataFileType.UNKNOWN) {
                unknown.add(file.getName());
                continue;
            }
            if (detected.containsKey(type)) {
                duplicates.add(file.getName());
                continue;
            }
            detected.put(type, file);
        }

        ArrayList<String> loaded = new ArrayList<>();

        if (detected.containsKey(DataFileType.STUDENTS)) {
            importStudents(detected.get(DataFileType.STUDENTS));
            loaded.add("students");
        }
        if (detected.containsKey(DataFileType.CLASSROOMS)) {
            importClassrooms(detected.get(DataFileType.CLASSROOMS));
            loaded.add("classrooms");
        }
        if (detected.containsKey(DataFileType.COURSES)) {
            importCourses(detected.get(DataFileType.COURSES));
            loaded.add("courses");
        }
        if (detected.containsKey(DataFileType.ATTENDANCE)) {
            importAttendance(detected.get(DataFileType.ATTENDANCE));
            loaded.add("attendance");
        }

        DataFileType[] requiredTypes = new DataFileType[] {
                DataFileType.STUDENTS,
                DataFileType.CLASSROOMS,
                DataFileType.COURSES,
                DataFileType.ATTENDANCE };
        String[] requiredLabels = new String[] {
                "students",
                "classrooms",
                "courses",
                "attendance" };

        ArrayList<String> missing = new ArrayList<>();
        for (int i = 0; i < requiredTypes.length; i++) {
            if (!detected.containsKey(requiredTypes[i])) {
                missing.add(requiredLabels[i]);
            }
        }

        StringBuilder summary = new StringBuilder();
        if (!loaded.isEmpty()) {
            summary.append("Auto-imported ").append(String.join(", ", loaded)).append(".");
        } else {
            summary.append("No known data files selected.");
        }
        if (!missing.isEmpty()) {
            summary.append(" Missing: ").append(String.join(", ", missing)).append(".");
        }
        if (!unknown.isEmpty()) {
            summary.append(" Unrecognized: ").append(String.join(", ", unknown)).append(".");
        }
        if (!duplicates.isEmpty()) {
            summary.append(" Skipped duplicates: ").append(String.join(", ", duplicates)).append(".");
        }

        statusLabel.setText(summary.toString());
    }

    private void importStudents() {
        importStudents(chooseCsvFile("Import students"));
    }

    private void importStudents(File file) {
        if (file == null)
            return;
        try {
            students.setAll(FileManager.readStudents(file.getAbsolutePath()));
            showStudents();
            statusLabel.setText("Imported students from " + file.getName());
        } catch (Exception e) {
            statusLabel.setText("Import failed: " + e.getMessage());
        }
    }

    private void importClassrooms() {
        importClassrooms(chooseCsvFile("Import classrooms"));
    }

    private void importClassrooms(File file) {
        if (file == null)
            return;
        try {
            classrooms.setAll(FileManager.readClassrooms(file.getAbsolutePath()));
            showClassrooms();
            statusLabel.setText("Imported classrooms from " + file.getName());
        } catch (Exception e) {
            statusLabel.setText("Import failed: " + e.getMessage());
        }
    }

    private void importCourses() {
        importCourses(chooseCsvFile("Import courses"));
    }

    private void importCourses(File file) {
        if (file == null)
            return;
        try {
            courses.setAll(FileManager.readCourses(file.getAbsolutePath(), new ArrayList<>(students),
                    new ArrayList<>(classrooms)));
            showCourses();
            statusLabel.setText("Imported courses from " + file.getName());
        } catch (Exception e) {
            statusLabel.setText("Import failed: " + e.getMessage());
        }
    }

    private void importAttendance() {
        importAttendance(chooseCsvFile("Import attendance"));
    }

    private void importAttendance(File file) {
        if (file == null)
            return;
        try {
            FileManager.readAttendance(file.getAbsolutePath(), new ArrayList<>(students), new ArrayList<>(courses));
            Main.calculate(new ArrayList<>(classrooms), new ArrayList<>(courses), new ArrayList<>(students));
            showCourses();
            showStudents();
            statusLabel.setText("Imported attendance from " + file.getName());

        } catch (Exception e) {
            statusLabel.setText("Import failed: " + e.getMessage());
        }
    }

    private <T> TableColumn<T, String> tableColumn(String title, Function<T, String> mapper) {
        TableColumn<T, String> col = new TableColumn<>(title);
        col.setCellValueFactory(cell -> new SimpleStringProperty(mapper.apply(cell.getValue())));
        return col;
    }

    // Renders long text inside a scrollable cell so large student lists don't stretch the table
    private <T> TableColumn<T, String> scrollableColumn(String title, Function<T, String> mapper) {
        TableColumn<T, String> col = new TableColumn<>(title);
        col.setPrefWidth(220);
        col.setCellValueFactory(cell -> {
            T value = cell.getValue();
            String mapped = value == null ? "" : mapper.apply(value);
            return new SimpleStringProperty(mapped == null ? "" : mapped);
        });
        col.setCellFactory(column -> new TableCell<>() {
            private final Label label = new Label();
            private final ScrollPane scroller = new ScrollPane(label);

            {
                label.setWrapText(true);
                label.setMaxWidth(Double.MAX_VALUE);
                scroller.setFitToWidth(true);
                scroller.getStyleClass().add("students-scroll");
                scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                scroller.setPannable(true);
                scroller.setPrefViewportHeight(48);
                scroller.setMinHeight(48);
                scroller.setMaxHeight(96);
                scroller.setPadding(new Insets(2, 0, 2, 0));
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setGraphic(null);
                } else {
                    label.setText(item);
                    setGraphic(scroller);
                }
                setText(null);
            }
        });
        return col;
    }

    private <T> VBox buildScheduleWithSearch(TableView<T> table, List<T> rows, Function<T, String> searchMapper) {
        return buildScheduleWithSearch(table, rows, searchMapper, "Search schedule");
    }

    private <T> VBox buildScheduleWithSearch(TableView<T> table, List<T> rows, Function<T, String> searchMapper,
            String placeholder) {
        ObservableList<T> data = FXCollections.observableArrayList(rows);
        FilteredList<T> filtered = new FilteredList<>(data, r -> true);

        TextField searchBox = new TextField();
        searchBox.setPromptText(placeholder);
        searchBox.setMaxWidth(Double.MAX_VALUE);
        searchBox.textProperty().addListener((obs, oldText, newText) -> {
            String query = newText == null ? "" : newText.trim().toLowerCase();
            filtered.setPredicate(item -> {
                if (item == null) {
                    return false;
                }
                if (query.isEmpty()) {
                    return true;
                }
                String content = searchMapper.apply(item);
                if (content == null) {
                    content = "";
                }
                return content.toLowerCase().contains(query);
            });
        });

        table.setItems(filtered);

        VBox layout = new VBox(10, searchBox, table);
        layout.setPadding(new Insets(10));
        layout.setFillWidth(true);
        table.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(table, Priority.ALWAYS);
        return layout;
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
        if (!hasAnySelection()) {
            if (confirmClearAllData()) {
                clearAllImportedData();
            } else {
                statusLabel.setText("Delete cancelled.");
            }
            return;
        }

        Object selected = getSelectionFor(currentView);
        if (selected == null) {
            statusLabel.setText("Select a row to delete.");
            return;
        }

        boolean removed = switch (currentView) {
            case COURSES -> courses.remove(selected);
            case CLASSROOMS -> {
                boolean r = classrooms.remove(selected);
                if (r) {
                    FileManager.exportClassrooms(
                            new ArrayList<>(classrooms),
                            "data/sampleData_AllClassroomsAndTheirCapacities.csv");
                }
                yield r;
            }
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

    private boolean hasAnySelection() {
        return (coursesTable != null && coursesTable.getSelectionModel().getSelectedItem() != null)
                || (classroomsTable != null && classroomsTable.getSelectionModel().getSelectedItem() != null)
                || (studentsTable != null && studentsTable.getSelectionModel().getSelectedItem() != null);
    }

    private boolean confirmClearAllData() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(primaryStage);
        alert.setTitle("Delete all data?");
        alert.setHeaderText(null);
        alert.setContentText("This will remove all imported courses, classrooms, and students. Continue?");
        alert.getDialogPane().setGraphic(null);
        styleDialog(alert.getDialogPane());

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void styleDialog(DialogPane pane) {
        if (pane == null) {
            return;
        }
        final String bg = "#0c1224";
        final String fg = "#e0e7ff";

        Runnable apply = () -> {
            pane.setStyle("-fx-background-color: " + bg + "; -fx-control-inner-background: " + bg + "; -fx-text-fill: "
                    + fg + ";");

            var scene = pane.getScene();
            if (scene != null) {
                scene.setFill(Color.web(bg));
            }

            var header = pane.lookup(".header-panel");
            if (header != null) {
                header.setStyle("-fx-background-color: " + bg + ";");
            }
            var headerLabel = pane.lookup(".header-panel .label");
            if (headerLabel instanceof Label label) {
                label.setTextFill(Color.web(fg));
            }

            var content = pane.lookup(".content");
            if (content != null) {
                content.setStyle("-fx-background-color: " + bg + ";");
            }
            var contentLabel = pane.lookup(".content.label");
            if (contentLabel instanceof Label label) {
                label.setTextFill(Color.web(fg));
            } else if (content instanceof Label label) {
                label.setTextFill(Color.web(fg));
            }

            var buttonBar = pane.lookup(".button-bar");
            if (buttonBar != null) {
                buttonBar.setStyle("-fx-background-color: " + bg + ";");
            }
        };

        apply.run();
        pane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(apply);
            }
        });
    }

    private void clearAllImportedData() {
        courses.clear();
        classrooms.clear();
        students.clear();
        Main.setResults(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        if (coursesTable != null) {
            coursesTable.getSelectionModel().clearSelection();
        }
        if (classroomsTable != null) {
            classroomsTable.getSelectionModel().clearSelection();
        }
        if (studentsTable != null) {
            studentsTable.getSelectionModel().clearSelection();
        }

        refreshAllTables();
        statusLabel.setText("All imported data deleted.");
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

    private <T> void enableRowDeselection(TableView<T> table, View view) {
        table.setRowFactory(tv -> {
            TableRow<T> row = new TableRow<>();
            row.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                setCurrentView(view);
                if (row.isEmpty()) {
                    table.getSelectionModel().clearSelection();
                    event.consume();
                } else if (table.getSelectionModel().isSelected(row.getIndex())) {
                    table.getSelectionModel().clearSelection();
                    statusLabel.setText("Selection cleared.");
                    event.consume();
                }
            });
            return row;
        });

        table.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                table.getSelectionModel().clearSelection();
                statusLabel.setText("Selection cleared.");
                event.consume();
            }
        });
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
        String normalized = query.toLowerCase();
        if (String.valueOf(course.getCode()).toLowerCase().contains(normalized)) {
            return true;
        }
        if (String.valueOf(course.getExamDuration()).toLowerCase().contains(normalized)) {
            return true;
        }

        if (course.getExamClass() != null) {
            for (Classroom room : course.getExamClass()) {
                if (room != null && String.valueOf(room.getName()).toLowerCase().contains(normalized)) {
                    return true;
                }
            }
        }

        // Only match attendees when the query is numeric and equals the student ID
        if (normalized.chars().allMatch(Character::isDigit)) {
            try {
                int targetId = Integer.parseInt(normalized);
                Student[] attendees = course.getAttendees();
                if (attendees != null) {
                    for (Student student : attendees) {
                        if (student != null && student.getID() == targetId) {
                            return true;
                        }
                    }
                }
            } catch (NumberFormatException ignored) {
                // non-numeric attendee queries are ignored
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
            File saveFile = chooseSaveFile("Export Updated Attendance");
            if (saveFile != null) {
                FileManager.exportAttendance(new ArrayList<>(courses), saveFile.getAbsolutePath());
            }

        }
        FileManager.exportAttendance(
                new ArrayList<>(courses),
                "data/sampleData_AllAttendanceLists.csv");
        FileManager.exportStudents(
                new ArrayList<>(students),
                "data/sampleData_AllStudents.csv");

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
        applyAppIcon(dialog);
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
                FileManager.exportStudents(
                        new ArrayList<>(students),
                        "data/sampleData_AllStudents.csv");
                dialog.close();
                refreshCurrentView();
                statusLabel.setText("Student added.");
            } catch (NumberFormatException ex) {
                feedback.setText("Enter a valid numeric ID.");
            }
        });

        VBox layout = new VBox(10, form, save, feedback);
        layout.setPadding(new Insets(12));
        dialog.setScene(buildStyledDialogScene(layout));
        dialog.showAndWait();
    }

    private void showEditStudentDialog(Student student) {
    Stage dialog = createDialog("Edit student");

    GridPane form = new GridPane();
    form.setPadding(new Insets(12));
    form.setHgap(8);
    form.setVgap(8);

    // ID Alanı
    Label idLabel = new Label("Student ID:");
    TextField idField = new TextField(String.valueOf(student.getID()));
    form.addRow(0, idLabel, idField);

    // Kurs Seçim Listesi (ListView)
    Label coursesLabel = new Label("Enrolled Courses:");
    ListView<Course> coursesListView = new ListView<>(courses);
    coursesListView.getStyleClass().add("dialog-list");
    coursesListView.setPrefHeight(200);

    // Checkbox durumlarını takip etmek için map
    Map<Course, BooleanProperty> selectedCourses = new LinkedHashMap<>();

    coursesListView.setCellFactory(CheckBoxListCell.forListView(course -> {
        final boolean isEnrolled = (course.getAttendees() != null) && 
            Arrays.stream(course.getAttendees())
                  .anyMatch(s -> s != null && s.getID() == student.getID());
        
        // Bu sayede lambda içinde güvenle kullanılabilir
        BooleanProperty prop = selectedCourses.computeIfAbsent(course, 
            c -> new SimpleBooleanProperty(isEnrolled));
        return prop;
    }, new StringConverter<Course>() {
        @Override public String toString(Course c) { return "Course: " + c.getCode(); }
        @Override public Course fromString(String s) { return null; }
    }));

    form.addRow(1, coursesLabel, coursesListView);

    Label feedback = new Label();
    Button save = new Button("Save changes");

    save.setOnAction(event -> {
        try {
            int newId = Integer.parseInt(idField.getText().trim());
            
            // Öğrencinin ID'sini güncelle
            student.setID(newId);

            // Kurs katılım listelerini güncelle
            for (Course course : courses) {
                BooleanProperty isSelected = selectedCourses.get(course);
                if (isSelected == null) continue;

                // Mevcut katılımcıları listeye al
                List<Student> attendeesList = new ArrayList<>(Arrays.asList(
                    course.getAttendees() != null ? course.getAttendees() : new Student[0]
                ));

                boolean currentlyIn = attendeesList.stream()
                        .anyMatch(s -> s != null && s.getID() == student.getID());

                if (isSelected.get() && !currentlyIn) {
                    // Checkbox işaretli ama listede yoksa ekle
                    attendeesList.add(student);
                    course.setAttendees(attendeesList.toArray(new Student[0]));
                } else if (!isSelected.get() && currentlyIn) {
                    // Checkbox işareti kalkmış ama listede varsa çıkar
                    attendeesList.removeIf(s -> s != null && s.getID() == student.getID());
                    course.setAttendees(attendeesList.toArray(new Student[0]));
                }
            }

            FileManager.exportStudents(new ArrayList<>(students), "data/sampleData_AllStudents.csv");
            FileManager.exportAttendance(new ArrayList<>(courses), "data/sampleData_AllAttendanceLists.csv");

            dialog.close();
            refreshCurrentView();
            statusLabel.setText("Student ID and courses updated.");
            
        } catch (NumberFormatException ex) {
            feedback.setText("Enter a valid numeric ID.");
        }
    });

    VBox layout = new VBox(10, form, save, feedback);
    layout.setPadding(new Insets(12));
    dialog.setScene(buildStyledDialogScene(layout, 450, 500));
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
                FileManager.exportClassrooms(
                        new ArrayList<>(classrooms),
                        "data/sampleData_AllClassroomsAndTheirCapacities.csv");
                dialog.close();
                refreshCurrentView();
                statusLabel.setText("Classroom added.");
            } catch (NumberFormatException ex) {
                feedback.setText("Enter valid numeric values.");
            }
        });

        VBox layout = new VBox(10, form, save, feedback);
        layout.setPadding(new Insets(12));
        dialog.setScene(buildStyledDialogScene(layout));
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
                FileManager.exportClassrooms(
                        new ArrayList<>(classrooms),
                        "data/sampleData_AllClassroomsAndTheirCapacities.csv");
                dialog.close();
                refreshCurrentView();
                statusLabel.setText("Classroom updated.");
            } catch (NumberFormatException ex) {
                feedback.setText("Enter valid numeric values.");
            }
        });

        VBox layout = new VBox(10, form, save, feedback);
        layout.setPadding(new Insets(12));
        dialog.setScene(buildStyledDialogScene(layout));
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
        studentsList.getStyleClass().add("dialog-list");
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
                        "data/sampleData_AllAttendanceLists.csv");

                dialog.close();
                refreshCurrentView();
            } catch (NumberFormatException ex) {
                feedback.setText("Code and duration must be numeric.");
            }
        });

        VBox layout = new VBox(10, form, save, feedback);
        layout.setPadding(new Insets(12));
        dialog.setScene(buildStyledDialogScene(layout, 520, 520));
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

    private Scene buildStyledDialogScene(Parent root, double width, double height) {
        if (root != null) {
            if (!root.getStyleClass().contains("dialog")) {
                root.getStyleClass().add("dialog");
            }
            if (!root.getStyleClass().contains("dialog-window")) {
                root.getStyleClass().add("dialog-window");
            }
        }
        Scene scene = (width > 0 && height > 0) ? new Scene(root, width, height) : new Scene(root);
        scene.setFill(Color.web("#0c1224"));
        attachStyles(scene);
        return scene;
    }

    private Scene buildStyledDialogScene(Parent root) {
        return buildStyledDialogScene(root, -1, -1);
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

    private Image loadAppIcon() {
        try {
            var stream = GUI.class.getResourceAsStream("/iconn.png");
            if (stream != null) {
                return new Image(stream);
            }
        } catch (Exception ignored) {
        }
        File fallback = new File("demo/src/main/resources/iconn.png");
        if (fallback.exists()) {
            try {
                return new Image(fallback.toURI().toString());
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private void applyAppIcon(Stage stage) {
        if (stage != null && appIcon != null && !stage.getIcons().contains(appIcon)) {
            stage.getIcons().add(appIcon);
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
        if (student == null) {
            statusLabel.setText("No student selected.");
            return;
        }

        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        applyAppIcon(dialog);
        dialog.setTitle("Schedule for Student " + student.getID());

        TableView<CourseStudentRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No scheduled exams found for this student."));

        TableColumn<CourseStudentRow, String> colStudent = new TableColumn<>("Student");
        colStudent.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().studentId)));

        TableColumn<CourseStudentRow, String> colCourse = new TableColumn<>("Course");
        colCourse.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().courseCode)));

        TableColumn<CourseStudentRow, String> colRoom = new TableColumn<>("Classroom");
        colRoom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().room));

        TableColumn<CourseStudentRow, String> colDay = new TableColumn<>("Day");
        colDay.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().day)));

        TableColumn<CourseStudentRow, String> colTime = new TableColumn<>("Time");
        colTime.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().time));

        table.getColumns().addAll(colStudent, colCourse, colRoom, colDay, colTime);

        ArrayList<CourseStudentRow> rows = new ArrayList<>();

        for (Course c : courses) {
            if (c == null || c.getAttendees() == null)
                continue;

            boolean attends = Arrays.stream(c.getAttendees())
                    .filter(Objects::nonNull)
                    .anyMatch(s -> s.getID() == student.getID());

            if (!attends)
                continue;

            String timeText = (c.getTimeOfExam() == null)
                    ? "-"
                    : c.getTimeOfExam() + " - " + c.getEndOfExam();

            String roomName = "-";
            if (c.getExamClass() != null && !c.getExamClass().isEmpty()) {
                Classroom r = c.getExamClass().get(0);
                if (r != null)
                    roomName = String.valueOf(r.getName());
            }

            rows.add(new CourseStudentRow(
                    student.getID(),
                    c.getCode(),
                    roomName,
                    c.getExamDay(),
                    timeText));
        }

        VBox root = buildScheduleWithSearch(
                table,
                rows,
                r -> r == null ? "" : String.valueOf(r.courseCode),
                "Search by course code");

        dialog.setScene(buildStyledDialogScene(root, 900, 500));
        dialog.showAndWait();
    }

    public void showCourseSchedule(Course course) {
        if (course == null) {
            statusLabel.setText("No course selected.");
            return;
        }

        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        applyAppIcon(dialog);
        dialog.setTitle("Exam Schedule - Course " + course.getCode());

        TableView<CourseRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No exams to display."));

        TableColumn<CourseRow, String> colCourse = new TableColumn<>("Course");
        colCourse.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().courseCode)));

        TableColumn<CourseRow, String> colRoom = new TableColumn<>("Classroom");
        colRoom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().room));

        TableColumn<CourseRow, String> colCap = new TableColumn<>("Capacity");
        colCap.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().capacity)));

        TableColumn<CourseRow, String> colDay = new TableColumn<>("Day");
        colDay.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().day)));

        TableColumn<CourseRow, String> colTime = new TableColumn<>("Time");
        colTime.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().time));

        TableColumn<CourseRow, String> colStudents = scrollableColumn("Students", r -> r.students);

        table.getColumns().addAll(colCourse, colRoom, colCap, colDay, colTime, colStudents);

        ArrayList<CourseRow> rows = new ArrayList<>();

        for (Course c : courses) {
            if (c == null)
                continue;
            if (c.getCode() != course.getCode())
                continue; // sadece seçilen ders

            String timeText = (c.getTimeOfExam() == null)
                    ? "-"
                    : c.getTimeOfExam() + " - " + c.getEndOfExam();

            String studentList = (c.getAttendees() == null)
                    ? "-"
                    : Arrays.stream(c.getAttendees())
                            .filter(Objects::nonNull)
                            .map(s -> String.valueOf(s.getID()))
                            .collect(Collectors.joining(", "));

            List<Classroom> examRooms = c.getExamClass() == null
                    ? new ArrayList<>()
                    : c.getExamClass().stream()
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

            String roomNames = examRooms.isEmpty()
                    ? "-"
                    : examRooms.stream()
                            .map(r -> String.valueOf(r.getName()))
                            .collect(Collectors.joining(", "));

            int totalCapacity = examRooms.stream()
                    .mapToInt(Classroom::getCapacity)
                    .sum();

            rows.add(new CourseRow(
                    c.getCode(),
                    roomNames,
                    totalCapacity,
                    c.getExamDay(),
                    timeText,
                    studentList));
        }
        VBox root = buildScheduleWithSearch(
                table,
                rows,
                r -> r == null ? "" : String.valueOf(r.courseCode),
                "Search by course code");

        dialog.setScene(buildStyledDialogScene(root, 900, 500));
        dialog.showAndWait();
    }

    public void showClassroomSchedule(Classroom classroom) {
        System.out.println("POPUP ÇAĞRILDI (CLASSROOM)");
        if (classroom == null) {
            statusLabel.setText("No classroom selected.");
            return;
        }

        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        applyAppIcon(dialog);
        dialog.setTitle("Schedule for Classroom " + classroom.getName());

        TableView<ClassroomRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No scheduled exams found for this classroom."));

        TableColumn<ClassroomRow, String> colRoom = new TableColumn<>("Classroom");
        colRoom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().room));

        TableColumn<ClassroomRow, String> colCap = new TableColumn<>("Capacity");
        colCap.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().capacity)));

        TableColumn<ClassroomRow, String> colCourse = new TableColumn<>("Course");
        colCourse.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().courseCode)));

        TableColumn<ClassroomRow, String> colDay = new TableColumn<>("Day");
        colDay.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().day)));

        TableColumn<ClassroomRow, String> colTime = new TableColumn<>("Time");
        colTime.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().time));

        TableColumn<ClassroomRow, String> colStudents = scrollableColumn("Students", r -> r.students);

        table.getColumns().addAll(colRoom, colCap, colCourse, colDay, colTime, colStudents);

        ArrayList<ClassroomRow> rows = new ArrayList<>();

        for (Course c : courses) {
            if (c == null || c.getExamClass() == null)
                continue;

            boolean usesRoom = c.getExamClass().stream()
                    .anyMatch(r -> r != null && r.getName() == classroom.getName());

            if (!usesRoom)
                continue;

            String timeText = (c.getTimeOfExam() == null)
                    ? "-"
                    : c.getTimeOfExam() + " - " + c.getEndOfExam();

            String studentList = (c.getAttendees() == null)
                    ? "-"
                    : Arrays.stream(c.getAttendees())
                            .filter(Objects::nonNull)
                            .map(s -> String.valueOf(s.getID()))
                            .collect(Collectors.joining(", "));

            rows.add(new ClassroomRow(
                    String.valueOf(classroom.getName()),
                    classroom.getCapacity(),
                    c.getCode(),
                    c.getExamDay(),
                    timeText,
                    studentList));
        }

        VBox root = buildScheduleWithSearch(
                table,
                rows,
                r -> r == null ? "" : r.room,
                "Search by classroom");

        dialog.setScene(buildStyledDialogScene(root, 900, 500));
        dialog.showAndWait();
    }

    private void rescheduleAll() {
        if (courses.isEmpty() || classrooms.isEmpty() || students.isEmpty()) {
            statusLabel.setText("Please import students, classrooms, courses and attendance first.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(primaryStage);
        alert.setTitle("Reschedule Exams");
        alert.setHeaderText("This will rebuild the entire exam schedule.");
        alert.setContentText("Are you sure you want to continue?");
        styleDialog(alert.getDialogPane());
        alert.setGraphic(null);
        Platform.runLater(() -> applyAppIcon((Stage) alert.getDialogPane().getScene().getWindow()));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        Main.resetSchedule(
                new ArrayList<>(classrooms),
                new ArrayList<>(courses),
                new ArrayList<>(students));

        Main.calculate(
                new ArrayList<>(classrooms),
                new ArrayList<>(courses),
                new ArrayList<>(students));

        refreshAllTables();
        statusLabel.setText("Exam schedule rebuilt successfully.");
    }

    private File chooseSaveFile(String title) {
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        return chooser.showSaveDialog(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
