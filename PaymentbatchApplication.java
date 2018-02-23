package com.awc.paymentbatch.paymentbatch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "com.awc.springbatch","com.awc","com.awc.domain" })
@EnableAutoConfiguration
@EnableBatchProcessing
public class PaymentbatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentbatchApplication.class, args);
	}
}
