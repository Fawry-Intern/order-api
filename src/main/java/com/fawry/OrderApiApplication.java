package com.fawry;

import com.fawry.order_api.services.OrderCancellationSaga;
import com.fawry.order_api.services.impl.SagaOrderServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class OrderApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderApiApplication.class, args);
	}


	@Configuration
	@RequiredArgsConstructor
	class StartUp implements CommandLineRunner {

		private final OrderCancellationSaga orderCancellationSaga;

		@Override
		public void run(String... args) throws Exception {
			orderCancellationSaga.cancelOrder(2L, "Failed to shipped order", "muhammadhussein2312@gmail.com");
		}
	}
}
