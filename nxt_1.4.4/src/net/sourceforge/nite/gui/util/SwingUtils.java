/* @author Dennis Reidsma
 * @version  0, revision $Revision: 1.3 $,
 * $Date: 2010/10/01 12:59:36 $        
 */
// Last modification by: $Author: jonathankil $
// $Log: SwingUtils.java,v $
// Revision 1.3  2010/10/01 12:59:36  jonathankil
// Updated JGoodies JARs and made corresponding changes to package and method names in the code
//
// Revision 1.2  2008/06/25 11:22:23  jonathankil
// improved clockface layout and flexibility
//
// Revision 1.1  2004/12/10 16:08:22  reidsma
// DR: Upmigration of AMI code into sourceforge
//
// Revision 1.2  2004/10/20 15:04:01  dennisr
// *** empty log message ***
//
// Revision 1.1  2004/10/20 14:59:54  dennisr
// removed kunststoff
//
// Revision 1.7  2004/07/12 09:40:20  dennisr
// *** empty log message ***

package net.sourceforge.nite.gui.util;

import javax.swing.*;
import java.awt.event.*;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.*;
import java.net.URL;

import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBlue;



/**
 * General util methods
 * @author Dennis Reidsma, UTwente
 */
public class SwingUtils {
    /**
     * Set a icon for given frame
     * CallingClass is used to determine where to find resource...
     */
    public static void getResourceIcon(JFrame fr, String name, Class callingClass) {
      try {
        ImageIcon i = null;
        i = new ImageIcon(callingClass.getResource(name));
        fr.setIconImage(i.getImage());
      } catch (Exception e) {
        System.out.println("Icon not loaded: " + name);
      }
    }
    /**
     * Set a icon for given frame
     * CallingClass is used to determine where to find resource...
     */
    public static void getResourceIcon(JInternalFrame fr, String name, Class callingClass) {
      try {
        ImageIcon i = null;
        i = new ImageIcon(callingClass.getResource(name));
        fr.setFrameIcon(i);
      } catch (Exception e) {
        System.out.println("Icon not loaded: " + name);
      }
    }

    /** Initialize look and feel using Dennis's code. I made this a
     * static so that arbitrary programs can use it, not just
     * AbstractCallableTools */
    /** 
     * Initializes the look and feel to something more elegant. Others might disagree and override this method...
     */
    public static void initLnF() {
	System.out.println("Swing utils init");
        JDialog.setDefaultLookAndFeelDecorated(true);
        JFrame.setDefaultLookAndFeelDecorated(true);
        Toolkit.getDefaultToolkit().setDynamicLayout(true);
        System.setProperty("sun.awt.noerasebackground","true");        
        try {
            PlasticLookAndFeel.setCurrentTheme(new DesertBlue());
            Options.setPopupDropShadowEnabled(true);
            Options.setUseNarrowButtons(true);
            Options.setUseSystemFonts(true);
            Options.setDefaultIconSize(new Dimension (16,16));
            Plastic3DLookAndFeel olaLF = new Plastic3DLookAndFeel();
            UIManager.setLookAndFeel(olaLF);
            //update(c);
        } catch (Exception ex) {
            System.out.println("Error in LookaNdFeelSupport!");ex.printStackTrace();
        }
    }
    

}