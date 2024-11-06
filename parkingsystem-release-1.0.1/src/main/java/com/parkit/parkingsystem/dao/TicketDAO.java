package com.parkit.parkingsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;

/**
 * Saves (persists), Reads (query) an Updates on table ticket 
 * @author Olivier MOREL
 *
 */
public class TicketDAO {

    private static final Logger logger = LogManager.getLogger("TicketDAO");

    private DataBaseConfig dataBaseConfig = new DataBaseConfig();

    /**
     * Setter for SIT tests
     * @param dataBaseConfig : configuration for database access
     */
     public void setDataBaseConfig(DataBaseConfig dataBaseConfig) {
		this.dataBaseConfig = dataBaseConfig;
	}

	/**
     * Saves (persists) a new ticket model
     * @param ticket : model
     * @return boolean : true = success or false = failure
     */
    public boolean saveTicket(Ticket ticket){
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = dataBaseConfig.getConnection(); //throws ClassNotFoundException, SQLException will be caught see catch
            ps = con.prepareStatement(DBConstants.SAVE_TICKET);
            //PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME
            ps.setInt(1,ticket.getParkingSpot().getId());
            ps.setString(2, ticket.getVehicleRegNumber());
            ps.setDouble(3, ticket.getPrice());
            ps.setTimestamp(4, new Timestamp(ticket.getInTime().getTime()));
            ps.setTimestamp(5, (ticket.getOutTime() == null)?null: (new Timestamp(ticket.getOutTime().getTime())) );
            return ps.execute();
        } catch(Exception ex) {
            logger.error("Error persisting ticket",ex);
            return false;
        } finally { //The finally block will be executed even after a return statement in a method.
        	dataBaseConfig.closePreparedStatement(ps); //will test ps != null
            dataBaseConfig.closeConnection(con);//will test con =! null
        }
    }

    /**
     * Does a query to get the ticket and associated (Foreign Key) parking slot number and type from given
     * vehicle's registration number where out time is null and last time in (order desc, limit 1)
     * "... where p.parking_number = t.parking_number and t.OUT_TIME IS NULL and t.VEHICLE_REG_NUMBER=? order by t.IN_TIME desc limit 1"
     *  
     * @param vehicleRegNumber : vehicle's registration number
     * @return Ticket model object or null
     */
    public Ticket getTicket(String vehicleRegNumber) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Ticket ticket = null;
        try {
            con = dataBaseConfig.getConnection(); //throws ClassNotFoundException, SQLException will be caught see catch
            ps = con.prepareStatement(DBConstants.GET_TICKET);
            //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
            ps.setString(1,vehicleRegNumber);
            rs = ps.executeQuery();
            if(rs.next()) {
                ticket = new Ticket();
                ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(6)),false);
                ticket.setParkingSpot(parkingSpot);
                ticket.setId(rs.getInt(2));
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(rs.getDouble(3));
                ticket.setInTime(rs.getTimestamp(4));
                ticket.setOutTime(rs.getTimestamp(5));
            }
        } catch(Exception ex) {
            logger.error("Error getting ticket",ex);
        } finally {
        	dataBaseConfig.closeResultSet(rs); //will test rs != null
        	dataBaseConfig.closePreparedStatement(ps); //will test ps != null
        	dataBaseConfig.closeConnection(con); //will test con != null
        }
        return ticket; //can return a ticket = null 
    }

    /**
     * Upadates a given Ticket model's object
     * @param ticket : model
     * @return boolean : true = success or false = failure
     */
    public boolean updateTicket(Ticket ticket) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = dataBaseConfig.getConnection(); //throws ClassNotFoundException, SQLException will be caught see catch
            ps = con.prepareStatement(DBConstants.UPDATE_TICKET);
            ps.setDouble(1, ticket.getPrice());
            ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
            ps.setInt(3,ticket.getId());
            ps.execute();
            return true;
        } catch (Exception ex) {
            logger.error("Error saving ticket info",ex);
            return false;
        } finally { //The finally block will be executed even after a return statement in a method.
        	dataBaseConfig.closePreparedStatement(ps); //will test ps != null
        	dataBaseConfig.closeConnection(con); //will test con != null
        }
    }

	/**
	 * How many times user get parked last month
	 * @param ticket : the pointer to object
	 * @return Boolean : true if user parked at least 11 times last month 
	 */
    public Boolean isRecurringUserTicket(Ticket ticket) {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        int times = 0;
        try {
            con = dataBaseConfig.getConnection(); //throws ClassNotFoundException, SQLException will be caught see catch
            ps = con.prepareStatement(DBConstants.GET_TIMES); // LAST_MONTH_BEGIN, LAST_MONT_END, VEHICLE_REG_NUMBER
            Calendar lastMonthBegin = new GregorianCalendar();
            lastMonthBegin.setTime(ticket.getInTime());
            lastMonthBegin.set(Calendar.DATE, 1); //set date at the begin of month
            lastMonthBegin.add(Calendar.MONTH, -1); //Add rule example : 01/01/2022 Calling add(Calendar.MONTH, -1) sets the calendar to 01/12/2021
            Calendar lastMonthEnd = (GregorianCalendar)lastMonthBegin.clone();
            lastMonthBegin.set(Calendar.HOUR_OF_DAY, 0);
            lastMonthBegin.set(Calendar.MINUTE, 0);
            lastMonthBegin.set(Calendar.SECOND, 0);
            lastMonthEnd.set(Calendar.DATE, lastMonthEnd.getActualMaximum(Calendar.DATE));//Go to the last day of Month 
            lastMonthEnd.set(Calendar.HOUR_OF_DAY, 23);
            lastMonthEnd.set(Calendar.MINUTE, 59);
            lastMonthEnd.set(Calendar.SECOND, 59);
            
            ps.setTimestamp(1, new Timestamp(lastMonthBegin.getTimeInMillis()));
            ps.setTimestamp(2, new Timestamp(lastMonthEnd.getTimeInMillis()));
            ps.setString(3, ticket.getVehicleRegNumber());
            rs = ps.executeQuery();
            if(rs.next()){
            times = rs.getInt(1);
            }
        } catch(Exception ex) {
            logger.error("Error getting user last month times",ex);
            return null; //Optional.ofNullable(ticketDAO.isRecurringUserTicket(ticket))... see ParkingService line 209
        } finally {
        	dataBaseConfig.closeResultSet(rs); //will test rs != null
        	dataBaseConfig.closePreparedStatement(ps); //will test ps != null
        	dataBaseConfig.closeConnection(con); //will test con != null
        }
        return times>10;
	}
}