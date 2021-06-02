package br.com.marinoprojetos.sismarsensoresaproximacao.jobs;

import java.util.ArrayList;
import java.util.List;

import br.com.marinoprojetos.sismarsensoresaproximacao.dtos.SensorDTO;

public class Teste {

	public static void main(String args[]) {
		
		
		List<SensorDTO> teste = new ArrayList<>();
		
		SensorDTO s1 = new SensorDTO();
		s1.setId(10L);
		
		SensorDTO s2 = new SensorDTO();
		s2.setId(15L);
		
		SensorDTO s3 = new SensorDTO();
		s3.setId(20L);
		
		teste.add(s1);
		teste.add(s2);
		teste.add(s3);
		
		List<SensorDTO> novo = new ArrayList<>();
		teste.forEach(obj -> novo.add(obj));
		
		SensorDTO s4 = new SensorDTO();
		s4.setId(28L);
		
		teste.add(s4);
		
		teste.removeAll(novo);
		
		System.out.println(teste.size());
		teste.forEach(obj -> System.out.println(obj));
		
	}
	
}
