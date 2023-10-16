package com.mesageservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class MessageServiceApplication {

	//get the message from RabbitMQ
	@RabbitListener(queues = {"${msvc.queue.name}"})
	public void handleMessage(String message){
		log.info("Message received from RabbitMQ - {}", message);
	}

	public static void main(String[] args) {
		SpringApplication.run(MessageServiceApplication.class, args);
	}



}
