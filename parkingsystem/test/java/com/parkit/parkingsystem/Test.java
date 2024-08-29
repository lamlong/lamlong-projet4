package com.parkit.parkingsystem;

public class Test {

	public String messageBienvenue () {
		return "Au revoir";
	}
	
	// On va appeler notre fonction messageBienvenue.
	// On va récupérer ce qu'elle renvoit : "Au revoir".
	// On sait qu'on attend "Bienvenue".
	// => On compare l'attendu ("Bienvenue") par rapport à ce qu'on a réellement ("Au revoir").
	// => Pas bon, le test échoue, on a détectée un bug.
}
