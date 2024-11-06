package com.parkit.parkingsystem.dao;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class for loading dp properties
 * @author Olivier MOREL
 *
 */
public class LoadDBConfigFromFile implements DBConfigIO {
	
	private static final Logger logger = LogManager.getLogger("LoadDBConfigFromFile");
	private Reader fileReader;
	private final String userDir = System.getProperty("user.dir"); //=parkingsystem/
	private final String fileToRead = "db.properties";
	private String filePath = userDir+"/"+ fileToRead;

	/**
	 * Constructor : initialize system resources
	 */
	public LoadDBConfigFromFile() {
		try {
			fileReader = new InputStreamReader(new FileInputStream(filePath), "UTF-8");
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
			if(fileReader != null) {
				fileReader.close();
			}
		} catch(IOException e) {
			logger.error("Error closing file reader", e);
		} catch(Exception e) {
			logger.error("Unexpected error closing file reader", e);
		}
	}
	
	/**
	 * Loads db properties from file 
	 */
	@Override
	public Properties getDBProperties() {
		Properties dbProperties = new Properties();
		try {
			dbProperties.load(fileReader);
		} catch(IOException e) {
			logger.error("Error loading db.properties" ,e);
		} catch(Exception e) {
			logger.error("Unexpected error loading db.properties", e);
		} finally {
			closeResource();
		}
		return dbProperties;
	}

	@Override
	public void setDBProperties(Properties dbProperties) {}
}
