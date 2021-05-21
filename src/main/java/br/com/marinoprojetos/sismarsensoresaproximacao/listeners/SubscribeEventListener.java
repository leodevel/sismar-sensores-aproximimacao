package br.com.marinoprojetos.sismarsensoresaproximacao.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import br.com.marinoprojetos.sismarsensoresaproximacao.services.WebSocketSessionService;

@Component
public class SubscribeEventListener implements ApplicationListener<SessionSubscribeEvent> {

	@Autowired
	private WebSocketSessionService webSocketSessionService;
	
	@Override
	public void onApplicationEvent(SessionSubscribeEvent event) {
		
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap (event.getMessage ());
		webSocketSessionService.subscribe(headerAccessor.getSessionId(), headerAccessor.getDestination());		
		
	}

}
