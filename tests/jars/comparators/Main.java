import java.util.*;
import java.text.*;
public class Main {

    public static void main(String[] args) {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry("a", "b"));
        entries.add(new Entry("c", "d"));
        entries.add(new Entry("d", "c"));
        entries.add(new Entry("f", "f"));

        Collator collator = Collator.getInstance(Locale.US);
        collator.setStrength(Collator.IDENTICAL);

        entries.sort(Comparator.comparing(e -> e.getFirstName(), collator));
        for(Entry e : entries) {

            System.out.println(e);
        }
        entries.sort(Comparator.comparing(e -> e.getLastName(), collator));
        for(Entry e : entries) {

            System.out.println(e);
        }
    }
}
