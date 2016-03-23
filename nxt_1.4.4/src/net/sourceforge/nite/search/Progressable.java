package net.sourceforge.nite.search;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unbekannt
 * @version 1.0
 */

public interface Progressable
{
  public void addProgressListner(ProgressListener listener);

  public void removeProgressListner(ProgressListener listener);
}