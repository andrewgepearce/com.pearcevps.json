package com.pearcevps.json;

public class JsonMembers {

	private JsonMembers	members;
	private JsonPair		pair;

	public JsonMembers(JsonPair pair, JsonMembers members) {
		super();
		this.pair = pair;
		this.members = members;
	}

	public JsonMembers getMembers() {
		return this.members;
	}

	public JsonPair getPair() {
		return this.pair;
	}

	public void setMembers(JsonMembers members) {
		this.members = members;
	}

	public void setPair(JsonPair pair) {
		this.pair = pair;
	}

	@Override
	public String toString() {
		if (this.pair == null) {
			return "";
		}
		if (this.members == null) {
			return this.pair.toString();
		}
		return this.pair.toString() + ", " + this.members.toString();
	}
}
