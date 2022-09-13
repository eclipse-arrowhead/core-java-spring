package eu.arrowhead.core.translator.services.translator.protocols;

import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.MimeTypes;

public class InterProtocolResponse {

    private final String contentType;
    private final int statusCode;
    private byte[] content;

    public InterProtocolResponse() {
        contentType = MimeTypes.Type.TEXT_PLAIN.asString();
        statusCode = HttpServletResponse.SC_NOT_IMPLEMENTED;
        content = "Not Implemented".getBytes();
    }

    public InterProtocolResponse(String contentType, int statusCode, byte[] content) {
        this.contentType = contentType;
        this.statusCode = statusCode;
        this.content = content;
    }

    public InterProtocolResponse(MimeTypes.Type contentType, int statusCode, byte[] content) {
        this.contentType = contentType.asString();
        this.statusCode = statusCode;
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public byte[] getContent() {
        return content;
    }

    public String getContentAsString() {
        return new String(content);
    }

}
