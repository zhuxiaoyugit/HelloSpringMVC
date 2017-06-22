package com.mongo.web;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mongo.entity.Report;

public interface ReportRepository extends MongoRepository<Report, String>
{
	Report findByTitle(String title);
	List<Report> findByDate(String date);
}
