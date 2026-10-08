package com.mymart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class MymartApplication {

    public static void main(String[] args) {
        SpringApplication.run(MymartApplication.class, args);
    }
}
