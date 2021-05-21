package br.com.marinoprojetos.sismarsensoresaproximacao.services;

import org.springframework.stereotype.Service;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

@Service
public class SerialUtilsService {

	public boolean existPort(SerialPort serial, String port) {
        for (String p : SerialPortList.getPortNames()) {
            if (p.equalsIgnoreCase(port)) {
                if (serial != null) {
                    try {
                        if (serial.getEventsMask() < 0) {
                            return false;
                        }
                    } catch (SerialPortException ex) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
	
	public void setFlowControlModel(int mode, SerialPort serial) {
        try {
            serial.setFlowControlMode(mode);
        } catch (SerialPortException ex) {
        }
    }
	
}