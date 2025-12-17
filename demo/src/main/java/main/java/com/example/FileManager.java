package main.java.com.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    private static String clean(String text) {
        if (text == null) return "";
        return text.replace("\uFEFF", "").trim();
    }

    public static List<String[]> FileReader(String filePath) {
        List<String[]> rows = new ArrayList<>();

        if (filePath == null || filePath.trim().isEmpty() || !Files.exists(Path.of(filePath))) {
            return rows;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                line = clean(line);
                if (line.isEmpty()) continue;

                // skip header
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                rows.add(line.split(","));
            }
        } catch (IOException e) {
            System.out.println("File read error: " + e.getMessage());
        }

        return rows;
    }


    public static ArrayList<Student> readStudents(String filePath) {
        ArrayList<Student> students = new ArrayList<>();
        List<String[]> rows = FileReader(filePath);

        for (String[] row : rows) {
            int id = Integer.parseInt(row[0]);
            students.add(new Student(id));
        }

        return students;
    }

    public static ArrayList<Classroom> readClassrooms(String filePath) {
        ArrayList<Classroom> classrooms = new ArrayList<>();
        List<String[]> rows = FileReader(filePath);

        for (String[] row : rows) {
            int name = Integer.parseInt(row[0]);
            int capacity = Integer.parseInt(row[1]);
            classrooms.add(new Classroom(name, capacity));
        }

        return classrooms;
    }

    public static ArrayList<Course> readCourses(
            String filePath,
            ArrayList<Student> students,
            ArrayList<Classroom> classrooms) {

        ArrayList<Course> courses = new ArrayList<>();
        List<String[]> rows = FileReader(filePath);

        for (String[] row : rows) {
            int code = Integer.parseInt(row[0]);
            int duration = Integer.parseInt(row[1]);

            String[] studentIds = row[2].split("\\|");
            ArrayList<Student> attendeeList = new ArrayList<>();

            for (String idStr : studentIds) {
                int id = Integer.parseInt(idStr);
                for (Student s : students) {
                    if (s.getID() == id) {
                        attendeeList.add(s);
                        break;
                    }
                }
            }

            String[] roomNames = row[3].split("\\|");
            ArrayList<Classroom> roomList = new ArrayList<>();

            for (String roomStr : roomNames) {
                int roomName = Integer.parseInt(roomStr);
                for (Classroom c : classrooms) {
                    if (c.getName() == roomName) {
                        roomList.add(c);
                        break;
                    }
                }
            }

            courses.add(
                new Course(
                    code,
                    attendeeList.toArray(new Student[0]),
                    roomList,
                    duration
                )
            );
        }

        return courses;
    }

    public static void FileWriter(String filePath, List<String[]> data) {
        if (filePath == null || data == null) return;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (String[] row : data) {
                bw.write(String.join(",", row));
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("File write error: " + e.getMessage());
        }
    }

    public static void ExportSchedule(Schedule[] schedules, String filePath) {

        if (schedules == null || filePath == null) return;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {

            bw.write("StudentID,CourseCode,Classroom,ExamDuration");
            bw.newLine();

            for (Schedule schedule : schedules) {
                if (schedule == null) continue;

                int studentId = schedule.getStudent().getID();
                Course[] courses = schedule.getCourses();
                if (courses == null) continue;

                for (Course c : courses) {
                    if (c == null) continue;

                    ArrayList<Classroom> rooms = c.getExamClass();
                    if (rooms == null) continue;

                    for (Classroom room : rooms) {
                        bw.write(
                            studentId + "," +
                            c.getCode() + "," +
                            room.getName() + "," +
                            c.getExamDuration()
                        );
                        bw.newLine();
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Export error: " + e.getMessage());
        }
    }
}
