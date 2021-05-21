package br.com.marinoprojetos.sismarsensoresaproximacao.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.marinoprojetos.sismarsensoresaproximacao.models.Sensor;
import br.com.marinoprojetos.sismarsensoresaproximacao.repositories.SensorRepository;

@Service
public class SensorService {

	@Autowired
	private SensorRepository sensorRepository;
	
	public List<Sensor> findAll(){
		return sensorRepository.findAll();		
	}
	
	public Sensor findById(Long id) {
		return sensorRepository.getOne(id);
	}
	
	public Sensor findBySerial(String serial) {
		return sensorRepository.findBySerial(serial).orElse(null);
	}
	
	public Sensor save(Sensor sensor) {		
		return sensorRepository.save(sensor);		
	}
	
	public void deleteById(Long id) {
		sensorRepository.deleteById(id);
	}
	
}
