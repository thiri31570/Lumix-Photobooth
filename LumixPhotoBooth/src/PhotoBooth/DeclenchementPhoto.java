package PhotoBooth;

import java.awt.Image;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class DeclenchementPhoto implements Runnable {

	private ImageIcon image;
	private int index;
	private JButton label;
	
	static String getUrlContents(String theUrl)  
	  {  
	    StringBuilder content = new StringBuilder();  
	    // Use try and catch to avoid the exceptions  
	    try  
	    {  
	      URL url = new URL(theUrl); // creating a url object  
	      URLConnection urlConnection = url.openConnection(); // creating a urlconnection object  
	  
	      // wrapping the urlconnection in a bufferedreader  
	      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));  
	      String line;  
	      // reading from the urlconnection using the bufferedreader  
	      while ((line = bufferedReader.readLine()) != null)  
	      {  
	        content.append(line + "\n");  
	      }  
	      bufferedReader.close();  
	    }  
	    catch(Exception e)  
	    {  
	      e.printStackTrace();  
	    }  
	    return content.toString();  
	  }
	
	static int numDernierePhoto (int index)
    {
	int i = 0;
	boolean OK = false;
	// 5 photos sont testées, au delà il vaut mieux relancer en mettant dans properties le num de la dernière photo
    while ( i < 5) { //while1
 	   System.out.println("Check Photo :" + index);
 	   URL url;
		try {
		 		  // Recherche de la dernière photo augmentant l'index
		 		  url = new URL( "http://" + PhotoBoothMain.cameraIp +":50001/DO" + (index + 1) + ".jpg");
		 	 	   HttpURLConnection huc2 = (HttpURLConnection) url.openConnection();
		 	 	   int responseCode2 = huc2.getResponseCode();
		 	 	   if (  responseCode2 == HttpURLConnection.HTTP_OK) {
		 	 		   index++;
		 	 		   i++;
		 	 	   }
		 	 	   else
		 	 	   {
		 	 	   // Si i = 0 aucune photo + 1 n'a été détecté
		 	 	   // Vérifier que la photo courante existe, sinon recherche de la dernière photo en diminuant l'index
		 	 	   if (i == 0)
		 	 	   { 
		 	 			// Check que la photo courante existe
		 	 		    while ( i < 5) { //while2
		 				url = new URL( "http://" + PhotoBoothMain.cameraIp +":50001/DO" + index  + ".jpg");			
		 				HttpURLConnection huc = (HttpURLConnection) url.openConnection();
		 			 	   int responseCode = huc.getResponseCode();
		 			 	   if (  responseCode == HttpURLConnection.HTTP_OK) {
		 			 		   // On sort de la boucle & la dernière photo est trouvée
		 			 		   OK=true;
		 			 		   i=5;
		 			 	   }
		 			 	 else
		 			 	 {  
		 			 		 // La photo courante n'existe pas
		 			 		 index--;
		 			 		 i++;
		 			 		 }
		 	 		    } // fin while2
		 	 	   }
		 	 	   else
		 	 	   {
		 	 		   if (i != 5)  {
 			 		  // On sort de la boucle & la dernière photo est trouvée
 			 		  OK=true;
 			 		  i=5; 
		 	 		   }
		 	 	   }
		 	 	   }
		 	 } catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
 		 System.out.println("MalformedURLException");
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			 System.out.println("IOException");
			e1.printStackTrace();
		}
 	 	                	 
    } // fin while1
    if ( OK ) {
        System.out.println("Dernière photo :" + index);
    }
    else
        {
    	System.out.println("Dernière photo non trouvée. Demande de saisie");
    	String output = JOptionPane.showInputDialog(PhotoBoothMain.mainFramePhotoBooth,"Quel est le numéro de la dernière photo ?", Integer.toString(index));
    	if ( output == null )
    	{
    		// Clique sur le bouton annulé. Demande de confirmation de sortie
    		PhotoBoothMain.mainFramePhotoBooth.dispatchEvent(new WindowEvent(PhotoBoothMain.mainFramePhotoBooth, WindowEvent.WINDOW_CLOSING));
    	}
    	index = Integer.valueOf(output);
		// Nouvelle =boucle de vérification
    	index = numDernierePhoto(index);

        }
    return(index);
} // Fin numDernierePhoto
	
	public DeclenchementPhoto  (int index, JButton label) {
		this.index = index;
		this.label = label;
		System.out.println("Enter DeclenchementPhoto : " + index);
		}
	
	// @Override
	public void run() {
	
	String output = new String();
	int indexCtrl = index;
	int i = 0;
	Boolean OK = false;
	
	if (PhotoBoothMain.camera == "Panasonic")
	{
	while ( i < 5  ) { // Tant que la photo n'est pas prise, max 5 essais
	  output  = getUrlContents("http://" + PhotoBoothMain.cameraIp +"/cam.cgi?mode=camcmd&value=capture");
	  // Temporisation 500ms pour laisser le temps à l'appareil d'enregistrer la photo (et éviter de la doubler...)
	  try {
		Thread.sleep(1000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	  
	  i++;
	  index = numDernierePhoto(index);
	  System.out.println("indexPhoto après prise de vue : "+ index);
	  if ( indexCtrl != index ) { // La photo a bien été prise & on sort de la boucle
		  OK = true;
		  i=5;
	  }
		}
	if (!OK) // Aucune photo n'a été prise après "i" essais
	{
    	System.out.println("Impossible de prendre la photo");
    		// Affichage de l'image dans le label
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
			    	JOptionPane.showMessageDialog(PhotoBoothMain.mainFramePhotoBooth,"Impossible de prendre la photo. Appelez le support :)");
			    	PhotoBoothMain.mainFramePhotoBooth.dispatchEvent(new WindowEvent(PhotoBoothMain.mainFramePhotoBooth, WindowEvent.WINDOW_CLOSING));
			              							
				}
			});
	}
	System.out.println("indexPhoto après prise de vue : "+ index); 
	//TODO Verifier s'il faut faire capture_cancel
	//    System.out.println(output);
	//    output  = getUrlContents("http://" + PhotoBoothMain.cameraIp +"/cam.cgi?mode=camcmd&value=capture_cancel");

	// Chargement de la photo depuis l'appareil
	try {
		image =	new ImageIcon(new ImageIcon(new URL( "http://" + PhotoBoothMain.cameraIp + ":50001/DO" + index + ".jpg")).getImage().getScaledInstance(640,480,Image.SCALE_SMOOTH));
		// Affichage de l'image dans le label
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
			label.setIcon(image);
			label.setActionCommand("Photo" + index);
			}
		});
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	else
	{ // Capture images Webcam
		image =	new ImageIcon(new ImageIcon(PhotoBoothMain.dataRep + "/Test/photo" + index +".jpg").getImage().getScaledInstance(640,480,Image.SCALE_SMOOTH));

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
			label.setIcon(image);
			label.setActionCommand("Photo" + index);
			}
		});
	
	}
	System.out.println("End LoadPhoto");
	}
}

