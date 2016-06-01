package com.pearcevps.json;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.pearcevps.utils.Output;

public class JsonValueNumberDouble extends JsonValue {

	Double value;

	public JsonValueNumberDouble(double value) {
		super();
		this.value = new Double(value);
	}

	static public boolean isJsonDouble(String value) {
		Pattern p = Pattern.compile("(-?)(0|[1-9]\\d*)(\\.\\d+){1}([eE]{1}[+-]?\\d+)?");
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

		if (((field.getType() == java.lang.Float.class) || field.getType().equals(Float.TYPE)
				|| (field.getType() == java.lang.Double.class) || field.getType().equals(Double.TYPE))) {
			Annotation[] annotations = field.getAnnotations();
			for (int count = 0; count < annotations.length; count++) {
				if (annotations[count].toString().contains(Json_Property.class.getCanonicalName())) {
					jsonProperty = true;

					if (!field.isAccessible()) {
						field.setAccessible(true);
					}
					// NULL values, primitives
					if ((this.value == null) && field.getType().equals(Double.TYPE)) {
						field.setDouble(fieldsBean, 0.0);
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueNumberDouble::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type double) to 0");
						}
					} else if ((this.value == null) && field.getType().equals(Float.TYPE)) {
						field.setFloat(fieldsBean, (float) 0.0);
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueNumberDouble::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type float) to 0");
						}
					}
					// NULL values, classes
					else if ((this.value == null) && (field.getType() == java.lang.Double.class)) {
						field.set(fieldsBean, null);
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueNumberDouble::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type Double) to null");
						}
					} else if ((this.value == null) && (field.getType() == java.lang.Float.class)) {
						field.set(fieldsBean, null);
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueNumberDouble::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type Float) to null");
						}
					}
					// Non NULL values, primitives
					else if ((this.value != null) && field.getType().equals(Double.TYPE)) {
						field.setDouble(fieldsBean, this.value.doubleValue());
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueNumberDouble::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type double) to "
									+ this.value.doubleValue());
						}
					} else if ((this.value != null) && field.getType().equals(Float.TYPE)) {
						field.setFloat(fieldsBean, this.value.floatValue());
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueNumberDouble::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type float) to "
									+ this.value.floatValue());
						}
					}
					// Non NULL values, classes
					else if ((this.value != null) && (field.getType() == java.lang.Double.class)) {
						field.set(fieldsBean, this.value);
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueNumberDouble::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type Double) to "
									+ this.value.doubleValue());
						}
					} else if ((this.value != null) && (field.getType() == java.lang.Float.class)) {
						field.set(fieldsBean, new Float(this.value.floatValue()));
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueNumberDouble::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName() + "\" (of type Float) to "
									+ this.value.floatValue());
						}
					}
					break;
				}
			}
		}
		if (!jsonProperty) {
			if (JsonValue.debug) {
				Output.logdebug(Output.padStr("JsonValueNumberDouble::deserializeJsonValueToBean: ", 60)
						+ "Field \"" + field.getName()
						+ "\" (of type Double or Float) ignored becuase not a @JsonProperty");
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
			Output.logdebug(Output.padStr("JsonValueNumberDouble::deserializeJsonValueToMapValue: ", 60)
					+ "Trying to deserialize field \"" + name + "\" into a map value class of type "
					+ mapValueClass.getName() + "...");
		}

		if ((mapValueClass != null) && mapValueClass.equals(java.lang.String.class)) {
			map.put(name, "" + this.getValue());
			if (JsonValue.debug) {
				Output.logdebug(Output.padStr("JsonValueNumberDouble::deserializeJsonValueToMapValue: ", 60)
						+ "Deserializing \"" + name + "\" with value " + this.getValue()
						+ " into Map<String,String>");
			}
		} else if ((mapValueClass != null) && mapValueClass.equals(java.lang.Float.class)) {
			map.put(name, new Float(this.getValue().floatValue()));
			if (JsonValue.debug) {
				Output.logdebug(Output.padStr("JsonValueNumberDouble::deserializeJsonValueToMapValue: ", 60)
						+ "Deserializing \"" + name + "\" with value " + this.getValue().floatValue()
						+ " into Map<String,Float>");
			}
		} else if ((mapValueClass != null) && mapValueClass.equals(java.lang.Double.class)) {
			map.put(name, new Double(this.getValue()));
			if (JsonValue.debug) {
				Output.logdebug(Output.padStr("JsonValueNumberDouble::deserializeJsonValueToMapValue: ", 60)
						+ "Deserializing \"" + name + "\" with value " + this.getValue().doubleValue()
						+ " into Map<String,Double>");
			}
		} else {
			if (JsonValue.debug) {
				Output.logdebug(Output.padStr("JsonValueNumberDouble::deserializeJsonValueToMapValue: ", 60)
						+ "Failed to deserialize \"" + name + "\" into a map with value type of "
						+ mapValueClass.getName() + " - ignoring");
			}
		}
	}

	public Double getValue() {
		return this.value;
	}

	@Override
	public boolean isNull() {
		if (this.value == null) {
			return true;
		}
		return false;
	}

	public void setValue(double value) {
		this.value = new Double(value);
	}

	@Override
	public String toString() {
		if (this.value == null) {
			return "";
		}
		return Double.toString(this.value);
	}

}
