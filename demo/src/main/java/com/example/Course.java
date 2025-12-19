package com.example;

import java.util.ArrayList;

public class Course {
    int code;
    public boolean alreadyScheduled = false;
    Student[] attendees;
    ArrayList<Classroom> examClass;
    int examDuration; // in minutes
    String timeOfExam; // The scheduled time (add 9*60 minutes to find the exam hour in minutes, then
                       // divide it by 60 to find hour and minute.)
    int examHour, examMinute; // in case the exam hour and date are needed as integers.
    int timeOfExamInMinutes;
    int examDay = -1;
    int examStartBlock;

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
        // alreadyScheduled flag is set in Main.java scheduling methods
    }

    public void setTimeOfExam(int startBlock) {
        /*
         * This method calculates the hour and the minute the exam starts.
         */
        int time = startBlock * Main.eachBlockDuration;
        time += 9 * 60;
        setTimeOfExamInMinutes(time);
        this.examHour = time / 60;
        this.examMinute = time % 60;
        this.examStartBlock = startBlock;
        this.timeOfExam = String.format("%02d:%02d", this.examHour, this.examMinute);
        calculateEndOfExam();
        for (Student attendee : attendees) {
            if (attendee.currentDayExams == 0) {
                attendee.setFirstExamTime(time);
                attendee.setFirstExamDuration(this.examDuration);
            }
        }
    }

    private void setTimeOfExamInMinutes(int time) {
        this.timeOfExamInMinutes = time;
    }

    private void calculateEndOfExam() {
        /*
         * This method calculates the hour and the minute the exam finishes.
         */
        int total = this.examHour * 60 + this.examMinute + examDuration;
        this.endOfExamHour = total / 60;
        this.endOfExamMinute = total % 60;
        endOfExam = String.format("%02d:%02d", this.endOfExamHour, this.endOfExamMinute);
    }

    public int getTimeOfExamInMinutes() {
        return this.timeOfExamInMinutes;
    }

    public String getEndOfExam() {
        return endOfExam;
    }

    public int getExamHour() {
        return this.examHour;
    }

    public int getExamMinute() {
        return this.examMinute;
    }

    public String getTimeOfExam() {
        return this.timeOfExam;
    }

    public int getExamDay() {
        return examDay;
    }

    public void setExamDay(int examDay) {
        this.examDay = examDay;
    }

    public void setExamStartBlock(int block) {
        this.examStartBlock = block;
    }

    public int getExamStartBlock() {
        return this.examStartBlock;
    }

}
