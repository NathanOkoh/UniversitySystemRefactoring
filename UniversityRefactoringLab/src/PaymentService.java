import java.util.List;

public class PaymentService  {
    private List<Student> students;
    private List<PaymentRecord> payments;

    public PaymentService(List<Student> students, List<PaymentRecord> payments) {
        this.students = students;
        this.payments = payments;
    }

    private boolean isValidEmail(String email) {
        return email != null && email.contains("@");
    }

    private Student findStudent(String id) {
        for (Student student : students) {
            if (student.getId().equals(id)) return student;
        }
        return null;
    }

    public String processPayment(String studentId, double amount, String method) {
        Student student = null;

        student = findStudent(studentId);

        if (student == null) {
            return "Student not found";
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
}
