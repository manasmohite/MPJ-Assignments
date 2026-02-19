import java.util.Scanner;

class Assignment1_StudentGrades {

    String name;
    int rollNo;
    int[] marks = new int[5];
    int average;
    char grade;

    static int[] subjectTotal = new int[5];
    static int studentCount = 0;

    void input(Scanner sc) {
        System.out.println("Enter Name:");
        name = sc.nextLine();

        System.out.println("Enter Roll No:");
        rollNo = sc.nextInt();

        System.out.println("Enter marks for 5 subjects (out of 100):");
        int sum = 0;
        for (int i = 0; i < 5; i++) {
            marks[i] = sc.nextInt();
            sum += marks[i];
            subjectTotal[i] += marks[i];
        }

        average = sum / 5;

        if (average >= 75)
            grade = 'A';
        else if (average >= 60)
            grade = 'B';
        else
            grade = 'C';

        studentCount++;
        sc.nextLine();
    }

    void display() {
        System.out.println("\nName: " + name);
        System.out.println("Roll No: " + rollNo);
        System.out.println("Average Marks: " + average);
        System.out.println("Grade: " + grade);
    }

    static void displaySubjectAverages() {
        System.out.println("\nAverage Marks for Each Subject:");
        for (int i = 0; i < 5; i++) {
            System.out.println("Subject " + (i + 1) + ": " + (subjectTotal[i] / studentCount));
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter number of students:");
        int n = sc.nextInt();
        sc.nextLine();

        Assignment1_StudentGrades[] s = new Assignment1_StudentGrades[n];

        for (int i = 0; i < n; i++) {
            s[i] = new Assignment1_StudentGrades();
            s[i].input(sc);
            s[i].display();
        }

        Assignment1_StudentGrades.displaySubjectAverages();
        sc.close();
    }
}
