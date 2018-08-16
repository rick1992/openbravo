/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package it.extrasys.utility.report.common;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.openbravo.base.ConfigParameters;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ReportEmailManager extends ReportManager {

  private Map<String, Object> param;

  public ReportEmailManager(ConnectionProvider connectionProvider, String ftpDirectory,
      String replaceWithFull, String baseDesignPath, String defaultDesignPath, String prefix,
      boolean multiReport, ConfigParameters config) {
    super(connectionProvider, ftpDirectory, replaceWithFull, baseDesignPath, defaultDesignPath,
        prefix, multiReport);
    param = readNumberFormat(config.getFormatPath());
  }

  @Override
  protected HashMap<String, Object> populateDesignParameters(VariablesSecureApp variables,
      Report report) {
    final String baseDesignPath = this.prefix + "/" + this.strBaseDesignPath + "/"
        + this.strDefaultDesignPath;
    final HashMap<String, Object> designParameters = new HashMap<String, Object>();

    designParameters.put("DOCUMENT_ID", report.getDocumentId());

    designParameters.put("BASE_ATTACH", this.strAttachmentPath);
    designParameters.put("BASE_WEB", this.strBaseWeb);
    designParameters.put("BASE_DESIGN", baseDesignPath);
    designParameters.put("IS_IGNORE_PAGINATION", false);
    designParameters.put("USER_CLIENT",
        Utility.getContext(this.connectionProvider, variables, "#User_Client", ""));
    designParameters.put("USER_ORG",
        Utility.getContext(this.connectionProvider, variables, "#User_Org", ""));

    final String language = (variables.getLanguage() == null || variables.getLanguage().equals("")) ? "en_US"
        : variables.getLanguage();
    designParameters.put("LANGUAGE", language);

    final Locale locale = new Locale(language.substring(0, 2), language.substring(3, 5));
    designParameters.put("LOCALE", locale);

    final DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    dfs.setDecimalSeparator(param.get("#AD_ReportDecimalSeparator").toString().charAt(0));
    dfs.setGroupingSeparator(param.get("#AD_ReportGroupingSeparator").toString().charAt(0));
    final DecimalFormat NumberFormat = new DecimalFormat(param.get("#AD_ReportNumberFormat")
        .toString(), dfs);
    designParameters.put("NUMBERFORMAT", NumberFormat);

    return designParameters;
  }

  protected Map<String, Object> readNumberFormat(String strFormatFile) {
    Map<String, Object> param = new HashMap<String, Object>();
    String strNumberFormat = "###,##0.00"; // Default number format
    String strGroupingSeparator = ","; // Default grouping separator
    String strDecimalSeparator = "."; // Default decimal separator
    final String formatNameforJrxml = "euroInform"; // Name of the format to use
    final HashMap<String, String> formatMap = new HashMap<String, String>();

    try {
      // Reading number format configuration
      final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      final Document doc = docBuilder.parse(new File(strFormatFile));
      doc.getDocumentElement().normalize();
      final NodeList listOfNumbers = doc.getElementsByTagName("Number");
      final int totalNumbers = listOfNumbers.getLength();
      for (int s = 0; s < totalNumbers; s++) {
        final Node NumberNode = listOfNumbers.item(s);
        if (NumberNode.getNodeType() == Node.ELEMENT_NODE) {
          final Element NumberElement = (Element) NumberNode;
          final String strNumberName = NumberElement.getAttributes().getNamedItem("name")
              .getNodeValue();
          // store in session all the formats
          final String strFormatOutput = NumberElement.getAttributes().getNamedItem("formatOutput")
              .getNodeValue();
          formatMap.put(strNumberName, strFormatOutput);

          param.put("#FormatOutput|" + strNumberName, strFormatOutput);
          param.put("#DecimalSeparator|" + strNumberName, NumberElement.getAttributes()
              .getNamedItem("decimal").getNodeValue());
          param.put("#GroupSeparator|" + strNumberName,
              NumberElement.getAttributes().getNamedItem("grouping").getNodeValue());
          // set the numberFormat to be used in the renderJR function
          if (strNumberName.equals(formatNameforJrxml)) {
            strDecimalSeparator = NumberElement.getAttributes().getNamedItem("decimal")
                .getNodeValue();
            strGroupingSeparator = NumberElement.getAttributes().getNamedItem("grouping")
                .getNodeValue();
            strNumberFormat = strFormatOutput;
          }
        }
      }
    } catch (final Exception e) {
      log4j.error("error reading number format", e);
    }

    param.put("#FormatMap", formatMap);
    param.put("#AD_ReportNumberFormat", strNumberFormat);
    param.put("#AD_ReportGroupingSeparator", strGroupingSeparator);
    param.put("#AD_ReportDecimalSeparator", strDecimalSeparator);
    return param;
  }
}
