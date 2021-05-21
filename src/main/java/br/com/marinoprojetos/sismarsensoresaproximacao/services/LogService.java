package br.com.marinoprojetos.sismarsensoresaproximacao.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import br.com.marinoprojetos.sismarsensoresaproximacao.dtos.LogDTO;
import br.com.marinoprojetos.sismarsensoresaproximacao.dtos.SensorDTO;

@Service
public class LogService {
	
	private final Logger LOG = LoggerFactory.getLogger(LogService.class);	
	private static final Path DIR_LOGS = Paths.get("logs");
	
	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate;
	
	@Autowired
	private WebSocketSessionService webSocketSessionService;

	public void addLog(LocalDateTime dateTime, SensorDTO sensor, String log, Exception ex) {		
		String msg = dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + " - " + sensor.getDescricao() + ": " + log;
		LOG.warn(msg, ex);	
		save(dateTime, msg, ex);
	}
	
	public void addLog(LocalDateTime dateTime, SensorDTO sensor, String log) {		
		String msg = dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + " - " + sensor.getDescricao() + ": " + log; 		
		LOG.info(msg);
		save(dateTime, msg, null);
	}
	
	public List<String> getLog() {
		
		File log = new File(DIR_LOGS.toFile(), "logs.log");
		
		if (!log.exists()) {
			return new ArrayList<>();
		}
		
		try {			
			return Files.readAllLines(log.toPath(), StandardCharsets.UTF_8);			
		} catch (IOException e) {			
		}
		
		return new ArrayList<>();
		
	}
	
	private void save(LocalDateTime dateTime, String msg, Exception ex) {
		
		if (webSocketSessionService.isTopicConnected("/topic/logs")) {		
			simpMessagingTemplate.convertAndSend("/topic/logs", new LogDTO(dateTime, msg));
		}
		
		File dir = DIR_LOGS.toFile();

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File log = new File(dir, "logs.log");

        if (!log.exists()) {
            try {
                log.createNewFile();
            } catch (IOException ex1) {
            }
        }

        if (ex == null) {
            try {
                Files.write(log.toPath(), (msg + "\r\n").getBytes(), StandardOpenOption.APPEND);
            } catch (IOException ex1) {
            }
        }

        if (ex != null) {
            try {
                try (FileWriter fw = new FileWriter(log, true)) {
                    ex.printStackTrace(new PrintWriter(fw));
                } catch (Exception ex2) {
                }
            } catch (Exception ex2) {
            }
        }	
		
	}
	
}