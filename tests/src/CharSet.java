/**
 * Menge von char Elementen.
 */
public class CharSet {

    private char[] set;
    private int    size;

    /**
     * Initialisiert eine leere Menge.
     */
    public CharSet() { }

    /**
     * Initialisiert eine initial gefüllte Menge.
     * @param set Der initiale Inhalt der neuen Menge.
     */
    public CharSet(char[] set) {
        if (!check1(set)) {
            System.out.println("Mehrfaches Element!         ");
        }
        if (!check2(set)) {
            System.out.println("Keine Ziffer oder Buchstabe!");
        }
        size = set.length;
        this.set = set;
    }

    /**
     * Gibt den Inhalt der Menge zurück.
     * @return Die Buchstaben die in der Menge gespeichert sind.
     */
    public char[] getSet()         {
        return set;
    }

    /**
     * Gibt die Anzahl der Elemente der Menge zurück.
     * @return Wie viele Elemente sind in der Menge gespeichert?
     */
    public int    getSize()        {
        return size;
    }

    /**
     * Setzt den Inhalt der Menge auf die übergebenen Werte.
     * @param a Der neue Inhalt der Menge.
     */
    public void   setSet(char[] a) {
        set = a;
    }

    private boolean check1(char[] a) {
        for (int i = 0;   i < a.length; i++) {
            for (int j = i + 1; j < a.length; j++) {
                if (a[j] == a[i]) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean check2(char[] a) {
        for (int i = 0; i < a.length; i++) {
            int j = (int) a[i];
            if (!((48 <= j && j <= 57) || (65 <= j && j <= 90) || (97 <= j && j <= 122))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isIn(char[] x, char y) {
        for (int i = 0; i < x.length; i++) {
            if (x[i] == y) {
                return true;
            }
        }
        return false;
    }

    /**
     * Bildet den Durchschnitt zwischen zwei Mengen.
     * @param c Die Menge mit der der Durchschnitt gebildet werden soll.
     * @return Der Durchschnitt von this und c.
     */
    public CharSet durchschnitt(CharSet c) {
        int size = 0;
        CharSet d = new CharSet(c.getSet());
        char[] e = d.getSet();
        for (char x : e) {
            if (isIn(set, x)) {
                size++;
            }
        }
        char[] s = new char[size];
        int i = 0;
        for (i = 0; i < s.length;) {
            for (int j = 0; j < e.length; j++) {
                if (isIn(set, e[j])) {
                    s[i] = e[j];
                    i++;
                }
            }
        }
        return new CharSet(s);
    }

    /**
     * Bildet die Vereinigung zwischen zwei Mengen.
     * @param c Die Menge mit der die Vereinigung gebildet werden soll.
     * @return Die Vereinigung von this und c.
     */
    public CharSet vereinigung(CharSet c) {
        int size = set.length;
        CharSet d = new CharSet(c.getSet());
        char[] e = d.getSet();
        for (char x : e) {
            if (!isIn(set, x)) {
                size++;
            }
        }
        char[] s = new char[size];
        int i = 0;
        for (i = 0; i < set.length; i++) {
            s[i] = set[i];
        }
        for (int j = 0; j < e.length; j++) {
            if (!isIn(set, e[j])) {
                s[i] = e[j];
                i++;
            }
        }
        return new CharSet(s);
    }

    @Override
    public boolean equals(Object x) {
        if (x == null) {
            return false;
        }
        if (x.getClass() != getClass()) {
            return false;
        }
        CharSet other = (CharSet) x;
        if (other.getSet().length != set.length) {
            return false;
        }
        for (int i = 0; i < set.length; i++) {
            if (!isIn(set, other.getSet()[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Object clone() {
        return new CharSet(set);
    }

    @Override
    public String toString() {
        if (set.length == 0) {
            return "{}";
        }
        String s = "{";
        for (int i = 0; i < set.length - 1; i++) {
            s += "" + set[i] + ",";
        }
        return s + set[set.length - 1] + "}";
    }

    public static void main(String[] args) {
        String str1 = "{2,a,b,x}";
        String  x = str1.replace("{", "");
        x = x.replace(",", "");
        x = x.replace("}", "");

        String str2 = "{b,1,2,3,a}";
        String  y = str2.replace("{", "");
        y = y.replace(",", "");
        y = y.replace("}", "");

        String str3 = "{0,1,A,3,a}";
        String  z = str3.replace("{", "");
        z = z.replace(",", "");
        z = z.replace("}", "");

        char[]  a = x.toCharArray();
        char[]  b = y.toCharArray();
        char[]  c = z.toCharArray();

        CharSet k = new CharSet(a);
        System.out.println("1. Menge:  " + k + "  Size: " + k.getSize());
        CharSet l = new CharSet(b);
        System.out.println("2. Menge:  " + l + "  Size: " + l.getSize());
        CharSet m = new CharSet(c);
        System.out.println("3. Menge:  " + m + "  Size: " + m.getSize());

        System.out.println();
        CharSet r = k.vereinigung(l);
        System.out.println("Vereinigung der beiden ersten Mengen:  " + r);
        CharSet s = k.durchschnitt(l);
        System.out.println("Durchschnitt der beiden ersten Mengen: " + s);

        System.out.println();
        r = k.vereinigung(l).vereinigung(m);
        System.out.println("Vereinigung der drei Mengen:           " + r);
        s = k.durchschnitt(l).durchschnitt(m);
        System.out.println("Durchschnitt der drei Mengen:          " + s);

        System.out.println();
        CharSet t = (CharSet) k.clone();
        System.out.println("Erwartet: " + k + "  Clone:   " + t);
        System.out.println("Erwartet: true.  " + "    Equals:  " + t.equals(k));
        System.out.println("Erwartet: false. " + "    Equals:  " + t.equals(l));
        System.out.println();

    }

}
