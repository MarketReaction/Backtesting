package com.jassoft.markets.learningmodel;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;
import com.jassoft.markets.BaseSpringConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created by jonshaw on 13/07/15.
 */
@Configuration
@ComponentScan("com.jassoft.markets.learningmodel")
public class SpringConfiguration extends BaseSpringConfiguration {

    @Bean
    public AmazonS3Client amazonS3Client() {
        return new AmazonS3Client(new ProfileCredentialsProvider());
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(SpringConfiguration.class, args);
    }
}
