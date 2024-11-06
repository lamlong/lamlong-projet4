package com.parkit.parkingsystem.dao;

import java.util.Properties;

public interface DBConfigIO {

	void closeResource();
	
	Properties getDBProperties();
	
	void setDBProperties(Properties dbProperties);
}
