package com.pearcevps.json;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import com.pearcevps.utils.Output;

public class JsonValueNull extends JsonValue {

	static public boolean isJsonNull(String value) {
		if (value == null) {
			return false;
		}
		if (value.isEmpty()) {
			return true;
		}
		return value.equalsIgnoreCase("null");
	}

	@Override
	public void deserializeJsonValueToBean(Field field, Object fieldsBean)
			throws IllegalArgumentException, IllegalAccessException, JsonException {
		if ((field == null) || (fieldsBean == null)) {
			throw new JsonException("Can't deserialize into a null field or bean");
		}
		boolean jsonProperty = false;

		///////////////////////
		// Found a match of type string
		Annotation[] annotations = field.getAnnotations();
		for (int count = 0; count < annotations.length; count++) {
			if (annotations[count].toString().contains(Json_Property.class.getCanonicalName())) {
				jsonProperty = true;

				if (!field.isAccessible()) {
					field.setAccessible(true);
				}
				String fieldType = field.getType().getName();
				if (!field.getType().isPrimitive()) {
					field.set(fieldsBean, null);
					if (JsonValue.debug) {
						Output.logdebug(Output.padStr("JsonValueNull::deserializeJsonValueToBean: ", 60)
								+ "Deserialising field \"" + field.getName() + "\" of type \"" + fieldType
								+ "\" to \"null\"");
					}
				} else {
					if (JsonValue.debug) {
						Output.logdebug(Output.padStr("JsonValueNull::deserializeJsonValueToBean: ", 60)
								+ "Deserialising field \"" + field.getName() + "\" of type \"" + fieldType
								+ "\" to non-initialised value");
					}
				}
				break;
			}
		}
		if (!jsonProperty) {
			if (JsonValue.debug) {
				Output.logdebug(
						"Field \"" + field.getName() + "\" (of type NULL) ignored becuase not a @JsonProperty");
			}
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void deserializeJsonValueToMapValue(Map map, String name, Class mapValueClass)
			throws JsonException {
		if ((map == null) || (name == null) || name.isEmpty() || (mapValueClass == null)) {
			throw new JsonException(
					"Cannot desrialize Json Value to a null map, or map without an indicated dvalue class, or to an existing map without a value name");
		}

		if (debug) {
			Output.logdebug(Output.padStr("JsonValueNull::deserializeJsonValueToMapValue: ", 60)
					+ "Trying to deserialize field \"" + name + "\" into a map value class of type "
					+ mapValueClass.getName() + "...");
		}

		map.put(name, null);
		if (JsonValue.debug) {
			Output.logdebug(
					Output.padStr("JsonValueNull::deserializeJsonValueToMapValue: ", 60) + "Deserializing \""
							+ name + "\" with a null value into Map<String," + mapValueClass.getName() + ">");
		}
	}

	@Override
	public boolean isNull() {
		return true;
	}

	@Override
	public String toString() {
		return "null";
	}

}
