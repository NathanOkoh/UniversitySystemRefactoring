public class Student {
    public enum StudentType {
        LOCAL, INTERNATIONAL, SCHOLARSHIP
    }
    public String id;
    public String name;
    public String email;
    public String department;
    public StudentType type; // change variable to student type because it's used only for studentypes
    public String status;
    public boolean isBlocked;
    public double outstandingBalance;
    public int totalCompletedCredits;
    public double totalGradePoints;
    public double gpa;

    public Student(String id, String name, String email, String department, StudentType type) { // updated to studenttype in constructor
        this.id = id;
        this.name = name;
        this.email = email;
        this.department = department;
        this.type = type;
        this.status = "GOOD";
        this.isBlocked = false;
        this.outstandingBalance = 0;
        this.totalCompletedCredits = 0;
        this.totalGradePoints = 0;
        this.gpa = 0;
    }

    public void setId(String id) { this.id = id; }

    public StudentType getType() {
        return type;
    }
    public String getId() { return id; }
    public String getEmail() { return email; }




}
