package com.eumakase.eumakase.security;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;

public class JasyptEncryptor {
    public static void main(String[] args) {
        String key = "cbejzjem2023!";
        StandardPBEStringEncryptor pbeEnc = new StandardPBEStringEncryptor();
        pbeEnc.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        pbeEnc.setPassword(key);
        pbeEnc.setIvGenerator(new RandomIvGenerator());
        System.out.println("Encrypted Text: " + pbeEnc.encrypt(""));
    }
}
