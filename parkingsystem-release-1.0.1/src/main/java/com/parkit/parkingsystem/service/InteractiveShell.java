package com.parkit.parkingsystem.service;

import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.dao.DBConfigIO;
import com.parkit.parkingsystem.dao.LoadDBConfigFromFile;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.dao.WriteDBConfigToFile;
import com.parkit.parkingsystem.util.InputReaderUtil;
import com.parkit.parkingsystem.view.Viewer;
import com.parkit.parkingsystem.view.ViewerImpl;

/**
 * Main controller
 * 
 * This is a singleton, only one instance possible.
 * 
 * @author Olivier MOREL
 *
 */
public final class InteractiveShell {

	private static InteractiveShell interactiveShellInstance; //interactiveShellInstance is private to exercise access control

	private static final Logger logger = LogManager.getLogger("InteractiveShell");
	private final Viewer viewer = new ViewerImpl(); // Viewer instance

	private InteractiveShell() {
	} //Constructor is private to exercise access control

	/**
	 * Getter Static to get it without instantiating it
	 * 
	 * @return the only one InteractiveShell()'s instance
	 */
	public static InteractiveShell getInstance() {
		if (interactiveShellInstance == null) {
			interactiveShellInstance = new InteractiveShell();
		}
		return interactiveShellInstance;
	}
	
	/**
	 * Main controller
	 */
	public void loadInterface() {
		logger.info("App initialized!!!");
		viewer.println("Welcome to Parking System!");

		boolean continueApp = true;
		InputReaderUtil inputReaderUtil = new InputReaderUtil(); //to read keyboard input and give an expected result
		ParkingSpotDAO parkingSpotDAO = new ParkingSpotDAO(); //for CRUD : Create, Read, Update and Delete on table parking
		TicketDAO ticketDAO = new TicketDAO(); //for CRUD : Create, Read, Update and Delete on table ticket
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO, viewer);

		while(continueApp) {
			loadMenu();
			int option = inputReaderUtil.readSelection();
			switch(option) {
				case 1: {
					parkingService.processIncomingVehicle();
					break;
				}
				case 2: {
					parkingService.processExitingVehicle();
					break;
				}
				case 3: {
					viewer.println("Exiting from the system !");
					continueApp = false;
					break;
				}
				default: {
					viewer.println("Unsupported option. Please enter a number corresponding to the provided menu");
				}
			}
		}
	}

	private void loadMenu() {
		viewer.println("Please select an option. Simply enter the number to choose an action");
		viewer.println("1 New Vehicle Entering - Allocate Parking Space");
		viewer.println("2 Vehicle Exiting - Generate Ticket Price");
		viewer.println("3 Shutdown System");
	}

	public void loadDBConfigInterface() {

		logger.info("DB Configuration initialized!!!");
		viewer.println("DB Configuration");

		boolean continueApp = true;
		InputReaderUtil inputReaderUtil = new InputReaderUtil(); //to read keyboard input and give an expected result
		Properties dbProperties;
		DBConfigIO fileDBConfigIO;

		while(continueApp) {
			loadMenuDBConfig();
			int option = inputReaderUtil.readSelection();
			switch(option) {
				case 1: {
					fileDBConfigIO = new WriteDBConfigToFile();
					dbProperties = new Properties();
					
					viewer.println("Enter login for db");
					try {
						dbProperties.setProperty("user", inputReaderUtil.readVehicleRegistrationNumber());
					} catch (Exception e) {
						logger.error(e); //IllegalArgumentException("Invalid input provided")
					}
					
					viewer.println("Enter root password");					
					try {
						dbProperties.setProperty("password", inputReaderUtil.readVehicleRegistrationNumber());
					} catch(Exception e) {
						logger.error(e); //IllegalArgumentException("Invalid input provided")
					}
					
					fileDBConfigIO.setDBProperties(dbProperties);
					fileDBConfigIO = null;
					dbProperties = null;
					break;
				}
				case 2: {
					fileDBConfigIO = new LoadDBConfigFromFile();
					dbProperties = fileDBConfigIO.getDBProperties();

					dbProperties.forEach((k,v) -> viewer.println(k+" "+v));

					fileDBConfigIO = null;
					dbProperties = null;
					break;
				}
				case 3: {
					viewer.println("Exiting from the system !");
					continueApp = false;
					break;
				}
				default: {
					viewer.println("Unsupported option. Please enter a number corresponding to the provided menu");
				}
			}
		}
	}
	
	private void loadMenuDBConfig() {
		viewer.println("Please select an option. Simply enter the number to choose an action");
		viewer.println("1 Set DB Configuration");
		viewer.println("2 Show DB Configuration");
		viewer.println("3 Shutdown System");
	}
}
