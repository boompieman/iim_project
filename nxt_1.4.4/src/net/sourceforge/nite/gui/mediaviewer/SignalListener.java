/* @author Dennis Hofs
 * @version  0, revision $Revision: 1.1 $,
 * $Date: 2004/12/10 16:08:21 $
 */
// Last modification by: $Author: reidsma $
// $Log: SignalListener.java,v $
// Revision 1.1  2004/12/10 16:08:21  reidsma
// DR: Upmigration of AMI code into sourceforge
//
// Revision 1.1  2004/08/23 14:24:09  hofs
// Initial version
//

package net.sourceforge.nite.gui.mediaviewer;

/**
 * <p>A signal listener can be registered with
 * {@link NMediaPlayer NMediaPlayer} in order to be notified when another
 * signal is loaded into the media player.</p>
 
 WILL BE MADE OBSOLETE
 WILL BE REMOVED
 WILL BE MADE OBSOLETE
 WILL BE REMOVED
 WILL BE MADE OBSOLETE
 WILL BE REMOVED
 WILL BE MADE OBSOLETE
 WILL BE REMOVED
 */
public interface SignalListener
{
	/**
	 * <p>Called when another signal is loaded.</p>
	 *
	 * @param name the name of the new signal
	 */
	public void signalChanged(String name);
}
