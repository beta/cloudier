package net.kyouko.cloudier.event;

/**
 * Event class for viewing users.
 *
 * @author beta
 */
public class ViewUserEvent {

    public String username;


    public ViewUserEvent(String username) {
        this.username = username;
    }

}
