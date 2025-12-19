package main.java.com.example;


public class Classroom {
    int name;
    int capacity;
    Course blocks[];
    int availability = Main.blocksPerDay;
    boolean allBlocksFilled = false;


    public Classroom(int name, int capacity) {
        this.name = name;
        this.capacity = capacity;
        this.blocks =  new Course[availability]; 
                // an array with a fixed length, each representing the block hours. 
                // To determine which course each block is taken by, we hold a course object in each index.
                // a "null" value means unallocated block (empty).
        for(int i = 0; i < blocks.length; i++){
            this.blocks[i] = null;
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

    public Course[] getBlocks() {
        return blocks;
    }

    public void setHours(Course[] blocks) {
        this.blocks = blocks;
    }

    public void decreaseAvailability(int blocksTaken){
        this.availability -= blocksTaken;
        if(this.availability == 0) allBlocksFilled = true;
    }
}
