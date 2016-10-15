package net.kyouko.cloudier.api;

import net.kyouko.cloudier.model.ImageHostingResponse;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Retrofit API interface for SM.MS.
 *
 * @author beta
 */
public interface ItorrApi {

    @POST("wb/x.php?up")
    Call<ImageHostingResponse> uploadImage(@Body RequestBody image);

}
