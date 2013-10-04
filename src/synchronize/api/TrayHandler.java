package synchronize.api;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

public class TrayHandler {
	
	public static void addTrayIcon() {
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                TrayHandler.createAndShowGUI();
            }
        });
	}
	
	private static void createAndShowGUI() {
		Path image = Synchronizer.getInstance().getDataPath().resolve("images").resolve("top.png");
        //Check the SystemTray support
        if (!SystemTray.isSupported()) {
            System.err.println("SystemTray is not supported");
            return;
        }
        final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon =
                new TrayIcon(createImage(image, "tray icon"));
        final SystemTray tray = SystemTray.getSystemTray();
        
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip("Synchronizer");
         
        // Create a popup menu components
        MenuItem exitItem = new MenuItem("Exit");
         
        //Add components to popup menu
        popup.add(exitItem);
         
        trayIcon.setPopupMenu(popup);
         
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
            return;
        }
         
        /*trayIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,
                        "This dialog box is run from System Tray");
            }
        });*/
         
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tray.remove(trayIcon);
                Synchronizer.exit();
            }
        });
    }
     
    //Obtain the image URL
    protected static Image createImage(Path path, String description) {
    	URL imageURL;
    	try {
        	imageURL = path.toUri().toURL();
            return (new ImageIcon(imageURL, description)).getImage();
        } catch(MalformedURLException e) {
        	System.err.println("Malformed URL");
        	e.printStackTrace();
        }
    	return null;
    }
}
