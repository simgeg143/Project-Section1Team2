package main.java.com.example;


import java.util.ArrayList;

public class Main {

    ArrayList<Student> students = new ArrayList<>();
    ArrayList<Course> courses = new ArrayList<>();
    ArrayList<Classroom> classrooms = new ArrayList<>();

    public static int calculateBlock(int durationMinutes) {
        /*
            calculates the amount of time blocks each exam would occupy. 
            Each block represents *30 MINUTES.*
        */
        int blockSize = (int) Math.ceil(durationMinutes / 30.0); // how many blocks (indexes in the array) are needed
        return blockSize;
    }

    public static boolean scheduleClass(int startBlock, int blockSize, Classroom classroom, Course course){
        /*
            Schedules the chosen blocks of the given classroom's time schedule.
        */
        if(checkBlocksAvailability(startBlock, blockSize, classroom)){
            for(int i = 0; i < blockSize; i++){
                classroom.getBlocks()[startBlock+i] = course; // block is marked as occupied
                classroom.decreaseAvailability(blockSize);
                
                return true;
            }
        }
        // in case the blocks are not available
        return false;
    }

    public static boolean checkBlocksAvailability(int startBlock, int blockSize, Classroom room) {
        /*
            Checks whether the targeted block(s) is/are available to be assigned to an exam.
        */
        for (int i = 0; i < blockSize; i++) {
            if (!(room.getBlocks()[startBlock + i] == null)){ // if the chosen block is not null
                return false; // can not schedule
            }
        }
        return true; // all blocks empty
    }

    public static void sortCourses(ArrayList<Course> courses) {
        /*
            Sorts the exam list according to each exam's duration.
            longer exams have HIGHER priority.
        */
        for (int i = 0; i < courses.size()-1; i++) {
            int maxIndex = i; // index of the longest exam found so far
            for (int j = i + 1; j < courses.size(); j++) { // compare durations and find the longer exam
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

    public static void sortClasses(ArrayList<Classroom> classrooms){
        /*
            Sorts the classes list according to each class's capacity.
            Classes with higher capacity have HIGHER priority.
            Higher (0) -----------------> Lower (n)         (array with length n)
        */
        for (int i = 0; i < classrooms.size()-1; i++) {
            int maxIndex = i; // index of the longest exam found so far
            for (int j = i + 1; j < classrooms.size(); j++) { // compare durations and find the longer exam
                if (classrooms.get(j).getCapacity() > classrooms.get(maxIndex).getCapacity()) {
                    maxIndex = j;
                }
            }
            // swap positions (place the longest exam earlier in the list)
            Classroom temp = classrooms.get(i);
            classrooms.set(i, classrooms.get(maxIndex));
            classrooms.set(maxIndex, temp);
        }
    }

    public static Classroom bestFittingClass(Course course, ArrayList<Classroom> classrooms){
        // the smallest capacity that is greater than or equal to the exam's required capacity
        sortClasses(classrooms);
        int chosenCapacity = classrooms.get(0).getCapacity();
        int chosenIndex = 0;
        // boolean
        for(int i = 0; i < classrooms.size(); i++){
            if(classrooms.get(i).getCapacity() >= course.getAttendees().length && classrooms.get(i).getCapacity() < chosenCapacity){
                chosenCapacity = classrooms.get(i).getCapacity();
                chosenIndex = i;
            }
        }
        if(classrooms.get(chosenIndex).getCapacity() >= course.getAttendees().length){ // if the fit is proper
            return classrooms.get(chosenIndex);
        }
        return null;
    }

    public static int findAvailableBlocks(Course course, ArrayList<Classroom> classrooms){
        boolean match;
        int blockSize = calculateBlock(course.getExamDuration());
        for(int i = 0; i < 24; i++){
            match = true;
            for(Classroom classroom: classrooms){
                if(!checkBlocksAvailability(i, blockSize, classroom)){
                    match = false;
                    break;
                }
            }
            if(match){
                return i;
            }
        }
        return -1; // NONE AVAILABLE
    }

    public static ArrayList<Integer> findAllFreeBlocks(Classroom classroom){
        ArrayList<Integer> result = new ArrayList<>();
        for(int i = 0; i < 24; i ++){
            if(classroom.getBlocks()[i] == null){
                result.add(i);
            }
        }
        return result;
    }

    // TODO
    public static boolean findMultipleClasses(Course course, ArrayList<Classroom> classrooms){
        // it must choose classrooms that can fit all the students and then look for a time when all are available. This is easily done if they fit in a single class.
        int blockSize = calculateBlock(course.getExamDuration());
        ArrayList<Integer> availableBlocks = findAllFreeBlocks(classrooms.get(0)); // initial class
        int overlap = course.getAttendees().length;
        ArrayList<Classroom> result = new ArrayList<>();
        
        for(Classroom classroom : classrooms){
            availableBlocks = findAllFreeBlocks(classroom); // getting class's all free blocks
            for(Integer block : availableBlocks){
                if(checkBlocksAvailability(block, blockSize, classroom)){ // if the other class is available at any same hour available
                    overlap -= classroom.getCapacity();

                    result.add(classroom);
                    if(overlap <= 0){
                        course.setExamClass(result);
                        return true;
                    }
                }
            }
        }
        // for(Integer block : availableBlocks){
        //     for(Classroom classroom : classrooms){
        //         if(checkBlocksAvailability(block, blockSize, classroom)){ // if the other class is available at any same hour available
        //             overlap -= classroom.getCapacity();
        //             result.add(classroom);
        //         }
        //         if(overlap <= 0) return result;
        //     }
        // }
        return false;
    }

    // TODO
    public static boolean findClassForExam(Course course, ArrayList<Classroom> classrooms){
        boolean done = false;
        int examDuration = calculateBlock(course.getExamDuration());
        Classroom bestfit = bestFittingClass(course, classrooms); // finding the best fitting classroom
        if(course.alreadyScheduled){    // for not to schedule already scheduled exams
            return false;
        }
        if(bestfit == null){
            // findMultipleClasses(course, classrooms); ------------------------------------------------------------------------------------>  UNCOMMENT AFTER MAKING THE METHOD
        }
        else{
            for(int i = 0; i < 24; i++){ // checking hour availability
                if(scheduleClass(i, examDuration, bestfit, course)){
                    ArrayList<Classroom> examClass = new ArrayList<>();
                    examClass.add(bestfit);
                    course.setExamClass(examClass);
                    done = true;
                }
            }
        }
        if(!done){ // no hour available
            System.out.println("A single class does not have enough capacity for this exam.\nTrying to find multiple ones...");
            // findMultipleClasses(course, classrooms); ------------------------------------------------------------------------------------>  UNCOMMENT AFTER MAKING THE METHOD
            return false;
        }
        return true;
    }

    // TODO
    public static void nextDay(ArrayList<Classroom> classrooms,ArrayList<Course> courses) {
    //reset blocks
    for (Classroom classroom : classrooms) {

        Course[] blocks = classroom.getBlocks();
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = null;
        }

        classroom.availability = 24;
        classroom.allBlocksFilled = false;
    }

    for (Course course : courses) {
        if (course.getExamClass() != null && !course.getExamClass().isEmpty()) {
            course.alreadyScheduled = true;
        }
    }

    System.out.println("Everything is ready for the next day.");
}


    // TODO
    public static boolean allClassHoursFilled(){
        // MUST LOOP OVER ALL CLASSES'S HOURS TO MAKE SURE ALL CLASSES ARE FULLY UTILIZED (SOME SMALL BLOCKS (1-2) CAN BE FREE)
        // this method will be used to make sure all classes are filled for the day so it can move onto the next one in the main loop.
        return false;
    }



    public static void calculate(ArrayList<Classroom> classrooms, ArrayList<Course> courses, ArrayList<Student> students){
        sortCourses(courses);
        sortClasses(classrooms);

        boolean allClassesFilled = false;
        ArrayList<ArrayList<Course>> days = new ArrayList<ArrayList<Course>>(); // a list of courses in a list of days

        // MAINLOOP (NEEDS TO BE FIXED)
        for(int i = 0; i < 7; i++){ // LOOPING FOR EACH DAY
            while(!allClassesFilled){ // while classes are available
                ArrayList<Course> dayCourses = new ArrayList<>();
                for(Course course : courses){
                    if(findClassForExam(course, classrooms)){
                        dayCourses.add(course);
                    }
                }
                days.add(dayCourses);
            }
            nextDay(classrooms, courses);
        }
        
        
        




        // TODO

    }



    public static void main(String[] args) {
        GUI.main(args);
    }

}
