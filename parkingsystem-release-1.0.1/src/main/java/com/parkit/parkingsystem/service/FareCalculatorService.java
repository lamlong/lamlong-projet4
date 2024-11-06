package com.parkit.parkingsystem.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

/**
 * This service calculates the fare taking into account the vehicle's type
 * @author Olivier MOREL
 *
 */
public class FareCalculatorService {

	/**
	 * To calculate Fare discount
	 */
	private DiscountFareService discountFareService = new DiscountFareService();
	
	/**
     * From a given ticket calculates the fare taking into account the vehicle's type
     * @param ticket : model
     * @throws IllegalArgumentException if type is unknown
     */
	public void calculateFare(Ticket ticket) throws IllegalArgumentException{ // Throw is optional because RuntimeException but better readability
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inHour = ticket.getInTime().getTime(); // Returns the number of milliseconds (long) since
        long outHour = ticket.getOutTime().getTime(); // January 1, 1970, 00:00:00 GMT represented by this Date object

        double duration = (outHour - inHour) / (1000*3600d); // from milliseconds to decimal hours, d for double casting
        try {
	        switch(ticket.getParkingSpot().getParkingType()) { //When null show up in switch statement, Java will throw NullPointerException
	            case CAR: {
	                ticket.setPrice(BigDecimal.valueOf(duration * Fare.CAR_RATE_PER_HOUR).setScale(2, RoundingMode.HALF_UP).doubleValue());
	                // Set price with 2 decimals rounded towards "nearest neighbor" unless both neighbors are equidistant, in which case round up
	                discountFareService.fareForThirtyOrLessMinutes(ticket);
	                break;
	            }
	            case BIKE: {
	                ticket.setPrice(BigDecimal.valueOf(duration * Fare.BIKE_RATE_PER_HOUR).setScale(2, RoundingMode.HALF_UP).doubleValue());
	                // Set price with 2 decimals rounded towards "nearest neighbor" unless both neighbors are equidistant, in which case round up
	                discountFareService.fareForThirtyOrLessMinutes(ticket);
	                break;
	            }
	            default: { //Braces are optional but better readability 
	            	throw new IllegalArgumentException("Unkown Parking Type"); //Throw a java.lang.RuntimeException
	            }
	        }
        } catch(NullPointerException e) {
        	throw new IllegalArgumentException("Type is null");
        }
    }

	/**
	 * Fare for Recurring User call service method for five percent discount
	 * @param ticket : pointeur to object, not modified only the object so no need to return
	 */
	public void recurringUser(Ticket ticket) {
		discountFareService.fivePourcentsOff(ticket);
	}
}