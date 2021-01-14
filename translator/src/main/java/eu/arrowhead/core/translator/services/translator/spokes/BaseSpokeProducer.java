package eu.arrowhead.core.translator.services.translator.spokes;

public interface BaseSpokeProducer extends BaseSpoke {

    public void close();

    public String getAddress();
}
