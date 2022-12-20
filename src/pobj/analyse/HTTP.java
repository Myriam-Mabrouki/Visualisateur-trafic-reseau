package pobj.analyse;

public class HTTP {
	
	private Trame trame;
	private String s1 = ""; //Méthode si requête, Version si réponse
	private String s2 = ""; //URL si requête, Code statut si réponse
	private String s3 = ""; //Version si requête, Message si réponse
	private String content_type = ""; //entête Content-Type pour les réponses HTTP
	
	public HTTP(Trame trame) {
		this.trame = trame;
	}

	/**
	 * Fonction qui lit et initialise les champs de HTTP
	 */
	public void lire_protocole_http() {
		
		//Méthode ou Version
		String tmp = trame.get_nb_octets(1);
		while(trame.getCursor() < trame.getTaille() && ! tmp.equals("20")) {
			s1 += tmp;
			tmp = trame.get_nb_octets(1);
		}
		s1 = hexToAscii(s1);
		
		//URL ou Code statut
		tmp = trame.get_nb_octets(1);
		while(trame.getCursor() < trame.getTaille() && ! tmp.equals("20")) {
			s2 += tmp;
			tmp = trame.get_nb_octets(1);
		}
		s2 = hexToAscii(s2);
		
		// Version ou Message
		String tmp1 = trame.get_nb_octets(1);
		String tmp2 = trame.get_nb_octets(1);
		while(trame.getCursor() < trame.getTaille() && ! tmp1.equals("Od") && ! tmp2.equals("0a")) {
			s3 += tmp1;
			tmp1 = tmp2;
			tmp2 = trame.get_nb_octets(1);
		}
		s3 = hexToAscii(s3);
		
		//Si la trame ne contient pas les informations pertinentes, on s'arrete de lire
		if (!isHttp()) return;
		
		//On vérifie s'il s'agit d'une réponse HTTP
		int port_source = trame.getIPv4().getTCP().getSourcePort();
		if (port_source == 80) {
			while(true) {
				//On a pas trouvé de Content-Type
				if (trame.getCursor() >= trame.getTaille() - 1)
					break;
				
				//Entete
				String nom_entete = "";
				tmp = trame.get_nb_octets(1);
				while(!tmp.equals("20") && trame.getCursor() < trame.getTaille()) {
					nom_entete += tmp;
					tmp = trame.get_nb_octets(1);
				}
				//Valeur du champ
				if (hexToAscii(nom_entete).contains("Content-Type:")) {	
					tmp1 = trame.get_nb_octets(1);
					tmp2 = trame.get_nb_octets(1);
					while(!tmp1.equals("Od") && !tmp2.equals("0a")) {
						content_type += tmp1;
						tmp1 = tmp2;
						tmp2 = trame.get_nb_octets(1);
					}
					content_type = hexToAscii(content_type);
					break;
				}
				//Si l'entête n'est pas Content-Type, on passe à l'entête suivant
				else {
					tmp1 = trame.get_nb_octets(1);
					tmp2 = trame.get_nb_octets(1);
					while(!tmp1.equals("Od") && !tmp2.equals("0a") && trame.getCursor() < trame.getTaille()) {
						tmp1 = tmp2;
						tmp2 = trame.get_nb_octets(1);
					}
				}
			}
		}
	}
	
	/**
	 * Fonction qui convertit une chaine de caractères hexadécimale en ASCII
	 * @param hex une chaine de caractères en hexadécimal
	 * @return la conversion en ASCII
	 */
	public String hexToAscii(String hex) {
		StringBuilder output = new StringBuilder();
		for (int i = 0; i < hex.length(); i+=2) {
		    String str = hex.substring(i, i+2);
		    output.append((char)Integer.parseInt(str, 16));
		}
		return output.toString();
	}
	
	/**
	 * Renvoie le commentaire associé à la trame lue
	 * @return une chaîne de caractères représentant le commentaire
	 */
	public String getCommentary() {
		TCP frag_origine = this.first_fragment();
		if (frag_origine == null) return "";
		String s = "";
		s += frag_origine.getHttp().s1 + " " + frag_origine.getHttp().s2 + " " + frag_origine.getHttp().s3;
		if (!frag_origine.getHttp().content_type.equals("")) {
			String[] tmp =  frag_origine.getHttp().content_type.split(";");
			s += " (" + tmp[0] + ")";
		}
		return s;
	}
	
	/**
	 * @return la trame contenant les informations HTTP pertinentes qui sont associées à cette trame
	 */
	public TCP first_fragment() {
        if (this.isHttp()) {
            return trame.getIPv4().getTCP();
        }
        
        TCP first = null;
        TCP courant = null;
        
        int i = trame.getNum()-1 ;
        int sourceHTTP = trame.getIPv4().getTCP().getSourcePort();        
    
        do {
        	if (Trace.getListe_trame() != null && Trace.getListe_trame().get(i) != null && Trace.getListe_trame().get(i).getIPv4().getTCP() == null) {
        		i--;
        		continue;
        	}
            courant = Trace.getListe_trame().get(i).getIPv4().getTCP();
            if (sourceHTTP == courant.getSourcePort())
                first = courant;
            i--;
        } while (i >= 1 && (!courant.getHttp().isHttp() || (courant.getHttp().isHttp() && sourceHTTP != courant.getSourcePort())));
        
        return first;
	}
	
	/**
	 * Fonction qui vérifie si la trame contient des informations HTTP pertinentes
	 * @return true si le data contient la chaine de caractère "HTTP", false sinon
	 */
	public boolean isHttp() {
        return s1.contains("HTTP") || s2.contains("HTTP") || s3.contains("HTTP");
    }
}
