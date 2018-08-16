package pe.com.unifiedgo.obwarehouse.pickinglist.erpReports;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.erpCommon.utility.reporting.DocumentType;
import org.openbravo.erpCommon.utility.reporting.printing.PrintController;
import org.openbravo.warehouse.pickinglist.OBWPL_Utils;
import org.openbravo.warehouse.pickinglist.PickingList;

@SuppressWarnings("serial")
public class PrintWarehousePickingLists extends PrintController {
  // private static Logger log4j = Logger.getLogger(PrintOrders.class);

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

    DocumentType documentType = DocumentType.WAREHOUSEPICKINGLISTS;
    // The prefix PRINTORDERS is a fixed name based on the KEY of the
    // AD_PROCESS
    String sessionValuePrefix = "PRINTWAREHOUSEPICKINGLIST";
    String strDocumentId = null;
    
    //System.out.println("SEGUNDO");

    // HttpSession mysessions = request.getSession();
    // String[] mysess_array = mysessions.getValueNames();
    // for (Integer i = 0; i < mysess_array.length; i++) {
    // System.out.println("session var-> Name: " + mysess_array[i] + " - Value: "
    // + mysessions.getValue(mysess_array[i]));
    // }

    strDocumentId = vars.getSessionValue(sessionValuePrefix + ".inpobwplPickingListId_R");
    if (strDocumentId.equals("")) {
      strDocumentId = vars.getSessionValue(sessionValuePrefix + ".inpobwplPickingListId");
    }
    
    //Vafaster Par a validar antes de imprimir
    String documentIds[] = null;
    strDocumentId = strDocumentId.replaceAll("\\(|\\)|'", "");
    if (strDocumentId.length() == 0)
      throw new ServletException(Utility.messageBD(this, "NoDocument", vars.getLanguage()));
    documentIds = strDocumentId.split(",");
    
    
   // String strOrg = "";
    /*for(int i = 0; i < documentIds.length; i++){
    	String documentId = documentIds[i];
    	PickingList pl = OBDal.getInstance().get(PickingList.class, documentId);
    	if(!strOrg.equals(pl.getOrganization().getId()) && !strOrg.equals(""))
    		throw new ServletException(Utility.messageBD(this, "swa_InvalidPrintPickingOrg", vars.getLanguage()));
    	strOrg=pl.getOrganization().getId();
    }*/
    ///
   // vars.setSessionValue("strTmpAdOrgId", strOrg);
   // System.out.println("SET strDocumentID: " + strOrg);
   // System.out.println("GET strDocumentID: " +  vars.getSessionValue("strTmpAdOrgId"));
    
    
    post(request, response, vars, documentType, sessionValuePrefix, strDocumentId, "_18",
        "pe/com/unifiedgo/obwarehouse/pickinglist/erpReports/PrintOptions_18");
  }

  public String getServletInfo() {
    return "Servlet that processes the print action";
  } // End of getServletInfo() method
}