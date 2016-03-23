/* NXT Corpus Manager
 * Copyright (c) 2008, Jean Carletta, Jonathan Kilgour
 * Created by Jonathan Kilgour 29/4/08 
 */
package net.sourceforge.nite.corpus.plugin;

import net.sourceforge.nite.corpus.CorpusTool;
import net.sourceforge.nite.meta.NMetaData;
import net.sourceforge.nite.meta.impl.NiteMetaData;
import net.sourceforge.nite.meta.NOntology;
import net.sourceforge.nite.gui.util.OntologyEditor;
import javax.swing.*;
import java.util.List;
import java.util.Iterator;
import java.awt.event.*;
import com.jgoodies.looks.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

/** Interface to be instantated by all tools that can perform editing
 * or management style tasks at a corpus-wide level. NOT intended for
 * annotation tools! Each CorpusTool must publish a JPanel containing
 * its interface and declare whether any changes need to be saved as
 * well as providing a method to do its own saving. */
public class OntologyTool implements CorpusTool {
    private boolean needssave = false;
    JPanel panel = new JPanel();

    /** provide the interface for this tool */
    public JPanel getPanel(NMetaData metadata) {
	try {
	    List ontologies = metadata.getOntologies();
	    String rowstr = "pref, 2dlu";
	    for (int i=1; i<ontologies.size(); i++) { rowstr += ", pref, 2dlu"; }
	    FormLayout layout = new FormLayout("pref, 2dlu, pref, 2dlu, pref, 2dlu", rowstr);
	    PanelBuilder builder = new PanelBuilder(layout);
	    builder.setDefaultDialogBorder();
	    CellConstraints cc = new CellConstraints();
	    int row=1;
	    for (Iterator oit=ontologies.iterator(); oit.hasNext(); ) {
		NOntology ontology = (NOntology)oit.next();
		builder.add(new JLabel(ontology.getName()), cc.xy(1, row));
		builder.add(new JLabel(ontology.getDescription()), cc.xy(3, row));
		JButton butt = new JButton("edit");
		butt.addActionListener(new MyActionListener(metadata, ontology.getElementName()));
		builder.add(butt, cc.xy(5, row));
		row+=2;
		panel = builder.getPanel();
	    }
	} catch (Exception ex) {
	    panel.add(new JLabel("No ontologies found"));
	    ex.printStackTrace();
	}
	return panel;
    }

    /** return true if outstanding changes are still to be committed */
    public boolean requiresSave() {
	return needssave;
    }

    /** apply the changes to the corpus */
    public void applyChanges() { }

    /** get a handy tip to associate with this app */
    public String getToolTip() {
	return "Edit tool requires prefuse on the classpath";
    }

    /** get a short textual description of this app */
    public String getDescription() {
	return "Edit ontologies using tree layout";
    }

    /** get the (one word) name of this app for naming a tab */
    public String getName() {
	return "Ontologies";
    }

    private class MyActionListener implements ActionListener {
	NMetaData metadata;
	String elname;

	public MyActionListener(NMetaData metadata, String elname) {
	    this.metadata=metadata;
	    this.elname=elname;
	}

	public void actionPerformed(ActionEvent e) {
	    OntologyEditor ontedit = new OntologyEditor((NiteMetaData)metadata, elname);
	    JFrame frameo = ontedit.demo((NiteMetaData)metadata, elname);
	    frameo.setVisible(true);
	}
    }
}
