import java.io.Serializable;
import javax.xml.transform.SourceLocator;

abstract class Base implements SourceLocator, Serializable {
    Base() {
    }

    public final String getPublicId() {
        return "Base";
    }

    public String getSystemId() {
        return "Base";
    }

    public int getColumnNumber() {
        return 1;
    }

    public int getLineNumber() {
        return 1;
    }
}

