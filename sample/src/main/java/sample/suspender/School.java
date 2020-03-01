package sample.suspender;

import java.util.ArrayList;
import java.util.List;

/**
 * A normal java class
 */
public class School {

    private List<Student> students = new ArrayList<>();

    /**
     * Adds a student, does some long running process before adding
     */
    public void addStudent(Student student) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("addStudent running on " + Thread.currentThread());
        students.add(student);
    }

    /**
     * Adds a student, does some long running process before adding
     */
    public List<Student> getStudents () {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("getStudents running on " + Thread.currentThread());
        return students;
    }
}
