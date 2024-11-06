# Author: oliviermorel.oc1@gmail.com
# language: fr

@freeThirtyMinutesOrLess
Fonctionnalité:  Gratuité du tarif pour 30 minutes ou moins;
	En tant qu'utilisateur, je souhaite la gratuité du tarif pour une occupation inférieure ou égale à 30 minutes afin de ne pas congestionner le parking

	Plan du scénario: Gratuité pour 30 minutes ou moins;
		Étant donné utilisateur avec l'immatriculation <plaque> est garé depuis <durée> minutes;
		Quand il sort;
		Alors le ticket persisté a une plaque <plaque>, un tarif à <tarif> et la place persistée a une disponibilité <dispo>;
	
	Exemples:
		|plaque |durée|tarif|dispo |
		|"CAR30"|30   |0,00 |"true"|
		|"CAR20"|20   |0,00 |"true"|
		|"CAR10"|10   |0,00 |"true"|