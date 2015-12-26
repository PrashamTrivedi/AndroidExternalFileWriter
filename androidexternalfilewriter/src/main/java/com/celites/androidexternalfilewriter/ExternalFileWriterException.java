package com.celites.androidexternalfilewriter;

/**
 * Exception to report back developer about media state or storage state if writing is not possible
 */
public class ExternalFileWriterException
		extends Exception {

	public ExternalFileWriterException(String messege) {
		super(messege);
	}

}
