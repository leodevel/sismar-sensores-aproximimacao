package br.com.marinoprojetos.sismarsensoresaproximacao.dtos;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogDTO {

	private LocalDateTime dateTime;
	private String message;
	
	public LogDTO(LocalDateTime dateTime, String message) {
		super();
		this.dateTime = dateTime;
		this.message = message;
	}	
	
}
