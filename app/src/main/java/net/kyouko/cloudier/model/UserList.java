package net.kyouko.cloudier.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for a list of users.
 *
 * @author beta
 */
public class UserList {

    @SerializedName("info")
    public List<User> users = new ArrayList<>();

}
