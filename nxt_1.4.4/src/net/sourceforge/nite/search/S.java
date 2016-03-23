package net.sourceforge.nite.search;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;

public class S
implements java.io.Serializable
{

  public static String[]   bookmarks    = {
    "($a word)($b): $a@pos == $b@pos",
//    "",
    "($a s)($b word)",
    "($a gest1 | gest2)($b pp)",
    "($a)",
//    "",
    "($a)($b):   $a@orth && $b@pos",
    "($a)($b)($c):   $a@orth && ($b@orth || $c@pos)",
    "($a)($b)($c):   $a@orth && !($b@orth || $c@pos)",
//    "",
    "($a)($b):   $a % $b",
    "($a)($b):   $a [[ $b",
    "($a)($b):   $a ]] $b",
    "($a)($b):   $a @ $b",
    "($a)($b):   $a [] $b",
    "($a)($b):   $a # $b",
    "($a)($b):   $a ][ $b",
    "($a)($b):   $a << $b",
//    "",
    "($b)($c): $b^$c :: ($a): $a^$b",
    "($a)($b)($c): ($a ^ $b or $b ^ $a) and !($a == $b) and $c ^2  $a"
  };

  public List      corpora         = new ArrayList();
  public HashMap   names           = new HashMap();

  public ArrayList bookmarksNames   = new ArrayList();
  public ArrayList bookmarksQueries = new ArrayList();

  public boolean   autoloaded       = false;
  public boolean   corpusLoaded     = false;

  public File      saveDir = new File( System.getProperty("user.dir") );


  public S()
  {
    for( int i = 0; i<bookmarks.length; i++ ){
      bookmarksNames.add( bookmarks[i] );
      bookmarksQueries.add( bookmarks[i] );
    }
  }

  public File getSaveDir() {
    return saveDir.exists() ?
             new File( System.getProperty("user.dir") ) :
             saveDir;
  }
}
