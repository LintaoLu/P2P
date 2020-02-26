package common;

/**
 * Response contains all response information.
 * Again I didn't use Java "serializable" object.
 * */
public class Response {

    private int responseType;
    private String responseBody;

    public Response(int responseType, String responseBody) {
        this.responseType = responseType;
        this.responseBody = responseBody;
    }

    public Response(){}

    public int getResponseType() {
        return responseType;
    }

    public void setResponseType(int responseType) {
        this.responseType = responseType;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    @Override
    public String toString() {
        return "Response{" +
                "responseType=" + responseType +
                ", responseBody='" + responseBody + '\'' +
                '}';
    }
}
