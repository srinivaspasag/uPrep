package com.vedantu.comm.utils;


import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.vedantu.commons.hbase.AbstractHbaseModels;

public class HBaseModelExclusionStrategy implements ExclusionStrategy {

	public boolean shouldSkipClass(Class<?> arg0) {
		return false;
	}

	public boolean shouldSkipField(FieldAttributes f) {

		return (f.getDeclaringClass() == AbstractHbaseModels.class && (f
				.getName().equals("columnFamily")
				|| f.getName().equals("column") || f.getName()
				.equals("wrapper")));

	}
}
