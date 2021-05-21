package br.com.marinoprojetos.sismarsensoresaproximacao.dtos;

import org.modelmapper.ModelMapper;

import br.com.marinoprojetos.sismarsensoresaproximacao.enums.ModeloSensor;
import br.com.marinoprojetos.sismarsensoresaproximacao.models.Sensor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SensorDTO {

	private Long id;	
	private String serial;
	private String porta;
	private Integer velocidadeDados;
	private Integer bitsDados;
	private Integer bitParada;
	private Integer paridade;	
	private ModeloSensor modelo;
	private boolean iniciarAutomaticamente;
	private String descricao;
	
	public static SensorDTO fromEntity(Sensor sensor) {
		ModelMapper mapper = new ModelMapper();
		return mapper.map(sensor, SensorDTO.class);
	}
	
}
