/* NXT (NITE XML Toolkit) Search
 * NITE (Natural Interactivity Tools Engineering) project
 * IMS, University of Stuttgart
 * Holger Voormann
 */
package net.sourceforge.nite.search;

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.JLabel;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
// NOM
import net.sourceforge.nite.nom.nomwrite.NOMElement;
import net.sourceforge.nite.nom.nomwrite.NOMAttribute;

/**
 * Wrapper around {@linkplain SimpleResultVisualisation} to use it in the
 * NXT Search {@linkplain GUI}.
 */
public class TableResultVisualisationComponent
extends ResultVisualisationComponent
{
  private GUI gui;
  private boolean showTime = false;
  private boolean showText = false;
  private int firstTimeColumn = 0;
  private int textColumn = -1;

  private List elements;
  private List attributes;

  private JTextArea nullScreen = new JTextArea(" Please select an element above!");

  public TableResultVisualisationComponent(GUI gui) {
    super(gui);
    this.gui = gui;
    setLayout(new BorderLayout());
    initialise();
    setBackground( new Color(204, 204, 204) );
  }
  
  public void initialise()
  {
    removeAll();
    add(nullScreen);
    nullScreen.setEditable(false);
    nullScreen.setBackground(new Color(204, 204, 204));
  }

  public void showElement(Object element)
  {

    removeAll();
    add(nullScreen);
    repaint();
    StringBuffer text = new StringBuffer(" Please select an element above!");
    if (element != null) {
      try {
        NOMElement el = (NOMElement)element;

        //column names
        List columnNames = new ArrayList();
        for (Iterator i = el.getAttributes().iterator(); i.hasNext(); ) {
          NOMAttribute attr = (NOMAttribute)i.next();
          columnNames.add( attr.getName() );
        }

        showTime = gui.showTime;
        firstTimeColumn = columnNames.size() + 1;
        int columnNumber = showTime ?
                             columnNames.size() + 3 :
                             columnNames.size() + 1;

        boolean showText = false;
        if(  el.getText() != null
          && !el.getText().equals("") ){
          columnNumber++;
          showText = true;
        }


        String[][] content = new String[2][columnNumber];
        for( int i=0; i<content.length; i++ ){
          for( int j=0; j<content[i].length; j++ ){
            content[i][j] = "";
          }
        }

        // row titles
        for( int i=0; i<columnNames.size(); i++ ){
          content[0][i+1] = " " + columnNames.get(i).toString();
        }
        if( showTime ){
          content[0][columnNames.size()+1] = " start()";
          content[0][columnNames.size()+2] = " end()";
        }
        if( showText ){ content[0][columnNumber-1] = " text()"; }

        // fill table
        // a) type
        content[1][0] = " " + el.getName();
        // b) attributes
        for( int i=1; i<=1; i++ ){
          for( Iterator k = el.getAttributes().iterator(); k.hasNext(); ) {
            NOMAttribute attr = (NOMAttribute)k.next();
            content[1][columnNames.indexOf(attr.getName())+1] = " " + attr.getStringValue();
          }
        }
        // c) time
        if( showTime ){
          if(  !Double.isNaN( el.getStartTime() )
            && !Double.isNaN( el.getEndTime() ) ){
            content[1][columnNames.size()+1] = " " + String.valueOf( el.getStartTime() );
            content[1][columnNames.size()+2] = " " + String.valueOf( el.getEndTime() );
          }
          if( showText ){
            content[1][columnNumber-1] = " " + String.valueOf( el.getText() );
          }
        }




        removeAll();
        JTable table = new JTable(content, content[0]);
        table.setBackground( new Color(204, 204, 204) );
        table.setDefaultRenderer( Object.class, new myTableCellRendererSingleRow() );
        removeAll();
        add(table);
        table.doLayout();
      } catch (ClassCastException e) {}

    }
  }

    public void showElements(List listOfElements)
    {
	elements = listOfElements;
	List attributeList = new ArrayList();
	
	//collect all attributes
	boolean timedElements = false;
	boolean texedElements = false;
	int quantifiedcomponents=0;
	for( Iterator it = elements.iterator(); it.hasNext(); ){
	    Object element = it.next();
	    if (!(element instanceof NOMElement)) { quantifiedcomponents++; continue; }
	    NOMElement itemElement = (NOMElement)element;
	    if(  !timedElements
		 && !Double.isNaN( itemElement.getStartTime() )
		 && !Double.isNaN( itemElement.getEndTime()   ) ){
		timedElements = true;
	    }
	    if( !texedElements
		&& (itemElement.getText() != null)
		&& !itemElement.getText().equals("") ){
		texedElements = true;
	    }
	    
	    List atts = itemElement.getAttributes();
	    if (atts!=null) {
		for( Iterator elIt = atts.iterator(); elIt.hasNext(); ){
		    NOMAttribute attr = (NOMAttribute)elIt.next();
		    String attrName = attr.getName();
		    if(  !attributeList.contains(attrName)
			 && !attrName.startsWith("xmlns:")){
			attributeList.add(attrName);
		    }
		}
	    }
	}
	attributes = attributeList;
	
	//time
	firstTimeColumn = attributeList.size() + 2;
	showTime = timedElements;
	
	//text
	showText = texedElements;
	textColumn = showText ?
	    (showTime ? firstTimeColumn+2 : firstTimeColumn) :
	    -1;
	
	
	JTable table = new JTable(new DefaultTableModel( elements.size(),
		 attributeList.size() + 2 + (showTime ? 2 : 0) + (showText ? 1 : 0) ){
		public String getColumnName( int columnIndex )
		{
		    String columnName = "";
		    if (columnIndex == 0) {
			columnName = "XLINK";
		    } else if (columnIndex == 1) {
			columnName = "NAME";
		    } else if (columnIndex < attributes.size()+2){
			columnName = "@" + attributes.get(columnIndex-2).toString();
		    } else if ( showTime && (columnIndex == attributes.size()+2) ){
			columnName = "start()";
		    } else if ( showTime && (columnIndex == attributes.size()+3) ){
			columnName = "end()";
		    } else {
			columnName = "text()";
		    }
		    return columnName;
		}
		
		public boolean isCellEditable( int rowIndex,
					       int columnIndex )
		{
		    return false;
		}
		
		public Object getValueAt( int rowIndex,
					  int columnIndex )
		{
		    Object content = null;
		    if (!(elements.get(rowIndex) instanceof NOMElement)) { return content; }
		    
		    if( columnIndex == 0 ){
			content = ((NOMElement)elements.get(rowIndex)).getXLink();
		    } else if( columnIndex == 1 ) {
			content = ((NOMElement)elements.get(rowIndex)).getName();
		    } else if( columnIndex < firstTimeColumn ){
			try {
			    NOMElement element = (NOMElement) elements.get(rowIndex);
			    NOMAttribute attribute = element.getAttribute(
									  attributes.get(columnIndex-2).toString());
			    content = attribute.getStringValue();
			} catch (Exception ex) {} //skip
		    } else if( showTime && ( columnIndex == firstTimeColumn ) ){
			try {
			    NOMElement element = (NOMElement)elements.get(rowIndex);
			    if( !Double.isNaN( element.getStartTime() ) ){ 
				content = new Double( element.getStartTime() );
			    } else {
                                content = new Double(Double.NaN);
			    }
			} catch (Exception ex) {} //skip                            
		    } else if( showTime && ( columnIndex == firstTimeColumn+1 ) ){
			try {
			    NOMElement element = (NOMElement)elements.get(rowIndex);
			    if (!Double.isNaN(element.getEndTime())) {
                                content = new Double(element.getEndTime());
			    } else {
                                content = new Double(Double.NaN);
			    }
			} catch (Exception ex) {} //skip
		    } else if( columnIndex == textColumn ){
			try {
			    NOMElement element = (NOMElement)elements.get(rowIndex);
			    if( element.getText() == null ){
				content = new StringBuffer( "" );
			    } else {
				content = new StringBuffer( element.getText() );                          
			    }
			} catch (Exception ex) {} //skip
		    } else {
			content = "";
		    }
		    return content;
		}
		
	    } );
	table.setDragEnabled(false);
	
	table.setBackground( new Color(204, 204, 204) );
	table.setDefaultRenderer( Object.class, new myTableCellRenderer() );
	removeAll();
	add( new JScrollPane(table) );
	table.doLayout();
	doLayout();
    }


   class myTableCellRenderer
   extends JLabel
   implements TableCellRenderer
   {
     public Component getTableCellRendererComponent( JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     boolean hasFocus,
                                                     int row,
                                                     int column ){

       JLabel cell = new JLabel( value != null ? value.toString() : "" );
       cell.setOpaque(true);
       cell.setBackground( value == null ?
                             new Color(204, 204, 204) :
                             Color.white );
       
       // start() and end()
       if(  (value != null)
         && ( value.getClass().equals(Double.class) ) ){
         cell.setBackground( new Color(204, 255, 204) );
         if( ((Double)value).isNaN() ){
           cell.setText("");
           cell.setBackground( new Color(102, 153, 102) );
         }
       }
       
       //text()
      if(  (value != null)
        && (value.getClass().equals(StringBuffer.class) ) ){
        cell.setBackground(new Color(204, 204, 255));
        if( value.toString().equals("") ){
          cell.setBackground(new Color(102, 102, 153));
        }
      }
       
       return cell;
     }
   }

   class myTableCellRendererSingleRow
   extends JLabel
   implements TableCellRenderer
   {
     public Component getTableCellRendererComponent( JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     boolean hasFocus,
                                                     int row,
                                                     int column ){
       JLabel cell = new JLabel( value.toString() );
       cell.setOpaque(true);
       cell.setBackground( (row == 0) || (column == 0) ?
                             new Color(204, 204, 204) :
                             Color.white );
       if( showTime && (column >= firstTimeColumn) &&  (column <= (firstTimeColumn+1))){
         cell.setBackground( row == 0 ?
                               new Color(102, 153, 102) :
                               new Color(204, 255, 204) );
       }
       if( showTime && (column >= firstTimeColumn) && (row == 0) ){
         cell.setForeground( Color.white );
       }
       return cell;
     }
   }

}
