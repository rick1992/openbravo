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
 * All portions are Copyright (C) 2013 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.ad_process.assets;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.accounting.Costcenter;
import org.openbravo.model.financialmgmt.accounting.UserDimension1;
import org.openbravo.model.financialmgmt.accounting.UserDimension2;
import org.openbravo.model.financialmgmt.assetmgmt.Amortization;
import org.openbravo.model.financialmgmt.assetmgmt.AmortizationLine;
import org.openbravo.model.financialmgmt.assetmgmt.Asset;
import org.openbravo.model.marketing.Campaign;
import org.openbravo.model.materialmgmt.cost.ABCActivity;
import org.openbravo.model.project.Project;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.openbravo.service.db.DbUtility;

import pe.com.unifiedgo.accounting.data.SCOAssetImprov;
import pe.com.unifiedgo.accounting.data.SCOAssetReeval;
import pe.com.unifiedgo.accounting.data.SCOAssetimpAmortli;
import pe.com.unifiedgo.accounting.data.SCOAssetreevAmortli;

public class AssetLinearDepreciationMethodProcess extends DalBaseProcess {
  private static final Logger log4j = Logger.getLogger(AssetLinearDepreciationMethodProcess.class);
  private static final String LINEAR = "LI";
  private static final String TIME = "TI";
  private static final String PERCENTAGE = "PE";
  private static final String MONTH = "MO";
  private static final String YEAR = "YE";
  private static final BigDecimal HUNDRED = new BigDecimal("100");
  private static final BigDecimal THIRTY = new BigDecimal("30");
  private static final BigDecimal YEARDAYS = new BigDecimal("365");
  private static final MathContext mc = new MathContext(32, RoundingMode.HALF_UP);

  @Override
  public void doExecute(ProcessBundle bundle) throws Exception {
    OBError msg = new OBError();
    try {
      final String strAssetId = (String) bundle.getParams().get("A_Asset_ID");
      final Asset asset = (Asset) OBDal.getInstance().getProxy(Asset.ENTITY_NAME, strAssetId);
      msg = generateAmortizationPlan(asset);
    } catch (final Exception e) {
      OBDal.getInstance().rollbackAndClose();
      log4j.error("Exception found in AssetLinearDepreciationMethodProcess process: ", e);

      Throwable ex = DbUtility.getUnderlyingSQLException(e);
      String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();
      msg.setType("Error");
      msg.setTitle(OBMessageUtils.messageBD("Error"));
      msg.setMessage(message);

    } finally {
      bundle.setResult(msg);
      if (msg.getType().equals("Error")) {
        OBDal.getInstance().rollbackAndClose();
      }
    }
  }

  public OBError generateAmortizationPlan(Asset asset) throws Exception {
    OBError msg = new OBError();
    msg.setType("Success");
    msg.setTitle(OBMessageUtils.messageBD("Success"));

    // =========== Read asset properties ===========
    Date startDate = asset.getDepreciationStartDate();
    if (startDate == null) {
      msg.setType("Error");
      msg.setTitle(OBMessageUtils.messageBD("Error"));
      msg.setMessage(OBMessageUtils.messageBD("ASSET_MANDATORY_STARTDATE"));
      return msg;
    }

    log4j.debug("A_ASSET_POST: " + asset.getName() + " - " + asset.getDocumentNo());

    Calendar calStart = Calendar.getInstance();
    calStart.setTime(startDate);
    calStart.set(Calendar.HOUR, 0);
    calStart.set(Calendar.MINUTE, 0);
    calStart.set(Calendar.SECOND, 0);
    startDate = calStart.getTime();

    // End date is calculated based on depreciation type
    Calendar calRealEnd = Calendar.getInstance();
    calRealEnd.setTime(startDate);
    String depreciationType = asset.getDepreciationType(); // Linear
    String calculateType = asset.getCalculateType(); // Time or Percentage
    String amortizationFrequency = asset.getAmortize(); // Monthly or Yearly
    BigDecimal uselifeMonths = asset.getUsableLifeMonths() == null ? BigDecimal.ZERO : new BigDecimal(asset.getUsableLifeMonths());
    boolean is30DayMonth = asset.isEveryMonthIs30Days();
    boolean is365DayYear = asset.isEveryMonthIs30Days();
    BigDecimal uselifeYears = asset.getUsableLifeYears() == null ? BigDecimal.ZERO : new BigDecimal(asset.getUsableLifeYears());
    BigDecimal annualDepreciation = asset.getAnnualDepreciation() == null ? BigDecimal.ZERO : asset.getAnnualDepreciation();
    BigDecimal depreciationAmount = asset.getDepreciationAmt() == null ? BigDecimal.ZERO : asset.getDepreciationAmt();
    BigDecimal previouslyDepreciatedAmount = asset.getPreviouslyDepreciatedAmt() == null ? BigDecimal.ZERO : asset.getPreviouslyDepreciatedAmt();

    BigDecimal totalPeriods = BigDecimal.ZERO; // Total amortization lines (records in the tab)
    BigDecimal amountPerPeriod = BigDecimal.ZERO; // Amount to depreciate per period (month or year)

    List<AmortizationLine> amortizationLineList = asset.getFinancialMgmtAmortizationLineList();
    BigDecimal alreadyAmortizedPeriods = new BigDecimal(amortizationLineList.size());
    // When recalculating the amortization the asset can be partially amortized
    BigDecimal alreadyAmortizedAmount = BigDecimal.ZERO;
    Calendar maxAmortizationDateCal = (Calendar) calStart.clone();
    for (AmortizationLine al : amortizationLineList) {
      // GROSS CHANGE
      // alreadyAmortizedAmount = alreadyAmortizedAmount.add(al.getAmortizationAmount());
      alreadyAmortizedAmount = alreadyAmortizedAmount.add(al.getScoGrossamortizationamt());
      Date amortizationEndDate = al.getAmortization().getEndingDate();
      if (amortizationEndDate.compareTo(maxAmortizationDateCal.getTime()) > 0) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(amortizationEndDate);
        // Add one because you want to new the next day
        cal.add(Calendar.DATE, 1);
        maxAmortizationDateCal = cal;
      }
    }

    // Total amount to be depreciated (including already depreciated amount)
    BigDecimal amount = depreciationAmount.subtract(previouslyDepreciatedAmount);
    // Pending amount to be depreciated
    BigDecimal pendingAmountToAmortize = amount.subtract(alreadyAmortizedAmount);
    BigDecimal totalDays = BigDecimal.ZERO;

    boolean isMonthly = false;
    boolean isYearly = false;
    boolean isPercentage = false;

    // =========== Validations ===========
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      msg.setType("Error");
      msg.setTitle(OBMessageUtils.messageBD("Error"));
      msg.setMessage(String.format(OBMessageUtils.messageBD("ASSET_MANDATORY_POSSITIVE"), OBMessageUtils.messageBD("ASSET_DEPRECIATION_AMOUNT")));
      return msg;
    }
    if (pendingAmountToAmortize.compareTo(BigDecimal.ZERO) <= 0) {
      msg.setType("Warning");
      msg.setTitle("");
      msg.setMessage(OBMessageUtils.messageBD("ASSET_FULLY_DEPRECIATED"));
      return msg;
    }
    if (LINEAR.equals(depreciationType) && PERCENTAGE.equals(calculateType) && annualDepreciation.compareTo(BigDecimal.ZERO) <= 0) {
      msg.setType("Error");
      msg.setTitle(OBMessageUtils.messageBD("Error"));
      msg.setMessage(String.format(OBMessageUtils.messageBD("ASSET_MANDATORY_POSSITIVE"), OBMessageUtils.messageBD("ASSET_ANNUAL_DEPRECIATION")));
      return msg;
    }
    if (TIME.equals(calculateType) && MONTH.equals(amortizationFrequency) && uselifeMonths.compareTo(BigDecimal.ZERO) <= 0) {
      msg.setType("Error");
      msg.setTitle(OBMessageUtils.messageBD("Error"));
      msg.setMessage(String.format(OBMessageUtils.messageBD("ASSET_MANDATORY_POSSITIVE"), OBMessageUtils.messageBD("ASSET_USABLE_LIFE_MONTHS")));
      return msg;
    }
    if (TIME.equals(calculateType) && YEAR.equals(amortizationFrequency) && uselifeYears.compareTo(BigDecimal.ZERO) <= 0) {
      msg.setType("Error");
      msg.setTitle(OBMessageUtils.messageBD("Error"));
      msg.setMessage(String.format(OBMessageUtils.messageBD("ASSET_MANDATORY_POSSITIVE"), OBMessageUtils.messageBD("ASSET_USABLE_LIFE_YEARS")));
      return msg;
    }
    if (asset.getCurrency() == null) {
      msg.setType("Error");
      msg.setTitle(OBMessageUtils.messageBD("Error"));
      msg.setMessage(OBMessageUtils.messageBD("ASSET_CURRENCY_MANDATORY"));
      return msg;
    }
    int stdPrecision = 2;
    try {
      OBContext.setAdminMode(true);
      stdPrecision = asset.getCurrency().getStandardPrecision().intValue();
    } finally {
      OBContext.restorePreviousMode();
    }

    int amtlinescreated = 0;
    // =========== Calculate amount to amortize per period ===========
    if (LINEAR.equals(depreciationType)) {
      if (PERCENTAGE.equals(calculateType)) {
        isPercentage = true;
        totalPeriods = HUNDRED.divide(annualDepreciation, RoundingMode.CEILING);
        // If the depreciation start date is not the first day of the period increment by 1 the
        // number of periods
        totalPeriods = (calStart.get(Calendar.DAY_OF_MONTH) == 1 && calStart.get(Calendar.MONTH) == 0) ? totalPeriods : totalPeriods.add(BigDecimal.ONE);
        amountPerPeriod = amount.multiply(annualDepreciation, mc).divide(HUNDRED, mc);

      } else if (TIME.equals(calculateType)) {
        if (MONTH.equals(amortizationFrequency)) {
          // Calculate real end date
          // From: 01/01/2013, 2 months, calRealdEnd = 01/03/2012
          calRealEnd.add(Calendar.MONTH, uselifeMonths.intValue());
          isMonthly = true;
          // If the depreciation start date is not the first day of the period increment by 1 the
          // number of periods
          totalPeriods = (calStart.get(Calendar.DAY_OF_MONTH) == 1) ? uselifeMonths : uselifeMonths.add(BigDecimal.ONE);

          if (is30DayMonth) {
            // Calculate total days proportional to months of 30 days
            if (alreadyAmortizedPeriods.compareTo(BigDecimal.ZERO) > 0) {
              // totalDays = calculateNumberOfDaysIn30DayMonths(maxAmortizationDateCal, calRealEnd);
              totalDays = getDaysBetweenProportionalPeriods(maxAmortizationDateCal, calRealEnd, isYearly, isMonthly);
            } else {
              totalDays = uselifeMonths.multiply(THIRTY, mc);
            }
          } else {
            // Calculate total natural days
            totalDays = new BigDecimal(FIN_Utility.getDaysBetween(maxAmortizationDateCal.getTime(), calRealEnd.getTime()));
          }

          if (totalDays.compareTo(BigDecimal.ZERO) == 0) {
            return msg;
          }

          // From: 01/01/2013, 2 months, calRealdEnd = 01/03/2012
          // Decrease by one. Last day should be the last day (included) of the range (28/02/2013)
          calRealEnd.add(Calendar.DATE, -1);

        } else if (YEAR.equals(amortizationFrequency)) {
          // Calculate real end date
          // From: 01/01/2013, 1 year, calRealdEnd = 01/01/2014
          calRealEnd.add(Calendar.YEAR, uselifeYears.intValue());
          isYearly = true;
          // If the depreciation start date is not the first day of the period increment by 1 the
          // number of periods
          totalPeriods = (calStart.get(Calendar.DAY_OF_MONTH) == 1 && calStart.get(Calendar.MONTH) == 0) ? uselifeYears : uselifeYears.add(BigDecimal.ONE);
          amountPerPeriod = amount.divide(uselifeYears, mc);

          if (is365DayYear) {
            // Calculate total days proportional to years of 365 days
            if (alreadyAmortizedPeriods.compareTo(BigDecimal.ZERO) > 0) {
              totalDays = getDaysBetweenProportionalPeriods(maxAmortizationDateCal, calRealEnd, isYearly, isMonthly);
            } else {
              totalDays = uselifeYears.multiply(YEARDAYS, mc);
            }
          } else {
            // Calculate total natural days
            totalDays = new BigDecimal(FIN_Utility.getDaysBetween(maxAmortizationDateCal.getTime(), calRealEnd.getTime()));
          }

          if (totalDays.compareTo(BigDecimal.ZERO) == 0) {
            return msg;
          }

          // Decrease by one. Last day should be the last day (included) of the range (31/12/2013)
          // From: 01/01/2013, 1 year, calRealdEnd = 01/01/2014
          calRealEnd.add(Calendar.DATE, -1);

        } else {
          msg.setType("Error");
          msg.setTitle(OBMessageUtils.messageBD("Error"));
          msg.setMessage("Unsupported amortization frequency: " + amortizationFrequency);
          return msg;
        }
      } else {
        msg.setType("Error");
        msg.setTitle(OBMessageUtils.messageBD("Error"));
        msg.setMessage("Unsupported amortization calculation type: " + calculateType);
        return msg;
      }
    }

    // Calendar auxiliary variables
    Calendar calAux = Calendar.getInstance();
    calAux.setTime(startDate);
    // First and last period (month or year) day. For example, 2 month, starting on 17/01/12.
    // period 1: calFirstDayOfPeriod = 01/01/12, calLastDayOfPeriod = 31/01/12.
    // period 2: calFirstDayOfPeriod = 01/02/12, calLastDayOfPeriod = 29/02/12.
    // period 3: calFirstDayOfPeriod = 01/03/12, calLastDayOfPeriod = 31/03/12.
    Calendar calLastDayOfPeriod = Calendar.getInstance(); // Last period (month or year) day.
    Calendar calFirstDayOfPeriod = (Calendar) calStart.clone(); // First period (month or year) day.
    calFirstDayOfPeriod.set(Calendar.DAY_OF_MONTH, 1);
    if (YEAR.equals(amortizationFrequency)) {
      calFirstDayOfPeriod.set(Calendar.MONTH, 0);
    }

    int currentYear = calStart.get(Calendar.YEAR);
    int currentMonth = calStart.get(Calendar.MONTH);
    int currentMonthDay = calStart.get(Calendar.DAY_OF_MONTH);
    int currentYearDay = calAux.get(Calendar.DAY_OF_YEAR);
    int lastDayOfMonth = calStart.getActualMaximum(Calendar.DAY_OF_MONTH);
    int lastDayOfYear = calStart.getActualMaximum(Calendar.DAY_OF_YEAR);

    BigDecimal totalizedAmount = BigDecimal.ZERO;
    BigDecimal totalizedPercentage = BigDecimal.ZERO;
    BigDecimal proportionalAmount = BigDecimal.ZERO;
    BigDecimal contDays = BigDecimal.ZERO;
    int contPeriods = 0;
    boolean endOfRange = false;
    int maxDaysOfRange = 0;
    Long seqNoAsset = null;

    // Loop till full amount is depreciated
    for (; !calAux.after(calRealEnd) || (isPercentage && totalizedAmount.compareTo(amount) < 0); calAux.add(Calendar.DATE, 1)) {
      contDays = contDays.add(BigDecimal.ONE);
      if (currentYear != calAux.get(Calendar.YEAR)) {
        currentYear = calAux.get(Calendar.YEAR);
        lastDayOfYear = calAux.getActualMaximum(Calendar.DAY_OF_YEAR);
      }
      if (currentMonth != calAux.get(Calendar.MONTH)) {
        currentMonth = calAux.get(Calendar.MONTH);
        lastDayOfMonth = calAux.getActualMaximum(Calendar.DAY_OF_MONTH);
      }

      currentMonthDay = calAux.get(Calendar.DAY_OF_MONTH);
      currentYearDay = calAux.get(Calendar.DAY_OF_YEAR);

      // Month
      if (isMonthly && currentMonthDay == lastDayOfMonth) {
        endOfRange = true;
        maxDaysOfRange = lastDayOfMonth;
      }
      // Percentage or Year
      if ((isPercentage || isYearly) && currentYearDay == lastDayOfYear) {
        endOfRange = true;
        maxDaysOfRange = lastDayOfYear;
      }

      // Last day of range (last iteration in the loop)
      if (!isPercentage && calAux.compareTo(calRealEnd) == 0) {
        endOfRange = true;
        // Month
        if (isMonthly) {
          maxDaysOfRange = lastDayOfMonth;
        }
        // Percentage or Year
        if (isPercentage || isYearly) {
          maxDaysOfRange = lastDayOfYear;
        }
      }

      if (endOfRange) {
        contPeriods += 1;
        calLastDayOfPeriod = (Calendar) calAux.clone();
        calLastDayOfPeriod.set(Calendar.DAY_OF_MONTH, calAux.getActualMaximum(Calendar.DAY_OF_MONTH));
        if (isYearly) {
          calLastDayOfPeriod.set(Calendar.MONTH, calAux.getActualMaximum(Calendar.MONTH));
        }

        AmortizationLine amortizationLine = getAmortizationLine(asset, null, calLastDayOfPeriod.getTime());

        if (amortizationLine != null) {
          // Recalculate percentage because the amount could have been changed
          // GROSS CHANGE
          // BigDecimal proportionaldPercentage =
          // amortizationLine.getAmortizationAmount().multiply(HUNDRED).divide(amount, stdPrecision,
          // RoundingMode.HALF_UP);
          // totalizedAmount = totalizedAmount.add(amortizationLine.getAmortizationAmount());
          BigDecimal proportionaldPercentage = amortizationLine.getScoGrossamortizationamt().multiply(HUNDRED).divide(amount, stdPrecision, RoundingMode.HALF_UP);
          totalizedAmount = totalizedAmount.add(amortizationLine.getScoGrossamortizationamt());
          totalizedPercentage = totalizedPercentage.add(proportionaldPercentage);
          if (amortizationLine.getAmortizationPercentage() != null && amortizationLine.getAmortizationPercentage().compareTo(proportionaldPercentage) != 0) {
            Amortization amortization = amortizationLine.getAmortization();
            boolean isAmortizationProcessed = false;
            if (amortization != null) {
              isAmortizationProcessed = "Y".equals(amortization.getProcessed());
            }
            if (isAmortizationProcessed) {
              // Avoid a_amortizationline_trg trigger error
              amortization.setProcessed("N");
              OBDal.getInstance().save(amortization);
              OBDal.getInstance().flush();
            }
            amortizationLine.setAmortizationPercentage(proportionaldPercentage);
            if (isAmortizationProcessed) {
              amortization.setProcessed("Y");
              OBDal.getInstance().save(amortization);
            }
          }

          OBDal.getInstance().save(amortizationLine);

        } else {
          // Calculate the proportional amount for current period (amortization line)
          if (isPercentage) {
            proportionalAmount = contDays.multiply(amountPerPeriod, mc).divide(new BigDecimal(maxDaysOfRange), mc);
          } else {
            if (isMonthly && is30DayMonth) {
              contDays = contDays.multiply(THIRTY, mc).divide(new BigDecimal(maxDaysOfRange), mc);
            }
            if (isYearly && is365DayYear) {
              contDays = contDays.multiply(YEARDAYS, mc).divide(new BigDecimal(maxDaysOfRange), mc);
            }
            proportionalAmount = contDays.multiply(pendingAmountToAmortize, mc).divide(totalDays, mc);

          }

          // Calculate percentage
          BigDecimal proportionaldPercentage = proportionalAmount.multiply(HUNDRED).divide(amount, mc).setScale(stdPrecision, RoundingMode.HALF_UP);

          // Round the amount after using it for percentage calculation
          proportionalAmount = proportionalAmount.setScale(stdPrecision, RoundingMode.HALF_UP);

          // Last period. Adjust for avoiding rounding issues.
          if (((!isPercentage && new BigDecimal(contPeriods).compareTo(totalPeriods) == 0)) || totalizedAmount.add(proportionalAmount).compareTo(amount) > 0) {
            proportionalAmount = amount.subtract(totalizedAmount);
            proportionaldPercentage = HUNDRED.subtract(totalizedPercentage);
          }

          log4j.debug(OBDateUtils.formatDate(calFirstDayOfPeriod.getTime()) + " to " + OBDateUtils.formatDate(calLastDayOfPeriod.getTime()) + "  " + proportionalAmount + "  " + proportionaldPercentage);

          // Accumulate amount and percentage
          totalizedAmount = totalizedAmount.add(proportionalAmount);
          totalizedPercentage = totalizedPercentage.add(proportionaldPercentage);

          // Search for not processed amortization (calFirstDayOfPeriod - calLastDayOfPeriod)
          Amortization amortization = getAmortization(asset.getOrganization(), null, calLastDayOfPeriod.getTime(), asset.getProject());
          if (amortization == null) {
            amortization = createNewAmortization(asset.getOrganization(), OBDateUtils.formatDate(calLastDayOfPeriod.getTime()), null /* description */, calFirstDayOfPeriod.getTime(), calLastDayOfPeriod.getTime(), asset.getCurrency(), asset.getProject(), null /* campaign */, null /* activity */, null /* user1 */, null /* user2 */);
          }

          // Calculate asset sequence number.
          // Asset 1
          // January lineno = 10, seqnoasset = 10
          // February lineno = 10, seqnoasset = 20
          if (seqNoAsset == null) {
            seqNoAsset = getMaxSeqNoAsset(asset) + 10L;
          }

          // Calculate amortization line number because the amortization can already exists.
          Long lineNo = getMaxLineNo(amortization) + 10L;

          // Create the amortization line
          amortizationLine = createNewAmortizationLine(amortization, lineNo, seqNoAsset, asset, proportionaldPercentage, proportionalAmount, proportionalAmount, asset.getCurrency(), asset.getProject(), null /* campaign */, null /* activity */, null /* user1 */, null /* user2 */, null /* costcenter */);
          amtlinescreated++;
          seqNoAsset += 10L;
        }

        // Initialize new range
        contDays = BigDecimal.ZERO;
        calFirstDayOfPeriod = (Calendar) calAux.clone();
        calFirstDayOfPeriod.add(Calendar.DAY_OF_MONTH, 1);
        endOfRange = false;
      }
    }

    asset.setDepreciatedPlan(amount);
    OBDal.getInstance().save(asset);
    OBDal.getInstance().flush();
    OBDal.getInstance().refresh(asset);

    Date assetstartDate = asset.getDepreciationStartDate();
    Calendar asset_calStart = Calendar.getInstance();
    asset_calStart.setTime(startDate);
    asset_calStart.set(Calendar.HOUR, 0);
    asset_calStart.set(Calendar.MINUTE, 0);
    asset_calStart.set(Calendar.SECOND, 0);

    // Process Assets Improvs
    List<SCOAssetImprov> improvs = asset.getSCOAssetImprovList();

    // Store asset amortization config
    BigDecimal asset_annualDepreciation = annualDepreciation;

    for (int i = 0; i < improvs.size(); i++) {

      // Restore amortization config
      annualDepreciation = asset_annualDepreciation;

      depreciationType = asset.getDepreciationType(); // Linear
      calculateType = asset.getCalculateType(); // Time or Percentage
      amortizationFrequency = asset.getAmortize(); // Monthly or Yearly

      SCOAssetImprov improv = improvs.get(i);

      Date improv_startDate = improv.getImprovdate();
      calStart = Calendar.getInstance();
      calStart.setTime(improv_startDate);
      calStart.set(Calendar.HOUR, 0);
      calStart.set(Calendar.MINUTE, 0);
      calStart.set(Calendar.SECOND, 0);

      annualDepreciation = BigDecimal.ZERO;
      BigDecimal asset_uselifeYears = null;
      if (PERCENTAGE.equals(calculateType)) {
        // transform to year type
        calculateType = TIME;
        amortizationFrequency = YEAR;
        asset_uselifeYears = HUNDRED.divide(asset_annualDepreciation, 0, RoundingMode.CEILING);

        if (calStart.get(Calendar.YEAR) == asset_calStart.get(Calendar.YEAR)) {
          // if the improvement starts at the same year of the asset consider the same startdate
          calStart = asset_calStart;
        } else {
          // always asume fist day of year
          calStart.set(Calendar.MONTH, 0);
          calStart.set(Calendar.DAY_OF_MONTH, 1);
        }
      } else if (TIME.equals(calculateType)) {
        if (MONTH.equals(amortizationFrequency)) {
          if (calStart.get(Calendar.MONTH) == asset_calStart.get(Calendar.MONTH) && calStart.get(Calendar.YEAR) == asset_calStart.get(Calendar.YEAR)) {
            // if the improvement starts at the same month of the asset consider the same startdate
            calStart = asset_calStart;
          } else {
            // always asume fist day of month
            calStart.set(Calendar.DAY_OF_MONTH, 1);
          }
        } else if (YEAR.equals(amortizationFrequency)) {
          if (calStart.get(Calendar.YEAR) == asset_calStart.get(Calendar.YEAR)) {
            // if the improvement starts at the same year of the asset consider the same startdate
            calStart = asset_calStart;
          } else {
            // always asume fist day of year
            calStart.set(Calendar.MONTH, 0);
            calStart.set(Calendar.DAY_OF_MONTH, 1);
          }
        }
      }

      startDate = calStart.getTime();

      calRealEnd = Calendar.getInstance();
      calRealEnd.setTime(startDate);

      // TO CHANGE TO REAL ONES
      int diffYear = calStart.get(Calendar.YEAR) - asset_calStart.get(Calendar.YEAR);
      int diffMonth = diffYear * 12 + calStart.get(Calendar.MONTH) - asset_calStart.get(Calendar.MONTH);

      uselifeMonths = asset.getUsableLifeMonths() == null ? BigDecimal.ZERO : new BigDecimal(asset.getUsableLifeMonths()).subtract(new BigDecimal(diffMonth));

      if (asset_uselifeYears != null) {
        uselifeYears = asset_uselifeYears.subtract(new BigDecimal(diffYear));
      } else {
        uselifeYears = asset.getUsableLifeYears() == null ? BigDecimal.ZERO : new BigDecimal(asset.getUsableLifeYears()).subtract(new BigDecimal(diffYear));
      }

      is30DayMonth = asset.isEveryMonthIs30Days();
      is365DayYear = asset.isEveryMonthIs30Days();

      depreciationAmount = improv.getAmount();
      previouslyDepreciatedAmount = BigDecimal.ZERO;

      totalPeriods = BigDecimal.ZERO; // Total amortization lines (records in the tab)
      amountPerPeriod = BigDecimal.ZERO; // Amount to depreciate per period (month or year)

      List<SCOAssetimpAmortli> improvamortizationlineList = improv.getSCOAssetimpAmortliList();
      alreadyAmortizedPeriods = new BigDecimal(improvamortizationlineList.size());

      // When recalculating the amortization the asset improv can be partially amortized
      alreadyAmortizedAmount = BigDecimal.ZERO;
      maxAmortizationDateCal = (Calendar) calStart.clone();
      for (SCOAssetimpAmortli ial : improvamortizationlineList) {
        alreadyAmortizedAmount = alreadyAmortizedAmount.add(ial.getAmortizationAmount());
        Date amortizationEndDate = ial.getAmortizationline().getAmortization().getEndingDate();
        if (amortizationEndDate.compareTo(maxAmortizationDateCal.getTime()) > 0) {
          Calendar cal = Calendar.getInstance();
          cal.setTime(amortizationEndDate);
          // Add one because you want to new the next day
          cal.add(Calendar.DATE, 1);
          maxAmortizationDateCal = cal;
        }
      }

      // Total amount to be depreciated (including already depreciated amount)
      amount = depreciationAmount.subtract(previouslyDepreciatedAmount);
      // Pending amount to be depreciated
      pendingAmountToAmortize = amount.subtract(alreadyAmortizedAmount);
      totalDays = BigDecimal.ZERO;

      isMonthly = false;
      isYearly = false;
      isPercentage = false;

      // =========== Validations ===========
      if (amount.compareTo(BigDecimal.ZERO) <= 0) {
        msg.setType("Error");
        msg.setTitle(OBMessageUtils.messageBD("Error"));
        msg.setMessage(improv.getName() + " - " + String.format(OBMessageUtils.messageBD("SCO_ASSET_MANDATORY_POSSITIVE_IMPROV"), OBMessageUtils.messageBD("ASSET_DEPRECIATION_AMOUNT")));
        return msg;
      }
      if (pendingAmountToAmortize.compareTo(BigDecimal.ZERO) <= 0) {
        msg.setType("Warning");
        msg.setTitle("");
        msg.setMessage(improv.getName() + " - " + OBMessageUtils.messageBD("SCO_ASSET_FULLY_DEPRECIATED_IMPROV"));
        return msg;
      }
      if (LINEAR.equals(depreciationType) && PERCENTAGE.equals(calculateType) && annualDepreciation.compareTo(BigDecimal.ZERO) <= 0) {
        msg.setType("Error");
        msg.setTitle(OBMessageUtils.messageBD("Error"));
        msg.setMessage(improv.getName() + " - " + String.format(OBMessageUtils.messageBD("SCO_ASSET_MANDATORY_POSSITIVE_IMPROV"), OBMessageUtils.messageBD("ASSET_ANNUAL_DEPRECIATION")));
        return msg;
      }
      if (TIME.equals(calculateType) && MONTH.equals(amortizationFrequency) && uselifeMonths.compareTo(BigDecimal.ZERO) <= 0) {
        msg.setType("Error");
        msg.setTitle(OBMessageUtils.messageBD("Error"));
        msg.setMessage(improv.getName() + " - " + String.format(OBMessageUtils.messageBD("ASSET_MANDATORY_POSSITIVE_IMPROV"), OBMessageUtils.messageBD("ASSET_USABLE_LIFE_MONTHS")));
        return msg;
      }
      if (TIME.equals(calculateType) && YEAR.equals(amortizationFrequency) && uselifeYears.compareTo(BigDecimal.ZERO) <= 0) {
        msg.setType("Error");
        msg.setTitle(OBMessageUtils.messageBD("Error"));
        msg.setMessage(improv.getName() + " - " + String.format(OBMessageUtils.messageBD("ASSET_MANDATORY_POSSITIVE_IMPROV"), OBMessageUtils.messageBD("ASSET_USABLE_LIFE_YEARS")));
        return msg;
      }

      stdPrecision = 2;
      try {
        OBContext.setAdminMode(true);
        stdPrecision = asset.getCurrency().getStandardPrecision().intValue();
      } finally {
        OBContext.restorePreviousMode();
      }

      // =========== Calculate amount to amortize per period ===========
      if (LINEAR.equals(depreciationType)) {
        if (PERCENTAGE.equals(calculateType)) {
          isPercentage = true;
          totalPeriods = HUNDRED.divide(annualDepreciation, RoundingMode.CEILING);
          // If the depreciation start date is not the first day of the period increment by 1 the
          // number of periods
          totalPeriods = (calStart.get(Calendar.DAY_OF_MONTH) == 1 && calStart.get(Calendar.MONTH) == 0) ? totalPeriods : totalPeriods.add(BigDecimal.ONE);
          amountPerPeriod = amount.multiply(annualDepreciation, mc).divide(HUNDRED, mc);

        } else if (TIME.equals(calculateType)) {
          if (MONTH.equals(amortizationFrequency)) {
            // Calculate real end date
            // From: 01/01/2013, 2 months, calRealdEnd = 01/03/2012
            calRealEnd.add(Calendar.MONTH, uselifeMonths.intValue());
            isMonthly = true;
            // If the depreciation start date is not the first day of the period increment by 1 the
            // number of periods
            totalPeriods = (calStart.get(Calendar.DAY_OF_MONTH) == 1) ? uselifeMonths : uselifeMonths.add(BigDecimal.ONE);

            if (is30DayMonth) {
              // Calculate total days proportional to months of 30 days
              if (alreadyAmortizedPeriods.compareTo(BigDecimal.ZERO) > 0) {
                // totalDays = calculateNumberOfDaysIn30DayMonths(maxAmortizationDateCal,
                // calRealEnd);
                totalDays = getDaysBetweenProportionalPeriods(maxAmortizationDateCal, calRealEnd, isYearly, isMonthly);
              } else {
                totalDays = uselifeMonths.multiply(THIRTY, mc);
              }
            } else {
              // Calculate total natural days
              totalDays = new BigDecimal(FIN_Utility.getDaysBetween(maxAmortizationDateCal.getTime(), calRealEnd.getTime()));
            }

            if (totalDays.compareTo(BigDecimal.ZERO) == 0) {
              return msg;
            }

            // From: 01/01/2013, 2 months, calRealdEnd = 01/03/2012
            // Decrease by one. Last day should be the last day (included) of the range (28/02/2013)
            calRealEnd.add(Calendar.DATE, -1);

          } else if (YEAR.equals(amortizationFrequency)) {
            // Calculate real end date
            // From: 01/01/2013, 1 year, calRealdEnd = 01/01/2014
            calRealEnd.add(Calendar.YEAR, uselifeYears.intValue());
            isYearly = true;
            // If the depreciation start date is not the first day of the period increment by 1 the
            // number of periods
            totalPeriods = (calStart.get(Calendar.DAY_OF_MONTH) == 1 && calStart.get(Calendar.MONTH) == 0) ? uselifeYears : uselifeYears.add(BigDecimal.ONE);
            amountPerPeriod = amount.divide(uselifeYears, mc);

            if (is365DayYear) {
              // Calculate total days proportional to years of 365 days
              if (alreadyAmortizedPeriods.compareTo(BigDecimal.ZERO) > 0) {
                totalDays = getDaysBetweenProportionalPeriods(maxAmortizationDateCal, calRealEnd, isYearly, isMonthly);
              } else {
                totalDays = uselifeYears.multiply(YEARDAYS, mc);
              }
            } else {
              // Calculate total natural days
              totalDays = new BigDecimal(FIN_Utility.getDaysBetween(maxAmortizationDateCal.getTime(), calRealEnd.getTime()));
            }

            if (totalDays.compareTo(BigDecimal.ZERO) == 0) {
              return msg;
            }

            // Decrease by one. Last day should be the last day (included) of the range (31/12/2013)
            // From: 01/01/2013, 1 year, calRealdEnd = 01/01/2014
            calRealEnd.add(Calendar.DATE, -1);

          } else {
            msg.setType("Error");
            msg.setTitle(OBMessageUtils.messageBD("Error"));
            msg.setMessage("Unsupported amortization frequency improv: " + amortizationFrequency);
            return msg;
          }
        } else {
          msg.setType("Error");
          msg.setTitle(OBMessageUtils.messageBD("Error"));
          msg.setMessage("Unsupported amortization calculation type improv: " + calculateType);
          return msg;
        }
      }

      // Calendar auxiliary variables
      calAux = Calendar.getInstance();
      calAux.setTime(startDate);
      // First and last period (month or year) day. For example, 2 month, starting on 17/01/12.
      // period 1: calFirstDayOfPeriod = 01/01/12, calLastDayOfPeriod = 31/01/12.
      // period 2: calFirstDayOfPeriod = 01/02/12, calLastDayOfPeriod = 29/02/12.
      // period 3: calFirstDayOfPeriod = 01/03/12, calLastDayOfPeriod = 31/03/12.
      calLastDayOfPeriod = Calendar.getInstance(); // Last period (month or year) day.
      calFirstDayOfPeriod = (Calendar) calStart.clone(); // First period (month or year) day.
      calFirstDayOfPeriod.set(Calendar.DAY_OF_MONTH, 1);
      if (YEAR.equals(amortizationFrequency)) {
        calFirstDayOfPeriod.set(Calendar.MONTH, 0);
      }

      currentYear = calStart.get(Calendar.YEAR);
      currentMonth = calStart.get(Calendar.MONTH);
      currentMonthDay = calStart.get(Calendar.DAY_OF_MONTH);
      currentYearDay = calAux.get(Calendar.DAY_OF_YEAR);
      lastDayOfMonth = calStart.getActualMaximum(Calendar.DAY_OF_MONTH);
      lastDayOfYear = calStart.getActualMaximum(Calendar.DAY_OF_YEAR);

      totalizedAmount = BigDecimal.ZERO;
      totalizedPercentage = BigDecimal.ZERO;
      proportionalAmount = BigDecimal.ZERO;
      contDays = BigDecimal.ZERO;
      contPeriods = 0;
      endOfRange = false;
      maxDaysOfRange = 0;
      seqNoAsset = null;

      // Loop till full amount is depreciated
      // Calculate amortization line number
      Long lineNo = getSCOAssetImprovLiMaxLineNo(improv) + 10L;
      for (; !calAux.after(calRealEnd) || (isPercentage && totalizedAmount.compareTo(amount) < 0); calAux.add(Calendar.DATE, 1)) {
        contDays = contDays.add(BigDecimal.ONE);
        if (currentYear != calAux.get(Calendar.YEAR)) {
          currentYear = calAux.get(Calendar.YEAR);
          lastDayOfYear = calAux.getActualMaximum(Calendar.DAY_OF_YEAR);
        }
        if (currentMonth != calAux.get(Calendar.MONTH)) {
          currentMonth = calAux.get(Calendar.MONTH);
          lastDayOfMonth = calAux.getActualMaximum(Calendar.DAY_OF_MONTH);
        }

        currentMonthDay = calAux.get(Calendar.DAY_OF_MONTH);
        currentYearDay = calAux.get(Calendar.DAY_OF_YEAR);

        // Month
        if (isMonthly && currentMonthDay == lastDayOfMonth) {
          endOfRange = true;
          maxDaysOfRange = lastDayOfMonth;
        }
        // Percentage or Year
        if ((isPercentage || isYearly) && currentYearDay == lastDayOfYear) {
          endOfRange = true;
          maxDaysOfRange = lastDayOfYear;
        }

        // Last day of range (last iteration in the loop)
        if (!isPercentage && calAux.compareTo(calRealEnd) == 0) {
          endOfRange = true;
          // Month
          if (isMonthly) {
            maxDaysOfRange = lastDayOfMonth;
          }
          // Percentage or Year
          if (isPercentage || isYearly) {
            maxDaysOfRange = lastDayOfYear;
          }
        }

        if (endOfRange) {
          contPeriods += 1;
          calLastDayOfPeriod = (Calendar) calAux.clone();
          calLastDayOfPeriod.set(Calendar.DAY_OF_MONTH, calAux.getActualMaximum(Calendar.DAY_OF_MONTH));
          if (isYearly) {
            calLastDayOfPeriod.set(Calendar.MONTH, calAux.getActualMaximum(Calendar.MONTH));
          }

          SCOAssetimpAmortli improv_amortizationLine = getSCOAssetImprovLine(improv, null, calLastDayOfPeriod.getTime());

          if (improv_amortizationLine != null) {
            // Recalculate percentage because the amount could have been changed
            BigDecimal proportionaldPercentage = improv_amortizationLine.getAmortizationAmount().multiply(HUNDRED).divide(amount, stdPrecision, RoundingMode.HALF_UP);
            totalizedAmount = totalizedAmount.add(improv_amortizationLine.getAmortizationAmount());
            totalizedPercentage = totalizedPercentage.add(proportionaldPercentage);
            if (improv_amortizationLine.getAmortizationPercentage() != null && improv_amortizationLine.getAmortizationPercentage().compareTo(proportionaldPercentage) != 0) {
              improv_amortizationLine.setAmortizationPercentage(proportionaldPercentage);
            }

            OBDal.getInstance().save(improv_amortizationLine);

          } else {
            // Calculate the proportional amount for current period (improv amortization line)
            if (isPercentage) {
              proportionalAmount = contDays.multiply(amountPerPeriod, mc).divide(new BigDecimal(maxDaysOfRange), mc);
            } else {
              if (isMonthly && is30DayMonth) {
                contDays = contDays.multiply(THIRTY, mc).divide(new BigDecimal(maxDaysOfRange), mc);
              }
              if (isYearly && is365DayYear) {
                contDays = contDays.multiply(YEARDAYS, mc).divide(new BigDecimal(maxDaysOfRange), mc);
              }
              proportionalAmount = contDays.multiply(pendingAmountToAmortize, mc).divide(totalDays, mc);

            }

            // Calculate percentage
            BigDecimal proportionaldPercentage = proportionalAmount.multiply(HUNDRED).divide(amount, mc).setScale(stdPrecision, RoundingMode.HALF_UP);

            // Round the amount after using it for percentage calculation
            proportionalAmount = proportionalAmount.setScale(stdPrecision, RoundingMode.HALF_UP);

            // Last period. Adjust for avoiding rounding issues.
            if (((!isPercentage && new BigDecimal(contPeriods).compareTo(totalPeriods) == 0)) || totalizedAmount.add(proportionalAmount).compareTo(amount) > 0) {
              proportionalAmount = amount.subtract(totalizedAmount);
              proportionaldPercentage = HUNDRED.subtract(totalizedPercentage);
            }

            log4j.debug(OBDateUtils.formatDate(calFirstDayOfPeriod.getTime()) + " to " + OBDateUtils.formatDate(calLastDayOfPeriod.getTime()) + "  " + proportionalAmount + "  " + proportionaldPercentage);

            // Accumulate amount and percentage
            totalizedAmount = totalizedAmount.add(proportionalAmount);
            totalizedPercentage = totalizedPercentage.add(proportionaldPercentage);

            // Search for amortization line
            AmortizationLine amortizationline = getAmortizationLineForImprovLi(asset, null, calLastDayOfPeriod.getTime());
            if (amortizationline == null) {
              msg.setType("Error");
              msg.setTitle(OBMessageUtils.messageBD("Error"));
              msg.setMessage(String.format(OBMessageUtils.messageBD("SCO_AMORTIZATIONLINEFORIMPROVLINE_NOTFOUND") + ": " + improv.getName()));
              return msg;
            }

            // Create the asset improv amortization line
            improv_amortizationLine = createNewSCOAssetImpAmortli(improv, amortizationline, lineNo, proportionaldPercentage, proportionalAmount);
            lineNo = lineNo + 10L;
          }

          // Initialize new range
          contDays = BigDecimal.ZERO;
          calFirstDayOfPeriod = (Calendar) calAux.clone();
          calFirstDayOfPeriod.add(Calendar.DAY_OF_MONTH, 1);
          endOfRange = false;
        }
      }

      asset.setDepreciatedPlan(asset.getDepreciatedPlan().add(amount));
      OBDal.getInstance().save(asset);

    }
    
    
    
    // Process Assets Reevaluations
    List<SCOAssetReeval> reevals = asset.getSCOAssetReevalList();

    for (int i = 0; i < reevals.size(); i++) {

      // Restore amortization config
      annualDepreciation = asset_annualDepreciation;

      depreciationType = asset.getDepreciationType(); // Linear
      calculateType = asset.getCalculateType(); // Time or Percentage
      amortizationFrequency = asset.getAmortize(); // Monthly or Yearly

      SCOAssetReeval reeval = reevals.get(i);

      Date reeval_startDate = reeval.getReevaldate();
      calStart = Calendar.getInstance();
      calStart.setTime(reeval_startDate);
      calStart.set(Calendar.HOUR, 0);
      calStart.set(Calendar.MINUTE, 0);
      calStart.set(Calendar.SECOND, 0);

      annualDepreciation = BigDecimal.ZERO;
      BigDecimal asset_uselifeYears = null;
      if (PERCENTAGE.equals(calculateType)) {
        // transform to year type
        calculateType = TIME;
        amortizationFrequency = YEAR;
        asset_uselifeYears = HUNDRED.divide(asset_annualDepreciation, 0, RoundingMode.CEILING);

        if (calStart.get(Calendar.YEAR) == asset_calStart.get(Calendar.YEAR)) {
          // if the improvement starts at the same year of the asset consider the same startdate
          calStart = asset_calStart;
        } else {
          // always asume fist day of year
          calStart.set(Calendar.MONTH, 0);
          calStart.set(Calendar.DAY_OF_MONTH, 1);
        }
      } else if (TIME.equals(calculateType)) {
        if (MONTH.equals(amortizationFrequency)) {
          if (calStart.get(Calendar.MONTH) == asset_calStart.get(Calendar.MONTH) && calStart.get(Calendar.YEAR) == asset_calStart.get(Calendar.YEAR)) {
            // if the improvement starts at the same month of the asset consider the same startdate
            calStart = asset_calStart;
          } else {
            // always asume fist day of month
            calStart.set(Calendar.DAY_OF_MONTH, 1);
          }
        } else if (YEAR.equals(amortizationFrequency)) {
          if (calStart.get(Calendar.YEAR) == asset_calStart.get(Calendar.YEAR)) {
            // if the improvement starts at the same year of the asset consider the same startdate
            calStart = asset_calStart;
          } else {
            // always asume fist day of year
            calStart.set(Calendar.MONTH, 0);
            calStart.set(Calendar.DAY_OF_MONTH, 1);
          }
        }
      }

      startDate = calStart.getTime();

      calRealEnd = Calendar.getInstance();
      calRealEnd.setTime(startDate);

      // TO CHANGE TO REAL ONES
      int diffYear = calStart.get(Calendar.YEAR) - asset_calStart.get(Calendar.YEAR);
      int diffMonth = diffYear * 12 + calStart.get(Calendar.MONTH) - asset_calStart.get(Calendar.MONTH);

      uselifeMonths = asset.getUsableLifeMonths() == null ? BigDecimal.ZERO : new BigDecimal(asset.getUsableLifeMonths()).subtract(new BigDecimal(diffMonth));

      if (asset_uselifeYears != null) {
        uselifeYears = asset_uselifeYears.subtract(new BigDecimal(diffYear));
      } else {
        uselifeYears = asset.getUsableLifeYears() == null ? BigDecimal.ZERO : new BigDecimal(asset.getUsableLifeYears()).subtract(new BigDecimal(diffYear));
      }

      is30DayMonth = asset.isEveryMonthIs30Days();
      is365DayYear = asset.isEveryMonthIs30Days();

      depreciationAmount = reeval.getAmount();
      previouslyDepreciatedAmount = BigDecimal.ZERO;

      totalPeriods = BigDecimal.ZERO; // Total amortization lines (records in the tab)
      amountPerPeriod = BigDecimal.ZERO; // Amount to depreciate per period (month or year)

      List<SCOAssetreevAmortli> reevalamortizationlineList = reeval.getSCOAssetreevAmortliList();
      alreadyAmortizedPeriods = new BigDecimal(reevalamortizationlineList.size());

      // When recalculating the amortization the asset improv can be partially amortized
      alreadyAmortizedAmount = BigDecimal.ZERO;
      maxAmortizationDateCal = (Calendar) calStart.clone();
      for (SCOAssetreevAmortli ral : reevalamortizationlineList) {
        alreadyAmortizedAmount = alreadyAmortizedAmount.add(ral.getAmortizationAmount());
        Date amortizationEndDate = ral.getAmortizationline().getAmortization().getEndingDate();
        if (amortizationEndDate.compareTo(maxAmortizationDateCal.getTime()) > 0) {
          Calendar cal = Calendar.getInstance();
          cal.setTime(amortizationEndDate);
          // Add one because you want to new the next day
          cal.add(Calendar.DATE, 1);
          maxAmortizationDateCal = cal;
        }
      }

      // Total amount to be depreciated (including already depreciated amount)
      amount = depreciationAmount.subtract(previouslyDepreciatedAmount);
      // Pending amount to be depreciated
      pendingAmountToAmortize = amount.subtract(alreadyAmortizedAmount);
      totalDays = BigDecimal.ZERO;

      isMonthly = false;
      isYearly = false;
      isPercentage = false;

      // =========== Validations ===========
      if (amount.compareTo(BigDecimal.ZERO) <= 0) {
        msg.setType("Error");
        msg.setTitle(OBMessageUtils.messageBD("Error"));
        msg.setMessage(reeval.getName() + " - " + String.format(OBMessageUtils.messageBD("SCO_ASSET_MANDATORY_POSSITIVE_REEVAL"), OBMessageUtils.messageBD("ASSET_DEPRECIATION_AMOUNT")));
        return msg;
      }
      if (pendingAmountToAmortize.compareTo(BigDecimal.ZERO) <= 0) {
        msg.setType("Warning");
        msg.setTitle("");
        msg.setMessage(reeval.getName() + " - " + OBMessageUtils.messageBD("SCO_ASSET_FULLY_DEPRECIATED_IMPROV"));
        return msg;
      }
      if (LINEAR.equals(depreciationType) && PERCENTAGE.equals(calculateType) && annualDepreciation.compareTo(BigDecimal.ZERO) <= 0) {
        msg.setType("Error");
        msg.setTitle(OBMessageUtils.messageBD("Error"));
        msg.setMessage(reeval.getName() + " - " + String.format(OBMessageUtils.messageBD("SCO_ASSET_MANDATORY_POSSITIVE_REEVAL"), OBMessageUtils.messageBD("ASSET_ANNUAL_DEPRECIATION")));
        return msg;
      }
      if (TIME.equals(calculateType) && MONTH.equals(amortizationFrequency) && uselifeMonths.compareTo(BigDecimal.ZERO) <= 0) {
        msg.setType("Error");
        msg.setTitle(OBMessageUtils.messageBD("Error"));
        msg.setMessage(reeval.getName() + " - " + String.format(OBMessageUtils.messageBD("ASSET_MANDATORY_POSSITIVE_REEVAL"), OBMessageUtils.messageBD("ASSET_USABLE_LIFE_MONTHS")));
        return msg;
      }
      if (TIME.equals(calculateType) && YEAR.equals(amortizationFrequency) && uselifeYears.compareTo(BigDecimal.ZERO) <= 0) {
        msg.setType("Error");
        msg.setTitle(OBMessageUtils.messageBD("Error"));
        msg.setMessage(reeval.getName() + " - " + String.format(OBMessageUtils.messageBD("ASSET_MANDATORY_POSSITIVE_REEVAL"), OBMessageUtils.messageBD("ASSET_USABLE_LIFE_YEARS")));
        return msg;
      }

      stdPrecision = 2;
      try {
        OBContext.setAdminMode(true);
        stdPrecision = asset.getCurrency().getStandardPrecision().intValue();
      } finally {
        OBContext.restorePreviousMode();
      }

      // =========== Calculate amount to amortize per period ===========
      if (LINEAR.equals(depreciationType)) {
        if (PERCENTAGE.equals(calculateType)) {
          isPercentage = true;
          totalPeriods = HUNDRED.divide(annualDepreciation, RoundingMode.CEILING);
          // If the depreciation start date is not the first day of the period increment by 1 the
          // number of periods
          totalPeriods = (calStart.get(Calendar.DAY_OF_MONTH) == 1 && calStart.get(Calendar.MONTH) == 0) ? totalPeriods : totalPeriods.add(BigDecimal.ONE);
          amountPerPeriod = amount.multiply(annualDepreciation, mc).divide(HUNDRED, mc);

        } else if (TIME.equals(calculateType)) {
          if (MONTH.equals(amortizationFrequency)) {
            // Calculate real end date
            // From: 01/01/2013, 2 months, calRealdEnd = 01/03/2012
            calRealEnd.add(Calendar.MONTH, uselifeMonths.intValue());
            isMonthly = true;
            // If the depreciation start date is not the first day of the period increment by 1 the
            // number of periods
            totalPeriods = (calStart.get(Calendar.DAY_OF_MONTH) == 1) ? uselifeMonths : uselifeMonths.add(BigDecimal.ONE);

            if (is30DayMonth) {
              // Calculate total days proportional to months of 30 days
              if (alreadyAmortizedPeriods.compareTo(BigDecimal.ZERO) > 0) {
                // totalDays = calculateNumberOfDaysIn30DayMonths(maxAmortizationDateCal,
                // calRealEnd);
                totalDays = getDaysBetweenProportionalPeriods(maxAmortizationDateCal, calRealEnd, isYearly, isMonthly);
              } else {
                totalDays = uselifeMonths.multiply(THIRTY, mc);
              }
            } else {
              // Calculate total natural days
              totalDays = new BigDecimal(FIN_Utility.getDaysBetween(maxAmortizationDateCal.getTime(), calRealEnd.getTime()));
            }

            if (totalDays.compareTo(BigDecimal.ZERO) == 0) {
              return msg;
            }

            // From: 01/01/2013, 2 months, calRealdEnd = 01/03/2012
            // Decrease by one. Last day should be the last day (included) of the range (28/02/2013)
            calRealEnd.add(Calendar.DATE, -1);

          } else if (YEAR.equals(amortizationFrequency)) {
            // Calculate real end date
            // From: 01/01/2013, 1 year, calRealdEnd = 01/01/2014
            calRealEnd.add(Calendar.YEAR, uselifeYears.intValue());
            isYearly = true;
            // If the depreciation start date is not the first day of the period increment by 1 the
            // number of periods
            totalPeriods = (calStart.get(Calendar.DAY_OF_MONTH) == 1 && calStart.get(Calendar.MONTH) == 0) ? uselifeYears : uselifeYears.add(BigDecimal.ONE);
            amountPerPeriod = amount.divide(uselifeYears, mc);

            if (is365DayYear) {
              // Calculate total days proportional to years of 365 days
              if (alreadyAmortizedPeriods.compareTo(BigDecimal.ZERO) > 0) {
                totalDays = getDaysBetweenProportionalPeriods(maxAmortizationDateCal, calRealEnd, isYearly, isMonthly);
              } else {
                totalDays = uselifeYears.multiply(YEARDAYS, mc);
              }
            } else {
              // Calculate total natural days
              totalDays = new BigDecimal(FIN_Utility.getDaysBetween(maxAmortizationDateCal.getTime(), calRealEnd.getTime()));
            }

            if (totalDays.compareTo(BigDecimal.ZERO) == 0) {
              return msg;
            }

            // Decrease by one. Last day should be the last day (included) of the range (31/12/2013)
            // From: 01/01/2013, 1 year, calRealdEnd = 01/01/2014
            calRealEnd.add(Calendar.DATE, -1);

          } else {
            msg.setType("Error");
            msg.setTitle(OBMessageUtils.messageBD("Error"));
            msg.setMessage("Unsupported amortization frequency reeval: " + amortizationFrequency);
            return msg;
          }
        } else {
          msg.setType("Error");
          msg.setTitle(OBMessageUtils.messageBD("Error"));
          msg.setMessage("Unsupported amortization calculation type reeval: " + calculateType);
          return msg;
        }
      }

      // Calendar auxiliary variables
      calAux = Calendar.getInstance();
      calAux.setTime(startDate);
      // First and last period (month or year) day. For example, 2 month, starting on 17/01/12.
      // period 1: calFirstDayOfPeriod = 01/01/12, calLastDayOfPeriod = 31/01/12.
      // period 2: calFirstDayOfPeriod = 01/02/12, calLastDayOfPeriod = 29/02/12.
      // period 3: calFirstDayOfPeriod = 01/03/12, calLastDayOfPeriod = 31/03/12.
      calLastDayOfPeriod = Calendar.getInstance(); // Last period (month or year) day.
      calFirstDayOfPeriod = (Calendar) calStart.clone(); // First period (month or year) day.
      calFirstDayOfPeriod.set(Calendar.DAY_OF_MONTH, 1);
      if (YEAR.equals(amortizationFrequency)) {
        calFirstDayOfPeriod.set(Calendar.MONTH, 0);
      }

      currentYear = calStart.get(Calendar.YEAR);
      currentMonth = calStart.get(Calendar.MONTH);
      currentMonthDay = calStart.get(Calendar.DAY_OF_MONTH);
      currentYearDay = calAux.get(Calendar.DAY_OF_YEAR);
      lastDayOfMonth = calStart.getActualMaximum(Calendar.DAY_OF_MONTH);
      lastDayOfYear = calStart.getActualMaximum(Calendar.DAY_OF_YEAR);

      totalizedAmount = BigDecimal.ZERO;
      totalizedPercentage = BigDecimal.ZERO;
      proportionalAmount = BigDecimal.ZERO;
      contDays = BigDecimal.ZERO;
      contPeriods = 0;
      endOfRange = false;
      maxDaysOfRange = 0;
      seqNoAsset = null;

      // Loop till full amount is depreciated
      // Calculate amortization line number
      Long lineNo = getSCOAssetReevalLiMaxLineNo(reeval) + 10L;
      for (; !calAux.after(calRealEnd) || (isPercentage && totalizedAmount.compareTo(amount) < 0); calAux.add(Calendar.DATE, 1)) {
        contDays = contDays.add(BigDecimal.ONE);
        if (currentYear != calAux.get(Calendar.YEAR)) {
          currentYear = calAux.get(Calendar.YEAR);
          lastDayOfYear = calAux.getActualMaximum(Calendar.DAY_OF_YEAR);
        }
        if (currentMonth != calAux.get(Calendar.MONTH)) {
          currentMonth = calAux.get(Calendar.MONTH);
          lastDayOfMonth = calAux.getActualMaximum(Calendar.DAY_OF_MONTH);
        }

        currentMonthDay = calAux.get(Calendar.DAY_OF_MONTH);
        currentYearDay = calAux.get(Calendar.DAY_OF_YEAR);

        // Month
        if (isMonthly && currentMonthDay == lastDayOfMonth) {
          endOfRange = true;
          maxDaysOfRange = lastDayOfMonth;
        }
        // Percentage or Year
        if ((isPercentage || isYearly) && currentYearDay == lastDayOfYear) {
          endOfRange = true;
          maxDaysOfRange = lastDayOfYear;
        }

        // Last day of range (last iteration in the loop)
        if (!isPercentage && calAux.compareTo(calRealEnd) == 0) {
          endOfRange = true;
          // Month
          if (isMonthly) {
            maxDaysOfRange = lastDayOfMonth;
          }
          // Percentage or Year
          if (isPercentage || isYearly) {
            maxDaysOfRange = lastDayOfYear;
          }
        }

        if (endOfRange) {
          contPeriods += 1;
          calLastDayOfPeriod = (Calendar) calAux.clone();
          calLastDayOfPeriod.set(Calendar.DAY_OF_MONTH, calAux.getActualMaximum(Calendar.DAY_OF_MONTH));
          if (isYearly) {
            calLastDayOfPeriod.set(Calendar.MONTH, calAux.getActualMaximum(Calendar.MONTH));
          }

          SCOAssetreevAmortli reeval_amortizationLine = getSCOAssetReevalLine(reeval, null, calLastDayOfPeriod.getTime());

          if (reeval_amortizationLine != null) {
            // Recalculate percentage because the amount could have been changed
            BigDecimal proportionaldPercentage = reeval_amortizationLine.getAmortizationAmount().multiply(HUNDRED).divide(amount, stdPrecision, RoundingMode.HALF_UP);
            totalizedAmount = totalizedAmount.add(reeval_amortizationLine.getAmortizationAmount());
            totalizedPercentage = totalizedPercentage.add(proportionaldPercentage);
            if (reeval_amortizationLine.getAmortizationPercentage() != null && reeval_amortizationLine.getAmortizationPercentage().compareTo(proportionaldPercentage) != 0) {
              reeval_amortizationLine.setAmortizationPercentage(proportionaldPercentage);
            }

            OBDal.getInstance().save(reeval_amortizationLine);

          } else {
            // Calculate the proportional amount for current period (improv amortization line)
            if (isPercentage) {
              proportionalAmount = contDays.multiply(amountPerPeriod, mc).divide(new BigDecimal(maxDaysOfRange), mc);
            } else {
              if (isMonthly && is30DayMonth) {
                contDays = contDays.multiply(THIRTY, mc).divide(new BigDecimal(maxDaysOfRange), mc);
              }
              if (isYearly && is365DayYear) {
                contDays = contDays.multiply(YEARDAYS, mc).divide(new BigDecimal(maxDaysOfRange), mc);
              }
              proportionalAmount = contDays.multiply(pendingAmountToAmortize, mc).divide(totalDays, mc);

            }

            // Calculate percentage
            BigDecimal proportionaldPercentage = proportionalAmount.multiply(HUNDRED).divide(amount, mc).setScale(stdPrecision, RoundingMode.HALF_UP);

            // Round the amount after using it for percentage calculation
            proportionalAmount = proportionalAmount.setScale(stdPrecision, RoundingMode.HALF_UP);

            // Last period. Adjust for avoiding rounding issues.
            if (((!isPercentage && new BigDecimal(contPeriods).compareTo(totalPeriods) == 0)) || totalizedAmount.add(proportionalAmount).compareTo(amount) > 0) {
              proportionalAmount = amount.subtract(totalizedAmount);
              proportionaldPercentage = HUNDRED.subtract(totalizedPercentage);
            }

            log4j.debug(OBDateUtils.formatDate(calFirstDayOfPeriod.getTime()) + " to " + OBDateUtils.formatDate(calLastDayOfPeriod.getTime()) + "  " + proportionalAmount + "  " + proportionaldPercentage);

            // Accumulate amount and percentage
            totalizedAmount = totalizedAmount.add(proportionalAmount);
            totalizedPercentage = totalizedPercentage.add(proportionaldPercentage);

            // Search for amortization line
            AmortizationLine amortizationline = getAmortizationLineForImprovLi(asset, null, calLastDayOfPeriod.getTime());
            if (amortizationline == null) {
              msg.setType("Error");
              msg.setTitle(OBMessageUtils.messageBD("Error"));
              msg.setMessage(String.format(OBMessageUtils.messageBD("SCO_AMORTIZATIONLINEFORIMPROVLINE_NOTFOUND") + ": " + reeval.getName()));
              return msg;
            }

            // Create the asset improv amortization line
            reeval_amortizationLine = createNewSCOAssetReevAmortli(reeval, amortizationline, lineNo, proportionaldPercentage, proportionalAmount);
            lineNo = lineNo + 10L;
          }

          // Initialize new range
          contDays = BigDecimal.ZERO;
          calFirstDayOfPeriod = (Calendar) calAux.clone();
          calFirstDayOfPeriod.add(Calendar.DAY_OF_MONTH, 1);
          endOfRange = false;
        }
      }

      asset.setDepreciatedPlan(asset.getDepreciatedPlan().add(amount));
      OBDal.getInstance().save(asset);

    }

    asset.setProcessed("Y");
    asset.setProcessAsset("Y");
    OBDal.getInstance().save(asset);
    OBDal.getInstance().flush();

    return msg;
  }

  /**
   * Get the amortization period matching given start/end date, organization and project. The
   * amortization period must be not processed.
   * 
   * @param org
   *          Organization.
   * @param startDate
   *          Start date.
   * @param endDate
   *          End date.
   * @return amortization period matching given start/end date, organization and project.
   */
  private Amortization getAmortization(Organization org, Date startDate, Date endDate, Project project) {
    OBCriteria<Amortization> obc = OBDal.getInstance().createCriteria(Amortization.class);
    obc.add(Restrictions.eq(Amortization.PROPERTY_ORGANIZATION, org));
    if (startDate != null) {
      obc.add(Restrictions.eq(Amortization.PROPERTY_STARTINGDATE, startDate));
    }
    obc.add(Restrictions.eq(Amortization.PROPERTY_ENDINGDATE, endDate));
    obc.add(Restrictions.eq(Amortization.PROPERTY_PROCESSED, "N"));
    if (project != null) {
      obc.add(Restrictions.eq(Amortization.PROPERTY_PROJECT, project));
    } else {
      obc.add(Restrictions.isNull(Amortization.PROPERTY_PROJECT));
    }
    obc.setFilterOnReadableOrganization(false);
    List<Amortization> amortizationList = obc.list();
    if (amortizationList.size() == 0) {
      return null;
    } else if (amortizationList.size() > 1) {
      throw new OBException("More than one amortization exist from " + startDate == null ? " null " : startDate.toString() + " to " + endDate.toString() + " for " + org.getName() + " organization");
    }
    return amortizationList.get(0);
  }

  private AmortizationLine getAmortizationLineForImprovLi(Asset asset, Date startDate, Date endDate) {

    List<AmortizationLine> als = asset.getFinancialMgmtAmortizationLineList();

    OBCriteria<AmortizationLine> obc = OBDal.getInstance().createCriteria(AmortizationLine.class);

    obc.add(Restrictions.eq(AmortizationLine.PROPERTY_ASSET, asset));
    obc.createAlias(AmortizationLine.PROPERTY_AMORTIZATION, "am");

    if (startDate != null) {
      obc.add(Restrictions.eq("am." + Amortization.PROPERTY_STARTINGDATE, startDate));
    }
    obc.add(Restrictions.eq("am." + Amortization.PROPERTY_ENDINGDATE, endDate));
    obc.setFilterOnReadableOrganization(false);
    List<AmortizationLine> amortizationlineList = obc.list();
    if (amortizationlineList.size() == 0) {
      return null;
    } else if (amortizationlineList.size() > 1) {
      throw new OBException("More than one amortization line exist from " + startDate == null ? " null " : startDate.toString() + " to " + endDate.toString() + " for " + asset.getName() + " asset");
    }
    return amortizationlineList.get(0);
  }
  
  private AmortizationLine getAmortizationLineForReevalLi(Asset asset, Date startDate, Date endDate) {

    List<AmortizationLine> als = asset.getFinancialMgmtAmortizationLineList();

    OBCriteria<AmortizationLine> obc = OBDal.getInstance().createCriteria(AmortizationLine.class);

    obc.add(Restrictions.eq(AmortizationLine.PROPERTY_ASSET, asset));
    obc.createAlias(AmortizationLine.PROPERTY_AMORTIZATION, "am");

    if (startDate != null) {
      obc.add(Restrictions.eq("am." + Amortization.PROPERTY_STARTINGDATE, startDate));
    }
    obc.add(Restrictions.eq("am." + Amortization.PROPERTY_ENDINGDATE, endDate));
    obc.setFilterOnReadableOrganization(false);
    List<AmortizationLine> amortizationlineList = obc.list();
    if (amortizationlineList.size() == 0) {
      return null;
    } else if (amortizationlineList.size() > 1) {
      throw new OBException("More than one amortization line exist from " + startDate == null ? " null " : startDate.toString() + " to " + endDate.toString() + " for " + asset.getName() + " asset");
    }
    return amortizationlineList.get(0);
  }

  /**
   * Get the amortization line associated to the given asset and included in the amortization for
   * the given period range.
   * 
   * @param asset
   *          Asset.
   * @param startDate
   *          Start date.
   * @param endDate
   *          End date.
   * @return Amortization line associated to the given asset and included in the amortization for
   *         the given period range.
   * @throws OBException
   *           If more than one amortization line exists for the given period.
   */
  private AmortizationLine getAmortizationLine(Asset asset, Date startDate, Date endDate) throws OBException {

    StringBuilder whereClause = new StringBuilder();
    final List<Object> parameters = new ArrayList<Object>();
    whereClause.append(" as aml join aml.amortization as am ");
    whereClause.append(" where aml.asset.id = ? ");
    if (startDate != null) {
      whereClause.append("       and am.startingDate = ?");
    }
    whereClause.append("       and am.endingDate = ?");
    final OBQuery<AmortizationLine> obq = OBDal.getInstance().createQuery(AmortizationLine.class, whereClause.toString());
    obq.setFilterOnReadableOrganization(false);
    parameters.add(asset.getId());
    if (startDate != null) {
      parameters.add(startDate);
    }
    parameters.add(endDate);
    obq.setParameters(parameters);
    List<AmortizationLine> amortizationLineList = obq.list();
    if (amortizationLineList.size() == 0) {
      return null;
    } else if (amortizationLineList.size() > 1) {
      throw new OBException("More than one amortization line exist from " + startDate.toString() + " to " + endDate.toString() + " for " + asset.getName() + " asset");
    }
    return amortizationLineList.get(0);
  }

  private SCOAssetimpAmortli getSCOAssetImprovLine(SCOAssetImprov improv, Date startDate, Date endDate) throws OBException {
    StringBuilder whereClause = new StringBuilder();
    final List<Object> parameters = new ArrayList<Object>();
    whereClause.append(" as ail join ail.amortizationline as aml join aml.amortization as am ");
    whereClause.append(" where ail.sCOAssetImprov.id = ? ");
    if (startDate != null) {
      whereClause.append("       and am.startingDate = ?");
    }
    whereClause.append("       and am.endingDate = ?");
    final OBQuery<SCOAssetimpAmortli> obq = OBDal.getInstance().createQuery(SCOAssetimpAmortli.class, whereClause.toString());
    obq.setFilterOnReadableOrganization(false);
    parameters.add(improv.getId());
    if (startDate != null) {
      parameters.add(startDate);
    }
    parameters.add(endDate);
    obq.setParameters(parameters);
    List<SCOAssetimpAmortli> improv_amortizationLineList = obq.list();
    if (improv_amortizationLineList.size() == 0) {
      return null;
    } else if (improv_amortizationLineList.size() > 1) {
      throw new OBException("More than one asset improv amortization line exist from " + startDate.toString() + " to " + endDate.toString() + " for " + improv.getName() + " asset");
    }
    return improv_amortizationLineList.get(0);
  }
  
  private SCOAssetreevAmortli getSCOAssetReevalLine(SCOAssetReeval reeval, Date startDate, Date endDate) throws OBException {
    StringBuilder whereClause = new StringBuilder();
    final List<Object> parameters = new ArrayList<Object>();
    whereClause.append(" as arl join arl.amortizationline as aml join aml.amortization as am ");
    whereClause.append(" where arl.reevaluacinDelActivo.id = ? ");
    if (startDate != null) {
      whereClause.append("       and am.startingDate = ?");
    }
    whereClause.append("       and am.endingDate = ?");
    final OBQuery<SCOAssetreevAmortli> obq = OBDal.getInstance().createQuery(SCOAssetreevAmortli.class, whereClause.toString());
    obq.setFilterOnReadableOrganization(false);
    parameters.add(reeval.getId());
    if (startDate != null) {
      parameters.add(startDate);
    }
    parameters.add(endDate);
    obq.setParameters(parameters);
    List<SCOAssetreevAmortli> reeval_amortizationLineList = obq.list();
    if (reeval_amortizationLineList.size() == 0) {
      return null;
    } else if (reeval_amortizationLineList.size() > 1) {
      throw new OBException("More than one asset reeval amortization line exist from " + startDate.toString() + " to " + endDate.toString() + " for " + reeval.getName() + " asset");
    }
    return reeval_amortizationLineList.get(0);
  }

  /**
   * Create a new amortization.
   * 
   * @return Amortization
   */
  private Amortization createNewAmortization(Organization organization, String name, String description, Date startDate, Date endDate, Currency currency, Project project, Campaign campaign, ABCActivity activity, UserDimension1 user1, UserDimension2 user2) {
    Amortization am = OBProvider.getInstance().get(Amortization.class);
    am.setOrganization(organization);
    am.setName(name);
    am.setDescription(description);
    am.setStartingDate(startDate);
    am.setEndingDate(endDate);
    am.setAccountingDate(endDate);
    am.setCurrency(currency);
    am.setProject(project);
    am.setSalesCampaign(campaign);
    am.setActivity(activity);
    am.setStDimension(user1);
    am.setNdDimension(user2);

    OBDal.getInstance().save(am);
    return am;
  }

  /**
   * Create a new amortization line.
   * 
   * @return Amortization line.
   */
  private AmortizationLine createNewAmortizationLine(Amortization amortization, Long lineNo, Long assetSeqNo, Asset asset, BigDecimal amortizationPercentage, BigDecimal amortizationAmount, BigDecimal grossAmortizationAmount, Currency currency, Project project, Campaign campaign, ABCActivity activity, UserDimension1 user1, UserDimension2 user2, Costcenter costCenter) {
    AmortizationLine aml = OBProvider.getInstance().get(AmortizationLine.class);
    aml.setOrganization(amortization.getOrganization());
    aml.setAmortization(amortization);
    aml.setLineNo(lineNo);
    aml.setSEQNoAsset(assetSeqNo);
    aml.setAsset(asset);
    aml.setAmortizationPercentage(amortizationPercentage);
    aml.setAmortizationAmount(amortizationAmount);
    aml.setScoGrossamortizationamt(grossAmortizationAmount);
    aml.setCurrency(currency);
    aml.setProject(project);
    aml.setStDimension(user1);
    aml.setNdDimension(user2);
    aml.setCostcenter(costCenter);

    OBDal.getInstance().save(aml);
    amortization.getFinancialMgmtAmortizationLineList().add(aml);
    OBDal.getInstance().save(amortization);

    asset.getFinancialMgmtAmortizationLineList().add(aml);
    OBDal.getInstance().save(asset);
    return aml;
  }

  private SCOAssetimpAmortli createNewSCOAssetImpAmortli(SCOAssetImprov improv, AmortizationLine amortizationline, Long lineNo, BigDecimal amortizationPercentage, BigDecimal amortizationAmount) {
    SCOAssetimpAmortli ail = OBProvider.getInstance().get(SCOAssetimpAmortli.class);
    ail.setOrganization(amortizationline.getOrganization());
    ail.setAmortizationline(amortizationline);
    ail.setSCOAssetImprov(improv);
    ail.setLineNo(lineNo);
    ail.setAmortizationPercentage(amortizationPercentage);
    ail.setAmortizationAmount(amortizationAmount);

    OBDal.getInstance().save(ail);
    amortizationline.getSCOAssetimpAmortliList().add(ail);
    OBDal.getInstance().save(amortizationline);

    improv.getSCOAssetimpAmortliList().add(ail);
    OBDal.getInstance().save(improv);

    return ail;
  }

  private SCOAssetreevAmortli createNewSCOAssetReevAmortli(SCOAssetReeval reeval, AmortizationLine amortizationline, Long lineNo, BigDecimal amortizationPercentage, BigDecimal amortizationAmount) {
    SCOAssetreevAmortli arl = OBProvider.getInstance().get(SCOAssetreevAmortli.class);
    arl.setOrganization(amortizationline.getOrganization());
    arl.setAmortizationline(amortizationline);
    arl.setReevaluacinDelActivo(reeval);;
    arl.setLineNo(lineNo);
    arl.setAmortizationPercentage(amortizationPercentage);
    arl.setAmortizationAmount(amortizationAmount);

    OBDal.getInstance().save(arl);
    amortizationline.getSCOAssetreevAmortliList().add(arl);
    OBDal.getInstance().save(amortizationline);

    reeval.getSCOAssetreevAmortliList().add(arl);
    OBDal.getInstance().save(reeval);

    return arl;
  }
  /**
   * Calculate total days between given dates being the months of 30 days and years of 365 (no
   * leap-years).
   * 
   * @param startDate
   *          Start date.
   * @param endDate
   *          End date.
   * @param isYearly
   *          Is yearly amortization.
   * @param isMonthly
   *          Is monthly amortization.
   * @return total days between given dates being the months of 30 days and years of 365 (no
   *         leap-years). The result can have decimals.
   */
  private BigDecimal getDaysBetweenProportionalPeriods(Calendar startDate, Calendar _endDate, boolean isYearly, boolean isMonthly) {
    Calendar calAux = (Calendar) startDate.clone();
    Calendar endDate = (Calendar) _endDate.clone();
    endDate.add(Calendar.DATE, -1);
    int currentYear = startDate.get(Calendar.YEAR);
    int currentMonth = startDate.get(Calendar.MONTH);
    int currentMonthDay = startDate.get(Calendar.DAY_OF_MONTH);
    int currentYearDay = startDate.get(Calendar.DAY_OF_YEAR);
    int lastDayOfMonth = startDate.getActualMaximum(Calendar.DAY_OF_MONTH);
    int lastDayOfYear = startDate.getActualMaximum(Calendar.DAY_OF_YEAR);
    BigDecimal totalDays = BigDecimal.ZERO;
    boolean endOfRange = false;
    for (; !calAux.after(endDate); calAux.add(Calendar.DATE, 1)) {
      if (currentYear != calAux.get(Calendar.YEAR)) {
        currentYear = calAux.get(Calendar.YEAR);
        lastDayOfYear = calAux.getActualMaximum(Calendar.DAY_OF_YEAR);
      }
      if (currentMonth != calAux.get(Calendar.MONTH)) {
        currentMonth = calAux.get(Calendar.MONTH);
        lastDayOfMonth = calAux.getActualMaximum(Calendar.DAY_OF_MONTH);
      }

      currentMonthDay = calAux.get(Calendar.DAY_OF_MONTH);
      currentYearDay = calAux.get(Calendar.DAY_OF_YEAR);

      // Month
      if (isMonthly && currentMonthDay == lastDayOfMonth) {
        endOfRange = true;
      }
      // Year
      if (isYearly && currentYearDay == lastDayOfYear) {
        endOfRange = true;
      }

      // Last day of range (last iteration in the loop)
      if (calAux.compareTo(endDate) == 0) {
        endOfRange = true;
      }

      if (endOfRange) {
        BigDecimal proportionalDays = BigDecimal.ZERO;
        if (isMonthly) {
          proportionalDays = THIRTY.multiply(new BigDecimal(currentMonthDay), mc).divide(new BigDecimal(lastDayOfMonth), mc);

        } else if (isYearly) {
          proportionalDays = YEARDAYS.multiply(new BigDecimal(currentYearDay), mc).divide(new BigDecimal(lastDayOfYear), mc);
        }

        totalDays = totalDays.add(proportionalDays);
        endOfRange = false;
      }
    }

    return totalDays;
  }

  /**
   * Get max asset sequence number in amortization lines related to the given asset.
   * 
   * @param asset
   *          Asset.
   * @return Max asset sequence number in amortization lines related to the given asset. 0 if there
   *         is no amortization line related to the given asset.
   */
  private Long getMaxSeqNoAsset(Asset asset) {
    StringBuilder hql = new StringBuilder();
    hql.append(" select coalesce(max(al.sEQNoAsset), 0) as maxSeqNoAsset ");
    hql.append(" from FinancialMgmtAmortizationLine as al where al.asset.id = ? ");
    Query query = OBDal.getInstance().getSession().createQuery(hql.toString());
    query.setString(0, (String) DalUtil.getId(asset));
    for (Object obj : query.list()) {
      if (obj != null) {
        return (Long) obj;
      }
    }
    return 0l;
  }

  /**
   * Get max asset sequence number in amortization lines that belong to the given amortization.
   * 
   * @param amortization
   *          Amortization.
   * @return max asset sequence number in amortization lines that belong to the given amortization.
   *         0 if there is no amortization line related to the given amortization.
   */
  private Long getMaxLineNo(Amortization amortization) {
    StringBuilder hql = new StringBuilder();
    hql.append(" select coalesce(max(al.lineNo), 0) as maxSeqNoAsset ");
    hql.append(" from FinancialMgmtAmortizationLine as al where al.amortization.id = ? ");
    Query query = OBDal.getInstance().getSession().createQuery(hql.toString());
    query.setString(0, (String) DalUtil.getId(amortization));
    for (Object obj : query.list()) {
      if (obj != null) {
        return (Long) obj;
      }
    }
    return 0l;
  }

  private Long getSCOAssetImprovLiMaxLineNo(SCOAssetImprov improv) {
    StringBuilder hql = new StringBuilder();
    hql.append(" select coalesce(max(ail.lineNo), 0) as lineno ");
    hql.append(" from SCO_Assetimp_Amortli as ail where ail.sCOAssetImprov.id = ? ");
    Query query = OBDal.getInstance().getSession().createQuery(hql.toString());
    query.setString(0, (String) DalUtil.getId(improv));
    for (Object obj : query.list()) {
      if (obj != null) {
        return (Long) obj;
      }
    }
    return 0l;
  }
  
  private Long getSCOAssetReevalLiMaxLineNo(SCOAssetReeval reeval) {
    StringBuilder hql = new StringBuilder();
    hql.append(" select coalesce(max(arl.lineNo), 0) as lineno ");
    hql.append(" from SCO_Assetreev_Amortli as arl where arl.reevaluacinDelActivo.id = ? ");
    Query query = OBDal.getInstance().getSession().createQuery(hql.toString());
    query.setString(0, (String) DalUtil.getId(reeval));
    for (Object obj : query.list()) {
      if (obj != null) {
        return (Long) obj;
      }
    }
    return 0l;
  }

}
