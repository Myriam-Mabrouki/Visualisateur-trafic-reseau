package pobj.analyse;

public class IPv4 {
	private int ip_header_length;
	private int total_length;
	
	private String ip_source;
	private String ip_destination;
	private String type_protocol_ip;
	private TCP tcp;

	private Trame trame;
	
	public IPv4(Trame t) {
		trame = t;
		ip_source = "";
		ip_destination = "";
		tcp = null;
	}
	
	/**
	 * Fonction qui lit et initialise les entêtes de IPv4 
	 */
	public void lire_ipv4() {
		
		//IHL
		String tmp = trame.get_nb_octets(1);
		ip_header_length = Integer.parseInt(tmp, 16)%16;
		
		//TOS [skipped]
		trame.get_nb_octets(1);
		
		//Total Length
		tmp = trame.get_nb_octets(2);
		total_length = Integer.parseInt(tmp, 16);
		
		//Identification + Flags + Fragment offset + TTL [skipped]
		trame.get_nb_octets(5);
			
		//Protocol IP Type
		type_protocol_ip = trame.get_nb_octets(1);
			
		//Header checksum [skipped]
		trame.get_nb_octets(2);
			
		//Source IP address
		for (int i=0; i<3; i++)
			ip_source = getIp_source() + Integer.parseInt(trame.get_nb_octets(1), 16)+".";
		ip_source = getIp_source() + Integer.parseInt(trame.get_nb_octets(1), 16);
			
		//Destination IP address
		for (int i=0; i<3; i++)
			ip_destination = getIp_destination() + Integer.parseInt(trame.get_nb_octets(1), 16)+".";
		ip_destination = getIp_destination() + Integer.parseInt(trame.get_nb_octets(1), 16);
			
		//Options [skipped]
		int taille_options = ip_header_length-5; // en nombre de lignes de 4 octets
		for (int i=0; i<taille_options; i++) {
			trame.get_nb_octets(4);
		}
			
		//Lecture TCP
		if (type_protocol_ip.equals("06")) { // TCP (6) -> Ox06
			tcp = new TCP(trame);
			tcp.lire_tcp();
			Trace.addProtocole("TCP");
		}	
		else if (type_protocol_ip.equals("01")) {
			Trace.addProtocole("ICMP");
		}
		else if (type_protocol_ip.equals("11")) {
			Trace.addProtocole("UDP");
		}
		
	}
	
	/**
	 * Renvoie le nom du protocole le plus encapsulé lorsqu'il s'agit d'un protocole traité
	 * @return une chaîne de caractères représentant le nom du protocole le plus encapsulé lorsqu'il s'agit d'un protocole traité
	 */
	public String nomProtocole() {
		if (tcp != null) {
			return tcp.nomProtocole();
		}
		else if (type_protocol_ip.equals("01")) {
			return "ICMP";
		}
		else if (type_protocol_ip.equals("11")) {
			return "UDP";
		}
		else {
			return "Protocole inconnu";
		}
	}
	
	/**
	 * Renvoie le protocole qu'encapsule le protocole IP
	 * @return une instance de TCP si le protocole encapsulé est une instance de TCP, null sinon
	 */
	public TCP getTCP() {
		return tcp;
	}
	
	/**
	 * Renvoie le commentaire associé à la trame lue
	 * @return une chaîne de caractères représentant le commentaire
	 */
	public String getCommentary() {
		if (tcp != null)
			return tcp.getCommentary();
		return "";
	}
	
	/**
	 * Renvoie la valeur de l'entête IP Header Length
	 * @return un entier représentant la valeur de l'entête IP Header Length
	 */
	public int getIp_header_length() {
		return ip_header_length;
	}
	
	/**
	 * Renvoie la valeur de l'entête Total Length
	 * @return un entier représentant la valeur de l'entête Total Length
	 */
	public int getTotal_length() {
		return total_length;
	}

	/**
	 * Renvoie l'adresse IP source
	 * @return une chaîne de caractères représentant l'adresse IP source
	 */
	public String getIp_source() {
		return ip_source;
	}

	/**
	 * Renvoie l'adresse IP destination
	 * @return une chaîne de caractères représentant l'adresse IP destination
	 */
	public String getIp_destination() {
		return ip_destination;
	}
	
	/**
	 * Renvoie l'affichage de la trame dans le visualisateur
	 * @return une chaîne de caractères représentant l'affichage
	 */
	public String affichage() {
		if (tcp != null)
			return tcp.affichage();
		else 
			return "\t\t\t" + nomProtocole() + ": Protocole non supporté"; 
	}
}
