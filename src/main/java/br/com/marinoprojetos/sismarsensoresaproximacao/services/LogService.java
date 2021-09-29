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
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import br.com.marinoprojetos.sismarsensoresaproximacao.dtos.LogDTO;
import br.com.marinoprojetos.sismarsensoresaproximacao.dtos.SensorDTO;
import br.com.marinoprojetos.sismarsensoresaproximacao.utils.Utils;

@Service
public class LogService {
	
	private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat formatWithHour = new SimpleDateFormat("yyyy-MM-dd-HH");
	private final Logger LOG = LoggerFactory.getLogger(LogService.class);	
	private static final Path DIR_LOGS = Paths.get("logs");
	private static final Path DIR_OUTPUT = Paths.get("output");
	
	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate;
	
	@Autowired
	private WebSocketSessionService webSocketSessionService;
	
	public void output(String data, Double distancia) {

		try {

			LocalDateTime dataLeitura = LocalDateTime.now();

			File dir = DIR_OUTPUT.toFile();

			if (!dir.exists()) {
				dir.mkdirs();
			}

			File out = new File(dir, getOutputFileName());

			if (!out.exists()) {
				try {
					FileUtils.cleanDirectory(dir);
					out.createNewFile();
				} catch (IOException ex1) {
				}
			}

			String txt = dataLeitura.format(
					DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")) + ";" + data + ";"
					+ distancia;

			try {
				Files.write(out.toPath(), (txt + "\r\n").getBytes(), StandardOpenOption.APPEND);
			} catch (IOException ex1) {
			}

		} catch (Exception ex) {
		}

	}
	
	public void resume(Double distancia) {

		try {

			LocalDateTime dataLeitura = LocalDateTime.now();

			File dir = DIR_OUTPUT.toFile();

			if (!dir.exists()) {
				dir.mkdirs();
			}

			File out = new File(dir, getOutputFileName());

			if (!out.exists()) {
				try {
					FileUtils.cleanDirectory(dir);
					out.createNewFile();
				} catch (IOException ex1) {
				}
			}

			String txt = "\r\n======== " + dataLeitura.format(
					DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")) + ";"
					+ distancia + " ======== \r\n \r\n";

			try {
				Files.write(out.toPath(), txt.getBytes(), StandardOpenOption.APPEND);
			} catch (IOException ex1) {
			}

		} catch (Exception ex) {
		}

	}

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
	
	private String getLogFileName() {
		return format.format(Date.from(Utils.getNowUTC().toInstant(ZoneOffset.UTC))) + ".log";
	}
	
	private String getOutputFileName() {
		return formatWithHour.format(Date.from(Utils.getNowUTC().toInstant(ZoneOffset.UTC))) + ".txt";
	}
	
	public List<String> getLog() {
		
		File log = new File(DIR_LOGS.toFile(), getLogFileName());
		
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

        File log = new File(dir, getLogFileName());

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