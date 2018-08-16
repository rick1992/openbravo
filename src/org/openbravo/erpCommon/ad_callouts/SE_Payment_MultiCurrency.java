/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.currency.ConversionRate;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.service.db.DalConnectionProvider;

import pe.com.unifiedgo.accounting.SCO_Utils;
import pe.com.unifiedgo.accounting.data.SCOParallelconvRate;

public class SE_Payment_MultiCurrency extends SimpleCallout {

  private static final long serialVersionUID = 1L;
  static Logger log4j = Logger.getLogger(SE_Payment_MultiCurrency.class);
  private static final RequestFilter filterYesNo = new ValueListFilter("Y", "N");

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    VariablesSecureApp vars = info.vars;
    String lastFieldChanged = info.getLastFieldChanged();

    /*
     * Enumeration<String> e = vars.getParameterNames(); while (e.hasMoreElements()) {
     * System.out.println(e.nextElement()); }
     */

    // Read input fields
    String strFinaccTxnAmount = vars.getNumericParameter("inpfinaccTxnAmount");
    String strConvertRate = vars.getNumericParameter("inpfinaccTxnConvertRate");
    String strAltConvertRate = vars.getNumericParameter("inpemScoAltConvertRate");
    String paymentDate = vars.getStringParameter("inppaymentdate");
    String strAmount = vars.getNumericParameter("inpgeneratedCredit").isEmpty() || (BigDecimal.ZERO)
        .compareTo(new BigDecimal(vars.getNumericParameter("inpgeneratedCredit"))) == 0
            ? vars.getNumericParameter("inpamount")
            : vars.getNumericParameter("inpgeneratedCredit");
    String currencyId = vars.getStringParameter("inpcCurrencyId");
    String strOrgId = vars.getStringParameter("inpadOrgId");
    String financialAccountId = vars.getStringParameter("inpfinFinancialAccountId");
    String strIsReceipt = vars.getStringParameter("inpisreceipt", filterYesNo);

    String finPaymentMethodId = vars.getStringParameter("inpfinPaymentmethodId");
    FIN_PaymentMethod finPaymentMethod = OBDal.getInstance().get(FIN_PaymentMethod.class,
        finPaymentMethodId);

    boolean isUseSalesTCOnly = SCO_Utils.isUseSalesTCOnly(new DalConnectionProvider(),
        vars.getClient());

    if ("inpcCurrencyId".equals(lastFieldChanged) || "inppaymentdate".equals(lastFieldChanged)) {

      FIN_FinancialAccount financialAccount = OBDal.getInstance().get(FIN_FinancialAccount.class,
          financialAccountId);
      Currency currency = OBDal.getInstance().get(Currency.class, currencyId);
      BigDecimal finAccConvertRate = BigDecimal.ONE;
      BigDecimal finAccTxnAmount = BigDecimal.ZERO;
      if (financialAccount != null && currency != null) { // Multicurrency
        final Currency financialAccountCurrency = financialAccount.getCurrency();
        if (!currency.equals(financialAccountCurrency)) {

          if (isUseSalesTCOnly) {
            // Siempre usar TC Venta
            ConversionRate conversionRate = null;
            String polarity = SCO_Utils.calculateRatePolarity(financialAccountCurrency.getId(),
                currency.getId());
            if (polarity.equals("E")) {
              conversionRate = getConversionRate(financialAccountCurrency, currency,
                  toDate(paymentDate), OBDal.getInstance().get(Organization.class, strOrgId));

              if (conversionRate != null) {
                finAccConvertRate = conversionRate.getMultipleRateBy();
                info.addResult("inpfinaccTxnConvertRate", finAccConvertRate);
                BigDecimal altconvrate = SCO_Utils.calculateAltConvRate(
                    financialAccountCurrency.getId(), currencyId, finAccConvertRate);

                info.addResult("inpemScoAltConvertRate", altconvrate);

              } else {
                info.addResult("inpfinaccTxnConvertRate", "");
                info.addResult("inpemScoAltConvertRate", "");
              }
              if (!strAmount.isEmpty() && conversionRate != null) {
                // UGO this part doesnt matter because new version strAmount = finAcctTxnAmount
                BigDecimal amount = new BigDecimal(strAmount);
                BigDecimal converted = amount.divide(conversionRate.getMultipleRateBy(),
                    financialAccountCurrency.getStandardPrecision().intValue(),
                    RoundingMode.HALF_UP);
                finAccTxnAmount = converted;
                info.addResult("inpfinaccTxnAmount", amount);
              } else {
                info.addResult("inpfinaccTxnAmount", "");
              }
            } else {
              conversionRate = getConversionRate(currency, financialAccountCurrency,
                  toDate(paymentDate), OBDal.getInstance().get(Organization.class, strOrgId));

              if (conversionRate != null) {
                finAccConvertRate = conversionRate.getDivideRateBy();
                info.addResult("inpfinaccTxnConvertRate", finAccConvertRate);
                BigDecimal altconvrate = SCO_Utils.calculateAltConvRate(
                    financialAccountCurrency.getId(), currencyId, finAccConvertRate);

                info.addResult("inpemScoAltConvertRate", altconvrate);

              } else {
                info.addResult("inpfinaccTxnConvertRate", "");
                info.addResult("inpemScoAltConvertRate", "");
              }
              if (!strAmount.isEmpty() && conversionRate != null) {
                // UGO this part doesnt matter because new version strAmount = finAcctTxnAmount
                BigDecimal amount = new BigDecimal(strAmount);
                BigDecimal converted = amount.divide(conversionRate.getMultipleRateBy(),
                    financialAccountCurrency.getStandardPrecision().intValue(),
                    RoundingMode.HALF_UP);
                finAccTxnAmount = converted;
                info.addResult("inpfinaccTxnAmount", amount);
              } else {
                info.addResult("inpfinaccTxnAmount", "");
              }
            }

          } else {
            // COBRO (PAYMENT IN)
            if (strIsReceipt.equals("Y")) {
              final ConversionRate conversionRate = getConversionRate(currency,
                  financialAccountCurrency, toDate(paymentDate),
                  OBDal.getInstance().get(Organization.class, strOrgId));

              if (conversionRate != null) {
                finAccConvertRate = conversionRate.getDivideRateBy();

                info.addResult("inpfinaccTxnConvertRate", finAccConvertRate);
                BigDecimal altconvrate = SCO_Utils.calculateAltConvRate(
                    financialAccountCurrency.getId(), currencyId, finAccConvertRate);
                info.addResult("inpemScoAltConvertRate", altconvrate);

              } else {
                info.addResult("inpfinaccTxnConvertRate", "");
                info.addResult("inpemScoAltConvertRate", "");
              }
              if (!strAmount.isEmpty() && conversionRate != null) {
                // UGO this part doesnt matter because new version strAmount = finAcctTxnAmount
                BigDecimal amount = new BigDecimal(strAmount);
                BigDecimal converted = amount.multiply(conversionRate.getDivideRateBy());
                finAccTxnAmount = converted;
                info.addResult("inpfinaccTxnAmount", amount);
              } else {
                info.addResult("inpfinaccTxnAmount", "");
              }

              // PAGO (PAYMENT OUT)
            } else {

              final ConversionRate conversionRate = getConversionRate(currency,
                  financialAccountCurrency, toDate(paymentDate),
                  OBDal.getInstance().get(Organization.class, strOrgId));

              if (conversionRate != null) {
                finAccConvertRate = conversionRate.getDivideRateBy();
                info.addResult("inpfinaccTxnConvertRate", finAccConvertRate);
                BigDecimal altconvrate = SCO_Utils.calculateAltConvRate(
                    financialAccountCurrency.getId(), currencyId, finAccConvertRate);

                info.addResult("inpemScoAltConvertRate", altconvrate);

              } else {
                info.addResult("inpfinaccTxnConvertRate", "");
                info.addResult("inpemScoAltConvertRate", "");
              }
              if (!strAmount.isEmpty() && conversionRate != null) {
                // UGO this part doesnt matter because new version strAmount = finAcctTxnAmount
                BigDecimal amount = new BigDecimal(strAmount);
                BigDecimal converted = amount.divide(conversionRate.getMultipleRateBy(),
                    financialAccountCurrency.getStandardPrecision().intValue(),
                    RoundingMode.HALF_UP);
                finAccTxnAmount = converted;
                info.addResult("inpfinaccTxnAmount", amount);
              } else {
                info.addResult("inpfinaccTxnAmount", "");
              }
            }
          }

        } else {
          info.addResult("inpfinaccTxnConvertRate", finAccConvertRate);
          info.addResult("inpemScoAltConvertRate", finAccConvertRate);
        }
      }

      if ("inppaymentdate".equals(lastFieldChanged)) {
        // show warning message if paymentdate if from the future
        Date pdate = toDate(paymentDate);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date tomorrow = calendar.getTime();

        if (pdate.compareTo(tomorrow) >= 0) {
          info.addResult("WARNING",
              Utility.messageBD(this, "SCO_PaymentFromFutureWarning", info.vars.getLanguage()));
        }
      }

    } else if ("inpamount".equals(lastFieldChanged)
        || "inpgeneratedCredit".equals(lastFieldChanged)) {
      FIN_FinancialAccount financialAccount = OBDal.getInstance().get(FIN_FinancialAccount.class,
          financialAccountId);
      if (!strConvertRate.isEmpty() && !strAmount.isEmpty() && financialAccount != null) {
        BigDecimal convertRate = new BigDecimal(strConvertRate);
        BigDecimal amount = new BigDecimal(strAmount);
        BigDecimal converted = amount.divide(convertRate,
            financialAccount.getCurrency().getStandardPrecision().intValue(), RoundingMode.HALF_UP);
        info.addResult("inpfinaccTxnAmount", amount);
      }
    } else if ("inpfinaccTxnConvertRate".equals(lastFieldChanged)) {
      FIN_FinancialAccount financialAccount = OBDal.getInstance().get(FIN_FinancialAccount.class,
          financialAccountId);

      if (!strConvertRate.isEmpty() && financialAccount != null) {
        BigDecimal altconvrate = SCO_Utils.calculateAltConvRate(
            financialAccount.getCurrency().getId(), currencyId, new BigDecimal(strConvertRate));
        info.addResult("inpemScoAltConvertRate", altconvrate);
      } else {
        info.addResult("inpemScoAltConvertRate", "");
      }

      if (!strConvertRate.isEmpty() && !strAmount.isEmpty() && financialAccount != null) {
        BigDecimal convertRate = new BigDecimal(strConvertRate);
        BigDecimal amount = new BigDecimal(strAmount);
        BigDecimal converted = amount.divide(convertRate,
            financialAccount.getCurrency().getStandardPrecision().intValue(), RoundingMode.HALF_UP);
        info.addResult("inpfinaccTxnAmount", amount);
      }
    } else if ("inpemScoAltConvertRate".equals(lastFieldChanged)) {
      BigDecimal convrate = null;
      FIN_FinancialAccount financialAccount = OBDal.getInstance().get(FIN_FinancialAccount.class,
          financialAccountId);

      if (!strAltConvertRate.isEmpty() && financialAccount != null) {
        convrate = SCO_Utils.calculateNormalConvRate(financialAccount.getCurrency().getId(),
            currencyId, new BigDecimal(strAltConvertRate));
        info.addResult("inpfinaccTxnConvertRate", convrate);
      } else {
        info.addResult("inpfinaccTxnConvertRate", "");
      }

      if (convrate != null && !strAmount.isEmpty() && financialAccount != null) {
        BigDecimal amount = new BigDecimal(strAmount);
        BigDecimal converted = amount.divide(convrate,
            financialAccount.getCurrency().getStandardPrecision().intValue(), RoundingMode.HALF_UP);
        info.addResult("inpfinaccTxnAmount", amount);
      }
    }
    /*
     * else if ("inpfinaccTxnAmount".equals(lastFieldChanged)) { if (!strFinaccTxnAmount.isEmpty()
     * && !strAmount.isEmpty()) { BigDecimal amount = new BigDecimal(strAmount); if
     * (amount.compareTo(BigDecimal.ZERO) != 0) { BigDecimal finaccTxnAmount = new
     * BigDecimal(strFinaccTxnAmount); BigDecimal convertRate = finaccTxnAmount.multiply(amount, new
     * MathContext(16)); info.addResult("inpfinaccTxnConvertRate", convertRate);
     * 
     * FIN_FinancialAccount financialAccount = OBDal.getInstance().get( FIN_FinancialAccount.class,
     * financialAccountId); if (convertRate != null && financialAccount != null) { BigDecimal
     * altconvrate = SCO_Utils.calculateAltConvRate(financialAccount.getCurrency() .getId(),
     * currencyId, convertRate); info.addResult("inpemScoAltConvertRate", altconvrate); } else {
     * info.addResult("inpemScoAltConvertRate", ""); } } } }
     */else {
      log4j.error(
          "SE_Payment_MultiCurrency. The following field executed the callout" + lastFieldChanged);
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