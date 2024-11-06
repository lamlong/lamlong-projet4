package com.parkit.parkingsystem.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;

/**
 * 
 * @author Olivier MOREL
 *
 */
public class TicketDAOSIT {
	
    private DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private DataBasePrepareService dataBasePrepareService;
	private ParkingSpot parkingSpot;
    private List<Ticket> tickets;
	private Calendar inTimeCal;
	private Calendar outTimeCal;
	private Ticket ticket;
	private TicketDAO ticketDAO; //SIT
    
    /**
     * Before Each Test initialize Class Under Test and 
     */
	@BeforeEach
    public void setUpPerTest() {
    	dataBaseTestConfig = new DataBaseTestConfig();
		ticketDAO = new TicketDAO();
		ticketDAO.setDataBaseConfig(dataBaseTestConfig);
		dataBasePrepareService  = new DataBasePrepareService();
		dataBasePrepareService.clearDataBaseEntries(); // "update parking set available = true" , "truncate table ticket"
    	parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
		tickets = new ArrayList<>();
		ticket = new Ticket();
    }

	/**
     * After Each Test nullify :
     *  - Class Under Test
     *  - Ticket's model pointer
     *  - ParkingSpot's model pointer (initialized in test methods)
     *  - ParkingType's enumeration valor;
     */
    @AfterEach
    public void undefPerTest() {
    	dataBaseTestConfig = null;
		ticketDAO.setDataBaseConfig(null);
    	ticketDAO = null;
		dataBasePrepareService  = null;
    	tickets = null;
    	parkingSpot = null;
    	inTimeCal = null;
    	outTimeCal = null;
    	ticket = null;
     }
    
    @ParameterizedTest(name = "{0} times for user FID in park last month should be {1} for recurrent user {2}")
    @CsvSource({"11,true,FID","10,false,FID","11,false,DIF"})
    @DisplayName("Nominal cases")
    public void isRecurringUserTicketTestShouldBeTrueIfMoreTenTimesLastMonth(int times, String isRecurrentS, String regNum) {
    	
    	//GIVEN
		inTimeCal = GregorianCalendar.getInstance();
		inTimeCal.setTimeInMillis(inTimeCal.getTimeInMillis()-3600*1000);
		outTimeCal = GregorianCalendar.getInstance();
		ticket.setParkingSpot(parkingSpot);
		ticket.setVehicleRegNumber(regNum); //current user
		ticket.setPrice(0);
		ticket.setInTime(inTimeCal.getTime());
		ticket.setOutTime(null);
		
		inTimeCal.set(Calendar.DATE, 1); //set date at the begin of month
		outTimeCal.set(Calendar.DATE, 1);
		inTimeCal.add(Calendar.MONTH, -1); //One month ago
		outTimeCal.add(Calendar.MONTH, -1); //Add rule example : 01/01/2022 Calling add(Calendar.MONTH, -1) sets the calendar to 01/12/2021
		Ticket ticketFor;
		for(int i=1; i<=times; i++) { //loops = times
			ticketFor = new Ticket(); //Declare and initialize a new pointer (reference value to object)
			ticketFor.setParkingSpot(parkingSpot);
			ticketFor.setVehicleRegNumber("FID"); //recurring user
			ticketFor.setPrice(1.50);
			ticketFor.setInTime(inTimeCal.getTime());
			ticketFor.setOutTime(outTimeCal.getTime());
			tickets.add(ticketFor); //The pointer (reference value to object) is added in the List
			ticketFor = null; //Nullify pointer to avoid usage in the next loop 
			inTimeCal.roll(Calendar.DATE, 2); //add 2 days so 11*2 = 22 days
			outTimeCal.roll(Calendar.DATE, 2); //Roll rule : Larger fields (here MONTH) are unchanged after the call.
		}
		tickets.add(ticket); //Remember : t.getOutTime()=null
		
		Connection con = null;
        try {
            con = dataBaseTestConfig.getConnection(); //throws ClassNotFoundException, SQLException will be caught see catch
            PreparedStatement psT = con.prepareStatement("insert into ticket(PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME) values(?,?,?,?,?)");
            tickets.forEach(t -> {
            	try {
		            psT.setInt(1, t.getParkingSpot().getId());
		            psT.setString(2, t.getVehicleRegNumber());
		            psT.setDouble(3, t.getPrice());
		            psT.setTimestamp(4, new Timestamp(t.getInTime().getTime()));
		            psT.setTimestamp(5, (t.getOutTime() == null)?null: (new Timestamp(t.getOutTime().getTime())));
		            psT.execute();
            	} catch(SQLException ex) {
            		ex.printStackTrace();
            	} catch(Exception ex) {
            		ex.printStackTrace();
            	}
            });
            	dataBaseTestConfig.closePreparedStatement(psT); //will test (ps != null)
        } catch(SQLException ex) {
    		ex.printStackTrace();
    	} catch(ClassNotFoundException ex) {
    		ex.printStackTrace();
    	} catch(Exception ex){
        	ex.printStackTrace();
        } finally {
            dataBaseTestConfig.closeConnection(con); //will test (con != null)
        }
        con = null;
        tickets.clear(); // Clear the list
        
        //WHEN
        Boolean isRecurent = ticketDAO.isRecurringUserTicket(ticket);

        //THEN
        assertThat(isRecurent).isEqualTo(Boolean.valueOf(isRecurrentS));        
    }
}
