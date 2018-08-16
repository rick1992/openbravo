package it.extrasys.utility.callout;

import org.apache.log4j.Logger;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.utils.Replace;

public class CustomFormatUtilities extends FormatUtilities{
  static Logger log4j = Logger.getLogger(CustomFormatUtilities.class);

  public static String formatStringCallout(String strIni) {
	    String strOutp = replaceJS(strIni);
	    strOutp = Replace.replace(strOutp, "\"", "\\\"");
	    return strOutp;
	  }
  
}
