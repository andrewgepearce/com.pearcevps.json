package com.pearcevps.json;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.pearcevps.utils.Output;

public class JsonValueNumberLong extends JsonValue {

	private Long value;

	public JsonValueNumberLong(long value) {
		super();
		this.value = new Long(value);
	}

	static public boolean isJsonLong(String value) {
		Pattern p = Pattern.compile("(-?)(\\p{Digit}*)");
		Matcher m = p.matcher(value);
		return m.matches();
	}

	@Override
	public void deserializeJsonValueToBean(Field field, Object fieldsBean)
			throws IllegalArgumentException, IllegalAccessException, JsonException {
		if ((field == null) || (fieldsBean == null)) {
			throw new JsonException("Can't deserialize into a null field or bean");
		}
		boolean jsonProperty = false;

		if ((field.getType() == java.lang.Long.class) || field.getType().equals(Long.TYPE)
				|| (field.getType() == java.lang.Integer.class) || field.getType().equals(Integer.TYPE)
				|| (field.getType() == java.lang.Short.class) || field.getType().equals(Short.TYPE)) {
			Annotation[] annotations = field.getAnnotations();

			for (int count = 0; count < annotations.length; count++) {
				if (annotations[count].toString().contains(Json_Property.class.getCanonicalName())) {
					jsonProperty = true;

					if (!field.isAccessible()) {
						field.setAccessible(true);
					}
					// NULL values, primitives
					if ((this.value == null) && field.getType().equals(Long.TYPE)) {
						field.setLong(fieldsBean, 0);
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueNumberLong::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type long) to 0");
						}
					} else if ((this.value == null) && field.getType().equals(Integer.TYPE)) {
						field.setInt(fieldsBean, 0);
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueNumberLong::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type integer) to 0");
						}
					} else if ((this.value == null) && field.getType().equals(Short.TYPE)) {
						field.setShort(fieldsBean, (short) 0);
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueNumberLong::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type short) to 0");
						}
					}
					// NULL values, classes
					else if ((this.value == null) && (field.getType() == java.lang.Long.class)) {
						field.set(fieldsBean, null);
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueNumberLong::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type Long) to null");
						}
					} else if ((this.value == null) && (field.getType() == java.lang.Integer.class)) {
						field.set(fieldsBean, null);
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueNumberLong::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type Integer) to null");
						}
					} else if ((this.value == null) && (field.getType() == java.lang.Short.class)) {
						field.set(fieldsBean, null);
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueNumberLong::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type Short) to null");
						}
					}
					// Non NULL values, primitives
					else if ((this.value != null) && field.getType().equals(Long.TYPE)) {
						field.setLong(fieldsBean, this.value.longValue());
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueNumberLong::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type long) to "
									+ this.value.longValue());
						}
					} else if ((this.value != null) && field.getType().equals(Integer.TYPE)) {
						field.setInt(fieldsBean, this.value.intValue());
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueNumberLong::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type integer) to "
									+ this.value.intValue());
						}
					} else if ((this.value != null) && field.getType().equals(Short.TYPE)) {
						field.setShort(fieldsBean, this.value.shortValue());
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueNumberLong::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type short) to "
									+ this.value.shortValue());
						}
					}
					// Non NULL values, classes
					else if ((this.value != null) && (field.getType() == java.lang.Long.class)) {
						field.set(fieldsBean, this.value);
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueNumberLong::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type Long) to "
									+ this.value.longValue());
						}
					} else if ((this.value != null) && (field.getType() == java.lang.Integer.class)) {
						field.set(fieldsBean, new Integer(this.value.intValue()));
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueNumberLong::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type Integer) to "
									+ this.value.intValue());
						}
					} else if ((this.value != null) && (field.getType() == java.lang.Short.class)) {
						field.set(fieldsBean, new Short(this.value.shortValue()));
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueNumberLong::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type Short) to "
									+ this.value.shortValue());
						}
					}
					break;
				}
			}
		}

		if (!jsonProperty) {
			if (JsonValue.debug) {
				Output.logdebug(Output.padStr("JsonValueNumberLong::deserializeJsonValueToBean: ", 60)
						+ "Field \"" + field.getName()
						+ "\" (of type Long, Integer or Short) ignored becuase not a @JsonProperty");
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
			Output.logdebug(Output.padStr("JsonValueNumberLong::deserializeJsonValueToMapValue: ", 60)
					+ "Trying to deserialize field \"" + name + "\" into a map value class of type "
					+ mapValueClass.getName() + "...");
		}

		if ((mapValueClass != null) && mapValueClass.equals(java.lang.String.class)) {
			map.put(name, "" + this.getValue());
			if (JsonValue.debug) {
				Output.logdebug(Output.padStr("JsonValueNumberLong::deserializeJsonValueToMapValue: ", 60)
						+ "Deserializing \"" + name + "\" with value " + this.getValue()
						+ " into Map<String,String>");
			}
		} else if ((mapValueClass != null) && mapValueClass.equals(java.lang.Long.class)) {
			map.put(name, new Long(this.getValue()));
			if (JsonValue.debug) {
				Output.logdebug(Output.padStr("JsonValueNumberLong::deserializeJsonValueToMapValue: ", 60)
						+ "Deserializing \"" + name + "\" with value " + this.getValue()
						+ " into Map<String,Long>");
			}
		} else if ((mapValueClass != null) && mapValueClass.equals(java.lang.Integer.class)) {
			map.put(name, new Integer(this.getValue().intValue()));
			if (JsonValue.debug) {
				Output.logdebug(Output.padStr("JsonValueNumberLong::deserializeJsonValueToMapValue: ", 60)
						+ "Deserializing \"" + name + "\" with value " + this.getValue().intValue()
						+ " into Map<String,Integer>");
			}
		} else if ((mapValueClass != null) && mapValueClass.equals(java.lang.Short.class)) {
			map.put(name, new Short(this.getValue().shortValue()));
			if (JsonValue.debug) {
				Output.logdebug(Output.padStr("JsonValueNumberLong::deserializeJsonValueToMapValue: ", 60)
						+ "Deserializing \"" + name + "\" with value " + this.getValue().shortValue()
						+ "\" into Map<String,Short>");
			}
		} else {
			if (JsonValue.debug) {
				Output.logdebug(Output.padStr("JsonValueNumberLong::deserializeJsonValueToMapValue: ", 60)
						+ "Failed to deserialize \"" + name + "\" into a map with value type of "
						+ mapValueClass.getName() + " - ignoring");
			}
		}
	}

	/**
	 * Get the Java Long value of this JsonValueNumberLong object
	 *
	 * @return
	 */
	public Long getValue() {
		return this.value;
	}

	@Override
	public boolean isNull() {
		if (this.value == null) {
			return true;
		}
		return false;
	}

	public void setValue(long value) {
		this.value = new Long(value);
	}

	@Override
	public String toString() {
		if (this.value == null) {
			return "";
		}
		return this.value.toString();
	}
}
