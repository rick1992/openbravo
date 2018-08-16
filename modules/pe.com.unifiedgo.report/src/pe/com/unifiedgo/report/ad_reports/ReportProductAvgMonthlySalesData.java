//Sqlc generated V1.O00-1
package pe.com.unifiedgo.report.ad_reports;

import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.data.UtilSql;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.database.SessionInfo;
import org.openbravo.model.common.plm.Product;
import org.openbravo.service.db.QueryTimeOutUtil;

class ReportProductAvgMonthlySalesData implements FieldProvider {
  static Logger log4j = Logger.getLogger(ReportProductAvgMonthlySalesData.class);
  private String InitRecordNumber = "0";
  public String productid;
  public String searchkey;
  public String internalcode;
  public String name;
  public String avgmonthlysales1;
  public String avgmonthlysales2;
  public String avgmonthlysales3;
  public String avgmonthlysales4;
  public String avgmonthlysales5;
  public String avgmonthlysales6;
  public String avgmonthlysales7;
  public String avgmonthlysales8;
  public String avgmonthlysales9;
  public String avgmonthlysales10;
  public String avgmonthlysales11;
  public String avgmonthlysales12;
  public String rownum;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  @Override
  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("productid"))
      return productid;
    else if (fieldName.equalsIgnoreCase("searchkey"))
      return searchkey;
    else if (fieldName.equalsIgnoreCase("internalcode"))
      return internalcode;
    else if (fieldName.equalsIgnoreCase("name"))
      return name;
    else if (fieldName.equalsIgnoreCase("avgmonthlysales1")) {
      if (Double.parseDouble(avgmonthlysales1) == -1)
        return "--";
      return avgmonthlysales1.toString();
    } else if (fieldName.equalsIgnoreCase("avgmonthlysales2")) {
      if (Double.parseDouble(avgmonthlysales2) == -1)
        return "--";
      return avgmonthlysales2.toString();
    } else if (fieldName.equalsIgnoreCase("avgmonthlysales3")) {
      if (Double.parseDouble(avgmonthlysales3) == -1)
        return "--";
      return avgmonthlysales3.toString();
    } else if (fieldName.equalsIgnoreCase("avgmonthlysales4")) {
      if (Double.parseDouble(avgmonthlysales4) == -1)
        return "--";
      return avgmonthlysales4.toString();
    } else if (fieldName.equalsIgnoreCase("avgmonthlysales5")) {
      if (Double.parseDouble(avgmonthlysales5) == -1)
        return "--";
      return avgmonthlysales5.toString();
    } else if (fieldName.equalsIgnoreCase("avgmonthlysales6")) {
      if (Double.parseDouble(avgmonthlysales6) == -1)
        return "--";
      return avgmonthlysales6.toString();
    } else if (fieldName.equalsIgnoreCase("avgmonthlysales7")) {
      if (Double.parseDouble(avgmonthlysales7) == -1)
        return "--";
      return avgmonthlysales7.toString();
    } else if (fieldName.equalsIgnoreCase("avgmonthlysales8")) {
      if (Double.parseDouble(avgmonthlysales8) == -1)
        return "--";
      return avgmonthlysales8.toString();
    } else if (fieldName.equalsIgnoreCase("avgmonthlysales9")) {
      if (Double.parseDouble(avgmonthlysales9) == -1)
        return "--";
      return avgmonthlysales9.toString();
    } else if (fieldName.equalsIgnoreCase("avgmonthlysales10")) {
      if (Double.parseDouble(avgmonthlysales10) == -1)
        return "--";
      return avgmonthlysales10.toString();
    } else if (fieldName.equalsIgnoreCase("avgmonthlysales11")) {
      if (Double.parseDouble(avgmonthlysales11) == -1)
        return "--";
      return avgmonthlysales11.toString();
    } else if (fieldName.equalsIgnoreCase("avgmonthlysales12")) {
      if (Double.parseDouble(avgmonthlysales12) == -1)
        return "--";
      return avgmonthlysales12.toString();
    } else if (fieldName.equals("rownum"))
      return rownum;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static ReportProductAvgMonthlySalesData[] select(String adClientId, String strAD_Org_ID,
      String mProductId, String docDate, String numMonths) throws ServletException {
    return select(adClientId, strAD_Org_ID, mProductId, docDate, numMonths, 0, 0);
  }

  public static ReportProductAvgMonthlySalesData[] select(String adClientId, String adOrgId,
      String mProductId, String docDate, String numMonths, int firstRegister, int numberRegisters)
      throws ServletException {

    Product product = OBDal.getInstance().get(Product.class, mProductId);

    String strSql = "";
    strSql = "SELECT COALESCE(sre_product_avg_monthly_specif('" + adOrgId + "', '" + adClientId
        + "', '" + product.getId() + "',"
        + "                                        DATE(to_char((TO_DATE('" + docDate
        + "') - interval '11 month'),'YYYY-MM') || '-01'), "
        + "                                        DATE(to_char((TO_DATE('" + docDate
        + "') - interval '10 month'),'YYYY-MM') || '-01') - 1,null), 0) AS avg_monthly_sales_1, "
        + "          COALESCE(sre_product_avg_monthly_specif('" + adOrgId + "', '" + adClientId
        + "', '" + product.getId() + "',"
        + "                                        DATE(to_char((TO_DATE('" + docDate
        + "') - interval '10 month'),'YYYY-MM') || '-01'), "
        + "                                        DATE(to_char((TO_DATE('" + docDate
        + "') - interval '9 month'),'YYYY-MM') || '-01') - 1,null), 0) AS avg_monthly_sales_2, "
        + "          COALESCE(sre_product_avg_monthly_specif('" + adOrgId + "', '" + adClientId
        + "', '" + product.getId() + "',"
        + "                                        DATE(to_char((TO_DATE('" + docDate
        + "') - interval '9 month'),'YYYY-MM') || '-01'), "
        + "                                        DATE(to_char((TO_DATE('" + docDate
        + "') - interval '8 month'),'YYYY-MM') || '-01') - 1,null), 0) AS avg_monthly_sales_3, "
        + "          COALESCE(sre_product_avg_monthly_specif('" + adOrgId + "', '" + adClientId
        + "', '" + product.getId() + "',"
        + "                                        DATE(to_char((TO_DATE('" + docDate
        + "') - interval '8 month'),'YYYY-MM') || '-01'), "
        + "                                        DATE(to_char((TO_DATE('" + docDate
        + "') - interval '7 month'),'YYYY-MM') || '-01') - 1,null), 0) AS avg_monthly_sales_4, "
        + "          COALESCE(sre_product_avg_monthly_specif('" + adOrgId + "', '" + adClientId
        + "', '" + product.getId() + "',"
        + "                                        DATE(to_char((TO_DATE('" + docDate
        + "') - interval '7 month'),'YYYY-MM') || '-01'), "
        + "                                        DATE(to_char((TO_DATE('" + docDate
        + "') - interval '6 month'),'YYYY-MM') || '-01') - 1,null), 0) AS avg_monthly_sales_5, "
        + "          COALESCE(sre_product_avg_monthly_specif('" + adOrgId + "', '" + adClientId
        + "', '" + product.getId() + "',"
        + "                                        DATE(to_char((TO_DATE('" + docDate
        + "') - interval '6 month'),'YYYY-MM') || '-01'), "
        + "                                        DATE(to_char((TO_DATE('" + docDate
        + "') - interval '5 month'),'YYYY-MM') || '-01') - 1,null), 0) AS avg_monthly_sales_6, "
        + "          COALESCE(sre_product_avg_monthly_specif('" + adOrgId + "', '" + adClientId
        + "', '" + product.getId() + "',"
        + "                                        DATE(to_char((TO_DATE('" + docDate
        + "') - interval '5 month'),'YYYY-MM') || '-01'), "
        + "                                        DATE(to_char((TO_DATE('" + docDate
        + "') - interval '4 month'),'YYYY-MM') || '-01') - 1,null), 0) AS avg_monthly_sales_7, "
        + "          COALESCE(sre_product_avg_monthly_sales('" + adOrgId + "', '" + adClientId
        + "', '" + product.getId() + "',"
        + "                                        DATE(to_char((TO_DATE('" + docDate
        + "') - interval '4 month'),'YYYY-MM') || '-01'), "
        + "                                        DATE(to_char((TO_DATE('" + docDate
        + "') - interval '3 month'),'YYYY-MM') || '-01') - 1), 0) AS avg_monthly_sales_8, "
        + "          COALESCE(sre_product_avg_monthly_specif('" + adOrgId + "', '" + adClientId
        + "', '" + product.getId() + "',"
        + "                                        DATE(to_char((TO_DATE('" + docDate
        + "') - interval '3 month'),'YYYY-MM') || '-01'), "
        + "                                        DATE(to_char((TO_DATE('" + docDate
        + "') - interval '2 month'),'YYYY-MM') || '-01') - 1,null), 0) AS avg_monthly_sales_9, "
        + "          COALESCE(sre_product_avg_monthly_specif('" + adOrgId + "', '" + adClientId
        + "', '" + product.getId() + "',"
        + "                                        DATE(to_char((TO_DATE('" + docDate
        + "') - interval '2 month'),'YYYY-MM') || '-01'), "
        + "                                        DATE(to_char((TO_DATE('" + docDate
        + "') - interval '1 month'),'YYYY-MM') || '-01') - 1,null), 0) AS avg_monthly_sales_10, "
        + "          COALESCE(sre_product_avg_monthly_specif('" + adOrgId + "', '" + adClientId
        + "', '" + product.getId() + "',"
        + "                                        DATE(to_char((TO_DATE('" + docDate
        + "') - interval '1 month'),'YYYY-MM') || '-01'), "
        + "                                        DATE(to_char((TO_DATE('" + docDate
        + "') - interval '0 month'),'YYYY-MM') || '-01') - 1,null), 0) AS avg_monthly_sales_11, "
        + "          COALESCE(sre_product_avg_monthly_specif('" + adOrgId + "', '" + adClientId
        + "', '" + product.getId() + "',"
        + "                                        DATE(to_char((TO_DATE('" + docDate
        + "') - interval '0 month'),'YYYY-MM') || '-01'), "
        + "                                        DATE(TO_DATE('" + docDate
        + "')),null), 0) AS avg_monthly_sales_12 ";

    // System.out.println("STRSQL:" + strSql);
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);

    DecimalFormat df = new DecimalFormat("#0.###");
    df.setRoundingMode(RoundingMode.HALF_UP);

    long countRecord = 0;
    int countColumn = 0, numberOfMonths = new Integer(numMonths).intValue();

    if (product != null) {
      try {
        Query sqlQuery = OBDal.getInstance().getSession().createSQLQuery(strSql);

        List<Object> productdata = sqlQuery.list();
        for (int k = 0; k < productdata.size(); k++) {
          Object[] obj = (Object[]) productdata.get(k);
          countRecord++;
          countColumn = 0;

          ReportProductAvgMonthlySalesData objectReportProductAvgMonthlySalesData = new ReportProductAvgMonthlySalesData();
          objectReportProductAvgMonthlySalesData.productid = product.getId();
          objectReportProductAvgMonthlySalesData.searchkey = product.getSearchKey();
          objectReportProductAvgMonthlySalesData.internalcode = (product.getScrInternalcode() != null) ? product
              .getScrInternalcode() : "--";
          objectReportProductAvgMonthlySalesData.name = product.getName();

          if (countColumn < numberOfMonths) {
            objectReportProductAvgMonthlySalesData.avgmonthlysales12 = df.format(obj[11]);
          } else {
            objectReportProductAvgMonthlySalesData.avgmonthlysales12 = "-1";
          }
          countColumn++;

          if (countColumn < numberOfMonths) {
            objectReportProductAvgMonthlySalesData.avgmonthlysales11 = df.format(obj[10]);
          } else {
            objectReportProductAvgMonthlySalesData.avgmonthlysales11 = "-1";
          }
          countColumn++;

          if (countColumn < numberOfMonths) {
            objectReportProductAvgMonthlySalesData.avgmonthlysales10 = df.format(obj[9]);
          } else {
            objectReportProductAvgMonthlySalesData.avgmonthlysales10 = "-1";
          }
          countColumn++;

          if (countColumn < numberOfMonths) {
            objectReportProductAvgMonthlySalesData.avgmonthlysales9 = df.format(obj[8]);
          } else {
            objectReportProductAvgMonthlySalesData.avgmonthlysales9 = "-1";
          }
          countColumn++;

          if (countColumn < numberOfMonths) {
            objectReportProductAvgMonthlySalesData.avgmonthlysales8 = df.format(obj[7]);
          } else {
            objectReportProductAvgMonthlySalesData.avgmonthlysales8 = "-1";
          }
          countColumn++;

          if (countColumn < numberOfMonths) {
            objectReportProductAvgMonthlySalesData.avgmonthlysales7 = df.format(obj[6]);
          } else {
            objectReportProductAvgMonthlySalesData.avgmonthlysales7 = "-1";
          }
          countColumn++;

          if (countColumn < numberOfMonths) {
            objectReportProductAvgMonthlySalesData.avgmonthlysales6 = df.format(obj[5]);
          } else {
            objectReportProductAvgMonthlySalesData.avgmonthlysales6 = "-1";
          }
          countColumn++;

          if (countColumn < numberOfMonths) {
            objectReportProductAvgMonthlySalesData.avgmonthlysales5 = df.format(obj[4]);
          } else {
            objectReportProductAvgMonthlySalesData.avgmonthlysales5 = "-1";
          }
          countColumn++;

          if (countColumn < numberOfMonths) {
            objectReportProductAvgMonthlySalesData.avgmonthlysales4 = df.format(obj[3]);
          } else {
            objectReportProductAvgMonthlySalesData.avgmonthlysales4 = "-1";
          }
          countColumn++;

          if (countColumn < numberOfMonths) {
            objectReportProductAvgMonthlySalesData.avgmonthlysales3 = df.format(obj[2]);
          } else {
            objectReportProductAvgMonthlySalesData.avgmonthlysales3 = "-1";
          }
          countColumn++;

          if (countColumn < numberOfMonths) {
            objectReportProductAvgMonthlySalesData.avgmonthlysales2 = df.format(obj[1]);
          } else {
            objectReportProductAvgMonthlySalesData.avgmonthlysales2 = "-1";
          }
          countColumn++;

          if (countColumn < numberOfMonths) {
            objectReportProductAvgMonthlySalesData.avgmonthlysales1 = df.format(obj[0]);
          } else {
            objectReportProductAvgMonthlySalesData.avgmonthlysales1 = "-1";
          }

          objectReportProductAvgMonthlySalesData.rownum = Long.toString(countRecord);
          objectReportProductAvgMonthlySalesData.InitRecordNumber = Integer.toString(firstRegister);

          vector.addElement(objectReportProductAvgMonthlySalesData);
        }

      } catch (Exception ex) {
        log4j.error("Exception in query: " + strSql + "Exception:" + ex);
        throw new ServletException("@CODE=@" + ex.getMessage());
      }
    }

    ReportProductAvgMonthlySalesData objectReportProductAvgMonthlySalesData[] = new ReportProductAvgMonthlySalesData[vector
        .size()];
    vector.copyInto(objectReportProductAvgMonthlySalesData);

    return (objectReportProductAvgMonthlySalesData);
  }

  public static ReportProductAvgMonthlySalesData[] set() throws ServletException {
    ReportProductAvgMonthlySalesData objectReportProductAvgMonthlySalesData[] = new ReportProductAvgMonthlySalesData[1];
    objectReportProductAvgMonthlySalesData[0] = new ReportProductAvgMonthlySalesData();
    objectReportProductAvgMonthlySalesData[0].productid = "";
    objectReportProductAvgMonthlySalesData[0].searchkey = "";
    objectReportProductAvgMonthlySalesData[0].internalcode = "";
    objectReportProductAvgMonthlySalesData[0].name = "";
    objectReportProductAvgMonthlySalesData[0].avgmonthlysales1 = "-1";
    objectReportProductAvgMonthlySalesData[0].avgmonthlysales2 = "-1";
    objectReportProductAvgMonthlySalesData[0].avgmonthlysales3 = "-1";
    objectReportProductAvgMonthlySalesData[0].avgmonthlysales4 = "-1";
    objectReportProductAvgMonthlySalesData[0].avgmonthlysales5 = "-1";
    objectReportProductAvgMonthlySalesData[0].avgmonthlysales6 = "-1";
    objectReportProductAvgMonthlySalesData[0].avgmonthlysales7 = "-1";
    objectReportProductAvgMonthlySalesData[0].avgmonthlysales8 = "-1";
    objectReportProductAvgMonthlySalesData[0].avgmonthlysales9 = "-1";
    objectReportProductAvgMonthlySalesData[0].avgmonthlysales10 = "-1";
    objectReportProductAvgMonthlySalesData[0].avgmonthlysales11 = "-1";
    objectReportProductAvgMonthlySalesData[0].avgmonthlysales12 = "-1";

    return objectReportProductAvgMonthlySalesData;
  }

  public static String selectMproduct(ConnectionProvider connectionProvider, String mProductId)
      throws ServletException {
    String strSql = "";
    strSql = strSql + "      SELECT (M_PRODUCT.VALUE || ' - ' || M_PRODUCT.NAME) AS name  FROM M_PRODUCT"
        + "      WHERE M_PRODUCT.M_PRODUCT_ID = ?";

    ResultSet result;
    String strReturn = "";
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, mProductId);

      result = st.executeQuery();
      if (result.next()) {
        strReturn = UtilSql.getValue(result, "name");
      }
      result.close();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    return (strReturn);
  }

  public static int getDifferenceBetwwenDatesInDays(Date start_date, Date end_date) {
    final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;

    int diffInDays = (int) ((end_date.getTime() - start_date.getTime() + 1) / DAY_IN_MILLIS);

    if (diffInDays < 0)
      return 0;
    return diffInDays;
  }
}
