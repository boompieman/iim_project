/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson, Dennis Reidsma
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.textviewer;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Comparator;
import java.util.Collection;
import java.util.Enumeration;

import javax.swing.text.*;
import javax.swing.undo.*;
import javax.swing.event.*;
import javax.swing.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

import net.sourceforge.nite.query.QueryResultHandler;
import net.sourceforge.nite.query.SimpleQueryResultHandler;
import net.sourceforge.nite.nxt.ObjectModelElement;
import net.sourceforge.nite.nxt.NOMObjectModelElement;
import net.sourceforge.nite.nom.nomwrite.NOMElement;
import net.sourceforge.nite.time.*;
import net.sourceforge.nite.util.*;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.*;


/**
 * The basic NITE GUI element used to display text - add text to the
 * TextArea using addElement(NTextElement element). This GUI element
 * can respond to timing highlights as well as query highlights and
 * also implements the Printable interface. 
 * <p>
 * <h2>Selection, highlighting and time aligned replay</h2>
 * The selection and highlighting mechanisms of the NTextArea are composed of
 * several parts.
 * <br><b>Caret tracking</b>:
 * As a descendant of JTextPane, the NTextArea uses a Caret for tracking cursor 
 * positions (influenced by arrow keys and mouse clicking and dragging). NTextArea 
 * uses an NTACaretListener to keep track of those cursor changes. 
 * Whenever the selection span changes, there are two possible reactions:
 * Ctrl was NOT pressed, the method newSelection will be called, causing a change 
 * in the currently selected elements.
 * Ctrl was pressed, the method newTimeSelection will be called, causing a replay
 * of the media aligned with the selected elements.
 * <br><b>Interpreting user selections</b>:
 * When the selection changes, the NTextArea will reinterpret the selection span to
 * determine which NTextElements were really selected. Those will then be highlighted.
 * E.g. if you click on one NTextElement, that will be highlighted. If you partially 
 * select several NTextElements, all of those elements will be fully highlighted and 
 * selected.
 * <br><b>Highlighting functionalities</b>:
 * There are four independent types of highlighting in NTextArea, that are 
 * defined by integer type constants:
 * <ul><li>SELECTION_HIGHLIGHTS are connected to GUI selections
 * in the text area described above.
 * <li>TIME_HIGHLIGHTS are highlights caused by time alignment.
 * <li>QUERY_HIGHLIGHTS are caused by displaying query results on the
 * NTextArea.
 * <li>USER_HIGHLIGHTS allow programmers to define their own highlightings
 * for displaying some custom information (see also 'Defining your own highlighting').
 * </ul>
 * <br><b>Time and query highlighting</b>:
 *
 * <br><b>Retrieving selection information</b>:
 * Information about current highlights (and therefore also selections) can be
 * retrieved using {@link #getHighlightedTextElements(int type)}.
 * The methods getSelectedNOMElementsOrdered, getSelectedTextElements,
 * getSelectedElements and getSelectedElementsOrdered give a more
 * flexible access to the selection information, though they will be renamed in the future to
 * a more consistent set of function calls.
 * <br> Furthermore, NTextArea offers the possibility to register an {@link
 * NTASelectionListener}. Whenever the selection of the NTextArea changes, e.g. through
 * mouse dragging, the NTASelectionListeners are notified of the change. They can then
 * call the above methods and react to the new selections. This only works for 
 * selections, and not for time or query highlights.
 * <br><b>Defining your own highlightings</b>:
 * If you want to display your own highlights for one reason or another, the easiest way 
 * is to us setHighlighted(NTextArea.USER_HIGHLIGHTS, element). If you want to use various
 * colours in your custom highlighting (for example to highlight the source and target element
 * of some link), you can use setHighlighted(NTextArea.USER_HIGHLIGHTS, element, templatestyle),
 * passing different styles for the different highlight colours. To remove those user highlights
 * call {@link #clearHighlights(int type) clearHighlights(NTextArea.USER_HIGHLIGHTS)}
 * The highlights are stored in TreeSets ordered by the transcript order. If you're planning to have highlights of 2000 or more elements, this will cause performance degradation, and you need to either reconsider the interface, or implement a different highlighting mechanism.
 */
public class NTextArea extends JTextPane
    implements net.sourceforge.nite.time.ScrollingTimeHandler, 
               QueryResultHandler, Printable, SimpleQueryResultHandler {


/*
TODO:
make a central 'static registry' of style-prefixes, to detect and avoid prefix name clashes.
*/

/* TODO
DR 26.11.04
DR: There are a lot of calls to setSelectionStart(pos) made to avoid hangups 
when inserting or deleting within the caret range. Wrt these calls the following
points should be considered:
1) are there more places where this should happen? When we forget one, it may lead
to an hangup in one of the fireUpdate methods in NITESTyledDocument (fireRemoveUpdate etc)
2) we should consider whether we want the caret to ALWAYS jump to those insertion and deletion 
points, or only if it was inside an affected area.
*/

    protected NITEStyledDocument defaultDoc;
    private JComponent status;
    private Clock niteclock;
    private Color qcolor = Color.lightGray;
    private Color ccolor = new Color(200, 255, 200);
    private static final int SCROLL_MARGIN=50;
    private boolean auto_scrolling=true;
    private double maxtime = NTextElement.UNTIMED;

    NTACaretListener caretListener;//[dr: proper naming and documentation!

    /**
     * Maintains a mapping between object model elements and text elements
     */
    private Map dataToTextElement;
    /**
     * Maintains a mapping between object model elements and text
     * elements for edited elements
     */
    protected Map dataEditToTextElement;
 
    /**
     * The font styles which will be used in the document displayed by
     * this NTextArea. 
     */
    protected StyleContext styles = new StyleContext();

    //[DR: watchdog. The given situation has been known to occur, and I want to know when exactly.
    public void addCaretListener(CaretListener newl) {
        //System.out.println("Adding caret: " + newl);
        //System.out.println("current list:  " + getCaretListeners().length);
        for (int i = 0; i < getCaretListeners().length; i++) {
            //System.out.println(getCaretListeners()[i] + "; equal="+(getCaretListeners()[i]==newl));
            if (getCaretListeners()[i]==newl) {
                System.out.println("A caretlistener has just been added for the second time. This is not supposed to happen!");
                //to test WHEN this happens, make the above a runtimeexception instead of a system.out
            }
        }
        super.addCaretListener(newl);
    }




    /**
     * create a screen area for the display of textual information
     * with syncing-to-signal and search highlighting functionality.
     **/
    public NTextArea() {
        super();
        super.setEditable(false);

        dataToTextElement = new TreeMap();
        dataEditToTextElement = new TreeMap();
        defaultDoc = new NITEStyledDocument((JTextPane)this, styles);
        setStyledDocument(defaultDoc);
        gotoDocumentStart();

        getDocument().addUndoableEditListener(undoHandler);
        //this tracker will be responsible for passing caret ('extended cursor') changes to the NTA
        caretListener = new NTACaretListener(this);
    }
    
    /*-----------------------------------------*/
    /* SimpleQueryResultHandler implementation */
    /*-----------------------------------------*/
    public void acceptResults(List results) {
        handleQueryResults(results);
    }

    /*-----------------------------------*/
    /* QueryResultHandler implementation */
    /*-----------------------------------*/
    /** display query results (arg should be a list of NOMElements).
     * Old query highlights are removed. New results are
     * highlighted. Afterwardss, getHighlightedTextElements(QUEYR_HIGH) contains
     * the corresponding text elements. */
    public void acceptQueryResults(List results) {
        handleQueryResults(results);
    }

    /** display query result 
    Old query highlights are removed. New results are highlighted. Afterwardss, getHighlightedTextElements(QUEYR_HIGH) contains 
    the corresponding text elements. */
    public void acceptQueryResult(NOMElement result) {
        handleQueryResults(Collections.singletonList(result));
    }

    /** display query results */
    private void handleQueryResults(List results) {
        clearHighlights(QUERY_HIGHLIGHTS);
        if (results==null) { return; }
        highlightQueryResults(results);
    }

    /** display query result recursively WHAT MEANS RECURSIVELY?*/
    private void acceptQueryResultRecursive(NOMElement result) {
        if (result==null) { return; }
        setHighlighted(QUERY_HIGHLIGHTS, (ObjectModelElement)new NOMObjectModelElement(result));
        if (result.getChildren()!=null) {
            for (Iterator kit=result.getChildren().iterator(); kit.hasNext(); ) {
                acceptQueryResultRecursive((NOMElement)kit.next());
            }
        }
    }

    /**
     * Is the given query result relevant to this text area?
     * @param result the result.
     * @return <code>true</code> if the result is relevant.
     */
    public boolean isResultRelevant(NOMElement result) {
    	return true;
    }

    /** display query results, including sub lists */
    private void highlightQueryResults(List results) {
	for (Iterator rit=results.iterator(); rit.hasNext(); ) {
	    Object result = rit.next();
	    if (result instanceof NOMElement) {
		NOMElement element = (NOMElement) result;
		if (isResultRelevant(element)) {
		    acceptQueryResultRecursive(element);
		}
	    } else if (result instanceof List) {
		List subList = (List) result;
		highlightQueryResults(subList);
	    }
        }
    }

    /** set the colour of the highlighting for queries.
    IGNORED. use setHighlightingStyle */
    public void setQueryHighlightColor(Color color) {
        qcolor=color;
    }

    /*------------------------------------------*/
    /* END OF QueryResultHandler implementation */
    /*------------------------------------------*/


    /*=================================================================*/
    /* SCREEN NAVIGATION AND DISPLAY                                   */
    /*=================================================================*/

    /** Print the document if an print service can be found - winds up
        calling the print method in the NITEStyledDocument. */
    public void printTextArea() {
        try {
            PrinterJob printJob = PrinterJob.getPrinterJob();
            printJob.setPrintable(this);
            // these two lines interfere with the paper size setting 
            // HashPrintRequestAttributeSet hpras = new HashPrintRequestAttributeSet();
            // hpras.add(new PageRanges(1,1));

            // this line has the purpose of finding the default page style
            // it seems to always default back to letter without it. Duh.
            PageFormat pf = printJob.pageDialog(printJob.defaultPage());
            if (!printJob.printDialog()) 
                return;
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            printJob.print(); 
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        } catch (PrinterException exc) {
            exc.printStackTrace();
        }
    }
    
    
    /*-------------------------------------------------*/
    /* java.awt.print.Printable implementation         */
    /*-------------------------------------------------*/
    /** print the document: calls the print method in NITEStyledDocument. */
    public int print(Graphics g, PageFormat pf, int pi) throws PrinterException {
        if (defaultDoc==null) return NO_SUCH_PAGE;
        return defaultDoc.print(g, pf, pi);
    }
    /*------------------------------------------------*/
    /* end of java.awt.print.Printable implementation */
    /*------------------------------------------------*/

    /** clear the display and (re)initialise all indexes */
    public void clear() {
        defaultDoc = new NITEStyledDocument((JTextPane)this, styles);
        setStyledDocument(defaultDoc);
        gotoDocumentStart();

        getDocument().addUndoableEditListener(undoHandler);
        dataToTextElement = new TreeMap();
        dataEditToTextElement = new TreeMap();
        //DR: added 15.11.2004. Somehow the caret (cursor) is made invisible for every window-focus-change. 
        //And there is still something strange going on, since the blink is switched off now....
        //I wanted to see the cursor for figuring out some keyboard navigation things...
        setCaret(new DefaultCaret() {
            public void focusLost(FocusEvent f) {
                super.focusLost(f);
            }
            public void focusGained(FocusEvent f) {
                super.focusGained(f);
                setVisible(true);
            }
        });
        getCaret().setVisible(true);
        getCaret().setBlinkRate(900);
    }

    /* clear query and time highlights on the display */
    //DR: not used anymore.
    /*public void clearHighlights() {
        setSelectionEnd(getSelectionStart());
        Highlighter h = getHighlighter();
        if (h!=null) { h.removeAllHighlights(); }
    }*/


    /** Jean's position comparator inner class used in the
     * newSelection code */
    private class PositionComparator implements Comparator {
        //DR: why is this not a separate class, likeall the other comparators?
        public int compare( Object o1, Object o2 ) {
            NTextElement e1 = (NTextElement)o1;
            NTextElement e2 = (NTextElement)o2;

            if( e1 == null && e2 == null ) {
                return 0;
            } else if( e1 == null ) {
                return -1;
            } else if( e2 == null ) {
                return 1;
            } else if( e1.equals( e2 )) {
                return 0;
            } else {
                int pos1 = e1.getPosition();
                int pos2 = e2.getPosition();

                if (pos1== pos2) {
                        return 0;
                } else if (pos1 < pos2) {
                        return -1;
                } else { //pos2 > pos1
                        return 1;
                }
            }
        }
    }

//====================================================================================
//              GUI BASED SELECTION
//====================================================================================

    /*--- selection listener support ---*/
    
    protected EventListenerList listenerList = new EventListenerList(); 
    public void addNTASelectionListener (NTASelectionListener newListener) {
        listenerList.remove(NTASelectionListener.class, newListener); 
        listenerList.add(NTASelectionListener.class, newListener); 
    }
    public void removeNTASelectionListener (NTASelectionListener listener){
        listenerList.remove(NTASelectionListener.class, listener);
    }
    protected void notifyNTASelectionListeners(){
        // Process the listeners last to first, notifying 
        // those that are interested in this event 
        Object[] listeners = listenerList.getListenerList(); 
        for (int i = listeners.length-2; i>=0; i-=2) { 
            ((NTASelectionListener)listeners[i+1]).selectionChanged();
        }
    }
    /** 
     * WRITE BETTER DOCUMENTATION HERE! THIS IS OBSCURE
     * This latest version extends the highlighting to NOMObjectModelElements
     * instead of NTextElements.
     * This method gets called whenever the user clicks or drags a new selection
     * in the NTextArea.
     * <p>
     * The superclass (JTextPane) uses a Caret for keeping track of GUI manipulations 
     * that manipulate the cursor in text, such as click or drag. 
     * The NTASelectionTracker of this NTextArea keeps track of changes in the caret.
     * Whenever the caret changes (i.e. the user wants to change the selection), the
     * tracker calls this newselection method. We usually want to select only 
     * complete NTextElemetns, even if the user dragged only a few words or characters.
     * The newSelection method interprets the caret span, determines which NTextElements should 
     * be considered 'selected', takes care of proper highlighting and makes sure that 
     * the relevant elements are made available as being 'selected' (getSelectedTextElements
     * and getSelectedElements). [DR: this is changed into getHighlightedTextElements
     * NB: the Caret that originally caused this call is left unchanged!!! this means that the Caret
     * does NOT always reflect the exact selection and highlighting information!
     *
     */
    public void newSelection(int dot, int mark) {
        // we have a new selection, so let's remove all the old ones
        getHighlighter().removeAllHighlights(); // this makes the original caret selection invisible again
        clearHighlights(SELECTION_HIGHLIGHTS);
        NTextElement startel;

        if (dot == mark) {
            //just a single cursor, so extend the highlight to the rest of this textelement
            startel = defaultDoc.getElementAtPosition(dot);
            if (startel == null) {
                if (dot != 0)System.out.println("!Null element at position "+ dot +" in " + this.getClass());
                //this one occurs when NTextArea.clear() was called. The caret gets an update to (0,0)
                //but there is no content in the TNextArea, so startel == null
            } else {
                ObjectModelElement objel = startel.getDataElement();
                if (objel != null) {
                    setHighlighted(SELECTION_HIGHLIGHTS, objel);
                } else {
                    setHighlighted(SELECTION_HIGHLIGHTS, startel);
                }
            }
        } else {
            Set s = null;
            if (dot < mark) {
                s = defaultDoc.getElementsBetweenPositions(dot, mark);
            } else if (dot > mark) {
                //a selection, make sure all text elements which lie between these positions are highlighted
                s = defaultDoc.getElementsBetweenPositions(mark, dot);
            }
            // JEANC - This previously used IDComparator, which does
            // string comparison on ids.  That's no good because we
            // want the last NTextElement positionally to be the last
            // here - setHighlighted below calls updateSelection,
            // which gets the correct effect by checking whether the
            // positions of the NTextElement the call results from
            // abuts the current selection (in which case it extends
            // the selection rather than replacing it).  We don't
            // guarantee id order (and in canonical order, e.g. s5_10
            // is before s5_9).  Changing to use a comparator based on
            // position.  I think this is safe (all NTextElements have
            // a position).
            TreeSet sorted = new TreeSet( new PositionComparator() );
            sorted.addAll( s );

            Iterator it = sorted.iterator();
            while (it.hasNext()) {
                startel = (NTextElement) it.next();
                ObjectModelElement objel = startel.getDataElement();
                if (objel != null) {
                    setHighlighted(SELECTION_HIGHLIGHTS, objel);
                } else {
                    setHighlighted(SELECTION_HIGHLIGHTS, startel);
                }
            }
        }
        notifyNTASelectionListeners();
    }

    //        What if one of the selected elements did not have a start or
    //        end time? If this is the case for the first element, hunt
    //        for text element to the right instead. If it was the case
    //        for the second el, hunt for elements to the left instead.

    private NTextElement findFirstGenuineTimedDataRight(int startpos, int endpos) {
        NTextElement start = null;
        // FIX ME - This is ineffcient. Should iterate over known text
        // elements rather than character positions

        // jonathan changed this from '<' to '<=' as some timed
        // elements were being missed 5.11.4.
        // for (int i = startpos; i < endpos; i++) {
        for (int i = startpos; i <= endpos; i++) {
            start = defaultDoc.getElementAtPosition(i);
            if (start.getStartTime() != NTextElement.UNTIMED){
                return start;
            }
        }
        return start;
    }

    private NTextElement findFirstGenuineTimedDataLeft(int startpos, int endpos) {
        NTextElement end = null;
        // FIX ME - This is ineffcient. Should iterate over known text
        // elements rather than character positions

        // jonathan changed this from '>' to '>=' as some timed
        // elements were being missed 5.11.4.
        for (int i = endpos; i >= startpos; i--) {
            end = defaultDoc.getElementAtPosition(i);
            if (end!=null && end.getStartTime() != NTextElement.UNTIMED){
                return end;
            }
        }
        return end;
    }

    // FIX ME -
    // this is extremely wasteful if the document has no timed data at
    // all. Should maybe have a global varibale to indicate whether
    // the data is timed or not
    /** the user has highlighted a bit of text while holding down
     * control, so reset the time on the clock to the timing data
     * stored with the data corresponding to the selection */
    public void newTimeSelection(int dot, int mark) {
        int start = Math.min(dot, mark);
        int end = Math.max(dot, mark);
        NTextElement startel, endel;
        startel = findFirstGenuineTimedDataRight(start, end);
        //jonathan changed this to use 'end' not 'end-1' as it was
        //failing to go to any timed element when one was highlighted
        // endel = findFirstGenuineTimedDataLeft(start, end-1);
        endel = findFirstGenuineTimedDataLeft(start, end);
        if (startel != null && endel != null) {
                if (niteclock == null) { //DR added check 15.11.2004. The clock is not necessarily known :-)
                    System.out.println("");
                } else {
                    niteclock.setTimeSpan(startel.getStartTime(), endel.getEndTime(),
                                  (ScrollingTimeHandler) this);
                }
        }
    }

    /** scroll the display so that the given NTextElement is in the middle */
    /*public void centre(NTextElement el) {
        //DR remark: how does this relate to scrollto?
        if (el==null) { return; } 
        try {
            int start = defaultDoc.getTextElementPosition(el);
            Rectangle visible = getVisibleRect();
            Rectangle dest = modelToView(start);
            dest.y += (dest.height - visible.height) / 2;
            dest.height=visible.height;
            scrollRectToVisible(dest);
        } catch (BadLocationException ble) {
            ble.printStackTrace();
        }
        }*/

    /** Scroll to the start of the displayed text */
    public void gotoDocumentStart() {
        
        System.out.println(""); // WHY??? [DR: some timing or flush problem. 
                                // If I don't do this, I sometimes get indexoutofboundsexceptions
                                // here when NTA.clear was called. Seems to be a bug in Swing.
        
	CaretListener[] carets=getCaretListeners();
	for (int i=0;i<carets.length;i++)
	    removeCaretListener(carets[i]);
	try {
	    setCaretPosition(0);
	} catch (IllegalArgumentException illex) {
	    System.out.println("Could not scroll to start!");
	}
	for (int i=0;i<carets.length;i++)
	    addCaretListener(carets[i]);

	/*

	This is what the method used to look like, but it didn't work at all for me.
	Maybe there are problems with this fix? Works on generic display for example
	Jonathan 24/4/2006.

        try {
            Rectangle r = modelToView(1);
            if (r!=null) { 
		System.out.println("SCROLLING 1");
                scrollRectToVisible(r);
            } else {
		System.out.println("r is null");
	    }
        } catch (BadLocationException be) {
            System.err.println(
                "Could not change the viewport to show new highlight");
            be.printStackTrace();
        }

	*/
    }

    /** Scroll to the end of the displayed text */
    public void gotoDocumentEnd() {
        
	CaretListener[] carets=getCaretListeners();
	for (int i=0;i<carets.length;i++)
	    removeCaretListener(carets[i]);
	try {
	    setCaretPosition(getText().length()-1);
	} catch (IllegalArgumentException illex) {
	    System.out.println("Could not scroll to end!");
	}
	for (int i=0;i<carets.length;i++)
	    addCaretListener(carets[i]);

    }

    /** Chopped out of the old setTimeHighlighted to scroll the window
     * to highlight a particular element */
    public void scrollTo(NTextElement el) {
        if (!auto_scrolling) { return; }
        try {
            String original = el.getText();
            int realStart = defaultDoc.getTextElementPosition(el);
            int realEnd = realStart + original.length();
            int curx, cury;
            Rectangle visible = getVisibleRect();
            Rectangle sdest = null;
            Rectangle edest = null;
            try {
                sdest = modelToView(realStart);
                edest = modelToView(realEnd);
            } catch (BadLocationException ex) {
                System.err.println("Could not change the viewport to show new time highlight");
                ex.printStackTrace();
                return; 
	    } catch (Exception exc) {
		// modelToView can throw an ArrayOutOfBounds
                System.err.println("Could not change the viewport to show new time highlight (" + realStart + ", " + realEnd + ").");
                exc.printStackTrace();
                return; 		
	    }
            // jonathan 26/4/4
            // centering was too jumpy - now only move if necessary
            // if we're scrolling down, make this plus the next 3
            // lines visible. Otherwise this and the previous 3
            if (edest!=null && sdest!=null) {
                Rectangle dest=null;
                if (visible.getY()<sdest.getY()) {
                    dest=edest;
                    cury=(new Double(dest.getY())).intValue()+SCROLL_MARGIN;
                    curx=(new Double(dest.getX())).intValue();
                    dest.setLocation(curx, cury);
                } else {
                    dest=sdest;
                    cury=(new Double(dest.getY())).intValue();
                    curx=(new Double(dest.getX())).intValue();
                    if (cury<=SCROLL_MARGIN) { cury=0; } else { cury-=SCROLL_MARGIN; }
                    dest.setLocation(curx, cury);
                }
                if (dest!=null) {
                    //System.out.println("Scrolling to " + dest.getX() + "; " + dest.getY() + "; vis: " + visible.getX() + "; " + visible.getY() + "; w: " + visible.getWidth() + ";h: " + visible.getHeight());
                    // this is an odd one, but if we try to scroll to
                    // where we are already, Swing sometimes hangs! Jonathan.
                    double yt = visible.getY();
                    double yb = visible.getY() + visible.getHeight();
                    if (dest.getY()<yt || dest.getY()>yb) { 
                        scrollRectToVisible(dest);
                    }
                }
            }            
        } catch (Exception npe) {
	    System.err.println("Could not change the viewport to show new time highlight");
            return;
	}
        //System.out.println("scrolled" + Math.random());
    }


    /*=================================================================*/
    /* END SCREEN NAVIGATION CODE                                      */
    /*=================================================================*/


    /*=================================================================*/
    /* HIGHLIGHTING                                                    */
    /*=================================================================*/

    /** Set a highlight on an ObjectModelElement in the given
     * colour. This should now be done using Styles rather than a real
     * Swing Highlighter
     */
    // reimplement? remove?
 /*   public void setHighlighted(ObjectModelElement startel, Color c) {
        if (startel==null) { return; }
        if (c==null) { c=MetalLookAndFeel.getTextHighlightColor(); }
        Set displays = (Set) dataEditToTextElement.get(startel.getID());
        if (displays == null) { return; }
        boolean first=true;
        Iterator it = displays.iterator();
        while (it.hasNext()) {
            NTextElement el = (NTextElement) it.next();
            if (el != null) {
                String original = el.getText();
                int realStart = defaultDoc.getTextElementPosition(el);
                int realEnd = realStart + original.length();
                CaretListener[] carets=getCaretListeners();
                for (int i=0;i<carets.length;i++)
                    removeCaretListener(carets[i]);
                try {
                    updateSelection( realStart, realEnd );
                    Highlighter h = getHighlighter();
                    h.addHighlight(realStart, realEnd,
                                   new DefaultHighlighter.DefaultHighlightPainter(c));
                } catch (BadLocationException b) {
                    b.printStackTrace();
                }
                try {
                    Rectangle dest = modelToView(realEnd);
                    if (dest != null) {
                        scrollRectToVisible(dest);
                    }
                } catch (BadLocationException be) {
                    System.err.println("Could not change the viewport to show new highlight");
                    be.printStackTrace();
                }
                for (int i=0;i<carets.length;i++)
                    addCaretListener(carets[i]);
            }
        }
    }*/

    /** Set a highlight on a specific NTextElement in the given
     * colour. This should now be done using Styles rather than a real
     * Swing Highlighter*/
    // remove? reimplement?
 /*   public void setHighlighted(NTextElement startel, Color c) {
        if (startel != null) {
            if (startel.getEditElement() != null) {
                Set displays = (Set) dataEditToTextElement.get(startel.getEditElement().getID());
                if (displays != null) {
                    Iterator it = displays.iterator();
                    while (it.hasNext()) {
                        NTextElement el = (NTextElement) it.next();
                        if (el != null) {
                            //don't remove the white space
                            String original = el.getText();

                            int realStart =
                                defaultDoc.getTextElementPosition(el);

                            int realEnd = realStart + original.length();
                            // have to remove the caret listener before changing 
                            // the selection unless we want a loop
                            CaretListener[] carets=getCaretListeners();
                            for (int i=0;i<carets.length;i++)
                                removeCaretListener(carets[i]);
                            try {
                                updateSelection( realStart, realEnd );
                                Highlighter h = getHighlighter();
                                h.addHighlight(realStart, realEnd,
                                    new DefaultHighlighter.DefaultHighlightPainter(c));
                            } catch (BadLocationException b) {
                                b.printStackTrace();
                            }
                            try {
                                Rectangle dest = modelToView(realEnd);
                                if (dest!=null) {
                                    scrollRectToVisible(dest);
                                }
                            } catch (BadLocationException be) {
                                System.err.println(
                                    "Could not change the viewport to show new highlight (" + realEnd + ")");
                                //     be.printStackTrace();
                            } 
                            for (int i=0;i<carets.length;i++)
                                addCaretListener(carets[i]);
                        }
                    }
                }
            }
        }

    }*/

    /** clear the query result highlights */
   // DR: reimplemented: since we dont use highlighter at the moment, clearHighlights(type) is used.
  /* private void clearQueryHighlights() {
        Highlighter high = getHighlighter();
        if (high.getHighlights().length!=0) {
            high.removeAllHighlights();
        }
    }*/

    /** Highlight a query result.  */
   // DR: reimplemented: since we dont use highlighter at the moment, setHighlighted(type, element) is used.
   // for changing colour, set the highlight style templates
    //only the centreToNTextelement should still be fixed?
/*    private void setQueryHighlighted(NTextElement el, Color resource) {
        if (el != null) {
            String original = el.getText();
            int realStart = defaultDoc.getTextElementPosition(el);
            int realEnd = realStart + original.length();
            //            Caret caret = getCaret();
            CaretListener[] carets=getCaretListeners();
                for (int i=0;i<carets.length;i++)
                    removeCaretListener(carets[i]);
            Highlighter high = getHighlighter();
            try {
                setSelectionStart(realStart);
                setSelectionEnd(realEnd);
                high.addHighlight(realStart, realEnd,
                  new DefaultHighlighter.DefaultHighlightPainter(resource));
            } catch (BadLocationException b) {
                b.printStackTrace();
            }

            try {
                // attempt to centre start of query result
                // jonathan 12.2.04
                Rectangle visible = getVisibleRect();
                Rectangle dest = modelToView(realStart);
                if (dest!=null) {
                   dest.y += (dest.height - visible.height) / 2;
                   dest.height=visible.height;
                   scrollRectToVisible(dest);
                }
            } catch (BadLocationException be) {
                System.err.println(
                    "Could not change the viewport to show new time highlight");
                be.printStackTrace();
            }

            for (int i=0;i<carets.length;i++)
                    addCaretListener(carets[i]);
           
        }
    }*/

    /** Highlight a query result.  */
    //DR not used anymore.
  /*  private void setQueryHighlighted(ObjectModelElement el) {
        Set displays = (Set) dataToTextElement.get(el.getID());
        if (displays == null) { return; }
        for (Iterator elit=displays.iterator(); elit.hasNext();) {
            NTextElement nte = (NTextElement)elit.next();
            setQueryHighlighted(nte, qcolor);
        }
    }*/
    
    /** return a set of NTextElements which appear in the span */
    public Set getElementsBetweenTimes(double start, double end) {
        Set elements=null;
        if (start==end) {
            elements = defaultDoc.getElementsAtTime(start);
        } else {
            elements = defaultDoc.getElementsBetweenTimes(start,end);
        }        
        return elements;
    }

    /** Turn off scrolling to the appropriate place in the NTextArea
     * when highlights are applied. This is mainly to get round a
     * (Swing?) bug which ocassionally happens: scrollRectToVisible
     * seems to hang and doesn't raise an exception. */
    public void setAutoScroll(boolean scrolling) {
        auto_scrolling=scrolling;
    }

  
    /**
     * time alignment is reimplemented in this
     * class using a <i>time alignment style</i>.  <p> The actual work
     * is done by setTimeAlignmentStyle and showTimeSpan <p> Note the
     * following: If we align using a style, and the time alignment
     * should be removed again, we do want to remove the style, but we
     * do NOT want to remove possible styles that were present on the
     * text before alignment. So we cannot just set an attribute such
     * as ITALIC to true, and set it to false again afterwards.  So
     * what we do is the following. Internally we create a Style
     * object used as "rubber stamp template". When a certain
     * NTextElement should be highlighted, we copy this style giving
     * it a unique name, give it as parent the current style of that
     * NTextElement, and set this style as the new style of the
     * NTextElement. When the timealignment is removed for this
     * element, we just remove that new style again from the chain of
     * styles for the NTextElement.  <p> One problem though: other
     * components may have added yet more style-children. So we cannot
     * just take the first style, remove it and use its parent again;
     * we need to search through the style-chain upwards to find the
     * correct style. For this the time-alignment style for the
     * NTextElement should have a wel-defined name, e.g. a unique ID
     * prefixed by TIME_ALIGNMENT_STYLENAME.  <p> To keep track of the
     * highlighted NTextElements we store them in a Set.
     */
    public void showTimeSpan(double start, double end) {
        //find newAlignedElements, the NTextElements correlating to this time-span.
        Set newAlignedElements;
        if (start==end) {
            newAlignedElements = defaultDoc.getElementsAtTime(start);
        } else {
            newAlignedElements = defaultDoc.getElementsBetweenTimes(start,end);
        }

        //remove highlights from those elements that are not in this set
        NTextElement next = null;
        Iterator it = new HashSet(highlights[TIME_HIGHLIGHTS]).iterator(); //make copy, or otherwise we get concurr Mod Exc
        
        while (it.hasNext()) {
            next = (NTextElement)it.next();
            if (!newAlignedElements.contains(next)) {
                removeHighlighted(TIME_HIGHLIGHTS, next);
            }
        }

            next=null;

        // add the time alignment for the elements in
        // newAlignedElements that are not in highights[TIME_HIGHLIGHTS],
        // adding them to the highights[TIME_HIGHLIGHTS] set.
        it = newAlignedElements.iterator(); 
        while (it.hasNext()) {
            next = (NTextElement)it.next();
            if (!highlights[TIME_HIGHLIGHTS].contains(next)) {
                setHighlighted(TIME_HIGHLIGHTS, next);
            }
        }
        if (next != null)
                scrollTo(next);
    }


    /*-----------------------------------------*/
    /* TimeHandler implementation              */
    /*-----------------------------------------*/
    /** Accept a new time (generally from another registered
    TimeHandler) and highlight the appropriate elements. */
    public void acceptTimeChange(double t) {
        showTimeSpan(t,t);
    }

    /** Accept a new time span from another registered time
      handler and highlight the appropriate elements. */
    public void acceptTimeSpanChange(double start, double end) {
       showTimeSpan(start, end);
    }

    /** Broadcast a new time to any concurrent TimeHandlers */
    public void setTime(double t) {
        niteclock.setSystemTime(t);
    }

    /** Broadcast a new time span to any concurrent TimeHandlers */
    public void setTimeSpan(double start, double end) {
        niteclock.setTimeSpan(start, end);
    }

    /** Return the Clock that is currently syncronising this TimeHandler */
    public Clock getClock() {
        return niteclock;
    }

    /** Set the Clock to which this TimeHandler is registered */
    public void setClock(Clock clock) {
        if (clock != null) {
            this.niteclock = clock;
            niteclock.registerTimeHandler(this);
        }
    }

    /**
     * this method is NOT preferred: use setHighlightingStyle.
     * This method is kept for backwards compatibility.
     */
    public void setTimeHighlightColor(Color color) {
            if (color!=null) {
                ccolor=color;
                highlightingStyleTemplates[TIME_HIGHLIGHTS].addAttribute(StyleConstants.ColorConstants.Background, ccolor);
            }
    }
    /*-----------------------------------------*/
    /* end TimeHandler implementation          */
    /*-----------------------------------------*/

    /*===================================================================*/
    /* End of HIGHLIGHTING code                                          */
    /*===================================================================*/



    /*===================================================================*/
    /* DR: Start of experimental highlighting section                    */
    /* Types of highlighting:                                            */
    /*      SELECTION_HIGHLIGHTS                                         */
                //display highlights according to GUI text selection (click, drag)
    /*      TIME_HIGHLIGHTS                                              */
                //display highlights according to time alignment information
    /*      QUERY_HIGHLIGHTS                                             */
                //display highlights according to query result alignment
    /*      USER_HIGHLIGHTS                                              */
                //under documented :(
    /*===================================================================*/

   
    //========================
    // Methods that actually USE the highlighting
    //========================
        //this involves methods such as newSelection, showTimeSpan, showQueryHighlights, etc.
        //which are defined elsewhere in this class.

    //========================
    // Highlighting types
    //========================
      
        public static final int SELECTION_HIGHLIGHTS = 0;
        public static final int TIME_HIGHLIGHTS      = 1;
        public static final int QUERY_HIGHLIGHTS     = 2;
        public static final int USER_HIGHLIGHTS      = 3; //anything devised by programmer :)

    //========================
    // Internal storage of highlights 
    //========================

        /**
         * The sets of highlighted elements. Indexed with the highlighting type.
         */
        protected SortedSet[] highlights = {new TreeSet(new NTextElementPositionComparator()), 
                                            new TreeSet(new NTextElementPositionComparator()), 
                                            new TreeSet(new NTextElementPositionComparator()), 
                                            new TreeSet(new NTextElementPositionComparator())}; //add one if new highlighting type is introduced!
 
    //========================
    // Highlighting style templates
    //========================
       
        /**
         * The Styles used for displaying highlights, for each type of highlight.
         */
        protected Style[] highlightingStyleTemplates = createDefaultStyles();
        protected int[] style_idcounters = {0,0,0,0};
        /** 
         * Style names for the 3 highlighting style templates
         */
        protected static final String[] highlighting_stylenames = {"SELECTION_HIGHLIGHT","TIME_HIGHLIGHT","QUERY_HIGHLIGHT","USER_HIGHLIGHT"};
        /**
         * Construct a list of default highlighting styles.
         * Standard implementation makes three 'background highlighting styles' with different colours.
         */
        protected Style[] createDefaultStyles() {
            Style s0 = StyleContext.getDefaultStyleContext().addStyle(highlighting_stylenames[0],null);
            s0.addAttribute(StyleConstants.ColorConstants.Background, new Color(128,145,222));
            
            Style s1 = StyleContext.getDefaultStyleContext().addStyle(highlighting_stylenames[1],null);
            s1.addAttribute(StyleConstants.ColorConstants.Background, new Color(70, 160, 255));
            
            Style s2 = StyleContext.getDefaultStyleContext().addStyle(highlighting_stylenames[2],null);
            s2.addAttribute(StyleConstants.ColorConstants.Background, new Color(255, 140, 35));
            
            Style s3 = StyleContext.getDefaultStyleContext().addStyle(highlighting_stylenames[3],null);
            s3.addAttribute(StyleConstants.ColorConstants.Background, new Color(255, 255, 100));
            
            Style[] result = {s0, s1, s2, s3};
            return result;
        }
        /**
         * THIS is the method that you call if you want to have your own special
         * Style for showing highlighting. You are absolutely free in what the style
         * looks like: colour, underline, strikethrough, font....
         */
        public void setHighlightingStyle(int type, Style newStyle) {
            if (newStyle == null) {
                return;
            }
            highlightingStyleTemplates[type] = newStyle;
        }        
        
    //========================
    // Conceptually setting highlights 
    //========================
       
        //
          //kept for backwards compatibility. Will be phased out.
          //@deprecated
         //
  /*      public void clearHighlights() {
            if(true)throw new RuntimeException("asdf");
            System.out.println("calling obsolete method");
            clearHighlights(SELECTION_HIGHLIGHTS);
            clearHighlights(QUERY_HIGHLIGHTS);
            clearHighlights(TIME_HIGHLIGHTS);
            clearHighlights(USER_HIGHLIGHTS);
        }*/
        
        /**
         * Clears all highlights of the given type.
         * Type is one of xx, xx or xx.
         * Afterwards, the visual highlighting is removed and the set of highlighted elements
         * for the given type is empty.
         */
        public void clearHighlights(final int type) {
            //remove highlightings for the given set of highlighted elements
            Iterator textelIt = highlights[type].iterator();
            NTextElement nextTextel;
            Predicate isAlignStyleP = new Predicate() { //should we make these predicates static? saves time i suppose...
                public boolean valid(Object o) {
                    return ((Style)o).getName().startsWith(highlighting_stylenames[type]);
                }
            };
            while (textelIt.hasNext()) {
                nextTextel = (NTextElement)textelIt.next();
                removeStyleFromChain(nextTextel, isAlignStyleP);
            }
            //clear the set
            highlights[type].clear();
        }

        /**
        TO DOCUYMENT: SETHIGHLIGHTED WILL MOVE THE CARET!!!!!!!!!
        this means that you should be careful with setting highlights from your own caretlisteners, because they would get a loop-callback!
        *
         * Highlight the given element as 'highlighted' for the given type
         * of highlighting. Sets the visual highlighting for the given element.
         * This element will also be returned subsequently by a call to 
         * getHighlighted.
         * Type is ONE of xx,xx or xx.
         * PRE: element was NOT yet highlighted for that type!
         * <p>
         <b>following documetnation should be rewritten</b>
     * highlighted is reimplemented in this
     * class using a <i>highlighted style</i>.  <p> Note the
     * following: If we highlighted using a style, and the highlighted
     * should be removed again, we do want to remove the style, but we
     * do NOT want to remove possible styles that were present on the
     * text before highlighting. So we cannot just set an attribute such
     * as ITALIC to true, and set it to false again afterwards.  So
     * what we do is the following. Internally we create a Style
     * object used as "rubber stamp template". When a certain
     * NTextElement should be highlighted, we copy this style giving
     * it a unique name, give it as parent the current style of that
     * NTextElement, and set this style as the new style of the
     * NTextElement. When the highlight is removed for this
     * element, we just remove that new style again from the chain of
     * styles for the NTextElement.  <p> One problem though: other
     * components may have added yet more style-children. So we cannot
     * just take the first style, remove it and use its parent again;
     * we need to search through the style-chain upwards to find the
     * correct style. For this the highlighted style for the
     * NTextElement should have a wel-defined name, e.g. a unique ID
     * prefixed by some constant string prefix.  <p> To keep track of the
     * highlighted NTextElements we store them in Sets. 
     * <p>Note that the SelectionListeners are not notified.
         */
        public void setHighlighted(int type, NTextElement el) {
            setHighlighted(type,el, highlightingStyleTemplates[type]); //this method calls the one below! see there for how it works.
        }
        //normally, do sethigh(user, el) or sethigh(user, el, style) or sethigh(user, el, colour) or 
        
        public void setHighlighted(int type, NTextElement el, Style templatestyle) {
	    boolean scrolled=false;
	    
            if (highlights[type].contains(el)) {
                //System.out.println("Duplicate highlighting");
                //avoid duplicated highlighting
                return;
            }
            insertCopyOfStyle(el, templatestyle, highlighting_stylenames[type]+String.valueOf(style_idcounters[type]));
            style_idcounters[type]++;
            if (style_idcounters[type] == Integer.MAX_VALUE) {
                style_idcounters[type] = Integer.MIN_VALUE;
            }

            highlights[type].add(el);
            
            //DR 01.12.2004, caret will move with
            //sethighlighted(SELECTION_HIGHLIGHTS,..) calls It's a
            //slightly large piece of code, because we need to check
            //if the caretListener should be removed or not (and
            //therefore re-added afterwards or not)
            
            if ((type == SELECTION_HIGHLIGHTS) && (highlights[SELECTION_HIGHLIGHTS].size()>0)) {
                NTextElement first = (NTextElement)highlights[SELECTION_HIGHLIGHTS].first();
                if (first == el) {
                    CaretListener[] carets=getCaretListeners();
                    boolean removed = false;
                    for (int i=0;i<carets.length;i++) {
                        if (carets[i] == caretListener) {
                            removeCaretListener(carets[i]);
                            removed = true;
                        }
                    }
                    getCaret().setDot(first.getPosition());
		    // jonathan tried moving this to avoid deadlock??
		    scrollTo(el);
		    scrolled=true;
                    if (removed) {
                        addCaretListener(caretListener);
                    }
                }
            } 

	    // only scroll if we haven't scrolled above
	    if (!scrolled) {
		scrollTo(el);
	    }
            
        } 
        /**
         * Remove the highlights from the given element for the given highlighting type.
         */
        public void removeHighlighted(final int type, NTextElement el) {
            Predicate isAlignStyleP = new Predicate() { //make static predicates?
                public boolean valid(Object o) {
                    return ((Style)o).getName().startsWith(highlighting_stylenames[type]);
                }
            };
            removeStyleFromChain(el, isAlignStyleP);
            //clear the set
            highlights[type].remove(el);
        }
        

        /**
         * Highlight the given ObjectModelElement(typically
         * corresponding to a NOMElement) with a given highlight type
         * and default style
         */
        public void setHighlighted(int type, ObjectModelElement el) {
            Set displays = (Set) dataToTextElement.get(el.getID());
            if (displays == null) { return; }
            for (Iterator elit=displays.iterator(); elit.hasNext();) {
                NTextElement nte = (NTextElement)elit.next();
                setHighlighted(type, nte);
            }
        }

    /**
     * Highlight the given ObjectModelElement(typically corresponding
     * to a NOMElement) with a given highlight type, using the
     * specified style
     */
        public void setHighlighted(int type, ObjectModelElement el, Style templateStyle) {
            Set displays = (Set) dataToTextElement.get(el.getID());
            if (displays == null) { return; }
            for (Iterator elit=displays.iterator(); elit.hasNext();) {
                NTextElement nte = (NTextElement)elit.next();
                setHighlighted(type, nte, templateStyle);
            }
        }
        

        /*public void setHighlighted(int type, NTextElement el, Color c) {
            Style template = StyleContext.getDefaultStyleContext().addStyle(highlighting_stylenames[3],null);
            template.addAttribute(StyleConstants.ColorConstants.Background, c); maybe hold a static list of styles for the colors somewhere?
            insertCopyOfStyle(el, templatestyle, highlighting_stylenames[type]+String.valueOf(style_idcounters[type]));
            style_idcounters[type]++;
            if (style_idcounters[type] == Integer.MAX_VALUE) {
                style_idcounters[type] = Integer.MIN_VALUE;
            }
            highlights[type].add(el);
        }*/
        
        /**Passes on to setHighlighted(int type, NTextElement el)*/
        public void setHighlighted(int type, Set elements) {
            setHighlighted(type, elements.iterator());
        }
        /**Passes on to setHighlighted(int type, NTextElement el)*/
        public void setHighlighted(int type, Iterator elements) {
            while (elements.hasNext()) {
                NTextElement next = (NTextElement)elements.next();
                if (!highlights[type].contains(next)) {
                    setHighlighted(type, next);
                }
            }
        }
      
    /** A convenience method to highlight a given object model element
     * as if it was selected by the user. After call to this method, X
     * will also be returned by
     * getHighlightedTextElements(SELECTION_HIGHLIGHTS). Does NOT
     * adjust dot/mark position of the actual selection. Equivalent to
     * a call to setHighlighted(SELECTION_HIGHLIGHTS, X)" except that
     * the selection listeners are notified. **/ 
    public void setSelected(ObjectModelElement el) {
        setHighlighted(SELECTION_HIGHLIGHTS, el);
        notifyNTASelectionListeners();
    }

    /** A convenience method to clear all selection highlights. After
     * a call tot his method getHighlightedTextElements(SELECTION_HIGHLIGHTS) will
     * return an empty set. However, the default dot and mark position
     * for the cursor will not be affected **/
    public void clearSelection() {
        clearHighlights(SELECTION_HIGHLIGHTS);
    }
    
    //========================
    // Accessing highlight information
    //========================
    
        /** 
         * Returns a (shared) set of highlighted NTextElements for the
         * given type. DO NOT MODIFY THE RETURNED SET!
         * The set is not guaranteed to be sorted in any particular order.
         * 
         * The method getHighlightedTextElements[TIME_HIGHLIGHTS] renders the method getSelectedTextElements
         * obsolete!
         */
        public SortedSet getHighlightedTextElements(int type) {
            return highlights[type];
        }


    /** Returns a set of ObjectModelElements associated with a
     * highlight for a given type. The elements are ordered by the
     * appearance of the corresponding NTextElements in the
     * transcript, but there is no duplication. If there are several
     * NTextElements associated with the same ObjectModelElement, then
     * the corresponding NOMElement will appear only once, at the
     * earliest possible point in the list ordering. No null elements
     * will be included. This is a convenience wrapper around
     * getHighlightedTextElements to return the NOMElements associated
     * with the text elements it returns.  See documentation for
     * {@link #getHighlightedTextElements}.
     **/
    public List getHighlightedModelElements(int type) {
        HashSet seen = new HashSet();
        ArrayList res = new ArrayList();
        for (Iterator it = getHighlightedTextElements(type).iterator(); it.hasNext(); ) {
            NTextElement el = (NTextElement)it.next();
            ObjectModelElement editElement = el.getEditElement();
            if ((editElement != null) && !seen.contains(editElement)){
                res.add(editElement);
                seen.add(editElement);
            }
        }
        return res;
    }

    /** Returns a set of NOMElements associated with a highlight for a
     * given type. The elements are ordered by the appearance of the
     * corresponding NTextElements in the transcript, but there is no
     * duplication. If there are several NTextElements associated with
     * the same NOMElement, then the corresponding NOMElement will
     * appear only once, at the earliest possible point in the list
     * ordering. No null elements will be included. This is a
     * convenience wrapper around getHighlightedTextElements to return
     * the NOMElements associated with the text elements it returns.
     * See documentation for {@link #getHighlightedTextElements}.
     **/
    public List getHighlightedNOMElements(int type) {
        HashSet seen = new HashSet();
        List res = new ArrayList();
        for (Iterator it = getHighlightedTextElements(type).iterator(); it.hasNext(); ) {
            NTextElement el = (NTextElement)it.next();
            NOMObjectModelElement editElement = (NOMObjectModelElement)el.getEditElement();
            if (editElement != null)  {
                NOMElement nel = editElement.getElement();
                if ((nel != null) && !seen.contains(nel)) {
                    res.add(nel);
                    seen.add(nel);
                }
            }
        }
        return res;
    }
            

    /** Returns a list of currently selected NOMElements, that is,
     * those highlighted with SELECTION_HIGHLIGHTS. A convenience
     * method equivalent to
     * getHighlightedNOMElements(SELECTION_HIGHLIGHTS);
    **/
    public List getSelectedNOMElements () {
        return getHighlightedNOMElements(SELECTION_HIGHLIGHTS);
    }

    /*===================================================================*/
    /* End of experimental highlighting section                          */
    /*===================================================================*/


    /*===================================================================*/
    /* DOCUMENT ACCESS                                                   */
    /*===================================================================*/

    /** returns a list of the NTextElements currently selected by the
     * user. Shared set, so don't modify it!
     * @deprecated Use {@link #getHighlightedTextElements(int type) getHighlightedTextElements(NTextArea.SELECTION_HIGHLIGHTS)}
     */
    public Set getSelectedTextElements() {
        //DR 04.11.10 this method is now deprecated. It transfers the call to getHighlightedTextElements.
        return getHighlightedTextElements(SELECTION_HIGHLIGHTS);

/*        Set set = new TreeSet(new NTextElementPositionComparator());
        
        //[DR: old version was Set set = new TreeSet(new NTextElementComparator(true));
        //[dr: THIS IS NOT CORRECT ANYMORE! THE SELECTION OF THE CARET (RETURNED HERE) IS NOT THE SAME AS THE SEELCTION
        //OF THE NTA!
        int start = getSelectionStart();
        int end = getSelectionEnd();

        for (int i = start; i < end; i++) {
            NTextElement myel = defaultDoc.getElementAtPosition(i);
            if (myel != null) {
                set.add(myel);
            }
        }

        return set;*/
    }

    /** returns a list of the NOMObjectModelElements currently
     * selected by the user. 
     * @deprecated This method will be renamed to achieve more consistent naming.
     */
    public Set getSelectedElements() {
        //DR 04.11.10 changed to return the NOMObjectModelElements for the getHighlightedTextElements(SELECTION_HIGHLIGHTS)
        Set set = new TreeSet(new ObjectModelComparator());
        Iterator it = getHighlightedTextElements(SELECTION_HIGHLIGHTS).iterator();
        NTextElement textel;
        while (it.hasNext()) {
            textel = (NTextElement)it.next();
            if (textel.getDataElement() != null) {
                set.add(textel.getDataElement());
            }
        }
        return set;

/*        int start = getSelectionStart();
        int end = getSelectionEnd();

        //        System.out.println("Selecting from: " + start + " to: " + end);

        for (int i = start; i < end; i++) {
            NTextElement myel = defaultDoc.getElementAtPosition(i);
            if (myel != null && myel.getEditElement()!=null) {
                set.add(myel.getEditElement());
            }
        }
        return set;*/
    }

    /** returns a list of the NOMObjectModeElements currently selected
     * by the user, guaranteeing to maintain screen order 
     * @deprecated This method will be renamed to achieve more consistent naming.
     */
    public List getSelectedElementsOrdered() {
        //DR 04.11.10 changed to return the NOMObjectModelElements for the getHighlightedTextElements(SELECTION_HIGHLIGHTS)
        ArrayList set = new ArrayList();
        Iterator it = getHighlightedTextElements(SELECTION_HIGHLIGHTS).iterator();
        NTextElement textel;
        while (it.hasNext()) {
            textel = (NTextElement)it.next();
            if (textel.getDataElement() != null) {
                // you can have >1 NOMObjectModelElement for the same element.
                if (!(member(set, textel.getDataElement()))) {
                    set.add(textel.getDataElement());
                }
            }
        }
        return set;

/*      ArrayList set = new ArrayList();
        int start = getSelectionStart();
        int end = getSelectionEnd();

        for (int i = start; i < end; i++) {
            NTextElement myel = defaultDoc.getElementAtPosition(i);
            if (myel != null && myel.getEditElement()!=null) {
                // you can have >1 NOMObjectModelElement for the same element.
                 if (!(member(set, myel.getEditElement()))) {
                    set.add(((NOMObjectModelElement)myel.getEditElement()));
                }
            }
        }
        return set;*/
    }

    /** returns a list of the NOMElements currently selected by the
     * user, guaranteeing to maintain screen order 
     *      
     * @deprecated This method will be renamed to achieve more consistent naming.
     */
    public Set getSelectedNOMElementsOrdered() {
        //DR 04.11.10 changed to return the NOMObjectModelElements for the getHighlightedTextElements(SELECTION_HIGHLIGHTS)
        LinkedHashSet set = new LinkedHashSet();
        Iterator it = getHighlightedTextElements(SELECTION_HIGHLIGHTS).iterator();
        NTextElement textel;
        while (it.hasNext()) {
            textel = (NTextElement)it.next();
            if (textel.getDataElement()!=null) {
                set.add(((NOMObjectModelElement)textel.getDataElement()).getElement());
            }
        }
        return set;
        /*int start = getSelectionStart();
        int end = getSelectionEnd();

        // System.out.println("Selecting from: " + start + " to: " + end);

        for (int i = start; i < end; i++) {
            NTextElement myel = defaultDoc.getElementAtPosition(i);
            if (myel != null && myel.getEditElement()!=null) {
                set.add(((NOMObjectModelElement)myel.getEditElement()).getElement());
            }
        }
        return set;*/
    }

    /** Given a position in a document (probably because of trying to move the
     * cursor), return the ObjectModelElement at it, or null **/
    public ObjectModelElement getObjectModelElementAtPosition(int position) {
        NTextElement retel = defaultDoc.getElementAtPosition(position);
        if (retel==null) { return null; }
        return retel.getEditElement();
    }

    /** given a Point (most likely a mouseEvent.getPoint() intercepted
     *  by a MouseListener), return the ObjctModelElement beneath it 
     * (or return null). 
     */
    public ObjectModelElement getObjectModelElementAtPoint(Point clickpoint) {
        int vtm = viewToModel(clickpoint);
        return getObjectModelElementAtPosition(vtm);
    }

    /** Given a position in a document (probably because of trying to move the
     * cursor), return the NTextElement at it, or null **/
    public NTextElement getNTextElementAtPosition(int position) {
        return  defaultDoc.getElementAtPosition(position);
    }

    /** given a Point (most likely a mouseEvent.getPoint() intercepted
     *  by a MouseListener), return the NTextElement beneath it (or
     *  return null). 
     */
    public NTextElement getNTextElementAtPoint(Point clickpoint) {
        int vtm = viewToModel(clickpoint);
        return getNTextElementAtPosition(vtm);
    }

    /** return the set of screen elements (NTextElements) represented
     * by the given object model element. The set is not sorted - the
     * programmer is expected to provide their own sort.
     @see NTextElementPositionComparator
    */
    public Set getTextElements(ObjectModelElement ome) {
        return (Set) dataEditToTextElement.get(ome.getID());
    }

    /** Returns the position of the selection start, or -1 if nothing
     * is selected. The selection start is determined by the contents
     * of getHighlightedTextElements(SELECTION_HIGHLIGHTS) rather than
     * the actual java selection, which is a subset of selection
     * highlights. See {@link #newSelection}
    **/
    public int getSelectionStartPosition() {
        if (getHighlightedTextElements(SELECTION_HIGHLIGHTS) != null) {
            SortedSet selected = getHighlightedTextElements(SELECTION_HIGHLIGHTS);
            if (selected.size() > 0) {
                NTextElement first = (NTextElement)selected.first();
                return first.getPosition();
            }
        }
        return -1;                                           
    }

    /** Returns the position of the selection end, or -1 if nothing is
     * selected. The selection end is determined by the contents
     * of getHighlightedTextElements(SELECTION_HIGHLIGHTS) rather than the actual
     * java selection, which is a subset of selection highlights. 
     * See {@link #newSelection}
    **/ 
    public int getSelectionEndPosition() {
        if (getHighlightedTextElements(SELECTION_HIGHLIGHTS) != null) {
            SortedSet selected = getHighlightedTextElements(SELECTION_HIGHLIGHTS);
            if (selected.size() > 0) {
                NTextElement last = (NTextElement)selected.last();
                return last.getPosition()+last.getText().length();
            }
        }
        return -1;                                           
    }
    
    
    /*===================================================================*/
    /* End of DOCUMENT ACCESS code                                       */
    /*===================================================================*/


    /*===================================================================*/
    /* DOCUMENT EDITING                                                  */
    /*===================================================================*/

    /**
     *Inserts the content specified in the text element argument at
     *the end of the current document. The content is displayed with
     *the style attributes
     *
     *@param te The element to be inserted into the document
     **/
    public void addElement(NTextElement te) {
        // disable the caret listener while making this insertion -
        CaretListener[] carets=getCaretListeners();
            for (int i=0;i<carets.length;i++)
                removeCaretListener(carets[i]);
        int currlength = defaultDoc.getLength();

        // jonathan: this is required since the indices
        // dataToTextElement and dataEditToTextElement use
        // NTextElementPositionComparator, and the position of new
        // elements (not yet in the NTextArea) will always be zero.
        te.setPosition(currlength);
        setupIndices(te);
        //System.out.println("Insert text: '" + te.getText() + "'; currlength=" + currlength + "; element position: " + te.getPosition());
        insertText(te, currlength, currlength);
        //the caret can go back to listening - insertion finished
        for (int i=0;i<carets.length;i++)
                addCaretListener(carets[i]);
	if (te.getEndTime()>maxtime) {
	    maxtime = te.getEndTime();
	}
    }

    /** get the largest end time of any NTextElement so far added
     * to this NTextArea. Returns UNTIMED if no timed elements */
    public double getMaxTime() {
	return maxtime;
    }

    /**
     * Insert a new NTextElement into the document after the given
     * existing element. The new element must be the first arg!
     *
     * DR: this method seems to add the text BEFORE the given element?
     */
    public void addElement(NTextElement newEl, NTextElement oldEl) {
        if (newEl==null || oldEl==null) { return; }
        // disable the caret listener while making this insertion -
        CaretListener[] carets=getCaretListeners();
            for (int i=0;i<carets.length;i++)
                removeCaretListener(carets[i]);
        setupIndices(newEl);
        int pos = oldEl.getPosition();
        int currlength = defaultDoc.getLength();
        insertText(newEl, pos, currlength);
        //the caret can go back to listening - insertion finished
        for (int i=0;i<carets.length;i++)
                addCaretListener(carets[i]);
    }

    protected static final class AppCloser extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
            System.exit(0);
        }
    }

    /**
     * Listener for the edits on the current document.
     */
    protected UndoableEditListener undoHandler = new UndoHandler();

    /** UndoManager that we add edits to. */
    protected UndoManager undo = new UndoManager();

    class UndoHandler implements UndoableEditListener {

        /**
         * Messaged when the Document has created an edit, the edit is
         * added to <code>undo</code>, an instance of UndoManager.
         */
        public void undoableEditHappened(UndoableEditEvent e) {
            undo.addEdit(e.getEdit());
            undoAction.update();
            redoAction.update();
        }
    }

    /**
     * FIXME - I'm not very useful yet
     */
    class StatusBar extends JComponent {

        public StatusBar() {
            super();
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        }

        public void paint(Graphics g) {
            super.paint(g);
        }

    }

    // --- action implementations -----------------------------------

    private UndoAction undoAction = new UndoAction();
    private RedoAction redoAction = new RedoAction();

    /**
     * Actions defined by the Notepad class
     */
    private Action[] defaultActions = { undoAction, redoAction };

    class UndoAction extends AbstractAction {
        public UndoAction() {
            super("Undo");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            try {
                undo.undo();
            } catch (CannotUndoException ex) {
                System.out.println("Unable to undo: " + ex);
                ex.printStackTrace();
            }
            update();
            redoAction.update();
        }

        protected void update() {
            if (undo.canUndo()) {
                setEnabled(true);
                putValue(Action.NAME, undo.getUndoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Undo");
            }
        }
    }

    class RedoAction extends AbstractAction {
        public RedoAction() {
            super("Redo");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            try {
                undo.redo();
            } catch (CannotRedoException ex) {
                System.out.println("Unable to redo: " + ex);
                ex.printStackTrace();
            }
            update();
            undoAction.update();
        }

        protected void update() {
            if (undo.canRedo()) {
                setEnabled(true);
                putValue(Action.NAME, undo.getRedoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Redo");
            }
        }
    }

    /**
     * Method redisplayElement. This is used to update the display
     * after a change has been made to the xml. This version assumes
     * the text element is displaying the value of its
     * 'displayedAttribute' attribute or PCDATA, but nothing else: if
     * the NTextElement's text has been updated manually, nothing
     * will happen!
     */
    public void redisplayElement(ObjectModelElement element) {
        // a single element can have many on screen representations, so
        // find 'em all
        Set s = (Set) dataEditToTextElement.get(element.getID());
        if (s==null) {
            System.err.println("Element " + element.getID() + " cannot be redisplayed!");
            return;
        }
        /*
          [DR: prviously this was */
        HashSet ds;
        Set dataset = (Set)dataToTextElement.get(element.getID());
        if (dataset==null) { ds = new HashSet(); }
        else { ds = new HashSet(dataset); }
        //HashSet newset= new HashSet();
        //TreeSet newset= new TreeSet();
        /*
        TreeSet ds;
        Set dataset = (Set)dataToTextElement.get(element.getID());
        if (dataset==null) { ds = new TreeSet(new NTextElementPositionComparator()); }
        else { 
            ds = new TreeSet(); 
            ds.addAll(dataset); 
        }
        */
        TreeSet newset= new TreeSet(new NTextElementPositionComparator());
        // taking out the caret listener while removes and
        // insertions happen to avoid mis-selections in the display
        CaretListener[] carets=getCaretListeners();
            for (int i=0;i<carets.length;i++)
                removeCaretListener(carets[i]);
        for (Iterator it=s.iterator(); it.hasNext(); ) {
            NTextElement oldEl = (NTextElement) it.next();
            NTextElement newEl = oldEl.copy();

            // update the content of the text element
            String newText = "";

            if (oldEl.getEditElement().getDisplayedAttribute() != null) {
                newText = element.getAttributeValue(
                oldEl.getEditElement().getDisplayedAttribute().trim());
                    // + " ";
            } else {
                newText = element.getTextualContent().trim();
                //+ " ";
            }
            newEl.setText(newText);

            // insert it into the new Set for this dataTextElement 
            newset.add(newEl);
            if (ds.contains(oldEl)) {
                ds.remove(oldEl);
                ds.add(newEl);
            }

            defaultDoc.replaceTextElement(oldEl, newEl);
        }
        //remove all highlights which are still hanging around
        getHighlighter().removeAllHighlights();
        for (int i=0;i<carets.length;i++)
                addCaretListener(carets[i]);
        dataEditToTextElement.remove(element.getID());
        dataEditToTextElement.put(element.getID(), newset);

        dataToTextElement.remove(element.getID());
        dataToTextElement.put(element.getID(), ds);
    }

    /** assuming we have a Map containing a mapping between element
     * IDs and Sets (of NTextElements), remove this NTextElement from
     * being associated with the given element */ 
    private void removeFromMap(Map index, ObjectModelElement el, NTextElement nte) {
        if (el==null || index==null || nte==null) { return; }
        Set displays = new HashSet();
        Set old = (Set)index.get(el.getID());
        if (old==null) { return; }
        for (Iterator oit=old.iterator(); oit.hasNext(); ) {
            NTextElement ome = (NTextElement) oit.next();
            if (ome!=nte) { displays.add(ome); }
        }
        index.put(el.getID(), displays);
    }

    /** assuming we have a Map containing a mapping between element IDs and
     * Sets (of elements), add the new element to the Set */ 
    private void addToMap(Map index, ObjectModelElement el, NTextElement nte) {
        if (index==null || el==null || nte==null) { return; }
        Set displays = (Set) index.get(el.getID());
        if (displays==null) { displays=new TreeSet(); }
        displays.add(nte);
        index.put(el.getID(), displays);
    }

    /** swap one NTextElement for another */
    public void redisplayTextElement(NTextElement oldEl, NTextElement newEl) {
        
        removeFromMap(dataToTextElement, oldEl.getDataElement(), oldEl);
        addToMap(dataToTextElement, newEl.getDataElement(), newEl);
        removeFromMap(dataEditToTextElement, oldEl.getEditElement(), oldEl);
        addToMap(dataEditToTextElement, newEl.getEditElement(), newEl);

        CaretListener[] carets=getCaretListeners();
            for (int i=0;i<carets.length;i++)
                removeCaretListener(carets[i]);
        removeHighlighted(SELECTION_HIGHLIGHTS, oldEl);        
        defaultDoc.replaceTextElement(oldEl, newEl);
        for (int i=0;i<carets.length;i++)
                addCaretListener(carets[i]);

        if (newEl.getDataElement()!=null) {
            setHighlighted(SELECTION_HIGHLIGHTS, newEl.getDataElement());
        }
    }


    /**
     * Method redisplayElement. This is used to update the display
     * after a change has been made to the xml, however we make no
     * assumption here about what is displayed - we find all the
     * on-screen representations and only replace the one(s) that
     * match the first string argument. The second string argument is
     * the new text. 
     */
    public void redisplayElement(ObjectModelElement element, String oldstring, String newstring) {
        // a single element can have many on screen representations, so
        // find 'em all
        if (element==null) { return; }
        Set s = (Set) dataEditToTextElement.get(element.getID());
        if (s==null) {
            s = (Set) dataToTextElement.get(element.getID());
        }
        if (s==null) {
            System.err.println("Element " + element.getID() + " cannot be redisplayed!");
            return;
        }
        Set gg=(Set)dataToTextElement.get(element.getID());
        CaretListener[] carets=getCaretListeners();
        for (int i=0;i<carets.length;i++)
            removeCaretListener(carets[i]);
        for (Iterator it=s.iterator(); it.hasNext(); ) {
            NTextElement oldEl = (NTextElement) it.next();
            if (!oldEl.getText().equals(oldstring)) {             
                continue; 
            }
            defaultDoc.replaceText(oldEl, newstring);
        }

        //remove all highlights which are still hanging around
        //        getHighlighter().removeAllHighlights();

        for (int i=0;i<carets.length;i++)
            addCaretListener(carets[i]);
    }


    /**
     * This version of redisplay takes the actual text element that
     * needs to be replaced as an argument along with the new text.
     */
    public void redisplayElement(NTextElement element, String newstring) {
        if (element==null) { return; }
        // taking out the caret listener while removes and
        // insertions happen to avoid mis-selections in the display
        CaretListener[] carets=getCaretListeners();
        for (int i=0;i<carets.length;i++)
            removeCaretListener(carets[i]);
        defaultDoc.replaceText(element, newstring);
        //remove all highlights which are still hanging around
        getHighlighter().removeAllHighlights();
        for (int i=0;i<carets.length;i++)
            addCaretListener(carets[i]);
    }


    /**
     * Remove all representations of the ObjectModelElement from the
     * NTextArea.
     */
    public void removeDisplayComponent(ObjectModelElement element) {
        //a single element can have many on screen representations, so find 'em all
        Set s = (Set) dataEditToTextElement.get(element.getID());
        Set copy = new HashSet(s); 
        //TreeSet(s); [DR:] this used to be a TreeSet, which is a
        //sorted type of set, but since the textElements in the set
        //are not well defined wrt their compare operator, the TreeSet
        //constructor throws an exception (nullpointer in comparing
        //when inserting new elements) I (DR) think we don't need a
        //sorted set here. If we do, we should define a proper
        //comparator to go with it.  [DR:] The next two lines provide
        //an alternative, if we decide to use sorting after all.
        
        //Set copy = new TreeSet(new NTextElementPositionComparator());
        //copy.addAll(s);

        CaretListener[] carets=getCaretListeners();
        for (int i=0;i<carets.length;i++)
            removeCaretListener(carets[i]);
        Iterator it = copy.iterator();
        while (it.hasNext()) {
            NTextElement textelement = (NTextElement) it.next();
            //remove this text element
            
            //!@#$%^&*( this code belo didn't remove it properly from time map and position map !@#$%^& DR
            //defaultDoc.remove(
            //  defaultDoc.getTextElementPosition(textelement),
            //textelement.getText().length());

            //before we do remove, the cursor should be set just BEFORE the removed element
            int pos = textelement.getPosition();
            //DR 26.11.04
                setSelectionStart(pos);   //[DR: added. if not, and the removal is inside the 'original selection' (i.e. before we modified highlight in newSelection), the system crashes terribly...
                setSelectionEnd(pos);//[DR: added. if not, and the removal is inside the 'original selection' (i.e. before we modified highlight in newSelection), the system crashes terribly...
            defaultDoc.removeTextElement(textelement);
        }
        // jonathan: more sensible removal from indices: means the
        // indexed value is null rather than an empty Set which seems
        // to be a good thing!
        dataEditToTextElement.remove(element.getID());
        dataToTextElement.remove(element.getID());
        for (int i=0;i<carets.length;i++)
            addCaretListener(carets[i]);
    }

    /**
     * Insert a new text element into the parent at the given position
     */
    public void insertDisplayElement(
        ObjectModelElement newElement,
        ObjectModelElement parent,
        int position) {
        NTextElement newTextElement = new NTextElement();
        newTextElement.setDataElement(newElement);
        //make the text of the text element the displayAttribute of the object model element
        newTextElement.setText(
            newElement.getAttributeValue(newElement.getDisplayedAttribute()));
        //now get the positionth child of the parent
        List children = parent.getChildren();
        ObjectModelElement sibling =
            (ObjectModelElement) children.get(position);
        // first find the text elements which represent the sibling        
        Set s = (Set) dataToTextElement.get(sibling.getID());
        Set copy = new TreeSet(s);
        Iterator it = copy.iterator();
        while (it.hasNext()) {
            NTextElement textelement = (NTextElement) it.next();
            try {
                // now reinsert the new element immediately after this
                // representation of the sibling
                int currlength = defaultDoc.getLength();
                //insert it into the dataTextElement positionToElement
                ((Set) dataToTextElement.get(newElement.getID())).add(
                    newTextElement);
                ((Set) dataEditToTextElement.get(newElement.getID())).add(
                    newTextElement);

            //DR 26.11.04
                setSelectionStart(position);   //[DR: added. if not, and the insertion is inside the 'original selection' (i.e. before we modified highlight in newSelection), the system crashes terribly...
                setSelectionEnd(position);//[DR: added. if not, and the insertion is inside the 'original selection' (i.e. before we modified highlight in newSelection), the system crashes terribly...

                defaultDoc.insertString(
                    defaultDoc.getTextElementPosition(textelement),
                    textelement.getText(),
                    textelement.getStyle(),
                    textelement);

            } catch (BadLocationException e) {
                System.err.println(
                    "Error inserting text element ( "
                        + textelement.getText()
                        + " ) into text area from NTextArea");
                e.printStackTrace();
            }

        }
    }

    /*===================================================================*/
    /* End of DOCUMENT EDITING code                                      */
    /*===================================================================*/



    /*===================================================================*/
    /* UTILITY                                                           */
    /*===================================================================*/

    /** Checks whether the given selection region is an extension of
     * the current selection region. If so, it extends the current
     * region; if not, it just selects the new region instead. 
     */
 /*   private void updateSelection( int realStart, int realEnd ) {
        //[DR: where is this used? I had some problems with it somewhere...
        if (getSelectionStart()<=realStart && realEnd<=getSelectionEnd()) {
             if we already cover the selection, don't change at all! 
            return;
        } else if (getSelectionStart()<=realStart && realStart<=(getSelectionEnd()+1) && 
                   realEnd > getSelectionEnd()) {
             This is right abuttment / overlap 
            setSelectionEnd( realEnd );
        } else if ((getSelectionStart()-1)<=realEnd && realEnd<=getSelectionEnd() &&
                   realStart < getSelectionStart()) {
             This is left abuttment / overlap 
            setSelectionStart( realStart );
        } else {
             Otherwise we have a non-contiguous selection - start again. 
            setSelectionStart( realStart );
            setSelectionEnd( realEnd );
        }
        //[DR: setselectionstart and end are halfway to being deprecated (see documentation in JTextPane)
    }*/


    /* Some common code when we're adding a new NTextElement */
    private void setupIndices(NTextElement te) {
        if (te.getDataElement() != null) {
            String datael = te.getDataElement().getID();
            Set l = (Set) dataToTextElement.get(datael);
            if (l == null) {
                l = new TreeSet(new NTextElementPositionComparator());
                //[DR: Was previously: l = new TreeSet(new NTextElementComparator());
            }
            l.add(te);
            dataToTextElement.put(datael, l);
        }

        if (te.getEditElement() != null) {
            String datael = te.getEditElement().getID();
            Set l = (Set) dataEditToTextElement.get(datael);
            if (l == null) {
                l = new TreeSet(new NTextElementPositionComparator());
                //[DR: Was previously: l = new TreeSet(new NTextElementComparator());
            } 
            l.add(te);
            dataEditToTextElement.put(datael, l);
        }
    }

    /* some more common code for the inserting routines */
    private void insertText(NTextElement te, int pos, int fullength) {

        if (te instanceof IconTextElement) {
            //try to make sure the cursor is in the right place
            CaretListener[] carets=getCaretListeners();
                for (int i=0;i<carets.length;i++)
                    removeCaretListener(carets[i]);
            setSelectionStart(pos);
            setSelectionEnd(pos);
            //javax.swing.text.MutableAttributeSet attr = new javax.swing.text.SimpleAttributeSet();
            //attr.addAttribute(javax.swing.text.StyleConstants.IconAttribute, ((IconTextElement)te).getIcon());
            insertIcon(((IconTextElement) te).getIcon());
            if (pos>=fullength) {
                defaultDoc.indexElement(pos, te, 1);
            } else {
                defaultDoc.insertElement(pos, te, 1);
            }
                for (int i=0;i<carets.length;i++)
                    addCaretListener(carets[i]);
        } else {
            try {
                CaretListener[] carets=getCaretListeners();
                for (int i=0;i<carets.length;i++)
                    removeCaretListener(carets[i]);
                setSelectionStart(pos);   //[DR: added. if not, and the insertion is inside the 'original selection' (i.e. before we modified highlight in newSelection), the system crashes terribly...
                setSelectionEnd(pos);//[DR: added. if not, and the insertion is inside the 'original selection' (i.e. before we modified highlight in newSelection), the system crashes terribly...
                
                defaultDoc.insertString(pos, te.getText(),te.getStyle(), te);
                if (pos>=fullength) {
                    defaultDoc.indexElement(pos, te, te.getText().length());
                } else {
                    defaultDoc.insertElement(pos, te, te.getText().length());
                }
                for (int i=0;i<carets.length;i++)
                    addCaretListener(carets[i]);
            } catch (BadLocationException e) {
                System.err.println("Error inserting text element ( " 
                                   + te.getText() + " ) into text area");
                e.printStackTrace();
            }
        }
    }

    /** Check membership of a list, but do it by comparing IDs: the
     * specific ObjectModelElement may well be different */
    private boolean member(List l, ObjectModelElement object) {
        if (l==null || l.size()==0 || object==null) { return false; }
        for (int i=l.size()-1; i>=0; i--) {
            ObjectModelElement ome = (ObjectModelElement)l.get(i);
            if (ome.getID()==object.getID()) { return true; }
        }
        return false; 
    }

    /** 3 METHODS BELOW ARE BY DENNIS REIDSMA */

    /**
     * Searches the style hierarchy on the text element and removes
     * the first style that satisfies the predicate. Also remove the
     * style from the
     *
     * As an example of combining the predicate with the name used in
     * insertCopyOfStyle method, see showTimeSpan(double start, double
     * end)
     *
     * Returns the removed style
     *
     * <p>TODO: improve documentation here with a picture of this
     * style hierarchy, highlightinh what will be removed
     */
    public Style removeStyleFromChain(NTextElement element, Predicate isAlignStyle) {
        //remove align style from this element --> find it, and remove it from the chain
        if (element.getStyle() == null) {
            return null;
        }
        Style chainStyle = getStyle(element.getStyle());
        Style oldChainStyle = null;
        Style firstChainStyle = chainStyle;
        while ((chainStyle != null) && !isAlignStyle.valid(chainStyle)) {
            oldChainStyle = chainStyle;
            chainStyle = (Style)chainStyle.getResolveParent();
        }
        if (chainStyle == null) {
            return null;
        }
        if (chainStyle.getName() == null) {
            return null;
        }
        if (isAlignStyle.valid(chainStyle)) {
            //System.out.println("remove: " + chainStyle.getName());
            if (oldChainStyle == null) {
                //System.out.println("was bottom style...");
                if (chainStyle.getResolveParent() == null) {
                    //System.out.println("no parent...");
                    element.setStyle(null);
                    defaultDoc.setCharacterAttributes(element.getPosition(), element.getText().length(), getStyle(StyleContext.DEFAULT_STYLE),true);
                } else {
                    //System.out.println("keep parent: " + ((Style)chainStyle.getResolveParent()).getName());
                    element.setStyle(((Style)chainStyle.getResolveParent()).getName());
                    defaultDoc.setCharacterAttributes(element.getPosition(), element.getText().length(), (Style)chainStyle.getResolveParent(),true);
                }
            } else {
                //System.out.println("keep child: " + oldChainStyle.getName());
                //System.out.println("keep parent: " + chainStyle.getResolveParent());
                oldChainStyle.setResolveParent(chainStyle.getResolveParent());
                defaultDoc.setCharacterAttributes(element.getPosition(), element.getText().length(), firstChainStyle,true);
            }
            removeStyle(chainStyle.getName());
        }
        chainStyle.setResolveParent(null);
        return chainStyle;
    }

    /**
     * This method inserts NTextElement e in the NTA at the certain position
     * Positions of the elements in the NTA are refreshed.
     */
    public void insertElement(NTextElement te,int pos) {
        // disable the caret listener while making this insertion -
        CaretListener[] carets=getCaretListeners();
              for (int i=0;i<carets.length;i++)
                removeCaretListener(carets[i]);
        setupIndices(te);
        int currlength = defaultDoc.getLength();
        if ((pos < 0) || (pos > currlength)) {
            System.out.println("error:"+pos);
        }
        insertText(te, pos, currlength);
        //the caret can go back to listening - insertion finished
              for (int i=0;i<carets.length;i++)
                addCaretListener(carets[i]);
    }           
        

    /**
     * Inserts a copy of the given style for the style hierarchy of the NTEXTelement,
     * using the given name (which should be unique).
     *
     */
    public Style insertCopyOfStyle(NTextElement element, Style templateStyle, String name) {
        Style parent = null;
        //System.out.println("element:"+element);
        //System.out.println("template:"+templateStyle);
        //System.out.println("name:"+name);
        if (element.getStyle() != null) {
            parent = getStyle(element.getStyle());
            //System.out.println("insert style. parent, according to ntv: " + element.getStyle());
            if (parent==null) {
                //System.out.println("nonexist parent?? style="+element.getStyle()+"parent="+getStyle(element.getStyle()));
            }else{
                //System.out.println("insert style. parent: " + parent.getName());
            }
            //System.out.println("insert style. name: " + name);
        }
        Style newStyle = addStyle(name,parent);
        Enumeration e = templateStyle.getAttributeNames();
        while (e.hasMoreElements()) { //this is annoying. addAttributes(Style) will also copy the NAME attribute, meaning that the wrong style will be added later on because the name attribute is no longer correct :(
            Object nextKey = e.nextElement();
            if (!(nextKey.equals(Style.NameAttribute)||(nextKey.equals(Style.ResolveAttribute)))) {
                newStyle.addAttribute(nextKey, templateStyle.getAttribute(nextKey));
            }
        }
        //System.out.println("Parent:"+newStyle.getResolveParent());
        element.setStyle(name);
        defaultDoc.setCharacterAttributes(element.getPosition(), element.getText().length(), newStyle,false);
        return newStyle;
    }

    /*===================================================================*/
    /* END OF UTILITY CODE                                               */
    /*===================================================================*/

    /** turn off media / time playing ability for this text area. The
     * control-key listener is removed from NITEStyledDocument so
     * media can't be played */
    public void setReplayEnabled(boolean value) {
	caretListener.setReplayEnabled(value);
    }
}
