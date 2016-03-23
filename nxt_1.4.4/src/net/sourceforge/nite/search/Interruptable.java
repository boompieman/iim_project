/* NXT (NITE XML Toolkit) Search
 * NITE (Natural Interactivity Tools Engineering) project
 * IMS, University of Stuttgart
 * Holger Voormann
 */
package net.sourceforge.nite.search;

/**
 * Interface with the one and only methode {@linkplain #interrupt()}
 * to stop something.
 */
public interface Interruptable
{
  /**
   * Stops a running methode.
   */
  public void interrupt();
}