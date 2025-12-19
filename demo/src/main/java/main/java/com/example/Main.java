package main.java.com.example;

import java.util.ArrayList;

public class Main {

    ArrayList<Student> students = new ArrayList<>();
    ArrayList<Course> courses = new ArrayList<>();
    ArrayList<Classroom> classrooms = new ArrayList<>();
    /*

    <-------------------------------------------- CAN CHANGE THE SIZE OF EACH BLOCK HERE -------------------------------------------->
            Each exam day is considered as 12 hours, from 9 a.m to 9 p.m.
            eachBlockDuration is set in minutes.
            blocksPerDay is calculated based on eachBlockDuration.

            NOTE: Try to choose the block duration in a way 12*60 is divisible by it. (avoiding time waste)

    */

    public static int eachBlockDuration = 30; // in minutes
    public static int blocksPerDay = (12 * 60) / eachBlockDuration;


    public static int calculateBlock(int durationMinutes) {
        /*
<<<<<<< Updated upstream
         * calculates the amount of time blocks each exam would occupy.
         * Each block represents *30 MINUTES.*
         */
        int blockSize = (int) Math.ceil(durationMinutes / 30.0); // how many blocks (indexes in the array) are needed
=======
            calculates the amount of time blocks each exam would occupy. 
            Each block represents the configured duration (eachBlockDuration).
        */

        int blockSize = (int) Math.ceil(durationMinutes / (double) eachBlockDuration);
>>>>>>> Stashed changes
        return blockSize;
    }

    public static boolean scheduleClass(int startBlock, int blockSize, Classroom classroom, Course course) {
        /*
<<<<<<< Updated upstream
         * Schedules the chosen blocks of the given classroom's time schedule.
         */
        if (checkBlocksAvailability(startBlock, blockSize, classroom)) {
            for (int i = 0; i < blockSize; i++) {
                classroom.getBlocks()[startBlock + i] = course; // block is marked as occupied
                classroom.decreaseAvailability(blockSize);

                return true;
=======
            Schedules the chosen blocks of the given classroom's time schedule.
        */

        if(checkBlocksAvailability(startBlock, blockSize, classroom)){
            for(int i = 0; i < blockSize; i++){
                classroom.getBlocks()[startBlock+i] = course; // block is marked as occupied
>>>>>>> Stashed changes
            }
            classroom.decreaseAvailability(blockSize);
            return true;
        }
        // in case the blocks are not available
        return false;
    }

    public static boolean checkBlocksAvailability(int startBlock, int blockSize, Classroom room) {
        /*
<<<<<<< Updated upstream
         * Checks whether the targeted block(s) is/are available to be assigned to an
         * exam.
         */
=======
            Checks whether the targeted block(s) is/are available to be assigned to an exam.
        */

        if(startBlock + blockSize > room.getBlocks().length){ // Check if blocks exceed array bounds
            return false;
        }
        
>>>>>>> Stashed changes
        for (int i = 0; i < blockSize; i++) {
            if (!(room.getBlocks()[startBlock + i] == null)) { // if the chosen block is not null
                return false; // can not schedule
            }
        }
        return true; // all blocks empty
    }

    public static void sortCourses(ArrayList<Course> courses) {
        /*
<<<<<<< Updated upstream
         * Sorts the exam list according to each exam's duration.
         * longer exams have HIGHER priority.
         */
        for (int i = 0; i < courses.size() - 1; i++) {
=======
            Sorts the exam list according to each exam's duration.
            Longer exams have HIGHER priority.
        */

        for (int i = 0; i < courses.size()-1; i++) {
>>>>>>> Stashed changes
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

    public static void sortClasses(ArrayList<Classroom> classrooms) {
        /*
<<<<<<< Updated upstream
         * Sorts the classes list according to each class's capacity.
         * Classes with higher capacity have HIGHER priority.
         * Higher (0) -----------------> Lower (n) (array with length n)
         */
        for (int i = 0; i < classrooms.size() - 1; i++) {
=======
            Sorts the classes list according to each class's capacity.
            Classes with higher capacity have HIGHER priority.
            Higher (0) -----------------> Lower (n)         (array with length n)
        */

        for (int i = 0; i < classrooms.size()-1; i++) {
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
    public static Classroom bestFittingClass(Course course, ArrayList<Classroom> classrooms) {
        // the smallest capacity that is greater than or equal to the exam's required
        // capacity
        sortClasses(classrooms);
        int chosenCapacity = classrooms.get(0).getCapacity();
        int chosenIndex = 0;
        // boolean
        for (int i = 0; i < classrooms.size(); i++) {
            if (classrooms.get(i).getCapacity() >= course.getAttendees().length
                    && classrooms.get(i).getCapacity() < chosenCapacity) {
                chosenCapacity = classrooms.get(i).getCapacity();
                chosenIndex = i;
            }
        }
        if (classrooms.get(chosenIndex).getCapacity() >= course.getAttendees().length) { // if the fit is proper
            return classrooms.get(chosenIndex);
        }
        return null;
    }

    public static int findAvailableBlocks(Course course, ArrayList<Classroom> classrooms) {
        boolean match;
        int blockSize = calculateBlock(course.getExamDuration());
        for (int i = 0; i < 24; i++) {
            match = true;
            for (Classroom classroom : classrooms) {
                if (!checkBlocksAvailability(i, blockSize, classroom)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return i;
            }
=======
    public static Classroom bestFittingClass(Course course, ArrayList<Classroom> classrooms){
        /*
            This method finds the smallest capacity that is greater than or equal to the exam's required capacity to minimize space waste.
            Note: assumes classrooms are ALREADY SORTED by capacity
        */
        
        int requiredCapacity = course.getAttendees().length;
        int requiredBlocks = calculateBlock(course.getExamDuration());
        int chosenCapacity = Integer.MAX_VALUE;
        Classroom bestFit = null;
        
        for(int i = 0; i < classrooms.size(); i++){ 
            Classroom classroom = classrooms.get(i);
            // Check if classroom has enough capacity AND enough available hours
            if(classroom.getCapacity() >= requiredCapacity && classroom.availability >= requiredBlocks){
                // Choose the smallest capacity that still fits
                if(classroom.getCapacity() < chosenCapacity){
                    chosenCapacity = classroom.getCapacity();
                    bestFit = classroom;
                }
            }
>>>>>>> Stashed changes
        }
        return bestFit;
    }

<<<<<<< Updated upstream
    public static ArrayList<Integer> findAllFreeBlocks(Classroom classroom) {
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            if (classroom.getBlocks()[i] == null) {
                result.add(i);
            }
        }
        return result;
    }

    // TODO
    public static boolean findMultipleClasses(Course course, ArrayList<Classroom> classrooms) {
        // it must choose classrooms that can fit all the students and then look for a
        // time when all are available. This is easily done if they fit in a single
        // class.
        int blockSize = calculateBlock(course.getExamDuration());
        ArrayList<Integer> availableBlocks = findAllFreeBlocks(classrooms.get(0)); // initial class
        int overlap = course.getAttendees().length;
        ArrayList<Classroom> result = new ArrayList<>();

        for (Classroom classroom : classrooms) {
            availableBlocks = findAllFreeBlocks(classroom); // getting class's all free blocks
            for (Integer block : availableBlocks) {
                if (checkBlocksAvailability(block, blockSize, classroom)) { // if the other class is available at any
                                                                            // same hour available
                    overlap -= classroom.getCapacity();
=======
    public static boolean findMultipleClasses(Course course, ArrayList<Classroom> classrooms){
        /*
            This method is used when the exam requires multiple classes to fit all of its attendees.
        */
>>>>>>> Stashed changes

        int blockSize = calculateBlock(course.getExamDuration());
        int totalCapacityNeeded = course.getAttendees().length;
        
        for(int startBlock = 0; startBlock < blocksPerDay; startBlock++){ // for each possible time block
            ArrayList<Classroom> availableClassrooms = new ArrayList<>();
            int totalCapacity = 0;
            
            // Check which classrooms are available at this time block
            for(Classroom classroom : classrooms){
                if(checkBlocksAvailability(startBlock, blockSize, classroom)){
                    availableClassrooms.add(classroom);
                    totalCapacity += classroom.getCapacity();
                }
            }
            
            // If we have enough combined capacity at this time block
            if(totalCapacity >= totalCapacityNeeded){
                // Schedule all the needed classrooms at the same time
                ArrayList<Classroom> result = new ArrayList<>();
                int capacityFilled = 0;
                
                for(Classroom classroom : availableClassrooms){
                    scheduleClass(startBlock, blockSize, classroom, course);
                    result.add(classroom);
<<<<<<< Updated upstream
                    if (overlap <= 0) {
=======
                    capacityFilled += classroom.getCapacity();
                    
                    if(capacityFilled >= totalCapacityNeeded){
>>>>>>> Stashed changes
                        course.setExamClass(result);
                        course.setTimeOfExam(startBlock);
                        return true;
                    }
                }
            }
        }
<<<<<<< Updated upstream
        // for(Integer block : availableBlocks){
        // for(Classroom classroom : classrooms){
        // if(checkBlocksAvailability(block, blockSize, classroom)){ // if the other
        // class is available at any same hour available
        // overlap -= classroom.getCapacity();
        // result.add(classroom);
        // }
        // if(overlap <= 0) return result;
        // }
        // }
=======
        return false;
    }

    public static boolean findClassForExam(Course course, ArrayList<Classroom> classrooms){
        /*
            This is the main method for scheduling an exam's classroom and hour
        */
        
        int examDuration = calculateBlock(course.getExamDuration());
        Classroom bestfit = bestFittingClass(course, classrooms); // finding the best fitting classroom
        
        if(bestfit == null){
            // No single class fits, try multiple classes
            return findMultipleClasses(course, classrooms);
        }
        else{
            for(int i = 0; i < blocksPerDay; i++){ // checking hour availability
                if(scheduleClass(i, examDuration, bestfit, course)){
                    ArrayList<Classroom> examClass = new ArrayList<>(); // since the method accepts an arraylist 
                    examClass.add(bestfit);
                    course.setExamClass(examClass);
                    course.setTimeOfExam(i);
                    return true; // Successfully scheduled
                }
            }
        }
        // no hour available
        System.out.println("No single class has enough capacity for this exam.\nTrying to find multiple classes...");
        return findMultipleClasses(course, classrooms);
    }

    public static boolean allClassHoursFilled(ArrayList<Classroom> classrooms){
        /* 
            This method will be used to make sure all classes are filled for the day so it can move onto the next one in the main loop.
        */

        for(Classroom classroom : classrooms){
            int freeBlocks = 0;
            for(int i = 0; i < blocksPerDay; i++){
                if(classroom.getBlocks()[i] == null){
                    freeBlocks++;
                }
            }
            if(freeBlocks > 2){
                return false; // not filed enough yet
            }
        }
        return true;  
    }

    // TODO
    public static boolean allExamsAreScheduled(ArrayList<Course> courses){
        /*
            This method checks whether all exam's are scheduled or not. 
            Returns true if all are scheduled, false if there is even a single unscheduled exam.
        */
        // This class must check and see whether all course exam's are scheduled or not. 
        // If there is even a single course with an unscheduled exam, false must be returned.
>>>>>>> Stashed changes
        return false;
    }

    // TODO
<<<<<<< Updated upstream
    public static boolean findClassForExam(Course course, ArrayList<Classroom> classrooms) {
        boolean done = false;
        int examDuration = calculateBlock(course.getExamDuration());
        Classroom bestfit = bestFittingClass(course, classrooms); // finding the best fitting classroom
        if (bestfit == null) {
            // findMultipleClasses(course, classrooms);
            // ------------------------------------------------------------------------------------>
            // UNCOMMENT AFTER MAKING THE METHOD
        } else {
            for (int i = 0; i < 24; i++) { // checking hour availability
                if (scheduleClass(i, examDuration, bestfit, course)) {
                    ArrayList<Classroom> examClass = new ArrayList<>();
                    examClass.add(bestfit);
                    course.setExamClass(examClass);
                    done = true;
                }
            }
        }
        if (!done) { // no hour available
            System.out.println(
                    "A single class does not have enough capacity for this exam.\nTrying to find multiple ones...");
            // findMultipleClasses(course, classrooms);
            // ------------------------------------------------------------------------------------>
            // UNCOMMENT AFTER MAKING THE METHOD
            return false;
        }
        return true;
    }

    // TODO
    public static void nextDay(ArrayList<Course> courses, ArrayList<Classroom> classrooms) {
=======
    public static void nextDay(ArrayList<Classroom> classrooms){
>>>>>>> Stashed changes
        /*
         * This method is used to reset all the classes's occupation, preparing it for
         * the next day's exam calculation.
         */
        // MUST RESET CLASSROOMS HOUR BLOCKS AND ALSO REMOVE OR DISABLE THE ALREADY DONE
        // EXAMS (using the "alreadyScheduled" flag in each course object)
        for (Classroom room : classrooms) {
            Course[] blocks = room.getBlocks();
            for (int i = 0; i < blocks.length; i++) {
                blocks[i] = null;
            }
            room.allBlocksFilled = false;
            room.availability = 24;
        }
        for (Course c : courses) {
            if (c.alreadyScheduled) {
                c.alreadyScheduled = false;
            }
            if (c.getExamClass() != null) {
                c.getExamClass().clear();
            }
        }
    }

<<<<<<< Updated upstream
    // TODO
    public static boolean allClassHoursFilled(ArrayList<Classroom> classrooms) {
        // MUST LOOP OVER ALL CLASSES'S HOURS TO MAKE SURE ALL CLASSES ARE FULLY
        // UTILIZED (SOME SMALL BLOCKS (1-2) CAN BE FREE)
        // this method will be used to make sure all classes are filled for the day so
        // it can move onto the next one in the main loop.
        for (Classroom classroom : classrooms) {
            int freeBlocks = 0;
            for (int i = 0; i < 24; i++) {
                if (classroom.getBlocks()[i] == null) {
                    freeBlocks++;
                }
            }
            if (freeBlocks > 2) {
                return false; // not filed enough yet
            }
        }
        return true;

    }

    public static void calculate(ArrayList<Classroom> classrooms, ArrayList<Course> courses,
            ArrayList<Student> students) {
=======
    public static void calculate(ArrayList<Classroom> classrooms, ArrayList<Course> courses, ArrayList<Student> students){
        /*
            This method contains the main loop and the calls to all of the scheduling methods.
        */
        
        // sorting all lists
>>>>>>> Stashed changes
        sortCourses(courses);
        sortClasses(classrooms);

        // The amount of days 
        ArrayList<ArrayList<Course>> days = new ArrayList<ArrayList<Course>>(); // a list of courses in a list of days

<<<<<<< Updated upstream
        // MAINLOOP (NEEDS TO BE FIXED)
        for (int i = 0; i < 7; i++) { // LOOPING FOR EACH DAY
            allClassesFilled = false; // reset for new day
            while (!allClassesFilled) { // while classes are available
                ArrayList<Course> dayCourses = new ArrayList<>();
                for (Course course : courses) {
                    if (findClassForExam(course, classrooms)) {
                        dayCourses.add(course);
=======

        // MAINLOOP
        while(!allExamsAreScheduled(courses)){

            ArrayList<Course> dayCourses = new ArrayList<>();
            while(!allClassHoursFilled(classrooms) && !allExamsAreScheduled(courses)){ // Try to schedule courses until the day is full

                boolean scheduledInThisPass = false;
                for(Course course : courses){
                    if(!course.alreadyScheduled){
                        if(findClassForExam(course, classrooms)){
                            dayCourses.add(course);
                            scheduledInThisPass = true;
                        }
>>>>>>> Stashed changes
                    }
                }


                if(!scheduledInThisPass){ // If nothing was scheduled in this pass, break to avoid infinite loop
                    break;
                }



            }

            if(dayCourses.size() > 0){ // Only add the day if something was scheduled
                days.add(dayCourses);
                nextDay(classrooms); // reset for the next day
            } else {
                // No exams were scheduled but not all are done - likely impossible to schedule remaining
                System.out.println("Warning: Unable to schedule remaining exams. Breaking out of scheduling loop.");
                break;
            }
<<<<<<< Updated upstream
            nextDay(courses, classrooms);
        }

        // TODO
=======
        }
        
        
        // output
        String s = days.size() > 1 ? "s" : "";
        System.out.println("All exams are scheduled.\nIt takes the total of " + days.size() + " day" + s + " for all the exams to end.");
>>>>>>> Stashed changes

    }

    public static void main(String[] args) {
        GUI.main(args);
    }

}
