/**
 * NITE XML Toolkit
 *
 * See the README file in this distribution for licence.
 *
 * @author Jonathan Kilgour
 */
package net.sourceforge.nite.util;

import net.sourceforge.nite.gui.util.PopupMessage;

/** A flexible java version checker that can take different actions
 * for different versions. The default is to warn (on STDERR) if the
 * Java version is less than 1.4.2_05 and to quit with an error
 * message on STDOUT if the Java version is less than 1.4. But you can
 * set the threshholds and make it pop messages on-screen if
 * preferred. Note: depends on alphanumeric String comparison - I
 * think that's valid.. Typical use:
<pre>
 VersionChecker vc = new VersionChecker();
 int vres = vc.checkVersion();
 if (vres==VersionChecker.VERSION_ERROR) { System.exit(1); }
</pre>
 */
public class VersionChecker {
    public final static int VERSION_OK = 1;
    public final static int VERSION_WARNING = 0;
    public final static int VERSION_ERROR = -1;
    private String cutoff = "1.4";
    private String warning = "1.4.2_05";
    boolean cutoff_message_on_screen=true;
    boolean warning_message_on_screen=false;

    /** This constructor uses all the defaults: warn (on STDERR) if
     * the Java version is less than 1.4.2_05 and popup an on-screen
     * error message if the Java version is less than 1.4.  */
    public VersionChecker() {

    }

    /** This constructor takes user-values for cutoff and warnings:
     * <tt>cut</tt> is the version number under which an error is
     * reported; <tt>warn</tt> is the version number below which a
     * warning is reported; <tt>cutmessage</tt> is true if you want
     * error messages on-screen (false for STDERR);
     * <tt>warnmessage</tt> is true if you want warninng messages
     * on-screen (false for STDERR). */
    public VersionChecker(String cut, String warn, boolean cutmessage, boolean warnmessage) {
	setCutoffMessageOnScreen(cutmessage);
	setWarningMessageOnScreen(warnmessage);
	setCutoffThreshhold(cut);
	setWarningThreshhold(warn);
    }

    /** check version from given Version string: popup any warning /
     * cutoff and return VERSION_OK, VERSION_WARNING or VERSION_ERROR
     * so the client program can decide what to do about it */
    public int checkVersion(String version) {
	int cerr = version.compareTo(cutoff);
	if (cerr<=0) { // version is lower or equal to error threshhold
	    String message = "ERROR: your Java version (" + version + ") is insufficient to run this program.\n Please upgrade to at least version " + warning;
	    deliverMessage(message, cutoff_message_on_screen);
	    return VERSION_ERROR;
	}
	int cwarn = version.compareTo(warning);
	if (cwarn<=0) { // version is lower or equal to warning threshhold
	    String message = "WARNING: your Java version (" + version + ") is lower than recommended to run this program.\n Please upgrade to at least version " + warning;
	    deliverMessage(message, warning_message_on_screen);
	    return VERSION_WARNING;
	}
	return VERSION_OK;
    }

    /** popup a message on screen or print to STDERR */
    private void deliverMessage(String message, boolean onscreen) {
	if (onscreen) {
	    PopupMessage pm = new PopupMessage(message);
	    pm.popup();
	} else {
	    System.err.println(message);
	}
    }

    /** check version compared to System version string: popup any
     * warning / cutoff and return VERSION_OK, VERSION_WARNING or
     * VERSION_ERROR so the client program can decide what to do about
     * it */
    public int checkVersion() {
	return checkVersion(System.getProperty("java.version"));
    }

    /** set to true to see any failure mesage as a popup; false for
     * STDERR. Default is popup */
    public void setCutoffMessageOnScreen(boolean val) {
	cutoff_message_on_screen=val;
    }

    /** set to true to see any warning mesage as a popup; false for
     * STDERR. Default is STDERR. */
    public void setWarningMessageOnScreen(boolean val) {
	warning_message_on_screen=val;
    }

    /** Set the cutoff threshold (as a string like '1.4.2_05' or
     * similar). Default is '1.4'. */
    public void setCutoffThreshhold(String val) {
	cutoff=val;
    }

    /** Set the warning threshold (as a string like '1.4.2_06' or
     * similar). Default is '1.4.2_06'. */
    public void setWarningThreshhold(String val) {
	warning=val;
    }

}
