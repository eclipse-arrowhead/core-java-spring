package eu.arrowhead.core.translator.services.translator.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TranslatorSetup {

    private final String producerName;
    private final String producerAddress;
    private final String consumerName;
    private final String consumerAddress;

    @JsonCreator
    public TranslatorSetup(
            @JsonProperty("producerName") String producerName,
            @JsonProperty("producerAddress") String producerAddress,
            @JsonProperty("consumerName") String consumerName,
            @JsonProperty("consumerAddress") String consumerAddress) {
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
