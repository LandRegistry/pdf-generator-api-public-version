package pdfgeneratorapi.utils;

import spark.Request;
import spark.Response;

/**
 * This is what workers return to route handler methods. It contains enough information
 * to construct a response to send back to the client.
 * 
 * @param <T> The object that will ultimately be transformed into the response text 
 * (using a transformer specified by the route). In the case of JSON, this could be anything the 
 * transformer supports e.g. a Map, POJO with attributes, Lists, or a combination of all depending 
 * on the complexity of the response.
 * Why do we enforce this as a generic instead of just Object? Helps us have better tests and
 * reduces chances of coding mistakes.
 *
 */
public class GenericResponse<T> {
	public int httpCode;
	public String responseBodyContentType;
	public T responseBodyObject;

	public GenericResponse(int httpCode, String responseBodyContentType, T responseBodyObject) {
		this.httpCode = httpCode;
		this.responseBodyContentType = responseBodyContentType;
		this.responseBodyObject = responseBodyObject;
	}
	
	public T getResult (Request req, Response res) {
		res.status(httpCode);
		res.type(responseBodyContentType);
		return responseBodyObject;
	};

}