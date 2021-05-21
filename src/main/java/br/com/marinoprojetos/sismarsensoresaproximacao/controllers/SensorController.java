package br.com.marinoprojetos.sismarsensoresaproximacao.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.marinoprojetos.sismarsensoresaproximacao.clients.SensorProximidadeClient;
import br.com.marinoprojetos.sismarsensoresaproximacao.dtos.SensorProximidade;
import br.com.marinoprojetos.sismarsensoresaproximacao.models.Sensor;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.ConfigService;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.SensorReadService;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.SensorService;
import br.com.marinoprojetos.sismarsensoresaproximacao.services.UtilService;

@Controller
@RequestMapping("/sensor")
public class SensorController {

	@Autowired
	private SensorService sensorService;
	
	@Autowired
	private SensorProximidadeClient sensorProximidadeClient;
	
	@Autowired
	private SensorReadService sensorReadService;
	
	@Autowired
	private ConfigService configService;
	
	@Autowired
	private UtilService utilService;
	
	@GetMapping("/add")
    public String add(Model model) {
		
		List<SensorProximidade> sensoresClient = sensorProximidadeClient.findAll(configService.getApiUrl()).getResposta();     
		Sensor sensor = new Sensor();
		
		model.addAttribute("sensoresClient", sensoresClient);
		model.addAttribute("sensor", sensor);
		
        return "sensor/add-edit";
        
    }
	
	@GetMapping("/edit/{id}")
    public String edit(@PathVariable(name = "id") Long id, Model model) {        		
			
		Sensor sensor = sensorService.findById(id);			
		if (sensorReadService.isStarted(sensor)) {
			return "redirect:/";
		}
		
		List<SensorProximidade> sensoresClient = sensorProximidadeClient.findAll(configService.getApiUrl()).getResposta();
		
		model.addAttribute("sensoresClient", sensoresClient);
		model.addAttribute("sensor", sensor);
		
        return "sensor/add-edit";
        
    }
	
	@GetMapping("/monitor/{id}")
    public String monitor(@PathVariable(name = "id") Long id, Model model) {        		
			
		Sensor sensor = sensorService.findById(id);			
		if (!sensorReadService.isStarted(sensor)) {
			return "redirect:/";
		}
				
		model.addAttribute("sensor", sensor);
		
        return "sensor/monitor";

    }
	
	@PostMapping("/save")
	public String save(@ModelAttribute("sensor") Sensor sensor, 
			BindingResult result, 
			RedirectAttributes redir,
			Model model) {
		
		Sensor sensorOld = sensorService.findBySerial(sensor.getSerial());
		
		if (sensor.getId() == null) {
			
			if (sensorOld != null) {				
				// objeto já cadastrado			
				result.addError(new ObjectError("errors", "Sensor já está configurado"));
				List<SensorProximidade> sensoresClient = sensorProximidadeClient.findAll(configService.getApiUrl()).getResposta();
				model.addAttribute("sensoresClient", sensoresClient);
				return "sensor/add-edit";
			}

		} else {
			
			if (sensorOld != null && sensorOld.getId().longValue() != sensor.getId().longValue()) {
				// objeto já cadastrado
				result.addError(new ObjectError("errors", "Sensor já está configurado"));
				List<SensorProximidade> sensoresClient = sensorProximidadeClient.findAll(configService.getApiUrl()).getResposta();
				model.addAttribute("sensoresClient", sensoresClient);
				return "sensor/add-edit";
			}			
			
		}
		
		
		if (utilService.isNullOrEmpty(sensor.getDescricao(), sensor.getSerial(), sensor.getPorta(), 
				sensor.getVelocidadeDados(), sensor.getBitsDados(), sensor.getBitParada(), sensor.getParidade(),
				sensor.getModelo())) {
			result.addError(new ObjectError("errors", "Campos obrigatórios não foram informados"));
			List<SensorProximidade> sensoresClient = sensorProximidadeClient.findAll(configService.getApiUrl()).getResposta();
			model.addAttribute("sensoresClient", sensoresClient);
			return "sensor/add-edit";
		}
				
		sensorService.save(sensor);		
		return "redirect:/";
		
	}
	
}