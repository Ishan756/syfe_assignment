package com.financemanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @SpringBootApplication is the entry point of our Spring Boot application.
 * It combines three critical annotations:
 * 1. @Configuration: Tells Spring that this class contains configuration bean definitions.
 * 2. @EnableAutoConfiguration: Tells Spring Boot to automatically configure beans based on dependencies in pom.xml (e.g., setting up PostgreSQL because the driver is in classpath).
 * 3. @ComponentScan: Directs Spring to scan the current package (com.financemanager) and all sub-packages for Spring components, services, controllers, etc.
 */
@SpringBootApplication
public class FinanceManagerApplication {

    /**
     * The standard Java main method which serves as the starting point of the JVM execution.
     * SpringApplication.run() starts the entire Spring Boot framework, boots up the embedded
     * Tomcat server (running on port 8080 by default), and initializes the ApplicationContext.
     */
    public static void main(String[] args) {
        SpringApplication.run(FinanceManagerApplication.class, args);
    }
}
