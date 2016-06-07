package com.pearcevps.json;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pearcevps.utils.Output;

/**
 * @author andrewpearce
 *
 */
public class JsonValueObject extends JsonValue {

	static Map<String, Integer> beanFieldRecursionValues = new HashMap<String, Integer>();

	/**
	 * Create a JsonObject from a bean
	 *
	 * @param beanObj
	 * @return
	 * @throws JsonException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	static public JsonValueObject createObjectFromBean(Object beanObj)
			throws JsonException, IllegalArgumentException, IllegalAccessException {
		beanFieldRecursionValues.clear();
		return JsonValueObject.createObjectFromBeanInternal(beanObj);
	}

	/**
	 * Create a JsonObject from a bean
	 *
	 * @param beanObj
	 * @return
	 * @throws JsonException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	static public JsonValueObject createObjectFromBean(Object beanObj, Map<String, Integer> beanFieldRecursionLimits)
			throws JsonException, IllegalArgumentException, IllegalAccessException {
		beanFieldRecursionValues.clear();
		return JsonValueObject.createObjectFromBeanInternal(beanObj);
	}

	/**
	 * This function takes a string that represents a a JSON object, i.e. starts
	 * with and { and ends with }, and then:
	 * <li>Gets the JSON pairs for this JsonValueObject - @see
	 * {@link com.pearcevps.json.JsonValueObject#getJsonPairstoDeserialize(String)}
	 * </li>
	 * <li>Gets the fields into which to deserialise the JSON pairs. This
	 * includes the fields in the beans parent classes.</li>
	 * <li>Loop over each JSON pair, and (if there is a name match to a field)
	 * assign the JSON value to the bean field.</li>
	 * <p>
	 *
	 * @param jsonObjectString
	 * @param bean
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws JsonException
	 * @throws InstantiationException
	 * @throws InvocationTargetException
	 * @throws ClassNotFoundException
	 */
	static public Object deserializeJsonStringToAnObject(String jsonObjectString, Object bean)
			throws IllegalArgumentException, IllegalAccessException, JsonException, InstantiationException,
			InvocationTargetException, ClassNotFoundException {
		if (bean == null) {
			return null;
		}

		////////////////////
		// Get a JsonValueObject that represents the string value being passed in
		if (JsonValue.debug) {
			Output.logdebug(Output.padStr("JsonValueObject::deserializeJsonStringToAnObject: ", 60)
					+ "Getting JSON pairs to deserialize from the JSON object string..."
					+ jsonObjectString.replaceAll("\n", "").replaceAll("    ", " "));
		}
		JsonValueObject targetObject = new JsonValueObject();
		targetObject.getJsonPairstoDeserialize(jsonObjectString);

		////////////////////
		// Initialize bean object fields to de-serialize values into
		if (JsonValue.debug) {
			Output.logdebug(Output.padStr("JsonValueObject::deserializeJsonStringToAnObject: ", 60)
					+ "Getting bean fields to deserialize values into...");
		}

		List<Field> allfields = new LinkedList<Field>();
		List<Field> usedFields = new LinkedList<Field>();
		List<Field> unusedFields = new LinkedList<Field>();
		Class<? extends Object> thisclass = bean.getClass();

		allfields.addAll(Arrays.asList(thisclass.getDeclaredFields()));
		while (thisclass.getSuperclass() != null) {
			thisclass = thisclass.getSuperclass();
			allfields.addAll(0, Arrays.asList(thisclass.getDeclaredFields()));
		}
		if (JsonValue.debug) {
			String fields = "";
			for (Field f : allfields) {
				fields += ("[" + f.getName() + "] ");
			}
			Output.logdebug(Output.padStr("JsonValueObject::deserializeJsonStringToAnObject: ", 60) + " ... found "
					+ allfields.size() + " fields in bean \"" + bean.getClass().getName() + "\": " + fields);
		}

		///////////////////////
		// Loop over the pairs in this JsonValue object, assign them to fields if
		/////////////////////// they are equal
		Set<String> pairNamesMatched = new HashSet<>();
		for (JsonPair pair : targetObject.getMembers()) {
			String name = JsonString.getJavaStringFromJsonString(pair.getName().getJsonStr());
			for (Field field : allfields) {
				if (field.getName().equalsIgnoreCase(name)) {
					if (debug) {
						Output.logdebug("=======================");
						Output.logdebug(Output.padStr("JsonValueObject::deserializeJsonStringToAnObject: ", 60)
								+ "Trying to deserialize \"" + field.getName() + "\"");
					}
					pair.getValue().deserializeJsonValueToBean(field, bean);
					pairNamesMatched.add(pair.getName().getJavaString());
					usedFields.add(field);
				}
			}
		}

		//////////////////////////////
		// Get a list of pairs haven't we matched
		List<JsonPair> unmatched = new LinkedList<>();
		for (JsonPair pair : targetObject.getMembers()) {
			if (!pairNamesMatched.contains(pair.getName().getJavaString())) {
				unmatched.add(pair);
			}
		}
		for (JsonPair pair : unmatched) {
			if (JsonValue.debug) {
				Output.logdebug(Output.padStr("JsonValueObject::deserializeJsonStringToAnObject: ", 60)
						+ "Found unmatched Pair \"" + pair.getName().getJavaString()
						+ "\". Adding it to a list of pairs to add try to add to a Map in the bean.");
			}
		}

		///////////////////////////////
		// Get a list of fields that we haven't used
		for (Field f : allfields) {
			if (!usedFields.contains(f)) {
				unusedFields.add(f);
			}
		}

		////////////////////////////////
		// Try to place all unused pairs into the bean, scanning the unused fields
		//////////////////////////////// for a matching map
		JsonValueObject.placeRemainingPairsIntoMaps(bean, unusedFields, unmatched);

		return bean;
	}

	/**
	 * Is this a JSON object, i.e. does the string start with { and end with }.
	 * This function does not check anything is valid withon the scope of the
	 * object.
	 *
	 * @param value
	 * @return
	 */
	static public boolean isJsonObject(String value) {
		if (value == null) {
			return false;
		}
		if (value.isEmpty()) {
			return true;
		}
		value = value.trim();
		if (value.startsWith("{") && value.endsWith("}")) {
			return true;
		}
		return false;
	}

	/**
	 * Do the pairs within this JSON object print null values when print is
	 * called?
	 * 
	 * @return boolean true if null values will be printed
	 */
	static public boolean isPrintNull() {
		return JsonPair.isPrintNull();
	}

	/**
	 * Pretty print this JSON object
	 *
	 * @param input
	 *           The JSON object to print
	 * @param level
	 *           What level to indent the JSON object
	 * @return
	 */
	static public String jsonPrettyPrint(String input, int level) {
		String indent = "   ";
		Map<Integer, Integer> insertionPoints = new HashMap<Integer, Integer>();
		boolean inQuote = false;
		// iterate through the input
		for (int i = 0; i < input.length(); i++) {
			if ((input.charAt(i) == '{') && !inQuote) {
				int insertionPoint = i + 1;
				insertionPoints.put(insertionPoint, ++level);
			}
			if (input.charAt(i) == ',') {
				int insertionPoint = i + 1;
				insertionPoints.put(insertionPoint, level);
			}
			if ((input.charAt(i) == '}') && !inQuote) {
				// int insertionPoint = i - 1;
				int insertionPoint = i;
				insertionPoints.put(insertionPoint, --level);
			}
			if (input.charAt(i) == '[') {
				int insertionPoint = i + 1;
				insertionPoints.put(insertionPoint, ++level);
			}
			if (input.charAt(i) == ']') {
				// int insertionPoint = i - 1;
				int insertionPoint = i;
				insertionPoints.put(insertionPoint, --level);
			}
			if (input.charAt(i) == '"') {
				inQuote = !inQuote;
			}
		}

		String ret = "";
		for (int i = 0; i < input.length(); i++) {
			if (!insertionPoints.containsKey(i)) {
				ret += input.charAt(i);
			} else {
				ret += JsonValueObject.getNewIndent(insertionPoints.get(i), indent);
				ret += input.charAt(i); // Was before the new indent line

			}
		}

		return ret;
	}

	/**
	 * Should the JSON pairs within this object print if their value is null?
	 * 
	 * @param printNull
	 *           True, if null values should be printed. False otherwise.
	 */
	static public void setPrintNull(boolean printNull) {
		JsonPair.setPrintNull(printNull);
	}

	/**
	 * Create a JSON object from a bean (a class with simple public types)
	 *
	 * @param bean
	 * @return
	 * @throws JsonException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	static protected JsonValueObject createObjectFromBeanInternal(Object bean)
			throws IllegalArgumentException, IllegalAccessException, JsonException {
		JsonValueObject ret = new JsonValueObject();
		List<JsonPair> fields = JsonPair.createStringPairsFromBean(bean);
		ret.addPairs(fields);
		return ret;
	}

	/**
	 * Get a string from the input that represents the object
	 *
	 * @param str
	 *           The input string to parse
	 * @return
	 */
	static private String getJsonStringFromObjectToDeserialize(String str) {
		String ret = "";
		boolean inString = false;
		boolean inArray = false;
		char previousChar = 'a';
		String braceScope = "";
		String arrayScope = "";
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);

			if ((c == ']') && !inString && (arrayScope.length() > 0)) {
				arrayScope = arrayScope.substring(0, arrayScope.length() - 1);
				if (arrayScope.isEmpty()) {
					inArray = false;
				}
			}
			if ((c == '}') && !inString && (braceScope.length() > 0)) {
				braceScope = braceScope.substring(0, braceScope.length() - 1);
			}
			if (braceScope.length() > 0) {
				if (inString || !Character.isWhitespace(c)) {
					ret += str.charAt(i);
				}
			}
			if ((c == '{') && !inString) {
				braceScope += "{";
			}
			if ((c == '[') && !inString) {
				arrayScope += "[";
				inArray = true;
			}
			if ((c == ',') && !inString && !inArray && (braceScope.length() == 1)) {
				ret += "\n";
			}
			if ((str.charAt(i) == '"') && !inString) {
				inString = true;
			} else if ((str.charAt(i) == '"') && inString && (previousChar != '\\')) {
				inString = false;
			}

			if ((ret.length() > 0) && (braceScope.length() == 0)) {
				return ret.trim();
			}
			previousChar = str.charAt(i);
		}
		return ret.trim();
	}

	static private String getNewIndent(int level, String indentStr) {
		String ret = "";
		// if (level > 0)
		ret += "\n";
		for (int i = 0; i < level; i++) {
			ret += indentStr;
		}
		return ret;
	}

	/**
	 * This function takes a list of unmatched JSON Pairs, and fields within a
	 * bean, and tries to place these unmatched pairs into a map values that can
	 * take it.<br>
	 * <b>WARNING:</b> it is required that an object into which the objects are
	 * placed only contains one Map. An exception will be thrown if there is more
	 * than one Map in the object.
	 *
	 * @param bean
	 *           The bean which can contain the Map fields
	 * @param fields
	 *           The fields of the bean, which shall be scanned to get the Map
	 *           values into which to place the JSON pairs.
	 * @param unmatchedPairs
	 *           A list of JSON pairs that need to be placed into Maps.
	 * @throws JsonException
	 *            Thrown if the bean contains more than on Map into which to
	 *            place the unmatched JSON pairs.
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws ClassNotFoundException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 */
	@SuppressWarnings({ "unused", "rawtypes" })
	static private void placeRemainingPairsIntoMaps(Object bean, List<Field> fields, List<JsonPair> unmatchedPairs)
			throws JsonException, IllegalArgumentException, IllegalAccessException, InstantiationException,
			InvocationTargetException, ClassNotFoundException {

		///////////////////////
		// Throw an exception if we a NULL bean or set of bean fields.
		if ((fields == null) || (bean == null)) {
			throw new JsonException("Can't deserialize into an null field or bean");
		}

		///////////////////////
		// Don't do anything if we do not have unmatched pairs
		if (unmatchedPairs.isEmpty()) {
			return;
		}

		///////////////////////
		// Boolean flags to show we have found a JsonProperty map field to assign
		/////////////////////// pairs to. This may
		// either be Maps we treat as JSON objects, or as Java class
		/////////////////////// representations of primitive
		// types
		boolean foundJavaTypeMap = false;
		boolean foundPojoMap = false;

		//////////////////////
		// The class for the value of the found map
		String mapValueClassName = null;
		Class mapValueClass = null;

		///////////////////////
		// The field to apply the map to field
		Field mapfield = null;
		////////////////////////
		// Find the map field, assign our new map to it.
		for (Field field : fields) {
			boolean jsonProperty = false;
			boolean jsonMapAsBeans = false;
			if (field.getType() == java.util.Map.class) {
				/////////////////////////////
				// Get the annotations for the Map field
				Annotation[] annotations = field.getAnnotations();
				for (int count = 0; count < annotations.length; count++) {
					if (annotations[count].toString().contains(Json_Property.class.getCanonicalName())) {
						jsonProperty = true;
					}
					if (annotations[count].toString().contains(Json_MapValuesAreBeans.class.getCanonicalName())) {
						jsonMapAsBeans = true;
					}
				}

				///////////////////////////
				// Only progress if this is is a Map field that is required to be
				/////////////////////////// de-serialized
				if (jsonProperty) {

					///////////////////////
					// We have already assigned a Map, give a warning that we can
					/////////////////////// only support one.
					if (foundPojoMap || foundJavaTypeMap) {
						throw new JsonException("Can only deserialize to a class with one @Json_Parameter Map. Class "
								+ bean.getClass().getName() + " has at least two.");
					}

					/////////////////////
					// Get the Map parameterized types, and check that that this one
					///////////////////// that can really be
					// assigned a JSON pair.
					if (field.getGenericType() instanceof ParameterizedType) {
						String key = null;
						ParameterizedType ptype = (ParameterizedType) field.getGenericType();
						Type[] typeArguments = ptype.getActualTypeArguments();
						if (typeArguments.length == 2) {
							mapValueClass = (Class) typeArguments[1];
							mapValueClassName = typeArguments[1].getTypeName();

							if (typeArguments[0].getTypeName().equalsIgnoreCase(String.class.getName())) {
								if (!field.isAccessible()) {
									field.setAccessible(true);
								}
								mapfield = field;
								if (jsonMapAsBeans) {
									foundPojoMap = true;
								} else {
									foundJavaTypeMap = true;
								}
							}
						}
					}
				}
			}
		}

		/////////////////////
		// Now give our unassigned pairs to it
		if ((mapValueClass != null)) {
			if (mapValueClass.equals(java.lang.String.class)) {
				Map<String, String> map = new HashMap<>();
				for (JsonPair pair : unmatchedPairs) {
					pair.getValue().deserializeJsonValueToMapValue(map, pair.getName().getJavaString(), mapValueClass);
				}
				mapfield.set(bean, map);
			} else if (mapValueClass.equals(java.lang.Integer.class)) {
				Map<String, Integer> map = new HashMap<>();
				for (JsonPair pair : unmatchedPairs) {
					pair.getValue().deserializeJsonValueToMapValue(map, pair.getName().getJavaString(), mapValueClass);
				}
				mapfield.set(bean, map);
			} else if (mapValueClass.equals(java.lang.Long.class)) {
				Map<String, Long> map = new HashMap<>();
				for (JsonPair pair : unmatchedPairs) {
					pair.getValue().deserializeJsonValueToMapValue(map, pair.getName().getJavaString(), mapValueClass);
				}
				mapfield.set(bean, map);
			} else if (mapValueClass.equals(java.lang.Short.class)) {
				Map<String, Short> map = new HashMap<>();
				for (JsonPair pair : unmatchedPairs) {
					pair.getValue().deserializeJsonValueToMapValue(map, pair.getName().getJavaString(), mapValueClass);
				}
				mapfield.set(bean, map);
			} else if (mapValueClass.equals(java.lang.Double.class)) {
				Map<String, Double> map = new HashMap<>();
				for (JsonPair pair : unmatchedPairs) {
					pair.getValue().deserializeJsonValueToMapValue(map, pair.getName().getJavaString(), mapValueClass);
				}
				mapfield.set(bean, map);
			} else if (mapValueClass.equals(java.lang.Float.class)) {
				Map<String, Float> map = new HashMap<>();
				for (JsonPair pair : unmatchedPairs) {
					pair.getValue().deserializeJsonValueToMapValue(map, pair.getName().getJavaString(), mapValueClass);
				}
				mapfield.set(bean, map);
			} else if (mapValueClass.equals(java.lang.Character.class)) {
				Map<String, Character> map = new HashMap<>();
				for (JsonPair pair : unmatchedPairs) {
					pair.getValue().deserializeJsonValueToMapValue(map, pair.getName().getJavaString(), mapValueClass);
				}
				mapfield.set(bean, map);
			} else if (mapValueClass.equals(java.lang.Boolean.class)) {
				Map<String, Boolean> map = new HashMap<>();
				for (JsonPair pair : unmatchedPairs) {
					pair.getValue().deserializeJsonValueToMapValue(map, pair.getName().getJavaString(), mapValueClass);
				}
				mapfield.set(bean, map);
			} else if (foundPojoMap) {
				Map<String, Object> map = new HashMap<>();
				for (JsonPair pair : unmatchedPairs) {
					pair.getValue().deserializeJsonValueToMapValue(map, pair.getName().getJavaString(), mapValueClass);
				}
				mapfield.set(bean, map);
			} else {
				if (JsonValue.isDebug()) {
					for (JsonPair pair : unmatchedPairs) {
						Output.logdebug(Output.padStr("JsonValueObject::placeRemainingPairsIntoMaps: ", 60)
								+ "Don't recognise map value class " + mapValueClass.getName()
								+ " to desrialize unmatched Json Pairs to. Ignoring pair " + pair.getName().getJavaString()
								+ ".");
					}
				}
			}
		}

	}

	/**
	 * The JSON pairs that form this JSON object
	 */
	private List<JsonPair> members = new LinkedList<JsonPair>();

	/**
	 * Add a JSON Pair (i.e. name and value) to this JSON object.
	 *
	 * @param pair
	 */
	public void addPair(JsonPair pair) {
		this.members.add(pair);
	}

	/**
	 * Add a list of JSON pairs to this JSON object
	 *
	 * @param pairs
	 */
	public void addPairs(List<JsonPair> pairs) {
		for (JsonPair pair : pairs) {
			this.members.add(pair);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.pearcevps.json.JsonValue#deserializeJsonValueToBean(java.lang.reflect.
	 * Field, java.lang.Object)
	 */
	@Override
	public void deserializeJsonValueToBean(Field field, Object fieldsBean) throws JsonException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException {
		if ((field == null) || (fieldsBean == null)) {
			throw new JsonException("Can't deserialize into a null field or bean");
		}
		boolean jsonProperty = false;

		Annotation[] annotations = field.getAnnotations();
		for (int count = 0; count < annotations.length; count++) {
			if (annotations[count].toString().contains(Json_Property.class.getCanonicalName())) {
				jsonProperty = true;

				if (!field.isAccessible()) {
					field.setAccessible(true);
				}
				if (field.getType().isPrimitive()) {
					throw new JsonException("Tried to de-serializes a JSON object into a primtive field type");
				}
				// Get the field constructors
				Constructor<?>[] constructors = field.getType().getDeclaredConstructors();
				Constructor<?> defaultConstructor = null;
				Constructor<?> innerClassDefaultConstructor = null;
				// Loop over each constructor
				for (int ci = 0; ci < constructors.length; ci++) {
					// System.err.println(constructors[ci].getName());
					constructors[ci].setAccessible(true);
					Class<?>[] parameters = constructors[ci].getParameterTypes();
					// Get the default constructor, if present
					if (parameters.length == 0) {
						defaultConstructor = constructors[ci];
						break;
					}
					// // If we have 1 parameter in the constructor, and it is the
					// type of the bean class
					// // then the class is an an embedded class
					// System.err.println("FB: " + fieldsBean.getClass().getName());
					// System.err.println("PA: " + parameters[0].getName());
					// System.err.println(parameters[0].isAssignableFrom(fieldsBean.getClass()));

					if ((parameters.length == 1) && parameters[0].isAssignableFrom(fieldsBean.getClass())) {

						// if ((parameters.length == 1) &&
						// parameters[0].equals(fieldsBean.getClass())) {
						// We have an embedded class
						innerClassDefaultConstructor = constructors[ci];
						break;
					}
				}
				if ((defaultConstructor == null) && (innerClassDefaultConstructor == null)) {
					throw new JsonException("Cannot deserialize to a bean member or inner class of type \""
							+ fieldsBean.getClass().getCanonicalName() + "\" that does not have a default constructor");
				}
				Object containedBean = null;
				if (defaultConstructor != null) {
					containedBean = defaultConstructor.newInstance();
				} else if (innerClassDefaultConstructor != null) {
					containedBean = innerClassDefaultConstructor.newInstance(fieldsBean);
				}
				if (JsonValue.debug) {
					Output.logdebug(Output.padStr("JsonValueObject::deserializeJsonValueToBean: ", 60)
							+ "Deserialising field \"" + field.getName() + "\" (of type Object) ...");
				}
				containedBean = JsonValueObject.deserializeJsonStringToAnObject(this.print(), containedBean);
				field.set(fieldsBean, containedBean);
			}
		}
		if (!jsonProperty) {
			if (JsonValue.debug) {
				Output.logdebug(Output.padStr("JsonValueObject::deserializeJsonValueToBean: ", 60) + "Field \""
						+ field.getName() + "\" (of type POJO) ignored becuase not a @JsonProperty");
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void deserializeJsonValueToMapValue(Map map, String name, Class mapValueClass)
			throws JsonException, IllegalArgumentException, IllegalAccessException, InstantiationException,
			InvocationTargetException, ClassNotFoundException {
		if ((map == null) || (name == null) || name.isEmpty() || (mapValueClass == null)) {
			throw new JsonException(
					"Cannot desrialize Json Value to a null map, or map without an indicated dvalue class, or to an existing map without a value name");
		}

		if (debug) {
			Output.logdebug(
					Output.padStr("JsonValueObject::deserializeJsonValueToMapValue: ", 60) + "Trying to deserialize field \""
							+ name + "\" into a map value class of type " + mapValueClass.getName() + "...");
		}

		Object o = deserializeJsonStringToAnObject(this.toString(), mapValueClass.newInstance());
		map.put(name, o);
		if (JsonValue.debug) {
			Output.logdebug(Output.padStr("JsonValueObject::deserializeJsonValueToMapValue: ", 60) + "Deserializing \""
					+ name + "\" with value \"" + this.toString() + "\" into Map<String, Object>");
		}
	}

	/**
	 * This class reads a JSON string, and populates this classes member
	 * JsonPairs from the string
	 *
	 * @param str
	 *           The string containing the JSON to parse
	 * @throws JsonException
	 */
	public void getJsonPairstoDeserialize(String str) throws JsonException {
		JsonValue.recursedDeserializedCount++;

		String result = JsonValueObject.getJsonStringFromObjectToDeserialize(str);
		Output.logdebug(Output.padStr("JsonValueObject::getJsonPairstoDeserialize: ", 60) + result.replaceAll("\n", " "));
		String[] pairs = result.split(",\n");
		// Loop over the pairs
		for (int i = 0; i < pairs.length; i++) {
			String pair = pairs[i];
			String name = "";
			String value = "";
			boolean inNameString = true;
			boolean inValueString = false;
			char previousChar = 'a';
			// Break the pair into a name and value
			for (int pairIdx = 0; pairIdx < pair.length(); pairIdx++) {
				char c = pair.charAt(pairIdx);
				if ((pairIdx == 0) && (c != '\"')) {
					throw new JsonException("JsonValueObject - object name doesn't start with \"");
				}
				if ((c == '"') && inNameString && (previousChar != '\\') && (pairIdx > 0)) {
					inNameString = false;
				}
				if (inNameString && (pairIdx > 0)) {
					name += c;
				}
				if (inValueString) {
					value += c;
				}
				if ((c == ':') && !inNameString && !inValueString) {
					inValueString = true;
				}
				previousChar = str.charAt(pairIdx);
			}
			name = JsonString.getJavaStringFromJsonString(name);
			value = value.trim();
			JsonValue jsonValue = JsonValue.getJsonValueFromString(value, name);
			if ((name != null) && (jsonValue != null)) {
				JsonPair jsonPair = new JsonPair(new JsonString(name), jsonValue);
				this.members.add(jsonPair);
			}
		}
		JsonValue.recursedDeserializedCount--;

	}

	public List<JsonPair> getMembers() {
		return this.members;
	}

	@Override
	public boolean isNull() {
		if (this.members == null) {
			return true;
		}
		if (this.members.size() == 0) {
			return true;
		}
		return false;
	}

	public String prettyPrint() {
		return JsonValueObject.jsonPrettyPrint(this.toString(), 0);
	}

	public String print() {
		return this.toString();
	}

	public void setMembers(List<JsonPair> members) {
		this.members = members;
	}

	@Override
	public String toString() {
		if ((this.members == null) || (this.members.size() == 0)) {
			return "{ }";
		}
		String ret = "{ ";
		Iterator<JsonPair> it = this.members.iterator();
		while (it.hasNext()) {
			JsonPair pair = it.next();
			String pairVal = pair.toString();
			ret += pair.toString();
			if (it.hasNext() && (pairVal != null) && !pairVal.isEmpty()) {
				ret += ", ";
			}
		}
		if (ret.endsWith(", ")) {
			ret = ret.substring(0, ret.length() - 2);
		}
		ret += " }";
		return ret;
	}

	/**
	 * Default constructor
	 */
	public JsonValueObject() {
		super();
		this.members = new LinkedList<JsonPair>();
	}

	/**
	 * Constructor for the JSON object
	 *
	 * @param members
	 *           JSON pairs to insantiate the JSON object with
	 */
	public JsonValueObject(List<JsonPair> members) {
		super();
		if (members != null) {
			this.members = members;
		} else {
			this.members = new LinkedList<JsonPair>();
		}
	}
}
