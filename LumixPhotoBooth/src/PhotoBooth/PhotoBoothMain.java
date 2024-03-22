package PhotoBooth;


import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Properties;
import javax.swing.*;
import streamviewer.StreamViewer;
import streamviewer.VideoPanel;


public class PhotoBoothMain {


	// TODO à mettre dans le fichier de Properties
	static String dataRep = new 
// String � modifier en fonction du contexte d'installation (localisation fichier properties + donn�es de tests)
String("C:/Users/thier/LumixPhotoBooth/data");
	static String camera ="NOPanasonic";
// Lorsque l'appareil est connect� en WIFI => set camera = "Panasonic" pour effectivement communiquer avec lui
// 	static String camera ="Panasonic";
	static String cameraIp = "192.168.54.1";
	static int cameraNetMaskBitSize = 24;

	// Variables initiatilisées depuis Properties
	//private static int indexPhoto;
	private static int indexPhoto=0;
	
    /* Organisation des fenetres :
 	mainFramePhotoBooth (JFrame) - Size 730x605
 	.setContentPane -> conteneur (JPanel)
		         		-> content (JPanel) - Size 686x518
		         			-> cardl (CardLayout)
		         				-> panvideo (JPanel)
		         					-> videoPanel (VideoPanel)
		         				-> scrollPhotos (JScrollPane)
		         					-> conteneurPhotos (JPanel)
		         						-> boutonPhoto[0-2] (JButton)
		         						
		         				-> diaporama (JPanel)
		         					-> diaporamaLabel (JLabel)
		         		-> panelBoutons (JPanel)
		         			-> boutonPriseVue (JButton)
		         			-> boutonImpression (JButton)
		         			-> boutonRetour (JButton)
		     
Fin organisation des fenetres */
	
	static JFrame mainFramePhotoBooth = new JFrame();
	private static CardLayout cardl = new CardLayout();
	private static JPanel content = new JPanel();
	// Creation espace affichage de texte
	private static  JLabel messagePrincipal = new JLabel();
	// Creation du panneau panelBoutons auquel sera affecté le bouton prise de vue
	private static JPanel panelBoutons = new JPanel();
	// Creation  du bouton "Prise de Vue"
	private static JButton boutonPriseVue = new JButton("Prise de vue");
	// Creation  du bouton "Impression"
	private static JButton boutonImpression = new JButton("Impression");
	// Creation  du bouton "Retour"
	private static JButton boutonRetour = new JButton("Retour");
	// JPanel pour l'affichage des photos prise
	private static JPanel conteneurPhotos = new JPanel();
	// Boutons pour les photos 
	private static JButton boutonPhoto[] = new JButton[3];
	
	
	// Temps entre chaque décompte (1000 = 1sec)
	private static int delais = 100;
	private static int tempsRestant;
	private static Timer timer;

	// Compteur de Clicks photos
	private static int NbClicks = 0; 
	
	// Threads
	private static Thread streamViewerThread;
	private static Thread clickThread;
	
	// Impression Photos : L'impression s'initialise en cliquant sur l'un des boutons "boutonPhoto[]"
	private static void impressionPhotos ()
	{
	    panelBoutons.remove(boutonPriseVue);
		panelBoutons.remove(boutonImpression);
		panelBoutons.add(boutonRetour);
		messagePrincipal.setText("Cliquez sur la photo à imprimer");
		System.out.println("Impression");
		// Affichage de la vue photo
		cardl.show(content, "Photos");
	}
	
	// Retour vers la prise de vue
	private static void RetourPriseVue ()
	{
		// Reinit des boutons
		for (int i = 0; i<3; i++ ) {
		boutonPhoto[i].setIcon(null);
		boutonPhoto[i].setActionCommand("");
		conteneurPhotos.remove(boutonPhoto[i]);
		}
		
		// Affichage de la vue vidéo
		cardl.show(content, "Video");
		// Il faut cliquer à nouveau (le pannel vidéo est affiché en sortie de la procédure)
		messagePrincipal.setText("A nouveau prêt pour prendre une photo !");
		boutonPriseVue.setText("Nouvelle photo");
		panelBoutons.remove(boutonImpression);
		panelBoutons.remove(boutonRetour);
		panelBoutons.add(boutonPriseVue);
		NbClicks = 0;
	}
	
	// Timer pour décompter la prise de vue
    private static Timer prisePhoto ()
    {
        ActionListener action = new ActionListener (){
            public void actionPerformed (ActionEvent event){
            	if(tempsRestant>0){
                	boutonPriseVue.setEnabled(false);
                    messagePrincipal.setText("Prise de la photo dans " + tempsRestant + " sec");
                    tempsRestant--;
                }
                else{
                	messagePrincipal.setText("Click !");
                	
	                if (camera == "Panasonic")
	         			{
                	// Recherche de la dernière photo avant la prise de vue
                	indexPhoto = DeclenchementPhoto.numDernierePhoto(indexPhoto);
	         			} 
	                else // Code pour WebCam par exemple
	                {	// On a 3 photos en test
	                	indexPhoto++;
	                }
                	// On prend la photo dand un nouveau thread pour ne pas bloquer l'affichage
                	DeclenchementPhoto click = new DeclenchementPhoto(indexPhoto,boutonPhoto[NbClicks]);
                	clickThread = new Thread(click);
                	clickThread.start();
                	
                 	conteneurPhotos.add(boutonPhoto[NbClicks]);
        			panelBoutons.add(boutonImpression);
        			
                 	NbClicks++;
        			
 				    boutonPriseVue.setText("Nouvelle photo");
				    boutonPriseVue.setEnabled(true);
				    // Affichage bouton impression (dès la 1ere photo)
				    boutonImpression.setText("Impression");
				    panelBoutons.add(boutonImpression);
				    // Impression systématique au bout de 3 photos
					if ( NbClicks >= 3) {
		                   int reponse = JOptionPane.showConfirmDialog(mainFramePhotoBooth,
                                   "Vous avez pris 3 photos. Impression !",
                                   "Confirmation",
                                   JOptionPane.YES_NO_OPTION,
                                   JOptionPane.QUESTION_MESSAGE);
              
			              if (reponse==JOptionPane.YES_OPTION){
								    impressionPhotos();
			              }
			              else
			              {
		                   reponse = JOptionPane.showConfirmDialog(mainFramePhotoBooth,
	                               "Etes vous certain d'annuler l'impression ?",
	                               "Confirmation",
	                               JOptionPane.YES_NO_OPTION,
	                               JOptionPane.QUESTION_MESSAGE);
			                 if (reponse==JOptionPane.YES_OPTION){
			                	RetourPriseVue();
			                 }
			                 else
			                 {
								    impressionPhotos();
			                 }
			              }
					}
					timer.stop();             	
                }
            }
        };
        return new Timer (delais, action);
    }
	
 
    public static void main(String[] args) throws Exception {

    	
    	// Chargement fichier de proprietes
    	try ( FileInputStream input = new FileInputStream(dataRep + "/photobooth.properties")) {
    	  	Properties prop = new Properties();
    	    prop.load(input);
    	    indexPhoto = Integer.valueOf(prop.getProperty("indexPhoto"));
    	    System.out.println("indexPhoto: " + indexPhoto); 
    	} catch (IOException ex) {
    	    ex.printStackTrace();
    	}

    	   	
    	// TODO Vérifier que la connexion UPNP est bien ouverte pour naviguer dans les photos prises est bien ouverte(initialisée par VLC)
    	// TODO Vérifier que la connexion de passage de commande et de capture stream video est bien ouverte (initialisée par LumixMR)
    	// TODO Initialiser le fichier properties s'il n'existe pas
    	// TODO Réveiller l'appareil photo lorsque l'appli est lancée et qu'il se met en veille (à faire avec l'idée d'un Diaporama qui défile au bout de X sec)
    	// TODO Warning : réveiller le flux vidéo n'est pas suffisant pour s'assurer que l'appareil est prêt (la premiere cmde capture ne fait rien). Test à faire lors de la prise de vue !

        // Capture video depuis l'appareil photo
    	// Flux capturé dans le JPanel panvideo
        
        final JPanel panvideo = new JPanel();
		if (camera == "Panasonic")
		{
		// Le JPanel videoPanel reçoit le stream video
        VideoPanel videoPanel = new VideoPanel();
       
        System.out.println("Trying to connect to camera " + cameraIp + " on subnet with mask size " +
                cameraNetMaskBitSize);
        try {
            StreamViewer streamViewer = new StreamViewer(videoPanel::displayNewImage, cameraIp, cameraNetMaskBitSize);
            streamViewerThread = new Thread(streamViewer);
            streamViewerThread.start();
        } catch (SocketException e) {
            System.out.println("Socket creation error : " + e.getMessage());
            System.exit(1);
        } catch (UnknownHostException e) {
            System.out.println("Cannot parse camera IP address: " + cameraIp + ".");
            System.exit(2);
        }


        panvideo.add(videoPanel);
        
        panvideo.setVisible(true);
        // Fin capture flux video dans le JPanel panvideo
		}
		else // Code pour WebCam par exemple
		{
			panvideo.add(new JTextField("Stream Video"));
		}

   		// Initialisation fenetre principale
   			mainFramePhotoBooth.setTitle("Daron PhotoBooth");
  			
   			// Dimension fenetre à choisir...
   			mainFramePhotoBooth.setSize(730, 605);
   			mainFramePhotoBooth.setLocationRelativeTo(null);
   			
			// Definition du layout pour basculer sur les différentes vues (retour video camera / photos prises / diaporama)
			content.setLayout(cardl);
		    content.setPreferredSize(new Dimension(686,518));   

   			// Creation des 3 boutonPhoto
   			for (int i = 0; i < 3; i++) {  
   	       		boutonPhoto[i] = new JButton();
   			}
   			
    	    JScrollPane scrollPhotos = new JScrollPane(conteneurPhotos,ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

   		    // JPanel pour le diaporama
    	    JPanel diaporama = new JPanel();
    	    JLabel diaporamaLabel = new JLabel();
    	    diaporamaLabel.setIcon(null);
    	    
    	    diaporama.add(diaporamaLabel);
   
    	    // Afficher Video, Photos ou Diaporama dans la fenêtre cardl

    	    cardl.addLayoutComponent(panvideo,"Video");
    	    content.add(panvideo);
    	    cardl.addLayoutComponent(scrollPhotos,"Photos");
    		content.add(scrollPhotos);
    	    cardl.addLayoutComponent(diaporama,"Diaporama");
    		content.add(diaporama);
    	    
       		// Creation du conteneur principal de la fenetre
   			JPanel conteneur = new JPanel();
    		conteneur.add(panelBoutons);
   			conteneur.add(content);
    			
   			// TODO Code pour le diaporama après un certain temps d'inactivité
    			
   			mainFramePhotoBooth.setContentPane(conteneur);
   				
   			mainFramePhotoBooth.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
  	      		       		
    		System.out.println("Début PhotoBooth");
	   	    // Afficher la fenêtre
   			mainFramePhotoBooth.setVisible(true);
       		
       		System.out.println("Fin PhotoBooth");
   		       	
   			//Définition de l'écouteur d'ouverture de fenetre à l'aide d'une classe interne anonyme   			
       		mainFramePhotoBooth.addWindowListener(new WindowAdapter(){
  			     public void windowOpened(WindowEvent e){
  			    	
	              if (camera == "Panasonic")
	         		{
                    // Retrouve la dernière photo
                System.out.println("indexPhoto1 : " + indexPhoto);
                indexPhoto = DeclenchementPhoto.numDernierePhoto(indexPhoto);
                System.out.println("indexPhoto2 : " + indexPhoto);  
	         		}
	              else // Code pour WebCam par exemple
	              {
	            	  indexPhoto = 0;
	              }
  			    	 		// Check l'affichage du retour video de l'appareil photo
  			    	
		                   int reponse = JOptionPane.showConfirmDialog(mainFramePhotoBooth,
		                                        "Bienvenue ! Prêt pour une séance PhotoBooth ? ",
		                                        "Confirmation",
		                                        JOptionPane.YES_NO_OPTION,
		                                        JOptionPane.QUESTION_MESSAGE);
		                   
		                   if (reponse==JOptionPane.NO_OPTION){
		               		// Clique sur le bouton annulé. Demande de confirmation de sortie
		               		mainFramePhotoBooth.dispatchEvent(new WindowEvent(mainFramePhotoBooth, WindowEvent.WINDOW_CLOSING));
		                   }
		                   
              
		                   		                   
			           			// Ajout des boutons sur le panneau et démarrage du PhotoBooth
			           			messagePrincipal.setText("Prêt pour prendre une photo !");

			        			panelBoutons.add(messagePrincipal);
			        			
			        		    boutonPriseVue.setText("Prise de vue");
			        			panelBoutons.add(boutonPriseVue);
			        			// Switch de cartes pour rafraichir le pannel vidéo et ainsi afficher les boutons
			        			cardl.show(content, "Diaporama");
			        			cardl.show(content, "Video");
			                	 
		                	   }
		          
   		    });

   			
   			//Définition de l'écouteur de cloture de fenetre à l'aide d'une classe interne anonyme
       		mainFramePhotoBooth.addWindowListener(new WindowAdapter(){
       			     public void windowClosing(WindowEvent e){
   		                   int reponse = JOptionPane.showConfirmDialog(mainFramePhotoBooth,
   		                                        "Voulez-vous quitter l'application",
   		                                        "Confirmation",
   		                                        JOptionPane.YES_NO_OPTION,
   		                                        JOptionPane.QUESTION_MESSAGE);
   		                   if (reponse==JOptionPane.YES_OPTION){
  		                	 // maj fichier de properties   
   		                	try (FileOutputStream output = new FileOutputStream(dataRep + "/photobooth.properties")) {
   		                		Properties prop = new Properties();
   		                		prop.setProperty("indexPhoto", Integer.toString(indexPhoto));
     		                	prop.store(output, null);
   		                	} catch (IOException io) {
   		                		io.printStackTrace();
   		                	}
   		                	if (camera == "Panasonic")
   		         			{
   		     		   		// Arrêt du flux video
   		                	streamViewerThread.interrupt();
   		         			}
   		                	else // Code pour WebCam par exemple
   		                	{ }
 		                	// Cloture de la fenêtre
   		                	mainFramePhotoBooth.dispose();
   		                   }
   		             }
   		    });
        
     		// Prise de vue
     		boutonPriseVue.addActionListener(new ActionListener() {
        			// Si click sur le boutonPriseVue, prise de vue
        					@Override
        					public void actionPerformed(ActionEvent e)  {
        					        tempsRestant = 3;
        					        timer = prisePhoto();
        					        timer.start();
        					}
        			});

     		// Bouton Retour
     		boutonRetour.addActionListener(new ActionListener() {
        			// Si click sur le boutonPriseVue, prise de vue
        					@Override
        					public void actionPerformed(ActionEvent e)  {
       		                   int reponse = JOptionPane.showConfirmDialog(mainFramePhotoBooth,
                                       "Etes vous certain d'annuler l'impression ?",
                                       "Confirmation",
                                       JOptionPane.YES_NO_OPTION,
                                       JOptionPane.QUESTION_MESSAGE);
     		                 if (reponse==JOptionPane.YES_OPTION){
     		                	RetourPriseVue();
     		                 }
        					}
      	  					
        			});     		

          		// Prise de vue
       			boutonImpression.addActionListener(new ActionListener() {
        				// Si click sur le boutonImpression, impression
        					@Override
        					public void actionPerformed(ActionEvent e)  {
        						System.out.println("Click Impression");
        						impressionPhotos();
      						}
        	  					
        			});
      			
          		// Impression de la photo selectionnée
       			for (int i = 0; i < 3; i++) {
       			boutonPhoto[i].addActionListener(new ActionListener() {
        				// Si click sur le boutonImpression, impression
        					@Override
        					public void actionPerformed(ActionEvent e)  {
        						String butSrcTxt = e.getActionCommand();
        						System.out.println("Envoyer vers l'imprimante : " + butSrcTxt);
        						// TODO Ajouter le code pour envoyer vers l'imprimante
      		                   int reponse = JOptionPane.showConfirmDialog(mainFramePhotoBooth,
                                        "Voulez-vous imprimer une nouvelles photo ?",
                                        "Confirmation",
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE);
      		                 if (reponse==JOptionPane.NO_OPTION){
      		                	RetourPriseVue();
      		                 }
       					}
       				});   
       			}
    }
}