package pdfgeneratorapi;

import static spark.Spark.port;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pdfgeneratorapi.views.General;
import pdfgeneratorapi.views.GeneratePdf;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    /**
     * This is the method through which the app runs.
     * It sets the port, registers filters (code that runs before and after each request),
     * and registers the routes and exception handlers.
     */
    public static void main(String[] args) {

        port(Config.PORT);

        new Filters().registerFilters();

        new General().registerRoutes();
        new GeneratePdf().registerRoutes();
        logger.info("All routes registered");

        new Exceptions().registerExceptions();
        
    }

    
}