package net.kyouko.cloudier.api;

import net.kyouko.cloudier.Config;
import net.kyouko.cloudier.model.ImgurResponseModel;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Retrofit API interface for Imgur.
 */
public interface ImgurApi {

    @POST("3/image")
    @Multipart
    @Headers("Authorization: Client-ID " + Config.IMGUR_CLIENT_ID)
    Call<ImgurResponseModel> uploadImage(@Part("image") RequestBody image);

}
