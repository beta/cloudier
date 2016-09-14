package net.kyouko.cloudier.api;

import net.kyouko.cloudier.Config;
import net.kyouko.cloudier.model.Timeline;
import net.kyouko.cloudier.model.User;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Retrofit API interface for Tencent Weibo.
 *
 * @author beta
 */
public interface TencentWeiboApi {

    String CONSTANT_PARAMS = "?format=json&oauth_consumer_key=" + Config.TENCENT_APP_KEY +
            "&oauth_version=2.a&scope=all&clientip=127.0.0.1";


    @GET("api/user/other_info" + CONSTANT_PARAMS)
    Call<User> getUser(@QueryMap Map<String, String> oAuthParams, @Query("name") String username);


    @GET("api/statuses/home_timeline" + CONSTANT_PARAMS +
            "&pageflag=0&pagetime=0&reqnum=20&type=0&contenttype=0")
    Call<Timeline> getHomeLatestTimeline(@QueryMap Map<String, String> oAuthParams);

}
