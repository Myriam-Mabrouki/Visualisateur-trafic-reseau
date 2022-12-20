package pobj.analyse;

import java.io.FileNotFoundException;

/**
 * Classe permettant l'exécution du visualisateur
 * @author Floria LIM et Myriam MABROUKI
 * 
 */

public class InterfaceGraphiqueMain {

	public static void main(String[] args) throws FileNotFoundException {
		//Lancement du programme
		InterfaceGraphique window = new InterfaceGraphique();
		window.setVisible(true);
	}

}
