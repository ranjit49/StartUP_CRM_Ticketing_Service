package startup.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class StartupTicketingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(StartupTicketingServiceApplication.class, args);
	}

}
