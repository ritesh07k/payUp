package com.payup.refund;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.payup")
public class RefundServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RefundServiceApplication.class, args);
    }
}
