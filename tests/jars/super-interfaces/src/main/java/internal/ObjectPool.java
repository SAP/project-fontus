package internal;

import java.io.Closeable;

public interface ObjectPool<T> extends Closeable {
    void addObject(T elem);

    T getObject();

    @Override
    void close();
}
