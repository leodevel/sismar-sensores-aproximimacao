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
public class SensorProximidadeMarcacao {

	private Long cod;
	private SensorProximidade sensorProximidade;
	
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime dataLeitura;
	
	private Double distancia;	
	
}