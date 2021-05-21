package br.com.marinoprojetos.sismarsensoresaproximacao.services;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.marinoprojetos.sismarsensoresaproximacao.dtos.SensorDTO;
import br.com.marinoprojetos.sismarsensoresaproximacao.jobs.SensorRead;
import br.com.marinoprojetos.sismarsensoresaproximacao.models.Sensor;
import br.com.marinoprojetos.sismarsensoresaproximacao.utils.Utils;

@Service
public class SensorReadService {

	@Autowired
	private SensorService sensorService;
	
	@Autowired
	private LogService logService;	
	
	@Autowired
	private BeanFactory beanFactory;
	
	private Map<Long, SensorRead> sensores;
	
	@PostConstruct
	public void init() {		
		
		sensores = new HashMap<>();
		
		sensorService.findAll().forEach(sensor -> {
			
			if (sensor.isIniciarAutomaticamente()) {
				start(sensor);
			}
			
		});
		
	}
	
	public void start(Sensor sensor) {	
		stop(sensor);
		
		logService.addLog(Utils.getNowUTC(), SensorDTO.fromEntity(sensor), " iniciado");
		
		SensorRead sensorRead = new SensorRead(beanFactory, SensorDTO.fromEntity(sensor));		
		sensorRead.start();
		
		sensores.put(sensor.getId(), sensorRead);		
		
	}
	
	public void stop(Sensor sensor) {
		if (sensores.containsKey(sensor.getId())) {
			
			SensorRead sensorRead = sensores.get(sensor.getId());
			sensorRead.close();			
			sensorRead.interrupt();
            try {
            	sensorRead.join();
            } catch (Exception ex) {
            }
            
			sensores.remove(sensor.getId());
			logService.addLog(Utils.getNowUTC(), SensorDTO.fromEntity(sensor), " encerrado com sucesso");
			
		}
	}
	
	public boolean isStarted(Sensor sensor) {
		return sensores.containsKey(sensor.getId());
	}
	
	public int getTotalProccess() {
		return sensores.size();
	}
	
}
