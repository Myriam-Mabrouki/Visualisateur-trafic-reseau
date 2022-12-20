package pobj.analyse;

import java.util.ArrayList;
import java.util.List;

public class TCP {
	
	private Trame t;
	private String nom_protocole = "TCP";

	private static boolean is_machine1 = true;
	private static boolean is_machine2 = false;
	private static TCP client = null;
	private static TCP serveur = null;
	
	private int sourcePort = 0;
	private int destinationPort = 0;
	private long sequenceNumber = 0;
	private long acknowledgmentNumber = 0;
	private int dataOffset = 0;
	private int option_length_total = 0;
	private boolean urg = false;
	private boolean ack = false;
	private boolean psh = false;
	private boolean rst = false;
	private boolean syn = false;
	private boolean fin = false;
	private int window = 0;
	private int len = 0;
	
	//Options
	private List<String> options = new ArrayList<>();
	private int window_scale = 1;
	private int mss = 1514; // longueur max d'un segment
	private boolean is_dup = false;
	
	private HTTP http;
	
	public TCP(Trame t) {
		this.t = t;
		http = null;
	}

	/**
	 * Fonction qui lit et initialise les entêtes de TCP 
	 */
	public void lire_tcp() {
		
		//Port source
		sourcePort = Integer.parseInt(t.get_nb_octets(2), 16);
		
		//Port destination
		destinationPort = Integer.parseInt(t.get_nb_octets(2), 16);
		
		//Sequence Number
		sequenceNumber= Long.parseLong(t.get_nb_octets(4), 16);
		
		//Acknowledgment Number
		acknowledgmentNumber = Long.parseLong(t.get_nb_octets(4), 16);
		
		//Data Offset
		dataOffset = Integer.parseInt(""+t.get_nb_octets(1).charAt(0), 16);
		
		//Reserved [skipped], URG ACK PSH RST SYN FIN
		String tmp = t.get_nb_octets(1);
		tmp = Integer.toBinaryString(Integer.parseInt(tmp, 16));
		while (tmp.length() < 8)
			tmp = "0" + tmp;
		
		urg = isSet(tmp.charAt(2));
		ack = isSet(tmp.charAt(3));
		psh = isSet(tmp.charAt(4));
		rst = isSet(tmp.charAt(5));
		syn = isSet(tmp.charAt(6));
		fin = isSet(tmp.charAt(7));
		
		//Reset dans le cas d'une nouvelle transmission TCP
		if (syn && is_machine2 == false) {
			is_machine1 = true;
			is_machine2 = false;
			client = null;
			serveur = null;
		}
		
		//Window
		if (is_machine1 || is_machine2)
			window = Integer.parseInt(t.get_nb_octets(2), 16);
		else if (client.sourcePort == sourcePort)
			window = Integer.parseInt(t.get_nb_octets(2), 16) * client.window_scale;
		else
			window = Integer.parseInt(t.get_nb_octets(2), 16) * serveur.window_scale;
		
		//Checksum + Urgent pointer [skipped]
		t.get_nb_octets(4);
		
		String option_type;
		int option_length;
		String value;
		
		//Options
		int i = dataOffset * 4 - 20;
		option_length_total = i;
		
		//On parcourt chaque option
		while(i > 0) {
			
			//On lit son type
			option_type = t.get_nb_octets(1);
			i--;
			//On traite le cas des options de longueur 1
			if (option_type.equals("01") || option_type.equals("00") || option_type.equals("0b") || option_type.equals("0c")|| option_type.equals("0c")) {
				//Comme la longueur ne peut être affichée, on passe à l'option suivante
				continue;
			}
			//On récupère la taille de l'option
			option_length = Integer.parseInt(t.get_nb_octets(1), 16);
			value = t.get_nb_octets(option_length - 2);
			i = i - option_length + 1;
			
			//Option Maximum Segment Size
			if (option_type.equals("02")) {
				options.add("MSS="+Integer.parseInt(value, 16));
				if (is_machine1 || is_machine2)
					mss = Integer.parseInt(value, 16);
			}
			
			//Option Sack Permitted
			else if (option_type.equals("04")) {
				options.add("SACK_PERM=1");
			}
			
			//Option TimeStamp
			else if (option_type.equals("08")) {
				String tsval = value.substring(0, 8);
				String tsecr = value.substring(8, 16);
				options.add("TSval="+Integer.parseInt(tsval, 16)+" TSecr="+Integer.parseInt(tsecr, 16));
			}
			
			//Option Sack
			else if (option_type.equals("05")) {
				is_dup = isDuplicate();
				long sle = Long.parseLong(value.substring(0, 8), 16);
				long sre = Long.parseLong(value.substring(8, 16), 16);
				TCP prec = Trace.getListe_trame().get(t.getNum() - 2).getIPv4().getTCP();
				if (prec.sequenceNumber == client.sequenceNumber) {
					sle -= client.sequenceNumber;
					sre -= client.sequenceNumber;
				}
					
				else 
					sle -= serveur.sequenceNumber;
					sre -= serveur.sequenceNumber;
				options.add("SLE="+sle+" SRE="+sre);
			}
			
			//Option Window Scale
			else if (option_type.equals("03")) {
				options.add("WS="+(int)Math.pow(2, Integer.parseInt(value, 16)));
				if (is_machine1 || is_machine2)
					window_scale = (int) Math.pow(2, Integer.parseInt(value, 16));
			}
			
			//On passe les options suivantes
			else{
				for (int k = 2; k < option_length; k++) {
					t.get_nb_octets(1);
					i--;
				}
			}
			
		}
		
		//Longueur des données
		IPv4 ipv4 = t.getIPv4();
		len = ipv4.getTotal_length() - (ipv4.getIp_header_length() * 4) - (dataOffset * 4);
		
		if (sourcePort == 80 || destinationPort == 80 ) {
			http = new HTTP(t);
			http.lire_protocole_http();
		}
		
		if (is_machine1) {
			client = this;
			is_machine1 = false;
			is_machine2 = true;
		} else if (is_machine2) {
			serveur = this;
			is_machine2 = false;
		}
		
	}
	
	/**
	 * Renvoie vrai si i est différent de 0, faux sinon
	 * Précondition: i = 1 ou i = 0
	 * @param i, un caractère
	 */
	public boolean isSet(char i) {
		return !(i == '0');
	}

	/**
	 * Renvoie le commentaire associé à la trame lue
	 * @return une chaîne de caractères représentant le commentaire
	 */
	public String getCommentary() {
		String s = "";
		boolean is_out_of_order = this.isOutOfOrder();
		if (http != null && psh && !is_out_of_order) {
			s += http.getCommentary();
			if (s != "") {
				Trace.addProtocole("HTTP");
				nom_protocole = "HTTP";
				return s;
			}
		}

		if (is_out_of_order) {
			s += "[TCP Out-Of-Order] ";
		}
		if (is_dup && numDup() != -1)
			s += "[TCP Dup ACK "+numDup()+"#1] ";
		s+= sourcePort + " -> " + destinationPort + " [";
		String tmp = "";
		if (syn)
			tmp += "SYN, ";
		if (fin)
			tmp += "FIN, ";
		if (psh)
			tmp += "PSH, ";
		if (urg)
			tmp += "URG, ";
		if (rst)
			tmp += "RST, ";
		if (ack)
			tmp += "ACK, ";
		if (tmp != "")
			tmp = tmp.substring(0, tmp.length() - 2);
		
		if (client.sourcePort == sourcePort)
			s += tmp + "] Seq=" + (sequenceNumber - client.sequenceNumber);
		else 
			s += tmp + "] Seq=" + (sequenceNumber - serveur.sequenceNumber);
		
		if (client.sourcePort == sourcePort && serveur != null)
			s += " Ack=" + (acknowledgmentNumber - serveur.sequenceNumber);
		else if (serveur != null)
			s += " Ack=" + (acknowledgmentNumber - client.sequenceNumber);

		s += " Win=" + window + " Len=" + len;
		for (String option : options) {
			s += " " + option;
		}
		if ( (client.sourcePort == sourcePort && (len+option_length_total) == client.mss) || (serveur != null && serveur.sourcePort == sourcePort && (len+option_length_total) == serveur.mss) )
			s += " [TCP segment of a reassembled PDU]";
		return s;
		
	}
	
	/**
	 * Renvoie l'affichage de la trame dans le visualisateur
	 * @return une chaîne de caractères représentant l'affichage
	 */
	public String affichage() {
		
		//Le premier flux (établissement de connexion) correspond toujours à une requête du client vers le serveur, la flèche va donc toujours de gauche à droite
		if (t.getIPv4().getTCP().syn && !t.getIPv4().getTCP().ack)
			return Affichage.fleche(sourcePort, destinationPort, t, true);
		//Le deuxieme flux (établissement de connexion) correspond toujours à une requête du serveur vers le client, la flèche va donc toujours de droite à gauche
		else if (t.getIPv4().getTCP().syn)
			return Affichage.fleche(destinationPort, sourcePort, t, false);
		//Sinon on détermine le sens de la flèche
		boolean x = client.sourcePort == sourcePort;
		return Affichage.fleche(client.sourcePort, serveur.sourcePort, t, x);
	}

	/**
	 * Renvoie le port source
	 * @return le port source
	 */
	public int getSourcePort() {
		return sourcePort;
	}

	
	/**
	 * Renvoie le port destination
	 * @return le port destination
	 */
	public int getDestinationPort() {
		return destinationPort;
	}

	/**
	 * Renvoie le nom du protocole le plus encapsulé lorsqu'il s'agit d'un protocole traité
	 * @return une chaîne de caractères représentant le nom du protocole le plus encapsulé lorsqu'il s'agit d'un protocole traité
	 */
	public String nomProtocole() {
		return nom_protocole;
	}

	/**
	 * Renvoie le protocole qu'encapsule TCP
	 * @return une instance de HTTP si le protocole encapsulé est une instance de HTTP, null sinon
	 */
	public HTTP getHttp() {
		return http;
	}

	/**
	 * Renvoie le flag Push de la trame TCP
	 * @return true si psh est à 1, false sinon;
	 */
	public boolean isPsh() {
		return psh;
	}
	
	/**
	 * Fonction qui indique si une trame est dupliquée ou non
	 * @return true si la trame est dupliquée, false sinon
	 */
	public boolean isDuplicate() {
		if (syn) return false;
		int i = t.getNum() - 2;	
		TCP courant = Trace.getListe_trame().get(i).getIPv4().getTCP();
		
		//On parcourt tant qu'on n'arrive pas au début de la transmission TCP
		while (!courant.syn) {
			courant = Trace.getListe_trame().get(i).getIPv4().getTCP();
			
			
			if (!psh && courant.sourcePort == this.sourcePort && courant.acknowledgmentNumber == acknowledgmentNumber) {
				return true;
			}
			else if (courant.destinationPort == this.destinationPort && courant.acknowledgmentNumber < acknowledgmentNumber) {
				return false;			
			}
			i--;
		}
		return false;
	}

	
	/**
	 * Fonction qui indique si une trame est out of order
	 * @return true si la trame est out of order, false sinon
	 */
	public boolean isOutOfOrder() {
        if (syn) return false;
        int i = t.getNum() - 2;  
        TCP courant = Trace.getListe_trame().get(i).getIPv4().getTCP();
        if (courant == null || courant.syn) return false;
        while (courant == null || ( !courant.syn && (courant.sourcePort != sourcePort || (courant.sourcePort == sourcePort && courant.isOutOfOrder())))) {
            courant = Trace.getListe_trame().get(i).getIPv4().getTCP();
            i--;
        }
        return courant.sequenceNumber > sequenceNumber;
    }
	
	/**
	 * Fonction qui renvoie le numéro de la trame depuis laquelle la trame courante s'est dupliquée s'il s'agit d'une duplication
	 * @return un numéro de trame
	 */
	public int numDup() {
		
		//On démarre à la trame précédente
        int i = t.getNum() - 2;
        TCP courant = Trace.getListe_trame().get(i).getIPv4().getTCP();
        //La trame trame_dupliquee commence à la trace courante
        TCP trame_dupliquee = Trace.getListe_trame().get(i+1).getIPv4().getTCP();
        
        //On continue de parcourir tant que la trame courante est null ou que son numéro de port ne correspond pas à la trame dupliquée ou que son numéro de port et son numéro d'acquittement corresponde à la trame dupliquée
        while (i >= 1 && (courant == null || !courant.syn && (courant.sourcePort != sourcePort || (courant.acknowledgmentNumber == acknowledgmentNumber && courant.sourcePort == sourcePort)))) {
            courant = Trace.getListe_trame().get(i).getIPv4().getTCP();
            
            //Si la trame courante possède le même numéro de port et le même numéro d'acquittement alors il s'agit potentiellement de la source de la duplication
            if (courant.acknowledgmentNumber == acknowledgmentNumber && courant.sourcePort == sourcePort)
            	trame_dupliquee = courant;
            i--;
        }
        return trame_dupliquee.t.getNum();
    }

	/**
	 * Fonction qui réinitialise les attributs statiques de TCP
	 */
	public static void reset_TCP() {
		TCP.is_machine1 = true;
		TCP.is_machine2 = false;
		TCP.client = null;
		TCP.serveur = null;
	}
	
}
