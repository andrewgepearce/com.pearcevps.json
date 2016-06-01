package com.pearcevps.json;

import java.util.HashMap;
import java.util.Map;

public class JsonString {

	private String str;

	public JsonString(String str) {
		super();
		this.str = str;
		if (str != null) {
			Map<Integer, String> insertionPoints = new HashMap<Integer, String>();
			for (int i = 0; i < this.str.length(); i++) {
				if (this.str.charAt(i) == '\"') {
					int insertionPoint = i;
					insertionPoints.put(insertionPoint, "\\u0022");
				}
				if (this.str.charAt(i) == '\\') {
					int insertionPoint = i;
					insertionPoints.put(insertionPoint, "\\u005C");
				}
				if (this.str.charAt(i) == '\'') {
					int insertionPoint = i;
					insertionPoints.put(insertionPoint, "\\u0027");
				}
				if (this.str.charAt(i) == '\n') {
					int insertionPoint = i;
					insertionPoints.put(insertionPoint, "\\u000A");
				}
				if (this.str.charAt(i) == '\r') {
					int insertionPoint = i;
					insertionPoints.put(insertionPoint, "\\u000D");
				}
				if (this.str.charAt(i) == '\f') {
					int insertionPoint = i;
					insertionPoints.put(insertionPoint, "\\u000C");
				}
				if (this.str.charAt(i) == '\b') {
					int insertionPoint = i;
					insertionPoints.put(insertionPoint, "\\u0008");
				}
				if (this.str.charAt(i) == '\t') {
					int insertionPoint = i;
					insertionPoints.put(insertionPoint, "\\u0009");
				}
			}
			String ret = "";
			for (int i = 0; i < this.str.length(); i++) {
				if (!insertionPoints.containsKey(i)) {
					ret += this.str.charAt(i);
				} else {
					ret += insertionPoints.get(i);
				}
			}
			this.str = ret;
		}
	}

	static public String getJavaStringFromJsonString(String jsonString) {
		String ret = jsonString;
		ret = ret.replaceAll("\\u0022", "\"");
		ret = ret.replaceAll("\\u0027", "\'");
		ret = ret.replaceAll("\\u000A", "\n");
		ret = ret.replaceAll("\\u000D", "\r");
		ret = ret.replaceAll("\\u000C", "\f");
		ret = ret.replaceAll("\\u0008", "\b");
		ret = ret.replaceAll("\\u0009", "\t");
		ret = ret.replaceAll("\\u005c", "\\\\");
		ret = ret.replaceAll("\\u002f", "/");
		return ret;
	}

	public String getJavaString() {
		return JsonString.getJavaStringFromJsonString(this.str);
	}

	public String getJsonStr() {
		return this.str;
	}

	public void setStr(String str) {
		this.str = str;
	}

	@Override
	public String toString() {
		if (this.str == null) {
			return "\"\"";
		}
		return "\"" + this.str + "\"";
	}

	/**
	 * Function to create JSON string value with appropriate escapes and quote marks
	 *
	 * @param value
	 * @return the JSON string
	 */
	static public JsonString createJsonString(String value) {
		return new JsonString(value);
	}
}
