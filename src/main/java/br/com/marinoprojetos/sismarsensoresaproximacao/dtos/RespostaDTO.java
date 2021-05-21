package br.com.marinoprojetos.sismarsensoresaproximacao.dtos;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RespostaDTO<T> {

	private T resposta;
	private Boolean sucesso;
	private String mensagem;
	private int status;
	
}