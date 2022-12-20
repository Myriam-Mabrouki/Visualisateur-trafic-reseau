package pobj.analyse;

/**
 * Classe où l'on retrouve les fonctions d'affichage
 * @author Floria LIM et Myriam MABROUKI
 * 
 */

public class Affichage {
	
	/**
	 * Renvoie une chaîne de caractères correspondant à la première ligne à afficher et contenant les adresses IP du client et du serveur
	 * @param T, une trame
	 */
	public static String firstLigne(Trame t) {
		return t.getIPv4().getIp_source() + "\t\t  "  + t.getIPv4().getIp_destination() + "\t Commentaire";
	}
	
	/**
	 * Renvoie une chaîne de caractères composée d'une flèche entre le port du client et le port du serveur, montrant le sens du flux, accompagné d'un commentaire sur celui-ci
	 * @param portClient, un entier correspondant au port du client
	 * @param portServeur, un entier correspondant au port du serveur
	 * @param T, une trame
	 * @param bool, un booléen indiquant le sens du flux
	 */
	public static String fleche(int portClient, int portServeur, Trame t, boolean bool) {
		String comment = t.getCommentary();
		if (bool)
			return portClient + " --------------------------------------------> " + portServeur + " \t " + t.nomProtocole() + ": " + comment; 
		return portClient + " <-------------------------------------------- " + portServeur + " \t " + t.nomProtocole() + ": " + comment;
	}

}
