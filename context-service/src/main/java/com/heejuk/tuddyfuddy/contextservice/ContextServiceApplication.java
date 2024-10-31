package com.heejuk.tuddyfuddy.contextservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class})
@EnableDiscoveryClient
@EnableFeignClients
public class ContextServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContextServiceApplication.class,
                              args);
    }

}
