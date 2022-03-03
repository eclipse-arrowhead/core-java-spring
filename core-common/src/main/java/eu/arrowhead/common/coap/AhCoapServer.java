package eu.arrowhead.common.coap;

import eu.arrowhead.common.coap.configuration.CoapServerConfiguration;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.elements.UDPConnector;
import org.eclipse.californium.elements.util.SslContextUtil;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.CertificateType;

public class AhCoapServer extends CoapServer {

    // =================================================================================================
    // members
    private final Logger logger = LogManager.getLogger(AhCoapServer.class);

    public AhCoapServer(CoapServerConfiguration configuration) {
        logger.info(String.format("CoAP Server [address:%s] [port:%d] [secured:%s]",
                configuration.getAddress(),
                configuration.getPort(),
                configuration.isSecured()));

        if (configuration.isSecured()) {
            logger.info(String.format("Security [ServerName:%s] [keyStore:%s] [TrustAlias:%s] [TrustStore:%s]",
                    configuration.getCredentials().getServerName(),
                    configuration.getCredentials().getKeyStorePath(),
                    configuration.getCertificates().getTrustName(),
                    configuration.getCertificates().getTrustStorePath()));
        }

        addEndpoint(
                configuration.isSecured()
                        ? createSecuredEndPoint(configuration)
                        : createUnsecuredEndPoint(configuration));
    }

    // =================================================================================================
    // assistant methods
    // -------------------------------------------------------------------------------------------------
    private Endpoint createUnsecuredEndPoint(CoapServerConfiguration configuration) {
        CoapEndpoint.Builder builder = new CoapEndpoint.Builder();
        UDPConnector udpConnector = new UDPConnector(
                new InetSocketAddress(configuration.getAddress(), configuration.getPort()));
        builder.setConnector(udpConnector);
        return builder.build();
    }

    // -------------------------------------------------------------------------------------------------
    private Endpoint createSecuredEndPoint(CoapServerConfiguration configuration) {
        DtlsConnectorConfig.Builder builder = new DtlsConnectorConfig.Builder();
        builder.setAddress(new InetSocketAddress(configuration.getAddress(), configuration.getPort()));
        builder.setRecommendedCipherSuitesOnly(false);
        try {
            SslContextUtil.Credentials serverCredentials = SslContextUtil.loadCredentials(
                    SslContextUtil.CLASSPATH_SCHEME + configuration.getCredentials().getKeyStorePath().substring(10),
                    configuration.getCredentials().getServerName(),
                    configuration.getCredentials().getKeyStorePassword().toCharArray(),
                    configuration.getCredentials().getKeyPassword().toCharArray());
            Certificate[] trustedCertificates = SslContextUtil.loadTrustedCertificates(
                    SslContextUtil.CLASSPATH_SCHEME + configuration.getCertificates().getTrustStorePath().substring(10),
                    "coap-root",
                    configuration.getCertificates().getTrustStorePassword().toCharArray());
            builder.setTrustStore(trustedCertificates);
            builder.setRpkTrustAll();
            List<CertificateType> types = new ArrayList<>();
            types.add(CertificateType.RAW_PUBLIC_KEY);
            types.add(CertificateType.X_509);
            builder.setIdentity(serverCredentials.getPrivateKey(), serverCredentials.getCertificateChain(), types);

        } catch (GeneralSecurityException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        }

        DTLSConnector connector = new DTLSConnector(builder.build());
        CoapEndpoint.Builder coapBuilder = new CoapEndpoint.Builder();

        coapBuilder.setConnector(connector);

        return coapBuilder.build();
    }
}
