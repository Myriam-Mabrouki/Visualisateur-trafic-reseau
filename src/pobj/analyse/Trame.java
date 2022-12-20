package pobj.analyse;


public class Trame {
	private String[] tab_octet;
	private int taille;
	private int cursor;
	private int num;
	
	private String type;
	private IPv4 ipv4;

	public Trame(int num) {
		
		//Initialisation avec des valeurs nulles
		tab_octet =  new String[1514]; // une trame Ethernet a au maximum 6+6+2+1500 = 1514 octets (sans preambule ni champ FCS)
		taille = 0;
		cursor = 0;
		this.num = num;

		type = "";
		ipv4 = null;
	}
	
	/**
	 * Fonction qui ajoute les octets d'une ligne dans un tableau d'octets
	 * @param ligne : ligne du fichier trace sans l'offset
	 */
	public void add_ligne_octets(String ligne) {
		String tab[] = ligne.split(" ");
		for (int i=0; i<tab.length; i++) {
			tab_octet[taille] = tab[i].toLowerCase();
			taille++;
		}
	}

	/**
	 * Fonction qui lit le tableau d'octets pour initialiser les différentes données de la trame
	 */
	public void lire_trame() {

		//MAC Destination address + MAC Source address [skipped]
		get_nb_octets(12);
		
		//Type
		type = get_nb_octets(2);
		if (type.equals("0800")) {
			ipv4 = new IPv4(this);
			ipv4.lire_ipv4();
		}
		if (type.equals("0806")) {
			Trace.addProtocole("ARP");
		}
		
	}
	
	/**
	 * Fonction qui renvoie la taille de la trame
	 */
	public int getTaille() {
		return taille;
	}
	
	/**
	 * Fonction qui permet d'avancer de nb octets dans la trame lue
	 * @param nb, le nombre d'octets qu'on souhaite lire et/ou avancer
	 * @return une chaîne de caractères de nb octets depuis la position du curseur
	 */
	public String get_nb_octets(int nb) {
		String s = "";
		for (int i=0; i<nb; i++) {
			s += tab_octet[cursor++];
		}
		return s;
	}
	
	/**
	 * Renvoie le nom du protocole le plus encapsulé lorsqu'il s'agit d'un protocole traité
	 * @return une chaîne de caractères représentant le nom du protocole le plus encapsulé lorsqu'il s'agit d'un protocole traité
	 */
	public String nomProtocole() {
		if (ipv4 != null) {
			return ipv4.nomProtocole();
		}
		else if (type.equals("0806")) {
			return "ARP";
		}
		else {
			return "Protocole inconnu";
		}
	}
	
	/**
	 * Renvoie le protocole qu'encapsule la trame Ethernet
	 * @return une instance d'IPv4 si le protocole encapsulé est une instance d'IPv4, null sinon
	 */
	public IPv4 getIPv4() {
		return ipv4;
	}

	/**
	 * Renvoie le commentaire associé à la trame lue
	 * @return une chaîne de caractères représentant le commentaire
	 */
	public String getCommentary() {
		if (ipv4 != null) 
			return ipv4.getCommentary(); //si le protocole est IPv4, on renvoie le commentaire d'IPv4
		else 
			return ""; //sinon on renvoie une chaîne vide
	}
	
	/**
	 * Renvoie le numéro de la trame courante
	 * @return un entier, le numéro de trame courante
	 */
	public int getNum() {
		return num;
	}

	/**
	 * Renvoie la position du curseur
	 * @return un entier, la position du curseur
	 */
	public int getCursor() {
		return cursor;
	}
	
	/**
	 * Renvoie l'affichage de la trame dans le visualisateur
	 * @return une chaîne de caractères représentant l'affichage
	 */
	public String affichage() {
		//Si le protocole est autre que IPv4, on ne le traite pas et on indique qu'il s'agit d'une trame avec un protocole non supporté
		if (ipv4 == null)
			return "\t\t\t" + nomProtocole() + ": Protocole non supporté";
		//Sinon on traite l'affichage avec IPv4
		return ipv4.affichage();
	}
}
