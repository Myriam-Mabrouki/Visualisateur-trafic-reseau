package pobj.analyse;

import java.awt.event.*;
import java.io.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.filechooser.*;

/**
 * Classe réalisant l'interface graphique de notre programme
 * @author Floria LIM et Myriam MABROUKI
 */

public class InterfaceGraphique extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	
	//Panneaux
	private final JSplitPane splitPane;
	private final JPanel panel1;
	private final JPanel panel2;
	private final JPanel panel3;
	
	//Boutons
	private final JButton button_fichier; 
	private final JButton button_filtre;
	private final JButton button_save;
	private final JButton button_clear_text_area;
	private final JButton new_window;
	private final JButton close;
	
	//Visualisateur
	private JTextArea textArea;
	private JScrollPane scrollPane;
	
	//Filtres
	private JComboBox<String> comboBox_ip;
	private JComboBox<String> comboBox_protocole;
	
	//Trace
	private Trace trace = null;
	
	//Fichier contenant la trace
	private File f = null;
	private FileInputStream file = null;
	
	//Chemin du fichier
	private JLabel nom_fichier;
	
	public InterfaceGraphique() throws FileNotFoundException{
		
		//Paramètres d'affichage
        setTitle("Graphique des flux");
        setPreferredSize(screenSize);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new GridLayout());
        
        //Création du premier panneau contenant le visualisateur
        panel1 = new JPanel();
        textArea = new JTextArea(30,100);
        textArea.setEditable(false);
        textArea.setFont(textArea.getFont().deriveFont(15f));
        textArea.setBackground(new Color(202, 255, 173));
        scrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            @Override
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
            	if (evt.isControlDown()) {
                    textArea.setFont(new java.awt.Font(textArea.getFont().getFontName(), textArea.getFont().getStyle(),
                                               evt.getUnitsToScroll() > 0 ? textArea.getFont().getSize() - 2 
                                               : textArea.getFont().getSize() + 2));
                }
            }
        });
        panel1.add(scrollPane);
        
        //Boutons
        
        //Bouton d'ouverture de fenêtre
        new_window = new JButton( new AbstractAction("Nouvelle fenêtre") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					//Création nouvelle fenêtre
					InterfaceGraphique window = new InterfaceGraphique();
					window.setVisible(true);
				} catch (FileNotFoundException e1) {
					//Erreur si échec
					System.out.println("Erreur ouverture d'une nouvelle fenêtre");
				}
			}
        });
        
        //Bouton de fermeture de fenêtre
        close = new JButton( new AbstractAction("Fermer la fenêtre") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
        });
       
        //Boutons de sélection de fichier
        button_fichier = new JButton( new AbstractAction("Choisir un fichier") {
            private static final long serialVersionUID = 1L;

			@Override
            public void actionPerformed( ActionEvent e ) {
				
				//On permet à l'utilisateur de chosir un fichier
            	JFileChooser choose = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                
            	//On n'autorise seulement les fichiers .txt
            	choose.setAcceptAllFileFilterUsed(false);
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Fichiers textes", "txt");
                choose.addChoosableFileFilter(filter);
                
            	int res = choose.showOpenDialog(null);
            	
            	//Une fois le fichier choisi
            	if (res == JFileChooser.APPROVE_OPTION) {
            		f = choose.getSelectedFile();
            		try {
            			//On réinitialise notre trace et notre visualisateur
            			trace = null;
            			textArea.setText("");
            			
            			//On lit la trame à partir du fichier sélectionné
						file = new FileInputStream(f);
						trace = new Trace(file);
						
						//On affiche l'emplacement du fichier sélectionné 
						nom_fichier.setText(f.getAbsolutePath());
	            		
						//On lit chaque trame et on les affiche chacune dans le visualisateur
	            		for (int i=0; i<Trace.getListe_trame().size(); i++) {
	            			Trame t = Trace.getListe_trame().get(i);
	            			t.lire_trame();
	            			if (i == 0)
	            				textArea.append(Affichage.firstLigne(Trace.getListe_trame().get(0))+'\n');
	            			textArea.append(t.affichage()+"\n");
	            		}

	            		
	            		//On réinitialise les filtres disponibles précédemment
	            		comboBox_protocole.removeAllItems();
	            		comboBox_ip.removeAllItems();
	            		
	            		//On ajoute des filtres en fonction des protocoles rencontrés lors de la lecture 
	            		comboBox_protocole.addItem("Tous les protocoles");
	            		for (String p : Trace.getListeProtocole()) {
	            			comboBox_protocole.addItem(p);
	            		}
	            		
	            		//On ajoute des filtres en fonction des adresses IP rencontrées lors de la lecture 
	            		comboBox_ip.addItem("Toutes les adresses IP");
	            		comboBox_ip.addItem(Trace.getListe_trame().get(0).getIPv4().getIp_source());
	            		comboBox_ip.addItem(Trace.getListe_trame().get(0).getIPv4().getIp_destination());
	            		
	            		//On activite les boutons de filtres et de sauvegarde
	                    button_filtre.setEnabled(true);
	                    button_save.setEnabled(true);
	                    comboBox_ip.setEnabled(true);
	                    comboBox_protocole.setEnabled(true);
	                    textArea.setCaretPosition(0);
					} catch (FileNotFoundException e1) {
						//Erreur si échec d'ouverture du fichier
						new JOptionPane();
						JOptionPane.showMessageDialog(null, "Erreur lors de l'ouverture du fichier", "Erreur", JOptionPane.ERROR_MESSAGE);
					}
            		catch (Exception e2) {
            			//Erreur si le format de la trace est invalide
            			JOptionPane.showMessageDialog(null, "Trace invalide", "Erreur", JOptionPane.ERROR_MESSAGE);
            		}
            	}
            }
        });
        
        //Bouton d'effaçage complet
        button_clear_text_area = new JButton(new AbstractAction("Tout effacer") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				//On vide la trace et l'affichage dans le visualisateur
				textArea.setText("");
				trace = null;
				
				//On interdit l'accès aux boutons de filtres et de sauvegarde
				button_save.setEnabled(false);
				comboBox_ip.setEnabled(false);
                comboBox_protocole.setEnabled(false);
			}
        });
        
        //Bouton de sauvegarde
        button_save = new JButton(new AbstractAction("Enregistrer sous") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					
					//On permet à l'utilisateur de choisir le répertoire dans lequel il souhaite sauvegarder son fichier
			        JFileChooser chooser = new JFileChooser();
			        chooser.setCurrentDirectory(new  File("."+File.separator));
			        int reponse = chooser.showDialog(chooser,"Enregistrer sous");

			        //Une fois le répertoire choisi
			        if  (reponse == JFileChooser.APPROVE_OPTION){
			        	
			        	//On écrit dans un nouveau fichier le contenu affiché par le visualisateur
			        	File f = chooser.getSelectedFile();
		        		FileWriter fw = new FileWriter(f);
		    			textArea.write(fw);
		    			fw.close();
			         }
				}
				catch (IOException e2) {
					//Erreur si échec à l'écriture
					JOptionPane.showMessageDialog(null, "Erreur à l'écriture", "Erreur", JOptionPane.ERROR_MESSAGE);
	        	}
				catch(HeadlessException he){
					//Erreur si échec lors de la sauvegarde
					JOptionPane.showMessageDialog(null, "Erreur lors de la sauvegarde", "Erreur", JOptionPane.ERROR_MESSAGE);
				}
				catch (Exception e1) {
					//Erreur si échec
					JOptionPane.showMessageDialog(null, "Erreur", "Erreur", JOptionPane.ERROR_MESSAGE);
				}
			}
        });
        
        //Bouton de filtrage
        button_filtre = new JButton( new AbstractAction("Filtrer") {
            private static final long serialVersionUID = 1L;

			@Override
            public void actionPerformed( ActionEvent e ) {
				
				//On réinitialise notre trace et notre visualisateur
    			trace = null;
    			textArea.setText("");
    			
    			//On relit la trame à partir du fichier sélectionné
    			try {
					file = new FileInputStream(f);
				} catch (FileNotFoundException e1) {
					//Erreur si échec
					JOptionPane.showMessageDialog(null, "Erreur", "Erreur", JOptionPane.ERROR_MESSAGE);
				}
				trace = new Trace(file);
        		
				//On lit chaque trame et on les affiche chacune dans le visualisateur
        		for (int i=0; i<Trace.getListe_trame().size(); i++) {
        			Trame t = Trace.getListe_trame().get(i);
        			t.lire_trame();
        			if (i == 0)
        				textArea.append(Affichage.firstLigne(Trace.getListe_trame().get(0))+'\n');
        			t.affichage();
        			//Si la trame courante respecte les conditions de filtrage, alors on l'ajoute au visualisateur
	        		if ( (comboBox_protocole.getSelectedItem().equals("Tous les protocoles") || comboBox_protocole.getSelectedItem().equals(t.nomProtocole()) || (comboBox_protocole.getSelectedItem().equals("TCP") && t.nomProtocole().equals("HTTP") )) 
	        		&& (comboBox_ip.getSelectedItem().equals("Toutes les adresses IP") || comboBox_ip.getSelectedItem().equals(t.getIPv4().getIp_source())) ) {
	        			textArea.append(t.affichage()+"\n");
	        		}
        		}
 
	            textArea.setCaretPosition(0);
            }
        });
        
        //Au départ, on interdit la sauvegarde et le filtrage à l'utilisateur car aucun fichier trace n'est présent
        button_save.setEnabled(false);
        button_filtre.setEnabled(false);
        
        //Créations des choix de filtres dont on interdit l'accès lorsqu'il n'y a pas de fichier
        comboBox_protocole = new JComboBox<String>();
        comboBox_ip = new JComboBox<String>();
        comboBox_ip.addItem("Toutes les adresses IP");
        comboBox_protocole.addItem("Tous les protocoles");
        comboBox_protocole.setEnabled(false);
        comboBox_ip.setEnabled(false);
        
        //Création d'un deuxième panneau et ajout des boutons dans celui-ci
        panel3 = new JPanel();
        panel3.add(new_window);
        panel3.add(close);
        panel3.add(button_fichier);  
        nom_fichier = new JLabel("");
        panel3.add(nom_fichier);  
        panel3.add(button_save);
        panel3.add(button_clear_text_area); 
        panel3.add(new JLabel("Adresse IP :"));
        panel3.add(comboBox_ip);
        panel3.add(new JLabel("Protocole :"));
        panel3.add(comboBox_protocole);
        panel3.add(button_filtre);
        panel2 = new JPanel();
        panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));
        panel2.add(panel3);
        
        
        //Création d'un dernier panneau comprenant les deux panneaux précédents
        splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(panel2);
        splitPane.setBottomComponent(panel1);
        splitPane.setDividerLocation(70);
        getContentPane().add(splitPane);
        
        //Améliore l'affichage
        pack();

	}
}
