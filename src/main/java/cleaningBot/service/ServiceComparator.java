package cleaningBot.service;

import java.util.Comparator;

/**
 * Comparator class used to compare services inside the PriorityQueue data structure
 * employed in the thread for the Mutual-Exclusion implementation.
 */
public class ServiceComparator implements Comparator<WaitingThread> {
    /**
     * Generic public constructor
     */
    public ServiceComparator() {}

    /**
     * Override of the compare method to handle the comparison between two waiting threads
     * inside the system
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return The returned value is:
     *          - 1 if the timestamp of the first waiting thread is greater than the
     *              second.
     *          - 0 if they are equal.
     *          - -1 if the second is greater than the first.
     */
    @Override
    public int compare(WaitingThread o1, WaitingThread o2) {
            if(o1.getTimestamp() > o2.getTimestamp()){
                return 1;
            }
            if(o1.getTimestamp() == o2.getTimestamp()){
                return 0;
            }
        return -1;
    }
}
