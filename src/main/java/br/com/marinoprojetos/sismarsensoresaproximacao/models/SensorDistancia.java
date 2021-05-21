package br.com.marinoprojetos.sismarsensoresaproximacao.models;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@Table(name = "sensor_distancias")
@ToString
public class SensorDistancia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "idsensor")
	private Long idSensor;	
	
	@Column(name = "dataleitura")
	private LocalDateTime dataLeitura;	
	
	@Column(name = "distancia")
	private Double distancia;		
	
}