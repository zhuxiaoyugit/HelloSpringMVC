package com.yiibai.tutorial.log4j;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/log4j")
public class Hellocon {

	private static final Logger log=Logger.getLogger(Hellocon.class);
	
	@RequestMapping()
	public String print(ModelMap model){
		log.info("print started.");
		
		if (log.isDebugEnabled()) {
			log.debug("Inside: print");
		}
		
//		log.error("Logging a sample exception", new Exception("Testing"));
		
		model.addAttribute("message", "Hello Spring MVC");
		log.info("print ended");
		
		return "helloworld";
	}
}
