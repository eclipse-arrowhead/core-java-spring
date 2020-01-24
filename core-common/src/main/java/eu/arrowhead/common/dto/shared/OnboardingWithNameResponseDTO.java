package eu.arrowhead.common.dto.shared;

public class OnboardingWithNameResponseDTO
{
    //=================================================================================================
    // members
    private ServiceEndpoint deviceRegistry;
    private ServiceEndpoint systemRegistry;
    private ServiceEndpoint serviceRegistry;

    private String onboardingCertificate;
    private String intermediateCertificate;
    private String rootCertificate;
    private String keyAlgorithm;
    private String keyFormat;
    private byte[] privateKey;
    private byte[] publicKey;


    //=================================================================================================
    // methods

    //-------------------------------------------------------------------------------------------------
    public ServiceEndpoint getDeviceRegistry()
    {
        return deviceRegistry;
    }

    //-------------------------------------------------------------------------------------------------
    public void setDeviceRegistry(final ServiceEndpoint deviceRegistry)
    {
        this.deviceRegistry = deviceRegistry;
    }

    //-------------------------------------------------------------------------------------------------
    public ServiceEndpoint getSystemRegistry()
    {
        return systemRegistry;
    }

    //-------------------------------------------------------------------------------------------------
    public void setSystemRegistry(final ServiceEndpoint systemRegistry)
    {
        this.systemRegistry = systemRegistry;
    }

    //-------------------------------------------------------------------------------------------------
    public ServiceEndpoint getServiceRegistry()
    {
        return serviceRegistry;
    }

    //-------------------------------------------------------------------------------------------------
    public void setServiceRegistry(final ServiceEndpoint serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    //-------------------------------------------------------------------------------------------------
    public String getOnboardingCertificate()
    {
        return onboardingCertificate;
    }

    //-------------------------------------------------------------------------------------------------
    public void setOnboardingCertificate(final String onboardingCertificate)
    {
        this.onboardingCertificate = onboardingCertificate;
    }

    //-------------------------------------------------------------------------------------------------
    public String getIntermediateCertificate()
    {
        return intermediateCertificate;
    }

    //-------------------------------------------------------------------------------------------------
    public void setIntermediateCertificate(final String intermediateCertificate)
    {
        this.intermediateCertificate = intermediateCertificate;
    }

    //-------------------------------------------------------------------------------------------------
    public String getRootCertificate()
    {
        return rootCertificate;
    }

    //-------------------------------------------------------------------------------------------------
    public void setRootCertificate(final String rootCertificate)
    {
        this.rootCertificate = rootCertificate;
    }

    //-------------------------------------------------------------------------------------------------
    public String getKeyAlgorithm()
    {
        return keyAlgorithm;
    }

    //-------------------------------------------------------------------------------------------------
    public void setKeyAlgorithm(final String keyAlgorithm)
    {
        this.keyAlgorithm = keyAlgorithm;
    }

    //-------------------------------------------------------------------------------------------------
    public String getKeyFormat()
    {
        return keyFormat;
    }

    //-------------------------------------------------------------------------------------------------
    public void setKeyFormat(final String keyFormat)
    {
        this.keyFormat = keyFormat;
    }

    //-------------------------------------------------------------------------------------------------
    public byte[] getPrivateKey()
    {
        return privateKey;
    }

    //-------------------------------------------------------------------------------------------------
    public void setPrivateKey(final byte[] privateKey)
    {
        this.privateKey = privateKey;
    }

    //-------------------------------------------------------------------------------------------------
    public byte[] getPublicKey()
    {
        return publicKey;
    }

    //-------------------------------------------------------------------------------------------------
    public void setPublicKey(final byte[] publicKey)
    {
        this.publicKey = publicKey;
    }
}
