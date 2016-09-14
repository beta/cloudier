package net.kyouko.cloudier.api;

import org.apache.commons.lang3.reflect.TypeUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Custom converter factory for Retrofit to parse API response with result envelop.
 *
 * @author beta
 */
public class CustomConverterFactory extends Converter.Factory {

    public static CustomConverterFactory create() {
        return new CustomConverterFactory();
    }


    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
                                                            Retrofit retrofit) {
        Type envelopType = TypeUtils.parameterize(ResponseEnvelop.class, type);
        Converter converter = retrofit.nextResponseBodyConverter(CustomConverterFactory.this,
                envelopType, annotations);
        return new ResponseConverter<>(converter);
    }


    public static class ResponseConverter<T> implements Converter<ResponseBody, T> {

        private final Converter<ResponseBody, ResponseEnvelop<T>> delegate;


        public ResponseConverter(Converter<ResponseBody, ResponseEnvelop<T>> delegate) {
            this.delegate = delegate;
        }


        @Override
        public T convert(ResponseBody value) throws IOException {
            ResponseEnvelop<T> envelop = delegate.convert(value);
            if (envelop.resultCode != 0) {
                throw new ResponseEnvelop.RequestErrorException(envelop.errorCode, envelop.message);
            }
            return envelop.data;
        }

    }

}
