import java.util.ArrayList;

public class Main {

    ArrayList<Student> students = new ArrayList<>();
    ArrayList<Course> courses = new ArrayList<>();
    ArrayList<Classroom> classrooms = new ArrayList<>();


    // block calculations
    public static void calculateBlock(int startBlock, int durationMinutes, Classroom room) {
        int block = (int) Math.ceil(durationMinutes / 30.0); // how many blocks needed

        for (int i = 0; i <= block; i++) {
            room.getHours().set(startBlock+i,1); // block is marked as occupied
        }
    }
    // control block is marked as occupied or not
    public static boolean canCalculateBlock(int startBlock, int durationMinutes, Classroom room) {
        int block = (int) Math.ceil(durationMinutes / 30.0); // Math.ceil() rounds the number up to the nearest whole number
        for (int i = 0; i <= block; i++) {
            if (room.getHours().get(startBlock + i) == 1){
                return false; // can not schedule
            }
        }
        return true; // all blocks empty
    }
    // sorts the exam list by duration (longest exams first)
    public static void sort(ArrayList<Course> courses) {
        for (int i = 0; i < courses.size()-1; i++) {
            int maxIndex = i; // index of the longest exam found so far
            for (int j = i + 1; j < courses.size(); j++) {
                // compare durations and find the longer exam
                if (courses.get(j).getExamDuration() > courses.get(maxIndex).getExamDuration()) {
                    maxIndex = j;
                }
            }
            // swap positions (place the longest exam earlier in the list)
            Course temp = courses.get(i);
            courses.set(i, courses.get(maxIndex));
            courses.set(maxIndex, temp);
        }
    }
    public static void calculate(ArrayList<Classroom>  classrooms,ArrayList<Course>  courses,ArrayList<Student>  students){

    }



}
