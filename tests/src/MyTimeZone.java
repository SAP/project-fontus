import java.util.TimeZone;
import java.util.Date;

class MyTimeZone extends TimeZone {

    MyTimeZone() {
        super();
    }

    public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds) {
        return 0;
    }

    public int getRawOffset() {
        return 1;
    }

    public boolean inDaylightTime(Date d) {
        return false;
    }

    public void setRawOffset(int ms) {

    }

    public boolean useDaylightTime() {
        return false;
    }

    public static void main(String[] args) {
        for(String id : MyTimeZone.getAvailableIDs()) {
            System.out.printf("Id: %s%n", id);
        }
    }
}
