package com.parkit.parkingsystem.service;

import java.util.Date;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;
import com.parkit.parkingsystem.view.Viewer;

/**
 * Used by InteractiveShell
 * @author Olivier MOREL
 *
 */
public class ParkingService {

    private static final Logger logger = LogManager.getLogger("ParkingService");

    private FareCalculatorService fareCalculatorService = new FareCalculatorService();

    private InputReaderUtil inputReaderUtil;
    private ParkingSpotDAO parkingSpotDAO;
    private TicketDAO ticketDAO;
    private Viewer viewer; // Declare Viewer instance

	/**
     * Constructor
     * @param inputReaderUtil to read keyboard input and give an expected result
     * @param parkingSpotDAO for CRUD : Create, Read, Update and Delete on table parking
     * @param ticketDAO for CRUD : Create, Read, Update and Delete on table ticket
     * @param viewer get initialized instance of ViewerImpl
     */
    public ParkingService(InputReaderUtil inputReaderUtil, ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO, Viewer viewer) {
    	this.inputReaderUtil = inputReaderUtil;
        this.parkingSpotDAO = parkingSpotDAO;
        this.ticketDAO = ticketDAO;
        this.viewer = viewer;
    }
    
    /**
     * Processing incoming vehicle : tries to get an available parking space (asks for vehicule's type)
     * then asks for vehicle's registered number, marks place occupied then creates a new model Ticket
     * and persists it into SGBD
     */
    public void processIncomingVehicle() {
        try {
            ParkingSpot parkingSpot = getNextParkingNumberIfAvailable(); //Declare and try to get a available ParkingSpot model
            if(parkingSpot !=null && parkingSpot.getId() > 0) {
                String vehicleRegNumber = getVehichleRegNumber(); //Throws Exception if invalid input, will be caught see catch
                parkingSpot.setAvailable(false);
                parkingSpotDAO.updateParking(parkingSpot);//allot this parking space and mark it's availability as false
                // !!! WARNING What to do if it returns false ? In DAO : logger.error("Error updating parking info",ex)

                /* Needs computer standards are defined in terms of Greenwich mean time (GMT)
                 * to prevent summer/winter timetable changes if the car park is used at night
                 * but will need a time zone offset to display in LocalDateTime 
                 * Or use of GregorianCalendar :
                 *  // create a Central Europe Standard Time time zone
 				 * SimpleTimeZone cet = new SimpleTimeZone(+1 * 60 * 60 * 1000, ids[0]);
				 *  // set up rules for Daylight Saving Time
				 * cet.setStartRule(Calendar.APRIL, 1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);
 				 * cet.setEndRule(Calendar.OCTOBER, -1, Calendar.SUNDAY, 2 * 60 * 60 * 1000);*/

                Date inTime = new Date();
                Ticket ticket = new Ticket();
                //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
                //ticket.setId(ticketID);
                ticket.setParkingSpot(parkingSpot);
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(0);
                ticket.setInTime(inTime);
                ticket.setOutTime(null);
                ticketDAO.saveTicket(ticket);
                // !!! WARNING What to do if it returns false ? In DAO : logger.error("Error persisting ticket",ex);
                
                viewer.println("Generated Ticket and saved in DB");
                viewer.println("Please park your vehicle in spot number:"+parkingSpot.getId());
                viewer.println("Recorded in-time for vehicle number:"+vehicleRegNumber+" is:"+inTime);
                /*inTime.toString() : Converts this Date object to a String of the form: dow mon dd hh:mm:ss zzz yyyy
                 *where:
				 *	dow is the day of the week (Sun, Mon, Tue, Wed, Thu, Fri, Sat).
				 *	mon is the month (Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec).
				 *	dd is the day of the month (01 through 31), as two decimal digits.
				 *	hh is the hour of the day (00 through 23), as two decimal digits.
				 *	mm is the minute within the hour (00 through 59), as two decimal digits.
				 *	ss is the second within the minute (00 through 61, as two decimal digits.
				 *	zzz is the time zone (and may reflect daylight saving time). Standard time zone abbreviations include those recognized by the method parse.
				 *	If time zone information is not available, then zzz is empty - that is, it consists of no characters at all.
				 *	yyyy is the year, as four decimal digits.*/
            }
        } catch(Exception e) { //if invalid vehivule's registered number input. Warning message already shown on console in InputReader method
            logger.error("Unable to process incoming vehicle",e);
        }
    }

    /**
     * Tries to get an available parking space but before ask vehicule's type
     * @return an available model ParkingSpot
     */
    private ParkingSpot getNextParkingNumberIfAvailable() {
        int parkingNumber=0;
        ParkingSpot parkingSpot = null;
        try {
            ParkingType parkingType = getVehichleType(); // Throws IllegalArgumentException Will be caught see catch
            parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
            if(parkingNumber > 0) {
                parkingSpot = new ParkingSpot(parkingNumber,parkingType, true);
            } else {
                throw new Exception("Error fetching parking number from DB. Parking slots might be full"); //Will be caught see catch
            }
        } catch(IllegalArgumentException ie) {
        	viewer.println("Incorrect input provided : provide 1 or 2");
        	logger.error("Error parsing user input for type of vehicle", ie);
        } catch(Exception e) {
        	viewer.println("Parking slots might be full");
        	logger.error("Error fetching next available parking slot", e);
        }
        return parkingSpot; //null if an exception occurs
    }

    /**
     * Asks and gets vehicule's type
     * @return Enumeration ParkingType is the vehicule's type
     * @throws IllegalArgumentException if incorrect input
     */
    private ParkingType getVehichleType() throws IllegalArgumentException{ // Throws optional because RuntimeException but better readability
    	viewer.println("Please select vehicle type from menu");
    	viewer.println("1 CAR");
    	viewer.println("2 BIKE");
        int input = inputReaderUtil.readSelection(); //return -1 if an exception occurred
        switch(input) {
            case 1: {
                return ParkingType.CAR; //no break because return
            }
            case 2: {
                return ParkingType.BIKE; //no break because return
            }
            default: {
                throw new IllegalArgumentException("Entered input is invalid"); //Throw a java.lang.RuntimeException 
            }
        }
    }
    
    /**
     * Prints the request for vehicle's registered number
     * @return
     * @throws Exception if null or only blank space or invalid String input
     */
    private String getVehichleRegNumber() throws Exception {
    	viewer.println("Please type the vehicle registration number and press enter key");
        return inputReaderUtil.readVehicleRegistrationNumber(); // Throws Exception if null or only blank space or invalid String input
        /* !!! WARNING What to do if :
         * GIVEN : user A with registered number "ABCDEF" came at 9.00
         * WHEN :  user B with registered number "ABCDEG" comes at 11:00
         *         and does a mistake inputing registered "ABCDEF"
         *         but there is no check that "ABCDEF" is already parked
         * THEN : user A will exit at 12:00 and will pay only 1 hour not 3 !
         *        user B will exit at 13:00 and will pay 4 hours ! not only 2 !
         */ 
    }

    /**
     * Processing exiting vehicle : asks for vehicle's registered number, uses DAO to query the ticket, set out time,
     * calls FareCalculatorService to calculate fare, updates ticket using DAO, set parking spot available and updates it with DAO  
     */
    public void processExitingVehicle() {
    	try {
            String vehicleRegNumber = getVehichleRegNumber(); //Throws Exception if invalid input, Will be caught see catch
            Ticket ticket = ticketDAO.getTicket(vehicleRegNumber); //can return null 
            Date outTime = new Date();
            ticket.setOutTime(outTime); //if ticket = null throws a NullPointerException, Will be caught see catch
            fareCalculatorService.calculateFare(ticket); // Throws IllegalArgumentException, Will be caught see catch
            //ticket is a pointer to the object. Only object'll be modified
            if(isRecurringUser(ticket)) {
            	fareCalculatorService.recurringUser(ticket);
            }
            if(ticketDAO.updateTicket(ticket)) {
                ParkingSpot parkingSpot = ticket.getParkingSpot();
                parkingSpot.setAvailable(true); //setup after ticket's update
                parkingSpotDAO.updateParking(parkingSpot);
                /* !!! WARNING What to do if it returns false ? In DAO : logger.error("Error updating parking info",ex)
                 * So not persisted but ticket persisted ...*/
 
                viewer.println("Please pay the parking fare:" + ticket.getPrice());
                viewer.println("Recorded out-time for vehicle number:" + ticket.getVehicleRegNumber() + " is:" + outTime);
            } else {
            	viewer.println("Unable to update ticket information. Error occurred");
            }
        } catch(Exception e) {
        	viewer.println("Unable to process exiting vehicle");
        	logger.error("Unable to process exiting vehicle",e);
        }
    }
    
    /**
     * Test if user is a recurring one (parks more 10 times last month) 
     * @param ticket
     * @return : boolean
     */
    private boolean isRecurringUser(Ticket ticket) {
    	try {
    		return Optional.ofNullable(ticketDAO.isRecurringUserTicket(ticket)).orElseThrow(() -> new NullPointerException());
    		/*(NullPointerException::new) is same lambda notation
    		 *in DAO : error logged */
    	} catch(NullPointerException e) {
    		viewer.println("Unable to process loyalty. Error occurred");
    		return false;
    	}
	}

	/**
      * To Inject Mock
      * @param fareCalculatorService : mock
      */
     public void setFareCalculatorService(FareCalculatorService fareCalculatorService) {
 		this.fareCalculatorService = fareCalculatorService;
 	}

}
