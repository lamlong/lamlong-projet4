package com.parkit.parkingsystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.DiscountFareService;

/**
 * Tests DiscountFareService
 * Declares class under test and a model attribute 
 * @author Olivier MOREL
 *
 */
public class DiscountFareServiceTest {
	private DiscountFareService discountFareService; //Class Under Test
	private Ticket ticket; //Model = only data so don't mock
	
	/**
	 * Before each test initialize attributes
	 */
	@BeforeEach
    public void setUpPerTest() {
		discountFareService = new DiscountFareService();
    	ticket = new Ticket();
    }
	
	/**
	 * After each test nullify attributes
	 */
    @AfterEach
    public void undefPerTest() {
		discountFareService = null;
    	ticket = null;
    }
    
    /**
     * Nominal tests for 30 minutes or less
     * @param min : minutes parked
     */
    @ParameterizedTest(name = "{0} minute in park should cost 0.00")
    @ValueSource(ints = {30, 20, 10})
    @DisplayName("Nominal cases")
    public void fareForThirtyOrLessMinutesTestShouldSetPriceToZero(int min){
    	//GIVEN
    	Date inTime = new Date(System.currentTimeMillis() - (min * 60 * 1000));
    	Date outTime = new Date();
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        double duration = (outTime.getTime() - inTime.getTime()) / (1000*3600d); // from milliseconds to decimal hours, d for double casting
        ticket.setPrice(BigDecimal.valueOf(duration * Fare.CAR_RATE_PER_HOUR).setScale(2, RoundingMode.HALF_UP).doubleValue());
        // Set price with 2 decimals rounded towards "nearest neighbor" unless both neighbors are equidistant, in which case round up
        
    	//WHEN
        discountFareService.fareForThirtyOrLessMinutes(ticket); //ticket is a pointer to the object. Only object'll be modified
        
    	//THEN
        assertThat(ticket.getPrice()).isZero();
    }
    
    /**
     * Test  five percent discount on ticket price
     */
    @Test
    @DisplayName("Test 5% off for reccurring users")
    public void fivePourcentsOffTestShouldReducePriceByFivePercent() {
    	//GIVEN
    	ticket.setPrice(1.50);
 
    	//WHEN
    	discountFareService.fivePourcentsOff(ticket);

    	//THEN
    	assertThat(ticket.getPrice()).isCloseTo(BigDecimal.valueOf(1.50*(1-5/100d)).setScale(2, RoundingMode.HALF_UP).doubleValue(), within(0.01));
    }

}
