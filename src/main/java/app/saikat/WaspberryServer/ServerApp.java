package app.saikat.WaspberryServer;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ServerApp {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(ServerApp.class, args);
    }
}