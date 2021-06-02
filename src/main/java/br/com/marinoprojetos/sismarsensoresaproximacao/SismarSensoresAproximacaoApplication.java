package br.com.marinoprojetos.sismarsensoresaproximacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableFeignClients
public class SismarSensoresAproximacaoApplication{

	public static void main(String[] args) {
		SpringApplication.run(SismarSensoresAproximacaoApplication.class, args);
	}

}
