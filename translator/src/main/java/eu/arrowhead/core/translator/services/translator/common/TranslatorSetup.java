package eu.arrowhead.core.translator.services.translator.common;

public class TranslatorSetup {

    private final String producerName;
    private final String producerAddress;
    private final String consumerName;
    private final String consumerAddress;

    public TranslatorSetup(String producerName, String producerAddress, String consumerName, String consumerAddress) {
        this.producerName = producerName;
        this.producerAddress = producerAddress;
        this.consumerName = consumerName;
        this.consumerAddress = consumerAddress;
    }

    public String getProducerName() {
        return producerName;
    }

    public String getProducerAddress() {
        return producerAddress;
    }

    public String getConsumerName() {
        return consumerName;
    }

    public String getConsumerAddress() {
        return consumerAddress;
    }
}
