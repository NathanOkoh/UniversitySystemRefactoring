public class TranscriptService extends PaymentService {
    public String universityName = "Metro University";

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
}
