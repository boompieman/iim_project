package net.sourceforge.nite.search;

import java.net.URL;
import java.io.File;
import java.util.List;

//import net.sourceforge.nite.jnom.JNOMCorpus;
//import net.sourceforge.nite.nomread.NOMCorpus;

public class NXTSearch {

  //Main method
  public static void main(String[] args) {
    if ( args.length == 0 ) {
      GUI gui = new GUI("");
      return;
    } else if ( args.length != 2 ) {
      System.out.println("Usage: java NXSearch corpus query");
      return;
    }
    String corpusPath = args[0];
    String query  = args[1];
    //System.out.println("corpusPath: " + corpusPath);
    //System.out.println("query: " + query);
    URL url = null;
//SearchableCorpus corpus = new JNOMCorpus();
SearchableCorpus corpus = null;
    Engine     searchEngine = new Engine();
    List   allResults   = null;
    try {
      File corpusFile = new File(corpusPath);
      if ( !corpusFile.exists() )
        System.out.println("corpus " + corpusFile.getName() + " not found");
      url = corpusFile.toURL();
      //url = (new File(corpusPath)).toURL();
//      ((NOMCorpus)corpus).load(url);
      try {
        allResults = searchEngine.search( corpus, query );
        System.out.println( Engine.resultToXML(allResults) );
      } catch (Throwable t) {
        System.out.println("the query is not correct");
//        t.printStackTrace(System.out);
      }
    } catch (Exception e) {
      System.out.println("Loading corpus failed");
    }
  }

}





