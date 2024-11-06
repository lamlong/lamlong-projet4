package com.parkit.parkingsystem.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.dao.DBConfigIO;
import com.parkit.parkingsystem.dao.LoadDBConfigFromFile;

/**
 * This class helps DAO and factors :
 *  - connection to SGBD (contains configurations)
 *  - close ResultSet (used for queries)
 *  - close PreparedStatement and connection
 * @author Olivier MOREL
 *
 */
public class DataBaseConfig {

    private static final Logger logger = LogManager.getLogger("DataBaseConfig");

    /**
     * To get connected to SGBD
     * @return Connection to SGBD object
     * @throws ClassNotFoundException : if class not found
     * @throws SQLException : An exception that provides information on a database accesserror or other errors. 
     */
    public Connection getConnection() throws ClassNotFoundException, SQLException {
        logger.info("Create DB connection");
        Class.forName("com.mysql.cj.jdbc.Driver");
		DBConfigIO fileDBConfigIO = new LoadDBConfigFromFile();
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/prod",fileDBConfigIO.getDBProperties());
    }

    /**
     * To close connection to SGBD
     * @param con : Connection object
     */
    public void closeConnection(Connection con){
        if(con != null) {
            try {
                con.close();
                logger.info("Closing DB connection");
            } catch(SQLException e) {
                logger.error("Error while closing connection",e);
            }
        }
    }

    /**
     * To close prepared statement
     * @param ps : PreparedStatement object
     */
    public void closePreparedStatement(PreparedStatement ps) {
        if(ps != null) {
            try {
                ps.close();
                logger.info("Closing Prepared Statement");
            } catch(SQLException e) {
                logger.error("Error while closing prepared statement",e);
            }
        }
    }

    /**
     * To close result set statement (queries)
     * @param rs : ResultSet object
     */
    public void closeResultSet(ResultSet rs) {
        if(rs != null) {
            try {
                rs.close();
                logger.info("Closing Result Set");
            } catch(SQLException e) {
                logger.error("Error while closing result set",e);
            }
        }
    }
}
