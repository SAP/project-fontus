import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;

public class Main {


    public static void main(String[] args) {
        String defaultTimeZoneID = java.util.TimeZone.getDefault().getID();
        System.out.printf("Calendar time zone: %s%n", defaultTimeZoneID);

        TimeZoneRegistry timeZoneRegistry = TimeZoneRegistryFactory.getInstance().createRegistry();
        TimeZone defaultTimeZone = timeZoneRegistry.getTimeZone(defaultTimeZoneID);
        if(defaultTimeZone == null) {
            System.out.printf("Cannot match the JVM default time zone to an ical4j time zone: %s%n", defaultTimeZoneID);
        }
    }

}
