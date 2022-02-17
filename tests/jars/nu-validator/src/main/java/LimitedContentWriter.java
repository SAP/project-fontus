import java.io.Writer;

public class LimitedContentWriter extends Writer {

    private final StringBuilder sb;
    private final int maxSize;

    /**
     * @param len
     */
    public LimitedContentWriter(int len, int maxSize) {
        sb = new StringBuilder(Math.min(len, maxSize));
        this.maxSize = maxSize;
    }

    protected LimitedContentWriter(int len) {
        this(len, Integer.MAX_VALUE);
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
        if(accept() && len + sb.length() < maxSize) {
            sb.append(cbuf, off, len);
        }
    }

    @Override
    public Writer append(CharSequence seq, int start, int end) {
        if(accept()) {
            if((end - start) + sb.length() < maxSize) {
                sb.append(seq, start, end);
            } else {
                sb.append(seq, start, start + (maxSize - sb.length()));
            }
        }
        return this;
    }

    @Override
    public Writer append(CharSequence csq) {
        if(accept()) {
            if(csq.length() + sb.length() < maxSize) {
                sb.append(csq);
            } else {
                sb.append(csq, 0, maxSize - sb.length());
            }
        }
        return this;
    }

    @Override
    public Writer append(char c) {
        if(accept()) sb.append(c);
        return this;
    }

    public final boolean accept() {
        return sb.length() < maxSize;
    }

    @Override
    public void flush() {
        //
    }

    @Override
    public void close() {
        //
    }

    public int length() {
        return sb.length();
    }

    public char charAt(int index) {
        return sb.charAt(index);
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}