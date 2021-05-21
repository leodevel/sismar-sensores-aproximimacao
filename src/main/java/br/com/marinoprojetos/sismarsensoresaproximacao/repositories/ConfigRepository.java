package br.com.marinoprojetos.sismarsensoresaproximacao.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.marinoprojetos.sismarsensoresaproximacao.models.Config;

public interface ConfigRepository extends JpaRepository<Config, Long>{
	
}