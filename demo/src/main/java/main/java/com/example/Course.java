package main.java.com.example;

import java.util.ArrayList;

public class Course {
    int code;
    public boolean alreadyScheduled = false;
    Student[] attendees;
    ArrayList<Classroom> examClass;
    int examDuration; // in minutes
    String timeOfExam; // The scheduled time (add 9*60 minutes to find the exam hour in minutes, then divide it by 60 to find hour and minute.)
    int examHour, examMinute; // in case the exam hour and date are needed as integers.

    String endOfExam; // the time exam finished
    int endOfExamHour, endOfExamMinute; // the hour and minute exam finishes.

    public Course(int code, Student[] attendees, ArrayList<Classroom> examClass, int examDuration) {
        this.code = code;
        this.attendees = attendees;
        this.examClass = examClass;
        this.examDuration = examDuration;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Student[] getAttendees() {
        return attendees;
    }

    public void setAttendees(Student[] attendees) {
        this.attendees = attendees;
    }

    public ArrayList<Classroom> getExamClass() {
        return examClass;
    }


    public int getExamDuration() {
        return examDuration;
    }

    public void setExamDuration(int examDuration) {
        this.examDuration = examDuration;
    }

    public void setExamClass(ArrayList<Classroom> examClass) {
        this.examClass = examClass; // the class in which the exam will be held
        alreadyScheduled = true; // the exam is scheduled 
    }

    public void setTimeOfExam(int startBlock){
        /*
            This method calculates the hour and the minute the exam starts.
        */
        int time = startBlock * Main.eachBlockDuration;
        time += 9 * 60;
        this.examHour = time / 60;
        this.examMinute = time % 60;
        this.timeOfExam = Integer.toString(this.examHour) + ":" + Integer.toString(this.examMinute);
        calculateEndOfExam();
    }

    private void calculateEndOfExam(){
        /*
            This method calculates the hour and the minute the exam finishes.
        */
        int total = this.examHour * 60 + this.examMinute + examDuration;
        this.endOfExamHour = total / 60;
        this.endOfExamMinute = total % 60;
        endOfExam = Integer.toString(this.endOfExamHour) + ":" + Integer.toString(this.endOfExamMinute);
    }

    public String getEndOfExam(){
        return endOfExam;
    }

    public int getExamHour(){
        return this.examHour;
    }
    public int getExamMinute(){
        return this.examMinute;
    }
    public String getTimeOfExam(){
        return this.timeOfExam;
    }
}
