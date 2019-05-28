/**
 * A "copy" of the Linux "cal" tool.
 */
public class Cal {

    /**
     * Calculates the remainder between two numbers.
     * <p>
     * Pretty useless but allows to directly translate the algorithm given as a reference.
     *
     * @param x Numerator
     * @param y Denumerator
     * @return Remainder of the division x/y
     */
    private static int remainder(int x, int y) {
        return x % y;
    }

    /**
     * Calculates the weekday of january the first in the provided year.
     *
     * @param year The year in question
     * @return 0 for sunday, 1 for monday, ...
     */
    private static int weekDayOfJanuaryFirst(int year) {
        return remainder(1
                        + (5 * remainder(year - 1, 4)
                        + (4 * remainder(year - 1, 100))
                        + (6 * remainder(year - 1, 400))),
                7);
    }

    /**
     * Determines whether a year is a leap year
     *
     * @param year The year in question
     * @return Is year a leap year?
     */
    private static boolean isLeapYear(int year) {
        if (year % 4 != 0) {
            return false;
        } else if (year % 400 == 0) {
            return true;
        } else return year % 100 != 0;
    }

    /**
     * Maps a string representation of a month to its number.
     *
     * @param month First 3 letters of the month's name. E.g., "dec", "jan", "jul"
     * @return 1 for "jan", ...
     */
    private static int getMonth(String month) {
        if (month == null) {
            return 11;
        }
        switch (month) {
            case "jan":
                return 1;
            case "feb":
                return 2;
            case "mar":
                return 3;
            case "apr":
                return 4;
            case "may":
                return 5;
            case "jun":
                return 6;
            case "jul":
                return 7;
            case "aug":
                return 8;
            case "sep":
                return 9;
            case "oct":
                return 10;
            case "nov":
                return 11;
            case "dec":
                return 12;
            default:
                // Return current month
                return 11;
        }
    }

    /**
     * Maps a month number to its full name. E.g., 1 -> January, 2 -> February, ..
     *
     * @param month The month's number.
     * @return The month's name, e.g., "January", "February", ..
     */
    private static String monthName(int month) {
        switch (month) {
            case 1:
                return "January";
            case 2:
                return "February";
            case 3:
                return "March";
            case 4:
                return "April";
            case 5:
                return "May";
            case 6:
                return "June";
            case 7:
                return "July";
            case 8:
                return "August";
            case 9:
                return "September";
            case 10:
                return "October";
            case 11:
                return "November";
            case 12:
                return "December";
            default:
                return "Unknown";
        }
    }

    /**
     * Calculates the number of days in a month.
     *
     * @param month The month in question (e.g., 1 for january)
     * @param year The year in question (to determine leap years)
     * @return The number of days in month.year, e.g. 31 for 1,2018.
     */
    private static int daysOfMonth(int month, int year) {
        boolean isLeapYear = isLeapYear(year);
        switch (month) {
            case 1:
                return 31;
            case 2:
                return isLeapYear ? 29 : 28;
            case 3:
                return 31;
            case 4:
                return 30;
            case 5:
                return 31;
            case 6:
                return 30;
            case 7:
                return 31;
            case 8:
                return 31;
            case 9:
                return 30;
            case 10:
                return 31;
            case 11:
                return 30;
            case 12:
                return 31;
            default:
                return 0;
        }
    }

    /**
     * Determines what day the 1st weekday in a given month is.
     *
     * @param month The month in question (e.g., 1 for january)
     * @param year The year in question
     * @return The corresponding weekday for 1.month.year in the form of: sunday -> 0, monday -> 1, ..
     */
    private static int firstWeekDayOfMonth(int month, int year) {
        int weekDay = weekDayOfJanuaryFirst(year);
        for (int i = 1; i < month; i++) {
            weekDay += daysOfMonth(i, year);
        }
        weekDay = weekDay % 7;
        return weekDay;
    }

    public static void main(String[] args) {
        int year = 2018;
        int month = 11;
        switch (args.length) {
            case 0:
                break;
            case 1:
                month = getMonth(args[0]);
                break;
            case 2:
                month = getMonth(args[0]);
                year = Integer.parseInt(args[1]);

                break;
            default:
                System.out.println("Bad input, only 0 to 2 parameters allowed!");
                System.exit(2);
        }

        printHeader(month, year);
        printMonth(month, year);
    }

    /**
     * Formats a day number (1 or 2 digits) to be 3 spaces wide with a trailing space.
     *
     * @param day A number of a day in a month (1..31)
     * @return The day as a String now exactly 3 spaces wide.
     */
    private static String formatDay(int day) {
        if (day < 10) {
            return " " + day + " ";
        }
        return day + " ";
    }

    /**
     * Prints a week.
     *
     * @param firstWeekDay The weekday of the first day we print in this week.
     * @param firstDay The day the specific week starts on (1-31)
     * @param daysLeftInMonth How many days are left in the month to print. In case the month ends before the week is
     * over.
     * @return How many days in the week we printed. This isn't always 7 as the month may end during this week.
     */
    private static int printWeek(int firstWeekDay, int firstDay, int daysLeftInMonth) {
        String weekString = "";
        for (int i = 0; i < firstWeekDay; i++) {
            weekString += "   ";
        }
        int daysPrinted = 0;
        for (int j = firstWeekDay; j < daysLeftInMonth && j < 7; j++) {
            weekString += formatDay(firstDay);
            firstDay++;
            daysPrinted++;
        }
        System.out.println(weekString);
        return daysPrinted;
    }

    /**
     * Prints the header for the calendar output.
     *
     * @param month The month to print (1-12)
     * @param year What year it is.
     */
    private static void printHeader(int month, int year) {
        String monthName = monthName(month);
        System.out.println("\t" + monthName + " " + year);
    }

    /**
     * Prints a month.
     *
     * @param month The month to print (1-12)
     * @param year What year it is.
     */
    private static void printMonth(int month, int year) {
        System.out.println("Su Mo Tu We Th Fr Sa");
        int firstWeekday = firstWeekDayOfMonth(month, year);
        int currentDay = 1;
        int daysLeft = daysOfMonth(month, year);
        while (daysLeft > 0) {
            int printed = printWeek(firstWeekday, currentDay, daysLeft);
            firstWeekday = 0;
            currentDay += printed;
            daysLeft -= printed;
        }
    }
}
