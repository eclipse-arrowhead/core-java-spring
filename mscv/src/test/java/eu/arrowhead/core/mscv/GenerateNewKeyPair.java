package eu.arrowhead.core.mscv;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.spec.RSAPrivateCrtKeySpec;

import org.bouncycastle.jcajce.provider.asymmetric.rsa.KeyPairGeneratorSpi;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

public class GenerateNewKeyPair {

    public static void main(String[] args) throws FileNotFoundException {
        final KeyPairGeneratorSpi rsa = new KeyPairGeneratorSpi();
        rsa.initialize(2048);
        final KeyPair keyPair = rsa.generateKeyPair();

        keyPair.getPrivate().getEncoded();
        FileOutputStream privateKeyStream = new FileOutputStream("mscv.private");


    }
}
