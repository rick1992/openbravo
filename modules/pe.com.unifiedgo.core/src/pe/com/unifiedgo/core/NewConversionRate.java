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
import org.openbravo.model.common.currency.ConversionRate;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

public class NewConversionRate extends DalBaseProcess {
  public void doExecute(ProcessBundle bundle) throws Exception {
    try {
      /*
       * Set<String> params = bundle.getParams().keySet(); for (int i = 0; i < params.size(); i++) {
       * System.out.println("param: "+params.toArray()[i]); }
       */

      final String conversionRateID = (String) bundle.getParams().get("C_Conversion_Rate_ID");

      ConversionRate crAux = OBDal.getInstance().get(ConversionRate.class, conversionRateID);

      String dttFechaStr = (String) bundle.getParams().get("date");

      String saleUSDStr = (String) bundle.getParams().get("salesconvratetousd");
      String purchaseUSDStr = (String) bundle.getParams().get("purchaseconvratetousd");
      String saleEURStr = (String) bundle.getParams().get("salesconvratetoeur");

      String strDateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("dateFormat.java");
      final SimpleDateFormat dateFormat = new SimpleDateFormat(strDateFormat);

      Date dttFecha = dateFormat.parse(dttFechaStr);

      Currency currUsd = OBDal.getInstance().get(Currency.class, "100");
      Currency currPen = OBDal.getInstance().get(Currency.class, "308");
      Currency currEur = OBDal.getInstance().get(Currency.class, "102");

      // sale usd
      if (saleUSDStr != null && saleUSDStr.length() > 0) {
        double dSaleUSD = Double.parseDouble(saleUSDStr);
        if (dSaleUSD != 0) {
          if (existsConversionRateFor(dttFecha, currUsd, currPen, crAux.getClient(), crAux.getOrganization())) {
            throw new Exception("20504");
          }

          ConversionRate cr = OBProvider.getInstance().get(ConversionRate.class);
          BigDecimal bd = new BigDecimal(dSaleUSD);

          cr.setClient(crAux.getClient());
          cr.setOrganization(crAux.getOrganization());
          cr.setConversionRateType("S");
          cr.setCurrency(currUsd);
          cr.setToCurrency(currPen);
          cr.setValidFromDate(dttFecha);
          cr.setValidToDate(dttFecha);
          cr.setDivideRateBy(BigDecimal.ONE.divide(bd, 15, RoundingMode.HALF_UP));
          cr.setMultipleRateBy(bd);

          OBDal.getInstance().save(cr);
        }
      }

      // sale eur
      if (saleEURStr != null && saleEURStr.length() > 0) {
        double dSaleEUR = Double.parseDouble(saleEURStr);
        if (dSaleEUR != 0) {
          if (existsConversionRateFor(dttFecha, currEur, currPen, crAux.getClient(), crAux.getOrganization())) {
            throw new Exception("20504");
          }

          ConversionRate cr = OBProvider.getInstance().get(ConversionRate.class);
          BigDecimal bd = new BigDecimal(dSaleEUR);

          cr.setClient(crAux.getClient());
          cr.setOrganization(crAux.getOrganization());
          cr.setConversionRateType("S");
          cr.setCurrency(currEur);
          cr.setToCurrency(currPen);
          cr.setValidFromDate(dttFecha);
          cr.setValidToDate(dttFecha);
          cr.setDivideRateBy(BigDecimal.ONE.divide(bd, 15, RoundingMode.HALF_UP));
          cr.setMultipleRateBy(bd);

          OBDal.getInstance().save(cr);
        }
      }

      // purchase usd
      if (purchaseUSDStr != null && purchaseUSDStr.length() > 0) {
        double dPurchaseUSD = Double.parseDouble(purchaseUSDStr);
        if (dPurchaseUSD != 0) {
          if (existsConversionRateFor(dttFecha, currPen, currUsd, crAux.getClient(), crAux.getOrganization())) {
            throw new Exception("20504");
          }

          ConversionRate cr = OBProvider.getInstance().get(ConversionRate.class);
          BigDecimal bd = new BigDecimal(dPurchaseUSD);

          cr.setClient(crAux.getClient());
          cr.setOrganization(crAux.getOrganization());
          cr.setConversionRateType("S");
          cr.setCurrency(currPen);
          cr.setToCurrency(currUsd);
          cr.setValidFromDate(dttFecha);
          cr.setValidToDate(dttFecha);
          cr.setDivideRateBy(bd);
          cr.setMultipleRateBy(BigDecimal.ONE.divide(bd, 15, RoundingMode.HALF_UP));

          OBDal.getInstance().save(cr);
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

  public boolean existsConversionRateFor(Date _dttFecha, Currency curr, Currency currTo, Client client, Organization org) {

    // TRUNCATE DATE
    Date dttFecha = null;
    try {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      dttFecha = sdf.parse(sdf.format(_dttFecha));
    } catch (Exception e) {
      return true;
    }

    OBCriteria<ConversionRate> convrates = OBDal.getInstance().createCriteria(ConversionRate.class);
    convrates.add(Restrictions.and(Restrictions.le(ConversionRate.PROPERTY_VALIDFROMDATE, dttFecha), Restrictions.ge(ConversionRate.PROPERTY_VALIDTODATE, dttFecha)));
    convrates.add(Restrictions.eq(ConversionRate.PROPERTY_CURRENCY, curr));
    convrates.add(Restrictions.eq(ConversionRate.PROPERTY_TOCURRENCY, currTo));
    convrates.add(Restrictions.eq(ConversionRate.PROPERTY_ORGANIZATION, org));
    convrates.add(Restrictions.eq(ConversionRate.PROPERTY_CLIENT, client));

    if (convrates.list().size() > 0)
      return true;
    return false;
  }
}