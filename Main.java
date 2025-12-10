import java.util.ArrayList;

public class Main {

    ArrayList<Student> students = new ArrayList<>();
    ArrayList<Course> courses = new ArrayList<>();
    ArrayList<Classroom> classrooms = new ArrayList<>();


    // block calculations
    public static void calculateBlock(int startBlock, int durationMinutes, Classroom room) {
        int block = (int) Math.ceil(durationMinutes / 30.0); // how many blocks needed

        for (int i = 0; i <= block; i++) {
            room.getHours()[startBlock+i] = 1; // block is marked as occupied
        }
    }
    // control block is marked as occupied or not
    public static boolean canCalculateBlock(int startBlock, int durationMinutes, Classroom room) {
        int block = (int) Math.ceil(durationMinutes / 30.0);
        for (int i = 0; i <= block; i++) {
            if (room.getHours()[startBlock+i] == 1){
                return false; // can not schedule
            }
        }
        return true; // all blocks empty
    }
    public static void calculate(ArrayList<Classroom>  classrooms,ArrayList<Course>  courses,ArrayList<Student>  students){


    }



}
