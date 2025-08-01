package com.example.identifyService;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IdentifyServiceApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.filename(".env") // Tên file .env của bạn
				.load();

		System.setProperty("URL_DB", dotenv.get("URL_DB"));
		System.setProperty("USERNAME_DB", dotenv.get("USERNAME_DB"));
		System.setProperty("PASSWORD_DB", dotenv.get("PASSWORD_DB"));
		System.setProperty("SENDGRID_API_KEY", dotenv.get("SENDGRID_API_KEY"));
		System.setProperty("FROM_EMAIL", dotenv.get("FROM_EMAIL"));
		System.setProperty("SIGNER_KEY", dotenv.get("SIGNER_KEY"));
		System.setProperty("GOOGLE_API_KEY", dotenv.get("GOOGLE_API_KEY"));
		System.setProperty("CLIENT_ID", dotenv.get("CLIENT_ID"));
		System.setProperty("CLIENT_SECRET", dotenv.get("CLIENT_SECRET"));
		SpringApplication.run(IdentifyServiceApplication.class, args);
	}

}
