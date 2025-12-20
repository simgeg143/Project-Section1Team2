package com.example;

import java.util.ArrayList;

public class Student {
    int ID;
    ArrayList<Integer> dailyAmountOfExams = new ArrayList<>();
    int currentDayExams = 0;
    int firstExamTime;
    int firstExamDuration;

    public Student(int ID) {
        this.ID = ID;
    }
    public int getID() {
        return this.ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }


    public void setFirstExamTime(int timeInMinutes){
        this.firstExamTime = timeInMinutes;
    }
    public int getFirstExamTime(){
        return firstExamTime;
    }
    

    public void setFirstExamDuration(int timeInMinutes){
        this.firstExamDuration = timeInMinutes;
    }
    public int getFirstExamDuration(){
        return this.firstExamDuration;
    }

    public void CurrentDayExamCount(){
        this.currentDayExams++;
    }
    public int getCurrentDayExams(){
        return this.currentDayExams;
    }

    public void endOfDay(){
        dailyAmountOfExams.add(currentDayExams);
        this.currentDayExams = 0;
        this.firstExamTime = 0; // Reset for next day
        this.firstExamDuration = 0; // Reset for next day
    }

    public ArrayList<Integer> getDailyExamCounts() {
        return this.dailyAmountOfExams;
    }

    public boolean exceedsDailyLimit() {
        return this.currentDayExams > 2;
    }

    @Override
    public String toString() {
        return "Student{" +
                "ID=" + ID +
                ", currentDayExams=" + currentDayExams +
                ", totalDailyExams=" + dailyAmountOfExams +
                '}';
    }

}
