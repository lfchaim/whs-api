package com.whs.whsapi.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class JSONUtil {

	public static String toJSONString( Map<?,?> map ) {
		String ret = null;
		ObjectMapper om = new ObjectMapper();
		try {
			ret =  om.writeValueAsString(map);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public static String toJSONString( List<?> list ) {
		String ret = null;
		ObjectMapper om = new ObjectMapper();
		try {
			ret =  om.writeValueAsString(list);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public static String toJSONString( Object obj ) {
		String ret = null;
		ObjectMapper om = new ObjectMapper();
		try {
			ret =  om.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public static boolean isJSON( String val ) {
		boolean ret = false;
		if( val != null ) {
			val = val.trim();
			if( val.startsWith("{") && val.endsWith("}") )
				return true;
			if( val.startsWith("[") && val.endsWith("]") )
				return true;
			
		}
		return ret;
	}
	
	public static Map<String,Object> readValue( String json ){
		Map<String,Object> map = null;
		try {
			ObjectMapper om = new ObjectMapper();
			map = om.readValue(json,Map.class);
		} catch (Exception e) {
			System.err.println("JSONUtil.readValue json: "+json);
			e.printStackTrace();
		}
		return map;
	}

	public static Object readObject( String json ){
		Object obj = null;
		try {
			ObjectMapper om = new ObjectMapper();
			obj = om.readValue(json,Object.class);
		} catch (Exception e) {
			System.err.println("JSONUtil.readValue json: "+json);
			e.printStackTrace();
		}
		return obj;
	}

	public static List<String> getAllKeysInJsonUsingJsonNodeFields(String json, ObjectMapper mapper) throws JsonMappingException, JsonProcessingException {
        List<String> keys = new ArrayList<>();
        JsonNode jsonNode = mapper.readTree(json);
        getAllKeysUsingJsonNodeFields(jsonNode, keys);
        return keys;
    }
	
	public static void getAllKeysUsingJsonNodeFields(JsonNode jsonNode, List<String> keys) {
	    if (jsonNode.isObject()) {
	        Iterator<Entry<String, JsonNode>> fields = jsonNode.fields();
	        fields.forEachRemaining(field -> {
	            keys.add(field.getKey());
	            getAllKeysUsingJsonNodeFieldNames((JsonNode) field.getValue(), keys);
	        });
	    } else if (jsonNode.isArray()) {
	        ArrayNode arrayField = (ArrayNode) jsonNode;
	        arrayField.forEach(node -> {
	            getAllKeysUsingJsonNodeFieldNames(node, keys);
	        });
	    }
	}

	public static void getAllKeysUsingJsonNodeFieldNames(JsonNode jsonNode, List<String> keys) {
        if (jsonNode.isObject()) {
            Iterator<String> fieldNames = jsonNode.fieldNames();
            fieldNames.forEachRemaining(fieldName -> {
                keys.add(fieldName);
                getAllKeysUsingJsonNodeFieldNames(jsonNode.get(fieldName), keys);
            });
        } else if (jsonNode.isArray()) {
            ArrayNode arrayField = (ArrayNode) jsonNode;
            arrayField.forEach(node -> {
                getAllKeysUsingJsonNodeFieldNames(node, keys);
            });
        }
    }
	
	public static List<String> getKeysInJsonUsingJsonParser(String json) throws JsonParseException, IOException {
	    List<String> keys = new ArrayList<>();
	    JsonFactory factory = new JsonFactory();
	    JsonParser jsonParser = factory.createParser(json);
	    while (!jsonParser.isClosed()) {
	        if (jsonParser.nextToken() == JsonToken.FIELD_NAME) {
	            keys.add((jsonParser.getCurrentName()));
	        }
	    }
	    return keys;
	}

}
