package pe.com.unifiedgo.webservice;

import java.io.Writer;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.Language;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.db.DbUtility;
import org.openbravo.service.web.WebService;
import org.openbravo.warehouse.pickinglist.PickingList;
import org.openbravo.warehouse.pickinglist.PickingListActionHandler;

import pe.com.unifiedgo.accounting.SCO_Utils;
import pe.com.unifiedgo.core.Result;
import pe.com.unifiedgo.core.ad_actionbutton.ProcessOrder;

public class MobileServices implements WebService {

  public static String convertToXml(String code, String message) {

    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><mobileservices><errorcode>" + code
        + "</errorcode><message>" + message + "</message></mobileservices>";
  }

  public static String convertPricesToXml(String code, String value1, String value2) {

    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><mobileservices><errorcode>" + code
        + "</errorcode><pricelist>" + value1 + "</pricelist><unitprice>" + value2
        + "</unitprice></mobileservices>";
  }

  public static String convertNumberToXml(String code, BigDecimal value) {

    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><mobileservices><errorcode>" + code
        + "</errorcode><message>" + value + "</message></mobileservices>";
  }

  public void doGet(String path, HttpServletRequest request, HttpServletResponse response)
      throws Exception {

    response.setHeader("Access-Control-Allow-Origin", "*");
    response.setHeader("Access-Control-Allow-Credentials", "true");
    response.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS, HEAD");
    response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With");

    final String method = request.getParameter("method");

    String result = convertToXml("0", "Method not found");

    if (method == null) {
      result = convertToXml("0", "Method is required");
    } else {

      final String username = request.getParameter("l");

      OBCriteria<User> lsUsers = OBDal.getInstance().createCriteria(User.class);
      lsUsers.add(Restrictions.eq(User.PROPERTY_USERNAME, username));

      String adUserId = "";

      if (lsUsers.list().size() != 1) {
        result = convertToXml("0", "User in more than 1 client");
      } else {
        adUserId = lsUsers.list().get(0).getId();
      }

      if (!adUserId.equals("")) {
        ConnectionProvider conn = new DalConnectionProvider();
        Language lang = (OBContext.getOBContext().getUser().getDefaultLanguage() != null) ? OBContext
            .getOBContext().getUser().getDefaultLanguage()
            : OBDal.getInstance().get(Language.class, "146"); // es_PE

        OBContext.getOBContext().setLanguage(lang);
        VariablesSecureApp vars = new VariablesSecureApp(
            OBContext.getOBContext().getUser().getId(), OBContext.getOBContext().getCurrentClient()
                .getId(), OBContext.getOBContext().getCurrentOrganization().getId(), OBContext
                .getOBContext().getRole().getId(), OBContext.getOBContext().getLanguage()
                .getLanguage());

        if (method.equals("completePickingList")) {
          final String idPicking = request.getParameter("idPicking");
          result = completePickingList(idPicking, adUserId);

        } else if (method.equals("completePickingLine")) {
          final String idPicking = request.getParameter("idPicking");
          final String idLine = request.getParameter("idLine");
          final String check = request.getParameter("check");
          result = completePickingLine(idPicking, idLine, check, adUserId);

        } else if (method.equals("completeSalesOrder")) {
          final String orderId = request.getParameter("orderId");

          result = completeSalesOrder(conn, vars, orderId, adUserId);

        } else if (method.equals("reactivateSalesOrder")) {
          final String orderId = request.getParameter("orderId");

          result = reactivateSalesOrder(conn, vars, orderId, adUserId);

        } else if (method.equals("getProductPrices")) {
          final String adOrgId = request.getParameter("adOrgId");
          final String productId = request.getParameter("productId");
          final String priceListId = request.getParameter("priceListId");
          final Date date = new Date();

          result = getProductPrices(conn, vars, adOrgId, productId, priceListId, date, adUserId);

        } else if (method.equals("getProductStockbyWarehouse")) {
          System.out.println("ACAA getProductStockbyWarehouse");
          final String adOrgId = request.getParameter("adOrgId");
          final String warehouseId = request.getParameter("warehouseId");
          final String productId = request.getParameter("productId");

          result = getProductStockbyWarehouse(conn, vars, adOrgId, warehouseId, productId, adUserId);

        } else if (method.equals("getProductStockTotal")) {
          final String adOrgId = request.getParameter("adOrgId");
          final String productId = request.getParameter("productId");

          result = getProductStockTotal(conn, vars, adOrgId, productId, adUserId);
        }

      }
    }
    final Writer w = response.getWriter();
    w.write(result);
    w.close();
  }

  public void doDelete(String path, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
  }

  public void doPost(String path, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
  }

  public void doPut(String path, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
  }

  public static String completePickingLine(String idPicking, String idLine, String check,
      String adUserId) {

    String result = "";
    String mensaje = "";

    ShipmentInOutLine inoutline = OBDal.getInstance().get(ShipmentInOutLine.class, idLine);
    if (inoutline != null) {
      if (check.equals("Y"))
        inoutline.setSWAEstadoDePicking("CO");
      else
        inoutline.setSWAEstadoDePicking("PD");

      OBDal.getInstance().save(inoutline);
      OBDal.getInstance().flush();
      SessionHandler.getInstance().commitAndStart();

      result = "1";
      mensaje = "Linea de picking actualizada";

    } else {
      result = "0";
      mensaje = "Linea de picking no existe";
    }

    return convertToXml(result, mensaje);
  }

  public static String completePickingList(String idPicking, String adUserId) {

    String json = "{ \"pickings\": [" + idPicking + "]}";
    String result = "";
    String mensaje = "";

    PickingList pick = OBDal.getInstance().get(PickingList.class, idPicking);
    if (pick == null || !pick.getPickliststatus().equals("DR")) {
      result = "0";
      mensaje = "Otro usuario ha modificado este Picking. Refresque la aplicacion.";
    } else {

      try {
        JSONObject jsnobject = new JSONObject(json);
        JSONArray jsonArray = jsnobject.getJSONArray("pickings");
        PickingListActionHandler picking = new PickingListActionHandler();
        JSONObject jsonResponse = picking.doProcess(jsonArray);

        if (jsonResponse.getJSONObject("message").get("severity").equals("success")) {
          result = "1";
        } else {
          result = "0";
        }

        mensaje = jsonResponse.getJSONObject("message").get("text").toString();

      } catch (Exception ex) {

        mensaje = "No se pudo procesar el picking";
        result = "0";
      }
    }
    return convertToXml(result, mensaje);
  }

  public static String completeSalesOrder(ConnectionProvider conn, VariablesSecureApp vars,
      String orderId, String adUserId) {

    String result = "";
    String mensaje = "";

    User user = OBDal.getInstance().get(User.class, adUserId);

    Order order;
    String strdocaction = "CO", message = "";
    OBError myMessage = null;
    boolean doRollback = true;
    try {
      order = OBDal.getInstance().get(Order.class, orderId);
      order.setDocumentAction(strdocaction);
      OBDal.getInstance().save(order);
      OBDal.getInstance().flush();

      // if (!PEN_CURRID.equals(order.getCurrency().getId())) {
      if (SCO_Utils.getExchangeRateUSDSales(conn, order.getClient().getId(), order
          .getOrganization().getId(), order.getOrderDate()) == null) {
        throw new Exception("@SCR_NoConversionRateFor@" + order.getOrderDate());
      }
      // }

      // Checking if Business Partner is blocked (only for documents with credit)
      if (!"SCOINMEDIATETERM".equals(order.getPaymentTerms().getScoSpecialpayterm())) {
        if ("CO".equals(strdocaction) && order.getBusinessPartner().isCustomerBlocking()
            && order.getBusinessPartner().isSalesOrder() && !order.getDocumentType().isReturn()) {
          String detail_msg = "";
          if (order.getBusinessPartner().getScrMsgBlocking() != null
              && !"".equals(order.getBusinessPartner().getScrMsgBlocking().trim()))
            detail_msg = order.getBusinessPartner().getScrMsgBlocking();
          else
            detail_msg = OBMessageUtils.messageBD("SSA_BusinessPartnerBlocked");

          throw new Exception("@ThebusinessPartner@" + " " + order.getBusinessPartner().getName()
              + " " + detail_msg);
        }
      }

      // Checking if Sales Order has Lines
      if ("CO".equals(strdocaction)) {
        if (order.getOrderLineList().size() <= 0) {
          throw new Exception("@OrderWithoutLines@");
        }
      }

      // Check for the SUNAT Restriction 15 lines(m_product_id,priceactual) max
      String strSunatlinescount = ProcessOrder.getOrderSunatLinesCount(conn, order.getId());
      int sunatlinescount = new Long(strSunatlinescount).intValue();
      if (sunatlinescount > 15) {
        throw new Exception("@SCO_SUNATDocMax15@");
      }

      // Automatic Post 
      myMessage = ProcessOrder.postOrder(conn, vars, order, strdocaction);
      if (myMessage != null && "Error".equals(myMessage.getType())) {
        throw new OBException(myMessage.getMessage());
      }

      message = myMessage.getMessage();
      if (message.contains("@")) {
        message = OBMessageUtils.parseTranslation(message);
      }
      message = Utility.messageBD(conn, message, vars.getLanguage());

      result = "1";
      mensaje = message;

    } catch (Exception ex) {

      message = DbUtility.getUnderlyingSQLException(ex).getMessage();
      if (message.contains("@")) {
        message = OBMessageUtils.parseTranslation(message);
      }
      message = Utility.messageBD(conn, message, vars.getLanguage());

      if (doRollback) {
        OBDal.getInstance().rollbackAndClose();
      }

      if (myMessage != null && !"Error".equals(myMessage.getType())) {
        result = "1";
      } else {
        result = "0";
      }

      mensaje = message;
    }

    return convertToXml(result, StringEscapeUtils.escapeXml(mensaje));
  }

  public static String reactivateSalesOrder(ConnectionProvider conn, VariablesSecureApp vars,
      String orderId, String adUserId) {

    String result = "";
    String mensaje = "";

    Order order;
    String strdocaction = "RE", message = "";
    OBError myMessage = null;
    try {
      order = OBDal.getInstance().get(Order.class, orderId);
      order.setDocumentAction(strdocaction);
      OBDal.getInstance().save(order);
      OBDal.getInstance().flush();

      // Automatic Post if discount and credit evaluations have been passed successfully
      myMessage = ProcessOrder.postOrder(conn, vars, order, strdocaction);
      if (myMessage != null && "Error".equals(myMessage.getType())) {
        throw new OBException(myMessage.getMessage());
      }

      message = myMessage.getMessage();
      if (message != null && "".equals(message)) {
        message = "@ProcessOK@";
      }
      if (message.contains("@")) {
        message = OBMessageUtils.parseTranslation(message);
      }
      message = Utility.messageBD(conn, message, vars.getLanguage());

      result = "1";
      mensaje = message;

    } catch (Exception ex) {
      message = DbUtility.getUnderlyingSQLException(ex).getMessage();
      if (message.contains("@")) {
        message = OBMessageUtils.parseTranslation(message);
      }
      message = Utility.messageBD(conn, message, vars.getLanguage());

      OBDal.getInstance().rollbackAndClose();

      result = "0";
      mensaje = message;
    }

    return convertToXml(result, StringEscapeUtils.escapeXml(mensaje));
  }

  public static String getProductPrices(ConnectionProvider conn, VariablesSecureApp vars,
      String adOrgId, String productId, String priceListId, Date date, String adUserId) {

    String result = "";
    String strPriceList, strPriceStd;
    try {
      String product_data[] = SCO_Utils.getProductPricesByDate(conn, adOrgId, productId,
          priceListId, date);
      strPriceList = product_data[0];
      strPriceStd = product_data[1];
      // String strPriceLimit = product_data[2];

      result = "1";

    } catch (Exception ex) {
      ex.printStackTrace();

      OBDal.getInstance().rollbackAndClose();

      result = "0";
      strPriceList = "0";
      strPriceStd = "0";
    }

    return convertPricesToXml(result, strPriceList, strPriceStd);
  }

  public static String getProductStockbyWarehouse(ConnectionProvider conn, VariablesSecureApp vars,
      String adOrgId, String mWarehouseId, String productId, String adUserId) {
    String result = "";
    BigDecimal stock;
    try {
      stock = SCO_Utils.getWarehouseStockAvailable(adOrgId, vars.getClient(), mWarehouseId,
          productId);
      result = "1";

    } catch (Exception ex) {
      ex.printStackTrace();

      OBDal.getInstance().rollbackAndClose();

      result = "0";
      stock = BigDecimal.ZERO;
    }

    return convertNumberToXml(result, stock);
  }

  public static String getProductStockTotal(ConnectionProvider conn, VariablesSecureApp vars,
      String adOrgId, String productId, String adUserId) {

    String result = "";
    BigDecimal stock;
    try {
      stock = SCO_Utils.getTotalStockAvailable(adOrgId, vars.getClient(), productId);
      result = "1";

    } catch (Exception ex) {
      ex.printStackTrace();

      OBDal.getInstance().rollbackAndClose();

      result = "0";
      stock = BigDecimal.ZERO;
    }

    return convertNumberToXml(result, stock);
  }

  // http://localhost:8080/openbravo/ws/pe.com.unifiedgo.webservice.MobileServices?l=Openbravo&p=openbravo&method=completePickingList
}
