package com.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.*;
import java.util.stream.Collectors;

public class FileManager {

    private static String clean(String text) {
        if (text == null)
            return "";
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
                if (line.isEmpty())
                    continue;

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
                if (line.isEmpty())
                    continue;

                if (skipHeader) { // ALL OF THE STUDENTS IN THE SYSTEM
                    skipHeader = false;
                    continue;
                }

                String digits = line.replaceAll("\\D+", "");
                if (digits.isEmpty())
                    continue;

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
                if (line.isEmpty())
                    continue;

                if (skipHeader) { // ALL OF THE CLASSROOMS...
                    skipHeader = false;
                    continue;
                }

                String[] parts = line.split(";");
                if (parts.length != 2)
                    continue;

                int room = Integer.parseInt(parts[0].replaceAll("\\D+", ""));
                int cap = Integer.parseInt(parts[1].replaceAll("\\D+", ""));

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
                if (line.isEmpty())
                    continue;

                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }

                String digits = line.replaceAll("\\D+", "");
                if (digits.isEmpty())
                    continue;

                int code = Integer.parseInt(digits);

                courses.add(
                        new Course(
                                code,
                                new Student[0],
                                new ArrayList<>(),
                                90));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("COURSES LOADED = " + courses.size());
        return courses;
    }

    public static void readAttendance(
            String filePath,
            ArrayList<Student> students,
            ArrayList<Course> courses) {

        int totalAssigned = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            Course currentCourse = null;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;

                if (line.startsWith("CourseCode_")) {
                    String digits = line.replaceAll("\\D+", "");
                    int courseCode = Integer.parseInt(digits);

                    currentCourse = courses.stream()
                            .filter(c -> c.getCode() == courseCode)
                            .findFirst()
                            .orElse(null);

                    continue;
                }

                if (line.startsWith("[")) {
                    if (currentCourse == null)
                        continue;
                    line = line.replace("[", "")
                            .replace("]", "")
                            .replace("'", "")
                            .replace("\"", "");
                    String[] i = line.split(",");

                    ArrayList<Student> list = new ArrayList<>();

                    for (String id : i) {
                        id = id.trim();
                        if (id.isEmpty())
                            continue;
                        String digits = id.replaceAll("\\D+", "");
                        if (digits.isEmpty())
                            continue;
                        int si = Integer.parseInt(digits);
                        Student s = students.stream()
                                .filter(st -> st.getID() == si)
                                .findFirst()
                                .orElse(null);
                        if (s != null) {
                            list.add(s);
                            totalAssigned++;
                        }
                    }
                    currentCourse.setAttendees(list.toArray(new Student[0]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Loaded attendance = " + totalAssigned);
    }

    public static void FileWriter(String filePath, List<String[]> data) {
        if (filePath == null || data == null)
            return;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (String[] row : data) {
                bw.write(String.join(",", row));
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("File write error: " + e.getMessage());
        }
    }

    // 4 tane update edilmiş csv exportu eklencek
    public static void ExportSchedule(Schedule[] schedules, String filePath) {

        if (schedules == null || filePath == null)
            return;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {

            bw.write("StudentID,CourseCode,Classroom,ExamDuration");
            bw.newLine();

            for (Schedule schedule : schedules) {
                if (schedule == null)
                    continue;

                int studentId = schedule.getStudent().getID();
                Course[] courses = schedule.getCourses();
                if (courses == null)
                    continue;

                for (Course c : courses) {
                    if (c == null)
                        continue;

                    ArrayList<Classroom> rooms = c.getExamClass();
                    if (rooms == null)
                        continue;

                    for (Classroom room : rooms) {
                        bw.write(
                                studentId + "," +
                                        c.getCode() + "," +
                                        room.getName() + "," +
                                        c.getExamDuration());
                        bw.newLine();
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Export error: " + e.getMessage());
        }
    }

    public static void updateLine(
            String filePath,
            int lineIndex,
            String[] newData) {
        List<String[]> data = FileReader(filePath);

        if (data == null || lineIndex < 0 || lineIndex >= data.size()) {
            return;
        }

        data.set(lineIndex, newData);

        FileWriter(filePath, data);
    } // CVC DEKİ HER SATIRI UPTADELEYEBİLMEK İÇİN
    public static void exportAttendance(
        ArrayList<Course> courses,
        String filePath) {

    if (courses == null || filePath == null) {
        return;
    }

    try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {

        for (Course c : courses) {

            bw.write("CourseCode_" + c.getCode());
            bw.newLine();

            Student[] attendees = c.getAttendees();

            if (attendees == null || attendees.length == 0) {
                bw.write("[]");
            } else {
                bw.write("[");
                for (int i = 0; i < attendees.length; i++) {
                    bw.write(String.valueOf(attendees[i].getID()));
                    if (i < attendees.length - 1) {
                        bw.write(", ");
                    }
                }
                bw.write("]");
            }

            bw.newLine();
        }

    } catch (IOException e) {
        e.printStackTrace();
    }
}
public static void exportStudents(
        ArrayList<Student> students,
        String filePath) {

    if (students == null || filePath == null) {
        return;
    }

    try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {

        // header
        bw.write("StudentID");
        bw.newLine();

        for (Student s : students) {
            if (s == null) continue;
            bw.write(String.valueOf(s.getID()));
            bw.newLine();
        }

    } catch (IOException e) {
        e.printStackTrace();
    }
}
public static void exportClassrooms(ArrayList<Classroom> classrooms, String filePath) {

    if (classrooms == null || filePath == null) return;

    try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {

        
        bw.write("Room;Capacity");
        bw.newLine();

        for (Classroom c : classrooms) {
            if (c == null) continue;
            bw.write(c.getName() + ";" + c.getCapacity());
            bw.newLine();
        }

    } catch (IOException e) {
        e.printStackTrace();
    }
}




    public static void editStudent(
            String filePath,
            int studentId,
            int newStudentId) {
        List<String[]> data = FileReader(filePath);

        for (int i = 0; i < data.size(); i++) {
            if (data.get(i)[0].equals(String.valueOf(studentId))) {
                data.set(i, new String[] { String.valueOf(newStudentId) });
                break;
            }
        }

        FileWriter(filePath, data);
    }

    public static void editCourse(
            String filePath,
            int courseCode,
            int newDuration) {
        List<String[]> data = FileReader(filePath);

        for (int i = 0; i < data.size(); i++) {
            if (data.get(i)[0].equals(String.valueOf(courseCode))) {
                data.set(i, new String[] {
                        String.valueOf(courseCode),
                        String.valueOf(newDuration)
                });
                break;
            }
        }

        FileWriter(filePath, data);
    }

    public static void editClassroom(
            String filePath,
            int roomNo,
            int newCapacity) {
        List<String[]> data = FileReader(filePath);

        for (int i = 0; i < data.size(); i++) {
            if (data.get(i)[0].equals(String.valueOf(roomNo))) {
                data.set(i, new String[] {
                        String.valueOf(roomNo),
                        String.valueOf(newCapacity)
                });
                break;
            }
        }

        FileWriter(filePath, data);
    }
    




  

public static void editAttendanceStudent(String filePath, int courseCode, Student oldStudent, Student newStudent) {
    List<String> lines = new ArrayList<>();
    
    
    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
        String line;
        while ((line = br.readLine()) != null) {
            lines.add(line);
        }
    } catch (IOException e) {
        e.printStackTrace();
        return;
    }

    try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
        boolean insideTargetCourse = false;
        
        Pattern pattern = Pattern.compile("Std_ID_(\\d+)");

        for (int i = 0; i < lines.size(); i++) {
            String currentLine = lines.get(i).trim();

            if (currentLine.startsWith("CourseCode_")) {
                int foundCode = Integer.parseInt(currentLine.replaceAll("\\D+", ""));
                insideTargetCourse = (foundCode == courseCode);
                bw.write(lines.get(i));
            } 
            else if (insideTargetCourse && currentLine.startsWith("[")) {
                Matcher matcher = pattern.matcher(currentLine);
                StringBuilder sb = new StringBuilder();
                int lastEnd = 0;

                while (matcher.find()) {
                   
                    int foundId = Integer.parseInt(matcher.group(1));
                    
                    
                    sb.append(currentLine, lastEnd, matcher.start());
                    
                    if (foundId == oldStudent.getID()) {
                        
                        sb.append("Std_ID_").append(newStudent.getID());
                    } else {
                       
                        sb.append(matcher.group());
                    }
                    lastEnd = matcher.end();
                }
                sb.append(currentLine.substring(lastEnd));
                bw.write(sb.toString());
                insideTargetCourse = false;
            } 
            else {
                bw.write(lines.get(i));
            }
            bw.newLine();
        }
    } catch (IOException e) {
        e.printStackTrace();
    }

    // düzenlendi  ve test edildi
}



public static void exportCourses(ArrayList<Course> courses, String filePath) {
    if (courses == null || filePath == null) {
        return;
    }

    try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
        
        bw.write("CourseCode,ExamDuration");
        bw.newLine();

        for (Course c : courses) {
            if (c == null) continue;
            
           
            bw.write(c.getCode() + "," + c.getExamDuration());
            bw.newLine();
        }

    } catch (IOException e) {
        System.out.println("Course export error: " + e.getMessage());
        e.printStackTrace();
    }
}










// deneme
// GUIdeki  tekli exam schedule exportlanmasi


public static void exportCourseExamSchedule(Course course, String filePath) {
    if (course == null || filePath == null) return;
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
        bw.write("Course,Classroom,Capacity,Day,Time,Students");
        bw.newLine();
        String rooms = course.getExamClass() == null ? "-" : course.getExamClass().stream().map(r -> String.valueOf(r.getName())).collect(Collectors.joining("; "));
        String time = course.getTimeOfExam() == null ? "-" : course.getTimeOfExam() + " - " + course.getEndOfExam();
        String studentList = (course.getAttendees() == null) ? "-" : Arrays.stream(course.getAttendees()).map(s -> String.valueOf(s.getID())).collect(Collectors.joining("; "));
        
        bw.write(course.getCode() + "," + rooms + "," + (course.getExamClass() != null ? course.getExamClass().get(0).getCapacity() : 0) + "," + course.getExamDay() + "," + time + ",\"" + studentList + "\"");
    } catch (IOException e) { e.printStackTrace(); }
}


public static void exportClassroomExamSchedule(Classroom classroom, ArrayList<Course> classroomCourses, String filePath) {
    if (classroom == null || filePath == null) return;
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
        bw.write("Classroom,Capacity,Course,Day,Time,Students");
        bw.newLine();
        for (Course c : classroomCourses) {
            String time = (c.getTimeOfExam() == null) ? "-" : c.getTimeOfExam() + " - " + c.getEndOfExam();
            String studentList = (c.getAttendees() == null) ? "-" : Arrays.stream(c.getAttendees()).map(s -> String.valueOf(s.getID())).collect(Collectors.joining("; "));
            bw.write(classroom.getName() + "," + classroom.getCapacity() + "," + c.getCode() + "," + c.getExamDay() + "," + time + ",\"" + studentList + "\"");
            bw.newLine();
        }
    } catch (IOException e) { e.printStackTrace(); }
}






}
