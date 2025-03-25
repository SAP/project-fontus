import javax.xml.transform.SourceLocator;

interface Printable extends SourceLocator {
    void print();

    String getSystemId();

    String getPublicId();
}

