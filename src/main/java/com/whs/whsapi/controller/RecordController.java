package com.whs.whsapi.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.whs.whsapi.exception.ErrorCode;
import com.whs.whsapi.service.RecordService;

public class RecordController {

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/{table}", method = RequestMethod.POST, headers = "Content-Type=application/json")
	public ResponseEntity<?> create(
			@PathVariable("table") String table, 
			@RequestBody Object record,
			@RequestParam LinkedMultiValueMap<String, String> params) {
		
		RecordService service = new RecordService();
		
		if (!service.exists(table)) {
			ErrorCode ec = ErrorCode.TABLE_NOT_FOUND;
			Map<String,Object> mapErr = new LinkedHashMap<>();
			mapErr.put("code", ec.value());
			mapErr.put("message", ec.getMessage(table));
			return new ResponseEntity<>( mapErr, HttpStatus.UNPROCESSABLE_ENTITY);
		}
		
		if (record instanceof ArrayList<?>) {
			ArrayList<?> records = (ArrayList<?>) record;
			ArrayList<Object> result = new ArrayList<>();
			for (int i = 0; i < records.size(); i++) {
				Map<String,Object> mapRec = (Map<String,Object>)records.get(i);
				result.add(service.create(table, mapRec, params));
			}
			return new ResponseEntity<>(result, HttpStatus.OK);
		} else {
			Map<String,Object> mapRec = (Map<String,Object>)record;
			return new ResponseEntity<>(service.create(table, mapRec, params), HttpStatus.OK);
		}
	}
	
}
