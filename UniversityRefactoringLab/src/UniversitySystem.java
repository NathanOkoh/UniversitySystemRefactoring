import java.util.ArrayList;
import java.util.List;

public class UniversitySystem {

    public List<Student> students = new ArrayList<>();
    public List<Course> courses = new ArrayList<>();
    public List<Enrollment> enrollments = new ArrayList<>();
    public List<Instructor> instructors = new ArrayList<>();
    public List<PaymentRecord> payments = new ArrayList<>();
    public List<String> logs = new ArrayList<>();

    public String universityName = "Metro University";
    public double localRate = 300;
    public double internationalRate = 550;
    public double scholarshipRate = 100;

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

    private static double calculateFee(String courseCode, String semester, String paymentType, Student student, Course course) {
        double fee = 0;
        if (student.type.equals("LOCAL")) {
            fee = course.creditHours * 300;
        } else if (student.type.equals("INTERNATIONAL")) {
            fee = course.creditHours * 550;
        } else if (student.type.equals("SCHOLARSHIP")) {
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

    public String processPayment(String studentId, double amount, String method) {
        Student student = null;

        student = findStudent(studentId);

        if (student == null) {
            return  "Student not found";
        }

        if (amount <= 0) {
            return "Invalid payment amount";
        }

        if (method.equals("CARD")) {
            amount = amount - 5;
        } else if (method.equals("BANK")) {
            amount = amount - 2;
        } else if (method.equals("CASH")) {
            amount = amount;
        } else {
            amount = amount - 10;
        }

        student.outstandingBalance = student.outstandingBalance - amount;
        if (student.outstandingBalance < 0) {
            student.outstandingBalance = 0;
        }

        payments.add(new PaymentRecord(studentId, amount, method, "PAID"));

        // Email message (no printing)
        String emailMsg = isValidEmail(student.getEmail())
                ? "Email sent to " + student.getEmail() + ": payment received"
                : "Could not send payment email";

        return "Payment processed for " + student.name +
                "\nMethod: " + method +
                "\nAmount accepted: " + amount +
                "\nRemaining balance: " + student.outstandingBalance + "\n"
                + emailMsg;
    }

public String printTranscript(String studentId) {
    Student student = findStudent(studentId);

    if (student == null) {
        return "Student not found";
    }

    StringBuilder output = new StringBuilder();

    output.append("----- TRANSCRIPT -----\n");
    output.append("University: ").append(universityName).append("\n");
    output.append("Name: ").append(student.name).append("\n");
    output.append("ID: ").append(student.id).append("\n");
    output.append("Department: ").append(student.department).append("\n");
    output.append("Status: ").append(student.status).append("\n");
    output.append("GPA: ").append(student.gpa).append("\n");

    for (Enrollment e : enrollments) {
        if (e.studentId.equals(studentId)) {
            String title = "";
            int credits = 0;

            for (Course course : courses) {
                if (course.code.equals(e.courseCode)) {
                    title = course.title;
                    credits = course.creditHours;
                }
            }

            output.append(e.courseCode)
                    .append(" - ")
                    .append(title)
                    .append(" - ")
                    .append(credits)
                    .append(" credits - Grade: ")
                    .append(e.grade)
                    .append("\n");
        }
    }

    output.append("Outstanding Balance: ").append(student.outstandingBalance).append("\n");

    if (student.outstandingBalance > 0) {
        output.append("WARNING: unpaid dues\n");
    }

    return output.toString();
}

public String printCourseRoster(String courseCode) {
    StringBuilder output = new StringBuilder();

    output.append("----- COURSE ROSTER -----\n");

    for (Course c : courses) {
        if (c.code.equals(courseCode)) {
            output.append("Course: ").append(c.title).append("\n");
            output.append("Instructor: ").append(c.instructorName).append("\n");
            output.append("Capacity: ").append(c.capacity).append("\n");
            output.append("Enrolled: ").append(c.enrolled).append("\n");
        }
    }

    for (Enrollment e : enrollments) {
        if (e.courseCode.equals(courseCode)) {
            for (Student s : students) {
                if (s.id.equals(e.studentId)) {
                    output.append(s.id)
                            .append(" - ")
                            .append(s.name)
                            .append(" - ")
                            .append(s.status)
                            .append("\n");
                }
            }
        }
    }

    return output.toString();
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
