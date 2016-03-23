/* NXT (NITE XML Toolkit) Search
 * NITE (Natural Interactivity Tools Engineering) project
 * IMS, University of Stuttgart
 * Holger Voormann
 */
package net.sourceforge.nite.search;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JEditorPane;
import javax.swing.Icon;
import javax.swing.border.EmptyBorder;

/**
 * Non-modal dialog: dangling variable references detected.
 * This non-modal dialog is shown if deleting one or more {@linkplain FBox}es
 * would caused dangling variable references. The user have to make a decision
 * if either nothing or the selected {@linkplain FBox}es and also all
 * {@linkplain FBox}es with dangling references should be deletet.
 * @author Holger Voormann
 */
public class About
extends JDialog
implements ActionListener
{
  private static final String TEXT1 = "<center><p><font face='Arial,Helvetica' color='#0a529a' size='5'><b>";
  private static final String TEXT2 =
      "</b></font><br><font face='Arial,Helvetica' color='#999999' size='3'>developed by</font></p><p><font face='Arial,Helvetica' color='#000000' size='3'><b>Holger Voormann</b><br>holger.voormann@ims.uni-stuttgart.de</font></p><p><font face='Arial,Helvetica' color='#000000' size='3'><b>IMS University Stuttgart (NITE project)</b><br>http://www.ims.uni-stuttgart.de/projekte/nite/</font></p></center>";

  /**
   * Creates a &quot;dangling variable references detected&quot; non-modal
   * dialog with the specified Frame as its owner.
   * If owner is null, a shared, hidden frame will be set as the owner of
   * the dialog.
   * @param owner the {@linkplain java.awt.Frame} from which the dialog is displayed
   */
  public About(Frame owner, Icon image)
  {
    super(owner, GUI.TITLE, true);
    Container c = getContentPane();
    c.setLayout( new BorderLayout(0, 16) );
    c.setBackground(Color.white);

    //image
    JLabel imagePanel = new JLabel(image);
    imagePanel.setBorder( new EmptyBorder(8, 42, 0, 42) );
    c.add( "North", imagePanel );

    //text
    JEditorPane text = new JEditorPane("text/html", TEXT1 + GUI.TITLE + TEXT2);
    text.setEditable(false);
    c.add( "Center", text );

    //close button
    JButton closeButton = new JButton( "Close" );
    closeButton.addActionListener( this );
    //closeButton.setBackground( new Color(189, 214, 58) );
    c.add( "South", closeButton );

    //show window
    pack();
    setResizable(false);
    setLocation( owner.getLocation().x + ( owner.getWidth()  - this.getWidth()  )/2,
                 owner.getLocation().y + ( owner.getHeight() - this.getHeight() )/2 );
    setVisible( true );
  }

  /**
   * The user has clicked the close button.
   * @param event not used
   */
  public void actionPerformed(ActionEvent event)
  {
    setVisible( false );
    dispose();
  }

}
