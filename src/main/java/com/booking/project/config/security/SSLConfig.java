package com.booking.project.config.security;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class SSLConfig {
    @Autowired
    private Environment env;

    @PostConstruct
    private void configureSSL() {
        //set to TLSv1.1 or TLSv1.2

        //load the 'javax.net.ssl.trustStore' and
        //'javax.net.ssl.trustStorePassword' from application.properties
        System.setProperty("javax.net.ssl.trustStore","src/main/resources/static/springtruststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword","password");
    }
}