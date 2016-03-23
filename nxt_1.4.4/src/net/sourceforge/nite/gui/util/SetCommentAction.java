package net.sourceforge.nite.gui.util;

import net.sourceforge.nite.nom.nomwrite.impl.*;
import javax.swing.*;
import java.awt.event.*;

/**
 * A button designed to add a comment to the current NOMWriteElement of the
 * NOMWriteElementContainer passed at construction.
 * Example:
 * DialogueActEditPanel is an NOMWriteElementContainer, i.e. it implements an 'getCurrentElement' function.
 * You can pass this panel to the constructor of SetCommentAction; resulting actions will be usable for example for
 * a button on that same panel that, when pressed, allows user to add/change the comment for the current element of the panel.
 * @author Dennis Reidsma, UTwente
 */
public class SetCommentAction extends AbstractAction {
    NOMWriteElementContainer container;
    public SetCommentAction(String title, NOMWriteElementContainer container) {
        super(title);
        this.container=container;
    }
    public void actionPerformed(ActionEvent e) {
        NOMWriteElement nwe = container.getCurrentElement();
        String comment = nwe.getComment();
        comment = JOptionPane.showInputDialog(null, "Comment", comment);
        if (comment != null) {
            nwe.setComment(comment);
        }
    }
}