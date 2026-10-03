package com.myfitness.workout_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class WorkoutServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkoutServiceApplication.class, args);
	}

}
