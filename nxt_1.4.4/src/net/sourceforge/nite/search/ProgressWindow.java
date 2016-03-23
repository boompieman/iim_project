/* NXT (NITE XML Toolkit) Search
 * NITE (Natural Interactivity Tools Engineering) project
 * IMS, University of Stuttgart
 * Holger Voormann
 */
package net.sourceforge.nite.search;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Dialog with a chancel button to stop a running thread
 * (used by {@linkplain GUI}).
 */
public class ProgressWindow
extends JDialog
implements ActionListener, ProgressListener
{
  Interruptable interruptable;

  private JLabel text = new JLabel("Tested: 0 - Found: 0");

  public ProgressWindow( Frame         owner,
                         String        titel,
                         Runnable      thread,
                         Interruptable interruptable,
                         Progressable  progressable)
  {
      //super(owner, titel, true);
    super(owner, titel, false);
    progressable.addProgressListner(this);
    getContentPane().setLayout( new BorderLayout() );

    // progress label
    getContentPane().add("Center", text);
    text.setPreferredSize( new Dimension( text.getPreferredSize().width + 100,
                                          text.getPreferredSize().height ) );

    // cancel button
    JButton cancel = new JButton("Cancel");
    cancel.setActionCommand("cancel");
    cancel.addActionListener(this);
    getContentPane().add("South", cancel);

    pack();
    setLocation( owner.getLocation().x + ( owner.getWidth()  - getWidth()  )/2,
                 owner.getLocation().y + ( owner.getHeight() - getHeight() )/2 );
    this.interruptable = interruptable;
    Thread t = new Thread(thread);
    t.start();

  }

  public void actionPerformed(ActionEvent e)
  {
    if ( e.getActionCommand().equals("cancel") ) {
      stop();
    }
  }

  public void stop()
  {
    interruptable.interrupt();
    setVisible(false);
    dispose();
  }

  private long tested = 0;
  public void progressChanged(long found)
  {
    tested++;
    if( (tested%1000) == 0 ){
//      System.out.println("Tested: "+tested+" - Found: "+found);
      text.setText("Tested: "+tested+" - Found: "+found);
    }
  }

}