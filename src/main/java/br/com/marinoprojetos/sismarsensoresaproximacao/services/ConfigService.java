package br.com.marinoprojetos.sismarsensoresaproximacao.services;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.marinoprojetos.sismarsensoresaproximacao.models.Config;
import br.com.marinoprojetos.sismarsensoresaproximacao.repositories.ConfigRepository;

@Service
public class ConfigService {

	@Autowired
	private ConfigRepository configRepository;
	
	public Config getConfig() {		
		return configRepository
				.findAll()
				.stream()
				.findFirst()
				.orElse(null);
	}
	
	public Config save(Config config) {		
		return configRepository.save(config);		
	}
	
	public URI getApiUrl() {
		Config config = getConfig();
		return config == null ? URI.create("") : URI.create(config.getApiUrl());
	}
		
}
