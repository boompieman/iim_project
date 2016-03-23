/* NXT (NITE XML Toolkit) Search
 * NITE (Natural Interactivity Tools Engineering) project
 * IMS, University of Stuttgart
 * Holger Voormann
 */
package net.sourceforge.nite.search;

import javax.swing.JTextArea;
import java.util.Iterator;
import java.util.List;
// JNOM
//import net.sourceforge.nite.nomread.NOMElement;
//import net.sourceforge.nite.nomread.NOMAttribute;
// NOM
import net.sourceforge.nite.nom.nomwrite.NOMElement;
import net.sourceforge.nite.nom.nomwrite.NOMAttribute;


/**
 * This is very simple textual visualisation of a single result element.
 */
public class SimpleResultVisualisation
extends JTextArea
{
  private GUI gui;

  public SimpleResultVisualisation(GUI gui)
  {
    super(8, 40);
    this.gui = gui;
    showElement(null);
  }

  public void showElement(Object element)
  {
    StringBuffer text = new StringBuffer(" Please select an element above!");
    if( element != null ) {
      try{
        NOMElement el = (NOMElement)element;
        text = new StringBuffer();
        // XLink
        text.append( el.getXLink() ).append("\n");
        // type
        text.append( el.getName() ).append("[");
        // attributes
        boolean first = true;
        for( Iterator i=el.getAttributes().iterator(); i.hasNext(); ) {
          NOMAttribute attr = (NOMAttribute)i.next();
          text.append( first ? "" : " ").append( attr.getName() ).append("=\"");
          if( first ){ first = false; }
          text.append( attr.getStringValue() ).append("\"");
       }
       text.append("]\n");
       // time
        text.append("time: ");
        if(  !Double.isNaN( el.getStartTime() )
             && !Double.isNaN( el.getEndTime()   ) ) {
          text.append("start=").append( el.getStartTime() );
          text.append(" end=").append( el.getEndTime() ).append("\n");
        } else {
          text.append("not timed.");
        }


        // ONLY JNOM !!!!!!!
//        // layers
//        text.append("\nSublayer ").append( gui.getSublayer(el) );
//        List l = gui.getLayers();
//        text.append("\n");
//        for(int i=0; i<l.size(); i++) {
//          String layer = l.get(i).toString();
//          text.append(layer).append(" (");
//          text.append( gui.getNumberOfSublayers(layer) ).append(") - ");
//        }

      } catch(ClassCastException e) {}

      // layers
      List l = gui.getLayers();
      text.append("\n").append(l.size()).append("\n layers (sublayers): \n");
      for(int i=0; i<l.size(); i++) {
        String layer = l.get(i).toString();
        text.append(layer).append(" (");
        text.append( gui.getNumberOfSublayers(layer) ).append(") ");
//        text.append(") - ");
      }

    }
//    text = (element == null) ?
//             new StringBuffer("null"):
//             new StringBuffer( element.getClass().toString() );
    setText( text.toString() );
  }

}