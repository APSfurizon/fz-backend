package net.furizon.backend.infrastructure.configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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
            PEMKeyPair ukp = (PEMKeyPair) object;
            kp = converter.getKeyPair(ukp);
        }
        //Not needed I suppose?
        //KeyFactory keyFac = KeyFactory.getInstance("RSA");
        //RSAPrivateCrtKeySpec privateKey = keyFac.getKeySpec(kp.getPrivate(), RSAPrivateCrtKeySpec.class);

        return kp.getPrivate();
    }
}
