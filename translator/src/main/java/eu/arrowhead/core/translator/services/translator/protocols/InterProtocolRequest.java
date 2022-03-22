package eu.arrowhead.core.translator.services.translator.protocols;

import org.eclipse.jetty.http.MimeTypes;

public class InterProtocolRequest {
  private final String path;
  private final String queries;
  private final String contentType;
  private final byte[] content;

  public InterProtocolRequest(String path, String queries, MimeTypes.Type contentType, byte[] content) {
        this.path = path;
        this.queries = queries;
        this.contentType = contentType.asString();
        this.content = content;
    }

  public InterProtocolRequest(String path, String queries, String contentType, byte[] content) {
        this.path = path;
        this.queries = queries;
        this.contentType = (contentType == null) ? MimeTypes.Type.TEXT_PLAIN.asString() : contentType;
        this.content = content;
    }

  public String getPath() {
    return path;
  }

  public String getQueries() {
    return queries;
  }

  public String getContentType() {
    return contentType;
  }

  public byte[] getContent() {
    return content;
  }
}
