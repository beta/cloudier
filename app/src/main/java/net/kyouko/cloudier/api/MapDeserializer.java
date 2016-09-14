package net.kyouko.cloudier.api;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Custom deserializer for Gson to deserialize JSON objects to {@link Map}s.
 *
 * @author beta
 */
public class MapDeserializer implements JsonDeserializer<Map<String, String>> {

    @Override
    public Map<String, String> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        Type mapType = new TypeToken<Map<String, String>>() {}.getType();
        return new GsonBuilder().create().fromJson(json.toString(), mapType);
    }

}
