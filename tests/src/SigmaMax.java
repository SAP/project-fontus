public class SigmaMax {

    static int sigma(int n) {
        int summe = 0;
        for (int i = 1; i <= n / 2; i++) {
            if (n % i == 0) {
                summe += i;
            }
        }
        return summe + n;
    }

    public static void main(String[] args) {

        int index = 1,
            max = sigma(1);

        for (int i = 2; i <= 999; i++) {
            int t = sigma(i);

            if (max <= t) {
                index = i;
                max = t;
            }
        }
        System.out.println("Maximum:  " + index + " " + max);
        System.out.println("Beispiel:  12" + "   " + sigma(12));

    }

}
