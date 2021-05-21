package br.com.marinoprojetos.sismarsensoresaproximacao.services;

import org.springframework.stereotype.Service;

@Service
public class UtilService {

	public boolean isNullOrEmpty(String value) {
        if (value == null) {
            return true;
        }
        return value.trim().isEmpty();
    }
	
	public boolean isNullOrEmpty(Object ...values) {        
		for(Object value : values) {
			if (value == null) {
				return true;
			}
			if (value instanceof String) {
				if (value.toString().trim().isEmpty()) {
					return true;
				}
			}
		}		
		return false;
    }
	
}