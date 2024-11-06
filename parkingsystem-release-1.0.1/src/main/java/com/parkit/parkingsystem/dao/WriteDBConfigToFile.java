package com.parkit.parkingsystem.dao;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class for writing dp properties
 * @author Olivier MOREL
 *
 */
public class WriteDBConfigToFile implements DBConfigIO{

	private static final Logger logger = LogManager.getLogger("WriteDBConfigToFile");
	private Writer fileWriter;
	private final String userDir = System.getProperty("user.dir"); //=parkingsystem/
	private final String fileToWrite = "db.properties";
	private String filePath = userDir+"/"+ fileToWrite;
	
	/**
	 * Constructor : initialize system resources
	 */
	public WriteDBConfigToFile() {
		try {
			fileWriter = new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8");
		} catch(FileNotFoundException e) {
			logger.error("Error findind file", e);
		} catch(UnsupportedEncodingException e) {
			logger.error("The Character Encoding is not supported", e);
		} catch (Exception e) {
			logger.error("Unexpected error", e);
		}
	}
	
	/**
	 * Close system resources
	 */
	@Override
	public void closeResource() {
		try {
			if (fileWriter != null) {
				fileWriter.close();
			}
		} catch(IOException e) {
			logger.error("Error closing file writer", e);
		} catch(Exception e) {
			logger.error("Unexpected error closing file reader", e);
		}
	}
	
	/**
	 * Saves db properties to file 
	 */
	@Override
	public void setDBProperties(Properties dbProperties) {
		try {
			dbProperties.store(fileWriter, null);
		} catch(IOException e) {
			logger.error("Error writing db.properties" ,e);
		} catch(Exception e) {
			logger.error("Unexpected error writing db.properties", e);
		} finally {
			closeResource();
		}
	}
	
	@Override
	public Properties getDBProperties() {
		return null;
	}
}
