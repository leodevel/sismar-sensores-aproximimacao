package br.com.marinoprojetos.sismarsensoresaproximacao.services;

import org.springframework.stereotype.Service;

import com.fazecast.jSerialComm.SerialPort;

@Service
public class SerialUtilsService {

	public boolean existPort(SerialPort serial, String port) {
        for (SerialPort p : SerialPort.getCommPorts()) {
            if (p.getSystemPortName().equalsIgnoreCase(port)) {
                return true;
            }
        }
        return false;
    }
	
	public void setFlowControlModel(int mode, SerialPort serial) {
        try {
            serial.setFlowControl(mode); 
        } catch (Exception ex) {
        }
    }
	
}