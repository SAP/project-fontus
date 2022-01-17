package internal;

import java.util.ArrayList;
import java.util.List;

public class BaseGenericObjectPool<T> {

    protected List<T> items = new ArrayList<>();
    protected int createdCount = 0;
    public BaseGenericObjectPool() {

    }

    public void addObject(T elem) {
        items.add(elem);
        this.createdCount++;
    }

    public T getObject() {
        if(this.items.size() > 0 ) {
            return this.items.remove(0);
        } else {
            return null;
        }
    }

    public void close() {
        this.items.clear();
    }


    public int getCreatedCount() {
        return this.createdCount;
    }

    public final String getCreationStackTrace() {
        return "CREATIONSTACKTRACE";
    }

    public int getCurrentCount() {
        return this.items.size();
    }
}
