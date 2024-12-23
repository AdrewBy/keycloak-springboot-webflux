package com.ustsinau.springsecuritykeycloakapi.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JavaTimeModule());
    }

    public static <T> T readJsonFromFile(String filePath, Class<T> clazz) {
        try {
            return objectMapper.readValue(new File(filePath), clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readJsonFromFile(String path) {
        try {
            // Чтение содержимого файла как строки
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            throw new RuntimeException("Error reading JSON from file: " + path, e);
        }
    }
}
