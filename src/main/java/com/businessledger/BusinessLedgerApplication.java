package com.businessledger;

import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BusinessLedgerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusinessLedgerApplication.class, args);
        Application.launch(JavaFxLauncher.class, args);
    }
}