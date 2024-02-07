import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class StrippedDownServletExample {

    private static final String[] DATE_FORMATS = new String[] {
        "EEE, dd MMM yyyy HH:mm:ss zzz",
            "EEE, dd-MMM-yy HH:mm:ss zzz",
            "EEE MMM dd HH:mm:ss yyyy"
    };
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    private final Map<String, String[]> parameters = new LinkedHashMap<>(16);

    public void setParameter(String name, String value) {
        setParameter(name, new String[] {value});
    }

    public void setParameter(String name, String... values) {
        this.parameters.put(name, values);
    }

    public void setParameters(Map<String, ?> params) {
        params.forEach((key, value) -> {
            if (value instanceof String) {
                setParameter(key, (String) value);
            }
            else if (value instanceof String[]) {
                setParameter(key, (String[]) value);
            }
            else {
                throw new IllegalArgumentException(
                        "Parameter map value must be single value " + " or array of type [" + String.class.getName() + "]");
            }
        });
    }

    public void addParameter(String name, String value) {
        addParameter(name, new String[] {value});
    }

    public void addParameter(String name, String... values) {
        String[] oldArr = this.parameters.get(name);
        if (oldArr != null) {
            String[] newArr = new String[oldArr.length + values.length];
            System.arraycopy(oldArr, 0, newArr, 0, oldArr.length);
            System.arraycopy(values, 0, newArr, oldArr.length, values.length);
            this.parameters.put(name, newArr);
        }
        else {
            this.parameters.put(name, values);
        }
    }

    public void addParameters(Map<String, ?> params) {
        params.forEach((key, value) -> {
            if (value instanceof String) {
                addParameter(key, (String) value);
            }
            else if (value instanceof String[]) {
                addParameter(key, (String[]) value);
            }
            else {
                throw new IllegalArgumentException("Parameter map value must be single value " +
                        " or array of type [" + String.class.getName() + "]");
            }
        });
    }

    /**
     * Remove already registered values for the specified HTTP parameter, if any.
     */
    public void removeParameter(String name) {
        this.parameters.remove(name);
    }

    public void removeAllParameters() {
        this.parameters.clear();
    }

    public String getParameter(String name) {
        String[] arr = this.parameters.get(name);
        return (arr != null && arr.length > 0 ? arr[0] : null);
    }

    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(this.parameters.keySet());
    }

    public String[] getParameterValues(String name) {
        return this.parameters.get(name);
    }

    public Map<String, String[]> getParameterMap() {
        return Collections.unmodifiableMap(this.parameters);
    }

    public long parseDateHeader(String name, String value) {
        for (String dateFormat : DATE_FORMATS) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.US);
            simpleDateFormat.setTimeZone(GMT);
            try {
                return simpleDateFormat.parse(value).getTime();
            }
            catch (ParseException ex) {
                // ignore
            }
        }
        throw new IllegalArgumentException("Cannot parse date value '" + value + "' for '" + name + "' header");
    }

    public static void main(String[] args) {
        StrippedDownServletExample sdse = new StrippedDownServletExample();
        sdse.addParameter("whats", "up");
        sdse.addParameter("greetings", "hello", "hallo", "hola");

        String g1 = sdse.getParameter("greetings");
        System.out.println("First greeting is: " + g1);

        String[] gs = sdse.getParameterValues("greetings");
        System.out.println("Greetings:");
        for(String g : gs) {
            System.out.println("\t" + g);
        }
    }
}
