package br.com.marinoprojetos.sismarsensoresaproximacao.models;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import br.com.marinoprojetos.sismarsensoresaproximacao.enums.ModeloSensor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name = "sensores")
@ToString
public class Sensor implements Serializable {

	private static final long serialVersionUID = 3042195569410336091L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String serial;
	private String porta;
	
	@Column(name = "velocidadedados")
	private Integer velocidadeDados;
	
	@Column(name = "bitsdados")
	private Integer bitsDados;
	
	@Column(name = "bitparada")
	private Integer bitParada;
	
	@Column(name = "paridade")
	private Integer paridade;	
	
	@Enumerated(EnumType.STRING)
	private ModeloSensor modelo;
	
	@Column(name = "iniciarautomaticamente")
	private boolean iniciarAutomaticamente;
	
	@Column(name = "descricao")
	private String descricao;
	
	@Transient
	private boolean iniciado;
	
	@Transient
	private long totalBuffer;
	
}
