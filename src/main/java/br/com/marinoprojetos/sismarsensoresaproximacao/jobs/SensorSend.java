package br.com.marinoprojetos.sismarsensoresaproximacao.jobs;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import br.com.marinoprojetos.sismarsensoresaproximacao.clients.SensorProximidadeMarcacaoClient;
import br.com.marinoprojetos.sismarsensoresaproximacao.dtos.LogDTO;
import br.com.marinoprojetos.sismarsensoresaproximacao.dtos.RespostaDTO;
import br.com.marinoprojetos.sismarsensoresaproximacao.dtos.SensorDTO;
import br.com.marinoprojetos.sismarsensoresaproximacao.dtos.SensorProximidade;
import br.com.marinoprojetos.sismarsensoresaproximacao.dtos.SensorProximidadeMarcacao;
import br.com.marinoprojetos.sismarsensoresaproximacao.models.SensorDistancia;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.ConfigService;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.LogService;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.SensorDistanciaService;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.WebSocketSessionService;
import br.com.marinoprojetos.sismarsensoresaproximacao.utils.Utils;

public class SensorSend extends Thread {

	private SensorDistanciaService sensorDistanciaService;
	private SensorProximidadeMarcacaoClient sensorProximidadeMarcacaoClient;
	private ConfigService configService;
	private WebSocketSessionService webSocketSessionService;
	private SimpMessagingTemplate simpMessagingTemplate;
	private LogService logService;
	
	private boolean run;
	private SensorDTO sensor;
	private List<SensorDistancia> distancias;
	
	public SensorSend(SensorDistanciaService sensorDistanciaService,
			SensorProximidadeMarcacaoClient sensorProximidadeMarcacaoClient,
			ConfigService configService,
			WebSocketSessionService webSocketSessionService,
			SimpMessagingTemplate simpMessagingTemplate,
			LogService logService,
			SensorDTO sensor) {
		
		super();
		
		this.sensorDistanciaService = sensorDistanciaService;
		this.sensorProximidadeMarcacaoClient = sensorProximidadeMarcacaoClient;
		this.configService = configService;
		this.webSocketSessionService  = webSocketSessionService;
		this.simpMessagingTemplate = simpMessagingTemplate;
		this.logService = logService;
		
		this.sensor = sensor;
		
		this.run = false;
		this.distancias = sensorDistanciaService.findByIdSensor(sensor.getId());
		
		sendAll();
		
	}
	
	public void sendAll() {
		
		if (distancias.isEmpty()) {
			logService.addLog(Utils.getNowUTC(), sensor, "Sem buffer/cache para enviar do sensor #" + this.sensor.getSerial());
			return;
		}
		
		logService.addLog(Utils.getNowUTC(), sensor, "Enviando buffer/cache para enviar do sensor #" +
				this.sensor.getSerial() + ". Total de " + distancias.size() + " registros");
		
		List<SensorProximidadeMarcacao> marcacoes = distancias.stream().map(sensorDistancia -> {			
	        
	        SensorProximidade sensor = new SensorProximidade();
			sensor.setSerial(this.sensor.getSerial());
			
			SensorProximidadeMarcacao marcacao = new SensorProximidadeMarcacao();
			marcacao.setSensorProximidade(sensor);
			marcacao.setDataLeitura(sensorDistancia.getDataLeitura());
			marcacao.setDistancia(sensorDistancia.getDistancia());
			
			return marcacao;			
			
		}).collect(Collectors.toList());
		

		try {
			
			RespostaDTO<Boolean> resposta = sensorProximidadeMarcacaoClient
					.saveAll(configService.getApiUrl(), marcacoes);
			
			if (resposta.getStatus() == 200 && resposta.getSucesso()) {
				
				sensorDistanciaService.deleteAll(distancias);
				distancias.clear();		
				
				logService.addLog(Utils.getNowUTC(), sensor, "Enviado buffer/cache do sensor #" +
						this.sensor.getSerial() + ". Total restante: " + distancias.size() + " registros");
				
			}
			
		}catch(Exception ex) {
			
			logService.addLog(Utils.getNowUTC(), sensor, "Falha ao enviar buffer/cache do sensor #" +
					this.sensor.getSerial() + ". Total restante: " + distancias.size() + " registros");
			
		}		
		
	}
	
	public void close() {
		this.run = false;
        this.clear();
    }
	
	public void clear() {
		distancias.clear();
		sensorDistanciaService.deleteAllByIdSensor(sensor.getId());		
	}
	
	public synchronized void add(LocalDateTime dataLeitura, Double distancia) {
        try {
        	
        	SensorDistancia sensorDistancia = new SensorDistancia();
        	sensorDistancia.setDataLeitura(dataLeitura);
        	sensorDistancia.setIdSensor(sensor.getId());
        	sensorDistancia.setDistancia(distancia);
        	
        	if (configService.getConfig().isGravarDadosLocal()) {
        		sensorDistancia = sensorDistanciaService.save(sensorDistancia);
        	}
        	
            distancias.add(sensorDistancia);
            
            notify();
            
        } catch (Exception ex) {
        }
    }
	
	@Override
	public void run() {
		
		run = true;

        while (run) {

            if (distancias.isEmpty()) {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                    }
                }
            }

            if (distancias.isEmpty()) {
                continue;
            }
            
            List<SensorDistancia> distanciasSend = new ArrayList<>();
            for (int i = 0; i < distancias.size(); i++) {
            	distanciasSend.add(distancias.get(i));
			}            
            
            List<SensorProximidadeMarcacao> marcacoes = distanciasSend.stream().map(sensorDistancia -> {			    	        
    	        SensorProximidade sensor = new SensorProximidade();
    			sensor.setSerial(this.sensor.getSerial());    			
    			SensorProximidadeMarcacao marcacao = new SensorProximidadeMarcacao();
    			marcacao.setSensorProximidade(sensor);
    			marcacao.setDataLeitura(sensorDistancia.getDataLeitura());
    			marcacao.setDistancia(sensorDistancia.getDistancia());    			
    			return marcacao;			    			
    		}).collect(Collectors.toList());            
            
            try {
    			
    			RespostaDTO<Boolean> resposta = sensorProximidadeMarcacaoClient
    					.saveAll(configService.getApiUrl(), marcacoes);
    			
    			if (resposta.getStatus() == 200 && resposta.getSucesso()) {
    				 				    	
    				// remove do bd e da lista
    				sensorDistanciaService.deleteAll(distanciasSend.stream()
    						.filter(obj -> obj.getId() != null).collect(Collectors.toList()));
    				distancias.removeAll(distanciasSend);		
    				
    				if (webSocketSessionService.isTopicConnected("/topic/sensor/" + this.sensor.getId() + "/push")) {		
    					
    					marcacoes.forEach(marcacao -> {
    						
    						simpMessagingTemplate.convertAndSend("/topic/sensor/" + this.sensor.getId() + "/push", 
        							new LogDTO(marcacao.getDataLeitura(), (marcacao.getDistancia() == null ? "" : marcacao.getDistancia().toString()) ));
    						
    					});
    					
    				}
    				
    			}
    			
    		} catch(Exception ex) {    			
    		}		
            
            
            /*SensorDistancia sensorDistancia = distancias.get(0);
            
            SensorProximidade sensor = new SensorProximidade();
    		sensor.setSerial(this.sensor.getSerial());
    		
    		SensorProximidadeMarcacao marcacao = new SensorProximidadeMarcacao();
    		marcacao.setSensorProximidade(sensor);
    		marcacao.setDataLeitura(sensorDistancia.getDataLeitura());
    		marcacao.setDistancia(sensorDistancia.getDistancia());
    		
    		try {
    			
    			RespostaDTO<SensorProximidadeMarcacao> resposta = sensorProximidadeMarcacaoClient
    					.save(configService.getApiUrl(), marcacao);
    			
    			if (resposta.getStatus() == 200 && resposta.getSucesso()) {
    				
    				if (sensorDistancia.getId() != null) {
    					sensorDistanciaService.delete(sensorDistancia);
    				}
    				
    				distancias.remove(0);
    				
    				if (webSocketSessionService.isTopicConnected("/topic/sensor/" + this.sensor.getId() + "/push")) {		
    					
    					simpMessagingTemplate.convertAndSend("/topic/sensor/" + this.sensor.getId() + "/push", 
    							new LogDTO(marcacao.getDataLeitura(), (marcacao.getDistancia() == null ? "" : marcacao.getDistancia().toString()) ));
    				
    				}
    				
    			}
    			
    		}catch(Exception ex) {
    		}*/
            
        }
		
	}
	
}
