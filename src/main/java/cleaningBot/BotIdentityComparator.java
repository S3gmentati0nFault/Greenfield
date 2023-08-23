package cleaningBot;

import beans.BotIdentity;

import java.util.Comparator;

public class BotIdentityComparator implements Comparator<BotIdentity> {

    @Override
    public int compare(BotIdentity o1, BotIdentity o2) {
        if(o1.getId() > o2.getId()) {
            return 1;
        }
        else {
            return -1;
        }
    }
}
