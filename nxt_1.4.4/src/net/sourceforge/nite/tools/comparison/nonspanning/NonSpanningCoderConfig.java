package net.sourceforge.nite.tools.comparison.nonspanning;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.nite.tools.necoder.NECoderConfig;

/**
 * Coder configuration settings for NonSpanning Comparison display.
 * <p>
 * Soft config: see NECoderConfig. Hardcoded settings: see overrides below.
 * <p>
 * @author Craig Nicol, University of Edinburgh
 * @see NonSpanningComparisonDisplay
 */
public class NonSpanningCoderConfig extends NECoderConfig {
   
   public String getHelpSetName() {
       return "nonspanningcomparison.hs";
   }
   
   private String getSomeSetting(String settingname) {
       return getNXTConfig().getCorpusSettingValue(settingname);
   }
   
   /**
    * extracodings is used to load additional trees into
    * the comparison view by defining the root layer.
    * Layer names are separated by semicolons.
    * It is not required to list layers included in the
    * hierarchies whose roots are in the annotator or
    * common layers. 
    * 
    * @return List of Strings of requested layer names.
    */
   public List getExtraCodings() {
           String extracodings = getSomeSetting("extracodings");
           if (extracodings == null || extracodings.length() == 0) {
	      return new ArrayList(0);
	   }
      
   	   String[] codingnames = extracodings.split(";");
   	   List codings = new ArrayList();
   	   for (int i = 0; i < codingnames.length; i++) {
   	   		codings.add((Object)codingnames[i]);
   	   }
   	   return codings;
   }
   
  	/** 
  	 * @return contents of nsannotatorlayer in corpus settings
  	 * nsannotatorlayer defines the layer to be compared
  	 * for differences between annotators.
  	 * The display may show elements of nsannotatorlayer or
  	 * elements of one of its children depending on the value
  	 * of neelementname.
  	 */	
   public String getAnnotatorLayer() {
   		return getSomeSetting("nsannotatorlayer");
   }

   	/** 
   	 * @return contents of nscommonlayer in corpus settings
   	 * nscommonlayer defines the common data shared by all
   	 * annotators.
   	 */	
   public String getCommonLayer() {	
		return getSomeSetting("nscommonlayer");
   }
   
   public boolean getAllowNestedNamedEntities() {
      String nest =  getNXTConfig().getCorpusSettingValue("nenesting");
      if (nest!=null && nest.equalsIgnoreCase("true")) { return true;	}
      return false;
   }
}
