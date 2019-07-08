class Main {

    public static void main(String[] args) {

        String germanGreeting = Constants.GermanGreeting;
        System.out.println(germanGreeting);
        Greeter g = new Greeter();
        g.greet();
        g.greetInGerman();
        String gg = g.getGermanGreeting();
        System.out.println(gg);
        String eg = g.getGreeting();
        System.out.println(eg);

    }

}
