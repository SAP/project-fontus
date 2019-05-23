/**
 * Validates ISBN13 checksums.
 */
class ISBN {

    private static int charToInt(char c) {
        return Integer.parseInt("" + c);
    }

    private static int extractCheckNumber(String isbn) {
        char checkNumber = isbn.charAt(12);
        int number = charToInt(checkNumber);
        System.out.println("Extracted Check Number is: " + number);
        return number;
    }

    private static int multiplyWith(int index) {
        if (index % 2 == 0) {
            return 1;
        }
        return 3;
    }

    private static int calculateCheckNumber(String isbn) {
        int sum = 0;

        for (int i = 0; i < 12; i++) {
            char c = isbn.charAt(i);
            int numberAt = charToInt(c);
            int multilicant = multiplyWith(i);
            int addOnto = numberAt * multilicant;
            sum = sum + addOnto;
            //System.out.println("At index: " + i + " number: " + numberAt + " will be multiplied with: " + multilicant + " resulting in: " + sum);
        }
        // sum is 105
        int lastDigit = sum % 10;
        int diff = 10 - lastDigit;
        System.out.println("Calculated Check Number is: " + diff);
        return diff;
    }

    private static boolean validateIsbn(String isbn) {
        int originalCheckNumber = extractCheckNumber(isbn);
        int calculatedCheckNumber = calculateCheckNumber(isbn);
        return originalCheckNumber == calculatedCheckNumber;
    }

    public static void main(String[] args) {
        String isbnExampleInput = "9783837416315";
        boolean isValid = validateIsbn(isbnExampleInput);
        System.out.println("Isbn: '" + isbnExampleInput + "' is valid: " + isValid);
    }
}
