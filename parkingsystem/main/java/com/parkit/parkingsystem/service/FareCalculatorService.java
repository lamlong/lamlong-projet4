package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {
	 public void calculateFare(Ticket ticket){
		 this.calculateFare(ticket, false);
	 }

    public void calculateFare(Ticket ticket, boolean estUtilisateurRecurrent){
    if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        double inHour = ticket.getInTime().getTime();
        // inHour : à quelle heure est rentrée la voiture.
       
        double outHour = ticket.getOutTime().getTime();
        // outHour : à quelle heure est sortie la voiture.
        
        double duration = (outHour - inHour) / (60*60*1000); // Conversion de millisecondes => heure.
        // durée est en heure.
      
        
        if (duration <= 0.5) {
        	ticket.setPrice(0.0);
        } else  {
        	// Regarder les différents types Java (long / double / int, principalement)

            switch (ticket.getParkingSpot().getParkingType()){
                case CAR: {
                    ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                    break;
                }
                case BIKE: {
                    ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                    break;
                }
                default: throw new IllegalArgumentException("Unkown Parking Type");
            }
        }
        
        // Cas de l'utilisateur récurrent.
        if (estUtilisateurRecurrent) {
        	 ticket.setPrice(ticket.getPrice() * 0.95); // * 0.95 permet d'avoir 5% de réduction.
        }
      
    }
}