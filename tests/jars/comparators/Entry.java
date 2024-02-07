class Entry {

    private final String firstName;
    private final String lastName;

    public Entry(String first, String last) {
        this.firstName = first;
        this.lastName = last;
    }

    public String getFirstName() {
        return this.firstName;
    }
    public String getLastName() {
        return this.lastName;
    }

    @Override
    public String toString() {
        return String.format("Entry{first_name=%s, last_name=%s}", this.firstName, this.lastName);
    }
}
