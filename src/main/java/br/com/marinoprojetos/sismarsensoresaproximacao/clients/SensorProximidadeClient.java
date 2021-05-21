package br.com.marinoprojetos.sismarsensoresaproximacao.clients;

import java.net.URI;
import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import br.com.marinoprojetos.sismarsensoresaproximacao.config.FeignClientConfig;
import br.com.marinoprojetos.sismarsensoresaproximacao.dtos.RespostaDTO;
import br.com.marinoprojetos.sismarsensoresaproximacao.dtos.SensorProximidade;

@FeignClient(name = "sensor-proximidade-client", value = "sensor-proximidade-client", configuration = FeignClientConfig.class)
public interface SensorProximidadeClient {	
	
	@GetMapping(value = "/sensores-proximidade")
	RespostaDTO<List<SensorProximidade>> findAll(URI baseUri);
	
	@GetMapping(value = "/sensores-proximidade/serial/{serial}")
	RespostaDTO<SensorProximidade> findBySerial(URI baseUri, @PathVariable String serial);
	
}