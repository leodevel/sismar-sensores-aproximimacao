package br.com.marinoprojetos.sismarsensoresaproximacao.dtos;

import br.com.marinoprojetos.sismarsensoresaproximacao.enums.ModeloSensor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SensorProximidade {

	private Long cod;	
	private String serial;	
	private Long lado;
	private ModeloSensor modelo;
	private Long codBerco;
	
}