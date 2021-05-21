package br.com.marinoprojetos.sismarsensoresaproximacao.services;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import br.com.marinoprojetos.sismarsensoresaproximacao.models.WebSocketSubscribe;

@Service
public class WebSocketSessionService {

	private List<WebSocketSubscribe> subscribes;
	
	@PostConstruct
	private void init() {
		subscribes = new ArrayList<>();
	}
	
	public void subscribe(String sessionId, String topic) {
		if (subscribes.stream().noneMatch(subscribe -> subscribe.getSessionId().equalsIgnoreCase(sessionId)
			&& subscribe.getTopic().equalsIgnoreCase(topic))) {
			subscribes.add(new WebSocketSubscribe(sessionId, topic));
		}
	}
	
	public void disconnectClient(String sessionId) {
		subscribes.removeIf(subscribe -> subscribe.getSessionId().equalsIgnoreCase(sessionId));		
	}
	
	public boolean isTopicConnected(String topic) {		
		return subscribes.stream().anyMatch(subscribe -> subscribe.getTopic().equalsIgnoreCase(topic));		
	}
	
}