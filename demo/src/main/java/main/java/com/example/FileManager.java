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

import com.example.Classroom;
import com.example.Course;
import com.example.Schedule;
import com.example.Student;

import java.util.Arrays;


public class FileManager {

    private static String clean(String text) {
        if (text == null) return "";
        return text.replace("\uFEFF", "").trim();
    }

    public static List<String[]> FileReader(String filePath) {
        System.out.println("Trying to read: " + filePath);
System.out.println("Exists? " + Files.exists(Path.of(filePath)));

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

    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
        String line;
        boolean skipHeader = true;

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (skipHeader) { // ALL OF THE STUDENTS IN THE SYSTEM
                skipHeader = false;
                continue;
            }

            String digits = line.replaceAll("\\D+", "");
            if (digits.isEmpty()) continue;

            students.add(new Student(Integer.parseInt(digits)));
        }

    } catch (IOException e) {
        e.printStackTrace();
    }

    return students;
}

    

    public static ArrayList<Classroom> readClassrooms(String filePath) {
    ArrayList<Classroom> classrooms = new ArrayList<>();

    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
        String line;
        boolean skipHeader = true;

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (skipHeader) { // ALL OF THE CLASSROOMS...
                skipHeader = false;
                continue;
            }

            String[] parts = line.split(";");
            if (parts.length != 2) continue;

            int room = Integer.parseInt(parts[0].replaceAll("\\D+", ""));
            int cap  = Integer.parseInt(parts[1].replaceAll("\\D+", ""));

            classrooms.add(new Classroom(room, cap));
        }

    } catch (IOException e) {
        e.printStackTrace();
    }

    return classrooms;
}



public static ArrayList<Course> readCourses(
        String filePath,
        ArrayList<Student> students,
        ArrayList<Classroom> classrooms) {

    ArrayList<Course> courses = new ArrayList<>();

    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
        String line;
        boolean skipHeader = true;

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (skipHeader) {
                skipHeader = false;
                continue;
            }


            String digits = line.replaceAll("\\D+", "");
            if (digits.isEmpty()) continue;

            int code = Integer.parseInt(digits);

            courses.add(
                new Course(
                    code,
                    new Student[0],          
                    new ArrayList<>(),       
                    90                       
                )
            );
        }

    } catch (IOException e) {
        e.printStackTrace();
    }

    System.out.println("COURSES LOADED = " + courses.size());
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



public static void updateLine(
    //updating any lıne in  a csv file

        String filePath,
        int lineIndex,
        String[] newData
) {
    List<String[]> data = FileReader(filePath);

    if (data == null || lineIndex < 0 || lineIndex >= data.size()) {
        return;
    }

    data.set(lineIndex, newData);

    FileWriter(filePath, data);
}


public static void editStudent( String filePath, int studentId, int newStudentId) {
    List<String[]> data = FileReader(filePath);

    for (int i = 0; i < data.size(); i++) {
        if (data.get(i)[0].equals(String.valueOf(studentId))) {
            data.set(i, new String[]{ String.valueOf(newStudentId) });
            break;
        }
    }

    FileWriter(filePath, data);
}






}