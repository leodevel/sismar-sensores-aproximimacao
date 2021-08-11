package br.com.marinoprojetos.sismarsensoresaproximacao.models;

import java.io.Serializable;

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
@Table(name = "config")
@ToString
public class Config implements Serializable {

	private static final long serialVersionUID = 3042195569410336091L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "apiurl")
	private String apiUrl;
	
	@Column(name = "token")
	private String token;	
	
	@Column(name = "gravardadoslocal")
	private boolean gravarDadosLocal;
	
	@Column(name = "osusername")
	private String osUsername;

	@Column(name = "ospassword")
	private String osPassword;
	
}
