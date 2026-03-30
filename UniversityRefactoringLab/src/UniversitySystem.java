import java.util.ArrayList;
import java.util.List;

public class UniversitySystem{

    public List<Student> students = new ArrayList<>();
    public List<Course> courses = new ArrayList<>();
    public List<Enrollment> enrollments = new ArrayList<>();
    public List<PaymentRecord> payments = new ArrayList<>();
    public List<String> logs = new ArrayList<>();
    public List<Instructor> instructors = new ArrayList<>();


    public double localRate = 300;
    public double internationalRate = 550;
    public double scholarshipRate = 100;

    // refactored enrollStudent
    private EnrollmentService enrollmentService;

    public UniversitySystem() {
        enrollmentService = new EnrollmentService(students, courses, enrollments, logs);
    }
    public String enrollStudent(String studentId, String courseCode, String semester, String paymentType) {
        return enrollmentService.enrollStudent(studentId, courseCode, semester, paymentType);
    }

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

    public String assignGrade(String studentId, String courseCode, String semester, String grade) {
        for (Enrollment e : enrollments) {
            if (e.studentId.equals(studentId) &&
                    e.courseCode.equals(courseCode) &&
                    e.semester.equals(semester)) {

                e.grade = grade;

                double points = 0;
                if (grade.equals("A")) points = 4.0;
                else if (grade.equals("B")) points = 3.0;
                else if (grade.equals("C")) points = 2.0;
                else if (grade.equals("D")) points = 1.0;
                else if (grade.equals("F")) points = 0.0;

                Student student = findStudent(studentId);
                Course course = findCourse(courseCode);

                if (student != null && course != null) {
                    student.totalCompletedCredits += course.creditHours;
                    student.totalGradePoints += points * course.creditHours;
                    student.gpa = student.totalGradePoints / student.totalCompletedCredits;

                    if (student.gpa < 2.0) {
                        student.status = "PROBATION";
                    } else if (student.gpa < 3.5) {
                        student.status = "GOOD";
                    } else {
                        student.status = "HONOR";
                    }

                    logs.add("Grade assigned to " + studentId + " for " + courseCode);

                    String emailMsg = isValidEmail(student.getEmail())
                            ? "Email sent to " + student.getEmail() + ": grade posted"
                            : "Could not send grade email";

                    return "Grade assigned\n"
                            + "Course: " + courseCode + "\n"
                            + "Grade: " + grade + "\n"
                            + "Updated GPA: " + student.gpa + "\n"
                            + "Updated Status: " + student.status + "\n"
                            + emailMsg;
                }
            }
        }

        return "Enrollment not found for given student/course/semester";
    }
    // refactored processPayment
    private PaymentService paymentService;

    public String processPayment(String studentId, double amount, String method) {
        return paymentService.processPayment(studentId, amount, method);
    }

    // refactored printTranscript and printCourseRoster into Transcript service
    private TranscriptService transcriptService;

    public String printTranscript(String studentId) {
        return transcriptService.printTranscript(studentId);
    }
    public String printCourseRoster(String courseCode) {
        return transcriptService.printCourseRoster(courseCode);
    }

    public void printDepartmentSummary(String department) {
        System.out.println("----- DEPARTMENT SUMMARY -----");
        System.out.println("Department: " + department);

        int studentCount = 0;
        int instructorCount = 0;
        int courseCount = 0;
        double avgGpa = 0;
        int gpaCount = 0;

        for (Student student : students) {
            if (student.department.equals(department)) {
                studentCount++;
                avgGpa += student.gpa;
                gpaCount++;
            }
        }

        for (Instructor i : instructors) {
            if (i.department.equals(department)) {
                instructorCount++;
            }
        }

        for (Course course : courses) {
            if (course.code.startsWith(department)) {
                courseCount++;
            }
        }

        if (gpaCount > 0) {
            avgGpa = avgGpa / gpaCount;
        }

        System.out.println("Students: " + studentCount);
        System.out.println("Instructors: " + instructorCount);
        System.out.println("Courses: " + courseCount);
        System.out.println("Average GPA: " + avgGpa);
    }

    public void sendWarningLetters() {
        for (Student student : students) {
            if (student.outstandingBalance > 500 || student.status.equals("PROBATION")) {
                if (isValidEmail(student.getEmail())) {
                    System.out.println("Sending warning email to " + student.email);
                    if (student.outstandingBalance > 500) {
                        System.out.println("Reason: unpaid balance");
                    }
                    if (student.status.equals("PROBATION")) {
                        System.out.println("Reason: academic probation");
                    }
                    logs.add("Warning sent to " + student.getId());
                } else {
                    System.out.println("Could not send warning to " + student.name);
                    logs.add("Warning failed for " + student.getId());
                }
            }
        }
    }

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

}