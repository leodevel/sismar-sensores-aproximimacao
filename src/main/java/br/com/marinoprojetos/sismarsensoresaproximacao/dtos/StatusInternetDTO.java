package br.com.marinoprojetos.sismarsensoresaproximacao.dtos;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusInternetDTO {

	private LocalDateTime dateTime;
	private String status;
	private String classStatus;
	
	public StatusInternetDTO(LocalDateTime dateTime, String status, String classStatus) {
		super();
		this.dateTime = dateTime;
		this.status = status;
		this.classStatus = classStatus;
	}	
	
}
