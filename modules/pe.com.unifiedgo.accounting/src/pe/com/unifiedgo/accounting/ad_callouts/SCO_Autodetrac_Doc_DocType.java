package pe.com.unifiedgo.accounting.ad_callouts;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class SCO_Autodetrac_Doc_DocType extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strChanged = vars.getStringParameter("inpLastFieldChanged");
      if (log4j.isDebugEnabled())
        log4j.debug("CHANGED: " + strChanged);
      String strDocTypeTarget = vars.getStringParameter("inpcDoctypetargetId");
      String strDocType = vars.getStringParameter("inpcDoctypeId");
      String docNo = vars.getStringParameter("inpdocumentno");
      String strSCOAutodetracDocId = vars.getStringParameter("inpscoAutodetracDocId");
      String strDescription = vars.getStringParameter("inpdescription");
      String strTabId = vars.getStringParameter("inpTabId");

      try {
        printPage(response, vars, strDocTypeTarget, strDocType, docNo, strSCOAutodetracDocId, strDescription, strTabId);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strDocTypeTarget, String strDocType, String docNo, String strSCOAutodetracDocId, String strDescription, String strTabId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    StringBuffer resultado = new StringBuffer();
    if (strDocTypeTarget.equals(""))
      resultado.append("var respuesta = null;");
    else {
      resultado.append("var calloutName='SCO_Autodetrac_Doc_DocType';\n\n");
      resultado.append("var respuesta = new Array(");
      String PaymentRule = "P";
      String InvoiceRule = "D";
      String DeliveryRule = "A";
      boolean newDocNo = docNo.equals("");
      if (!newDocNo && docNo.startsWith("<") && docNo.endsWith(">"))
        newDocNo = true;
      String AD_Sequence_ID = "0";
      SCOAutodetracDocDocTypeData[] data = null;

      if (!newDocNo && !"0".equals(strDocType)) {
        data = SCOAutodetracDocDocTypeData.select(this, strDocType);
        if (data != null && data.length > 0) {
          AD_Sequence_ID = data[0].adSequenceId;
        }
      }
      String DocSubTypeSO = "";
      boolean IsSOTrx = true;
      SCOAutodetracDocDocTypeData[] dataNew = SCOAutodetracDocDocTypeData.select(this, strDocTypeTarget);
      if (dataNew != null && dataNew.length > 0) {
        DocSubTypeSO = dataNew[0].docsubtypeso;
        if (DocSubTypeSO == null)
          DocSubTypeSO = "--";
        String strOldDocTypeTarget = SCOAutodetracDocDocTypeData.selectOldDocSubType(this, strSCOAutodetracDocId);
        if (!DocSubTypeSO.equals("OB") && strOldDocTypeTarget.equals("OB")) {
          String strOldDocNo = SCOAutodetracDocDocTypeData.selectOldDocNo(this, strSCOAutodetracDocId);
          resultado.append("new Array(\"inpdescription\", \"" + FormatUtilities.replaceJS(Utility.messageBD(this, "Quotation", vars.getLanguage()) + " " + strOldDocNo + ". " + strDescription) + "\"),\n");
        }
        resultado.append("new Array(\"inpordertype\", \"" + DocSubTypeSO + "\")\n");
        PaymentRule = "P";
        InvoiceRule = (DocSubTypeSO.equals("PR") || DocSubTypeSO.equals("WI") ? "I" : "D");
        DeliveryRule = "A";
        if (dataNew[0].isdocnocontrolled.equals("Y")) {
          if (!newDocNo && !AD_Sequence_ID.equals(dataNew[0].adSequenceId) && !SCOAutodetracDocDocTypeData.selectOldDocTypeTargetId(this, strSCOAutodetracDocId).equalsIgnoreCase(strDocTypeTarget))
            newDocNo = true;
          if (newDocNo) {
            if (vars.getRole().equalsIgnoreCase("System") && new BigDecimal(vars.getClient()).compareTo(new BigDecimal("1000000.0")) < 0)
              resultado.append(", new Array(\"inpdocumentno\", \"<" + dataNew[0].currentnextsys + ">\")\n");
            else
              resultado.append(", new Array(\"inpdocumentno\", \"<" + dataNew[0].currentnext + ">\")\n");
          }
        }
        if (dataNew[0].issotrx.equals("N"))
          IsSOTrx = false;
      }

      resultado.append(", new Array(\"EXECUTE\", \"displayLogic();\")\n");
      resultado.append(");\n");
    }
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}