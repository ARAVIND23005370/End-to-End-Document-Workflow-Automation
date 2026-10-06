package com.e2edocs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@SpringBootApplication
@EnableAsync
public class E2EDocsApplication {

    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");
        loadDotEnv();
        SpringApplication.run(E2EDocsApplication.class, args);
    }

    private static void loadDotEnv() {
        Path envPath = Paths.get(".env");
        if (Files.exists(envPath)) {
            try {
                List<String> lines = Files.readAllLines(envPath);
                for (String line : lines) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#") && line.contains("=")) {
                        int idx = line.indexOf('=');
                        String key = line.substring(0, idx).trim();
                        String value = line.substring(idx + 1).trim();
                        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
                            value = value.substring(1, value.length() - 1);
                        }
                        if (System.getProperty(key) == null && System.getenv(key) == null) {
                            System.setProperty(key, value);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }
}

