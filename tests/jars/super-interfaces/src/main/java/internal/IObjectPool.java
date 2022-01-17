package internal;

import java.io.Closeable;

public interface IObjectPool<T> extends Closeable {
    void addObject(T elem);

    T getObject();

    @Override
    void close();
}
