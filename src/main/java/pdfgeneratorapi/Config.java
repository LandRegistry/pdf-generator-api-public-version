package pdfgeneratorapi;

import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

/** 
 * RULES OF CONFIG:
 * 1. No region specific code. Regions are defined by setting the OS environment variables appropriately to build up the
 * desired behaviour.
 * 2. No use of defaults when getting OS environment variables. They must all be set to the required values prior to the
 * app starting.
 * 3. This is the only file in the app where System.getenv should be used.
 * 4. All fields are public static final, and directly referenced in other classes e.g. Config.PORT 
 */
public final class Config {
    // Hide constructor
    private Config() {
    }
    
    /* 
      For general info:
      Optional.ofNullable creates an Optional object with the inner value being the result of the getenv. 
      The orElseThrow method on the Optional will return the inner value if it is not null, 
      otherwise will throw the specified exception (by passing it the new method to call)
      This ensures that the app WILL NOT EVEN START if any of the config variables are not set in the environment. 
      Fail-fast and all that.
    */
    
    // For health route
    public static final String COMMIT = Optional.ofNullable(System.getenv("COMMIT")).orElseThrow(IllegalArgumentException::new);

    // for cascade health route
    // environment names of resources on which api is reliant and to be checked by /heath/cascade
    public static final int MAX_HEALTH_CASCADE = Integer.parseInt(Optional.ofNullable(System.getenv("MAX_HEALTH_CASCADE")).orElseThrow(IllegalArgumentException::new));
    // Following is an example of building the dependency structure used by the cascade route
    // SELF can be used to demonstrate how it works (i.e. it will call it's own casecade
    // route until MAX_HEALTH_CASCADE is hit)
    public static final Map<String, String> DEPENDENCIES = new HashMap<String, String>();
    /*
    public static final String SELF = "http://localhost:8080/";
    static {
      DEPENDENCIES.put("SELF", SELF); // Put ALL dependencies into static
    }
    */

    // This APP_NAME variable is to allow changing the app name when the app is running in a cluster. So that
    // each app in the cluster will have a unique name.
    public static final String APP_NAME = Optional.ofNullable(System.getenv("APP_NAME")).orElseThrow(IllegalArgumentException::new);

    // The port to run on
    public static final int PORT = Integer.parseInt(Optional.ofNullable(System.getenv("PORT")).orElseThrow(IllegalArgumentException::new));

} 