package br.com.marinoprojetos.sismarsensoresaproximacao.jobs;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import br.com.marinoprojetos.sismarsensoresaproximacao.clients.SensorProximidadeClient;
import br.com.marinoprojetos.sismarsensoresaproximacao.clients.SensorProximidadeMarcacaoClient;
import br.com.marinoprojetos.sismarsensoresaproximacao.clients.SensorProximidadeStatusClient;
import br.com.marinoprojetos.sismarsensoresaproximacao.dtos.LogDTO;
import br.com.marinoprojetos.sismarsensoresaproximacao.dtos.SensorDTO;
import br.com.marinoprojetos.sismarsensoresaproximacao.dtos.SensorProximidade;
import br.com.marinoprojetos.sismarsensoresaproximacao.dtos.SensorProximidadeStatus;
import br.com.marinoprojetos.sismarsensoresaproximacao.dtos.StatusSensorDTO;
import br.com.marinoprojetos.sismarsensoresaproximacao.dtos.UltimaLeituraDTO;
import br.com.marinoprojetos.sismarsensoresaproximacao.enums.ModeloSensor;
import br.com.marinoprojetos.sismarsensoresaproximacao.enums.StatusComunicacaoSensor;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.ConfigService;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.LogService;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.SensorDistanciaService;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.SerialUtilsService;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.UtilService;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.WebSocketSessionService;
import br.com.marinoprojetos.sismarsensoresaproximacao.utils.Utils;

public class SensorRead extends Thread implements SerialPortDataListener {

	private final Logger LOG = LoggerFactory.getLogger(SensorRead.class);	
	
	private SerialUtilsService serialUtilsService;
	private LogService logService;
	private UtilService utilService;
	private SensorProximidadeMarcacaoClient sensorProximidadeMarcacaoClient;
	private SensorDistanciaService sensorDistanciaService;
	private ConfigService configService;
	private SimpMessagingTemplate simpMessagingTemplate;
	private WebSocketSessionService webSocketSessionService;
	private SensorProximidadeClient sensorProximidadeClient;
	private SensorProximidadeStatusClient sensorProximidadeStatusClient;
	
	private SerialPort serialPort;
	private SensorDTO sensor;
	private String port;
	private Map<String, Boolean> reportLogByPorts;
	private boolean run;
	
	private String buffer;
	private String ultimaLeitura;
	private LocalDateTime dataLeituraAnterior;
	private LocalDateTime dataUltimaAtualizacao;
	private SensorProximidade sensorProximidade;
	
	private SensorSend sensorSend;
	
	// temp ??
	private Double distanciaValida = null;

	public SensorRead(BeanFactory beanFactory, SensorDTO sensor) {
		
		super();
		
		this.serialUtilsService = beanFactory.getBean(SerialUtilsService.class);
		this.configService = beanFactory.getBean(ConfigService.class);
		this.logService = beanFactory.getBean(LogService.class);
		this.utilService = beanFactory.getBean(UtilService.class);
		this.sensorProximidadeMarcacaoClient = beanFactory.getBean(SensorProximidadeMarcacaoClient.class);
		this.sensorDistanciaService = beanFactory.getBean(SensorDistanciaService.class);
		this.simpMessagingTemplate = beanFactory.getBean(SimpMessagingTemplate.class);
		this.webSocketSessionService = beanFactory.getBean(WebSocketSessionService.class);
		this.sensorProximidadeClient = beanFactory.getBean(SensorProximidadeClient.class);
		this.sensorProximidadeStatusClient = beanFactory.getBean(SensorProximidadeStatusClient.class);
		
		this.sensor = sensor;
		this.reportLogByPorts = new HashMap<>();	
		
		this.distanciaValida = null;
		
		Stream.of(sensor.getPorta().split(";")).forEach(port -> {
			if (port != null && !port.trim().isEmpty()) {
				this.reportLogByPorts.put(port.trim(), false);
			}			
		});	
		
		this.run = false;
		this.serialPort = null;
		this.buffer = "";
		
	}
	
	private Double getDistance(String data) {
		
		Double distancia = null;
		
		try {

			if (sensor.getModelo() == ModeloSensor.LD90) {

				if (data.substring(0, 1).equals("r")) {
					distancia = Double.parseDouble(data.substring(1));
				}

			} else if (sensor.getModelo() == ModeloSensor.TRU_SENSE) {

				if (!data.split(",")[0].equalsIgnoreCase("$ER")) {
					distancia = Double.parseDouble(data.split(",")[2].trim());
				}

			} else if (sensor.getModelo() == ModeloSensor.M_200) {
				
				if (data.startsWith("aa") && data.endsWith("ff") && data.length() == 10) {
					
					String partInteira = data.substring(2, 4);
					String partDecimal = data.substring(4, 6);
					
					String value = Integer.parseInt(partInteira, 16) + "." + Integer.parseInt(partDecimal, 16);
					distancia = Double.parseDouble(value);
					
				}			
				
			}

		} catch (Exception ex) {
		}
		
		if (distancia != null) {
			distancia = Utils.round(distancia, 2);
		}
		
		return distancia;
		
	}
	
	private void input(String data) {
		
		ultimaLeitura = data;
		Double distancia = getDistance(data);
		
		if (distanciaValida == null || (distancia != null && distancia > distanciaValida)) {
			distanciaValida = distancia;
		}
		
		// grava no arquivo local
		logService.output(data, distancia);
		
		LocalDateTime dataLeitura = Utils.getNowUTC().withNano(0);
		
		if (dataLeituraAnterior != null && 
				(dataLeitura.isBefore(dataLeituraAnterior) || dataLeitura.isEqual(dataLeituraAnterior))) {
			return;
		}
		
		// grava no arquivo local
		logService.resume(distanciaValida);
		distancia = distanciaValida;
		distanciaValida = null;
		
		dataLeituraAnterior = dataLeitura;				
		
		LOG.info(sensor.getDescricao() + " - Porta " + port + " - Recebido: " + data + " / " + distancia);
		
		if (webSocketSessionService.isTopicConnected("/topic/sensor/" + sensor.getId() + "/monitor")) {		
			
			simpMessagingTemplate.convertAndSend("/topic/sensor/" + sensor.getId() + "/monitor", 
					new LogDTO(dataLeitura, data + " / " + distancia));
		
		}
		
		// verifica se pode mandar os dados
		if (sensorProximidade == null || sensorProximidade.getCodBerco() == null) {			
			this.sensorSend.clear();			
			return;
		}											
		
		if (sensorSend != null) {
			sensorSend.add(dataLeitura, distancia);
		}		
		
	}
	
	private void serialOpen(String porta) throws Exception {
		
		if (!serialUtilsService.existPort(null, porta)) {
            throw new Exception("Não foi possível localizar a porta serial " + porta);
        }
		
		try {

            serialPort = SerialPort.getCommPort(porta);
            serialPort.openPort();            

        } catch (Exception ex) {
        	
        	throw new Exception("Não foi possível abrir a porta serial " + porta, ex);
            
        }
		
	}
	
	private void serialClose() {
		if (serialPort != null) {
            try {
            	serialPort.removeDataListener();
            	serialPort.closePort();
            } catch (Exception ex) {
            }
        }
		serialPort = null;
	}
	
	private void serialConfig(boolean flowControlModel, String porta) throws Exception {

        try {

            serialPort.setComPortParameters(sensor.getVelocidadeDados(), 
            		sensor.getBitsDados(), 
            		sensor.getBitParada(), 
            		sensor.getParidade());

            if (flowControlModel) {
                serialUtilsService.setFlowControlModel(SerialPort.FLOW_CONTROL_DISABLED, serialPort);
            }

            serialPort.setDTR();
            serialPort.setRTS();
            
            serialPort.addDataListener(this);

        } catch (Exception ex) {
            throw new Exception("A porta " + porta + " não suporta os parâmetros!", ex);
        
        }

    }
	
	private boolean serialNotConnected() {
        return serialPort == null;
    }
	
	private void sendLogUltimaLeitura(String ultimaLeitura) {
		
		LocalDateTime dataHora = Utils.getNowUTC().withNano(0);
		
		if (webSocketSessionService.isTopicConnected("/topic/ultima-leitura-sensor")) {	
			
			simpMessagingTemplate.convertAndSend("/topic/ultima-leitura-sensor", 
					new UltimaLeituraDTO(dataHora, sensor.getId(), (ultimaLeitura == null ? "" : ultimaLeitura)));
			
		}
		
	}
	
	private void sendLogStatus(StatusComunicacaoSensor status) {
		
		LocalDateTime dataHora = Utils.getNowUTC().withNano(0);
		
		if (webSocketSessionService.isTopicConnected("/topic/status-sensor")) {	
			
			simpMessagingTemplate.convertAndSend("/topic/status-sensor", 
					new StatusSensorDTO(dataHora, sensor.getId(), status.getValue(), status.getClassValue()));
			
		}
		
	}

	@Override
	public void run() {
		
		this.run = true;
		
		this.sensorSend = new SensorSend(
				sensorDistanciaService, 
				sensorProximidadeMarcacaoClient, 
				configService, 
				webSocketSessionService,
				simpMessagingTemplate,
				logService,
				sensor);
		
		this.sensorSend.start();
		
		while(run) {
			
			String ip = null;
			try {
				InetAddress address = Utils.getLocalHostLANAddress();
				ip = address.getHostAddress();
			}catch(Exception ex) {}
			
			SensorProximidade sensorProximidade = new SensorProximidade();
			sensorProximidade.setSerial(sensor.getSerial());
			
			LocalDateTime dataHora = Utils.getNowUTC().withNano(0);
			
			SensorProximidadeStatus sensorProximidadeStatus = new SensorProximidadeStatus();
			sensorProximidadeStatus.setDataHora(dataHora);
			sensorProximidadeStatus.setSensorProximidade(sensorProximidade);
			sensorProximidadeStatus.setStatusComunicacaoLaser(true);
			sensorProximidadeStatus.setIp(ip);													
			
			if (dataLeituraAnterior == null || ChronoUnit.SECONDS.between(dataLeituraAnterior, dataHora) > 10) {
				
				sensorProximidadeStatus.setUltimaLeitura(null);				
				sendLogUltimaLeitura(null);
				
			} else {
				
				sensorProximidadeStatus.setUltimaLeitura(ultimaLeitura);				
				sendLogUltimaLeitura(ultimaLeitura);
				
			}
			
						
			try {				
				
				boolean portIsConnected = false;
				
				if (this.port != null) {					
					portIsConnected = connectToPortSerial(this.port);					
				}
				
				if (!portIsConnected) {							
					String[] ports = reportLogByPorts.keySet().toArray(new String[] {});										
					for(String port: ports) {	
						if (this.port != null && this.port.equalsIgnoreCase(port)) {
							continue;
						}
						portIsConnected = connectToPortSerial(port);						
						if (portIsConnected) {
							this.port = port;
							break;
						}												
					}				
				}
				
				// conectado na porta
				if (portIsConnected) {
					
					if (dataLeituraAnterior == null || ChronoUnit.SECONDS.between(dataLeituraAnterior, dataHora) > 10) {        				
                		sendLogStatus(StatusComunicacaoSensor.SEM_COMUNICACAO);        				
        			} else {        				
        				sendLogStatus(StatusComunicacaoSensor.COMUNICANDO);        				        				
        			}
					
				} else {
					
					if (this.port != null) {
						String[] ports = reportLogByPorts.keySet().toArray(new String[] {});
						for(String port: ports) {		
							reportLogByPorts.put(port, false);
						}
					}
					
					this.port = null;										
					sensorProximidadeStatus.setStatusComunicacaoLaser(false);
					
					sendLogStatus(StatusComunicacaoSensor.PORTA_NAO_ENCONTRADA);					
					
				}				
			    
            } catch (Exception ex) {
            	logService.addLog(Utils.getNowUTC(), sensor, ex.getMessage(), ex);
            }
			
			if (!run) {
				break;
			}			
			
			// grava o status de comunicação com o laser
			try {
				sensorProximidadeStatusClient.save(configService.getApiUrl(), sensorProximidadeStatus);
			}catch(Exception ex) {
			}
			
			// atualiza a configuração do sensor e verifica se pode mandar os dados
			if (dataUltimaAtualizacao == null || 
					dataUltimaAtualizacao.isBefore(Utils.getNowUTC().minusSeconds(10))) {			
				dataUltimaAtualizacao = Utils.getNowUTC();
				try {
					this.sensorProximidade = sensorProximidadeClient
							.findBySerial(configService.getApiUrl(), sensor.getSerial())
							.getResposta();				
				}catch(Exception ex) {
				}			
			}			
			
			try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
            }
			
		}
		
		try {
			serialClose();
		} catch(Exception ex) {}
		
	}
	
	
	public boolean connectToPortSerial(String port) {
		
		if (serialUtilsService.existPort(serialPort, port)) {

            if (serialNotConnected()) {

                try {

                    serialOpen(port);
                    serialConfig(true, port);
                    
                    logService.addLog(Utils.getNowUTC(), sensor, "Porta " + port + " aberta com sucesso!");
                    reportLogByPorts.put(port, false);                    
                    
                    return true;

                } catch (Exception ex) {
                    if (!reportLogByPorts.get(port)) {
                    	logService.addLog(Utils.getNowUTC(), sensor, ex.getMessage(), ex);
                    	reportLogByPorts.put(port, true);
                    }
                    return false;
                    
                }

            }
            
            return true;

        } else {

            try {

                serialClose();

                if (!reportLogByPorts.get(port)) {
                	logService.addLog(Utils.getNowUTC(), sensor, "Porta " + port + " não encontrada!");
                	reportLogByPorts.put(port, true);
                }

            } catch (Exception ex) {
            	logService.addLog(Utils.getNowUTC(), sensor, ex.getMessage(), ex);
            }
            
            return false;

        }		
		
	}
	
	
	public void close() {
		
		run = false;
		
		try {
			serialClose();
		} catch(Exception ex) {}
		
		if (sensorSend != null) {
			sensorSend.close();
			sensorSend.interrupt();
            try {
            	sensorSend.join();
            } catch (Exception ex) {
            }
		}
		
		sensorSend = null;
		
	}

	@Override
	public int getListeningEvents() {
		return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
	}	

	@Override
	public void serialEvent(SerialPortEvent spe) {

		if (spe.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
			return;
		}

		try {

			byte[] newData = new byte[serialPort.bytesAvailable()];
			serialPort.readBytes(newData, newData.length);
						
			// laser da china
			if (sensor.getModelo() == ModeloSensor.M_200) {
				
				String response = new String(Hex.encodeHexString(newData));
				
				if (response.isEmpty()) {
					return;
				}
				
				input(response);
				buffer = "";
				
				return;
				
			}
			
			
			String response = new String(newData);
						
			if (response.isEmpty()) {
				return;
			}

			for (int i = 0; i < response.length(); i++) {

				if (response.charAt(i) == 10) {
					if (!utilService.isNullOrEmpty(buffer)) {
						input(buffer.trim());
					}
					buffer = "";
					continue;
				}

				buffer += response.substring(i, i + 1);

			}

		} catch (Exception ex) {
		}

	}

}