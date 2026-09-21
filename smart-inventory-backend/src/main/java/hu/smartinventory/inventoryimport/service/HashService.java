package hu.smartinventory.inventoryimport.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.nio.charset.StandardCharsets;

@Service
public class HashService {

    public String sha256(InputStream inputStream) throws IOException {

        MessageDigest digest = createSha256Digest();

        byte[] buffer = new byte[8192];

        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
        }

        byte[] hashBytes = digest.digest();

        return HexFormat.of().formatHex(hashBytes);
    }

    private MessageDigest createSha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is not available.",
                    e
            );
        }
    }

    public String sha256(String value) {

        MessageDigest digest = createSha256Digest();

        byte[] hashBytes = digest.digest(
                value.getBytes(StandardCharsets.UTF_8)
        );

        return HexFormat.of().formatHex(hashBytes);
    }
}