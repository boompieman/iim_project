package net.sourceforge.nite.datainspection.view;

import net.sourceforge.nite.datainspection.calc.*;
import net.sourceforge.nite.datainspection.data.*;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** 
  * ConfusionTablePanel is a JPanel that shows a JTable with a ConfusionTableModel that encapsulates a ConfusionMatrix 

  * <br>The following is not yet true:<br>
  * By selection of column, row or cell administrated ConfusionTableListeners are send ConfusionPairEvents 
  * Listeners are subscribed by the addConfusionTableListener(ConfusionTableListener) method
 */
public class ConfusionTablePanel extends JPanel {
    
    private boolean ALLOW_COLUMN_SELECTION = true;
    private boolean ALLOW_ROW_SELECTION = true;
    
    private static final int CELL_WIDTH = 40;
    private static final int CELL_HEIGHT = 30;

    private Value rowValue, colValue;

    //private java.util.List listeners;
    TableColumnModel headercolumns;
    
    public ConfusionTablePanel(ConfusionMatrix cm){ 
        //listeners = new java.util.ArrayList();
        int size = cm.size();
        final ConfusionTableModel model = new ConfusionTableModel(cm);
        final JTable table = new JTable(model);
        table.setRowHeight(CELL_HEIGHT);
        table.setPreferredScrollableViewportSize(new Dimension(size*CELL_WIDTH,(size+1)*CELL_HEIGHT));
        table.setCellSelectionEnabled(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        
        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setToolTipText("Click to select a complete column");
        header.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    Point p = e.getPoint();
                    int selectedCol = table.columnAtPoint(p);
                    if (selectedCol>0){
                        colValue = model.getValue(selectedCol-1);
                        rowValue = null;
                        //System.out.println("column header pointed at: "+ colValue);
                        //sendEvent();
                    } 
                }
            });
        //headercolumns.setColumnSelectionAllowed(true);
        //ListSelectionModel headerSM = headercolumns.getSelectionModel();
        //headerSM.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //headerSM.addListSelectionListener(new ListSelectionListener() {
                //public void valueChanged(ListSelectionEvent e) {
                    //Ignore extra messages.
                    //if (e.getValueIsAdjusting()){
                         //sendEvent();
                         //System.out.println("headerSM--getValueIsAdjusting :"+ e.toString());
                         
                         //int selC = table.getSelectedColumn();
                         //int selR = table.getSelectedRow();
                         //String colname = table.getColumnName(selC);
                         //System.out.println("getSelectedColumn:"+ selC);
                         //System.out.println("getSelectedRow:"+ selR);
                         //System.out.println("selected Column Name:"+ colname);
                        // return;
                    //}
                //}});
        
        if (ALLOW_ROW_SELECTION) { // true by default
            ListSelectionModel rowSM = table.getSelectionModel();
            rowSM.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    //Ignore extra messages.
                    if (e.getValueIsAdjusting()){
                         //report("rowSM--getValueIsAdjusting :"+ e.toString());
                         return;
                    }
                    
                    ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                    if (lsm.isSelectionEmpty()) {
                            //report("No rows are selected.");
                    } else {
                        int selectedRow = lsm.getMinSelectionIndex();
                        System.out.println("Row " + selectedRow + " is now selected.");
                        rowValue = model.getValue(selectedRow);
                        //sendEvent();
                    }
                }
            });
        } else {
            table.setRowSelectionAllowed(false);
        }

        if (ALLOW_COLUMN_SELECTION) {
            if (ALLOW_ROW_SELECTION) {
                //We allow both row and column selection, which
                //implies that we *really* want to allow individual
                //cell selection.
                table.setCellSelectionEnabled(true);
            }
            table.setColumnSelectionAllowed(true);
            ListSelectionModel colSM = table.getColumnModel().getSelectionModel();
            colSM.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    //Ignore extra messages.
                    if (e.getValueIsAdjusting()){
                        //report("colSM--getValueIsAdjusting :"+ e.toString());
                         return;     
                    }
                    ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                    if (lsm.isSelectionEmpty()) {
                        //System.out.println("No columns are selected.");
                    } else {
                        int selectedCol = lsm.getMinSelectionIndex();
                        //report("Column "+ selectedCol +" is now selected");
                        if (selectedCol !=0){
                            colValue = model.getValue(selectedCol-1);
                            //sendEvent();
                        } else {
                            colValue = null;    
                            //sendEvent();
                        }
                    }
                }
            });
        }

    //setSize(new Dimension((size)*CELL_WIDTH,(size)*CELL_HEIGHT));
        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(size*CELL_WIDTH,(size)*CELL_HEIGHT));
    JPanel labelpanel = new JPanel();
    //labelpanel.setSize(500,100);
    JLabel label = new JLabel("Kappa:"+cm.kappa());
    labelpanel.add(label);
        //Add the scroll pane to this panel.
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(scrollPane,java.awt.BorderLayout.CENTER);
        add(labelpanel,java.awt.BorderLayout.PAGE_END);
    }
    
    
//    public void addTableListener(TableListener ctlstnr){
//        listeners.add(ctlstnr);    
//    }
    
//    // make a ConfusionTabelEvent and send it to all listeners
//    private void sendEvent(){
//        ValuePairSelectedEvent evt = new ValuePairSelectedEvent(this,rowValue,colValue);
//        TableListener listener;
//        for (java.util.Iterator iter = listeners.iterator();iter.hasNext();){
//                listener = (TableListener)iter.next();
//                if (rowValue==null) listener.columnSelected(evt);
//                else if (colValue==null) listener.rowSelected(evt);
//                else 
//                    listener.cellSelected(evt);
//        }
//        
//    }
    
//    private void report(String txt){
//        TableListener listener;
//        for (java.util.Iterator iter = listeners.iterator();iter.hasNext();){
//                listener = (TableListener)iter.next();
//                listener.append(txt);
//        }    
//    }


    private void printDebugData(JTable table) {
        int numRows = table.getRowCount();
        int numCols = table.getColumnCount();
        javax.swing.table.TableModel model = table.getModel();

        System.out.println("Value of data: ");
        for (int i=0; i < numRows; i++) {
            System.out.print("    row " + i + ":");
            for (int j=0; j < numCols; j++) {
                System.out.print("  " + model.getValueAt(i, j));
            }
            System.out.println();
        }
        System.out.println("--------------------------");
    }
    
    
    /** A convenience method to obtain a JInternalFrame containing exactly one confusion table. */
    public static JInternalFrame getConfusionPanelFrame(String title, ConfusionMatrix cm) {
        ConfusionTablePanel ctp = new ConfusionTablePanel(cm);
        JInternalFrame jif = new JInternalFrame(title,true,false,false,true);
        jif.getContentPane().add(ctp);
        jif.setVisible(true);
        jif.setLocation(200,200);
        jif.setSize(400,400);
        return jif;
    }

    
}