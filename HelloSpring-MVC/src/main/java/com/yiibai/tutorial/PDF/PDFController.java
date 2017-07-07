package com.yiibai.tutorial.PDF;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class PDFController extends AbstractController{

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, String> userData = new HashMap<>();
		userData.put("100", "Xiao.Lu");
		userData.put("102", "User 102");
		userData.put("301", "User 301");
		userData.put("400", "User 400");
		
		return new ModelAndView("UserSummary", "userData", userData);
	}

}
