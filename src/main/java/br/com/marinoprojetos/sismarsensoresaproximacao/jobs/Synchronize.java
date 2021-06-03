package br.com.marinoprojetos.sismarsensoresaproximacao.jobs;

import java.net.InetAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.marinoprojetos.sismarsensoresaproximacao.clients.SensorProximidadeStatusClient;
import br.com.marinoprojetos.sismarsensoresaproximacao.dtos.SensorProximidade;
import br.com.marinoprojetos.sismarsensoresaproximacao.dtos.SensorProximidadeStatus;
import br.com.marinoprojetos.sismarsensoresaproximacao.models.Sensor;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.ConfigService;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.SensorReadService;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.SensorService;
import br.com.marinoprojetos.sismarsensoresaproximacao.utils.Utils;

@Component
public class Synchronize {

	private final Logger LOG = LoggerFactory.getLogger(Synchronize.class);	
	
	@Autowired
	private SensorService sensorService;	
	
	@Autowired
	private SensorReadService sensorReadService;
	
	@Autowired
	private ConfigService configService;
	
	@Autowired
	private SensorProximidadeStatusClient sensorProximidadeStatusClient;
	
	@Scheduled(fixedDelay = 5000)
	public void synchronize() {
				
		// sincroniza o ip
		String ip = null;
		try {
			InetAddress address = Utils.getLocalHostLANAddress();
			ip = address.getHostAddress();
		}catch(Exception ex) {}
		
		List<Sensor> sensors = sensorService.findAll();	
		
		for(Sensor sensor : sensors) {
			
			if (sensorReadService.isStarted(sensor)) {
				continue;
			}
			
			if (configService.getConfig() == null) {
				continue;
			}
			
			SensorProximidade sensorProximidade = new SensorProximidade();
			sensorProximidade.setSerial(sensor.getSerial());
			
			SensorProximidadeStatus sensorProximidadeStatus = new SensorProximidadeStatus();
			sensorProximidadeStatus.setDataHora(Utils.getNowUTC());
			sensorProximidadeStatus.setSensorProximidade(sensorProximidade);
			sensorProximidadeStatus.setStatusComunicacaoLaser(false);
			sensorProximidadeStatus.setIp(ip);
			
			try {
				
				sensorProximidadeStatusClient.save(configService.getApiUrl(), sensorProximidadeStatus);
				LOG.info(sensor.getDescricao() + " - enviado ip:  " + ip);
				
			}catch(Exception ex) {
			}
			
		}
		
	}
	
}