import java.util.ArrayList;
import java.util.List;

public class EnrollmentService {
    public List<Student> students = new ArrayList<>();
    public List<Course> courses = new ArrayList<>();
    public List<Enrollment> enrollments = new ArrayList<>();
    public List<String> logs = new ArrayList<>();

    public EnrollmentService(List<Student> students,
                             List<Course> courses,
                             List<Enrollment> enrollments,
                             List<String> logs) {
        this.students = students;
        this.courses = courses;
        this.enrollments = enrollments;
        this.logs = logs;
    }

    public EnrollmentService() {}
    // helper methods from UniversitySystem
    public Student findStudent(String id) {
        for (Student student : students) {
            if (student.getId().equals(id)) {
                return student;
            }
        }
        return null;
    }
    public Course findCourse(String code) {
        for (Course course : courses) {
            if (course.code.equals(code)) {
                return course;
            }
        }
        return null;
    }

    private boolean isValidEmail(String email) { return email != null && email.contains("@"); }

    private static double calculateFee(String courseCode, String semester, String paymentType, Student student, Course course) {
        double fee = 0;
        if (student.getType() == Student.StudentType.LOCAL) {
            fee = course.creditHours * 300;
        } else if (student.getType() == Student.StudentType.INTERNATIONAL) {
            fee = course.creditHours * 550;
        } else if (student.getType() == Student.StudentType.SCHOLARSHIP) {
            fee = course.creditHours * 100;
        } else {
            fee = course.creditHours * 300;
        }

        if (paymentType.equals("INSTALLMENT")) {
            fee = fee + 50;
        } else if (paymentType.equals("CARD")) {
            fee = fee + 10;
        } else if (paymentType.equals("CASH")) {
            fee = fee + 0;
        } else {
            fee = fee + 100;
        }

        if (semester.equals("SUMMER")) {
            fee = fee + 200;
        }

        if (courseCode.startsWith("SE")) {
            fee = fee + 75;
        }
        return fee;
    }

    public String enrollStudent(String studentId, String courseCode, String semester, String paymentType) {
        Student student = null;
        Course course = null;

        student = findStudent(studentId);
        course = findCourse(courseCode);

        if (student == null) {
            logs.add("Student not found: " + studentId);
            return "Student not found";
        }

        if (course == null) {
            logs.add("Course not found: " + courseCode);
            return "Course not found";
        }

        if (student.isBlocked) {
            logs.add("Blocked student tried enrollment");
            return "Student is blocked";
        }

        if (student.status.equals("PROBATION")) {
            int count = 0;
            for (Enrollment e : enrollments) {
                if (e.studentId.equals(studentId) && e.semester.equals(semester)) {
                    count++;
                }
            }
            if (count >= 2) {
                logs.add("Probation limit reached");
                return "Probation student cannot register more than 2 courses";
            }
        }

        if (course.enrolled >= course.capacity) {
            logs.add("Course full: " + courseCode);
            return "Course is full";
        }

        if (student.outstandingBalance > 1000) {
            logs.add("Balance issue for " + student.getId());
            return "Student has unpaid balance";
        }

        for (Enrollment e : enrollments) {
            if (e.studentId.equals(studentId) && e.semester.equals(semester)) {
                if (e.day.equals(course.day) && e.timeSlot.equals(course.timeSlot)) {
                    logs.add("Conflict for " + studentId);
                    return "Schedule conflict";
                }
            }
        }

        if (course.prerequisite != null && !course.prerequisite.equals("")) {
            boolean passed = false;
            for (Enrollment e : enrollments) {
                if (e.studentId.equals(studentId) && e.courseCode.equals(course.prerequisite)) {
                    if (e.grade != null && (e.grade.equals("A") || e.grade.equals("B") || e.grade.equals("C"))) {
                        passed = true;
                    }
                }
            }
            if (!passed) {
                logs.add("Missing prerequisite for " + studentId);
                return "Missing prerequisite";
            }
        }

        double fee = calculateFee(courseCode, semester, paymentType, student, course);

        student.outstandingBalance = student.outstandingBalance + fee;
        Enrollment newEnrollment = new Enrollment(studentId, courseCode, semester, course.day, course.timeSlot);
        enrollments.add(newEnrollment);
        course.enrolled++;

        if (isValidEmail(student.getEmail())) {
            System.out.println("Email sent to " + student.email + ": enrolled in " + course.title);
            logs.add("Enrollment email sent");
        } else {
            System.out.println("Invalid email");
            logs.add("Invalid email for " + student.getId());
        }

        logs.add("Enrolled " + studentId + " into " + courseCode);

        String emailMsg = isValidEmail(student.email)
                ? "Email sent to " + student.email
                : "Invalid email";

        return "Enrollment completed\n"
                + "Student: " + student.name + "\n"
                + "Course: " + course.title + "\n"
                + "Semester: " + semester + "\n"
                + "Fee charged: " + fee + "\n"
                + emailMsg;

    }
}
