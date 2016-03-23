package net.sourceforge.nite.gui.util;
import java.util.logging.*;
import javax.swing.*;
import java.awt.*;


/**
 * Utility class for maintaining a status bar through the logging API.
 * Allows visualisation of messages from loggers; you can use this in one of several ways:
 * <ul>
 * <li> Request the panel on which the log messages are shown (and add panel to your interface)
 * <li> Request an JIngernalFrame that contains those same messages
 * <li> use 'createStatusFrame' to add this internal frame directly to a given JDesktopPane at a given location and size
 * </ul>
 * @author Dennis Reidsma, UTwente
 */
public class MyLogHandler extends Handler {
   //DR: possible extension: use limited queue of messages, discard after XX lines... 
    protected JEditorPane logPanel = null;
    private int max_length = 1000;
    protected JInternalFrame iframe = null;
    
    public MyLogHandler() {
        //the important method is getPanel, which returns a JPanel where the logging is visualized
        
    }
    
    public JComponent getPanel() {
        if (logPanel == null) {
            createLogPanel();
        }
        return logPanel;
    }
    public JInternalFrame getInternalFrame() {
        if (iframe == null) {
            createIFrame();
        }
        return iframe;
    }
    protected void createIFrame() {
        iframe = new JInternalFrame("Status and Feedback Window", true, false, true, true);
        JComponent jp = getPanel();
        jp.setPreferredSize(new Dimension(500,100));
        JScrollPane scroller = new JScrollPane(jp);
        iframe.getContentPane().add(scroller);
    }
    public JInternalFrame createStatusFrame(JDesktopPane desktop, int x, int y, int width, int height) {
        JInternalFrame logFrame = getInternalFrame();
        logFrame.setVisible(true);                    
        logFrame.setSize(width,height);
        logFrame.setLocation(x,y);
        desktop.add(logFrame);
        return logFrame;
    }
    
    protected void createLogPanel() {
        logPanel = new JEditorPane();
        logPanel.setEditable(false);
        try {
            logPanel.getDocument().insertString(0,"<<:START",null);
        } catch (javax.swing.text.BadLocationException ex) {
        }
    }

    public void close()  {
          //Close the Handler and free all associated resources. 
          if (iframe != null) {
            iframe.dispose();
        }
    }
        
    public void flush()  {
          //Flush any buffered output. 
    }

    public void publish(LogRecord record) {
        if (isLoggable(record)) {
            try {
                if (getFormatter() == null) {
                    logPanel.getDocument().insertString(0,record.getMessage()+"\n",null);
                    logPanel.setCaretPosition(0);
                } else {
                    logPanel.getDocument().insertString(0,getFormatter().format(record),null);
                    logPanel.setCaretPosition(0);
                }
                if (logPanel.getDocument().getLength() > max_length) {
                    logPanel.getDocument().remove(0,max_length/2);
                }
            } catch (javax.swing.text.BadLocationException ex) {
            }
        }
    }

}