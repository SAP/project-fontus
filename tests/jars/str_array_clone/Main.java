import java.util.ArrayList;
import java.util.Scanner;

class Main {

    static class Size {

        public final String[] args;

        public Size(String sentence) {
            ArrayList<String> as = new ArrayList<>();
            Scanner reader = new Scanner(sentence);
            while(reader.hasNext()) {
                as.add(reader.next());
            }
            this.args = as.toArray(new String[0]);
        }
    }

    static Size getSize(String s) {
        return new Size(s);
    }

    public static void main(String[] args) {
        StringBuilder collected = new StringBuilder();
        for(String s : args) {
            collected.append(s);
            collected.append(" ");
        }
        String whole = collected.toString();
        System.out.println("Collected as: " + whole);
        String[] splitted = (String[]) getSize(whole).args.clone();
        System.out.println("Split into:");
        for(String s : splitted) {
            System.out.println(s);
        }
    }
}
