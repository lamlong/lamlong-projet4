package com.parkit.parkingsystem.util;

import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.view.Viewer;
import com.parkit.parkingsystem.view.ViewerImpl;
/**
 * Read from keyboard and return valid choice
 * @author Olivier MOREL
 *
 */
public class InputReaderUtil {

    private Scanner scan = new Scanner(System.in);

	private static final Logger logger = LogManager.getLogger("InputReaderUtil");
    private final Viewer viewer = new ViewerImpl(); // Viewer instance
    
    /**
     * Read choice from menu
     * @return choice number (int)
     */
    public int readSelection() {
        try {
            return Integer.parseInt(scan.nextLine()); // delete /n in the buffer better than nextInt() !
        } catch(Exception e) {
            logger.error("Error while reading user input from Shell", e);
            viewer.println("Error reading input. Please enter valid number for proceeding further");
            return -1;
        }
    }

    /**
     * Tries to read from input vehicle's registration number 
     * @return vehicule's registration number as a String
     * @throws Exception : IllegalArgumentException if null or only blank space or invalid String input
     */
    public String readVehicleRegistrationNumber() throws Exception {
        try {
            String vehicleRegNumber = scan.nextLine(); //can throw NoSuchElementException or IllegalStateException 
            if(vehicleRegNumber == null || vehicleRegNumber.trim().length()==0) {
                throw new IllegalArgumentException("Invalid input provided"); //Will be caught see catch
            }
            return vehicleRegNumber;
        } catch(Exception e) {
            logger.error("Error while reading user input from Shell", e);
            viewer.println("Error reading input. Please enter a valid string for vehicle registration number");
            throw e;
        }
    }
}
