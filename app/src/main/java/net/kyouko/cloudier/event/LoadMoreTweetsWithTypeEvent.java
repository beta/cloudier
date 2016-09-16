package net.kyouko.cloudier.event;

/**
 * Event for loading more tweets.
 *
 * @author beta
 */
public class LoadMoreTweetsWithTypeEvent {

    public int type;


    public LoadMoreTweetsWithTypeEvent(int type) {
        this.type = type;
    }

}
