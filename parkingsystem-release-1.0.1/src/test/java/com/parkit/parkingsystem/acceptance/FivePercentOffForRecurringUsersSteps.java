package com.parkit.parkingsystem.acceptance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import com.parkit.parkingsystem.view.Viewer;
import com.parkit.parkingsystem.view.ViewerImpl;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

/**
 * Implements scenario steps defined in freeThirtyMinutesOrLessSteps.feature
 * Before steps declares : DataBaseTestConfig, DAOs, DataBasePrepareService, InputReaderUtil, Viewer, ParkingService, expected in and out time Date
 *  
 * @author Olivier MOREL
 *
 */
@ExtendWith(MockitoExtension.class) //Is this necessary ?
public class FivePercentOffForRecurringUsersSteps {

    private DataBaseTestConfig dataBaseTestConfig;
    private ParkingSpotDAO parkingSpotDAO;
    private TicketDAO ticketDAO;
    private DataBasePrepareService dataBasePrepareService;
    private InputReaderUtil inputReaderUtil;
    private Viewer viewer;
    ParkingService parkingService;
	Date expectedInTime;
	Date expectedOutTime;
	
	/**
	 * Given Step :
	 *  - initializes DataBaseTestConfig, Viewer, ParkingService,
	 *  - initializes DAOs and sets DataBaseTestConfig,
	 *  - initializes DataBasePrepareService and clear DB entries,
	 *  - mocks InputReaderUtil and configures when...then... for vehicle registration number given in step definition,
	 *  - initializes expected in and out times Date. In time is current DateTime minus 1 hour,
	 *  - constructs a TestResults list with vehicle registration number : used parking 11 times 1 month ago and parked 1 hour ago
	 *  - and persists it in Test DB.
	 * 
	 * @param regNum : vehicle registration number given in step definition
	 * @param recurrence : times user used parking the last month
	 */
	@Given("utilisateur avec l'immatriculation {word} qui s’est garé plus de {int} fois dans le parking le mois précédent;")
	public void recurrentUserParkedSinceOneHour(String regNum, int recurrence) {
		dataBaseTestConfig = new DataBaseTestConfig();
		parkingSpotDAO= new ParkingSpotDAO();
		parkingSpotDAO.setDataBaseConfig(dataBaseTestConfig);
		ticketDAO = new TicketDAO();
		ticketDAO.setDataBaseConfig(dataBaseTestConfig);
		dataBasePrepareService  = new DataBasePrepareService();
		dataBasePrepareService.clearDataBaseEntries(); // "update parking set available = true" , "truncate table ticket"
		inputReaderUtil = mock(InputReaderUtil.class); //To mock user input (this class itself uses final class Scanner)
	    /* You can use Cucumber and Mockito at the same time.
	     * You can't use two JUnit runners at the same time.
	     * But if you add Mockito as a dependency to your project and create your mocks like this:
	     * ClassToMock mockedClass = mock(ClassToMock.class); then you should be able to combine the tools.
	     * https://stackoverflow.com/questions/38001759/run-cucumber-test-with-mockito*/
		viewer = new ViewerImpl();
		parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, viewer);
		try {
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(regNum);
		} catch(Exception e) {
			e.printStackTrace();
		}
    	List<TestResult> tResults = new ArrayList<>(); //TestResult is a nested class with fields to set/collect ResulSet fields, see below
		expectedInTime = new Date(System.currentTimeMillis() - (3600 * 1000));
		expectedOutTime = new Date();
		Calendar inTimeCal = new GregorianCalendar(); //no deprecated methods
		Calendar outTimeCal = new GregorianCalendar();
		inTimeCal.setTime(expectedInTime);
		outTimeCal.setTime(expectedOutTime);
		inTimeCal.set(Calendar.DATE, 1); //set date at the begin of month
		outTimeCal.set(Calendar.DATE, 1);
		inTimeCal.add(Calendar.MONTH, -1); //One month ago
		outTimeCal.add(Calendar.MONTH, -1); //Add rule example : 01/01/2022 Calling add(Calendar.MONTH, -1) sets the calendar to 01/12/2021 
		for(int i=0; i<=recurrence; i++) { //loops = recurrence+1 = 11 times
			tResults.add(new TestResult(1, "CAR", true, 1, regNum, 1.50, inTimeCal.getTime(), outTimeCal.getTime()));
			inTimeCal.roll(Calendar.DATE, 2); //add 2 days so 11*2 = 22 days
			outTimeCal.roll(Calendar.DATE, 2); //Roll rule : Larger fields (here MONTH) are unchanged after the call.
		}
		tResults.add(new TestResult(1, "CAR", false, 1, regNum, 0d, expectedInTime, null)); //Remember outTime = null

		Connection con = null;
        try {
            con = dataBaseTestConfig.getConnection(); //throws ClassNotFoundException, SQLException will be caught see catch
            PreparedStatement psT = con.prepareStatement("insert into ticket(PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME) values(?,?,?,?,?)");
            PreparedStatement psP = con.prepareStatement("update parking set available = ? where PARKING_NUMBER = ?");
            tResults.forEach(tR -> {
            	try {
		            psT.setInt(1,tR.parkingSpot);
		            psT.setString(2, tR.vehicleRegNumber);
		            psT.setDouble(3, tR.price);
		            psT.setTimestamp(4, new Timestamp(tR.inTime.getTime()));
		            psT.setTimestamp(5, (tR.outTime == null)?null: (new Timestamp(tR.outTime.getTime())));
		            psT.execute();
		            if(!tR.available) { // if availability false
		                psP.setBoolean(1, tR.available);
		                psP.setInt(2, tR.parkingNumber);
		                psP.executeUpdate();
		            }
            	} catch(Exception ex) {
            		ex.printStackTrace();
            	}
            });
            dataBaseTestConfig.closePreparedStatement(psT);
            dataBaseTestConfig.closePreparedStatement(psP);
        } catch(Exception ex) {
        	ex.printStackTrace();
        } finally {
            dataBaseTestConfig.closeConnection(con);
        }
        con = null;
        tResults.clear(); // Clear the list
	}

	/**
	 * When Step
	 * User exits parking
	 */	
	@When("il sort après une heure de parking;")
	public void userParkingExit() {
		parkingService.processExitingVehicle();
	}
		
	/**
	 *  Then Step :
	 *  - declares and initializes a TestResult which is a nested class with fields to collect ResulSet fields, see below
	 *  - tests if parking spots availability persisted to true,
	 *  - tests if ticket persisted has a vehicle number sets with regNum, a fare with reduc % off and correct inTime, outTime ...
	 *  - unset and nullify : parkingService, parkingSpotDAO, ticketDAO, dataBasePrepareService, dataBaseTestConfig, viewer, inputReaderUtil, expectedInTime and expected outTime 
	 * @param regNum: vehicle registration number given in step definition for each outline
	 * @param reduc : % off (5)
	 * @param availability : given in step definition = true
	 */
	@Then("le ticket persisté a une plaque {word}, un tarif réduit de {int} % et la place persistée a une disponibilité {word};")
	public void persistedTicketRegNumFareDiscountFiveSpotTrue(String regNum, int reduc, String availability) {
    	TestResult tResult = new TestResult();
    	Connection con = null;
        try {
            con = dataBaseTestConfig.getConnection(); //throws ClassNotFoundException, SQLException will be caught see catch
            PreparedStatement ps = con.prepareStatement("select p.PARKING_NUMBER, p.TYPE, p.AVAILABLE, "
            		+ "t.PARKING_NUMBER, t.VEHICLE_REG_NUMBER, t.PRICE, t.IN_TIME, t.OUT_TIME "
            		+ "from parking p inner join ticket t on p.PARKING_NUMBER = t.PARKING_NUMBER "
            		+ "where t.In_TIME = ?");
            Timestamp ts = new Timestamp(BigDecimal.valueOf(expectedInTime.getTime()/1000d).setScale(0, RoundingMode.HALF_UP).longValue()*1000);
            //To Round Half Up from millisecond (d for double) to second (long so no d) because MySQL do this.
            ps.setTimestamp(1, ts);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                tResult.parkingNumber = rs.getInt(1);
            	tResult.type = rs.getString(2);
            	tResult.available = rs.getBoolean(3);
            	tResult.parkingSpot = rs.getInt(4);
            	tResult.vehicleRegNumber = rs.getString(5);
            	tResult.price = rs.getDouble(6);
            	tResult.inTime = new Date(rs.getTimestamp(7).getTime());
            	tResult.outTime = (rs.getTimestamp(8) == null)?null: new Date(rs.getTimestamp(8).getTime());
            }
            dataBaseTestConfig.closeResultSet(rs);
            dataBaseTestConfig.closePreparedStatement(ps);
        } catch(Exception ex) {
        	ex.printStackTrace();
        } finally {
            dataBaseTestConfig.closeConnection(con);
        }
        con = null;
 
        try {
			verify(inputReaderUtil, times(1)).readVehicleRegistrationNumber(); // 1 time used
		} catch(Exception e) {
			e.printStackTrace();
		}
        assertThat(tResult)
        	.extracting(
        			tR -> tR.parkingNumber,
        			tR -> tR.type,
        			tR -> tR.available,
        			tR -> tR.parkingSpot,
        			tR -> tR.vehicleRegNumber,
        			tR -> tR.price) 
        	.containsExactly(
        			1,
        			"CAR",
        			Boolean.valueOf(availability).booleanValue(), //(1)
        			1,
        			regNum,
        			BigDecimal.valueOf(1.50*(1-reduc/100d)).setScale(2, RoundingMode.HALF_UP).doubleValue()); //reduc is int so d for decimal division 
        assertThat(tResult.inTime).isCloseTo(expectedInTime, 3000);
        assertThat(tResult.outTime).isCloseTo(expectedOutTime, 3000);
        	/* Verifies that the output Dates are close to the expected Dates by less than delta (expressed in milliseconds),
        	 * if difference is equal to delta it's ok. */
        	/*(1) :
        	 * Cucumber comes with the following built-in parameter types :
        	 * {int}, {float}, {word}, {string}, {} anonymous, {bigdecimal}, {double}, {biginteger}, {byte}, {short} and {long}.
        	 * So not boolean ...
        	 * https://github.com/cucumber/cucumber-expressions#readme */
        
        parkingService = null;
        parkingSpotDAO.setDataBaseConfig(null);
        parkingSpotDAO = null;
        ticketDAO.setDataBaseConfig(null);
        ticketDAO = null;
        dataBasePrepareService = null;
        dataBaseTestConfig = null;
        viewer = null;
        inputReaderUtil = null;
        expectedInTime = null;
        expectedOutTime = null;
    }

	/**
     * Nested class with fields to collect ResulSet fields
     * @author Olivier MOREL
     *
     */
    class TestResult {
    	
		int parkingNumber; //Primary Key
        String type;
        boolean available;

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
