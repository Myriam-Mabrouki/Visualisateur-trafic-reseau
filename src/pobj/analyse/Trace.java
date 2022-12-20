package pobj.analyse;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/**
 * Classe réalisant la lecture d'une trame
 * @author Floria LIM et Myriam MABROUKI
 * 
 */


public class Trace {
	
	//On conserve dans des attributs statiques les listes de trames et de protocoles
	private static List<Trame> liste_trame;
	private int nb_trames = 0;
	private static List<String> liste_protocole;
	
	public Trace(FileInputStream file) {
		
		//On initialise les listes de trames et de protocoles
		liste_trame = new ArrayList<Trame>();
		liste_protocole = new ArrayList<>();
		
		//On réinitialise les attributs statiques de TCP (les autres classes de protocoles ne comportent pas d'attributs statiques et nécessite donc pas cette opération)
		TCP.reset_TCP();
		
		//Fichier permettant la lecture de la trace
		Scanner scanner = new Scanner(file);
		
		//On lit chaque ligne de notre fichier
		while(scanner.hasNextLine()) {
			String ligne = scanner.nextLine();
			if (ligne.isEmpty() || ligne.trim().isEmpty()) continue; // si la ligne est vide, on passe à la ligne suivante
			
			//On stocke dans un tableau les éléments séparés par trois espaces
			String tab[] = ligne.split("   ");
			
			//Si l'offset vaut 0000 il s'agit d'une nouvelle trame et on l'ajoute à notre liste
			if (tab[0].equals("0000")) { 
				liste_trame.add(new Trame(++nb_trames));
			}
			
			liste_trame.get(nb_trames-1).add_ligne_octets(tab[1]);
		}
		//Fermeture du fichier
		scanner.close();
	}

	/**
	 * Renvoie la liste de trames
	 * @return liste de de trames
	 */
	public static List<Trame> getListe_trame() {
		return liste_trame;
	}
	
	/**
	 * Renvoie la liste de protocoles
	 * @return liste de de protocoles
	 */
	public static List<String> getListeProtocole() {
		return liste_protocole;
	}
	
	/** 
	 * Ajoute un nom de protocole à la liste de protocoles
	 * @param p, la chaîne de caractères du nom du protocole
	 */
	public static void addProtocole(String p) {
		if (!liste_protocole.contains(p))
			liste_protocole.add(p);
	}
}
