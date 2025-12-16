package com.example;

import java.util.ArrayList;

public class Classroom {
    int name;
    int capacity;
    ArrayList<Integer> hours;

    public Classroom(int name, int capacity, ArrayList<Integer> hours) {
        this.name = name;
        this.capacity = capacity;
        this.hours = new ArrayList<>();
        for(int i = 0; i < hours.size(); i++){
            this.hours.add(0);
        }
    }

    public int getName() {
        return name;
    }

    public void setName(int name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public ArrayList<Integer> getHours() {
        return hours;
    }

    public void setHours(ArrayList<Integer> hours) {
        this.hours = hours;
    }
}
