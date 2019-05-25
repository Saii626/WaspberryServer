package app.saikat.WaspberryServer.ServerComponents.Logging;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Priority;

public class LoggingConfigurations {
    private List<Appenders> enabledAppenders;

    private String consolePattern;
    private String consolLevel; 

    private String filePattern;
    private String fileName;
    private String fileLevel;

    public List<Appenders> getEnabledAppenders() {
        return enabledAppenders;
    }

    public String getConsolePattern() {
        return consolePattern;
    }
    
    public String getConsolLevel() {
        return consolLevel;
    }

    public String getFilePattern() {
        return filePattern;
    }
    
    public String getFileLevel() {
        return fileLevel;
    }
    
    public String getFileName() {
        return fileName;
    }

    public static LoggingConfigurations getDefault() {
        LoggingConfigurations configurations = new LoggingConfigurations();

        List<Appenders> appenders = new ArrayList<>();
        appenders.add(Appenders.CONSOLE);
        appenders.add(Appenders.FILE);
        configurations.enabledAppenders = appenders;

        configurations.consolLevel = "DEBUG";
        configurations.consolePattern = "%d{yy-MM-dd HH:mm:ss:SSS} %5p %t %c{2}:%L - %m%n";

        configurations.fileLevel = "WARN";
        configurations.filePattern = "%d{yy-MM-dd HH:mm:ss:SSS} %5p %t %c{2}:%L - %m%n";
        configurations.fileName = "warn-log.log";

        return configurations;
    }

}