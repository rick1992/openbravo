package it.extrasys.utility.callout;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;

import org.openbravo.data.UtilSql;
import org.openbravo.database.ConnectionProvider;

public class Utility {

  public static String replaceAllToResponse(String res, String key, String value, boolean skip) {
    String newRes = res;
    int initial = res.indexOf(key);
    if (initial > 0) {
      newRes = res.substring(0, initial + key.length() + 2);
      String resSubString = res.substring(initial, res.length());
      int last = resSubString.indexOf(")");
      newRes = newRes
          + CustomFormatUtilities.formatStringCallout(value)
          + replaceAllToResponse(resSubString.substring(last, resSubString.length()), key, value,
              true);
    } else if (!skip) {
      newRes = newRes.replaceAll("\\);</SCRIPT>", ",new Array(\"" + key + "\", \""
          + CustomFormatUtilities.formatStringCallout(value) + "\"));</SCRIPT>");
    }
    return newRes;
  }

  public static String getValueFromResponse(String res, String key) {
    int initial = res.indexOf(key);
    String response = null;
    if (initial > 0) {
      String resSubString = res.substring(initial, res.length());
      response = resSubString.substring(resSubString.indexOf(",") + 1, resSubString.indexOf(")"))
          .trim();
    }
    return response;
  }

  /**
   * Get Precision
   * 
   * @param connectionProvider
   *          , table, idEntry
   * @return
   * @throws ServletException
   */
  public static String[] findPrecision(ConnectionProvider connectionProvider, String table,
      String idEntry) throws ServletException {
    String strSql = "";
    strSql = "SELECT C_Currency.StdPrecision, C_Currency.PricePrecision, M_PriceList.EnforcePriceLimit "
        + "   FROM "
        + table
        + ", M_PriceList, C_Currency "
        + "   WHERE "
        + table
        + ".M_PriceList_ID = M_PriceList.M_PriceList_ID"
        + "   AND M_PriceList.C_Currency_ID = C_Currency.C_Currency_ID"
        + "   AND "
        + table
        + ".C_Order_ID = ?";

    ResultSet result;
    PreparedStatement st = null;
    String[] array = new String[3];
    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, idEntry);

      result = st.executeQuery();

      if (result.next()) {

        array[0] = UtilSql.getValue(result, "stdprecision");
        array[1] = UtilSql.getValue(result, "priceprecision");
        array[2] = UtilSql.getValue(result, "enforcepricelimit");

      }
      result.close();
    } catch (SQLException e) {
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }

    return array;
  }
}
