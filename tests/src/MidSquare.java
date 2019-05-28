/**
 * Die main-Methode dieser Klasse Führt die MidSquare-Methode für
 * die Zahlen from bis to aus.
 * Ausgabe: Eine Zahl, die die meisten Zufallszahlen erzeugt,
 * die Grenzen und die erzeugten Zufallszahlen.
 * @author Werner Struckmann 1234567 Gruppe 02a
 * @version 1.0 vom 17. Oktober 1700
 */
public class MidSquare {

    /**
      * @param args wird nicht benutzt.
      */
    public static void main(String[] args) {

        int grenze = 9000;
        int    max =    0;
        int[]    a = new int[grenze];
        int from = 1000;
        int to = 9999;
        int welche = 0; // Eine Zahl, die die max. Anzahl erzeugt
        int anzahl = 1; // Anzahl der Zahlen, die die max. Anzahl erzeugen.
        // Anfangszahl zählt als Zufallszahl mit, optional.

        for (int i = from; i <= to; i++) {
            int zahl = i;
            for (int j : a) {
                j = -1;
            }
            xx:
            for (int j = 0; j < grenze; j++) {
                a[j] = zahl;
                zahl = ((zahl * zahl) / 100) % 10000;
                for (int k = 0; k <= j; k++) {
                    if (a[k] == zahl) {
                        if (j == max) {
                            ++anzahl;
                        }
                        if (j > max) {
                            max = j;
                            welche = i;
                            anzahl = 1;
                        }
                        break xx;
                    }
                }
            }
        }
        System.out.printf("%nGrenzen: Von %4d bis %4d.%n", from, to);
        if (anzahl == 1) {
            System.out.printf("%d ist die einzige Zahl, die %d Zufallszahlen erzeugt:%n%n",
                              welche, max + 1);
        } else {
            System.out.printf("Es gibt %d Zahlen, die %d Zufallszahlen erzeugen.%n", anzahl,
                              max + 1);
            System.out.printf("Eine Zahl ist %d:%n%n", welche);
        }
        int zahl = welche;
        int l = 0;
        for (int j = 0; j <= max + 1; j++) {
            System.out.printf("%4d ", zahl);
            l++;
            if (l == 20) {
                System.out.println();
                l = 0;
            }
            zahl = ((zahl * zahl) / 100) % 10000;
        }
        System.out.printf("%n%n");
    }

}
