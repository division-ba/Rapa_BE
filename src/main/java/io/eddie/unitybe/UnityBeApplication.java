package io.eddie.unitybe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class UnityBeApplication {

    static void main(String[] args) {
        SpringApplication.run(UnityBeApplication.class, args);
    }

}
