/* NXT (NITE XML Toolkit) Search
 * NITE (Natural Interactivity Tools Engineering) project
 * IMS, University of Stuttgart
 * Holger Voormann
 */
package net.sourceforge.nite.search;

import java.util.List;
import java.awt.BorderLayout;

/**
 * Wrapper around {@linkplain SimpleResultVisualisation} to use it in the
 * NXT Search {@linkplain GUI}.
 */
public class SimpleResultVisualisationComponent
extends ResultVisualisationComponent
{
  private SimpleResultVisualisation visu;

  public SimpleResultVisualisationComponent(GUI gui)
  {
    super(gui);
    setLayout( new BorderLayout() );
    visu = new SimpleResultVisualisation(gui);
    add(visu);
  }

  public void showElement(Object element) { visu.showElement(element); }

  public void showElements(List elements){}

}