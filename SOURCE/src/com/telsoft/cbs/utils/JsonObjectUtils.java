package com.telsoft.cbs.utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class JsonObjectUtils {

	public static String jsonify(Object o) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		return mapper.writeValueAsString(o);
	}

	public static String jsonifyNoThrow(Object o) {
		if (o instanceof String) {
			return (String) o;
		}
		try {
			return jsonify(o);
		} catch (JsonProcessingException e) {
			log.error("Error when Parse object to Json string");
			return null;
		}
	}

	public static <T> T parseJson(String o, Class<T> clazz) throws IOException, JsonParseException, JsonMappingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(o, clazz);
	}

}
