package br.com.marinoprojetos.sismarsensoresaproximacao;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableFeignClients
public class SismarSensoresAproximacaoApplication implements CommandLineRunner{

	public static void main(String[] args) {
		SpringApplication.run(SismarSensoresAproximacaoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		OkHttpClient client = new OkHttpClient();

				Request request = new Request.Builder()
				  .url("https://sismarapp.com.br/sensores-proximidade")
				  .method("GET", null)
				  .addHeader("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0aXBvIjoiQ0xJRU5URSIsImlzcyI6ImF1dGgwIiwiY29kIjoxMn0.0AyTOAIwpkFIBU3tR3xGZDkzjTdcjZeEtIJybYWD_d0")
				  .build();
				Response response = client.newCall(request).execute();
				
				System.out.println(response.message());
				System.out.println(response.code());
				System.out.println(response.body().string());
		
	}

}
