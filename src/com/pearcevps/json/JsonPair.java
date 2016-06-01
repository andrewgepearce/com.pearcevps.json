package com.pearcevps.json;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JsonPair {

	private JsonString		name;
	private boolean			printNullLocal	= JsonPair.printNull;
	private JsonValue			value;
	static public boolean	printNull		= true;

	public JsonPair(JsonString name, JsonValue value) {
		super();
		this.name = name;
		this.value = value;
	}

	public static boolean isPrintNull() {
		return JsonPair.printNull;
	}

	public static void setPrintNull(boolean printNull) {
		JsonPair.printNull = printNull;
	}

	public JsonString getName() {
		return this.name;
	}

	public JsonValue getValue() {
		return this.value;
	}

	public boolean isPrintNullLocal() {
		return this.printNullLocal;
	}

	public void setName(JsonString name) {
		this.name = name;
	}

	public void setPrintNullLocal(boolean printNullLocal) {
		this.printNullLocal = printNullLocal;
	}

	public void setValue(JsonValue value) {
		this.value = value;
	}

	@Override
	public String toString() {
		if ((this.name == null) && !this.printNullLocal) {
			return "";
		}
		if ((this.name == null) && this.printNullLocal) {
			return "\"\": \"\"";
		}

		if (!this.printNullLocal) {
			if (this.value == null) {
				return "";
			}
			if (this.value.isNull()) {
				return "";
			}
		}
		if (this.printNullLocal) {
			if (this.value == null) {
				return this.name.toString() + ": " + new JsonValueNull().toString();
			}
			if (this.value.isNull()) {
				return this.name.toString() + ": " + new JsonValueNull().toString();
			}
		}
		return this.name.toString() + ": " + this.value.toString();
	}

	/**
	 * The class reads a Java POJO's fields, and returns a list of Json Pairs that describe these
	 * fields and their values. The current supported field types, parsed in this order, are:
	 * <ul>
	 * <li>String</li>
	 * <li>Long class or long primitive</li>
	 * <li>Integer class or integer primitive</li>
	 * <li>Short class or short primitive</li>
	 * <li>Float class or float primitive</li>
	 * <li>Double class or double primitive</li>
	 * <li>Character class or character primitive</li>
	 * <li>Boolean class or boolean primitive</li>
	 * <li>java.net.URI class</li>
	 * <li>List&lt;String&gt; class</li>
	 * <li>Map&lt;String,Object&gt;, where the key Strings are used to get the JsonPair names, and
	 * the value Objects are extracted using toString(). The name of the map is not used when
	 * unwrapped.</li>
	 * <li>Any class that can be cast to an Object</li>
	 * </ul>
	 *
	 * All fields have to be annotated using {@link Json_Property} in order to be processed. NOTE: At
	 * the moment, the fields have to be public to be read.
	 *
	 * @param beanObj
	 *           The POJO that is to be read to create the list of JsonPair objects
	 * @return The list of JsonPair objects parsed from the POJO
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	static public List<JsonPair> createStringPairsFromBean(Object beanObj)
			throws JsonException, IllegalArgumentException, IllegalAccessException {
		List<JsonPair> ret = new LinkedList<JsonPair>();
		List<Field> fields = new LinkedList<Field>();
		List<String> ignoredFields = new LinkedList<String>();
		Class<? extends Object> thisclass = beanObj.getClass();
		fields.addAll(Arrays.asList(thisclass.getDeclaredFields()));
		while (thisclass.getSuperclass() != null) {
			thisclass = thisclass.getSuperclass();
			fields.addAll(0, Arrays.asList(thisclass.getDeclaredFields()));
		}
	
		for (Member mbr : fields.toArray(new Field[0])) {
			if (mbr instanceof Field) {
				Field f = (Field) mbr;
				Annotation[] annotations = f.getAnnotations();
				for (int count = 0; count < annotations.length; count++) {
					if (annotations[count].toString()
							.contains(Json_IgnoreFieldsInThisList.class.getCanonicalName())) {
						if (!Modifier.isPublic(f.getModifiers())) {
							f.setAccessible(true);
						}
						if (f.getType() == java.util.List.class) {
							List<?> val;
							val = (List<?>) ((Field) mbr).get(beanObj);
							if (val != null) {
								// Add all the String objects to the array
								for (Object arrayObj : val.toArray()) {
									if (arrayObj != null) {
										ignoredFields.add(arrayObj.toString());
									}
								}
							}
						}
					}
				}
			}
		}
	
		for (Member mbr : fields.toArray(new Field[0])) {
			// For the all bean fields
			if (mbr instanceof Field) {
				Field f = (Field) mbr;
				boolean processField = false;
				boolean mapValuesAreBeans = false;
				boolean listValuesAreBeans = false;
				boolean required = false;
				Annotation[] annotations = f.getAnnotations();
				for (int count = 0; count < annotations.length; count++) {
					if (annotations[count].toString().contains(Json_MapValuesAreBeans.class.getCanonicalName())) {
						mapValuesAreBeans = true;
					}
					if (annotations[count].toString().contains(Json_ListValuesAreBeans.class.getCanonicalName())) {
						listValuesAreBeans = true;
					}
					if (annotations[count].toString().contains(Json_Property.class.getCanonicalName())) {
						processField = true;
						if (!Modifier.isPublic(f.getModifiers())) {
							f.setAccessible(true);
						}
					}
					if (annotations[count].toString().contains(Json_PropertyRequired.class.getCanonicalName())) {
						processField = true;
						required = true;
						if (!Modifier.isPublic(f.getModifiers())) {
							f.setAccessible(true);
						}
					}
					if (annotations[count].toString()
							.contains(Json_IgnoreFieldsInThisList.class.getCanonicalName())) {
						processField = false;
						if (!Modifier.isPublic(f.getModifiers())) {
							f.setAccessible(true);
						}
					}
				}
	
				if (ignoredFields.contains(f.getName())) {
					processField = false;
				}
	
				if (processField) {
					JsonPair pair = null;
					// String
					if (f.getType() == java.lang.String.class) {
						String val = (String) ((Field) mbr).get(beanObj);
						pair = createPairString(mbr.getName(), val);
					}
					// LONG primitive
					else if (f.getType().equals(Long.TYPE)) {
						Long val = (Long) ((Field) mbr).get(beanObj);
						pair = createPairLong(mbr.getName(), val);
					}
					// LONG class
					else if (f.getType() == java.lang.Long.class) {
						Long val = (Long) ((Field) mbr).get(beanObj);
						if (val != null) {
							pair = createPairLong(mbr.getName(), val.longValue());
						} else {
							pair = createPairString(mbr.getName(), null);
						}
					}
					// Integer class
					else if (f.getType() == java.lang.Integer.class) {
						Integer val = (Integer) ((Field) mbr).get(beanObj);
						if (val != null) {
							pair = createPairLong(mbr.getName(), val.longValue());
						} else {
							pair = createPairString(mbr.getName(), null);
						}
					}
					// Integer primitive
					else if (f.getType().equals(Integer.TYPE)) {
						Integer val = (Integer) f.get(beanObj);
						if (val != null) {
							pair = createPairLong(mbr.getName(), val.longValue());
						} else {
							pair = createPairString(mbr.getName(), null);
						}
					}
					// Short class
					else if (f.getType() == java.lang.Short.class) {
						Short val = (Short) ((Field) mbr).get(beanObj);
						if (val != null) {
							pair = createPairLong(mbr.getName(), val.longValue());
						} else {
							pair = createPairString(mbr.getName(), null);
						}
					}
					// Short primitive
					else if (f.getType().equals(Short.TYPE)) {
						Short val = (Short) ((Field) mbr).get(beanObj);
						if (val != null) {
							pair = createPairLong(mbr.getName(), val.longValue());
						} else {
							pair = createPairString(mbr.getName(), null);
						}
					}
					// Float class
					else if (f.getType() == java.lang.Float.class) {
						Float val = (Float) ((Field) mbr).get(beanObj);
						if (val != null) {
							pair = createPairFloat(mbr.getName(), val.doubleValue());
						} else {
							pair = createPairString(mbr.getName(), null);
						}
					}
					// Float primitive
					else if (f.getType().equals(Float.TYPE)) {
						Float val = (Float) ((Field) mbr).get(beanObj);
						if (val != null) {
							pair = createPairFloat(mbr.getName(), val.doubleValue());
						} else {
							pair = createPairString(mbr.getName(), null);
						}
					}
					// Double class
					else if (f.getType() == java.lang.Double.class) {
						Double val = (Double) ((Field) mbr).get(beanObj);
						if (val != null) {
							pair = createPairFloat(mbr.getName(), val.doubleValue());
						} else {
							pair = createPairString(mbr.getName(), null);
						}
					}
					// Double primitive
					else if (f.getType().equals(Double.TYPE)) {
						Double val = (Double) ((Field) mbr).get(beanObj);
						if (val != null) {
							pair = createPairFloat(mbr.getName(), val.doubleValue());
						} else {
							pair = createPairString(mbr.getName(), null);
						}
					}
					// Character class
					else if (f.getType() == java.lang.Character.class) {
						Character val = (Character) ((Field) mbr).get(beanObj);
						if (val != null) {
							pair = createPairString(mbr.getName(), val.toString());
						} else {
							pair = createPairString(mbr.getName(), null);
						}
					}
					// Character primitive
					else if (f.getType().equals(Character.TYPE)) {
						Character val = (Character) ((Field) mbr).get(beanObj);
						if (val != null) {
							pair = createPairString(mbr.getName(), val.toString());
						} else {
							pair = createPairString(mbr.getName(), null);
						}
					}
					// Boolean class
					else if (f.getType() == java.lang.Boolean.class) {
						Boolean val = (Boolean) ((Field) mbr).get(beanObj);
						if (val != null) {
							pair = createPairBoolean(mbr.getName(), val.booleanValue());
						} else {
							pair = createPairString(mbr.getName(), null);
						}
					}
					// Boolean primitive
					else if (f.getType().equals(Boolean.TYPE)) {
						Boolean val = (Boolean) ((Field) mbr).get(beanObj);
						if (val != null) {
							pair = createPairBoolean(mbr.getName(), val.booleanValue());
						} else {
							pair = createPairString(mbr.getName(), null);
						}
					}
					////////////////////////
					// URI class
					else if (f.getType() == java.net.URI.class) {
						java.net.URI val = (java.net.URI) ((Field) mbr).get(beanObj);
						if (val != null) {
							pair = createPairURI(mbr.getName(), val);
						} else {
							pair = createPairString(mbr.getName(), null);
						}
					}
					////////////////////////
					// List class
					else if (f.getType() == java.util.List.class) {
						JsonValueArray array = new JsonValueArray();
						List<?> val;
						val = (List<?>) ((Field) mbr).get(beanObj);
						if (val != null) {
							String listtype = JsonValueArray.getListTypeName(f, beanObj);
							// Add all the String objects to the array
							for (Object arrayObj : val.toArray()) {
								if (arrayObj != null) {
									if (listValuesAreBeans) {
										array.addElement(JsonValueObject.createObjectFromBeanInternal(arrayObj));
									} else if (listtype.equalsIgnoreCase(Integer.class.getName())) {
										array.addElement(new JsonValueNumberLong(new Integer(arrayObj.toString())));
									} else if (listtype.equalsIgnoreCase(Long.class.getName())) {
										array.addElement(new JsonValueNumberLong(new Long(arrayObj.toString())));
									} else if (listtype.equalsIgnoreCase(Short.class.getName())) {
										array.addElement(new JsonValueNumberLong(new Short(arrayObj.toString())));
									} else if (listtype.equalsIgnoreCase(Double.class.getName())) {
										array.addElement(new JsonValueNumberDouble(new Double(arrayObj.toString())));
									} else if (listtype.equalsIgnoreCase(Float.class.getName())) {
										array.addElement(new JsonValueNumberDouble(new Float(arrayObj.toString())));
									} else if (listtype.equalsIgnoreCase(Character.class.getName())) {
										array.addElement(new JsonValueString(arrayObj.toString()));
									} else if (listtype.equalsIgnoreCase(Boolean.class.getName())) {
										Boolean b = new Boolean(arrayObj.toString());
										if (b.booleanValue()) {
											array.addElement(new JsonValueTrue());
										} else {
											array.addElement(new JsonValueFalse());
										}
									} else {
										array.addElement(new JsonValueString(arrayObj.toString()));
									}
								}
							}
						}
						JsonPair arrayPair = new JsonPair(JsonString.createJsonString(mbr.getName()), array);
						pair = arrayPair;
					}
					////////////////////////////
					// Map class
					else if (f.getType() == java.util.Map.class) {
						// get the Map this is this member
						Map<?, ?> val = (Map<?, ?>) ((Field) mbr).get(beanObj);
						// if the map is not nulll
						if (val != null) {
							///////////////////////////
							// Iterate through the keys of this map
							for (Object keyObj : val.keySet().toArray()) {
								///////////////////////////////////
								// Provided the map key is a string, we need to create a JSON pair with this
								// map key as the pair name
								if (keyObj instanceof java.lang.String) {
									/////////////////////////////
									// Get the value of the map
									Object valObj = val.get(keyObj);
									/////////////////////////////
									// The value of the map is a List
									if (mapValuesAreBeans && (valObj != null) && (valObj instanceof List)) {
										List<?> mapList = (List<?>) valObj;
										JsonValueArray array = new JsonValueArray();
										if (mapList != null) {
											// Loop across each object in the List
											for (Object arrayObj : mapList.toArray()) {
												if (arrayObj != null) {
													array.addElement(JsonValueObject.createObjectFromBeanInternal(arrayObj));
												}
											}
										}
										ret.add(new JsonPair(JsonString.createJsonString(keyObj.toString()), array));
									}
									///////////////////////////////
									// The value of the map is not a List, but should be treated as a bean
									else if (mapValuesAreBeans && (valObj != null)) {
										JsonValueObject valBean = JsonValueObject.createObjectFromBeanInternal(valObj);
										pair = createPairObject(keyObj.toString(), valBean);
										ret.add(pair);
									}
									////////////////////////////////
									// The value in an integer
									else if ((valObj != null) && (valObj instanceof Integer)) {
										Integer valObjCast = (Integer) valObj;
										pair = createPairLong(keyObj.toString(), valObjCast.longValue());
										ret.add(pair);
									}
									////////////////////////////////
									// The value in an long
									else if ((valObj != null) && (valObj instanceof Long)) {
										Long valObjCast = (Long) valObj;
										pair = createPairLong(keyObj.toString(), valObjCast.longValue());
										ret.add(pair);
									}
									////////////////////////////////
									// The value in an short
									else if ((valObj != null) && (valObj instanceof Short)) {
										Short valObjCast = (Short) valObj;
										pair = createPairLong(keyObj.toString(), valObjCast.longValue());
										ret.add(pair);
									}
									////////////////////////////////
									// The value in an float
									else if ((valObj != null) && (valObj instanceof Float)) {
										Float valObjCast = (Float) valObj;
										pair = createPairFloat(keyObj.toString(), valObjCast.floatValue());
										ret.add(pair);
									}
									////////////////////////////////
									// The value in an double
									else if ((valObj != null) && (valObj instanceof Double)) {
										Double valObjCast = (Double) valObj;
										pair = createPairFloat(keyObj.toString(), valObjCast.doubleValue());
										ret.add(pair);
									}
									////////////////////////////////
									// The value in a boolean
									else if ((valObj != null) && (valObj instanceof Boolean)) {
										Boolean valObjCast = (Boolean) valObj;
										pair = createPairBoolean(keyObj.toString(),
												valObjCast.booleanValue());
										ret.add(pair);
									}
									////////////////////////////////
									// The value should be a string. The value vould be a String, Chat, URI,
									// etc.
									else if (valObj != null) {
										pair = createPairString(keyObj.toString(), valObj.toString());
										ret.add(pair);
									}
									////////////////////////////////
									// There is no value in the map
									else if (valObj == null) {
										if (required) {
											throw new JsonException("Map " + keyObj + " must not have a null value");
										}
										pair = createPairString(keyObj.toString(), null);
										ret.add(pair);
									}
								}
								pair = null;
							}
						}
					}
					/////////////////////
					// A POJO
					else {
						Object basicObj = new Object();
						if (basicObj.getClass().isInstance(f.get(beanObj))) {
							basicObj = ((Field) mbr).get(beanObj);
							JsonValueObject val = JsonValueObject.createObjectFromBeanInternal(basicObj);
							pair = createPairObject(mbr.getName(), val);
						}
					}
					if (pair != null) {
						if (required) {
							if (pair.getValue().isNull()) {
								throw new JsonException(
										"Json Pair " + pair.getName() + " must not have a null value");
							}
						}
						ret.add(pair);
					}
				}
			}
		}
		return ret;
	}

	/**
	 * Create a string value using a URI
	 *
	 * @param name
	 * @param uri
	 * @return The JsonPair with the name and the URI
	 */
	static public JsonPair createPairURI(String name, URI uri) {
		JsonString n = new JsonString(name);
		JsonValueString v = new JsonValueString(uri.toString());
		return new JsonPair(n, v);
	}

	/**
	 * Create a JSON name value pair, where the value is an array of Strings
	 *
	 * @param name
	 * @param values
	 * @return
	 */
	static public JsonPair createPairStringArray(String name, String[] values) {
		JsonString n = new JsonString(name);
		JsonValueArray v = new JsonValueArray();
		for (String value : values) {
			v.addElement(new JsonValueString(value));
		}
		return new JsonPair(n, v);
	}

	/**
	 * Create a JSON name value pair, where the value is a String
	 *
	 * @param name
	 * @param value
	 * @return
	 */
	static public JsonPair createPairString(String name, String value) {
		JsonString n = new JsonString(name);
		JsonValueString v = new JsonValueString(value);
		return new JsonPair(n, v);
	}

	/**
	 * Create a JSON name value pair, where the value is another JSON object
	 *
	 * @param name
	 * @param value
	 * @return
	 */
	static public JsonPair createPairObject(String name, JsonValueObject value) {
		JsonString n = new JsonString(name);
		return new JsonPair(n, value);
	}

	/**
	 * Create a JSON name value pair, where the value is a Long
	 *
	 * @param name
	 * @param value
	 * @return
	 */
	static public JsonPair createPairLong(String name, long value) {
		JsonString n = new JsonString(name);
		JsonValueNumberLong v = new JsonValueNumberLong(value);
		return new JsonPair(n, v);
	}

	/**
	 * Create a JSON name value pair, where the value is a double/float
	 *
	 * @param name
	 * @param value
	 * @return
	 */
	static public JsonPair createPairFloat(String name, double value) {
		JsonString n = new JsonString(name);
		JsonValueNumberDouble v = new JsonValueNumberDouble(value);
		return new JsonPair(n, v);
	}

	/**
	 * Create a JSON name value pair, where the value is a single character represented as a JSON
	 * string
	 *
	 * @param name
	 * @param value
	 * @return
	 */
	static public JsonPair createPairChar(String name, char value) {
		JsonString n = new JsonString(name);
		JsonValueString v = new JsonValueString(new String() + value);
		return new JsonPair(n, v);
	}

	/**
	 * Create a JSON name value pair, where the value is a boolean
	 *
	 * @param name
	 * @param value
	 * @return
	 */
	static public JsonPair createPairBoolean(String name, boolean value) {
		JsonString n = new JsonString(name);
		if (value) {
			return new JsonPair(n, new JsonValueTrue());
		}
		return new JsonPair(n, new JsonValueFalse());
	}

}
