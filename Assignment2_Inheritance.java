import java.util.*;

class Employee {
    double salary;

    void displaySalary() {
        System.out.println("Salary: " + salary);
    }
}

class FullTime extends Employee {

    void calculateSalary(Scanner sc) {

        System.out.println("Full Time Employee");
        System.out.print("Enter salary: ");
        salary = sc.nextDouble();

        System.out.print("Salary before hike: ");
        displaySalary();

        salary = salary + (salary * 0.50);
        // salary = salary*1.5;
        
        System.out.print("Salary after 50% hike: ");
        displaySalary();
        System.out.println();
    }
}

class Intern extends Employee {
    
    void calculateSalary(Scanner sc) {
        
        System.out.println("Intern Employee");
        System.out.print("Enter salary: ");
        salary = sc.nextDouble();
        
        System.out.print("Salary before hike: ");
        displaySalary();
        
        salary = salary + (salary * 0.25);
        
        System.out.print("Salary after 25% hike: ");
        displaySalary();
        System.out.println();
    }
}

public class Assignment2_Inheritance {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        
        FullTime fulltimeworker = new FullTime();
        fulltimeworker.calculateSalary(sc);
        
        Intern internworker = new Intern();
        internworker.calculateSalary(sc);
        
        sc.close();
    }
}
