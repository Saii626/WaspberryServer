package app.saikat.WaspberryServer;

import java.io.File;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import app.saikat.ConfigurationManagement.ConfigurationManagerInstanceHandler;
import app.saikat.ConfigurationManagement.interfaces.ConfigurationManager;

@SpringBootApplication
// @ComponentScan(basePackages = "app.saikat.WaspberryServer")
public class ServerApp {

    
    
    public static void main(String[] args) throws IOException {
        SpringApplication.run(ServerApp.class, args);
    }
}