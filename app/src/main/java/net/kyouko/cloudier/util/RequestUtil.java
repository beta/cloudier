package net.kyouko.cloudier.util;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.kyouko.cloudier.Config;
import net.kyouko.cloudier.api.BooleanConverter;
import net.kyouko.cloudier.api.CustomConverterFactory;
import net.kyouko.cloudier.api.ImageUrlConverter;
import net.kyouko.cloudier.api.ImgurApi;
import net.kyouko.cloudier.api.ItorrApi;
import net.kyouko.cloudier.api.MapDeserializer;
import net.kyouko.cloudier.api.TencentWeiboApi;
import net.kyouko.cloudier.model.Account;
import net.kyouko.cloudier.model.ImageHostingResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Util class for creating and executing API requests.
 *
 * @author beta
 */
public class RequestUtil {

    private static TencentWeiboApi apiInstance;
    private static ImgurApi imgurApiInstance;
    private static ItorrApi itorrApiInstance;

    private static Map<String, String> constantParams;
    private static Map<String, String> oAuthParams;

    private static Map<String, RequestBody> constantParts;
    private static Map<String, RequestBody> oAuthParts;


    public final static String CONSTANT_PARAMS = "format=json&clientip=127.0.0.1";


    /**
     * Returns a {@link Map} of constant parameters used in requests.
     *
     * @return a {@link Map} of constant parameters used in requests.
     */
    public static Map<String, String> getConstantParams() {
        if (constantParams == null) {
            synchronized (RequestUtil.class) {
                if (constantParams == null) {
                    constantParams = new HashMap<>();
                    constantParams.put("format", "json");
                    constantParams.put("clientip", "127.0.0.1");
                    constantParams.put("pic_type", "1");
                }
            }
        }

        return constantParams;
    }


    /**
     * Creates a {@link Map} of OAuth parameters from account information of logged in user.
     *
     * @param context {@link Context} used to read user account.
     * @return a {@link Map} of OAuth parameters.
     */
    public static Map<String, String> getOAuthParams(Context context) {
        if (oAuthParams == null) {
            synchronized (RequestUtil.class) {
                if (oAuthParams == null) {
                    Account account = AuthUtil.readAccount(context);

                    oAuthParams = new HashMap<>();
                    oAuthParams.put("oauth_consumer_key", Config.TENCENT_APP_KEY);
                    oAuthParams.put("oauth_version", "2.a");
                    oAuthParams.put("scope", "all");
                    oAuthParams.put("access_token", account.accessToken);
                    oAuthParams.put("openid", account.openId);
                }
            }
        }

        return oAuthParams;
    }


    /**
     * Returns an instance of {@link TencentWeiboApi} for performing API requests.
     *
     * @return an instance of {@link TencentWeiboApi} for performing API requests.
     */
    public static TencentWeiboApi getApiInstance() {
        if (apiInstance == null) {
            synchronized (RequestUtil.class) {
                if (apiInstance == null) {
                    apiInstance = createApiInstance();
                }
            }
        }

        return apiInstance;
    }


    /**
     * Returns an instance of {@link ImgurApi} for performing API requests.
     *
     * @return an instance of {@link ImgurApi} for performing API requests.
     */
    public static ImgurApi getImgurApiInstance() {
        if (imgurApiInstance == null) {
            synchronized (RequestUtil.class) {
                if (imgurApiInstance == null) {
                    imgurApiInstance = createImgurApiInstance();
                }
            }
        }

        return imgurApiInstance;
    }


    /**
     * Returns an instance of {@link ItorrApi} for performing API requests.
     *
     * @return an instance of {@link ItorrApi} for performing API requests.
     */
    public static ItorrApi getItorrApiInstance() {
        if (itorrApiInstance == null) {
            synchronized (RequestUtil.class) {
                if (itorrApiInstance == null) {
                    itorrApiInstance = createItorrApiInstance();
                }
            }
        }

        return itorrApiInstance;
    }


    private static TencentWeiboApi createApiInstance() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(boolean.class, new BooleanConverter())
                .registerTypeAdapter(Boolean.class, new BooleanConverter())
                .registerTypeAdapter(Map.class, new MapDeserializer())
                .create();

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request().newBuilder()
                                .header("Accept", Config.HTTP_ACCEPT)
                                .build();
                        return chain.proceed(request);
                    }
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://open.t.qq.com/")
                .client(client)
                .addConverterFactory(CustomConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        return retrofit.create(TencentWeiboApi.class);
    }


    private static ImgurApi createImgurApiInstance() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ImageHostingResponse.class, new ImageUrlConverter())
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.imgur.com/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        return retrofit.create(ImgurApi.class);
    }


    private static ItorrApi createItorrApiInstance() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ImageHostingResponse.class, new ImageUrlConverter())
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://x.mouto.org/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        return retrofit.create(ItorrApi.class);
    }

}
