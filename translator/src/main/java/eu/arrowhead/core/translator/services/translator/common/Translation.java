package eu.arrowhead.core.translator.services.translator.common;

import javax.servlet.http.HttpServletResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

public class Translation {

  public static enum Protocol {
    UNKOWN(0), HTTP(1), COAP(2), MQTT(3), WS(4);

    public final int value;

    Protocol(int value) {
      this.value = value;
    }

    public static Protocol valueOf(final int value) {
      switch (value) {
        case 1:
          return HTTP;
        case 2:
          return COAP;
        case 3:
          return MQTT;
        case 4:
          return WS;
        default:
          return UNKOWN;
      }
    }
  }

  public static enum ContentType {
    ANY(0),
    TEXT(1),
    XML(2),
    JSON(3),
    CBOR(4);

    public final int value;

    ContentType(int value) {
      this.value = value;
    }

    public static ContentType valueOf(final int value) {
      switch (value) {
        case 1:
          return TEXT;
        case 2:
          return XML;
        case 3:
          return JSON;
        case 4:
          return CBOR;
        default:
          return ANY;
      }
    }
  }

  public static String contentFormatFromCoap(int cf) {
    switch (cf) {
      case 0:
        return "text/plain;charset=utf-8";
      case 40:
        return "application/link-format";
      case 41:
        return "application/xml";
      case 42:
        return "application/octet-stream";
      case 47:
        return "application/exi";
      case 50:
        return "application/json";
      default:
        return "text/plain";
    }
  }

  public static int contentFormatToCoap(String cf) {
    switch (cf) {
      case "text/plain":
        return 0;
      case "application/link-format":
        return 40;
      case "application/xml":
        return 41;
      case "application/octet-stream":
        return 42;
      case "application/exi":
        return 47;
      case "application/json":
        return 50;
      default:
        return 0;
    }
  }

  public static int statusFromCoap(ResponseCode rc) {
    switch (rc) {

      // 2.xx
      case CREATED:
        return HttpServletResponse.SC_CREATED;
      case DELETED:
        return HttpServletResponse.SC_NO_CONTENT;
      case VALID:
        return HttpServletResponse.SC_NOT_MODIFIED;
      case CHANGED:
        return HttpServletResponse.SC_NO_CONTENT;
      case CONTENT:
        return HttpServletResponse.SC_OK;

      // 4.xx
      case BAD_REQUEST:
        return HttpServletResponse.SC_BAD_REQUEST;
      case UNAUTHORIZED:
        return HttpServletResponse.SC_UNAUTHORIZED;
      case BAD_OPTION:
        return HttpServletResponse.SC_BAD_REQUEST;
      case FORBIDDEN:
        return HttpServletResponse.SC_FORBIDDEN;
      case NOT_FOUND:
        return HttpServletResponse.SC_NOT_FOUND;
      case METHOD_NOT_ALLOWED:
        return HttpServletResponse.SC_METHOD_NOT_ALLOWED;
      case NOT_ACCEPTABLE:
        return HttpServletResponse.SC_NOT_ACCEPTABLE;
      case PRECONDITION_FAILED:
        return HttpServletResponse.SC_PRECONDITION_FAILED;
      case REQUEST_ENTITY_TOO_LARGE:
        return HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE;
      case UNSUPPORTED_CONTENT_FORMAT:
        return HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE;

      // 5.xx
      case INTERNAL_SERVER_ERROR:
        return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
      case NOT_IMPLEMENTED:
        return HttpServletResponse.SC_NOT_IMPLEMENTED;
      case BAD_GATEWAY:
        return HttpServletResponse.SC_BAD_GATEWAY;
      case SERVICE_UNAVAILABLE:
        return HttpServletResponse.SC_SERVICE_UNAVAILABLE;
      case GATEWAY_TIMEOUT:
        return HttpServletResponse.SC_GATEWAY_TIMEOUT;

      // Others - No way to match
      default:
        return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }
  }

  public static ResponseCode statusToCoap(int status) {
    switch (status) {

      // 2xx
      case HttpServletResponse.SC_OK:
        return ResponseCode.CONTENT;
      case HttpServletResponse.SC_CREATED:
        return ResponseCode.CREATED;
      case HttpServletResponse.SC_NO_CONTENT:
        return ResponseCode.CHANGED;

      // 3xx
      case HttpServletResponse.SC_NOT_MODIFIED:
        return ResponseCode.VALID;

      // 4xx
      case HttpServletResponse.SC_BAD_REQUEST:
        return ResponseCode.BAD_REQUEST;
      case HttpServletResponse.SC_UNAUTHORIZED:
        return ResponseCode.UNAUTHORIZED;
      case HttpServletResponse.SC_FORBIDDEN:
        return ResponseCode.FORBIDDEN;
      case HttpServletResponse.SC_NOT_FOUND:
        return ResponseCode.NOT_FOUND;
      case HttpServletResponse.SC_METHOD_NOT_ALLOWED:
        return ResponseCode.METHOD_NOT_ALLOWED;
      case HttpServletResponse.SC_NOT_ACCEPTABLE:
        return ResponseCode.FORBIDDEN;
      case HttpServletResponse.SC_PRECONDITION_FAILED:
        return ResponseCode.PRECONDITION_FAILED;
      case HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE:
        return ResponseCode.REQUEST_ENTITY_TOO_LARGE;
      case HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE:
        return ResponseCode.UNSUPPORTED_CONTENT_FORMAT;

      // 5xx
      case HttpServletResponse.SC_INTERNAL_SERVER_ERROR:
        return ResponseCode.INTERNAL_SERVER_ERROR;
      case HttpServletResponse.SC_NOT_IMPLEMENTED:
        return ResponseCode.NOT_IMPLEMENTED;
      case HttpServletResponse.SC_BAD_GATEWAY:
        return ResponseCode.BAD_GATEWAY;
      case HttpServletResponse.SC_SERVICE_UNAVAILABLE:
        return ResponseCode.SERVICE_UNAVAILABLE;
      case HttpServletResponse.SC_GATEWAY_TIMEOUT:
        return ResponseCode.GATEWAY_TIMEOUT;

      // Others - No way to match
      default:
        return ResponseCode.INTERNAL_SERVER_ERROR;
    }
  }

}
