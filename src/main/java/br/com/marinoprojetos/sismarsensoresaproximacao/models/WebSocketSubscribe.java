package br.com.marinoprojetos.sismarsensoresaproximacao.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebSocketSubscribe {

	private String sessionId;
	private String topic;
	
	public WebSocketSubscribe(String sessionId, String topic) {
		super();
		this.sessionId = sessionId;
		this.topic = topic;
	}	
	
}
