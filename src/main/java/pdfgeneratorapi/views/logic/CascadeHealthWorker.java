package pdfgeneratorapi.views.logic;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import pdfgeneratorapi.Config;
import pdfgeneratorapi.utils.GenericResponse;
import pdfgeneratorapi.utils.GenericWorker;
import pdfgeneratorapi.utils.HealthUtils;
import pdfgeneratorapi.utils.JsonTransformer;

public class CascadeHealthWorker extends GenericWorker<Object, Map<String, Object>> {
	private static final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS"); 

	/* 
		If using database connections then uncomment the code blocks below to check your database routes.
		Want the HealthDAO code?
		http://192.168.249.38/gadgets/gadget-sync-api/tree/master/src/main/java/gadgetsyncapi/dependencies/db2/HealthDAO.java
		Don't forget to copy it's DB-aware CascadeHealthWorkerTest class too!

	private HealthDAO healthDAO = null;
	
	public CascadeHealthWorker() {
		healthDAO = new HealthDAO();
	}
	
	// For testing
	public CascadeHealthWorker(HealthDAO healthDAO) {
		this.healthDAO = healthDAO;
	}
	*/

	@Override
	public GenericResponse<Map<String, Object>> process(Object body, Map<String, String> urlParams, Map<String, String[]> queryParams, Map<String, String> headers) {
		Logger LOGGER = LoggerFactory.getLogger(CascadeHealthWorker.class);
		LOGGER.debug("Cascade Health check - worker process start");

		int depth = Integer.parseInt(urlParams.get(":str_depth"));

	    if ((depth < 0) || (depth > Config.MAX_HEALTH_CASCADE)) {
        	LOGGER.error(String.format("Cascade depth %d out of allowed range (0 - %d)", depth, Config.MAX_HEALTH_CASCADE));

			Map<String, Object> res = new HashMap<String, Object>(4);
			res.put("app", Config.APP_NAME);
			res.put("cascade_depth", depth);
			res.put("status", "ERROR");
			Date dt = new Date();			
			String dtStr = fmt.format(dt);
			res.put("timestamp", dtStr);

			return new GenericResponse<>(400, "application/json", res);
		}

		// Create the response body
		// Can be a full blown object or just a map
		Map<String, Object> responseBody = new HashMap<>(8);
		responseBody.put("app", Config.APP_NAME);
		
		// Convert our headers map into a list of lists of strings, where each inner list 
		// contains two strings (being the header key and value respectively).
		// This matches Flask's output.
		List<List<String>> thefinalHeaders = HealthUtils.extractHeaders(headers);

		responseBody.put("headers", thefinalHeaders);
		responseBody.put("status", "OK");
        responseBody.put("cascade_depth", depth);

		List<Map<String, Object>> services = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> dbs = new ArrayList<Map<String, Object>>();
		int overall_status = 200;
		for(Map.Entry<String, String> dependency : Config.DEPENDENCIES.entrySet()) {
			String s = dependency.getValue().toLowerCase();
		
			if (s.startsWith("jdbc:")) {
				LOGGER.debug("Checking database connection " + s);
				throw new UnsupportedOperationException("Database code not in place. Go add it!");
			} else if (depth > 0) { //assume service url for now	
				LOGGER.debug("Calling health cascade on " + s);
				Map<String, Object> service = new HashMap<String, Object>();

				service.put("name", dependency.getKey());
				service.put("type", "http");
				HttpResponse<String> response = null;
				try {
					// some inconsistency regarding urls with trailing '/' so add one if it's missing'
					if (!s.endsWith("/")) {
						s = s + "/";
					}
					response = Unirest.get(s + "health/cascade/" + new Integer(depth -1))
						.header("X-Trace-ID", headers.get("X-Trace-ID"))
						.asString();
					
					int responseStatus = response.getStatus();
					service.put("status_code", responseStatus);
					service.put("content_type", response.getHeaders().getFirst("content-type"));
					switch (responseStatus) {
						case 200: 
								service.put("status", "OK");
								break;							
						case 500: 
								service.put("status", "BAD");
								overall_status = 500;
								break;
						default: 
								service.put("status", "UNKNOWN");
								overall_status = 500;
								break;
					}

					if (responseStatus == 200) {
						// Use the alternate JsonTransformer.parse method because we want it put into a generic class
						service.put("content", new JsonTransformer<Map<String, Object>>()
								.parse(response.getBody(), new TypeToken<Map<String, Object>>(){}.getType()));
					}
				} catch (UnirestException e) {
					LOGGER.error("ERROR : CascadeHealthWorker - see stacktrace : ", e);
					service.put("status_code", null); //null values will be omitted from final json
					service.put("status", "UNKNOWN");	
					service.put("content_type", null); //null values will be omitted from final json
					service.put("content", null); //null values will be omitted from final json
					overall_status = 500;
				}
				
				services.add(service);							
			}
		}

		responseBody.put("commit", Config.COMMIT);
		responseBody.put("db", dbs);
		responseBody.put("services", services);
		Date dt = new Date();
		String dtStr = fmt.format(dt);
		responseBody.put("server_timestamp", dtStr);		

		LOGGER.debug("Cascade Health check - worker process end");
		return new GenericResponse<>(overall_status, "application/json", responseBody);
	}
}