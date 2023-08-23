package com.mcsv.inventory;

import com.mcsv.inventory.model.Inventory;
import com.mcsv.inventory.repository.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableEurekaClient
public class InventoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryServiceApplication.class, args);
	}


	@Bean
	public CommandLineRunner loadData(InventoryRepository inventoryRepository){
		return (args) -> {
			Inventory inventory = new Inventory();
			inventory.setCodeSku("123456");
			inventory.setQuantity(10);

			Inventory inventory2 = new Inventory();
			inventory2.setCodeSku("123457");
			inventory2.setQuantity(15);

			Inventory inventory3 = new Inventory();
			inventory3.setCodeSku("123459");
			inventory3.setQuantity(0);
			inventoryRepository.save(inventory);
			inventoryRepository.save(inventory2);
			inventoryRepository.save(inventory3);
		};
	}
}
