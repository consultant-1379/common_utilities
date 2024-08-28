/*
 * Created on 24.8.2004
 *
 */
package com.distocraft.dc5000.etl.engine.common;

import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * @author savinen
 */
public class Tags {

	private Tags() {
		
	}
	
	/**
	 * tagpPairDelimiter delimited tag pairs from string. Each tag contains name
	 * and value devided by '=' (name=value). Identifier is added to each pairs
	 * name.
	 * 
	 * returns map containing tall found tag pairs.
	 * 
	 * @param identifier
	 * @param dataString
	 * @return
	 * @throws Exception
	 */
	public static HashMap<String,String> GetTagPairs(final String identifier, final String tagpPairDelimiter, final String dataString) {
		/* Read ACTION_CONTENT in to a vector */
		final HashMap<String,String> tagMap = new HashMap<String,String>();
		final StringTokenizer tokens = new StringTokenizer(dataString, tagpPairDelimiter);
		
		while (tokens.hasMoreElements()) {

			/* get the tags name and value */
			final String token = tokens.nextToken();

			/* Name is the first part of the string, before '=' */
			final String tagName = token.substring(0, token.indexOf("="));

			/* Value is the second part of the sttring, after the '=' */
			final String tagValue = token.substring(token.indexOf("=") + 1);

			tagMap.put(identifier + tagName, tagValue);

		}

		return tagMap;

	}

}
