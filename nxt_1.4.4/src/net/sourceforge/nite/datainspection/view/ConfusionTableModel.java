package net.sourceforge.nite.datainspection.view;

import net.sourceforge.nite.datainspection.calc.*;
import net.sourceforge.nite.datainspection.data.*;

import javax.swing.table.AbstractTableModel;
/**
 * TableModel for a JTable that shows a confusion matrix
 * @author Rieks op den Akker
 */
public class ConfusionTableModel extends AbstractTableModel{
    ConfusionMatrix data;

    public ConfusionTableModel(ConfusionMatrix cm){
    	super();
    	data=cm;	
    }
    //private String[] columnNames; 
    
    public String getColumnName(int col) {
    	if (col==0) return "CONFUSION";
        if (col==data.size()+1) return "TOTAL";
        return data.getValue(col-1).toString();
    }
    public int getRowCount() { return data.size()+1; }
    public int getColumnCount() { return data.size()+2; }

    public Object getValueAt(int row, int col) {
    	if (col==0) {
    	    if (row==data.size()) 
    	        return "TOTAL";
    		return data.getValue(row);
    	}
    	if ((row<data.size()) && (col<data.size()+1))
    		return new Double(data.entry(row,col-1));
        if ((row==data.size()) && (col==data.size()+1))
            return new Double(data.totalItems());
        if (row==data.size()) 
            return new Double(data.totalColumn(col-1));
        if (col==data.size()+1) 
            return new Double(data.totalRow(row));
        return "ERROR!";
    }
    
    public Value getValue(int index){
    	return data.getValue(index);	
    }
 
}
