package net.kyouko.cloudier.api;

import net.kyouko.cloudier.model.Timeline;
import net.kyouko.cloudier.model.Tweet;
import net.kyouko.cloudier.model.TweetResult;
import net.kyouko.cloudier.model.User;
import net.kyouko.cloudier.util.RequestUtil;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Retrofit API interface for Tencent Weibo.
 *
 * @author beta
 */
public interface TencentWeiboApi {

    // region User
    @GET("api/user/other_info?" + RequestUtil.CONSTANT_PARAMS)
    Call<User> getUser(@QueryMap Map<String, String> oAuthParams, @Query("name") String username);
    // endregion

    // region Timeline
    @GET("api/statuses/home_timeline?" + RequestUtil.CONSTANT_PARAMS +
            "&pageflag=0&pagetime=0&reqnum=20&type=0&contenttype=0")
    Call<Timeline> getHomeLatestTimeline(@QueryMap Map<String, String> oAuthParams);


    @GET("api/statuses/home_timeline?" + RequestUtil.CONSTANT_PARAMS +
            "&pageflag=1&reqnum=20&type=0&contenttype=0")
    Call<Timeline> getMoreHomeTimeline(@QueryMap Map<String, String> oAuthParams,
                                       @Query("pagetime") String lastTweetTimestamp);
    // endregion

    // region Tweet
    @GET("api/t/show?" + RequestUtil.CONSTANT_PARAMS)
    Call<Tweet> getTweet(@QueryMap Map<String, String> oAuthParams, @Query("id") String tweetId);


    @GET("api/t/re_list?" + RequestUtil.CONSTANT_PARAMS +
            "&flag=1&pageflag=0&pagetime=0&twitterid=0&reqnum=20&contenttype=0")
    Call<Timeline> getTweetComments(@QueryMap Map<String, String> oAuthParams,
                                    @Query("rootid") String tweetId);


    @GET("api/t/re_list?" + RequestUtil.CONSTANT_PARAMS +
            "&flag=1&pageflag=1&reqnum=20&contenttype=0")
    Call<Timeline> getMoreTweetComments(@QueryMap Map<String, String> oAuthParams,
                                        @Query("rootid") String tweetId,
                                        @Query("twitterid") String lastTweetId,
                                        @Query("pagetime") String lastTweetTimestamp);


    @GET("api/t/re_list?" + RequestUtil.CONSTANT_PARAMS +
            "&flag=0&pageflag=0&pagetime=0&twitterid=0&reqnum=20&contenttype=0")
    Call<Timeline> getTweetRetweets(@QueryMap Map<String, String> oAuthParams,
                                    @Query("rootid") String tweetId);


    @GET("api/t/re_list?" + RequestUtil.CONSTANT_PARAMS +
            "&flag=0&pageflag=1&reqnum=20&contenttype=0")
    Call<Timeline> getMoreTweetRetweets(@QueryMap Map<String, String> oAuthParams,
                                        @Query("rootid") String tweetId,
                                        @Query("twitterid") String lastTweetId,
                                        @Query("pagetime") String lastTweetTimestamp);


    @FormUrlEncoded
    @POST("api/t/add_pic_url")
    Call<TweetResult> postTweet(@FieldMap Map<String, String> constantParams,
                                @FieldMap Map<String, String> oAuthParams,
                                @Field("content") String content);
    // endregion

}
