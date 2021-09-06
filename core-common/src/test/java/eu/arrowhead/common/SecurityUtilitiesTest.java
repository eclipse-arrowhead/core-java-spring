package eu.arrowhead.common;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import eu.arrowhead.common.dto.shared.CertificateCreationRequestDTO;
import eu.arrowhead.common.dto.shared.KeyPairDTO;
import org.junit.Test;

public class SecurityUtilitiesTest {

    private static final String TEST1_PKCS8_PRIVATE_KEY =
            "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC4IeGH1hdAfmwX" +
                    "du07vxhFdztdMn1kjbcHOrWR8mntoAVyOX4UqA41seyO2SRqqwyGdtYdV9q5J0eE" +
                    "BMJY/xfwxXSMeHonDmCi6WfscI6y3tPmRa/JnBO9CUWOuPONg8D0PAVxTv/I3SeG" +
                    "C/OUVwClW36ec/wY1RzQZmGoXFPAfmNRZa49b4fmvujLi9VsyMIodrtrRTcnA4bB" +
                    "V9E7WuxI1glgnrUtugwikmHG/s903J8e6UN4nMaRhZKwdT1F5OTPn6/vYLyTemy3" +
                    "VscdapMdf26XkvmWyJhRuKEztrddZ3iu7Bfbk/jKuO5sks044OONR2vp8lRa2Gd7" +
                    "Kq7EDwk3AgMBAAECggEAOZi/h1H8q/vfq+dTheDcor4NH7Xva0i4+9Xtfd2qLN5l" +
                    "VbmOKr/20ez55iUeMw+WN2lsyHj2vfv/btheQqBoyLO7JUV6UEU7DTqde2Qp/uGc" +
                    "vucFebcMpZEE5QQNizKvZLZxrWPctuQWvON4KeqdFRxtmvsE3G+tN7MO0S8wM4HM" +
                    "9y+DMm2/fOpvu/HwOUdAjCwINRnc451tTxLM1CmeNyFiJBIO73kAs6kJ1E1I1oKH" +
                    "8yO9/6CkmDLeCdpezLtNqUpGmBdAhnaSBvv/zQcLDsJzrn+DynOqZIyW0W+ngXfC" +
                    "j0P9GNkzkI9GXVEDs/lU3QPzVeWqsyqmqJccBKnY4QKBgQDusfP88BVSMl4iUusB" +
                    "bYtSl8/sUY+/vQETkxA7IThgQkmBPpHJPKJ3s5yZuPvkDYlc8GaMJtyqvB947pCp" +
                    "jdRlqEoaDS7U/inUYRvbHLzMJIY5bjmZSOh7KeuCchYPcJwKiAuR5ohc/weiARDC" +
                    "ifuesftrPd1pMhcpiPgyOfaj0wKBgQDFe0YBZlDx4zVPQgK6+zNSpoXi6rqWKmSK" +
                    "5r+FO0WiC3L9pmsxJ7CHPO/rJ1kFbka9cL/xK7ReDvMI9vxlRoyLrxo+sGuj+lPV" +
                    "9Qy7b+9DIPct21XZ3GSfuavpFV1o7CeKPJLxypupqP2v9yEfpn2o4S5TxJhh+5UD" +
                    "KbiMRuM6jQKBgQCKEtpGWEKdF5GoXTqfytQq5LJbyxpSGWzD+/AJpD/OPsnyP1Vp" +
                    "iO+aOoW9zmm3E7V/03geXmSLRuMVuzpnOinA3EZxJ2sNvjBIMBxC9S6pD/+svPwI" +
                    "lUoV5Lvc602jhMpetS+sKCR9VvD9X4xoXBT3SZFcCZ9IOLd5SUf5Fdp8DQKBgAdX" +
                    "5ymEKXxlK4VP+hQD5Uusf4wJmHifCPAVVofYxWUaMeN2vcOtKyZMQMuowemuyRig" +
                    "SbLpIiLhQW5S6N3HOtCy6THtk4MceuOY4ilSJz6Cyk49OrsINAGwgNEBB2EwbuP4" +
                    "DqrJEvLDIvPZJ7Uqlr+h5/wbcmMqXMcqVP96X1a9AoGAGaiWIyXj9KKuEySWVc0c" +
                    "ovgex2jMNtUI+2IIWXvBtuCq+8gkAnpa7fWlsra1JcDifc4UzgjqeJJat/07WcwA" +
                    "zYQR3J70PEXnjc0gAjArrDdsvcXeRrgVgLW7bZtmC+3+anW6G8ZRZMilPeopSa3V" +
                    "RdgN4la7/C7AIrGls85tq64=";

    private static final String TEST1_X509_PUBLIC_KEY =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuCHhh9YXQH5sF3btO78Y" +
                    "RXc7XTJ9ZI23Bzq1kfJp7aAFcjl+FKgONbHsjtkkaqsMhnbWHVfauSdHhATCWP8X" +
                    "8MV0jHh6Jw5gouln7HCOst7T5kWvyZwTvQlFjrjzjYPA9DwFcU7/yN0nhgvzlFcA" +
                    "pVt+nnP8GNUc0GZhqFxTwH5jUWWuPW+H5r7oy4vVbMjCKHa7a0U3JwOGwVfRO1rs" +
                    "SNYJYJ61LboMIpJhxv7PdNyfHulDeJzGkYWSsHU9ReTkz5+v72C8k3pst1bHHWqT" +
                    "HX9ul5L5lsiYUbihM7a3XWd4ruwX25P4yrjubJLNOODjjUdr6fJUWthneyquxA8J" +
                    "NwIDAQAB";

    private static final String TEST2_PKCS1_PRIVATE_KEY =
            "MIIEpAIBAAKCAQEAtyZGE9RE1bkR/ee5gW6lbNWFQH25EKblVhHtd+onpaxRySzt" +
                    "V6ZV2zDUsXv2ZtdpbuU0HOGlCjGFRFRARObVJCvHUbvC+2kJv4EZSWTDQhonAqsG" +
                    "1bjaM4PCpjMRy8Q4+/aMmk2RI8+pEUy+ENIeIN0xPhNY664dswefqJEVKtgrjVgQ" +
                    "Hsx9h7A0Z38VYfritiDcK7ys55b65dF5i4SMvNlFLzukl977jGKWzdrkJ2Tr/9VM" +
                    "9ODu/AMuWK2Ugm4JHojqqNzQUtfOBhd3iYA0ernQVw9ohekKCZF0ju2nap1Ds621" +
                    "RLlFNrLHjDfLFQqau0KMwSXb77tDd6pkLhkucwIDAQABAoIBAE49hoBgFQvsZPg/" +
                    "7uqRNxA4YxV63/1yHtTXEchBerB23fWAQOBJybG6uZIcr9WdPohGWC3iY1vobdMT" +
                    "3uTQa8to/Fw5RwGaDLUH1KKa7iMmAvZL076nmmeZaSUQ+hA/gkx5NfWRH2jaBJLE" +
                    "YwnSiVZmx+uJ3lnIZx2dKyKa4B0NxCD6FZ9/kdCCPw6g+f0hUPSIUOXw6E3mWWlu" +
                    "hXXCDK0BYLZPXrK17tP+TffngdVutNND+PKitNQSx/GcOMHUqWUp35Cgx+P3lc8y" +
                    "2Lsr7qZCJmJN3f7sUOlFe79NnJ2/xmngMUmLuiVzQQ6EyfvJMeTr4G9jzraKOuEK" +
                    "3oEpE0kCgYEA7soTIi8JiQl3VJQQCxt4exxAe2w3Kjmg3/jF/vN+owZJxnnyYVDR" +
                    "AJJ4NlGjDRkLqOjNXhtwH1RzAK88VLLqG505SkLr00HjIEM0zsSEdMa65d2ydc33" +
                    "9TQwxBKNaOId2Xz2RBQJPiUZwSMA3in0d8cuSFYs0Y/mQqO9+o3wP38CgYEAxFmV" +
                    "NPOQSUPNOABGWoL5jFChwcoQ7vZt7qv6WCV96/1L+GWt31NWkYC0IHn5Obf2QBBI" +
                    "4TXZ+F1rXXivmQR0TtodEElo0FPnEMzWR3XpGy952VNCJ2hAFezms4hekAK4RRDF" +
                    "87GD1ph75bSgDC0DDBC6mXsEnZ1HdkJ3M6+Uiw0CgYEAjOw68ICb+wDYKNUcxsFW" +
                    "kCkzMOPDhqrt82Ao0Xa4NNPgND8BuUbtoY3PXhc9x9wREoUZtkExIXxARpYkX8Qa" +
                    "/2MBTX9DF7EZro+bGa/Gj/g8kq4MxRNiE5bSdNSSGeTgQttJpxHABqn0khPrTjGq" +
                    "cgndmZkp6B8pgAiMdNLRh1cCgYA4bhKOtE1McRsCtDmD+85igC3s9vjk5Jf1lyGM" +
                    "wcGt2A9EZVySKW5gxN9/0e5Jo1A3WzbG2uY5FggsReoZjQksPE1MB/0Cmop27pfK" +
                    "091ZvcxEJESH9NMuOfaXtGKQ+ucgmlB52BOu0gYRn1a8CReuGQS35X4PGkuWdkWx" +
                    "6g1D5QKBgQDLUnb3Zgd3FvcNJJ+7LFPqhFzt9ihCU1rUw3vLCudVbq7h+0WcV7XY" +
                    "cRv1AEatKyHErIP9hzF6BXNthv3cKQnLOfaPBXD/F0+22QBk0Rp7eEfPTWpsWXXQ" +
                    "17LWGCUOdbIwa6qFas++lHon6LV4PPWG5xUbx+Jl/TguI4/4ZGCOuQ==";

    private static final String TEST2_X509_PUBLIC_KEY =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuCHhh9YXQH5sF3btO78Y" +
                    "RXc7XTJ9ZI23Bzq1kfJp7aAFcjl+FKgONbHsjtkkaqsMhnbWHVfauSdHhATCWP8X" +
                    "8MV0jHh6Jw5gouln7HCOst7T5kWvyZwTvQlFjrjzjYPA9DwFcU7/yN0nhgvzlFcA" +
                    "pVt+nnP8GNUc0GZhqFxTwH5jUWWuPW+H5r7oy4vVbMjCKHa7a0U3JwOGwVfRO1rs" +
                    "SNYJYJ61LboMIpJhxv7PdNyfHulDeJzGkYWSsHU9ReTkz5+v72C8k3pst1bHHWqT" +
                    "HX9ul5L5lsiYUbihM7a3XWd4ruwX25P4yrjubJLNOODjjUdr6fJUWthneyquxA8J" +
                    "NwIDAQAB";

    private final SecurityUtilities utilities;
    private final KeyFactory keyFactory;

    public SecurityUtilitiesTest() throws NoSuchAlgorithmException {
        keyFactory = KeyFactory.getInstance("RSA");
        this.utilities = new SecurityUtilities("RSA", 2048, new SSLProperties());
    }

    @Test
    public void rsaKeyTest1() {
        final CertificateCreationRequestDTO request = new CertificateCreationRequestDTO("client");
        final KeyPairDTO keyPairDTO = new KeyPairDTO(null, null, TEST1_X509_PUBLIC_KEY, TEST1_PKCS8_PRIVATE_KEY);
        request.setKeyPairDTO(keyPairDTO);
        utilities.extractOrGenerateKeyPair(request);
    }

    @Test
    public void rsaKeyTest2() {
        final CertificateCreationRequestDTO request = new CertificateCreationRequestDTO("client");
        final KeyPairDTO keyPairDTO = new KeyPairDTO(null, null, TEST2_X509_PUBLIC_KEY, TEST2_PKCS1_PRIVATE_KEY);
        request.setKeyPairDTO(keyPairDTO);
        utilities.extractOrGenerateKeyPair(request);
    }

    @Test
    public void rsaPublicKeyTest1() throws InvalidKeySpecException {
        final byte[] bytes = Base64.getDecoder().decode(TEST1_X509_PUBLIC_KEY);
        final EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(bytes);
        keyFactory.generatePublic(publicKeySpec);
    }

    @Test
    public void rsaPublicKeyTest2() throws InvalidKeySpecException {
        final byte[] bytes = Base64.getDecoder().decode(TEST2_X509_PUBLIC_KEY);
        final EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(bytes);
        keyFactory.generatePublic(publicKeySpec);
    }

    @Test
    public void rsaPrivateKeyTest1() throws InvalidKeySpecException {
        final byte[] bytes = Base64.getDecoder().decode(TEST1_PKCS8_PRIVATE_KEY);
        final EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(bytes);
        keyFactory.generatePrivate(privateKeySpec);
    }

    @Test
    public void rsaPrivateKeyTest2() throws GeneralSecurityException {
        byte[] bytes = Base64.getDecoder().decode(TEST2_PKCS1_PRIVATE_KEY);
        bytes = readPkcs1PrivateKey(bytes);
        final EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(bytes);
        keyFactory.generatePrivate(privateKeySpec);
    }

    private byte[] readPkcs1PrivateKey(byte[] pkcs1Bytes) throws GeneralSecurityException {
        // We can't use Java internal APIs to parse ASN.1 structures, so we build a PKCS#8 key Java can understand
        int pkcs1Length = pkcs1Bytes.length;
        int totalLength = pkcs1Length + 22;
        byte[] pkcs8Header = new byte[] {
                0x30, (byte) 0x82, (byte) ((totalLength >> 8) & 0xff), (byte) (totalLength & 0xff), // Sequence + total length
                0x2, 0x1, 0x0, // Integer (0)
                0x30, 0xD, 0x6, 0x9, 0x2A, (byte) 0x86, 0x48, (byte) 0x86, (byte) 0xF7, 0xD, 0x1, 0x1, 0x1, 0x5, 0x0, // Sequence: 1.2.840.113549.1.1.1, NULL
                0x4, (byte) 0x82, (byte) ((pkcs1Length >> 8) & 0xff), (byte) (pkcs1Length & 0xff) // Octet string + length
        };
        return join(pkcs8Header, pkcs1Bytes);
    }

    private byte[] join(byte[] byteArray1, byte[] byteArray2){
        byte[] bytes = new byte[byteArray1.length + byteArray2.length];
        System.arraycopy(byteArray1, 0, bytes, 0, byteArray1.length);
        System.arraycopy(byteArray2, 0, bytes, byteArray1.length, byteArray2.length);
        return bytes;
    }
}