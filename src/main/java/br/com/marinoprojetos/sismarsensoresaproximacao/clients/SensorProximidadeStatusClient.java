package br.com.marinoprojetos.sismarsensoresaproximacao.clients;

import java.net.URI;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import br.com.marinoprojetos.sismarsensoresaproximacao.config.FeignClientConfig;
import br.com.marinoprojetos.sismarsensoresaproximacao.dtos.RespostaDTO;
import br.com.marinoprojetos.sismarsensoresaproximacao.dtos.SensorProximidadeStatus;

@FeignClient(name = "sensor-proximidade-status-client", value = "sensor-proximidade-status-client", configuration = FeignClientConfig.class)
public interface SensorProximidadeStatusClient {	
	
	@PostMapping(value = "/sensor-proximidade-status")
	RespostaDTO<SensorProximidadeStatus> save(URI baseUri, @RequestBody SensorProximidadeStatus obj);
		
}