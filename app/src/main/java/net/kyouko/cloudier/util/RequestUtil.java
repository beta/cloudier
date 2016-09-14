package net.kyouko.cloudier.util;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.kyouko.cloudier.api.BooleanConverter;
import net.kyouko.cloudier.api.CustomConverterFactory;
import net.kyouko.cloudier.api.TencentWeiboApi;
import net.kyouko.cloudier.model.Account;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Util class for creating and executing API requests.
 *
 * @author beta
 */
public class RequestUtil {

    private static TencentWeiboApi apiInstance;


    /**
     * Creates a {@link Map} of OAuth parameters from account information of logged in user.
     *
     * @param context {@link Context} used to read user account.
     * @return a {@link Map} of OAuth parameters.
     */
    public static Map<String, String> createOAuthParams(Context context) {
        Account account = AuthUtil.readAccount(context);

        Map<String, String> params = new HashMap<>();
        params.put("access_token", account.accessToken);
        params.put("openid", account.openId);

        return params;
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


    private static TencentWeiboApi createApiInstance() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(boolean.class, new BooleanConverter())
                .registerTypeAdapter(Boolean.class, new BooleanConverter())
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://open.t.qq.com/")
                .addConverterFactory(CustomConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        return retrofit.create(TencentWeiboApi.class);
    }

}
