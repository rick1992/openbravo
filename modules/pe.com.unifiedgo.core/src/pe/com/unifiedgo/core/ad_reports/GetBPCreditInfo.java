package pe.com.unifiedgo.core.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.xmlEngine.XmlDocument;

public class GetBPCreditInfo extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String stradOrgID = "0";// vars.getGlobalVariable("inpadOrgId", "GetBPCreditInfo|AD_ORG_ID",
                              // "");
      String strcBPartnerID = vars.getGlobalVariable("inpcBpartnerId",
          "GetBPCreditInfo|C_BPARTNER_ID", "");

      printPageDataSheet(response, vars);

    } else if (vars.commandIn("OK")) {
      String stradOrgID = "0"; // vars.getGlobalVariable("inpadOrgId", "GetBPCreditInfo|AD_ORG_ID",
                               // "");
      String strcBPartnerID = vars.getGlobalVariable("inpcBpartnerId",
          "GetBPCreditInfo|C_BPARTNER_ID", "");

      response.setCharacterEncoding("UTF-8");
      response.setContentType("application/json");
      PrintWriter out = response.getWriter();
      out.print(getJSON(stradOrgID, strcBPartnerID));
      out.close();
    }
  }

  private String getJSON(String stradOrgID, String strcBPartnerID) throws ServletException {
    JSONObject json = null;

    try {
      OBContext.setAdminMode();

      json = new JSONObject();
      json.put("adOrgId", stradOrgID);
      json.put("cBPartnerId", strcBPartnerID);

    } catch (Exception e) {
      try {
        json.put("error", e.getMessage());
      } catch (JSONException jex) {
        log4j.error("Error trying to generate message: " + jex.getMessage(), jex);
      }
    } finally {
      OBContext.restorePreviousMode();
    }

    return json.toString();
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;

    String discard[] = { "secTable" };
    xmlDocument = xmlEngine.readXmlTemplate("pe/com/unifiedgo/core/ad_reports/GetBPCreditInfo",
        discard).createXmlDocument();

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.core.ad_reports.GetBPCreditInfo");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "GetBPCreditInfo.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "GetBPCreditInfo.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("GetBPCreditInfo");
      vars.removeMessage("GetBPCreditInfo");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");

    out.println(xmlDocument.print());
    out.close();
  }

}
