package cleaningBot.service;

import java.util.Comparator;

public class ServiceComparator implements Comparator<ServiceEntry> {
    public ServiceComparator(){}
    @Override
    public int compare(ServiceEntry o1, ServiceEntry o2) {


            if(o1.getTimestamp() > o2.getTimestamp()){
                return 1;
            }
            if(o1.getTimestamp() == o2.getTimestamp()){
                return 0;
            }
        return -1;
    }
}
