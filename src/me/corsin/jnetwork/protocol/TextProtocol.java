/////////////////////////////////////////////////
// Project : SCJavaNetwork
// Package : me.corsin.jnetwork.protocol
// TextProtocol.java
//
// Author : Simon CORSIN <simoncorsin@gmail.com>
// File created on Oct 23, 2013 at 4:26:18 PM
////////

package me.corsin.jnetwork.protocol;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import me.corsin.javatools.io.IOUtils;

public class TextProtocol implements INetworkProtocol {

	////////////////////////
	// VARIABLES
	////////////////

	////////////////////////
	// CONSTRUCTORS
	////////////////

	////////////////////////
	// METHODS
	////////////////
	
	@Override
	public Object deserialize(InputStream inputStream) throws IOException {
		return IOUtils.readStreamAsString(inputStream);
	}

	@Override
	public InputStream serialize(Object object) {
		if (object instanceof String) {
			try {
				return new ByteArrayInputStream(((String) object).getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}

	////////////////////////
	// GETTERS/SETTERS
	////////////////
}
