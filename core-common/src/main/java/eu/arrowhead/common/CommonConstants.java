package eu.arrowhead.common;

public class CommonConstants {
	
	public static final String APPLICATION_PROPERTIES = "application.properties";
	
	public static final String DATABASE_URL = "spring.datasource.url";
	public static final String DATABASE_USER = "spring.datasource.username";
	public static final String DATABASE_PASSWORD = "spring.datasource.password";
	public static final String DATABASE_DRIVER_CLASS = "spring.datasource.driver-class-name"; 

	private CommonConstants() {
		throw new UnsupportedOperationException();
	}
}
