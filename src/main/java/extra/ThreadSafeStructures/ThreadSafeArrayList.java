package extra.ThreadSafeStructures;

import extra.Logger.Logger;

import java.util.ArrayList;
import java.util.List;

public class ThreadSafeArrayList<ELEMENT> {
    private List<ELEMENT> arrayList;
    private boolean writing;

    public ThreadSafeArrayList() {
        arrayList = new ArrayList<>();
    }

    public synchronized boolean addElement(ELEMENT element) {
        try{
            return arrayList.add(element);
        } catch(UnsupportedOperationException e) {
            Logger.red("The operation is not supported", e);
        } catch(ClassCastException e) {
            Logger.red("The element is not of the correct type", e);
        } catch(NullPointerException e) {
            Logger.red("The element is null", e);
        }
        return false;
    }

    public synchronized boolean removeElement(ELEMENT element) {
        try {
            return arrayList.remove(element);
        } catch(UnsupportedOperationException e) {
            Logger.red("The operation is not supported", e);
        } catch(ClassCastException e) {
            Logger.red("The element is not of the correct type", e);
        } catch(NullPointerException e) {
            Logger.red("The element is null", e);
        }
        return false;
    }

    public synchronized boolean isEmpty() {
        return arrayList.isEmpty();
    }

    public synchronized List<ELEMENT> getArrayList() {
        return new ArrayList<>(arrayList);
    }

    public synchronized int size() {
        return arrayList.size();
    }

    public synchronized void swap(ELEMENT oldElement, ELEMENT newElement) {
        arrayList.remove(oldElement);
        arrayList.add(newElement);
    }

    public synchronized void addAll(List<ELEMENT> elements) {
        arrayList.addAll(elements);
    }
}
