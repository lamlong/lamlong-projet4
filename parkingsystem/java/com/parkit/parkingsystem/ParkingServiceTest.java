package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    private void setUpPerTest() {
        try {
            //when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            // when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            // when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

            // when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    // Test de la méthode qui permet d'enregistrer un véhicule sortant du parking.
    @Test
    public void processExitingVehicleTest(){
    	// On prépare notre jeux de données.
    	
    	// On a besoin d'une immatriculation d'un véhicule.
    	String immatriculation = "AA-123-AA";
    	
    	Ticket ticket = new Ticket();
    	ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
    	ticket.setParkingSpot(parkingSpot);
    	
    	 // On a besoin d'un ticket.
    	when(ticketDAO.getTicket(immatriculation)).thenReturn(ticket);
    	
    	when (ticketDAO.updateTicket(any())).thenReturn(true);
    	when (parkingSpotDAO.updateParking(parkingSpot)).thenReturn(true);
    	
    	 // Appel de la méthode testée.
        parkingService.processExitingVehicle();
        
        // On fait nos vérifications suite à l'appel de la méthode.
        assertTrue(!ticket.getParkingSpot().isAvailable());
       
    }
    
    // Test de la méthode qui permet d'enregistrer un véhicule entrant dans le parking.
    @Test
    public void testProcessIncomingVehicle() {
    	 // On prépare notre jeux de données.
    	
    	 // On a besoin de rien pour un appel 'classique'. 
    	 
    	 // On fait nos vérifications suite à l'appel de la méthode.
    	 assertDoesNotThrow(() -> {
    		 // Appel de la méthode testée.
    		 parkingService.processIncomingVehicle();
    	 });
    }
    
    // Test de la méthode qui permet de gérer la sortie d'un véhicule et on a problème de mise à jour en base.
    @Test
    public void processExitingVehicleTestUnableUpdate () {
    	 // On prépare notre jeux de données.
    	
     	
    	 // Appel de la méthode testée.
    	 parkingService.processExitingVehicle ();
    	 
    	 // On fait nos vérifications suite à l'appel de la méthode.
    	// A FAIRE : Quelles vérifications on doit faire ?
    }
    
    // Test de la méthode qui permet de chercher une place dans le parking.
    @Test 
    public void testGetNextParkingNumberIfAvailable () {
    	// On prépare notre jeux de données.
    	
    	// On bouchonne les données. Ici on force le retour de la saisie à 1 (type voiture).
    	when(inputReaderUtil.readSelection()).thenReturn(1);
    	
    	// On force le retour de la recherche de place de parking.
    	ParkingType parkingType = ParkingType.CAR;
    	when (parkingSpotDAO.getNextAvailableSlot(parkingType)).thenReturn(1);
    	
    	// Appel de la méthode testée.
    	ParkingSpot placeParking = parkingService.getNextParkingNumberIfAvailable ();
    	
    	// On fait nos vérifications suite à l'appel de la méthode.
    	
    	assertEquals(placeParking.getParkingType(), ParkingType.CAR);
    	assertEquals(placeParking.getId(), 1);
    	assertEquals(placeParking.isAvailable(), true);
    }
    
    // Test de la méthode qui permet de chercher une place dans le parking mais dans le cas où il n'y a pas de place disponible.
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound () {
    	// On prépare notre jeux de données.
    	
    	// On bouchonne les données. Ici on force le retour de la saisie à 1 (type voiture).
    	when(inputReaderUtil.readSelection()).thenReturn(1);
    	
    	// On force le retour de la recherche de place de parking.
    	ParkingType parkingType = ParkingType.CAR;
    	when (parkingSpotDAO.getNextAvailableSlot(parkingType)).thenReturn(0);
    	
    	// Appel de la méthode testée.
    	ParkingSpot placeParking = parkingService.getNextParkingNumberIfAvailable();
    	
    	// On fait nos vérifications suite à l'appel de la méthode.
    	
    	assertNull(placeParking);
    }
    
    // Test de la méthode qui permet de chercher une place dans le parking mais dans le cas où il n'y a pas de place disponible.
    // Cas supplémentaire : le type de véhicule saisi par l'utilisateur n'existe pas (autre chose qu'un ou deux).
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument () {
    	// On prépare notre jeux de données.
   	  
    	// On bouchonne les données. Ici on force le retour de la saisie à 3 (type inconnu).
    	when(inputReaderUtil.readSelection()).thenReturn(3);
    	
    	// Appel de la méthode testée.
    	ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable ();
    	
    	// On fait nos vérifications suite à l'appel de la méthode.
    	assertNull(parkingSpot);
    }

}
