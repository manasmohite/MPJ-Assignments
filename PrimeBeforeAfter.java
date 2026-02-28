import java.util.Scanner;

public class PrimeBeforeAfter {

    // Method to check prime
    public static boolean isPrime(int num) {
        if (num <= 1)
            return false;

        for (int i = 2; i < num; i++) {   // simple loop
            if (num % i == 0)
                return false;
        }
        return true;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter a number: ");
        int number = sc.nextInt();

        int count = 0;
        int temp = number - 1;

        System.out.println("5 Prime Numbers Before " + number + ":");
        while (temp > 1 && count < 5) {
            if (isPrime(temp)) {
                System.out.print(temp + " ");
                count++;
            }
            temp--;
        }

        count = 0;
        temp = number + 1;

        System.out.println("\n5 Prime Numbers After " + number + ":");
        while (count < 5) {
            if (isPrime(temp)) {
                System.out.print(temp + " ");
                count++;
            }
            temp++;
        }

        sc.close();
    }
}