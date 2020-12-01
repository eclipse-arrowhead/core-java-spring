package eu.arrowhead.core.translator.services.translator.spokes;

public interface BaseSpoke {

    public void in(BaseContext context);

    public void setNextSpoke(Object nextSpoke);

    public int getLastActivity();

    public void clearActivity();
}
