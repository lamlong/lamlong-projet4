package com.parkit.parkingsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;

/**
 * Reads (query) and Updates on table parking 
 * @author Olivier MOREL
 *
 */
public class ParkingSpotDAO {
    private static final Logger logger = LogManager.getLogger("ParkingSpotDAO");

    private DataBaseConfig dataBaseConfig = new DataBaseConfig();

    /**
     * Setter for SIT tests
     * @param dataBaseConfig : configuration for database
     */
    public void setDataBaseConfig(DataBaseConfig dataBaseConfig) {
		this.dataBaseConfig = dataBaseConfig;
	}

    
    /**
     * Does a query to get the first minimal index of an available parking spot for a given vehicule's type
     * @param parkingType : given vehicule's type
     * @return the index of the available parking spot
     */
    public int getNextAvailableSlot(ParkingType parkingType) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs =null;
        int result=-1;
        try {
            con = dataBaseConfig.getConnection(); //throws ClassNotFoundException, SQLException will be caught see catch
            ps = con.prepareStatement(DBConstants.GET_NEXT_PARKING_SPOT);
            ps.setString(1, parkingType.toString());
            rs = ps.executeQuery();
            if(rs.next()){
                result = rs.getInt(1);
            }
        } catch(ClassNotFoundException e) {
        	logger.error("Error fetching next available slot",e);
        } catch(SQLException e) {
        	logger.error("Error fetching next available slot",e);
        } catch(Exception ex) {
            logger.error("Error fetching next available slot",ex);
        } finally {
            dataBaseConfig.closeResultSet(rs); //will test rs != null
            dataBaseConfig.closePreparedStatement(ps); //will test ps != null
            dataBaseConfig.closeConnection(con);  //will test con != null
        }
        return result;
    }

	/**
     * updates field available for a given ParkingSpot's identifier
     * @param parkingSpot : model
     * @return boolean : success or failure to get update
     */
    public boolean updateParking(ParkingSpot parkingSpot){
        //update the availability for that parking slot
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = dataBaseConfig.getConnection(); //throws ClassNotFoundException, SQLException will be caught see catch
            ps = con.prepareStatement(DBConstants.UPDATE_PARKING_SPOT);
            ps.setBoolean(1, parkingSpot.isAvailable());
            ps.setInt(2, parkingSpot.getId());
            int updateRowCount = ps.executeUpdate();
            dataBaseConfig.closePreparedStatement(ps);
            return (updateRowCount == 1);
        } catch(Exception ex) {
            logger.error("Error updating parking info",ex);
            return false;
        } finally { //The finally block will be executed even after a return statement in a method.
            dataBaseConfig.closePreparedStatement(ps); //will test ps != null
            dataBaseConfig.closeConnection(con); //will test con != null
        }
    }
}
