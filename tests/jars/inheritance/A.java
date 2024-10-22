import javax.xml.transform.SourceLocator;

abstract class A implements SourceLocator {
    A() {
    }

    public final String getPublicId() {
        return "A";
    }

    public String getSystemId() {
        return "A";
    }

    public int getColumnNumber() {
        return 1;
    }

    public int getLineNumber() {
        return 1;
    }
}
