package com.example.demo;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.MainResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
	@Bean
	public CommandLineRunner testElasticsearchConnection(RestHighLevelClient client) {
		return args -> {
			try {
				MainResponse response = client.info(RequestOptions.DEFAULT);
				System.out.println("Elasticsearch connected to cluster: " + response.getClusterName());
				System.out.println("Elasticsearch version: " + response.getVersion().getNumber());
			} catch (Exception e) {
				System.err.println("Failed to connect to Elasticsearch: " + e.getMessage());
			}
		};
	}
}
