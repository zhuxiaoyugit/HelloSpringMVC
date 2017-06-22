package com.mongo.service;

import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.mongo.entity.Report;
import com.mongo.web.ReportRepository;

@Service
public class ReportService
{
	@Autowired
	private ReportRepository reportRepository;
	
	public Report createReport(){
		Report report=new Report("date","content","title");
		reportRepository.save(report);
		return report;
	}
	
	
	@Cacheable(value="reportcache",keyGenerator = "wiselyKeyGenerator")
	public Report getReportDetails(String title){
		System.out.println(title);
		return reportRepository.findByTitle(title);
	}
	
	
	
	
}
