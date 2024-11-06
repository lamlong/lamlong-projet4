package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import com.parkit.parkingsystem.view.Viewer;
import com.parkit.parkingsystem.view.ViewerImpl;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * System Integration Test Class for cases
 *  - parking a car : will fill the parking lot, check that all tickets should be saved in DB,
 *    parking numbers should be updated with availability false and no extra vehicle should be saved
 *  - parking lot exit : will initialize ticket and parking tables with two cars ("CAR1" and "CAR2")
 *    which came and exited yesterday and come and exit today. Check if parking lots and tickets
 *    should be updated with availability true, out date and fare calculated to 1.50.  
 * @author Olivier MOREL
 *
 */
@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig(); //static for @BeforeAll and @AfterAll
    private static ParkingSpotDAO parkingSpotDAO; //static for @BeforeAll and @AfterAll
    private static TicketDAO ticketDAO; //static for @BeforeAll and @AfterAll
    private static DataBasePrepareService dataBasePrepareService; //static for @BeforeAll and @AfterAll

    @Mock
    private static InputReaderUtil inputReaderUtil; //To mock user input (this class itself uses final class Scanner)
    
    private static Viewer viewer;    

    @BeforeAll
    public static void setUp() {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.setDataBaseConfig(dataBaseTestConfig);
        ticketDAO = new TicketDAO();
        ticketDAO.setDataBaseConfig(dataBaseTestConfig);
        dataBasePrepareService = new DataBasePrepareService();
        viewer = new ViewerImpl(); //Viewer instance
    }

    @AfterAll
    public static void tearDown(){
        parkingSpotDAO.setDataBaseConfig(null);
        parkingSpotDAO = null;
        ticketDAO.setDataBaseConfig(null);
        ticketDAO = null;
        dataBasePrepareService = null;
        viewer = null;
    }

    @BeforeEach
    public void setUpPerTest() {
        lenient().when(inputReaderUtil.readSelection()).thenReturn(1).thenReturn(1).thenReturn(1).thenReturn(2).thenReturn(2).thenReturn(1).thenReturn(2);
        // on first call uses first thenReturn, on second uses second ... on seventh uses seventh, on eighth uses first ...
        try {
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("CAR1").thenReturn("CAR2").thenReturn("CAR3").thenReturn("BIKE4").thenReturn("BIKE5").thenReturn("CAR6").thenReturn("BIKE7");
		} catch(Exception e) {
			e.printStackTrace();
		}
        dataBasePrepareService.clearDataBaseEntries(); // "update parking set available = true" , "truncate table ticket"
    }

    /**
     * This method fill the parking lot, check that all tickets should be saved in DB,
     * parking numbers should be updated with availability false
     * and no extra vehicle should be saved
     */
    @Test
    @DisplayName("Check that all tickets should be saved in DB and Parking numbers should be updated with availability false and no extra vehicle should be saved")
    public void testParkingCarsAndBikesShouldSaveTicketsUpdateSlotAvailabilityToFalseAndAndNoMoreVehicles(){
        //GIVEN
    	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, viewer);
    	List<TestResult> tResults = new ArrayList<>(); //TestResult is a nested class with fields to collect ResulSet fields, see below  
		Date expectedInTime = new Date();

		//WHEN & Asserts that Exception was caught for the two extra vehicles
		for(int i=1; i<=7; i++) { //uses mocks seventh time... will fill the parking and test overfilling it
			assertDoesNotThrow(() -> parkingService.processIncomingVehicle());
		}
		
        //THEN
        Connection con = null;
        PreparedStatement ps = null ;
        ResultSet rs = null;
        try {
            con = dataBaseTestConfig.getConnection(); //throws ClassNotFoundException, SQLException will be caught see catch
            ps = con.prepareStatement("select p.PARKING_NUMBER, p.TYPE, p.AVAILABLE, "
            		+ "t.PARKING_NUMBER, t.VEHICLE_REG_NUMBER, t.PRICE, t.IN_TIME, t.OUT_TIME "
            		+ "from parking p inner join ticket t on p.PARKING_NUMBER = t.PARKING_NUMBER");
            rs = ps.executeQuery();
            while (rs.next()) {
                TestResult tResult = new TestResult(); //Declare and initialize a new pointer (reference value to object)
                tResult.parkingNumber = rs.getInt(1); //Primary key (int)
            	tResult.type = rs.getString(2);
            	tResult.available = rs.getBoolean(3);
            	tResult.parkingSpot = rs.getInt(4); //Foreign key (int)
            	tResult.vehicleRegNumber = rs.getString(5);
            	tResult.price = rs.getDouble(6);
            	tResult.inTime = new Date(rs.getTimestamp(7).getTime());
            	tResult.outTime = rs.getTimestamp(8); // = null
            	tResults.add(tResult); //The pointer (reference value to object) is added in the List
            	tResult = null; //Nullify pointer to avoid usage in the next loop 
            }
        } catch(Exception ex) {
        	ex.printStackTrace();
        } finally {
            dataBaseTestConfig.closeResultSet(rs); // will test rs != null
            dataBaseTestConfig.closePreparedStatement(ps); // will test ps != null
            dataBaseTestConfig.closeConnection(con); // will test con != null
        }
 
        verify(inputReaderUtil, times(7)).readSelection(); // 7 times used
        try {
			verify(inputReaderUtil, times(5)).readVehicleRegistrationNumber(); // but only 5 times used because the 2 extra vehicles shouldn't be treated
		} catch(Exception e) {
			e.printStackTrace();
		}
        assertThat(tResults).hasSize(5);
        assertThat(tResults)
        	.extracting(
        			tR -> tR.parkingNumber, //Primary key (int)
        			tR -> tR.type,
        			tR -> tR.available,
        			tR -> tR.parkingSpot, //Foreign key (int)
        			tR -> tR.vehicleRegNumber, 
        			tR -> tR.price,
        			tR -> tR.outTime)
        	.containsExactly(
        			tuple(1, "CAR", false, 1, "CAR1", 0d, null), //d to cast to double
        			tuple(2, "CAR", false, 2, "CAR2", 0d, null),
        			tuple(3, "CAR", false, 3, "CAR3", 0d, null),
        			tuple(4, "BIKE", false, 4, "BIKE4", 0d, null),
        			tuple(5, "BIKE", false, 5, "BIKE5", 0d, null));
        tResults.forEach(tR -> {
        	assertThat(tR.inTime).isCloseTo(expectedInTime, 3000);
        	/* Verifies that the tR.inTime Date is close to the expectedInTime Date by less than delta (expressed in milliseconds),
        	 * if difference is equal to delta it's ok. */ 
        });
    }

    /**
     * This method initialize ticket and parking tables with two cars ("CAR1" and "CAR2") :
     *  - "CAR1" parked 24 hours ago on spot 2, exited 23 hours ago and then parks today 1 hour ago on spot 1
     *  - "CAR2" parked 25 hours ago on spot 1, exited 24 hours ago and then parks today 1 hour ago just after "CAR1" on spot 2
     * Then the two cars exit the park, should update :
     *  - parking spots availability to true
     *  - tickets out dates and calculate fare to 1.50 
     */
    @Test
    @DisplayName("Check that the fares generated, out times are populated correctly and Parking table is updated with availability true in the database")
    public void testParkingLotExitPersitsSpotAvalaibilityTrueAndLastTwoTicketsFareAndOutDateTime(){
        //GIVEN
    	ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, viewer);
    	List<TestResult> tResults = new ArrayList<>(); //TestResult is a nested class with fields to set/collect ResulSet fields, see below
    	
    	Date expectedInTime = new Date(System.currentTimeMillis() - (3600 * 1000));
    	Date expectedOutTime = new Date();
    	
    	tResults.add(new TestResult(1, "CAR", true, 1, "CAR2", 1.50, new Date(expectedInTime.getTime()-86400000), new Date(expectedOutTime.getTime()-86400000)));
    	//86 400 000 = 24*60*60*1000 : CAR2 in park 25 hours ago on spot 1, exited 24 hours ago
    	
    	tResults.add(new TestResult(2, "CAR", true, 2, "CAR1", 1.50, new Date(expectedInTime.getTime()-82800000), new Date(expectedOutTime.getTime()-82800000)));
    	//82 800 000 = 23*60*60*1000 : CAR1 in park 24 hours ago on spot 2, exited 23 hours ago
    	
    	tResults.add(new TestResult(1, "CAR", false, 1, "CAR1", 0d, expectedInTime, null)); //Remember outTime = null
    	// CAR1 in park 1 hour ago on spot 1

    	tResults.add(new TestResult(2, "CAR", false, 2, "CAR2", 0d, expectedInTime, null)); //Remember outTime = null
    	// CAR2 just after CAR1 in park 1 hour ago on spot 2
    	    	
        Connection con = null;
        try {
            con = dataBaseTestConfig.getConnection(); //throws ClassNotFoundException, SQLException will be caught see catch
            PreparedStatement psT = con.prepareStatement("insert into ticket(PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME) values(?,?,?,?,?)");
            PreparedStatement psP = con.prepareStatement("update parking set available = ? where PARKING_NUMBER = ?");
            tResults.forEach(tR -> {
            	try {
		            psT.setInt(1,tR.parkingSpot); //Foreign key (int)
		            psT.setString(2, tR.vehicleRegNumber);
		            psT.setDouble(3, tR.price);
		            psT.setTimestamp(4, new Timestamp(tR.inTime.getTime()));
		            psT.setTimestamp(5, (tR.outTime == null)?null: (new Timestamp(tR.outTime.getTime())));
		            psT.execute();
		            if(!tR.available) { // if availability false
		                psP.setBoolean(1, tR.available);
		                psP.setInt(2, tR.parkingNumber); //Primary key (int)
		                psP.executeUpdate();
		            }
            	} catch(Exception ex) {
            		ex.printStackTrace();
            	}
            });
            dataBaseTestConfig.closePreparedStatement(psT); //will test psT != null
            dataBaseTestConfig.closePreparedStatement(psP); //will test psP != null
        } catch(Exception ex) {
        	ex.printStackTrace();
        } finally {
            dataBaseTestConfig.closeConnection(con); //will test con != null
        }  	
        tResults.clear(); // Clear the list
        
        //WHEN
        for(int i=1; i<=2; i++) { //use mocks two time to exit the 2 cars
        	parkingService.processExitingVehicle();
		}
    	
        //THEN
        con = null;
        try {
            con = dataBaseTestConfig.getConnection(); //throws ClassNotFoundException, SQLException will be caught see catch
            PreparedStatement ps = con.prepareStatement("select p.PARKING_NUMBER, p.TYPE, p.AVAILABLE, "
            		+ "t.PARKING_NUMBER, t.VEHICLE_REG_NUMBER, t.PRICE, t.IN_TIME, t.OUT_TIME "
            		+ "from parking p inner join ticket t on p.PARKING_NUMBER = t.PARKING_NUMBER "
            		+ "order by t.ID desc limit 2"); //only choose the two last one in descending order
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                TestResult tResult = new TestResult(); //Declare and initialize a new pointer (reference value to object)
                tResult.parkingNumber = rs.getInt(1); //Primary key (int)
            	tResult.type = rs.getString(2);
            	tResult.available = rs.getBoolean(3);
            	tResult.parkingSpot = rs.getInt(4); //Foreign key (int)
            	tResult.vehicleRegNumber = rs.getString(5);
            	tResult.price = rs.getDouble(6);
            	tResult.inTime = new Date(rs.getTimestamp(7).getTime());
            	tResult.outTime = (rs.getTimestamp(8) == null)?null: new Date(rs.getTimestamp(8).getTime());
            	tResults.add(tResult); //The pointer (reference value to object) is added in the List
            	tResult = null; //Nullify pointer to avoid usage in the next loop 
            }
            dataBaseTestConfig.closeResultSet(rs); //will test rs != null
            dataBaseTestConfig.closePreparedStatement(ps); //will test ps != null
        } catch(Exception ex) {
        	ex.printStackTrace();
        } finally {
            dataBaseTestConfig.closeConnection(con); //will test com != null
        }
 
        try {
			verify(inputReaderUtil, times(2)).readVehicleRegistrationNumber(); // 2 times used
		} catch(Exception e) {
			e.printStackTrace();
		}
        assertThat(tResults.size()).isEqualTo(2);
        assertThat(tResults)
        	.extracting(
        			tR -> tR.parkingNumber,  //Primary key (int)
        			tR -> tR.type,
        			tR -> tR.available,
        			tR -> tR.parkingSpot,  //Foreign key (int)
        			tR -> tR.vehicleRegNumber,
        			tR -> tR.price) 
        	.containsExactly( // descending order
        			tuple(2, "CAR", true, 2, "CAR2", 1.5),
        			tuple(1, "CAR", true, 1, "CAR1", 1.5));
        tResults.forEach(tR -> {
        	assertThat(tR.inTime).isCloseTo(expectedInTime, 3000);
        	assertThat(tR.outTime).isCloseTo(expectedOutTime, 3000);
        	/* Verifies that the output Dates are close to the expected Dates by less than delta (expressed in milliseconds),
        	 * if difference is equal to delta it's ok. */ 
        });
    }
    
    /**
     * Nested class with fields to collect/set ResulSet fields
     * @author Olivier MOREL
     *
     */
    private class TestResult {
    	
    	//ParkingSpot
		int parkingNumber; //Primary Key
        String type;
        boolean available;

        //Ticket
        int parkingSpot; //Foreign Key
        String vehicleRegNumber;
        double price;
        Date inTime;
        Date outTime;
 
        TestResult() {
 			this.parkingNumber = 0;
 			this.type = null;
 			this.available = false;
 			this.parkingSpot = 0;
 			this.vehicleRegNumber = null;
 			this.price = 0d;
 			this.inTime = null;
 			this.outTime = null;
        }
 		
        TestResult(int parkingNumber, String type, boolean available, int parkingSpot, String vehicleRegNumber,
				double price, Date inTime, Date outTime) {
			this.parkingNumber = parkingNumber;
			this.type = type;
			this.available = available;
			this.parkingSpot = parkingSpot;
			this.vehicleRegNumber = vehicleRegNumber;
			this.price = price;
			this.inTime = inTime;
			this.outTime = outTime;
		}
    }
}
