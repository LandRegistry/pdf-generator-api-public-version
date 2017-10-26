import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

public final class TestUtils {
	/**
	 * This sets values into the system environment.
	 * It is a massive hack so should only be used when testing.
	 * 
	 * @param newenv A map containing the key/value env var pairs
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void setEnv(Map<String, String> newenv) throws Exception
	{
	  try
	    {
	        Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
	        Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
	        theEnvironmentField.setAccessible(true);
			Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
	        env.putAll(newenv);
	        Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
	        theCaseInsensitiveEnvironmentField.setAccessible(true);
	        Map<String, String> cienv = (Map<String, String>)     theCaseInsensitiveEnvironmentField.get(null);
	        cienv.putAll(newenv);
	    }
	    catch (NoSuchFieldException e)
	    {
	        Class[] classes = Collections.class.getDeclaredClasses();
	        Map<String, String> env = System.getenv();
	        for(Class cl : classes) {
	            if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
	                Field field = cl.getDeclaredField("m");
	                field.setAccessible(true);
	                Object obj = field.get(env);
	                Map<String, String> map = (Map<String, String>) obj;
	                map.clear();
	                map.putAll(newenv);
	            }
	        }
	    }
	}
	
	/**
	 * This will ensure that the HikariDataSource and HikariConfig class are empty and mocked out
	 * respectively when they are created.
	 * 
	 * To use this method, make sure you annotate your test class with
	 <code>@RunWith(PowerMockRunner.class)</code>
	 * and <code>@PrepareForTest(Config.class)</code>
	 */
	
	/* public static void mockDataSource() throws Exception {
		// Replace the HikariConfig with a mock, for when it gets created in Config.java
		HikariConfig mock = PowerMock.createNiceMock(HikariConfig.class);
		// Allow as many config objects to be created as needed, and they will all be set to the mock
		PowerMock.expectNew(HikariConfig.class).andReturn(mock).anyTimes();
		PowerMock.replay(mock, HikariConfig.class);
		
		// Prevent the Datasource from initialising, for when it gets created in Config.java
		// We can't just replace it with a mock live above because it takes in an object.
		suppress(constructor(HikariDataSource.class, HikariConfig.class));
	} */
}
