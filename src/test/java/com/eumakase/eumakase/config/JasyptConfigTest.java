package com.eumakase.eumakase.config;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.jupiter.api.Test;
import java.io.IOException;

class JasyptConfigTest {
    @Test
    void privateKeyEN() throws IOException {
        StandardPBEStringEncryptor standardPBEStringEncryptor = new StandardPBEStringEncryptor();
        standardPBEStringEncryptor.setAlgorithm(JasyptConfig.ALGORITHM);
        standardPBEStringEncryptor.setPassword(JasyptConfig.KEY);

        String enc = standardPBEStringEncryptor.encrypt("");
        System.out.printf("enc = ENC(%s)\\n", enc);
        System.out.printf("dec = %s\\n", standardPBEStringEncryptor.decrypt(enc));
    }

    @Test
    void stringDE() {
        String privateKey = "cbejzjem2023!";
        StandardPBEStringEncryptor standardPBEStringEncryptor = new StandardPBEStringEncryptor();
        standardPBEStringEncryptor.setAlgorithm(JasyptConfig.ALGORITHM);
        standardPBEStringEncryptor.setPassword(JasyptConfig.KEY);

        String enc = standardPBEStringEncryptor.encrypt(privateKey);
        System.out.printf("enc = ENC(%s)\\n", enc);
        System.out.printf("dec = %s\\n", standardPBEStringEncryptor.decrypt(enc));
    }
}