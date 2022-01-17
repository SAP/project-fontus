package internal;

public interface ObjectPoolMXBean<T> {
    int getCreatedCount();

    String getCreationStackTrace();

    int getCurrentCount();
}
