public class Classroom {
    int name;
    int capacity;
    int[] hours;

    public Classroom(int name, int capacity, int[] hours) {
        this.name = name;
        this.capacity = capacity;
        this.hours = new int[24];
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

    public int[] getHours() {
        return hours;
    }

    public void setHours(int[] hours) {
        this.hours = hours;
    }
}
