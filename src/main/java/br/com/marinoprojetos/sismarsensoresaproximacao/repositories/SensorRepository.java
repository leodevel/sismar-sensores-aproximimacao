package br.com.marinoprojetos.sismarsensoresaproximacao.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.marinoprojetos.sismarsensoresaproximacao.models.Sensor;

public interface SensorRepository extends JpaRepository<Sensor, Long>{

	Optional<Sensor> findBySerial(String serial);
	
}