package pe.com.unifiedgo.accounting.ad_actionbutton;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.data.ScoRendicioncuentas;

public class CloseRendicionCuenta extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {

      String strOrgId = vars.getRequestGlobalVariable("inpadOrgId", "");
      String strWindowId = vars.getGlobalVariable("inpwindowId", "");
      String strTabId = vars.getGlobalVariable("inpTabId", "");
      String strDocumentId = vars.getGlobalVariable("inpscoRendicioncuentasId", "");
      String strDocumentNo = vars.getRequestGlobalVariable("inspdocumentno", "");
      String strAmount = vars.getRequestGlobalVariable("inpamount", "");

      printPage(response, vars, strDocumentId, strOrgId, strWindowId, strTabId, strAmount, strDocumentNo);

    } else if (vars.commandIn("SAVE") || vars.commandIn("SAVEANDPROCESS")) {
      OBError message = null;

      String recordId = vars.getRequiredStringParameter("inpcDocumentId");
      String strTabId = vars.getRequiredStringParameter("inpTabId");

      try {
        ScoRendicioncuentas rendicion = OBDal.getInstance().get(ScoRendicioncuentas.class, recordId);
        rendicion.setDocumentStatus("CL");
        OBDal.getInstance().save(rendicion);

        message = new OBError();
        message.setType("Success");
        message.setTitle(OBMessageUtils.getI18NMessage("OBUIAPP_Success", null));
        message.setMessage(OBMessageUtils.getI18NMessage("SCO_CloseAccountability", null));
      } catch (Exception ex) {
        message = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      }

      vars.setMessage(strTabId, message);
      printPageClosePopUpAndRefreshParent(response, vars);

    }
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strDocumentId, String strOrgId, String strWindowId, String strTabId, String strAmount, String strDocumentNo) throws IOException, ServletException {

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("pe/com/unifiedgo/accounting/ad_actionbutton/CloseRendicionCuenta").createXmlDocument();

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());

    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabId", strTabId);
    xmlDocument.setParameter("orgId", strOrgId);
    xmlDocument.setParameter("documentId", strDocumentId);
    xmlDocument.setParameter("documentno", strDocumentNo);
    xmlDocument.setParameter("amount", strAmount);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet that presents closing of cuentas a rendir";
    // end of getServletInfo() method
  }

}
