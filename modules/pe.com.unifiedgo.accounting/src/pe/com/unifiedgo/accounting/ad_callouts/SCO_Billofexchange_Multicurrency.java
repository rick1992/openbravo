package pe.com.unifiedgo.accounting.ad_callouts;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.currency.ConversionRate;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.service.db.DalConnectionProvider;

import pe.com.unifiedgo.accounting.SCO_Utils;
import pe.com.unifiedgo.accounting.data.SCOParallelconvRate;

public class SCO_Billofexchange_Multicurrency extends SimpleCallout {
  private static final long serialVersionUID = 1L;
  static Logger log4j = Logger.getLogger(SCO_Billofexchange_Multicurrency.class);
  private static final RequestFilter filterYesNo = new ValueListFilter("Y", "N");

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    // Enumeration<String> params = info.vars.getParameterNames();
    // while (params.hasMoreElements()) {
    // System.out.println(params.nextElement());
    // }

    VariablesSecureApp vars = info.vars;
    String lastFieldChanged = info.getLastFieldChanged();

    final String strinpissotrx = vars.getStringParameter("inpissotrx");
    final String strOrgId = vars.getStringParameter("inpadOrgId");
    final String strMPriceListID = vars.getStringParameter("inpmPricelistId");
    String strConvertRate = vars.getNumericParameter("inpconvertRate");
    String strAltConvertRate = vars.getNumericParameter("inpaltConvertRate");
    String strDategen = vars.getStringParameter("inpdategen");
    String strDateacct = vars.getStringParameter("inpdateacct");
    String currencyId = vars.getStringParameter("inpcCurrencyId");
    String otherCurrencyId = vars.getStringParameter("inpcOtherCurrencyId");

    boolean isUseSalesTCOnly = SCO_Utils.isUseSalesTCOnly(new DalConnectionProvider(),
        vars.getClient());

    if ("inpcOtherCurrencyId".equals(lastFieldChanged) || "inpdateacct".equals(lastFieldChanged)) {
      Currency currency = OBDal.getInstance().get(Currency.class, currencyId);
      Currency otherCurrency = OBDal.getInstance().get(Currency.class, otherCurrencyId);
      BigDecimal convertRate = BigDecimal.ONE;
      if (currency != null && otherCurrency != null) {
        if (!otherCurrency.equals(currency)) {

          if (isUseSalesTCOnly) {
            // Siempre usar TC Venta
            ConversionRate conversionRate = null;
            String polarity = SCO_Utils.calculateRatePolarity(currency.getId(),
                otherCurrency.getId());
            if (polarity.equals("E")) {
              conversionRate = getConversionRate(currency, otherCurrency, toDate(strDateacct),
                  OBDal.getInstance().get(Organization.class, strOrgId));

              if (conversionRate != null) {
                convertRate = conversionRate.getDivideRateBy();
                info.addResult("inpconvertRate", convertRate);
                BigDecimal altconvrate = SCO_Utils.calculateAltConvRate(otherCurrencyId, currencyId,
                    convertRate);

                info.addResult("inpaltConvertRate", altconvrate);

              } else {
                info.addResult("inpconvertRate", "");
                info.addResult("inpaltConvertRate", "");
              }
            } else {
              conversionRate = getConversionRate(otherCurrency, currency, toDate(strDateacct),
                  OBDal.getInstance().get(Organization.class, strOrgId));

              if (conversionRate != null) {
                convertRate = conversionRate.getMultipleRateBy();
                info.addResult("inpconvertRate", convertRate);
                BigDecimal altconvrate = SCO_Utils.calculateAltConvRate(otherCurrencyId, currencyId,
                    convertRate);

                info.addResult("inpaltConvertRate", altconvrate);

              } else {
                info.addResult("inpconvertRate", "");
                info.addResult("inpaltConvertRate", "");
              }
            }
          } else {
            if (strinpissotrx.equals("Y")) {
              final ConversionRate conversionRate = getConversionRate(otherCurrency, currency,
                  toDate(strDateacct), OBDal.getInstance().get(Organization.class, strOrgId));

              if (conversionRate != null) {
                convertRate = conversionRate.getMultipleRateBy();

                info.addResult("inpconvertRate", convertRate);
                BigDecimal altconvrate = SCO_Utils.calculateAltConvRate(otherCurrencyId, currencyId,
                    convertRate);
                info.addResult("inpaltConvertRate", altconvrate);

              } else {
                info.addResult("inpconvertRate", "");
                info.addResult("inpaltConvertRate", "");
              }

            } else {

              final ConversionRate conversionRate = getConversionRate(otherCurrency, currency,
                  toDate(strDateacct), OBDal.getInstance().get(Organization.class, strOrgId));

              if (conversionRate != null) {
                convertRate = conversionRate.getMultipleRateBy();
                info.addResult("inpconvertRate", convertRate);
                BigDecimal altconvrate = SCO_Utils.calculateAltConvRate(otherCurrencyId, currencyId,
                    convertRate);

                info.addResult("inpaltConvertRate", altconvrate);

              } else {
                info.addResult("inpconvertRate", "");
                info.addResult("inpaltConvertRate", "");
              }
            }
          }
        } else {
          info.addResult("inpconvertRate", convertRate);
          info.addResult("inpaltConvertRate", convertRate);
        }
      }

    } else if ("inpconvertRate".equals(lastFieldChanged)) {
      Currency currency = OBDal.getInstance().get(Currency.class, currencyId);
      Currency otherCurrency = OBDal.getInstance().get(Currency.class, otherCurrencyId);
      BigDecimal convertRate = BigDecimal.ONE;

      if (!strConvertRate.isEmpty() && currency != null) {
        BigDecimal altconvrate = SCO_Utils.calculateAltConvRate(otherCurrencyId, currencyId,
            new BigDecimal(strConvertRate));
        info.addResult("inpaltConvertRate", altconvrate);
      } else {
        info.addResult("inpaltConvertRate", "");
      }

    } else if ("inpaltConvertRate".equals(lastFieldChanged)) {
      Currency currency = OBDal.getInstance().get(Currency.class, currencyId);
      Currency otherCurrency = OBDal.getInstance().get(Currency.class, otherCurrencyId);
      BigDecimal convertRate = BigDecimal.ONE;

      if (!strAltConvertRate.isEmpty() && currency != null) {
        convertRate = SCO_Utils.calculateNormalConvRate(otherCurrencyId, currencyId,
            new BigDecimal(strAltConvertRate));
        info.addResult("inpconvertRate", convertRate);
      } else {
        info.addResult("inpconvertRate", "");
      }
    }

  }

  /**
   * Determine the conversion rate from one currency to another on a given date. Will use the spot
   * conversion rate defined by the system for that date
   * 
   * @param fromCurrency
   *          Currency to convert from
   * @param toCurrency
   *          Currency being converted to
   * @param conversionDate
   *          Date conversion is being performed
   * @return A valid conversion rate for the parameters, or null if no conversion rate can be found
   */
  private ConversionRate getConversionRate(Currency fromCurrency, Currency toCurrency,
      Date _conversionDate, Organization org) {

    // TRUNCATE DATE
    Date conversionDate = null;
    try {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      conversionDate = sdf.parse(sdf.format(_conversionDate));
    } catch (Exception e) {
      return null;
    }

    java.util.List<ConversionRate> conversionRateList;
    ConversionRate conversionRate;
    OBContext.setAdminMode(true);
    try {
      final OBCriteria<ConversionRate> obcConvRate = OBDal.getInstance()
          .createCriteria(ConversionRate.class);
      obcConvRate.setFilterOnReadableOrganization(false);
      obcConvRate.add(Restrictions.eq(ConversionRate.PROPERTY_ORGANIZATION, org));
      obcConvRate.add(Restrictions.eq(ConversionRate.PROPERTY_CURRENCY, fromCurrency));
      obcConvRate.add(Restrictions.eq(ConversionRate.PROPERTY_TOCURRENCY, toCurrency));
      obcConvRate.add(Restrictions.le(ConversionRate.PROPERTY_VALIDFROMDATE, conversionDate));
      obcConvRate.add(Restrictions.ge(ConversionRate.PROPERTY_VALIDTODATE, conversionDate));
      conversionRateList = obcConvRate.list();
      if ((conversionRateList != null) && (conversionRateList.size() != 0)) {
        conversionRate = conversionRateList.get(0);
      } else {
        if ("0".equals(org.getId())) {
          conversionRate = null;
        } else {
          return getConversionRate(fromCurrency, toCurrency, conversionDate,
              OBDal.getInstance().get(Organization.class, OBContext.getOBContext()
                  .getOrganizationStructureProvider().getParentOrg(org.getId())));
        }
      }
    } catch (Exception e) {
      log4j.error(e);
      return null;
    } finally {
      OBContext.restorePreviousMode();
    }
    return conversionRate;
  }

  private SCOParallelconvRate getSCOParallelConversionRate(Currency fromCurrency,
      Currency toCurrency, Date _conversionDate, Organization org) {

    // TRUNCATE DATE
    Date conversionDate = null;
    try {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      conversionDate = sdf.parse(sdf.format(_conversionDate));
    } catch (Exception e) {
      return null;
    }

    java.util.List<SCOParallelconvRate> conversionRateList;
    SCOParallelconvRate conversionRate;
    OBContext.setAdminMode(true);
    try {
      final OBCriteria<SCOParallelconvRate> obcConvRate = OBDal.getInstance()
          .createCriteria(SCOParallelconvRate.class);
      obcConvRate.setFilterOnReadableOrganization(false);
      obcConvRate.add(Restrictions.eq(SCOParallelconvRate.PROPERTY_ORGANIZATION, org));
      obcConvRate.add(Restrictions.eq(SCOParallelconvRate.PROPERTY_CURRENCY, fromCurrency));
      obcConvRate.add(Restrictions.eq(SCOParallelconvRate.PROPERTY_TOCURRENCY, toCurrency));
      obcConvRate.add(Restrictions.le(SCOParallelconvRate.PROPERTY_VALIDFROMDATE, conversionDate));
      obcConvRate.add(Restrictions.ge(SCOParallelconvRate.PROPERTY_VALIDTODATE, conversionDate));
      conversionRateList = obcConvRate.list();
      if ((conversionRateList != null) && (conversionRateList.size() != 0)) {
        conversionRate = conversionRateList.get(0);
      } else {
        if ("0".equals(org.getId())) {
          conversionRate = null;
        } else {
          return getSCOParallelConversionRate(fromCurrency, toCurrency, conversionDate,
              OBDal.getInstance().get(Organization.class, OBContext.getOBContext()
                  .getOrganizationStructureProvider().getParentOrg(org.getId())));
        }
      }
    } catch (Exception e) {
      log4j.error(e);
      return null;
    } finally {
      OBContext.restorePreviousMode();
    }
    return conversionRate;
  }

  /**
   * Convert a string to a Date object using the standard java date format
   * 
   * @param strDate
   *          String with date in java date format
   * @return valid Date object, or null if string cannot be parsed into a date
   */
  public static Date toDate(String strDate) {
    if (strDate == null || strDate.isEmpty())
      return null;
    try {
      String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("dateFormat.java");
      SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormat);
      return (outputFormat.parse(strDate));
    } catch (ParseException e) {
      log4j.error(e.getMessage(), e);
      return null;
    }
  }

}
