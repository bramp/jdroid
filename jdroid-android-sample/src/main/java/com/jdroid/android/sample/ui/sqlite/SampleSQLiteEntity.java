package com.jdroid.android.sample.ui.sqlite;


import com.jdroid.java.domain.Entity;

public class SampleSQLiteEntity extends Entity {

	private String field;

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("{");
		sb.append("id='").append(getId()).append("\', ");
		sb.append("field='").append(field).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
