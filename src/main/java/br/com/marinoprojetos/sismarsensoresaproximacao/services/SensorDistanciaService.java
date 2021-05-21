package br.com.marinoprojetos.sismarsensoresaproximacao.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marinoprojetos.sismarsensoresaproximacao.models.SensorDistancia;
import br.com.marinoprojetos.sismarsensoresaproximacao.repositories.SensorDistanciaRepository;

@Service
public class SensorDistanciaService {

	@Autowired
	private SensorDistanciaRepository sensorDistanciaRepository;
	
	public List<SensorDistancia> findByIdSensor(Long codSensor){
		return sensorDistanciaRepository.findByIdSensorOrderByDataLeituraAsc(codSensor);
	}
	
	public SensorDistancia save(SensorDistancia sensorDistancia) {		
		return sensorDistanciaRepository.save(sensorDistancia);		
	}
	
	public void delete(SensorDistancia obj) {
		sensorDistanciaRepository.delete(obj);
	}	
	
	@Transactional
	public void deleteAll(List<SensorDistancia> list) {
		sensorDistanciaRepository.deleteAll(list);
	}
	
	@Transactional
	public void deleteAllByIdSensor(Long idSensor) {
		sensorDistanciaRepository.deleteByIdSensor(idSensor);
	}
	
	public long countByIdSensor(Long idSensor) {
		return sensorDistanciaRepository.countByIdSensor(idSensor);
	}
	
}
