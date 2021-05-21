package br.com.marinoprojetos.sismarsensoresaproximacao.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class Utils {

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
