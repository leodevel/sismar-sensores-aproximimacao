package br.com.marinoprojetos.sismarsensoresaproximacao.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import br.com.marinoprojetos.sismarsensoresaproximacao.models.Config;
import br.com.marinoprojetos.sismarsensoresaproximacao.models.Sensor;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.ConfigService;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.LogService;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.SensorDistanciaService;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.SensorReadService;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.SensorService;

@Controller
public class HomeController {

	@Autowired
	private SensorService sensorService;	
	
	@Autowired
	private SensorDistanciaService sensorDistanciaService;
	
	@Autowired
	private SensorReadService sensorReadService;
		
	@Autowired
	private LogService logService;	
	
	@Autowired
	private ConfigService configService;
	
	@GetMapping("/")
    public String home(Model model) {
        
		List<Sensor> sensors = sensorService.findAll();		
		Config config = configService.getConfig();
		config = config == null ? new Config() : config;
				
		sensors.forEach(sensor -> {
			sensor.setIniciado(sensorReadService.isStarted(sensor));
			sensor.setTotalBuffer(sensorDistanciaService.countByIdSensor(sensor.getId()));
		});		
		
		model.addAttribute("sensors", sensors);
		model.addAttribute("config", config);
		model.addAttribute("logs", logService.getLog());	
		
        return "home";
        
    }
	
	@PostMapping("/delete")
	public String delete(@ModelAttribute("sensorRemove") Sensor sensor) {		
		sensorService.deleteById(sensor.getId());
		return "redirect:/";		
	}
	
	@PostMapping("/config")
	public String saveConfig(@ModelAttribute("config") Config config) {		
		configService.save(config);
		return "redirect:/";
	}	
	
	@PostMapping("/start")
	public String start(@ModelAttribute("sensorStart") Sensor sensor) {				
		sensorReadService.start(sensorService.findById(sensor.getId()));		
		return "redirect:/";
	}
	
	@PostMapping("/clearBuffer")
	public String clearBuffer(@ModelAttribute("sensorBuffer") Sensor sensor) {				
		sensorDistanciaService.deleteAllByIdSensor(sensor.getId());
		return "redirect:/";
	}
	
	@PostMapping("/stop")
	public String stop(@ModelAttribute("sensorStop") Sensor sensor) {				
		sensorReadService.stop(sensorService.findById(sensor.getId()));		
		return "redirect:/";
	}
	
}