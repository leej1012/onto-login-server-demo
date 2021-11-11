package io.ont;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class OntoLoginApplication {

	public static void main(String[] args) {
		SpringApplication.run(OntoLoginApplication.class, args);
	}
}