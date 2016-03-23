/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.textviewer;

import java.awt.*;
import java.awt.print.*;
import javax.swing.JTextPane;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.*;
import java.util.*;

public class NITEStyledDocument extends DefaultStyledDocument implements Printable {

/**
 */

    private PositionElementMapper positionMapper = new PositionElementMapper();
    private TimeIntervalMapper timemap;
    private TimeIntervalIterator currentTimeIterator = null;
    private Set previousSet = null;
    private Set currentSet = null;
    private PrintView printView = null;
    private JTextPane textpane = null;

    public NITEStyledDocument(JTextPane jtp, StyleContext styles) {
        super(styles);
        textpane=jtp;
        timemap = new TimeIntervalMapper();
    }

    public void insertString(int offset, String s, String stylename, NTextElement te) throws BadLocationException {
        Style parastyle = null;
        int length = te.getText().length();
        AttributeSet style=getCharacterElement(offset).getAttributes();
        //DR: I wouldn't do it like this... What if there is already a style defined at this position, that should NOT
        // be used for this new element?
        if (stylename != null) {
            style = getStyle(stylename);
        } 
        //FIX ME - There is an odd problem here in that mostly the
        //text is displayed with the correct style, but sometimes the
        //style is just the default plain black. Not sure why
        insertString(offset, s, style);
    }

    /** Insert an element at the end of the current document. Faster
        than insertElement as it doesn't need to recalculate other
        elements' positions. */
    public void indexElement(int offset, NTextElement te, int length) {
        if ((te.getStartTime() != NTextElement.UNTIMED) && 
            (te.getEndTime() != NTextElement.UNTIMED) &&
            (!Double.isNaN(te.getStartTime())) && 
            (!Double.isNaN(te.getEndTime())) ) {
            timemap.addObject(te, te.getStartTime(), te.getEndTime());
        }
        te.setPosition(offset);
        //index this element in the position mapper
        positionMapper.appendElement(te);
    }

    /** This will insert an element in the middle of a NTextArea. It
        is very much slower than indexElement which adds the element
        at the end */
    public void insertElement(int offset, NTextElement te, int length) {
        if ((te.getStartTime() != NTextElement.UNTIMED) && 
            (te.getEndTime() != NTextElement.UNTIMED) &&
            (!Double.isNaN(te.getStartTime())) && 
            (!Double.isNaN(te.getEndTime())) ) {
            timemap.addObject(te, te.getStartTime(), te.getEndTime());
        }
        te.setPosition(offset);
        //System.out.println("offset in insertelement"+offset);
        //index this element in the position mapper
        positionMapper.addElement(offset, te);
    }

    //replace the old text element with the new text element in the
    //document there is an unresolved bug which occasionaly pops up -
    //sometimes the absract document's locking behaviour kicks in and
    //results in deadlock. You can solve this by removing all the
    //document listeners before the removal and insertion, and
    //replacing the document listeners afterwards. The problem with
    //this is that the update behaviour isn't right, and the doc only
    //redisplays on window minimization. Jonathan swapped the order of
    //insert and remove as it was causing an occasional bug.
    /** replace the old text element with the new text element in the
        document */
    public void replaceTextElement(NTextElement oldEl, NTextElement newEl) {
        int offset = oldEl.getPosition();

        try { 
	    //insert the new string
	    insertString(offset, newEl.getText(), newEl.getStyle(), newEl);
            //add the new element to the time indexer
	    newEl.setPosition(offset);
	    timemap.addObject(newEl, newEl.getStartTime(), newEl.getEndTime());
	    positionMapper.addElement(offset, newEl);
            // remove the old element
            removeTextElement(oldEl);
        } catch (Exception ble) {
            System.err.println("Error replacing text");
            ble.printStackTrace();
        }

    }

    /** replace the text of an existing text element without removing
        the element */
    public void replaceText(NTextElement oldEl, String newText) {
        int offset = oldEl.getPosition();
        try {
            // note that this was occasionally breaking when remove
            // was before insert, so now they are swapped. 

            //insert the new element
            insertString(offset, newText, oldEl.getStyle(), oldEl);
            // remove the old text
            int len = oldEl.getText().length();
            int nlen = newText.length();
            super.remove(offset+nlen, len);
            // we do need to change the positionmapper, but not the timemapper
            positionMapper.removeElement(oldEl);
            oldEl.setText(newText);
            positionMapper.addElement(offset, oldEl);
        } catch (BadLocationException b) {
            b.printStackTrace();
        }
    }


    /**
     * Method removeTextElement.
     * @param textelement
     */
    public void removeTextElement(NTextElement textelement) {
        int offset = textelement.getPosition();
        //              remove the old element
        try {
            super.remove(offset, textelement.getText().length());
            timemap.removeObject(textelement, textelement.getStartTime(), textelement.getEndTime());
            positionMapper.removeElement(textelement);
        } catch (BadLocationException b) {
            b.printStackTrace();
        }
    }

    public int getTextElementPosition(NTextElement e) {
        return positionMapper.getTextElementPosition(e);
    }
/*
[DR: I used the following 4 methods to track down a terrible defaultcaret problem.
It turned out that the DefaultCaret$UpdateHandler didn't return from removeUpdate...
:-(

[DR:] KEEP in reserve, in case the carets get troublesome again.

protected void fireChangedUpdate(DocumentEvent e)  {
    System.out.println("in change");
    super.fireChangedUpdate(e);
    System.out.println("out change");
}
protected void fireInsertUpdate(DocumentEvent e)  {
    System.out.println("in insert");
    super.fireInsertUpdate(e);
    System.out.println("out insert");
}
protected void fireRemoveUpdate(DocumentEvent e)  {
    System.out.println("in rem");
    super.fireRemoveUpdate(e);
    System.out.println("out rem");
}
protected void fireUndoableEditUpdate(UndoableEditEvent e)  {
    System.out.println("in undoebel");
    super.fireUndoableEditUpdate(e);
    System.out.println("out undoebel");
}
*/

   /* public void remove(int offset, int len) throws BadLocationException {
this method was removed by DR on 9-11-2004
        super.remove(offset, len);
        //FIX ME - test this remove method in TimeIntervalMapper
        int index = ((NTextElement) positionMapper.getElementAtPosition(offset)).getPosition();
        positionMapper.removeElement(positionMapper.getElementAtPosition(offset));
    }*/

    public NTextElement getElementAtPosition(int pos) {
        return positionMapper.getElementAtPosition(pos);
    }

    public Set getElementsBetweenPositions(int start, int end) {
        //this is comparing the text elements by their text
        HashSet temp = new HashSet();
        for (int i = start; i < end; i++) {
            NTextElement docEl = (NTextElement) getElementAtPosition(i);
            if (docEl != null) {
                temp.add(docEl);
            } else {
                System.out.println("positionmapper contains null element at position "+i);
            }
        }
        return temp;
    }

    /**
     * Returns a set of NTextElements which are in time scope at the
     * specified time
     **/
    public Set getElementsAtTime(double time) {
        Set currentSet = null;
        
        currentTimeIterator = timemap.getTimeIntervalIterator();
        currentTimeIterator.setTime(time);
        //retrieve the display components which are currently in time
        //scope, and highlight them
        if (currentTimeIterator != null) {
            currentSet = currentTimeIterator.getMatchingObjects();
        }
        return currentSet;
    }

    /**
     * Returns a set of NTextElements which are in time scope between
     * the given start and end times
     **/
    public Set getElementsBetweenTimes(double start, double end) {
        Set currentSet = null;
        
        currentTimeIterator = timemap.getTimeIntervalIterator();
        currentTimeIterator.setTimes(start, end);
        //retrieve the display components which are currently in time
        //scope, and highlight them
        if (currentTimeIterator != null) {
            currentSet = currentTimeIterator.getMatchingObjects();
        }
        return currentSet;
    }

    /* Printable interface implementation */
    public int print(Graphics pg, PageFormat pageFormat, int pageIndex)
        throws PrinterException {
        pg.translate((int)pageFormat.getImageableX(),(int)pageFormat.getImageableY());
        int wPage = (int)pageFormat.getImageableWidth();
        int hPage = (int)pageFormat.getImageableHeight();
        pg.setClip(0, 0, wPage, hPage);
        
        if (printView == null) {
            BasicTextUI btui = (BasicTextUI)textpane.getUI();
            View root = btui.getRootView(textpane);
            printView = new PrintView(getDefaultRootElement(), root, wPage, hPage);
        }
        boolean cont = printView.paintPage(pg, hPage, pageIndex);
        System.gc(); // garbage collection is synchronous
        if (cont) return PAGE_EXISTS;
        printView = null;
        return NO_SUCH_PAGE;
    }
    
    /* This class is very similar to the print routine in 'Swing
       second edition'. See:
       http://manning.com/sbe/files/uts2/Chapter22html/Chapter22.htm 
    */
    class PrintView extends BoxView {
        protected int m_firstOnPage = 0;
        protected int m_lastOnPage = 0;
        protected int m_pageIndex = 0;
        
        public PrintView(Element elem, View root, int w, int h) {
            super(elem, Y_AXIS);
            setParent(root);
            setSize(w, h);
            layout(w, h);
        }

        public boolean paintPage(Graphics g, int hPage, 
                                 int pageIndex) {
            if (pageIndex > m_pageIndex) {
                m_firstOnPage = m_lastOnPage + 1;
                if (m_firstOnPage >= getViewCount())
                    return false;
                m_pageIndex = pageIndex;
            }
            int yMin = getOffset(Y_AXIS, m_firstOnPage);
            int yMax = yMin + hPage;
            Rectangle rc = new Rectangle();
            
            for (int k = m_firstOnPage; k < getViewCount(); k++) {
                rc.x = getOffset(X_AXIS, k);
                rc.y = getOffset(Y_AXIS, k);
                rc.width = getSpan(X_AXIS, k);
                rc.height = getSpan(Y_AXIS, k);
                if (rc.y+rc.height > yMax)
                    break;
                m_lastOnPage = k;
                rc.y -= yMin;
                paintChild(g, rc, k);
            }
            return true;
        }
    }
    
}

