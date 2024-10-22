class Main {

    public static void main(String[] args) {
        Person person = new Person("John Doe", "100 Linda Ln.");
        System.out.printf("%s in %s, or '%s'\n", person.name(), person.address(), person.toString());
    }
}
