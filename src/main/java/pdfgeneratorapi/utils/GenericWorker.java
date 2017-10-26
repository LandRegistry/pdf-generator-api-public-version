package pdfgeneratorapi.utils;

import java.util.Map;

import pdfgeneratorapi.ApplicationException;

/**
 * This is class to be extended that can be used to put the main processing
 * logic of routes in, to allow for separation from the spark-specific code
 * as well as easier unit testing.
 * 
 * The code will have access to the parsed body (if any), the query parameters 
 * from the URL and the HTTP headers, which should be all it needs.
 * 
 * @author Simon Chapman
 * 
 * @param <T> The type of the parsed incoming body. Can be set to Object if no body is expected/used.
 * @param <S> The type of the outgoing body to be parsed. Can be set to Object if no body is required. 
 *   See {@link GenericResponse} for more information.
 *
 */
public abstract class GenericWorker<T, S> {
	/**
	 * Uses the data from the original request to perform the needed work.
	 * 
	 * @return Information about the response that the route handler method
	 * can use to construct an appropriate response.
	 * 
	 * @throws ApplicationException If there has been an error.
	 */
	public abstract GenericResponse<S> process(T body, 
			Map<String, String> urlParams,
			Map<String, String[]> queryParams, 
			Map<String, String> headers);
}

