package pe.com.unifiedgo.core.printing.shipments;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.reporting.DocumentType;
import org.openbravo.erpCommon.utility.reporting.printing.PrintController;

@SuppressWarnings("serial")
public class PrintDirectShipments extends PrintController {
  private static Logger log4j = Logger.getLogger(PrintDirectShipments.class);

  // TODO: Als een email in draft staat de velden voor de email adressen
  // weghalen en melden dat het document
  // niet ge-emailed kan worden

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  @SuppressWarnings("unchecked")
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    DocumentType documentType = DocumentType.SHIPMENT;
    // The prefix NEWPRINTSHIPMENTS is a fixed name based on the KEY of the
    // AD_PROCESS
    String sessionValuePrefix = "NEWPRINTDIRECTSHIPMENTS";
    String strDocumentId = null;

    strDocumentId = vars.getSessionValue(sessionValuePrefix + ".inpmInoutId_R");
    if (strDocumentId.equals(""))
      strDocumentId = vars.getSessionValue(sessionValuePrefix + ".inpmInoutId");

    post(request, response, vars, documentType, sessionValuePrefix, strDocumentId, "direct", "/pe/com/unifiedgo/core/printing/shipments/PrintOptionsDirect");
  }

  public String getServletInfo() {
    return "Servlet that processes the print action";
  } // End of getServletInfo() method
}