package pe.com.unifiedgo.requirement.ad_callouts;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.plm.Product;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_Order_ServiceInProduct extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    /*
     * Enumeration<String> s = vars.getParameterNames(); while (s.hasMoreElements()) {
     * System.out.println(s.nextElement()); }
     */

    if (vars.commandIn("DEFAULT")) {

      String strMProductID = vars.getStringParameter("inpemSreServiceinproductId");
      String strADOrgID = vars.getStringParameter("inpadOrgId");
      String strMWarehouseID = vars.getStringParameter("inpmWarehouseId");
      String strCOrderId = vars.getStringParameter("inpcOrderId");
      String strWindowId = vars.getStringParameter("inpwindowId");
      String strIsSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);

      try {
        printPage(response, vars, strMProductID, strADOrgID, strMWarehouseID, strCOrderId, strIsSOTrx);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strMProductID, String strADOrgID, String strMWarehouseID, String strCOrderId, String strIsSOTrx) throws IOException, ServletException {
    log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    String strUOM = "";
    Product product = OBDal.getInstance().get(Product.class, strMProductID);
    if (product != null) {
      strUOM = product.getUOM().getId();
    }
    StringBuffer resultado = new StringBuffer();

    resultado.append("var calloutName='SL_Order_ServiceInProduct';\n\n");
    resultado.append("var respuesta = new Array(");
    resultado.append("new Array(\"inpemSreServiceinuomId\", \"" + strUOM + "\"),");
    resultado.append("new Array(\"EXECUTE\", \"displayLogic();\"),\n");

    // Para posicionar el cursor en el campo de cantidad
    resultado.append("new Array(\"CURSOR_FIELD\", \"inpqtyordered\")\n");
    resultado.append(");");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

}
