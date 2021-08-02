package br.com.marinoprojetos.sismarsensoresaproximacao.dtos;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UltimaLeituraDTO {

	private LocalDateTime dateTime;
	private Long sensorId;
	private String message;
	
	public UltimaLeituraDTO(LocalDateTime dateTime, Long sensorId, String message) {
		super();
		this.dateTime = dateTime;
		this.sensorId = sensorId;
		this.message = message;
	}	
	
}
