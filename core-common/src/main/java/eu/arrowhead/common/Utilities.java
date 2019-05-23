package eu.arrowhead.common;

public class Utilities {
	
	public static boolean isEmpty(final String str) {
		return str == null || str.isBlank();
	}

	private Utilities() {
		throw new UnsupportedOperationException();
	}
}
