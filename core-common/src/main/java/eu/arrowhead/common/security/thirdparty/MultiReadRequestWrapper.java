package eu.arrowhead.common.security.thirdparty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class MultiReadRequestWrapper extends HttpServletRequestWrapper {

    private final String body;

    public MultiReadRequestWrapper(final HttpServletRequest request) throws IOException {
    	super(request);
	    body = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
//	    final BufferedReader bufferedReader = request.getReader();
//	    String line;
//	    while ((line = bufferedReader.readLine()) != null){
//	    	body += line;
//	    }
	}
    
    public String getCachedBody() { 
    	return body;
    }
	
    @Override
	public ServletInputStream getInputStream() throws IOException {
    	CustomServletInputStream kid = new CustomServletInputStream(body.getBytes());
	    return kid;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(this.getInputStream()));
	}
}