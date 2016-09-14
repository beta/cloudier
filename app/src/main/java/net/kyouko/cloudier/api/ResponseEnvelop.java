package net.kyouko.cloudier.api;

/**
 * Envelop class for API response, containing request result and data.
 *
 * @author beta
 */
public class ResponseEnvelop<DataType> {

    public int resultCode;
    public String message;
    public int errorCode;
    public DataType data;


    public static class RequestErrorException extends RuntimeException {

        public final int errorCode;
        public final String message;


        public RequestErrorException(int errorCode, String message) {
            super("Error code: " + errorCode + ", " + message);
            this.errorCode = errorCode;
            this.message = message;
        }

    }

}
