import java.util.Scanner;

public class QuadraticEquation {

    public static void main(String[] args) {

        Scanner sc = new Scanner("2.0 8.0 4.0");

        double a, b, c, p, q, p2, d, eps = 0.0001;

        a = sc.nextDouble();
        System.out.print("a = " + a + "\n");

        b = sc.nextDouble();
        System.out.print("b = " + b + "\n");

        c = sc.nextDouble();
        System.out.print("c = " + c + "\n");
        System.out.println();
        if (Math.abs(a) < eps) {
            System.out.println("Unzulässiger Wert von a!");
        } else {
            p  = b / a;
            q  = c / a;
            p2 = p / 2.0;
            d  = p2 * p2 - q;
            if (d < -eps) {
                System.out.printf("Keine Lösung!%n");
            } else if (Math.abs(d) < eps) {
                System.out.printf("Einzige Lösung: %10.5f%n", -p2);
            } else {
                double w = Math.sqrt(d);
                System.out.printf("1. Lösung: %10.5f%n", -p2 + w);
                System.out.printf("2. Lösung: %10.5f%n", -p2 - w);
            }
        }

    }

}
