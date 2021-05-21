package br.com.marinoprojetos.sismarsensoresaproximacao.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.marinoprojetos.sismarsensoresaproximacao.models.SensorDistancia;

public interface SensorDistanciaRepository extends JpaRepository<SensorDistancia, Long>{

	List<SensorDistancia> findByIdSensorOrderByDataLeituraAsc(Long codSensor);
	
	long countByIdSensor(Long idSensor);
	
	void deleteByIdSensor(Long idSensor);
	
}