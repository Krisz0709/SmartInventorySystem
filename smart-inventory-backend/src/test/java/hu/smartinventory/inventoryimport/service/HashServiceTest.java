package hu.smartinventory.inventoryimport.service;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class HashServiceTest {

    private final HashService hashService = new HashService();

    @Test
    void shouldReturnSameHashForSameContent() throws IOException {

        byte[] content = "hello".getBytes(StandardCharsets.UTF_8);

        String firstHash = hashService.sha256(
                new ByteArrayInputStream(content)
        );

        String secondHash = hashService.sha256(
                new ByteArrayInputStream(content)
        );

        assertEquals(firstHash, secondHash);
    }

    @Test
    void shouldReturnDifferentHashForDifferentContent() throws IOException {

        String firstHash = hashService.sha256(
                new ByteArrayInputStream(
                        "hello".getBytes(StandardCharsets.UTF_8)
                )
        );

        String secondHash = hashService.sha256(
                new ByteArrayInputStream(
                        "Hello".getBytes(StandardCharsets.UTF_8)
                )
        );

        assertNotEquals(firstHash, secondHash);
    }
}