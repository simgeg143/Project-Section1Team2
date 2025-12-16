package demo.src.main.java.com.example;


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
    public static List<String[]>  FileReader(String filePath){
        List<String[]> rows = new ArrayList<>();
        if(filePath==null || filePath.trim().isEmpty() || !Files.exists(Path.of(filePath))){
            return rows;
        }
        try(BufferedReader br = new BufferedReader(new FileReader(filePath))){
            String line;
            boolean isFirstLine = true;
            while((line =br.readLine() ) != null){
                line =clean(line);
                if (line.isEmpty()) {

                    continue;
                }
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                rows.add(line.split(","));
            }

        } catch (IOException e){
            System.out.println("File read error: " + e.getMessage());
        }
        return  rows;
    }
    public static List<String[]>  readAttendance(String filePath){
        List<String[]> result = new ArrayList<>();
        if(filePath==null || filePath.trim().isEmpty() || !Files.exists(Path.of(filePath))){
            return result;
        }
        try(BufferedReader br = new BufferedReader(new FileReader(filePath))){
            String line;
            while((line = br.readLine() ) != null){
                line = clean(line);
                if (line.isEmpty()) {
                    continue;
                }
                if (!line.startsWith("CourseCode")) {
                    continue;
                }
                String courseCode = line;
                String student = br.readLine();
                if (student == null) {
                    break;
                }
                student = clean(student);

                student = student
                        .replace("[", "")
                        .replace("]", "")
                        .replace("'", "")
                        .trim();
                if (student.isEmpty()) {
                    continue;
                }
                String[] attendance = student.split("\\s*,\\s*");
                result.add(new String[]{courseCode, String.valueOf(attendance.length)});
            }

        } catch (IOException e){
            System.out.println("Attendance File read error: " + e.getMessage());
        }
        return  result;
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
