public class Course {
    int code;
    Student[] attendees;
    Classroom[] examClass;

    public Course(int code, Student[] attendees, Classroom[] examClass) {
        this.code = code;
        this.attendees = attendees;
        this.examClass = examClass;
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

    public Classroom[] getExamClass() {
        return examClass;
    }

    public void setExamClass(Classroom[] examClass) {
        this.examClass = examClass;
    }
}
