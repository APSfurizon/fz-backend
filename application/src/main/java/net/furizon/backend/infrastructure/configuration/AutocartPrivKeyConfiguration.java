package net.furizon.backend.infrastructure.configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.FileReader;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "pretix.autocart")
public class AutocartPrivKeyConfiguration {
    private String privKeyPath;
    private String privKeyPassword;

    //Generate key with:
    // `openssl genrsa -out priv-key-autocart.rsa 1024`
    // `openssl rsa -in priv-key-autocart.rsa -pubout -out pub-key-autocart.rsa`


    @Bean
    @Primary
    PrivateKey privateRSAKeyConfiguration() throws IOException {
        log.info("Loading private RSA key from file '{}'. Cwd: '{}'", privKeyPath, System.getProperty("user.dir"));

        //From https://stackoverflow.com/questions/22920131/read-an-encrypted-private-key-with-bouncycastle-spongycastle
        Security.addProvider(new BouncyCastleProvider());
        PEMParser pemParser = new PEMParser(new FileReader(privKeyPath));
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
        Object object = pemParser.readObject();
        pemParser.close();
        KeyPair kp;
        if (object instanceof PEMEncryptedKeyPair ckp) {
            PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(privKeyPassword.toCharArray());
            kp = converter.getKeyPair(ckp.decryptKeyPair(decProv));
        } else {
            if (object instanceof PEMKeyPair ukp) {
                kp = converter.getKeyPair(ukp);
            } else if (object instanceof PrivateKeyInfo ckp) {
                return converter.getPrivateKey(ckp);
            } else {
                throw new IllegalArgumentException("Unsupported key object type: " + object.getClass().getName());
            }
        }

        return kp.getPrivate();
    }
}
