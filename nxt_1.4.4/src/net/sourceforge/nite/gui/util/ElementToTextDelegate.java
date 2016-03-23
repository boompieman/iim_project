/**
 * A utility class to extract text from a NOMElement using a delegate, an
 * attribute or via its children. Generalised from {@link NTranscriptionView}
 */
package net.sourceforge.nite.gui.util;

import java.util.NoSuchElementException;

import net.sourceforge.nite.gui.transcriptionviewer.TranscriptionToTextDelegate;
import net.sourceforge.nite.nom.nomwrite.NOMElement;

/**
 * An ElementToTextDelegate returns a String for any NOMElement passed to it. There
 * are three ways the delegate determines this String. In order of preference these are:
 * 
 * <dl>
 * <dt>{@link TranscriptionToTextDelegate} - {@link #setTranscriptionToTextDelegate(TranscriptionToTextDelegate)}</dt>
 * <dd>This delegate allows the programmer to write their own conversion routine by7
 * providing their own {@link TranscriptionToTextDelegate#getTextForTranscriptionElement(NOMElement)}</dd>
 * <dt>Attribute - {@link #setTranscriptionAttribute(String)}</dt>
 * <dd>If the TranscriptionToTextDelegate is null, the attribute on the NOMElement whose name
 * matches this string will be returned, if it exists.</dd>
 * <dt>Text Content</dt>
 * <dd>If both the TranscriptionToTextDelegate and the Attribute are null, the text returned
 * by {@link NOMElement#getText()} will be returned. This is the default.</dd>
 * </dl>
 * 
 * @author Craig Nicol
 *
 */
public class ElementToTextDelegate {
	/********************************************************************************
	 * 
	 * Get text from NOMElement
	 * 
	 ********************************************************************************/
	
	/**
	 * Construct a default delegate that will return the text content of an element.
	 */
	public ElementToTextDelegate() {
		// Allow default construction
	}
	
	/**
	 * Create a new ElementToTextDelegate from a custom TranscriptionToTextDelegate.
	 * @param transToTextDelegate the custom delegate to use
	 */
	public ElementToTextDelegate(TranscriptionToTextDelegate transToTextDelegate) {
		this.transToTextDelegate = transToTextDelegate;
	}
	
	/**
	 * Create a new ElementToTextDelegate using a specified attribute.
	 * @param transcriptionAttribute
	 */
	public ElementToTextDelegate(String transcriptionAttribute) {
		this.transcriptionAttribute = transcriptionAttribute;
	}	
	
    /**
     * See the documentation of the method 
     * {@link #setTranscriptionToTextDelegate setTranscriptionToTextDelegate}
     */
    protected TranscriptionToTextDelegate transToTextDelegate = null;
    /**
     * There are three ways to determine the text that should be used to represent a certain
     * element <code>tle</code> from the transcription layer.
     * <p>The one with the highest precedence is the TranscriptionToTextDelegate. If this parameter is set,
     * the text for element <code>tle</code> is determined by a call to 
     * {@link TranscriptionToTextDelegate#getTextForTranscriptionElement getTextForTranscriptionElement}.
     * <br>The one with the second-highest precedence is the parameter {@link #transcriptionAttribute}.
     * If there is no TranscriptionToTextDelegate set, and this parameter is non-null, the text for 
     * element <code>tle</code> is determined by the value of attribute 'transcriptionAttribute' of
     * that element.
     * <br>If both above parameters are not set, the text representation of element <code>tle</code>
     * is taken to be the text content of the element.
     * <p>
     * In order to make the text of a transcription element available independent of these different
     * styles of derivation the method {@link #getTranscriptionText} is used.
     */
    public void setTranscriptionToTextDelegate(TranscriptionToTextDelegate newDelegate) {
        transToTextDelegate = newDelegate;
    }
    /**
     * See the documentation of the method 
     * {@link #setTranscriptionToTextDelegate setTranscriptionToTextDelegate}
     */
    protected String transcriptionAttribute = null;
    /**
     * See the documentation of the method 
     * {@link #setTranscriptionToTextDelegate setTranscriptionToTextDelegate}
     */
    public void setTranscriptionAttribute(String newName) {
        transcriptionAttribute = newName;
    }
    /**
     * See the documentation of the method 
     * {@link #setTranscriptionToTextDelegate setTranscriptionToTextDelegate}
     */
    public String getTranscriptionAttribute() {
        return transcriptionAttribute;
    }

    /**
     * This method returns the transcript text of a certain transcription element. 
     * See {@link #transcriptionAttribute} for information on how that text is derived.
     * <p>
     * This method should be used whenever the text content for a certain transcription element is needed.
     * @param nme The NOMElement containing transcription data. 
     * @throws IllegalArgumentException If called with wrong TYPE of element (not in transcription layer),
     * an IllegalArgumentException is thrown.
     * @throws NoSuchElementException : If the given element does not contain the right attribute
     * {@link #transcriptionAttribute} when that attribute is needed, a NoSuchElementException is thrown.
     */
    public String getTranscriptionText(NOMElement nme) {
        /* if (!isTranscriptionElement(nme)) {
            throw new IllegalArgumentException("getTranscriptionText should only be called for transcription elements of the right type. Correct layer: " + transLayerName + "; Used type: " + nme.getName());
        } */
        if (transToTextDelegate != null) {
            return transToTextDelegate.getTextForTranscriptionElement(nme);
        } else if (transcriptionAttribute == null || transcriptionAttribute == "") {
            return nme.getText();
        } else {
            String result = (String)nme.getAttributeComparableValue(transcriptionAttribute);
            if (result == null) {
                throw new NoSuchElementException("Can't find attribute " 
                                                + transcriptionAttribute 
                                                + " on transcription element");
            }
            return result;
        }
    }

}
