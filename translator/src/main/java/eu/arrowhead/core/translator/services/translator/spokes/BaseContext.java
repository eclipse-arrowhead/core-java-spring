package eu.arrowhead.core.translator.services.translator.spokes;

import eu.arrowhead.core.translator.services.translator.common.TranslatorDef.Method;

public class BaseContext {

    //=================================================================================================
    // members
    private int key = 0;
    private String content;
    private String contentType;
    private Method method;
    private String path;
    int cacheTimeout;

    //=================================================================================================
    // methods
    //-------------------------------------------------------------------------------------------------
    public int getKey() {
        if (key == 0) {
            this.key = (int) (Math.random() * 1000000);
        }
        return key;
    }

    //-------------------------------------------------------------------------------------------------
    @SuppressWarnings("unused")
    private void setKey(int key) {
        this.key = key;
    }

    //-------------------------------------------------------------------------------------------------
    public String getContent() {
        return content;
    }

    //-------------------------------------------------------------------------------------------------
    public void setContent(String content) {
        this.content = content;
    }

    //-------------------------------------------------------------------------------------------------
    public String getContentType() {
        return contentType;
    }

    //-------------------------------------------------------------------------------------------------
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    //-------------------------------------------------------------------------------------------------
    public Method getMethod() {
        return method;
    }

    //-------------------------------------------------------------------------------------------------
    public void setMethod(Method method) {
        this.method = method;
    }

    //-------------------------------------------------------------------------------------------------
    public String getPath() {
        return path;
    }

    //-------------------------------------------------------------------------------------------------
    public void setPath(String path) {
        this.path = path;
    }
}
