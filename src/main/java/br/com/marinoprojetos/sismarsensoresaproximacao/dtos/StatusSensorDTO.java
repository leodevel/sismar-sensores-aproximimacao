package br.com.marinoprojetos.sismarsensoresaproximacao.dtos;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatusSensorDTO {

	private LocalDateTime dateTime;
	private Long sensorId;
	private String status;
	private String classStatus;
	
	public StatusSensorDTO(LocalDateTime dateTime, Long sensorId, String status, String classStatus) {
		super();
		this.dateTime = dateTime;
		this.sensorId = sensorId;
		this.status = status;
		this.classStatus = classStatus;
	}	
	
}
