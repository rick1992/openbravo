package pe.com.unifiedgo.core.printing.invoices;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.reporting.DocumentType;
import org.openbravo.erpCommon.utility.reporting.printing.PrintController;

import pe.com.unifiedgo.core.ad_actionbutton.GenerateSORelatedDocuments;

@SuppressWarnings("serial")
public class PrintDirectInvoices extends PrintController {
  private static Logger log4j = Logger.getLogger(PrintDirectInvoices.class);

  // TODO: Als een email in draft staat de velden voor de email adressen
  // weghalen en melden dat het document
  // niet ge-emailed kan worden

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  @SuppressWarnings("unchecked")
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    // HttpSession mysessions = request.getSession();
    // String[] mysess_array = mysessions.getValueNames();
    // for (Integer i = 0; i < mysess_array.length; i++) {
    // System.out.println("session var-> Name: " + mysess_array[i] + " - Value: "
    // + mysessions.getValue(mysess_array[i]));
    // }

    System.out.println("HOLAAAAAAAAAAAAAAaa");
    DocumentType documentType = DocumentType.SALESINVOICE;
    // The prefix NEWPRINTINVOICES is a fixed name based on the KEY of the
    // AD_PROCESS
    String sessionValuePrefix = "NEWPRINTDIRECTINVOICES";
    String strDocumentId = null;

    strDocumentId = vars.getSessionValue(sessionValuePrefix + ".inpcInvoiceId_R");
    if (strDocumentId.equals(""))
      strDocumentId = vars.getSessionValue(sessionValuePrefix + ".inpcInvoiceId");

    post(request, response, vars, documentType, sessionValuePrefix, strDocumentId, "direct",
        "org/openbravo/erpCommon/utility/reporting/printing/PrintOptionsDirect");
  }

  public String getServletInfo() {
    return "Servlet that processes the print action";
  } // End of getServletInfo() method
}