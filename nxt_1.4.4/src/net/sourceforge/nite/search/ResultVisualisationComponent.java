/* NXT (NITE XML Toolkit) Search
 * NITE (Natural Interactivity Tools Engineering) project
 * IMS, University of Stuttgart
 * Holger Voormann
 */
package net.sourceforge.nite.search;

/**
 * The result of a query is a tree. The leafs of the tree are
 * {@linkplain net.sourceforge.nite.nomread.NOMElement}s. By selecting
 * one of this elements, in anohter part of the window the element
 * and maybe neighbouring elements will be shown.
 */
public abstract class ResultVisualisationComponent
extends javax.swing.JComponent
{

  /**
   * Creates a new JComponent to show a result element.
   * @param gui the grahic user interface using this component
   */
  public ResultVisualisationComponent(GUI gui){}
  
  /**
   * Initialise the result visualisation component.
   */
  public void initialise(){}

  /**
   * By selecting an element in the result tree, this methode
   * will be fired to show the current element.
   * @param element the current selected element
   */
  abstract public void showElement(Object element);

  /**
   * By selecting an element in the result tree, this methode
   * will be fired to show all terminal nodes dominated by the
   * current element.
   * @param elements the current selected element
   */
  abstract public void showElements(java.util.List elements);
}