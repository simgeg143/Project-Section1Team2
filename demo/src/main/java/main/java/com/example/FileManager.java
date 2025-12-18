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
            if (row.length == 0) continue;
            String digits = row[0].replaceAll("\\D+", "");
            if (digits.isEmpty()) continue;
            try {
                int id = Integer.parseInt(digits);
                students.add(new Student(id));
            } catch (NumberFormatException ignored) {
            }
        }

        return students;
    }
    

    public static ArrayList<Classroom> readClassrooms(String filePath) {
        ArrayList<Classroom> classrooms = new ArrayList<>();
        List<String[]> rows = FileReader(filePath);

        for (String[] row : rows) {
            if (row.length == 0) continue;

            // files can be "Classroom_01;40" or "101,40"
            String first = row[0];
            String second = row.length > 1 ? row[1] : "";
            if (first.contains(";")) {
                String[] parts = first.split(";");
                if (parts.length > 0) first = parts[0];
                if (parts.length > 1) second = parts[1];
            }

            String nameDigits = first.replaceAll("\\D+", "");
            String capDigits = second.replaceAll("\\D+", "");
            if (nameDigits.isEmpty() || capDigits.isEmpty()) continue;
            try {
                int name = Integer.parseInt(nameDigits);
                int capacity = Integer.parseInt(capDigits);
                classrooms.add(new Classroom(name, capacity));
            } catch (NumberFormatException ignored) {
            }
            
        }
        return classrooms ;

       
    }

    public static ArrayList<Course> readCourses(
            String filePath,
            ArrayList<Student> students,
            ArrayList<Classroom> classrooms) {

        ArrayList<Course> courses = new ArrayList<>();
        List<String[]> rows = FileReader(filePath);
for (String[] row : rows) {
            if (row.length == 0) continue;

            // Structured CSV: code,duration,studentIds,roomIds
            if (row.length >= 4) {
                String codeDigits = row[0].replaceAll("\\D+", "");
                String durationDigits = row[1].replaceAll("\\D+", "");
                if (codeDigits.isEmpty()) continue;

                int code;
                int duration = 90;
                try {
                    code = Integer.parseInt(codeDigits);
                } catch (NumberFormatException e) {
                    continue;
                }
                if (!durationDigits.isEmpty()) {
                    try {
                        duration = Integer.parseInt(durationDigits);
                    } catch (NumberFormatException ignored) {
                    }
                }
                if (duration <= 0) duration = 90;

                String[] studentIds = row[2].split("\\|");
                ArrayList<Student> attendeeList = new ArrayList<>();
                for (String idStr : studentIds) {
                    String digits = idStr.replaceAll("\\D+", "");
                    if (digits.isEmpty()) continue;
                    int id;
                    try {
                        id = Integer.parseInt(digits);
                    } catch (NumberFormatException e) {
                        continue;
                    }
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
                    String digits = roomStr.replaceAll("\\D+", "");
                    if (digits.isEmpty()) continue;
                    int roomName;
                    try {
                        roomName = Integer.parseInt(digits);
                    } catch (NumberFormatException e) {
                        continue;
                    }
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
                                duration > 0 ? duration : 90
                        )
                );
                continue;
            }

            // Simple file containing only course codes (e.g., sampleData_AllCourses.csv)
            String digits = row[0].replaceAll("\\D+", "");
            if (digits.isEmpty()) continue;
            try {
                int code = Integer.parseInt(digits);
                courses.add(new Course(code, new Student[0], new ArrayList<>(), 90));
            } catch (NumberFormatException ignored) {
            }
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
