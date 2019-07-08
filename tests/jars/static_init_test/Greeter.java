class Greeter {

    private final String engGreeting;

    public Greeter() {
        this.engGreeting = Constants.getGreeting();
    }

    public void greet() {
        System.out.println(this.engGreeting);
    }

    public String getGreeting() {
        return this.engGreeting;
    }

    public String getGermanGreeting() {
        return Constants.GermanGreeting;
    }

    public void greetInGerman() {
        System.out.println(this.getGermanGreeting());
    }
}
