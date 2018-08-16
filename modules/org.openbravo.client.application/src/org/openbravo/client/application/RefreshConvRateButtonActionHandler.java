package org.openbravo.client.application;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.User;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.db.DbUtility;

import pe.com.unifiedgo.accounting.SCO_Utils;

public class RefreshConvRateButtonActionHandler extends BaseActionHandler {
  private static final Logger log4j = Logger.getLogger(RefreshConvRateButtonActionHandler.class);

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    JSONObject response = new JSONObject();
    try {
      final JSONObject request = new JSONObject(content);
      VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();
      ConnectionProvider connProvider = new DalConnectionProvider();
      User user = OBDal.getInstance().get(User.class, vars.getUser());

      // do not show any message if the client is '0'(System Administrator)
      if (OBContext.getOBContext().getCurrentClient().getId().compareTo("0") == 0) {
        response.put("showconvrate", "N");
        response.put("message", "");
      } else {

        String convmsg = "";
        Date now = new Date();
        String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties()
            .getProperty("dateFormat.java");
        SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);
        String strDate = sdf.format(now);

        ConnectionProvider conn = new DalConnectionProvider();
        String strExchangeRateUSDPurch = null;
        String strExchangeRateUSDSales = null;
        String strExchangeRateEURSales = null;
        String strParallelExchangeRateUSDSales = null;

        try {
          strExchangeRateUSDPurch = SCO_Utils.getExchangeRateUSDPurch(conn,
              OBContext.getOBContext().getCurrentClient().getId(), "0", now);
          strExchangeRateUSDSales = SCO_Utils.getExchangeRateUSDSales(conn,
              OBContext.getOBContext().getCurrentClient().getId(), "0", now);
          strExchangeRateEURSales = SCO_Utils.getExchangeRateEURSales(conn,
              OBContext.getOBContext().getCurrentClient().getId(), "0", now);
          strParallelExchangeRateUSDSales = SCO_Utils.getParallelExchangeRateUSDSales(conn,
              OBContext.getOBContext().getCurrentClient().getId(), "0", now);

        } catch (ServletException ex) {
        }

        if (strExchangeRateUSDPurch == null && strExchangeRateUSDSales == null
            && strExchangeRateEURSales == null) {
          convmsg = "<p class=\"OBNavBarTextConvRate\" style=\"color:#ff0000\"><b>" + strDate
              + " &nbsp;-&nbsp; NO SE HA INGRESADO TIPO DE CAMBIO</b></p>";
        } else {
          if (strExchangeRateUSDPurch == null) {
            strExchangeRateUSDPurch = "--";
          }
          if (strExchangeRateUSDSales == null) {
            strExchangeRateUSDSales = "--";
          }
          if (strExchangeRateEURSales == null) {
            strExchangeRateEURSales = "--";
          }
          if (strParallelExchangeRateUSDSales == null) {
            strParallelExchangeRateUSDSales = "--";
          }
          convmsg = "<p class=\"OBNavBarTextConvRate\" style=\"color:#000\"><b>" + strDate
              + "</b> &nbsp;-&nbsp; <b>T/C Ve. $:</b>" + strExchangeRateUSDSales
              + "&nbsp;&nbsp;<b>T/C Co. $:</b>" + strExchangeRateUSDPurch
              + "&nbsp;&nbsp;<b>T/C Ve. €:</b>" + strExchangeRateEURSales
              + "&nbsp;&nbsp;<b>T/C Ve. P. $:</b>" + strParallelExchangeRateUSDSales + "</p>";
        }

        JSONObject message = new JSONObject();
        message.put("severity", "success");
        message.put("text", convmsg);

        response.put("showconvrate", "Y");
        response.put("message", message);
      }

      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();

    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();

      Throwable ex = DbUtility.getUnderlyingSQLException(e);
      String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();
      try {
        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        response.put("text", errorMessage);
      } catch (JSONException ignore) {
      }
    }
    return response;
  }
}