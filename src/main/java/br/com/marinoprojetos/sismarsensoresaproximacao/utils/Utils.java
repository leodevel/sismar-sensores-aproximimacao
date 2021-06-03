package br.com.marinoprojetos.sismarsensoresaproximacao.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Enumeration;

public class Utils {

	public static InetAddress getLocalHostLANAddress() throws UnknownHostException {
	    try {
	        InetAddress candidateAddress = null;

	        for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
	            
	        	NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
	            
	            for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
	            
	            	InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
	                
	            	if (!inetAddr.isLoopbackAddress()) {

	                    if (inetAddr.isSiteLocalAddress()) {
	                        return inetAddr;
	                    } else if (candidateAddress == null) {
	                        candidateAddress = inetAddr;
	                    }
	                    
	                }
	            }
	        }
	        
	        if (candidateAddress != null) {
	            return candidateAddress;
	        }

	        InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
	        if (jdkSuppliedAddress == null) {
	            throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
	        }
	        
	        return jdkSuppliedAddress;
	        
	    }
	    catch (Exception e) {
	        UnknownHostException unknownHostException = new UnknownHostException("Failed to determine LAN address: " + e);
	        unknownHostException.initCause(e);
	        throw unknownHostException;
	    }
	    
	}
	
	public static float round(float value, int casas) {
        if (casas < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(casas, RoundingMode.HALF_UP);

        return bd.floatValue();

    }

    public static double round(double value, int casas) {
        if (casas < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(casas, RoundingMode.HALF_UP);

        return bd.doubleValue();

    }
	
    public static LocalDateTime getNowUTC() {
    	return LocalDateTime.now(ZoneOffset.UTC);
    }
    
}
