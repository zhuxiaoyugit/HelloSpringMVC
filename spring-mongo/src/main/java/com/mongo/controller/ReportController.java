package com.mongo.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mongo.entity.Report;
import com.mongo.service.ReportService;

@RestController
@RequestMapping("/report")
public class ReportController
{
	private static final Logger logger = Logger.getLogger(ReportController.class);
	@Autowired
	ReportService reportService;

	@RequestMapping(method = RequestMethod.GET,value="/save")
	public Map<String, Object> createReport()
	{
		logger.info("createReport");
		Report report = reportService.createReport();

		Map<String, Object> response = new LinkedHashMap<String, Object>();
		response.put("message", "Report created successfully");
		response.put("report", report);

		return response;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{reportTitle}")
	public Report getReportDetails(@PathVariable("reportTitle") String title)
	{
		logger.info("getReportDetails");
		return reportService.getReportDetails(title);
	}
}
