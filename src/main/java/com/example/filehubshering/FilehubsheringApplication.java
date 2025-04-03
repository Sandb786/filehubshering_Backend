package com.example.filehubshering;


import java.time.LocalDateTime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FilehubsheringApplication 
{

	public static void main(String[] args)
	{
		SpringApplication.run(FilehubsheringApplication.class, args);
		System.out.println("\n\nApplication Stated: 8080 \n "+LocalDateTime.now()+"\n\n");

	}

}

