package com.parkit.parkingsystem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.service.InteractiveShell;

/**
 * parking system's entry point
 * @author Olivier MOREL
 *
 */
public class App {
    private static final Logger logger = LogManager.getLogger("App");
    private static InteractiveShell interactiveShellInstance;
    
	/**
     * Main method to launch the app.
     * Gets the only one instance of main controller InteractiveShell in service package
     * Then can run instantiated methode loadInterface()  
     * @param args not used in this method
     */
    
    public static void main(String[] args){
    	logger.info("Initializing Parking System");
        interactiveShellInstance = InteractiveShell.getInstance();
        switch (args.length) {
        	case 1 : {
        		if(args[0].equals("setDBConfig")) {
        			interactiveShellInstance.loadDBConfigInterface();
        			break;
        		}
        	}
        	default : {
        		interactiveShellInstance.loadInterface();
        	}	
        }
    }
}
