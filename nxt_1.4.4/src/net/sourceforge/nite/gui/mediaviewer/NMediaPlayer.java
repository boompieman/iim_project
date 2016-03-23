/* @author Dennis Hofs
 * @version  0, revision $Revision: 1.6 $,
 * $Date: 2006/05/25 14:12:33 $
 */
// Last modification by: $Author: jonathankil $
// $Log: NMediaPlayer.java,v $
// Revision 1.6  2006/05/25 14:12:33  jonathankil
// Allow individual signals to modify the path + centralize the
// generation of signal filenames / paths.
//
// Revision 1.5  2006/05/24 17:22:31  jonathankil
// Allow a modifier to the signal path on the basis of observation name -
// sort of a cheap fix for AMI distro but potentially useful way of doing
// things for annotations too?
//
// Revision 1.4  2005/04/28 15:02:24  dhofs
// Made closable
//
// Revision 1.3  2005/02/02 12:29:55  dhofs
// Re-registers time handler after openFile
//
// Revision 1.2  2005/01/24 16:42:20  jonathankil
// further check on signals.
//
// Revision 1.1  2004/12/10 16:08:21  reidsma
// DR: Upmigration of AMI code into sourceforge
//
// Revision 1.14  2004/12/08 12:43:20  dennisr
// *** empty log message ***
//
// Revision 1.13  2004/12/08 09:30:32  dennisr
// *** empty log message ***
//
// Revision 1.12  2004/10/27 07:19:40  hofs
// Loads first available signal at startup.
//
// Revision 1.11  2004/10/26 14:53:25  hofs
// Error handling if media file cannot be opened; editable time label
//
// Revision 1.10  2004/10/11 14:42:46  dennisr
// [DR] added timelabel to player
//
// Revision 1.9  2004/09/27 09:47:44  hofs
// Moved close() to NXT
//
// Revision 1.8  2004/09/16 10:49:15  hofs
// Moved rate slider to NXT
//
// Revision 1.7  2004/09/13 12:31:25  hofs
// Video position saved when signal is changed
//
// Revision 1.6  2004/09/09 08:19:33  hofs
// Improved rate slider
//
// Revision 1.5  2004/09/08 10:57:14  hofs
// Added rate slider
//
// Revision 1.4  2004/08/31 09:03:07  hofs
// Added close()
//
// Revision 1.3  2004/08/24 07:37:32  hofs
// Added getCurrentSignal()
//
// Revision 1.2  2004/08/23 14:25:57  hofs
// Notifies signal listeners when another signal is loaded into the player
//
// Revision 1.1  2004/08/19 13:23:16  hofs
// Moved from project.ami.textlabeler
//
// Revision 1.4  2004/08/18 12:33:10  hofs
// Removed closable attribute without a change in NITE package
//
// Revision 1.3  2004/08/17 11:58:11  hofs
// Removed closable attribute
//
// Revision 1.2  2004/08/16 11:53:58  hofs
// Added documentation
//
// Revision 1.1  2004/08/16 07:53:23  hofs
// Initial version
//

package net.sourceforge.nite.gui.mediaviewer;

import net.sourceforge.nite.meta.*;
import net.sourceforge.nite.gui.util.*;
import net.sourceforge.nite.time.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.*;
import javax.media.*;

/**
 * <p>This media player is an extension of the video player from the NITE XML
 * Toolkit. It can automatically load the interaction signals (both audio and
 * video) for a certain observation. The media player adds a combo box at the
 * top of the window listing all available signals. The names of the available
 * signals are taken from the metadata.</p>
 *
 * <p>When the user selects another signal from the combo box, the new signal
 * is loaded and the player is paused and reset to the start of the signal.</p>
 *
 * <p>This media player also adds a rate slider at the bottom of the window.
 * It ranges from -4x (meaning that the media is played 4 times slower than
 * normal) to +4x (the media is played 4 times faster). The default value is
 * 0 (normal play rate). The play rate is computed with an exponential function
 * on the distance of the slider value from 0. This means that the rate grows
 * faster as the distance of the slider value from 0 increases. The play rate
 * is not changed while the player is playing. A slider change will have its
 * effect when the player is paused and playback is restarted.</p>
 *
 * <p>If a video does not fit in the window, it is automatically scaled so that
 * it fits.</p>
 
 THIS CLASS WILL BE MADE OBSOLETE
 THIS CLASS WILL BE MADE OBSOLETE
 THIS CLASS WILL BE MADE OBSOLETE
 THIS CLASS WILL BE MADE OBSOLETE

DR: clockFace will more or less contain all functionality for which this class was made in the first place

 */
public class NMediaPlayer extends NITEVideoPlayer {
	private NMetaData metaData;
	private String obsName;
	private int lastSignal = -1; // used to reset if a signal cannot be opened
	private JComboBox signalCombo;
	private Vector signalListeners = new Vector();
	private double lastPosition = 0.0;
	
	/**
	 * This layout manager can display one component. The component is displayed
	 * at the top left corner of a container. If the component does not fit in
	 * the container, it is scaled down so that it fits. The proportion of the
	 * component's width and height is maintained.
	 *
	 * If more than one component is added to the container, only the first
	 * component will be displayed. The component should be added to the
	 * container, not to this layout manager.
	 */
	private class ShrinkToFitLayout implements LayoutManager {
		public void addLayoutComponent(String name, Component comp) {}
		
		public void layoutContainer(Container parent) {
			if (parent.getComponentCount() == 0) return;
			Component component = parent.getComponent(0);
			Dimension size = component.getPreferredSize();
			if (size.getWidth() > parent.getWidth()) {
				double scale = (double)parent.getWidth()/size.getWidth();
				size.setSize(size.getWidth()*scale,size.getHeight()*scale);
			}
			if (size.getHeight() > parent.getHeight()) {
				double scale = (double)parent.getHeight()/size.getHeight();
				size.setSize(size.getWidth()*scale,size.getHeight()*scale);
			}
			component.setBounds(0,0,(int)size.getWidth(),(int)size.getHeight());
			for (int i = 1; i < parent.getComponentCount(); i++) {
				component = parent.getComponent(i);
				component.setBounds(0,0,0,0);
			}
		}
		
		public Dimension minimumLayoutSize(Container parent) {
			if (parent.getComponentCount() == 0)
				return new Dimension(0,0);
			else
				return parent.getComponent(0).getMinimumSize();
		}
		
		public Dimension preferredLayoutSize(Container parent)
		{
			if (parent.getComponentCount() == 0)
				return new Dimension(0,0);
			else
				return parent.getComponent(0).getPreferredSize();
		}
		
		public void removeLayoutComponent(Component comp) {}
	}

	/**
	 * <p>Constructs a new media player. The combo box at the top of the media
	 * player is filled with the names of the interaction signals that are
	 * specified in the metadata file. When a signal name is selected in the
	 * combo box, the signal for the specified observation is loaded into the
	 * player.</p>
	 *
	 * @param metaData the metadata specifying interaction signals
	 * @param obsName the name of an observation
	 * @param c the NITE clock
	 */
	public NMediaPlayer(NMetaData newmetaData, String newobsName, DefaultClock c) {
		super(c);
		c.registerTimeHandler(this);
		videoPanel.setLayout(new ShrinkToFitLayout());
		this.metaData = newmetaData;
		this.obsName = newobsName;
		signalCombo = new JComboBox();
		List signals = metaData.getSignals();
		Iterator it = signals.iterator();
		while (it.hasNext()) {
			NSignal sig = (NSignal)it.next();
			signalCombo.addItem(sig.getName());
		}
		signalCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadSelectedSignal();
			}
		});
		getContentPane().add(signalCombo,BorderLayout.NORTH);
		loadSelectedSignal();
		checkPanel.add(new JButton(new AbstractAction("New") {
		    public void actionPerformed(ActionEvent e) {
		        NMediaPlayer nmp = new NMediaPlayer(metaData, obsName, (DefaultClock)getClock());
                nmp.setSize((int)getSize().getWidth(), (int)getSize().getHeight());
            	nmp.setLocation(getX()+20,getY()+20);
        		SwingUtils.getResourceIcon(nmp,"/eclipseicons/clcl16/run_tool.gif",getClass());
		        getDesktopPane().add(nmp);
		    }
		}));
	}
	
	/**
	 * Loads the signal that is selected in the combo box. If the signal cannot
	 * be loaded, but another signal was loaded before, the media player will
	 * be reset to the last signal. If no signal was loaded before, this method
	 * will try to find a signal that can be loaded. If no signal is available,
	 * the media player is closed.
	 */
	private void loadSelectedSignal() {
	    
		if (openFile(signalCombo.getSelectedIndex()))
		    lastSignal = signalCombo.getSelectedIndex();
		else if (lastSignal != -1) {
		    // show error and reset last signal
    	    JOptionPane.showMessageDialog(this,
    	        "The file for signal " + signalCombo.getSelectedItem() + " could not be opened.",
    	        "Error",
    	        JOptionPane.ERROR_MESSAGE);
		    signalCombo.setSelectedIndex(lastSignal);
		}
		else {
		    // no signal was loaded before:
		    // load first available signal or make player invisible
		    boolean found = false;
		    int i = 1;
		    while (!found && i < signalCombo.getItemCount()) {
		        found = openFile(i);
		        if (!found) i++;
		    }
		    if (found)
		        signalCombo.setSelectedIndex(i);
		    else
		        setVisible(false);
		}
	}
	
	/**
	 * Opens the media file for the signal with the specified
	 * index in the metadata - this should be deprecated as we can
	 * have agent signals and this method makes no sense for them.
	 * <p> If the file cannot be opened, this method will return
	 * false.
	 */
	private boolean openFile(int index) {
		// save the current position, reset after PrefetchCompleteEvent (see controllerUpdate)
		lastPosition = getClock().getSystemTime();
		List signals = metaData.getSignals();
		if (index<0 || signals==null || signals.size()==0) { return false; }
		NSignal sig = (NSignal)signals.get(index);
		// note - assume it's an interaction signal!
		String filename = sig.getFilename(obsName, (String)null);
		File f = new File(filename);
		if (f.isFile() && f.canRead()) {
    		openFile(f);
    		niteclock.registerTimeHandler(this);
    		Iterator it = signalListeners.iterator();
    		while (it.hasNext()) {
    			SignalListener l = (SignalListener)it.next();
    			l.signalChanged(sig.getName());
    		}
    		return true;
    	} else {
    	    System.out.println("ERROR: Could not open media file " + f);
    	    return false;
    	}
	}
	
	/**
	 * <p>Returns the name of the signal that is currently loaded. If no signal
	 * is currently loaded, this method returns null</p>
	 *
	 * @return the name of the current signal or null
	 */
	public String getCurrentSignal() {
		List signals = metaData.getSignals();
		int index = signalCombo.getSelectedIndex();
		if (index == -1) return null;
		NSignal sig = (NSignal)signals.get(index);
		return sig.getName();
	}
	
	/**
	 * <p>Loads the signal with the specified name. The signal name should exist
	 * in the metadata.</p>
	 *
	 * @param name the signal name
	 * @return true if the signal exists, false otherwise
	 */
	public boolean loadSignal(String name) {
		boolean found = false;
		int i = 0;
		while (!found && (i < signalCombo.getItemCount())) {
			found = ((String)signalCombo.getItemAt(i)).equals(name);
			if (!found) i++;
		}
		if (!found) return false;
		signalCombo.setSelectedIndex(i);
		return true;
	}
	
	/**
	 * <p>Adds a signal listener that will be notified when another signal is
	 * loaded into the media player.</p>
	 *
	 * @param l a signal listener
	 */
	public void addSignalListener(SignalListener l) {
		signalListeners.add(l);
	}
	
	/**
	 * <p>Removes a signal listener from this media player.</p>
	 *
	 * @param l the signal listener that should be removed
	 */
	public void removeSignalListener(SignalListener l) {
		signalListeners.remove(l);
	}
	
	/**
	 * <p>Called whenever there is a media event. When a media file has been
	 * prefetched, this method resets the position of the media player to the
	 * saved last position. Before loading new media, the current position is
	 * saved, so it can be reset in this method. This means that the position is
	 * not reset to 0 every time another signal is selected.</p>
	 */
	public synchronized void controllerUpdate(ControllerEvent event) {
		super.controllerUpdate(event);
		if (event instanceof PrefetchCompleteEvent) {
			getClock().setSystemTime(lastPosition);
		}
	}
}
