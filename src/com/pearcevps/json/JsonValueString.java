package com.pearcevps.json;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.Map;
import com.pearcevps.utils.Output;

public class JsonValueString extends JsonValue {

	private String			javaStringValue;
	private JsonString	jsonStringValue;

	public JsonValueString(String javaStringValue) {
		super();
		this.javaStringValue = javaStringValue;
		this.jsonStringValue = new JsonString(javaStringValue);

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
		if ((field.getType() == java.lang.String.class) || (field.getType() == java.lang.Character.class)
				|| (field.getType() == java.net.URI.class) || field.getType().equals(Character.TYPE)) {
			Annotation[] annotations = field.getAnnotations();
			for (int count = 0; count < annotations.length; count++) {
				if (annotations[count].toString().contains(Json_Property.class.getCanonicalName())) {
					jsonProperty = true;

					if (!field.isAccessible()) {
						field.setAccessible(true);
					}
					String value = this.getJavaStringValue();

					// NULL values, primitives
					if ((value == null) && field.getType().equals(Character.TYPE)) {
						field.setChar(fieldsBean, (char) 0);
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueString::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type char) to 0");
						}
					}

					// NULL values, classes
					else if ((value == null) && (field.getType() == java.lang.Character.class)) {
						field.set(fieldsBean, null);
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueString::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type Character) to null");
						}
					} else if ((value == null) && (field.getType() == java.lang.String.class)) {
						field.set(fieldsBean, null);
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueString::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type String) to null");
						}
					} else if ((value == null) && (field.getType() == java.net.URI.class)) {
						field.set(fieldsBean, null);
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueString::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type URI) to null");
						}
					}

					// Non NULL values, empty value, primitives
					if ((value != null) && value.isEmpty() && field.getType().equals(Character.TYPE)) {
						field.setChar(fieldsBean, (char) 0);
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueString::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type char) to 0");
						}
					}
					// Non NULL values, empty value, classes
					else if ((value != null) && value.isEmpty()
							&& (field.getType() == java.lang.Character.class)) {
						field.set(fieldsBean, null);
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueString::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type Character) to null");
						}
					} else if ((value != null) && value.isEmpty() && (field.getType() == java.lang.String.class)) {
						field.set(fieldsBean, null);
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueString::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type String) to null");
						}
					} else if ((value != null) && value.isEmpty() && (field.getType() == java.net.URI.class)) {
						field.set(fieldsBean, null);
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueString::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type URI) to null");
						}
					}
					// Non NULL values, primitives
					else if ((value != null) && field.getType().equals(Character.TYPE)) {
						field.setChar(fieldsBean, value.charAt(0));
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueString::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type char) to "
									+ value.charAt(0));
						}
					}
					// Non NULL values, classes
					else if ((value != null) && !value.isEmpty()
							&& (field.getType() == java.lang.Character.class)) {
						field.set(fieldsBean, new Character(value.charAt(0)));
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueString::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type Character) to "
									+ value.charAt(0));
						}
					} else if ((value != null) && !value.isEmpty()
							&& (field.getType() == java.lang.String.class)) {
						field.set(fieldsBean, value);
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueString::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type String) to \"" + value
									+ "\"");
						}
					} else if ((value != null) && !value.isEmpty() && (field.getType() == java.net.URI.class)) {
						java.net.URI uri;
						try {
							uri = new java.net.URI(value);
							field.set(fieldsBean, uri);
							if (JsonValue.debug) {
								Output.logdebug(Output.padStr("JsonValueString::deserializeJsonValueToBean: ", 60)
										+ "Deserialising field \"" + field.getName() + "\" (of type URI) to \""
										+ uri.toString() + "\"");
							}
						} catch (URISyntaxException e) {
							throw new JsonException(
									"Trying to parse a URI field " + field.getName() + " which is not a vaid URI");
						}
					}
					break;
				}
			}
		}
		if (!jsonProperty) {
			if (JsonValue.debug) {
				String type = field.getType().getName();
				Output.logdebug(Output.padStr("JsonValueString::deserializeJsonValueToBean: ", 60) + "Field \""
						+ field.getName() + "\" (of type " + type + ") ignored becuase not a @JsonProperty");
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
			Output.logdebug(Output.padStr("JsonValueString::deserializeJsonValueToMapValue: ", 60)
					+ "Trying to deserialize field \"" + name + "\" into a map value class of type "
					+ mapValueClass.getName() + "...");
		}

		if ((mapValueClass != null) && mapValueClass.equals(java.lang.String.class)) {
			map.put(name, this.getJavaStringValue());
			if (JsonValue.debug) {
				Output.logdebug(Output.padStr("JsonValueString::deserializeJsonValueToMapValue: ", 60)
						+ "Deserializing \"" + name + "\" with value \"" + this.getJavaStringValue()
						+ "\" into Map<String,String>");
			}
		} else if ((mapValueClass != null) && mapValueClass.equals(java.lang.Character.class)
				&& !this.getJavaStringValue().isEmpty()) {
			char c = this.getJavaStringValue().charAt(0);
			map.put(name, new Character(c));
			if (JsonValue.debug) {
				Output.logdebug(Output.padStr("JsonValueString::deserializeJsonValueToMapValue: ", 60)
						+ "Deserializing \"" + name + "\" with value \"" + c + "\" into Map<String,Character>");
			}
		} else {
			if (JsonValue.debug) {
				Output.logdebug(Output.padStr("JsonValueString::deserializeJsonValueToMapValue: ", 60)
						+ "Failed to deserialize \"" + name + "\" into a map with value type of "
						+ mapValueClass.getName() + " - ignoring");
			}
		}
	}

	/**
	 * Get the Java string representation of this JSON string value
	 *
	 * @return
	 */
	public String getJavaStringValue() {
		return this.javaStringValue;
	}

	/**
	 * Get the JSON string representation of this JSON string value
	 *
	 * @return
	 */
	public JsonString getJsonStringValue() {
		return this.jsonStringValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.pearcevps.json.JsonValue#isNull()
	 */
	@Override
	public boolean isNull() {
		if (this.jsonStringValue == null) {
			return true;
		}
		if (this.jsonStringValue.getJsonStr() == null) {
			return true;
		}
		return false;
	}

	/**
	 * Set the Java string value for this JSON string
	 * 
	 * @param javaValue
	 */
	public void setJavaValue(String javaValue) {
		this.javaStringValue = javaValue;
	}

	/**
	 * Set the Json String value
	 * 
	 * @param jsonValue
	 */
	public void setJsonValue(JsonString jsonValue) {
		this.jsonStringValue = jsonValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.pearcevps.json.JsonValue#toString()
	 */
	@Override
	public String toString() {
		if (this.jsonStringValue == null) {
			return "";
		}
		return this.jsonStringValue.toString();
	}

	/**
	 * Get a JSON string from a Java string
	 * 
	 * @param value
	 *           The Java string to convert
	 * @return The Json string object
	 */
	public static JsonValueString createStringValue(String value) {
		return new JsonValueString(value);
	}

}
