package br.com.marinoprojetos.sismarsensoresaproximacao.jobs;

import java.time.LocalDateTime;

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
import br.com.marinoprojetos.sismarsensoresaproximacao.enums.ModeloSensor;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.ConfigService;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.LogService;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.SensorDistanciaService;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.SerialUtilsService;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.UtilService;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.WebSocketSessionService;
import br.com.marinoprojetos.sismarsensoresaproximacao.utils.Utils;

public class SensorRead extends Thread implements SerialPortDataListener {

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
	private boolean reportLog;
	private boolean run;
	
	private String buffer;
	private LocalDateTime dataLeituraAnterior;
	private LocalDateTime dataUltimaAtualizacao;
	private SensorProximidade sensorProximidade;
	
	private SensorSend sensorSend;

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
		this.run = false;
		this.serialPort = null;
		this.reportLog = false;
		this.buffer = "";
		
	}
	
	private void input(String data) {

		LocalDateTime dataLeitura = Utils.getNowUTC().withNano(0);
		
		if (dataLeituraAnterior != null && 
				(dataLeitura.isBefore(dataLeituraAnterior) || dataLeitura.isEqual(dataLeituraAnterior))) {
			return;
		}
		
		dataLeituraAnterior = dataLeitura;
		
		// verifica se pode mandar os dados
		if (sensorProximidade != null && sensorProximidade.getCodBerco() == null) {
			return;
		}
		
		if (webSocketSessionService.isTopicConnected("/topic/sensor/" + sensor.getId() + "/monitor")) {		
		
			simpMessagingTemplate.convertAndSend("/topic/sensor/" + sensor.getId() + "/monitor", 
					new LogDTO(Utils.getNowUTC(), data));
		
		}
		
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

			}

		} catch (Exception ex) {
		}
		
		if (distancia != null) {
			distancia = Utils.round(distancia, 2);
		}
		
		if (sensorSend != null) {
			sensorSend.add(dataLeitura, distancia);
		}		
		
	}
	
	private void serialOpen() throws Exception {
		
		if (!serialUtilsService.existPort(null, sensor.getPorta())) {
            throw new Exception("Não foi possível localizar a porta serial " + sensor.getPorta());
        }
		
		try {

            serialPort = SerialPort.getCommPort(sensor.getPorta());
            serialPort.openPort();            

        } catch (Exception ex) {
        	
        	throw new Exception("Não foi possível abrir a porta serial "
                    + sensor.getPorta(), ex);
            
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
	
	private void serialConfig(boolean flowControlModel) throws Exception {

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
            throw new Exception("A porta " + sensor.getPorta()
                    + " não suporta os parâmetros!", ex);
        
        }

    }
	
	private boolean serialNotConnected() {
        return serialPort == null;
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
			
			SensorProximidade sensorProximidade = new SensorProximidade();
			sensorProximidade.setSerial(sensor.getSerial());
			
			SensorProximidadeStatus sensorProximidadeStatus = new SensorProximidadeStatus();
			sensorProximidadeStatus.setDataHora(Utils.getNowUTC());
			sensorProximidadeStatus.setSensorProximidade(sensorProximidade);
			sensorProximidadeStatus.setStatusComunicacaoLaser(true);
						
			try {
				
                if (serialUtilsService.existPort(serialPort, sensor.getPorta())) {

                    if (serialNotConnected()) {

                        try {

                            serialOpen();
                            serialConfig(true);
                            
                            logService.addLog(Utils.getNowUTC(), sensor, "Porta " + sensor.getPorta() + " aberta com sucesso!");
                            reportLog = false;

                        } catch (Exception ex) {
                            if (!reportLog) {
                            	logService.addLog(Utils.getNowUTC(), sensor, ex.getMessage(), ex);
                                reportLog = true;
                            }
                            sensorProximidadeStatus.setStatusComunicacaoLaser(false);
                            
                        }

                    }

                } else {

                    try {

                        serialClose();

                        if (!reportLog) {
                        	logService.addLog(Utils.getNowUTC(), sensor, "Porta " + sensor.getPorta() + " não encontrada!");
                            reportLog = true;
                        }

                    } catch (Exception ex) {
                    	logService.addLog(Utils.getNowUTC(), sensor, ex.getMessage(), ex);
                    }
                    
                    sensorProximidadeStatus.setStatusComunicacaoLaser(false);

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
					sensorProximidade = sensorProximidadeClient
							.findBySerial(configService.getApiUrl(), sensor.getSerial())
							.getResposta();				
				}catch(Exception ex) {}			
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