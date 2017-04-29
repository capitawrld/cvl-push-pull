package com.capitaworld.service.loans.utils;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import com.fasterxml.jackson.databind.DeserializationFeature;

/**
 * Created by win7 on 11/16/2016.
 */
public class MultipleJSONObjectHelper implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MultipleJSONObjectHelper() {
	}

	public static <T> T getObject(String data, String key, Class<?> clazz) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(data);
		return (T) mapper.convertValue(node.get(key), clazz);
	}

	public static List getListOfObjects(String data, String key, Class<?> clazz) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readTree(data);
		return mapper.readValue(node.get(key), mapper.getTypeFactory().constructCollectionType(List.class, clazz));
	}

	public static <T> T getObjectFromMap(Map map, Class<?> clazz) throws IOException {
		final ObjectMapper mapper = new ObjectMapper(); // jackson's
		mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return (T) mapper.convertValue(map, clazz);
	}

//	public static <T> T getListFromMap(List<Map<String, Object>> map, Class<?> clazz) throws IOException {
//		final ObjectMapper mapper = new ObjectMapper(); // jackson's
//		return mapper.readValue(map, mapper.getTypeFactory().constructCollectionType(List.class, clazz));
//	}

}
