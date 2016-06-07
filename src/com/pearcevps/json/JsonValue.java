package com.pearcevps.json;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.pearcevps.utils.Output;

abstract public class JsonValue {

	public static int				recursedDeserializedCount	= 0;
	protected static boolean	debug								= false;

	/**
	 * This function takes a JsonValue, and applies it to a bean field if it
	 * matches the correct type
	 *
	 * @param field
	 * @param fieldsBean
	 * @param valueToBePlacedToBean
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	static public void deserializeJsonValueToBean(Field field, Object fieldsBean, JsonValue valueToBePlacedToBeanField)
			throws IllegalArgumentException, IllegalAccessException {

		///////////////////////
		// Found a match of type string
		if ((field.getType() == java.lang.String.class) && (valueToBePlacedToBeanField instanceof JsonValueString)) {
			Annotation[] annotations = field.getAnnotations();
			for (int count = 0; count < annotations.length; count++) {
				if (annotations[count].toString().contains(Json_Property.class.getCanonicalName())) {
					if (!field.isAccessible()) {
						field.setAccessible(true);
					}
					String value = ((JsonValueString) valueToBePlacedToBeanField).getJavaStringValue();
					field.set(fieldsBean, value);
					if (JsonValue.debug) {
						Output.logdebug(Output.padStr("JsonValue::deserializeJsonValueToBean: ", 60)
								+ "Deserialising field \"" + field.getName() + "\" (of type String) to \"" + value + "\"");
					}
					break;
				}
			}
		}
		///////////////////////
		// Found a match of type boolean true
		else if (((field.getType() == java.lang.Boolean.class) || field.getType().equals(Boolean.TYPE))
				&& (valueToBePlacedToBeanField instanceof JsonValueTrue)) {
			Annotation[] annotations = field.getAnnotations();
			for (int count = 0; count < annotations.length; count++) {
				if (annotations[count].toString().contains(Json_Property.class.getCanonicalName())) {
					if (!field.isAccessible()) {
						field.setAccessible(true);
					}
					if (field.getType().equals(Boolean.TYPE)) {
						field.setBoolean(fieldsBean, true);
					} else {
						field.set(fieldsBean, true);
					}
					if (JsonValue.debug) {
						Output.logdebug(Output.padStr("JsonValue::deserializeJsonValueToBean: ", 60)
								+ "Deserialising field \"" + field.getName() + "\" (of type boolean) to \"true\"");
					}
					break;
				}
			}
		}
		///////////////////////
		// Found a match of type Long
		else if (((field.getType() == java.lang.Long.class) || field.getType().equals(Long.TYPE))
				&& (valueToBePlacedToBeanField instanceof JsonValueNumberLong)) {
			if (JsonValue.debug) {
				Output.logdebug(Output.padStr("JsonValue::deserializeJsonValueToBean: ", 60) + "Deserialising long - "
						+ field.getName(), ">> ");
			}

			Annotation[] annotations = field.getAnnotations();
			for (int count = 0; count < annotations.length; count++) {
				if (annotations[count].toString().contains(Json_Property.class.getCanonicalName())) {
					if (!field.isAccessible()) {
						field.setAccessible(true);
					}
					Long value = ((JsonValueNumberLong) valueToBePlacedToBeanField).getValue();
					if (field.getType().equals(Long.TYPE)) {
						field.setLong(fieldsBean, value.longValue());
					} else {
						field.set(fieldsBean, value);
					}
					if (JsonValue.debug) {
						Output.logdebug(Output.padStr("JsonValue::deserializeJsonValueToBean: ", 60)
								+ "Deserialising field \"" + field.getName() + "\" (of type long) to " + value.toString());
					}
					break;
				}
			}
		}
		///////////////////////
		// Found a match of type Integer
		else if (((field.getType() == java.lang.Integer.class) || field.getType().equals(Integer.TYPE))
				&& (valueToBePlacedToBeanField instanceof JsonValueNumberLong)) {
			if (JsonValue.debug) {
				Output.logdebug(Output.padStr("JsonValue::deserializeJsonValueToBean: ", 60) + "Deserialising integer - "
						+ field.getName(), ">> ");
			}

			Annotation[] annotations = field.getAnnotations();
			for (int count = 0; count < annotations.length; count++) {
				if (annotations[count].toString().contains(Json_Property.class.getCanonicalName())) {
					if (!field.isAccessible()) {
						field.setAccessible(true);
					}
					Integer value = ((JsonValueNumberLong) valueToBePlacedToBeanField).getValue().intValue();
					if (field.getType().equals(Integer.TYPE)) {
						field.setInt(fieldsBean, value.intValue());
					} else {
						field.set(fieldsBean, value);
					}
					if (JsonValue.debug) {
						Output.logdebug(Output.padStr("JsonValue::deserializeJsonValueToBean: ", 60)
								+ "Deserialising field \"" + field.getName() + "\" (of type int) to " + value.toString());
					}
					break;
				}
			}
		}
		///////////////////////
		// Found a match of type Short
		else if (((field.getType() == java.lang.Short.class) || field.getType().equals(Short.TYPE))
				&& (valueToBePlacedToBeanField instanceof JsonValueNumberLong)) {
			if (JsonValue.debug) {
				Output.logdebug(Output.padStr("JsonValue::deserializeJsonValueToBean: ", 60) + "Deserialising short - "
						+ field.getName(), ">> ");
			}
			Annotation[] annotations = field.getAnnotations();
			for (int count = 0; count < annotations.length; count++) {
				if (annotations[count].toString().contains(Json_Property.class.getCanonicalName())) {
					if (!field.isAccessible()) {
						field.setAccessible(true);
					}
					Short value = ((JsonValueNumberLong) valueToBePlacedToBeanField).getValue().shortValue();
					if (field.getType().equals(Short.TYPE)) {
						field.setShort(fieldsBean, value.shortValue());
					} else {
						field.set(fieldsBean, value);
					}
					if (JsonValue.debug) {
						Output.logdebug(Output.padStr("JsonValue::deserializeJsonValueToBean: ", 60)
								+ "Deserialising field \"" + field.getName() + "\" (of type short) to " + value.toString());
					}
					break;
				}
			}
		}

	}

	/**
	 * Get a JSON value from a string.
	 *
	 * @param value
	 *           The string from which the JSON value is parsed.
	 * @return
	 * @throws JsonException
	 */
	static public JsonValue getJsonValueFromString(String value) throws JsonException {
		return JsonValue.getJsonValueFromString(value, null);
	}

	/**
	 * Get a JSON value from a string.
	 *
	 * @param value
	 *           The string from which the JSON value is parsed.
	 * @param name
	 *           The name of the JSON value. Used only for debug purposes.
	 * @return
	 * @throws JsonException
	 */
	static public JsonValue getJsonValueFromString(String value, String name) throws JsonException {

		if (name == null) {
			name = "";
		}

		String pad = Output.padStr("JsonValue::getJsonValueFromString: ", 60);
		for (int i = 1; i < JsonValue.recursedDeserializedCount; i++)
			pad += "  ";

		//////////////////////////////
		// Do we have a string value?
		if ((value != null) && value.startsWith("\"") && value.endsWith("\"")) {
			value = Output.trim(value, "\"");
			JsonValue ret = new JsonValueString(JsonString.getJavaStringFromJsonString(value));
			if (JsonValue.debug) {
				Output.logdebug(pad + "Level " + JsonValue.recursedDeserializedCount + ", JsonPair found: name=\"" + name
						+ "\" " + ret.getClass().getSimpleName() + "=" + ret.toString());
			}
			return ret;
		}
		//////////////////////////////
		// Do we have a long value?
		else if (JsonValueNumberLong.isJsonLong(value)) {
			JsonValue ret = new JsonValueNumberLong(new Long(value).longValue());
			if (JsonValue.debug) {
				Output.logdebug(pad + "Level " + JsonValue.recursedDeserializedCount + ", JsonPair found: name=\"" + name
						+ "\" " + ret.getClass().getSimpleName() + "=" + ret.toString());
			}
			return ret;
		}
		//////////////////////////////
		// Do we have a double value?
		else if (JsonValueNumberDouble.isJsonDouble(value)) {
			JsonValue ret = new JsonValueNumberDouble(new Double(value).doubleValue());
			if (JsonValue.debug) {
				Output.logdebug(pad + "Level " + JsonValue.recursedDeserializedCount + ", JsonPair found: name=\"" + name
						+ "\" " + ret.getClass().getSimpleName() + "=" + ret.toString());
			}
			return ret;
		}
		//////////////////////////////
		// Do we have a boolean true value?
		else if (JsonValueTrue.isJsonBooleanTrue(value)) {
			JsonValue ret = new JsonValueTrue();
			if (JsonValue.debug) {
				Output.logdebug(pad + "Level " + JsonValue.recursedDeserializedCount + ", JsonPair found: name=\"" + name
						+ "\" " + ret.getClass().getSimpleName() + "=" + ret.toString());
			}
			return ret;
		}
		//////////////////////////////
		// Do we have a boolean false value?
		else if (JsonValueFalse.isJsonBooleanFalse(value)) {
			JsonValue ret = new JsonValueFalse();
			if (JsonValue.debug) {
				Output.logdebug(pad + "Level " + JsonValue.recursedDeserializedCount + ", JsonPair found: name=\"" + name
						+ "\" " + ret.getClass().getSimpleName() + "=" + ret.toString());
			}
			return ret;
		}
		//////////////////////////////
		// Do we have a null?
		else if (JsonValueNull.isJsonNull(value)) {
			JsonValue ret = new JsonValueNull();
			if (JsonValue.debug) {
				Output.logdebug(pad + "Level " + JsonValue.recursedDeserializedCount + ", JsonPair found: name=\"" + name
						+ "\" " + ret.getClass().getSimpleName() + "=" + ret.toString());
			}
			return ret;
		}
		//////////////////////////////
		// Do we have an object?
		else if (JsonValueObject.isJsonObject(value)) {
			JsonValueObject ret = new JsonValueObject();
			if (JsonValue.debug) {
				Output.logdebug(pad + "Level " + JsonValue.recursedDeserializedCount + ", JsonPair found: name=\"" + name
						+ "\" " + ret.getClass().getSimpleName() + "=" + ret.toString());
			}
			ret.getJsonPairstoDeserialize(value);
			return ret;
		}
		//////////////////////////////
		// Do we have an array?
		else if (JsonValueArray.isJsonArray(value)) {
			JsonValueArray ret = new JsonValueArray();
			if (JsonValue.debug) {
				Output.logdebug(pad + "Level " + JsonValue.recursedDeserializedCount + ", JsonPair found: name=\"" + name
						+ "\" " + ret.getClass().getSimpleName() + "=" + ret.toString());
			}
			JsonValue.recursedDeserializedCount++;
			List<JsonValue> values = new LinkedList<JsonValue>();
			values = JsonValueArray.getValuesFromArray(value);
			JsonValue.recursedDeserializedCount--;
			ret.setElements(values);
			return ret;
		} else {
			if (JsonValue.debug) {
				Output.logdebug(Output.padStr("JsonValue::getJsonValueFromString: ", 60) + "JSON pair \"" + name
						+ "\" has a value of '" + value + "': not have a valid JSON value. Ignoring.");
			}
		}

		return null;
	}

	public static boolean isDebug() {
		return JsonValue.debug;
	}

	/**
	 * Turns on debugging for this JsonValue. WARNING: also turns on general
	 * logging output from the {@link com.pearcevps.utils.Output} utlity.
	 * 
	 * @param i_debug
	 *           true if logging is enabled; false otherwise
	 */
	public static void setDebug(boolean i_debug) {
		JsonValue.debug = i_debug;
		Output.printdebug = i_debug;
	}

	public abstract void deserializeJsonValueToBean(Field field, Object fieldsBean)
			throws IllegalArgumentException, IllegalAccessException, JsonException, InstantiationException,
			InvocationTargetException, ClassNotFoundException;

	@SuppressWarnings("rawtypes")
	public abstract void deserializeJsonValueToMapValue(Map map, String name, Class mapValueClass)
			throws JsonException, IllegalArgumentException, IllegalAccessException, InstantiationException,
			InvocationTargetException, ClassNotFoundException;

	public abstract boolean isNull();

	@Override
	public abstract String toString();

}
