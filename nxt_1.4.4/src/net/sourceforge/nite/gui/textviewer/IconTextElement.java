/**
 * Natural Interactivity Tools Engineering
 * Copyright (c) 2003, Jean Carletta, Jonathan Kilgour, Judy Robertson
 * See the README file in this distribution for licence.
 */
package net.sourceforge.nite.gui.textviewer;

import javax.swing.ImageIcon;

/**
 * This is used for inserting icons into an NTextArea.
 *
 * @author judyr
 */
public class IconTextElement extends NTextElement {
        
        ImageIcon icon;

        /**
         * Returns the icon.
         * @return ImageIcon
         */
        public ImageIcon getIcon() {
            return icon;
        }

        /**
         * Sets the icon.
         * @param icon The icon to set
         */
        public void setIcon(ImageIcon icon) {
            this.icon = icon;
        }

}
