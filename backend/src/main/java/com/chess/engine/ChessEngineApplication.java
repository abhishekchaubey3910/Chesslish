package com.chess.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ChessEngineApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChessEngineApplication.class, args);
    }
}
