package net.kyouko.cloudier.api;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.kyouko.cloudier.model.ImageHostingResponse;
import net.kyouko.cloudier.model.ImgurResponseModel;
import net.kyouko.cloudier.model.ItorrResponseModel;

import java.lang.reflect.Type;

/**
 * Custom converter for Gson to deserialize image URLs after uploading images.
 *
 * @author beta
 */
public class ImageUrlConverter implements JsonDeserializer<ImageHostingResponse> {

    @Override public ImageHostingResponse deserialize(
            JsonElement json,
            Type typeOfT,
            JsonDeserializationContext context
    ) throws JsonParseException {
        if (json instanceof JsonNull) {
            return null;
        }

        JsonObject jsonObject = (JsonObject) json;
        ImageHostingResponse response;

        if (jsonObject.has("data")) {
            response = new ImgurResponseModel();
            response.succeeded = true;
            response.imageUrl = jsonObject.getAsJsonObject("data").get("link").getAsString();
        } else if (jsonObject.has("pid")) {
            response = new ItorrResponseModel();
            response.succeeded = true;
            response.imageUrl = "http://ww2.sinaimg.cn/large/" + jsonObject.get("pid").getAsString();
        } else {
            response = new ImageHostingResponse();
            response.succeeded = false;
        }

        return response;
    }

}
