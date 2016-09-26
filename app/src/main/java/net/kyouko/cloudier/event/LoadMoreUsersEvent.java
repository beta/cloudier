package net.kyouko.cloudier.event;

import net.kyouko.cloudier.model.User;

/**
 * Event for loading more users.
 *
 * @author beta
 */
public class LoadMoreUsersEvent {

    public int type;


    public LoadMoreUsersEvent() {
        this.type = User.TYPE_FOLLOWER;
    }


    public LoadMoreUsersEvent(int type) {
        this.type = type;
    }

}
