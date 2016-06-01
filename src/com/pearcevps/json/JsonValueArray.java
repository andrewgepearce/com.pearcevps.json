package com.pearcevps.json;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.pearcevps.utils.Output;

public class JsonValueArray extends JsonValue {

	List<JsonValue> elements = new LinkedList<JsonValue>();

	/**
	 * Constructor for an array with no JsonObjects
	 */
	public JsonValueArray() {
		super();
		this.elements = new LinkedList<JsonValue>();
	}

	/**
	 * Constructor for an array with some Json objects
	 *
	 * @param elements
	 */
	public JsonValueArray(List<JsonValue> elements) {
		super();
		if (elements != null) {
			this.elements = elements;
		} else {
			this.elements = new LinkedList<JsonValue>();
		}
	}

	/**
	 * Looks at a field in an object, and if the field is a List it returns the parameterized type of
	 * the list, otherwise returns null
	 *
	 * @param field
	 *           The field that could contain the list
	 * @param bean
	 *           The object in which this field is contained
	 * @return Name of parameterized List, or null if not a List
	 * @throws JsonException
	 *            Thrown if the field or bean are null
	 */
	@SuppressWarnings("rawtypes")
	static public String getListTypeName(Field field, Object bean) throws JsonException {
		if ((field == null) || (bean == null)) {
			throw new JsonException("Can't get the type of a List with a null field and bean");
		}
		if (field.getType() == java.util.List.class) {
			Class typeArgClass = null;

			if (field.getGenericType() instanceof ParameterizedType) {
				ParameterizedType ptype = (ParameterizedType) field.getGenericType();
				Type[] typeArguments = ptype.getActualTypeArguments();
				for (Type typeArgument : typeArguments) {
					typeArgClass = (Class) typeArgument;
				}
				if (typeArgClass != null) {
					return typeArgClass.getName();
				}
			}
		}
		return null;
	}

	/**
	 * From a string that represents a Json Array (i.e. regex "([)(.*)(])" get the Json Values
	 * contained
	 *
	 * @param str
	 * @return
	 * @throws JsonException
	 */
	static public List<JsonValue> getValuesFromArray(String str) throws JsonException {
		if (!JsonValueArray.isJsonArray(str)) {
			throw new JsonException("Trying to get Json array values from a non-Json Array");
		}
		List<JsonValue> values = new LinkedList<JsonValue>();
		str = Output.trim(str, "[");
		str = Output.trim(str, "]");
		char previousChar = 'a';
		String value = "";
		String objectScope = "";
		String arrayScope = "";
		String stringScope = "";
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if ((c == '[') && stringScope.isEmpty()) {
				arrayScope += '[';
			}
			if ((c == ']') && stringScope.isEmpty() && (arrayScope.length() > 0)) {
				arrayScope = arrayScope.substring(0, arrayScope.length() - 1);
			}
			if ((c == '{') && stringScope.isEmpty()) {
				objectScope += '{';
			}
			if ((c == '}') && stringScope.isEmpty() && (objectScope.length() > 0)) {
				objectScope = objectScope.substring(0, objectScope.length() - 1);
			}
			if ((c == '\"') && (previousChar != '\\') && stringScope.isEmpty()) {
				stringScope += "\"";
			}
			if ((c == '\"') && (previousChar != '\\') && !stringScope.isEmpty()) {
				stringScope = stringScope.substring(0, stringScope.length() - 1);
			}
			// We have the end of a Json Object
			if ((c == ',') && arrayScope.isEmpty() && stringScope.isEmpty() && objectScope.isEmpty()) {
				value = value.trim();
				JsonValue jsonValue = JsonValue.getJsonValueFromString(value);
				values.add(jsonValue);
				value = "";
			} else {
				value = value + c;
			}
			previousChar = str.charAt(i);
		}
		if (!value.isEmpty() && arrayScope.isEmpty() && stringScope.isEmpty() && objectScope.isEmpty()) {
			value = value.trim();
			JsonValue jsonValue = JsonValue.getJsonValueFromString(value);
			values.add(jsonValue);
		}
		return values;
	}

	/**
	 * Is this value string a JSON array, i.e. does it start with [ and end with ]
	 *
	 * @param value
	 *           The JSON String to check
	 * @return true if this looks like a JSON array, false, otherwise
	 */
	static public boolean isJsonArray(String value) {
		if (value == null) {
			return false;
		}
		if (value.isEmpty()) {
			return true;
		}
		value = value.trim();
		if (value.startsWith("[") && value.endsWith("]")) {
			return true;
		}
		return false;
	}

	public void addElement(JsonValue v) {
		this.elements.add(v);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.pearcevps.json.JsonValue#deserializeJsonValueToBean(java.lang.reflect.Field,
	 * java.lang.Object)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void deserializeJsonValueToBean(Field field, Object fieldsBean)
			throws JsonException, IllegalArgumentException, IllegalAccessException, InstantiationException,
			ClassNotFoundException, InvocationTargetException {
		if ((field == null) || (fieldsBean == null)) {
			throw new JsonException("Can't deserialize into an null field or bean");
		}
		String listType = JsonValueArray.getListTypeName(field, fieldsBean);
		if (listType == null) {
			throw new JsonException("Can't get the parametrised type of the List class, or the field "
					+ field.getName() + "is not a list");
		}

		//////////////////////
		// check annotation values
		boolean jsonProperty = false;
		boolean jsonArrayAreBeans = false;
		Annotation[] annotations = field.getAnnotations();
		for (int count = 0; count < annotations.length; count++) {
			if (annotations[count].toString().contains(Json_Property.class.getCanonicalName())) {
				jsonProperty = true;
				if (!field.isAccessible()) {
					field.setAccessible(true);
				}
			}
			if (annotations[count].toString().contains(Json_ListValuesAreBeans.class.getCanonicalName())) {
				jsonArrayAreBeans = true;
			}
		}
		if (!jsonProperty) {
			if (JsonValue.debug) {
				Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60) + "Field \""
						+ field.getName() + "\" (of type array) ignored becuase not a @JsonProperty");
			}
			return;
		}

		////////////////////////
		// We need to try and de-serialize into a POJO
		if (jsonArrayAreBeans) {
			List list = new LinkedList<>();
			field.set(fieldsBean, list);
			if (JsonValue.debug) {
				Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60) + "Field \""
						+ field.getName() + "\" being instantiated as type of LinkedList");
			}
			List<JsonValue> arrayValues = this.getElements();
			for (JsonValue value : arrayValues) {
				if (value instanceof JsonValueObject) {
					// Get the field constructors
					Constructor<?>[] constructors = Class.forName(listType).getDeclaredConstructors();
					Constructor<?> defaultConstructor = null;
					Constructor<?> innerClassDefaultConstructor = null;
					/////////////////////////////
					// Loop over each constructor
					for (int ci = 0; ci < constructors.length; ci++) {
						constructors[ci].setAccessible(true);
						Class<?>[] parameters = constructors[ci].getParameterTypes();
						///////////////////////////
						// Get the default constructor, if present
						if (parameters.length == 0) {
							defaultConstructor = constructors[ci];
							break;
						}
						// If we have 1 parameter in the constructor, and it is the type of the bean class
						// then the class is an an embedded class
						if ((parameters.length == 1) && parameters[0].equals(fieldsBean.getClass())) {
							// We have an embedded class
							innerClassDefaultConstructor = constructors[ci];
							break;
						}
					}
					if ((defaultConstructor == null) && (innerClassDefaultConstructor == null)) {
						throw new JsonException(
								"Cannot deserialize to a bean member or inner class that does not have a default constructor");
					}
					Object arrayBean = null;
					if (defaultConstructor != null) {
						arrayBean = defaultConstructor.newInstance();
					} else if (innerClassDefaultConstructor != null) {
						arrayBean = innerClassDefaultConstructor.newInstance(fieldsBean);
					}
					arrayBean = JsonValueObject.deserializeJsonStringToAnObject(((JsonValueObject) value).print(),
							arrayBean);
					list.add(arrayBean);
				}
			}

		}

		////////////////////////
		// List is of type string
		if (listType.equals(String.class.getCanonicalName())) {
			List<String> list = (List<String>) field.get(fieldsBean);
			// Always create a new list...
			if (JsonValue.debug) {
				Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60) + "Field \""
						+ field.getName() + "\" being instantiated as type of LinkedList<String>");
			}
			list = new LinkedList<String>();
			field.set(fieldsBean, list);
			List<JsonValue> arrayValues = this.getElements();
			for (JsonValue value : arrayValues) {
				if (value instanceof JsonValueString) {
					JsonValueString valstr = (JsonValueString) value;
					list.add(valstr.getJavaStringValue());
					if (JsonValue.debug) {
						Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60)
								+ "Deserialising field \"" + field.getName()
								+ "\" (of type JsonValueString) in String List to contain \""
								+ valstr.getJavaStringValue() + "\"");
					}
				} else if (value instanceof JsonValueTrue) {
					list.add("true");
					if (JsonValue.debug) {
						Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60)
								+ "Deserialising field \"" + field.getName()
								+ "\" (of type JsonValueTrue) in String List to contain \"true\"");
					}
				} else if (value instanceof JsonValueFalse) {
					list.add("true");
					if (JsonValue.debug) {
						Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60)
								+ "Deserialising field \"" + field.getName()
								+ "\" (of type JsonValueFalse) in String List to contain \"false\"");
					}
				} else if (value instanceof JsonValueNull) {
					list.add("null");
					if (JsonValue.debug) {
						Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60)
								+ "Deserialising field \"" + field.getName()
								+ "\" (of type JsonValueNull) in String List to contain \"null\"");
					}
				} else if (value instanceof JsonValueNumberLong) {
					String num = ((JsonValueNumberLong) value).getValue().toString();
					list.add(num);
					if (JsonValue.debug) {
						Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60)
								+ "Deserialising field \"" + field.getName()
								+ "\" (of type JsonValueNumberLong) in String List to contain \"" + num + "\"");
					}
				} else if (value instanceof JsonValueNumberDouble) {
					String num = ((JsonValueNumberDouble) value).getValue().toString();
					list.add(num);
					if (JsonValue.debug) {
						Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60)
								+ "Deserialising field \"" + field.getName()
								+ "\" (of type JsonValueNumberDouble) in String List to contain \"" + num + "\"");
					}
				} else {
					if (JsonValue.debug) {
						Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60)
								+ "Failed to deserialise field \"" + field.getName() + "\" value=" + value
								+ " to list of type " + listType);
					}
				}
			}
		}

		////////////////////////
		// List is of type Boolean
		else if (listType.equals(Boolean.class.getCanonicalName())) {
			List<Boolean> list = (List<Boolean>) field.get(fieldsBean);
			if (list == null) {
				if (JsonValue.debug) {
					Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60) + "Field \""
							+ field.getName() + "\" being instantiated as type of LinkedList<Boolean>");
				}
				list = new LinkedList<Boolean>();
				field.set(fieldsBean, list);
			}
			List<JsonValue> arrayValues = this.getElements();
			for (JsonValue value : arrayValues) {
				if (value instanceof JsonValueTrue) {
					list.add(new Boolean(true));
					if (JsonValue.debug) {
						Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60)
								+ "Deserialising field \"" + field.getName()
								+ "\" (of type JsonValueTrue) in String List to contain true");
					}
				} else if (value instanceof JsonValueFalse) {
					list.add(new Boolean(false));
					if (JsonValue.debug) {
						Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60)
								+ "Deserialising field \"" + field.getName()
								+ "\" (of type JsonValueTrue) in String List to contain false");
					}
				} else if (value instanceof JsonValueNumberLong) {
					long num = ((JsonValueNumberLong) value).getValue();
					if (num != 0) {
						list.add(new Boolean(true));
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName()
									+ "\" (of type JsonValueNumberLong) in String List to contain true");
						}
					} else {
						list.add(new Boolean(false));
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName()
									+ "\" (of type JsonValueNumberFalse) in String List to contain false");
						}
					}
				} else if (value instanceof JsonValueNumberDouble) {
					double num = ((JsonValueNumberDouble) value).getValue();
					if (num != 0) {
						list.add(new Boolean(true));
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName()
									+ "\" (of type JsonValueNumberDouble) in String List to contain true");
						}
					} else {
						list.add(new Boolean(false));
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName()
									+ "\" (of type JsonValueNumberDouble) in String List to contain false");
						}
					}
				} else {
					if (JsonValue.debug) {
						Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60)
								+ "Failed to deserialise field \"" + field.getName() + "\" value=" + value
								+ " to list of type " + listType);
					}
				}
			}
		}
		////////////////////////
		// List is of type Long
		else if (listType.equals(Long.class.getCanonicalName())) {
			List<Long> list = (List<Long>) field.get(fieldsBean);
			if (list == null) {
				if (JsonValue.debug) {
					Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60) + "Field \""
							+ field.getName() + "\" being instantiated as type of LinkedList<Long>");
				}
				list = new LinkedList<Long>();
				field.set(fieldsBean, list);
			}
			List<JsonValue> arrayValues = this.getElements();
			for (JsonValue value : arrayValues) {
				if (value instanceof JsonValueNumberLong) {
					long num = ((JsonValueNumberLong) value).getValue();
					list.add(new Long(num));
					if (JsonValue.debug) {
						Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60)
								+ "Deserialising field \"" + field.getName()
								+ "\" (of type JsonValueNumberLong) in Long List to contain " + num);
					}
				} else {
					if (JsonValue.debug) {
						Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60)
								+ "Failed to deserialise field \"" + field.getName() + "\" value=" + value
								+ " to list of type " + listType);
					}
				}
			}
		}
		////////////////////////
		// List is of type Integer
		else if (listType.equals(Integer.class.getCanonicalName())) {
			List<Integer> list = (List<Integer>) field.get(fieldsBean);
			if (list == null) {
				if (JsonValue.debug) {
					Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60) + "Field \""
							+ field.getName() + "\" being instantiated as type of LinkedList<Integer>");
				}
				list = new LinkedList<Integer>();
				field.set(fieldsBean, list);
			}
			List<JsonValue> arrayValues = this.getElements();
			for (JsonValue value : arrayValues) {
				if (value instanceof JsonValueNumberLong) {
					int num = ((JsonValueNumberLong) value).getValue().intValue();
					list.add(new Integer(num));
					if (JsonValue.debug) {
						Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60)
								+ "Deserialising field \"" + field.getName()
								+ "\" (of type JsonValueNumberLong) in Integer List to contain " + num);
					}
				} else {
					if (JsonValue.debug) {
						Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60)
								+ "Failed to deserialise field \"" + field.getName() + "\" value=" + value
								+ " to list of type " + listType);
					}
				}
			}
		}
		////////////////////////
		// List is of type Short
		else if (listType.equals(Short.class.getCanonicalName())) {
			List<Short> list = (List<Short>) field.get(fieldsBean);
			List<JsonValue> arrayValues = this.getElements();
			for (JsonValue value : arrayValues) {
				if (value instanceof JsonValueNumberLong) {
					short num = ((JsonValueNumberLong) value).getValue().shortValue();
					list.add(new Short(num));
					if (JsonValue.debug) {
						Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60)
								+ "Deserialising field \"" + field.getName()
								+ "\" (of type JsonValueNumberLong) in Short List to contain " + num);
					}
				} else {
					if (JsonValue.debug) {
						Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60)
								+ "Failed to deserialise field \"" + field.getName() + "\" value=" + value
								+ " to list of type " + listType);
					}
				}
			}
		}
		////////////////////////
		// List is of type Float
		else if (listType.equals(Float.class.getCanonicalName())) {
			List<Float> list = (List<Float>) field.get(fieldsBean);
			if (list == null) {
				if (JsonValue.debug) {
					Output.logdebug(
							"Field \"" + field.getName() + "\" being instantiated as type of LinkedList<Float>");
				}
				list = new LinkedList<Float>();
				field.set(fieldsBean, list);
			}
			List<JsonValue> arrayValues = this.getElements();
			for (JsonValue value : arrayValues) {
				if (value instanceof JsonValueNumberDouble) {
					float num = ((JsonValueNumberDouble) value).getValue().floatValue();
					list.add(new Float(num));
					if (JsonValue.debug) {
						Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60)
								+ "Deserialising field \"" + field.getName()
								+ "\" (of type JsonValueNumberLong) in Float List to contain " + num);
					}
				} else {
					if (JsonValue.debug) {
						Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60)
								+ "Failed to deserialise field \"" + field.getName() + "\" value=" + value
								+ " to list of type " + listType);
					}
				}
			}
		}
		////////////////////////
		// List is of type Double
		else if (listType.equals(Double.class.getCanonicalName())) {
			List<Double> list = (List<Double>) field.get(fieldsBean);
			if (list == null) {
				if (JsonValue.debug) {
					Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60) + "Field \""
							+ field.getName() + "\" being instantiated as type of LinkedList<Double>");
				}
				list = new LinkedList<Double>();
				field.set(fieldsBean, list);
			}
			List<JsonValue> arrayValues = this.getElements();
			for (JsonValue value : arrayValues) {
				if (value instanceof JsonValueNumberDouble) {
					double num = ((JsonValueNumberDouble) value).getValue().doubleValue();
					list.add(new Double(num));
					if (JsonValue.debug) {
						Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60)
								+ "Deserialising field \"" + field.getName()
								+ "\" (of type JsonValueNumberLong) in Double List to contain " + num);
					}
				} else {
					if (JsonValue.debug) {
						Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60)
								+ "Failed to deserialise field \"" + field.getName() + "\" value=" + value
								+ " to list of type " + listType);
					}
				}
			}
		}
		////////////////////////
		// List is of type URI
		else if (listType.equals(java.net.URI.class.getCanonicalName())) {
			List<URI> list = (List<URI>) field.get(fieldsBean);
			if (list == null) {
				if (JsonValue.debug) {
					Output.logdebug(
							"Field \"" + field.getName() + "\" being instantiated as type of LinkedList<URI>");
				}
				list = new LinkedList<URI>();
				field.set(fieldsBean, list);
			}
			List<JsonValue> arrayValues = this.getElements();
			for (JsonValue value : arrayValues) {
				if (value instanceof JsonValueString) {
					try {
						list.add(new URI(((JsonValueString) value).getJavaStringValue()));
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60)
									+ "Deserialising field \"" + field.getName()
									+ "\" (of type JsonValueString) in URI List to contain "
									+ new URI(((JsonValueString) value).getJavaStringValue()));
						}
					} catch (URISyntaxException e) {
						if (JsonValue.debug) {
							Output.logdebug(Output.padStr("JsonValueArray::deserializeJsonValueToBean: ", 60)
									+ "Failed to deserialise field \"" + field.getName() + "\" value=" + value
									+ " (of type JsonValueString) to URI List");
						}
					}
				}
			}
		}

	}

	@SuppressWarnings("rawtypes")
	@Override
	public void deserializeJsonValueToMapValue(Map map, String name, Class mapValueClass) {
		if (debug) {
			Output.logdebug(
					"Ignoring field \"" + name + "\" as an Json Array cannot be placed into a Map value.");
		}
	}

	public List<JsonValue> getElements() {
		return this.elements;
	}

	@Override
	public boolean isNull() {
		if ((this.elements == null) || (this.elements.size() == 0)) {
			return true;
		}
		return false;
	}

	public void setElements(List<JsonValue> elements) {
		this.elements = elements;
	}

	@Override
	public String toString() {
		if ((this.elements == null) || (this.elements.size() == 0)) {
			return "[ ]";
		}
		String ret = "[ ";
		Iterator<JsonValue> it = this.elements.iterator();
		while (it.hasNext()) {
			JsonValue value = it.next();
			ret += value.toString();
			if (it.hasNext()) {
				ret += ", ";
			}
		}
		return ret + " ]";
	}

}
