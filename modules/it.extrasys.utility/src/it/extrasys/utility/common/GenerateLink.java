package it.extrasys.utility.common;

import org.apache.log4j.Logger;

public class GenerateLink {
	 private static final Logger log = Logger.getLogger(GenerateLink.class);

	  private static GenerateLink singleton;

	  private GenerateLink() {
	  }
	  
	  public static GenerateLink getSingleton() {
		    if (singleton == null) {
		      singleton = new GenerateLink();
		    }
		    return singleton;
		  }
	  
	  public String generateLink(String windowId, String tabId, String tabTitle, String recordId,
			  String command, String icon, boolean readOnly, boolean singleRecord, boolean direct,
			  boolean editOrDeleteOnly, String linktext) {
		  String strLink = "";
		  strLink="<a href=\"#\" onclick=\"OB.Utilities.openView('"+windowId+"','"+tabId+"','"+tabTitle+"','"+recordId+"',";
		  strLink=strLink+(command.equals("")?"'DIRECT'":"'"+command+"'")+",'',"+(readOnly?"true":"false")+",";
		  strLink=strLink+(singleRecord?"true":"false")+","+(direct?"true":"false")+","+(editOrDeleteOnly?"true":"false")+");\">"+linktext+"</a>";
		  
		  return strLink;
		  }  

}