package app.saikat.WaspberryServer.ServerComponents.Logging;

import java.io.IOException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import app.saikat.ConfigurationManagement.interfaces.ConfigurationManager;

@Configuration
public class Log {

    @Autowired
    private ConfigurationManager configurationManager;

    private void configugeLog4j() {
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.removeAllAppenders();

        LoggingConfigurations loggingConfigurations = configurationManager.<LoggingConfigurations>get("logging")
            .orElseGet( () -> {
                LoggingConfigurations conf = LoggingConfigurations.getDefault();
                configurationManager.put("logging", conf);
                    try {
                        configurationManager.syncConfigurations();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                return conf;
            });

        if (loggingConfigurations.getEnabledAppenders().contains(Appenders.CONSOLE)) {
            ConsoleAppender consoleAppender = new CustomConsoleAppender();
            consoleAppender.setName("console");
            consoleAppender.setTarget("System.out");
            consoleAppender.setLayout(new PatternLayout(loggingConfigurations.getConsolePattern()));
            consoleAppender.setThreshold(Level.toLevel(loggingConfigurations.getConsolLevel()));
            consoleAppender.activateOptions();

            rootLogger.addAppender(consoleAppender);
        }

        if (loggingConfigurations.getEnabledAppenders().contains(Appenders.FILE)) {
            FileAppender fileAppender = new FileAppender();
            fileAppender.setName("file");
            fileAppender.setFile(loggingConfigurations.getFileName());
            fileAppender.setLayout(new PatternLayout(loggingConfigurations.getFilePattern()));
            fileAppender.setThreshold(Level.toLevel(loggingConfigurations.getFileLevel()));
            fileAppender.activateOptions();

            rootLogger.addAppender(fileAppender);
        }

        org.slf4j.Logger l = org.slf4j.LoggerFactory.getLogger(this.getClass());
        l.debug("Testing debug");
        l.info("Testing info");
        l.warn("Testing warn");
        l.error("Testing error");
    }

    private boolean isConfigured = false;

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    @DependsOn("configurationManager")
    public org.slf4j.Logger logger(InjectionPoint injectionPoint) {
        if (!isConfigured) {
            configugeLog4j();
            isConfigured = true;
        }
        return LoggerFactory.getLogger(injectionPoint.getMethodParameter().getContainingClass());
    }
}