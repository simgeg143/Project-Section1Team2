package com.example;


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

    
    public static void scheduleClass(int startBlock, int blockSize, Classroom classroom){
        /*
            Schedules the chosen blocks of the given classroom's time schedule.
        */
        for(int i = 0; i < blockSize; i++){
            classroom.getHours().set(startBlock+i,1); // block is marked as occupied
        }
    }

    public static boolean checkBlocksAvailability(int startBlock, int blockSize, Classroom room) {
        /*
            Checks whether the targeted block(s) is/are available to be assigned to an exam.
        */
        for (int i = 0; i < blockSize; i++) {
            if (room.getHours().get(startBlock + i) == 1){
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
        for(int i = 0; i < classrooms.size(); i++){
            if(classrooms.get(i).getCapacity() >= course.getAttendees().length && classrooms.get(i).getCapacity() < chosenCapacity){
                chosenCapacity = classrooms.get(i).getCapacity();
                chosenIndex = i;
            }
        }
        return classrooms.get(chosenIndex);
    }

    public static int findAvailableBlocks(Course course, ArrayList<Classroom> classrooms){
        boolean match;
        for(int i = 0; i < 24; i++){
            match = true;
            for(Classroom classroom: classrooms){
                if(!checkBlocksAvailability(i, calculateBlock(course.getExamDuration()), classroom)){
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

    
    // TODO
    // it must choose classrooms that can fit all the students and then look for a time when all are available. This is easily done if they fit in a single class.
    // public static ArrayList<Classroom> findMultipleClasses(int studentsOverlapping, Course course, ArrayList<Classroom> classrooms){ // UNFINISHED. MAY NEED TO USE RECURSION
    //     while(studentsOverlapping > classrooms.get(index)){
    //         return findMultipleClasses(startingBlock, examDuraiton)
    //     }
    // }


    // TODO
    // public static int findClassForExam(Course course, ArrayList<Classroom> classrooms){
    //     boolean done = false;
    //     int examDuration = calculateBlock(course.getExamDuration());
    //     for(int i = 0; i < classrooms.size(); i++){
    //         // try to find the bestFittingClass();
    //         // if it's null then findMultipleClasses();
    //         if(course.getAttendees().length > classrooms.get(i).getCapacity()){ // if they fit
    //             for(int j = 0; j < 24; j++){ // for each 30 minute block
    //                 if(checkBlocksAvailability(j, examDuration, classrooms.get(i))){
    //                     scheduleClass(j, examDuration, classrooms.get(i));
    //                     done = true;
    //                 }
    //             }
    //         }
    //     }
    //     if(!done){
    //     }
    //     return 0;
    // }





    public static void calculate(ArrayList<Classroom> classrooms, ArrayList<Course> courses, ArrayList<Student> students){
        sortCourses(courses);
        boolean allClassesFilled = false;

        // TODO

    }



    public static void main(String[] args) {
        GUI.main(args);
    }

}
