package pe.com.unifiedgo.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

import pe.com.unifiedgo.accounting.data.SCOParallelconvRate;

public class NewParallelConversionRate extends DalBaseProcess {
  public void doExecute(ProcessBundle bundle) throws Exception {
    try {
      /*
       * Set<String> params = bundle.getParams().keySet(); for (int i = 0; i < params.size(); i++) {
       * System.out.println("param: "+params.toArray()[i]); }
       */

      final String conversionRateID = (String) bundle.getParams().get("SCO_Parallelconv_Rate_ID");

      SCOParallelconvRate crAux = OBDal.getInstance().get(SCOParallelconvRate.class, conversionRateID);

      String dttFechaStr = (String) bundle.getParams().get("date");

      String saleUSDStr = (String) bundle.getParams().get("salesconvratetousd");

      String strDateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("dateFormat.java");
      final SimpleDateFormat dateFormat = new SimpleDateFormat(strDateFormat);

      Date dttFecha = dateFormat.parse(dttFechaStr);

      Currency currUsd = OBDal.getInstance().get(Currency.class, "100");
      Currency currPen = OBDal.getInstance().get(Currency.class, "308");

      // sale usd
      BigDecimal bd;
      if (saleUSDStr != null && saleUSDStr.length() > 0) {
        double dSaleUSD = Double.parseDouble(saleUSDStr);
        if (dSaleUSD != 0) {
          if (existsParallelConversionRateFor(dttFecha, currUsd, currPen, crAux.getClient(), crAux.getOrganization())) {
            throw new Exception("20504");
          }

          // USD to PEN
          SCOParallelconvRate cr1 = OBProvider.getInstance().get(SCOParallelconvRate.class);
          bd = new BigDecimal(dSaleUSD);

          cr1.setClient(crAux.getClient());
          cr1.setOrganization(crAux.getOrganization());
          cr1.setConversionRateType("S");
          cr1.setCurrency(currUsd);
          cr1.setToCurrency(currPen);
          cr1.setValidFromDate(dttFecha);
          cr1.setValidToDate(dttFecha);
          cr1.setDivideRateBy(BigDecimal.ONE.divide(bd, 15, RoundingMode.HALF_UP));
          cr1.setMultipleRateBy(bd);

          OBDal.getInstance().save(cr1);

          // ////////////////////////////////////////
          if (existsParallelConversionRateFor(dttFecha, currPen, currUsd, crAux.getClient(), crAux.getOrganization())) {
            throw new Exception("20504");
          }

          // PEN to USD
          SCOParallelconvRate cr2 = OBProvider.getInstance().get(SCOParallelconvRate.class);
          bd = new BigDecimal(dSaleUSD);

          cr2.setClient(crAux.getClient());
          cr2.setOrganization(crAux.getOrganization());
          cr2.setConversionRateType("S");
          cr2.setCurrency(currPen);
          cr2.setToCurrency(currUsd);
          cr2.setValidFromDate(dttFecha);
          cr2.setValidToDate(dttFecha);
          cr2.setDivideRateBy(bd);
          cr2.setMultipleRateBy(BigDecimal.ONE.divide(bd, 15, RoundingMode.HALF_UP));

          OBDal.getInstance().save(cr2);
        }
      }

      final OBError msg = new OBError();
      msg.setType("Success");
      msg.setTitle(OBMessageUtils.getI18NMessage("OBUIAPP_Success", null));
      msg.setMessage(OBMessageUtils.getI18NMessage("SCO_ConversionRate_Success", null));
      bundle.setResult(msg);

    } catch (final Exception e) {
      // e.printStackTrace();
      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(OBMessageUtils.getI18NMessage(e.getMessage(), null));
      msg.setTitle(OBMessageUtils.getI18NMessage("Error", null));
      bundle.setResult(msg);
    }
  }

  public boolean existsParallelConversionRateFor(Date _dttFecha, Currency curr, Currency currTo, Client client, Organization org) {

    // TRUNCATE DATE
    Date dttFecha = null;
    try {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      dttFecha = sdf.parse(sdf.format(_dttFecha));
    } catch (Exception e) {
      return true;
    }

    OBCriteria<SCOParallelconvRate> convrates = OBDal.getInstance().createCriteria(SCOParallelconvRate.class);
    convrates.add(Restrictions.and(Restrictions.le(SCOParallelconvRate.PROPERTY_VALIDFROMDATE, dttFecha), Restrictions.ge(SCOParallelconvRate.PROPERTY_VALIDTODATE, dttFecha)));
    convrates.add(Restrictions.eq(SCOParallelconvRate.PROPERTY_CURRENCY, curr));
    convrates.add(Restrictions.eq(SCOParallelconvRate.PROPERTY_TOCURRENCY, currTo));
    convrates.add(Restrictions.eq(SCOParallelconvRate.PROPERTY_ORGANIZATION, org));
    convrates.add(Restrictions.eq(SCOParallelconvRate.PROPERTY_CLIENT, client));

    if (convrates.list().size() > 0)
      return true;
    return false;
  }

}