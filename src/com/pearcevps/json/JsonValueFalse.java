package com.pearcevps.json;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import com.pearcevps.utils.Output;

public class JsonValueFalse extends JsonValue {

	static public boolean isJsonBooleanFalse(String value) {
		if (value == null) {
			return false;
		}
		return value.equalsIgnoreCase("false");
	}

	@Override
	public void deserializeJsonValueToBean(Field field, Object fieldsBean)
			throws IllegalArgumentException, IllegalAccessException, JsonException {
		if ((field == null) || (fieldsBean == null)) {
			throw new JsonException("Can't deserialize into a null field or bean");
		}
		boolean jsonProperty = false;

		if (((field.getType() == java.lang.Boolean.class) || field.getType().equals(Boolean.TYPE))) {
			Annotation[] annotations = field.getAnnotations();
			for (int count = 0; count < annotations.length; count++) {
				if (annotations[count].toString().contains(Json_Property.class.getCanonicalName())) {
					jsonProperty = true;

					if (!field.isAccessible()) {
						field.setAccessible(true);
					}
					if (field.getType().equals(Boolean.TYPE)) {
						field.setBoolean(fieldsBean, false);
					} else {
						field.set(fieldsBean, false);
					}
					if (JsonValue.debug) {
						Output.logdebug(Output.padStr("JsonValueFalse::deserializeJsonValueToBean: ", 60)
								+ "Deserialising field \"" + field.getName() + "\" (of type boolean) to \"false\"");
					}
					break;
				}
			}
		}

		if (!jsonProperty) {
			if (JsonValue.debug) {
				Output.logdebug(Output.padStr("JsonValueFalse::deserializeJsonValueToBean: ", 60) + "Field \""
						+ field.getName() + "\" (of type boolean) ignored becuase not a @JsonProperty");
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
			Output.logdebug(Output.padStr("JsonValueFalse::deserializeJsonValueToMapValue: ", 60)
					+ "Trying to deserialize field \"" + name + "\" into a map value class of type "
					+ mapValueClass.getName() + "...");
		}

		if ((mapValueClass != null) && mapValueClass.equals(java.lang.String.class)) {
			map.put(name, "false");
			if (JsonValue.debug) {
				Output.logdebug(Output.padStr("JsonValueFalse::deserializeJsonValueToMapValue: ", 60)
						+ "Deserializing \"" + name + "\" with value \"false\" into Map<String,String>");
			}
		} else if ((mapValueClass != null) && mapValueClass.equals(java.lang.Boolean.class)) {
			map.put(name, new Boolean(true));
			if (JsonValue.debug) {
				Output.logdebug(Output.padStr("JsonValueFalse::deserializeJsonValueToMapValue: ", 60)
						+ "Deserializing \"" + name + "\" with value false into Map<String,Boolean>");
			}
		} else {
			if (JsonValue.debug) {
				Output.logdebug(Output.padStr("JsonValueFalse::deserializeJsonValueToMapValue: ", 60)
						+ "Failed to deserialize \"" + name + "\" into a map with value type of "
						+ mapValueClass.getName() + " - ignoring");
			}
		}
	}

	@Override
	public boolean isNull() {
		return false;
	}

	@Override
	public String toString() {
		return "false";
	}
}
