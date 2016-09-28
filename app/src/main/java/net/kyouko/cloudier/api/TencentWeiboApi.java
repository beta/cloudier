package net.kyouko.cloudier.api;

import net.kyouko.cloudier.model.Empty;
import net.kyouko.cloudier.model.Timeline;
import net.kyouko.cloudier.model.Tweet;
import net.kyouko.cloudier.model.TweetResult;
import net.kyouko.cloudier.model.Update;
import net.kyouko.cloudier.model.UploadImageResult;
import net.kyouko.cloudier.model.User;
import net.kyouko.cloudier.model.UserList;
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


    // region Update
    int UPDATE_TYPE_MENTIONS = 6;
    int UPDATE_TYPE_FOLLOWERS = 8;


    @GET("api/info/update?" + RequestUtil.CONSTANT_PARAMS + "&op=0")
    Call<Update> getUpdates(@QueryMap Map<String, String> oAuthParams);


    @GET("api/info/update?" + RequestUtil.CONSTANT_PARAMS + "&op=1&type=" + UPDATE_TYPE_MENTIONS)
    Call<Update> clearMentionsUpdate(@QueryMap Map<String, String> oAuthParams);


    @GET("api/info/update?" + RequestUtil.CONSTANT_PARAMS + "&op=1&type=" + UPDATE_TYPE_FOLLOWERS)
    Call<Update> clearFollowersUpdate(@QueryMap Map<String, String> oAuthParams);
    // endregion


    // region Timeline
    @GET("api/statuses/home_timeline?" + RequestUtil.CONSTANT_PARAMS +
            "&pageflag=0&pagetime=0&reqnum=20&type=0&contenttype=0")
    Call<Timeline> getLatestHomeTimeline(@QueryMap Map<String, String> oAuthParams);


    @GET("api/statuses/home_timeline?" + RequestUtil.CONSTANT_PARAMS +
            "&pageflag=1&reqnum=20&type=0&contenttype=0")
    Call<Timeline> getMoreHomeTimeline(@QueryMap Map<String, String> oAuthParams,
                                       @Query("lastid") String lastTweetId,
                                       @Query("pagetime") String lastTweetTimestamp);


    @GET("api/statuses/mentions_timeline?" + RequestUtil.CONSTANT_PARAMS +
            "&pageflag=0&pagetime=0&reqnum=20&type=0&contenttype=0")
    Call<Timeline> getLatestNotificationTimeline(@QueryMap Map<String, String> oAuthParams);


    @GET("api/statuses/mentions_timeline?" + RequestUtil.CONSTANT_PARAMS +
            "&pageflag=1&reqnum=20&type=0&contenttype=0")
    Call<Timeline> getMoreNotificationsTimeline(@QueryMap Map<String, String> oAuthParams,
                                                @Query("lastid") String lastTweetId,
                                                @Query("pagetime") String lastTweetTimestamp);


    @GET("api/statuses/user_timeline?" + RequestUtil.CONSTANT_PARAMS +
            "&pageflag=0&pagetime=0&lastid=0&reqnum=20&type=35&contenttype=0")
    Call<Timeline> getLatestUserTimeline(@QueryMap Map<String, String> oAuthParams,
                                         @Query("name") String username);


    @GET("api/statuses/user_timeline?" + RequestUtil.CONSTANT_PARAMS +
            "&pageflag=1&reqnum=20&type=35&contenttype=0")
    Call<Timeline> getMoreUserTimeline(@QueryMap Map<String, String> oAuthParams,
                                       @Query("name") String username,
                                       @Query("lastid") String lastTweetId,
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
                                @Field("content") String content,
                                @Field("pic_url") String imageUrls);


    @FormUrlEncoded
    @POST("api/t/upload_pic")
    Call<UploadImageResult> uploadImageFromUrl(@FieldMap Map<String, String> constantParams,
                                               @FieldMap Map<String, String> oAuthParams,
                                               @Field("pic_url") String imageUrl);


    @FormUrlEncoded
    @POST("api/t/comment")
    Call<TweetResult> comment(@FieldMap Map<String, String> constantParams,
                              @FieldMap Map<String, String> oAuthParams,
                              @Field("reid") String tweetId,
                              @Field("content") String content);


    @FormUrlEncoded
    @POST("api/t/re_add")
    Call<TweetResult> retweet(@FieldMap Map<String, String> constantParams,
                              @FieldMap Map<String, String> oAuthParams,
                              @Field("reid") String tweetId,
                              @Field("content") String content,
                              @Field("pic_url") String imageUrls);
    // endregion


    // region relations
    @GET("api/friends/user_idollist?" + RequestUtil.CONSTANT_PARAMS + "&reqnum=20&startindex=0")
    Call<UserList> getFollowingList(@QueryMap Map<String, String> oAuthParams,
                                    @Query("name") String username);


    @GET("api/friends/user_idollist?" + RequestUtil.CONSTANT_PARAMS + "&reqnum=20")
    Call<UserList> getMoreFollowingList(@QueryMap Map<String, String> oAuthParams,
                                        @Query("name") String username,
                                        @Query("startindex") int startIndex);


    @GET("api/friends/user_fanslist?" + RequestUtil.CONSTANT_PARAMS + "&reqnum=20&startindex=0")
    Call<UserList> getFollowerList(@QueryMap Map<String, String> oAuthParams,
                                   @Query("name") String username);


    @GET("api/friends/user_fanslist?" + RequestUtil.CONSTANT_PARAMS + "&reqnum=20")
    Call<UserList> getMoreFollowerList(@QueryMap Map<String, String> oAuthParams,
                                       @Query("name") String username,
                                       @Query("startindex") int startIndex);


    @FormUrlEncoded
    @POST("api/friends/add")
    Call<Empty> followUser(@FieldMap Map<String, String> constantParams,
                           @FieldMap Map<String, String> oAuthParams,
                           @Field("name") String username);


    @FormUrlEncoded
    @POST("api/friends/del")
    Call<Empty> unfollowUser(@FieldMap Map<String, String> constantParams,
                             @FieldMap Map<String, String> oAuthParams,
                             @Field("name") String username);
    // endregion

}
