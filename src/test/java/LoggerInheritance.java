import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerInheritance extends Logger {
    /**
     * Protected method to construct a logger for a named subsystem.
     * <p>
     * The logger will be initially configured with a null Level
     * and with useParentHandlers set to true.
     *
     * @param name               A name for the logger.  This should
     *                           be a dot-separated name and should normally
     *                           be based on the package name or class name
     *                           of the subsystem, such as java.net
     *                           or javax.swing.  It may be null for anonymous Loggers.
     * @param resourceBundleName name of ResourceBundle to be used for localizing
     *                           messages for this logger.  May be null if none
     *                           of the messages require localization.
     * @throws MissingResourceException if the resourceBundleName is non-null and
     *                                  no corresponding resource can be found.
     */
    protected LoggerInheritance(String name) {
        super(name, null);
    }

    @Override
    public void log(Level level, String msg) {
        super.log(level, msg);
    }

    @Override
    public String getName() {
        return super.getName();
    }

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        LoggerInheritance logger = new LoggerInheritance("DaLogger");
        System.out.println(logger.getName());
        Method m = LoggerInheritance.class.getMethod("getName");
        System.out.println(m);
        System.out.println(m.invoke(logger));
    }
}
