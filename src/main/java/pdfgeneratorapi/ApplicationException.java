package pdfgeneratorapi;

/**
 * This class is to be thrown when the application identifies that there's been a problem
 * and that the client should be informed.
 *
 * Example:
 *  throw new ApplicationError("Title number invalid", "E102", 400)
 *
 * The handler method (see Exceptions.java) will then create the response body in a standard 
 * JSON structure so clients will always know what to parse.
 */
public class ApplicationException extends RuntimeException {
    private static final long serialVersionUID = 6897959604708374273L;
    public int httpCode;
    public String errorMessage;
    public String errorCode;

    public ApplicationException(String errorMessage, String errorCode, int httpCode) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.httpCode = httpCode;
    }

    /**
        * Use this constructor if this exception is being thrown due to catching a previous one -
        * the stack trace of the original will therefore be logged correctly (as "caused by").
    */
    public ApplicationException(String errorMessage, String errorCode, int httpCode, Throwable originalException) {
        super(errorMessage, originalException);
        this.errorCode = errorCode;
        this.httpCode = httpCode;
    }

}