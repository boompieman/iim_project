package net.sourceforge.nite.util;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JFrame;
import java.net.URL;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/** 
 * HTML viewer
 *
 * @author Jonathan Kilgour, UEdin
 */
public class HTMLViewer extends JFrame {

    public HTMLViewer(String filename, String title) {
	super(title);
	//setSize(400, 400);
	try {
	    final JEditorPane viewer = new JEditorPane(new URL(filename));
	    viewer.setEditable(false);
	    viewer.addHyperlinkListener( new HyperlinkListener() {
		    public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			    URL url = e.getURL();
			    if (url!=null) {
				try {
				    viewer.setPage(url);
				} catch (Throwable t) {
				    t.printStackTrace();
				}
			    }
			}
		    }
		});
	    JFrame frame = new JFrame();
	    getContentPane().add(new JScrollPane(viewer));
	    pack();
	    setSize(550, 450);
	    setVisible(true);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return;
	}
    }
}