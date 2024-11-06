# Author: oliviermorel.oc1@gmail.com
# language: fr

@fivePercentOffForRecurringUsers
Fonctionnalité: réduction de 5 % pour les utilisateurs récurrents;
  En tant qu'utilisateur récurrent, je souhaite bénéficier d'une réduction de 5 %

  Scénario: 5 % pour l'utilisateur qui a utilisé le parking plus de 10 fois le mois précédent
    Étant donné utilisateur avec l'immatriculation FID qui s’est garé plus de 10 fois dans le parking le mois précédent;
    Quand il sort après une heure de parking;
    Alors le ticket persisté a une plaque FID, un tarif réduit de 5 % et la place persistée a une disponibilité true;