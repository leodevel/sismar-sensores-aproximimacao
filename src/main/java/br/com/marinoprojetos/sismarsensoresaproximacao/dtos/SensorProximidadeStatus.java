package br.com.marinoprojetos.sismarsensoresaproximacao.dtos;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import br.com.marinoprojetos.sismarsensoresaproximacao.converters.LocalDateTimeDeserializer;
import br.com.marinoprojetos.sismarsensoresaproximacao.converters.LocalDateTimeSerializer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SensorProximidadeStatus {

	private SensorProximidade sensorProximidade;
	
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime dataHora;
	
	private boolean statusComunicacaoLaser;	
	
	private String ip;
	private String ultimaLeitura;
	
}