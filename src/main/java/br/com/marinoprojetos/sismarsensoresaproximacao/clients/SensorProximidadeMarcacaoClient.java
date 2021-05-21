package br.com.marinoprojetos.sismarsensoresaproximacao.clients;

import java.net.URI;
import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import br.com.marinoprojetos.sismarsensoresaproximacao.config.FeignClientConfig;
import br.com.marinoprojetos.sismarsensoresaproximacao.dtos.RespostaDTO;
import br.com.marinoprojetos.sismarsensoresaproximacao.dtos.SensorProximidadeMarcacao;

@FeignClient(name = "sensor-proximidade-marcacao-client", value = "sensor-proximidade-marcacao-client", configuration = FeignClientConfig.class)
public interface SensorProximidadeMarcacaoClient {	
	
	@PostMapping(value = "/sensores-proximidade-marcacoes")
	RespostaDTO<SensorProximidadeMarcacao> save(URI baseUri, @RequestBody SensorProximidadeMarcacao obj);
	
	@PostMapping(value = "/sensores-proximidade-marcacoes/all")
	RespostaDTO<Boolean> saveAll(URI baseUri, @RequestBody List<SensorProximidadeMarcacao> list);
	
}