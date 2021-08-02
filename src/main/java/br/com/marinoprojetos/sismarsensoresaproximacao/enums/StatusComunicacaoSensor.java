package br.com.marinoprojetos.sismarsensoresaproximacao.enums;

public enum StatusComunicacaoSensor {

	COMUNICANDO("Comunicando", "comunicando"),
	SEM_COMUNICACAO("Sem comunicação", "sem-comunicacao"),
	PORTA_NAO_ENCONTRADA("Porta não encontrada", "porta-nao-encontrada");
	
	String value;
	String classValue;
	
	StatusComunicacaoSensor(String value, String classValue){
		this.value = value;
		this.classValue = classValue;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getClassValue() {
		return classValue;
	}

	public void setClassValue(String classValue) {
		this.classValue = classValue;
	}	
	
}