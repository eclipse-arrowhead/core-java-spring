package eu.arrowhead.common.filter.thirdparty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Third party code from: https://stackoverflow.com/a/54258488
 * @author granadaCoder (https://stackoverflow.com/users/214977/granadacoder)
 *
 */
public class MultiReadRequestWrapper extends HttpServletRequestWrapper {

	//=================================================================================================
	// members

    private final String body;

    //=================================================================================================
	// methods
    
    //-------------------------------------------------------------------------------------------------
	public MultiReadRequestWrapper(final HttpServletRequest request) throws IOException {
    	super(request);
	    body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
	}
    
    //-------------------------------------------------------------------------------------------------
	public String getCachedBody() { return body; }
	
    //-------------------------------------------------------------------------------------------------
	@Override
	public ServletInputStream getInputStream() throws IOException {
    	return new CustomServletInputStream(body.getBytes());
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(this.getInputStream()));
	}
}