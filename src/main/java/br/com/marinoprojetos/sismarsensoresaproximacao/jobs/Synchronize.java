package br.com.marinoprojetos.sismarsensoresaproximacao.jobs;

import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.marinoprojetos.sismarsensoresaproximacao.clients.SensorProximidadeStatusClient;
import br.com.marinoprojetos.sismarsensoresaproximacao.dtos.SensorProximidade;
import br.com.marinoprojetos.sismarsensoresaproximacao.dtos.SensorProximidadeStatus;
import br.com.marinoprojetos.sismarsensoresaproximacao.dtos.StatusInternetDTO;
import br.com.marinoprojetos.sismarsensoresaproximacao.models.Sensor;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.ConfigService;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.SensorReadService;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.SensorService;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.WebSocketSessionService;
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
	
	@Autowired
	private WebSocketSessionService webSocketSessionService;
	
	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate;
	
	@Scheduled(fixedDelay = 5000)
	public void synchronize() {
		
		// envia status internet
		sendStatusInternet();
				
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
			sensorProximidadeStatus.setUltimaLeitura(null);	
			
			try {
				
				sensorProximidadeStatusClient.save(configService.getApiUrl(), sensorProximidadeStatus);
				LOG.info(sensor.getDescricao() + " - enviado ip:  " + ip);
				
			}catch(Exception ex) {
			}
			
		}
		
	}
	
	private void sendStatusInternet() {
		
		LocalDateTime dataHora = Utils.getNowUTC().withNano(0);
		
		if (webSocketSessionService.isTopicConnected("/topic/internet-status")) {

			if (isInternet()) {

				simpMessagingTemplate.convertAndSend("/topic/internet-status",
						new StatusInternetDTO(dataHora, "Online", "status-internet-online"));

			} else {

				simpMessagingTemplate.convertAndSend("/topic/internet-status",
						new StatusInternetDTO(dataHora, "Offline", "status-internet-offline"));

			}

		}
		
	}
	
	public boolean isInternet() {
	      try {
	         URL url = new URL("http://www.google.com");
	         URLConnection connection = url.openConnection();
	         connection.connect();
	         return true;
	      } catch (Exception e) {
	         return false;
	      }
	   }
	
}