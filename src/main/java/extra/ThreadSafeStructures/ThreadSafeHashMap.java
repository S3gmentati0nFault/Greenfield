package extra.ThreadSafeStructures;

import extra.Logger.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ThreadSafeHashMap<KEY, VALUE> {
    private Map<KEY, VALUE> hashMap;

    public ThreadSafeHashMap() {
        hashMap = new HashMap<>();
    }

    public synchronized VALUE addPair(KEY key, VALUE value) {
        try {
            if(hashMap.containsKey(key)) {
                return hashMap.get(key);
            }
            else{
                return hashMap.put(key, value);
            }
        } catch(UnsupportedOperationException e) {
            Logger.red("The operation is not supported", e);
        } catch(ClassCastException e) {
            Logger.red("There was an error while trying to memorize the pair", e);
        } catch(NullPointerException e) {
            Logger.red("Either the key or the value are null", e);
        }
        return null;
    }

    public synchronized VALUE removePair(KEY key) {
        try {
            return hashMap.remove(key);
        } catch(UnsupportedOperationException e) {
            Logger.red("The operation is not supported", e);
        } catch(ClassCastException e) {
            Logger.red("The key is not of the correct type", e);
        } catch(NullPointerException e) {
            Logger.red("The key is null", e);
        }
        return null;
    }

    public synchronized VALUE getValue(KEY key) {
        try {
            return hashMap.get(key);
        } catch(NullPointerException e) {
            Logger.red("The key is null", e);
        } catch(ClassCastException e) {
            Logger.red("The key is not of the correct type", e);
        }
        return null;
    }

    public synchronized Set<KEY> getKeySet() {
        return hashMap.keySet();
    }

    public synchronized Map<KEY, VALUE> getHashMap() {
        return hashMap;
    }

    public synchronized int size() {
        return getKeySet().size();
    }
}
