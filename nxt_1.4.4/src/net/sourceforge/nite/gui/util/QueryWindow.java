/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.util;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Calendar;
import java.io.*;
import javax.swing.filechooser.FileFilter;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.border.*;

import net.sourceforge.nite.query.*;
import net.sourceforge.nite.search.*;
import net.sourceforge.nite.nom.nomwrite.*;
import net.sourceforge.nite.nom.nomwrite.impl.*;

import ims.jmanual.*;

/**
 * Provides a search interface window to allow interaction with other
 * interfaces. Search interface adapted from code by Holger Voormann
 * and Halyna Seniv
 *
 * @author jonathan 
 *
 * @deprecated - This class is not expected to be maintained. Please use net.sourceforge.nite.search.GUI instead
 */
@Deprecated public class QueryWindow implements QueryHandler, TreeSelectionListener, ActionListener, Runnable, HelpViewerListener {
    private static final String DAT_SERIALISATION = ".nxtLocalSearch";
    private S preferences = new S();
    private Vector queryHandlers = new Vector(); // of type QueryHandler
    private ArrayList bookmarksNames    = new ArrayList();
    private ArrayList bookmarksQueries  = new ArrayList();
    private JMenu bookmarksMenu = null;
    private JMenuItem addBookmark = null;
    private JMenu deleteBookmark = null;
    private JMenuItem save = new JMenuItem("Save ...", 'S');
    private Engine searchEngine = new Engine();    
    private NOMWriteCorpus corpus=null;
    private JFrame main_window=null;
    private JTabbedPane mainPanel = new JTabbedPane();
    private JTextArea qin = new JTextArea(12, 42);
    private JLabel statusBar=null;
    private DefaultMutableTreeNode top = new DefaultMutableTreeNode("loading...");
    private JTree outTree = new JTree(top);
    private JButton submit=null;
    private ProgressWindow progressWindow;
    private List allResults=null;
    private HashMap treehash= new HashMap();
    private boolean searching=false;
    private JMenuBar menubar=null;
    private JFileChooser fc=null;
    private File helpDir;

    /** just set up a query handler for the NOM - no window popped up! */
    public QueryWindow(NOMWriteCorpus nom) {
	corpus=nom;
	// load preferences
	File file = new File(System.getProperty("user.home") + File.separator + DAT_SERIALISATION);
	try {
	    ObjectInputStream is = new ObjectInputStream(new FileInputStream(file));
	    preferences  = (S)is.readObject();
	} catch (Exception e) {
	    //System.out.print("First start ...");
	    file.delete();
	    preferences = new S();
	}
	bookmarksNames   = preferences.bookmarksNames;
	bookmarksQueries = preferences.bookmarksQueries;
    }

    /** pop up the search window. */
    public void popupSearchWindow() {
	if (main_window==null) {
	    main_window=new JFrame();
	    main_window.setTitle("Search");
	    initializeInterface();
	}
	clear();
	main_window.setVisible(true);
    }

    /** clear the search and results panels */
    private void clear() {

    }

    /** set up the search and results panels */
    private void initializeInterface() {
	JPanel      queryPanel   = new JPanel( new BorderLayout() );
	JPanel      resultsPanel = new JPanel( new BorderLayout() );
	queryPanel.setBorder( new EmptyBorder(4, 4, 0, 4) );
	JPanel queryButtonsPanel = new JPanel(
					      new FlowLayout(FlowLayout.RIGHT, 0, 6) );

	//menubar
	menubar = new JMenuBar();
	main_window.setJMenuBar(menubar);

	statusBar = new JLabel();

	//menu: Result
	JMenu resultMenu = new JMenu("Result");
	resultMenu.setMnemonic('R');
	menubar.add(resultMenu);
	save.setActionCommand("save");
	save.addActionListener(this);
	save.setEnabled(false);
	resultMenu.add(save);

	// bookmarks
	bookmarksMenu = new JMenu("Bookmarks");
	bookmarksMenu.setMnemonic('B');
	menubar.add(bookmarksMenu);
	addBookmark = new JMenuItem("Add Bookmark ...", 'A');
	addBookmark.setActionCommand("addBookmark");
	addBookmark.addActionListener(this);
	bookmarksMenu.add(addBookmark);
	if ( bookmarksNames.size() > 0 ) {
	    deleteBookmark = makeSubMenu("Delete Bookmark", "delete", 'D');
	    bookmarksMenu.add(deleteBookmark);
	    bookmarksMenu.addSeparator();
	    for( int i=0; i<bookmarksNames.size(); i++ ) {
		bookmarksMenu.add( makeMenuItem( (String)bookmarksQueries.get(i), "bookmark"+i, this) );
	    }
	}

	//help
	helpDir    = new File( System.getProperty("user.dir") );
	try {
	    helpDir = new File( "." + File.separator + "lib" );
	} catch( Exception e ){}  //skip
	JMenu helpMenu = new JMenu("Help");
	helpMenu.setMnemonic('H');
	menubar.add(helpMenu);
	//JManual
	try {
	    String homeid = "titlepage";
	    String helpwindowtitle = "Help Window";

	    HelpViewer helpview = new HelpViewer(
                                  "helpset",    //name main file (without .hs)
                                  "NXT Search - User's Manual", //window title
                                  "titlepage",  //homeID
                                   this,        //listener
                                   main_window);       //parent
	    /* old invocation...
	    HelpViewer helpview =
		new HelpViewer( "jar:file:"
				+ helpDir.getPath()
				+ File.separator
				+ "helpset.jar!/helpset.hs",
				helpwindowtitle,
				homeid,
				this,   // HelpViewerListener -> this
				main_window ); // parent -> this
	    */
	    JMenuItem helpJMI = new JMenuItem("Contents", 'C');
	    helpJMI.setActionCommand("Help");
	    helpJMI.addActionListener(helpview.getActionListener());
	    helpJMI.setEnabled(true);
	    helpMenu.add(helpJMI);
	} catch( HelpViewerException e ){ 
	    statusBar.setText("help failed."); 
	}
	
	
	//qin - Query INput field
	//	qin.addCaretListener(main_window);
	qin.setLineWrap(true);
	//submit button
	submit = new JButton( "Search" );
	submit.setActionCommand("submit");
	submit.addActionListener(this);
	//	submit.setEnabled(false);
	queryButtonsPanel.add( submit );
	main_window.getContentPane().add( statusBar, BorderLayout.SOUTH);

	//	main_window.setSize(400, 400);

	//assemble queryPanel
	queryPanel.add(queryButtonsPanel, "South");
	queryPanel.add(new JScrollPane(qin), "Center");
	//assemble resultsPanel
	outTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	outTree.addTreeSelectionListener(this);
	resultsPanel.add( new JScrollPane(outTree), "Center");

	mainPanel.addTab("Query", queryPanel);
	mainPanel.addTab("Results", resultsPanel);
	main_window.getContentPane().add( mainPanel, "Center" );
	//disable resultsPanel
	mainPanel.setEnabledAt( 1, false );

	//show window
	main_window.pack();
	main_window.setLocation(100, 200);
    }

    /** add an interface element that will be informed of results. */
    public void registerResultHandler(QueryResultHandler display) {
	queryHandlers.addElement((Object)display);	
    } 
    
    /** remove an interface element that will be informed of results  */
    public void deregisterResultHandler(QueryResultHandler display) {
	queryHandlers.removeElement((Object)display);
    } 

    /**
     * Perform the query and display the results on-screen
     */
    public void performQuery(String newQuery) {
	try {
	    searching=true;
	    List elist = searchEngine.search(corpus, newQuery);
	    searching=false;
	} catch (Throwable e) {
	    e.printStackTrace();
	}
    }

    /** TreeSelectionEvents - highlight the result on the registered displays */
    public void valueChanged (TreeSelectionEvent tse) {
	if (searching) { return; }
	if (tse.getNewLeadSelectionPath()==null) { return; }
	DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) tse.getNewLeadSelectionPath().getLastPathComponent();
	Object o = treehash.get(dmtn);
	if (o==null) { return; }
	if (o instanceof NOMElement) {
	    NOMElement ne = (NOMElement) o;
	    for (Iterator qhit=queryHandlers.iterator(); qhit.hasNext(); ) {
		QueryResultHandler qh = (QueryResultHandler) qhit.next();
		qh.acceptQueryResult(ne);
	    }
	} else if (o instanceof List) {
	    List l = (List) o;
	    for (Iterator qhit=queryHandlers.iterator(); qhit.hasNext(); ) {
		QueryResultHandler qh = (QueryResultHandler) qhit.next();
		qh.acceptQueryResults(l);
	    }
	}
    }

    private JMenuItem makeMenuItem(String label, String action, ActionListener al) {
	JMenuItem ret = new JMenuItem(label);
	ret.setActionCommand(action);
	ret.addActionListener(al);
	return ret;
    }

    private JMenu makeSubMenu(String label, String action, char mnemonic) {
	JMenu ret = new JMenu(label);
	ret.setMnemonic(mnemonic);
	for (int i=0; i<bookmarksNames.size(); i++) {
	    JMenuItem bookmark = makeMenuItem((String)bookmarksQueries.get(i), action, this);
	    bookmark.setName((String)bookmarksNames.get(i));
	    ret.add(bookmark);
	}
	return ret;
    }


    public void savePreferences() {
	
	try {
	    File file = new File( System.getProperty("user.home")
				  + File.separator
				  + DAT_SERIALISATION);
	    if( file.exists() ) { file.delete(); }
	    ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
	    preferences.bookmarksQueries  = bookmarksQueries;
	    preferences.bookmarksNames = bookmarksNames;
	    out.writeObject(preferences);
	    out.close();
	} catch (Exception e){
	    System.out.println(e.toString());
	}
    }

    /** This handles the interface actions */
    public void actionPerformed(ActionEvent event) {
	String cmd = event.getActionCommand();
	if ( cmd.equals("submit") ) {
	    if ( qin.getText().trim().length() == 0 ) {
		JOptionPane.showMessageDialog( main_window,
					       "Please specify a query",
					       "Syntax Error",
					       JOptionPane.ERROR_MESSAGE );
	    } else {
		progressWindow = new ProgressWindow(main_window, "Searching...", this, searchEngine, searchEngine);
		progressWindow.show();
	    }
	} else if ( cmd.equals("save") ) {
	    int confirmValue = 1;
	    String resultAsXML = null;
	    if (allResults != null) resultAsXML = Engine.resultToXML(allResults);
	    if (resultAsXML != null) {
		if ( searchEngine.isInterrupted()) {
		    confirmValue = JOptionPane.showConfirmDialog(main_window, "The xml file is not complete. Save anyway?", "Confirm", JOptionPane.YES_NO_OPTION);
		    if ( confirmValue == JOptionPane.NO_OPTION ) { return; }
		}
		fc = new JFileChooser( preferences.getSaveDir() );
		fc.setFileFilter(new MyFileFilter("xml"));
		int returnVal = fc.showSaveDialog(main_window);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
		    File file = fc.getSelectedFile();
		    if ( file.exists() ) {
			confirmValue = JOptionPane.showConfirmDialog(main_window, "The xml file exists. Overwrite?", "Confirm", JOptionPane.YES_NO_OPTION);
			if ( confirmValue == JOptionPane.NO_OPTION) { return; }
		    }
		    FileFilter ff = fc.getFileFilter();
		    try {
			BufferedWriter xml = null;
			if ( ff.getDescription().equals("xml files") && !file.getName().endsWith(".xml") ) {
			    File xmlFile = new File(file.getAbsolutePath() + ".xml");
			    xml = new BufferedWriter( new FileWriter(xmlFile) );
			} else {
			    xml = new BufferedWriter( new FileWriter(file) );
			}
			xml.write(resultAsXML);
			xml.close();
			preferences.saveDir = file.getParentFile();
		    } catch(IOException e) {
			e.printStackTrace(System.out);
		    }
		}
	    }
	} else if ( cmd.equals("addBookmark") ) {
	    if ( qin.getText().trim().length()>0 ) {
		BookmarkDialog dialog = new BookmarkDialog(main_window, "Add Bookmark", qin.getText(), true);
		dialog.setLocationRelativeTo(main_window);
		dialog.show();
		if ( dialog.submitted ) {
		    if ( bookmarksNames.size()==0 ) {
			deleteBookmark = makeSubMenu("Delete Bookmark", "delete", 'D');
			bookmarksMenu.add(deleteBookmark);
			bookmarksMenu.addSeparator();
		    }
		    bookmarksQueries.add(dialog.getName());
		    bookmarksNames.add(qin.getText());
		    bookmarksMenu.add( makeMenuItem( dialog.getName(), "bookmark"+(bookmarksNames.size()-1), this) );
		    JMenuItem bookmark = makeMenuItem( dialog.getName(), "delete", this);
		    bookmark.setName(qin.getText());
		    deleteBookmark.add(bookmark);
		}
	    }
	} else if( cmd.startsWith("bookmark") ) {
	    try {
		int i = Integer.parseInt( cmd.substring(8) );
		mainPanel.setSelectedIndex(0);
		qin.setText( (String)bookmarksNames.get(i) );
	    } catch(NumberFormatException e){}
	} else if ( cmd.equals("delete") ) {
	    JMenuItem bookmark = (JMenuItem)event.getSource();
	    int index = bookmarksNames.indexOf(bookmark.getName());
	    if ( bookmarksNames.size() > 0 ) {
		bookmarksMenu.remove(index+3);
		deleteBookmark.remove(bookmark);
		bookmarksNames.remove(bookmark.getName());
		bookmarksQueries.remove(index);
		if ( bookmarksNames.size() == 0){
		    //better solution???
		    bookmarksMenu.removeAll();
		    bookmarksMenu.add(addBookmark);
		} else {
		    // generate bookmarks new
		    bookmarksMenu.removeAll();
		    bookmarksMenu.add(addBookmark);
		    JMenu deleteBookmark = makeSubMenu("Delete Bookmark", "delete", 'D');
		    bookmarksMenu.add(deleteBookmark);
		    bookmarksMenu.addSeparator();
		    for( int i=0; i<bookmarksNames.size(); i++ ) {
			bookmarksMenu.add( makeMenuItem( (String)bookmarksQueries.get(i), "bookmark"+i, this) );
		    }
		}
	    }
	} else {
	    System.out.println("ERROR - UNKNOWN COMMAND " + cmd );
	}
    }


  /**
   * New query will be executed by a new thread.
   */
  public void run() {
      try {
	  long start = Calendar.getInstance().getTime().getTime();
	  statusBar.setText("Processing the query ...");
	  
	  StringBuffer result = new StringBuffer();
	  allResults = searchEngine.search( corpus, qin.getText() );
	  
	  mainPanel.setEnabledAt( 1, true );
	  mainPanel.setSelectedIndex( 1 );
	  long duration = Calendar.getInstance().getTime().getTime() - start;
	  
	  statusBar.setText( ( !searchEngine.isInterrupted() ?
			       "Processing took " :
			       "Interrupted after " )
			     + (duration/1000)
			     + "."
			     + (duration%1000)
			     + " seconds." );
	  
	  // clear tree
	  top.removeAllChildren();
	  ((DefaultTreeModel)outTree.getModel()).reload(); // otherwise the old tree will be shown
	  
	  // matchlist
	  if( allResults.size()>=1 ) {
	      top.setUserObject( "<matchlist size=\"" + (allResults.size()-1) + "\">" );
	      // build tree
	      resultToTree(allResults, top);
	  } else {
	      top.setUserObject("<matchlist size=\"0\">");
	  }
	  
	  // expand first level
	  outTree.expandPath( new TreePath(top) );
	  save.setEnabled(true);
	  mainPanel.validate();
	  mainPanel.repaint();
	  main_window.validate();
	  main_window.repaint();
	  
      } catch (Throwable e) {
	  if ( (progressWindow != null) && progressWindow.isVisible() ) {
	      progressWindow.stop();
	  }
	  
	  JOptionPane.showMessageDialog( main_window,
					 e.getMessage(),
					 "Syntax Error",
					 JOptionPane.ERROR_MESSAGE );
	  try {
	      ParseException parseException = (ParseException)e;
	      int x = parseException.currentToken.beginColumn;
	      int y = parseException.currentToken.beginLine;
	      int pos = qin.getDocument().getDefaultRootElement().getElement(y-1).getStartOffset()
                  + (x-1);
	      qin.grabFocus();
	      qin.setCaretPosition( pos );
	  } catch( Exception castException){} 
      }
      

      // close progress window
      if ( progressWindow != null ) {
	  progressWindow.setVisible(false);
	  progressWindow.dispose();
      }
  }
    
    
  private String simplifyString(String s)  {
    StringBuffer ret = new StringBuffer();
    char c;
    boolean isLastWhitespace = true,
            isWhitespace;
    for (int i=0; i<s.length(); i++) {
      c = s.charAt(i);
      isWhitespace = Character.isWhitespace(c);
      if ( !(isWhitespace && isLastWhitespace) ) {
        if (isWhitespace) {
          ret.append(' ');
        } else if (  ( (c == '(') || (c == ')') )
                  && !isLastWhitespace ) {
          ret.append(' ');
          ret.append(c);
        } else {
          ret.append(c);
        }
      }
      isLastWhitespace = isWhitespace || (c == '(') || (c == ')');
    }
    return ret.toString().trim();
  }

  private void resultToTree(java.util.List matchList, DefaultMutableTreeNode parent) {
      int n=1;
      boolean first = true;
      java.util.List variables = null;
      for( Iterator i=matchList.iterator(); i.hasNext(); ) {
	  java.util.List list = (java.util.List)i.next();
	  if(first) {
	      variables = list;
	      first = false;
	  } else {
	      DefaultMutableTreeNode match = new DefaultMutableTreeNode("<match n=\""+ n++ +"\">");
	      parent.add(match);
	      treehash.put(match, new ArrayList());
	      int nn = 0;
	      for( Iterator j=list.iterator(); j.hasNext(); ) {
		  Object item = j.next();
		  try {
		      String title=" ";
		      NOMElement x = (NOMElement)item;
		      title = x.getName() + " " + x.getID();
		      DefaultMutableTreeNode itemNode = new DefaultMutableTreeNode(title);
		      match.add( itemNode );
		      //		      itemNode.setUserObject(x);
		      treehash.put(itemNode, x);
		      ((List)treehash.get(match)).add(x);
		  } catch(ClassCastException e) {
		      try {
			  java.util.List subMatchList = (java.util.List)item;
			  DefaultMutableTreeNode subMatchListNode =
			      new DefaultMutableTreeNode(
							 "<matchlist type=\"sub\" size=\"" + (subMatchList.size()-1) + "\">" );
			  match.add(subMatchListNode);
			  resultToTree(subMatchList, subMatchListNode);
		      } catch(ClassCastException f) {}
		  }
	      }
	  }
      }
  }

    class MyFileFilter extends FileFilter  {
	String type;
	
	public MyFileFilter(String type) { this.type = type; }
	
	// accept all directories and .xml files
	public boolean accept(File f)
	{
	    if (f.isDirectory()) { return true; }
	    
	    // get extension
	    String extension = null;
	    String s = f.getName();
	    int i = s.lastIndexOf('.');
	    if (i > 0 &&  i < s.length() - 1) {
		extension = s.substring(i+1).toLowerCase();
	    }
	    
	    return (extension == null) ?
		false :
		(extension.equals(type) ? true : false);
	}
	// the description of this filter
	public String getDescription() { return type + " files"; }
	
    }

  /////////////////////////////////////////////////////////////////////////////
  // BookmarkDialog
  class BookmarkDialog extends JDialog implements ActionListener
   {
     boolean submitted = false;
     String bookmarkName;
     JTextField nameField = new JTextField(15);

     public BookmarkDialog(Frame owner, String title, String query,
                           boolean modal) {
       super(owner, title, modal);
       setSize(300, 400);
       JPanel pane1 = new JPanel();
       JPanel pane2 = new JPanel();
       JLabel   label = new JLabel("Name:");
       JButton cancel = new JButton("Cancel");
       JButton submit = new JButton("Ok");
       getContentPane().setLayout(new GridLayout(2, 2, 15, 10));
       pane1.setLayout(new FlowLayout(5));
       pane2.setLayout(new GridLayout(1, 1, 50, 0));
       getRootPane().setDefaultButton(submit);
       nameField.setText(query.length() < 16 ? query : query.substring(0, 16));
       nameField.selectAll();
       cancel.setActionCommand("cancel");
       cancel.addActionListener(this);
       submit.setActionCommand("ok");
       submit.addActionListener(this);
       pane1.add(label);
       pane1.add(nameField);
       pane2.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
       pane2.add(submit);
       pane2.add(cancel);
       getContentPane().add(pane1);
       getContentPane().add(pane2);
       pack();
     }

     public String getName() {
       return bookmarkName;
     }

     public void setName(String name) {
       bookmarkName = name;
     }

     public boolean isSubmitted() {
       return submitted;
     }

     public void setSubmitted(boolean submitted) {
       this.submitted = submitted;
     }

     public void actionPerformed(ActionEvent e) {
       String cmd = e.getActionCommand();
       if (cmd.equals("cancel")) {
         stop();
       }
       else if (cmd.equals("ok")) {
         setName(nameField.getText());
         setSubmitted(true);
         this.setVisible(false);
       }
     }

     public void stop() {
       setVisible(false);
       dispose();
     }

  }

   //set query from JManual
   public void querySubmitted(String query)
   {
     mainPanel.setSelectedIndex(0);
     qin.setText(query);
     qin.grabFocus();
   }
    
}

