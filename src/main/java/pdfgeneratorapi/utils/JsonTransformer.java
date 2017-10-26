package pdfgeneratorapi.utils;

import java.lang.reflect.Type;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import pdfgeneratorapi.utils.GenericWorker;

/**
 * Helper methods for ensuring consistent JSON. 
 * 
 * @author Simon Chapman
 *
 * @param <T> the type of the object that a JSON string will be turned into - 
 *   can be Map, a POJO etc. Can be left as Object if the parse method will not be used.
 */
public class JsonTransformer<T> {
	/**
	 * Notice the field naming policy - when rendering or parsing, 
	 * any keys (if a Map) or attributes (if a POJO) will be transformed into lowercase and
	 * any word boundaries (caseChange) will have an underscore inserted, when determining
	 * the JSON key to write to or read from.
	 */
    private static Gson gson = new GsonBuilder()
    	     .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
    	     .create();

    /**
     * This method should be used by routes that require a JSON body in their response.
     */
    public static String render(Object model) {
        return gson.toJson(model);
    }
    
    /**
     * This method should be used by routes that receive a JSON body. The output object
     * will need to be the object required by the worker that will be called to process 
     * the route - see {@link GenericWorker} for more information.
     * <br><br>
     * This version of the method is for use with non-generic classes.
     */
    public T parse(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }
    
    /**
     * This method should be used by routes that receive a JSON body. The output object
     * will need to be the object required by the worker that will be called to process 
     * the route - see {@link GenericWorker} for more information.
     * <br><br>
     * This version of the method is for use with generic classes. To get the correct type 
     * object, you can do something like 
     * <pre>
     * new TypeToken<Map<String, Object>>(){}.getType();
     * </pre>
     * ensuring that the generic class you are getting the Type for is the same as
     * what this JsonTransformer was created to return from this method.
     */
    public T parse(String json, Type type) {
        return gson.fromJson(json, type);
    }
}