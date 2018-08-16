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
 * All portions are Copyright (C) 2001-2014 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.PriceAdjustment;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.financial.FinancialUtils;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.SCO_Utils;

public class SL_Order_Amt extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private static final BigDecimal ZERO = BigDecimal.ZERO;

  @Override
  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strChanged = vars.getStringParameter("inpLastFieldChanged");
      log4j.debug("CHANGED: " + strChanged);
      String strQtyOrdered = vars.getNumericParameter("inpqtyordered");
      String strPriceActual = vars.getNumericParameter("inppriceactual");
      String strTaxPriceActual = vars.getNumericParameter("inpemSsaTaxpriceactual");
      String strDiscount = vars.getNumericParameter("inpdiscount");
      String strPriceLimit = vars.getNumericParameter("inppricelimit");
      String strPriceList = vars.getNumericParameter("inppricelist");
      String strPriceStd = vars.getNumericParameter("inppricestd");
      String strCOrderId = vars.getStringParameter("inpcOrderId");
      String strProduct = vars.getStringParameter("inpmProductId");
      String strUOM = vars.getStringParameter("inpcUomId");
      String strAttribute = vars.getStringParameter("inpmAttributesetinstanceId");
      String strQty = vars.getNumericParameter("inpqtyordered");
      boolean cancelPriceAd = "Y".equals(vars.getStringParameter("inpcancelpricead"));
      String strLineNetAmt = vars.getNumericParameter("inplinenetamt");
      String strTaxId = vars.getStringParameter("inpcTaxId");
      String strGrossUnitPrice = vars.getNumericParameter("inpgrossUnitPrice");
      String strGrossPriceList = vars.getNumericParameter("inpgrosspricelist");
      String strGrossBaseUnitPrice = vars.getNumericParameter("inpgrosspricestd");
      String strtaxbaseamt = vars.getNumericParameter("inptaxbaseamt");
      String strClientpolineId = vars.getStringParameter("inpemSsaClientpolineId");

      String strADOrgID = vars.getStringParameter("inpadOrgId");
      String strCOrderLineId = vars.getStringParameter("inpcOrderLineId");

      try {
        printPage(response, vars, strChanged, strQtyOrdered, strPriceActual, strDiscount,
            strPriceLimit, strPriceList, strCOrderId, strProduct, strUOM, strAttribute, strQty,
            strPriceStd, cancelPriceAd, strLineNetAmt, strTaxId, strGrossUnitPrice,
            strGrossPriceList, strtaxbaseamt, strGrossBaseUnitPrice, strClientpolineId, strADOrgID,
            strCOrderLineId, strTaxPriceActual);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strChanged,
      String strQtyOrdered, String _strPriceActual, String strDiscount, String strPriceLimit,
      String strPriceList, String strCOrderId, String strProduct, String strUOM,
      String strAttribute, String strQty, String strPriceStd, boolean cancelPriceAd,
      String strLineNetAmt, String strTaxId, String strGrossUnitPrice, String strGrossPriceList,
      String strTaxBaseAmt, String strGrossBaseUnitPrice, String strClientpolineId,
      String strADOrgID, String strCOrderLineId, String strTaxPriceActual) throws IOException,
      ServletException {
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    SLOrderAmtData[] data = SLOrderAmtData.select(this, strCOrderId);
    SLOrderStockData[] data1 = SLOrderStockData.select(this, strProduct);
    String strPrecision = "0", strPricePrecision = "0";
    String strStockSecurity = "0";
    String strEnforceAttribute = "N";
    String isSOTrx = SLOrderStockData.isSotrx(this, strCOrderId);
    String strStockNoAttribute;
    String strStockAttribute;
    String strPriceActual = _strPriceActual;
    PriceList currentPriceList = OBDal.getInstance().get(PriceList.class, data[0].mPricelistId);

    boolean isTaxIncludedPriceList = currentPriceList.isPriceIncludesTax();
    boolean isGrossUnitPriceChanged = strChanged.equals("inpgrossUnitPrice");
    boolean forceSetPriceStd = false;
    if (data1 != null && data1.length > 0) {
      strStockSecurity = data1[0].stock;
      strEnforceAttribute = data1[0].enforceAttribute;
    }
    // boolean isUnderLimit=false;
    if (data != null && data.length > 0) {
      strPrecision = data[0].stdprecision.equals("") ? "0" : data[0].stdprecision;
      strPricePrecision = data[0].priceprecision.equals("") ? "0" : data[0].priceprecision;
    }
    int stdPrecision = Integer.valueOf(strPrecision).intValue();
    int pricePrecision = Integer.valueOf(strPricePrecision).intValue();

    BigDecimal qtyOrdered, priceActual, priceLimit, netPriceList, stockSecurity, stockNoAttribute, stockAttribute, resultStock, priceStd, lineNetAmt, taxBaseAmt, taxpriceactual;
    stockSecurity = new BigDecimal(strStockSecurity);
    qtyOrdered = (strQtyOrdered.equals("") ? ZERO : new BigDecimal(strQtyOrdered));
    priceActual = (strPriceActual.equals("") ? ZERO : (new BigDecimal(strPriceActual))).setScale(
        pricePrecision, BigDecimal.ROUND_HALF_UP);

    taxpriceactual = (strTaxPriceActual.equals("") ? ZERO : (new BigDecimal(strTaxPriceActual)))
        .setScale(pricePrecision, BigDecimal.ROUND_HALF_UP);
    priceLimit = (strPriceLimit.equals("") ? ZERO : (new BigDecimal(strPriceLimit))).setScale(
        pricePrecision, BigDecimal.ROUND_HALF_UP);
    netPriceList = (strPriceList.equals("") ? ZERO : (new BigDecimal(strPriceList))).setScale(
        pricePrecision, BigDecimal.ROUND_HALF_UP);
    priceStd = (strPriceStd.equals("") ? ZERO : (new BigDecimal(strPriceStd))).setScale(
        pricePrecision, BigDecimal.ROUND_HALF_UP);
    lineNetAmt = (strLineNetAmt.equals("") ? ZERO : (new BigDecimal(strLineNetAmt))).setScale(
        pricePrecision, BigDecimal.ROUND_HALF_UP);
    taxBaseAmt = (strTaxBaseAmt.equals("") ? ZERO : (new BigDecimal(strTaxBaseAmt))).setScale(
        pricePrecision, BigDecimal.ROUND_HALF_UP);
    BigDecimal grossUnitPrice = (strGrossUnitPrice.equals("") ? ZERO : new BigDecimal(
        strGrossUnitPrice).setScale(pricePrecision, BigDecimal.ROUND_HALF_UP));
    BigDecimal grossPriceList = (strGrossPriceList.equals("") ? ZERO : new BigDecimal(
        strGrossPriceList).setScale(pricePrecision, BigDecimal.ROUND_HALF_UP));
    BigDecimal grossBaseUnitPrice = (strGrossBaseUnitPrice.equals("") ? ZERO : new BigDecimal(
        strGrossBaseUnitPrice).setScale(pricePrecision, BigDecimal.ROUND_HALF_UP));

    // A hook has been created. This hook will be raised when the qty is changed having selected a
    // product
    /*
     * if (!strProduct.equals("") && strChanged.equals("inpqtyordered")) { try {
     * OrderLineQtyChangedHookObject hookObject = new OrderLineQtyChangedHookObject();
     * hookObject.setProductId(strProduct); hookObject.setQty(qtyOrdered);
     * hookObject.setOrderId(strCOrderId); hookObject.setPricePrecision(pricePrecision);
     * hookObject.setPriceList(currentPriceList); if (isTaxIncludedPriceList) {
     * hookObject.setListPrice(grossPriceList); hookObject.setPrice(grossBaseUnitPrice); } else {
     * hookObject.setListPrice(netPriceList); hookObject.setPrice(priceStd); }
     * 
     * hookObject.setChanged(strChanged);
     * WeldUtils.getInstanceFromStaticBeanManager(OrderLineQtyChangedHookManager.class)
     * .executeHooks(hookObject); if (isTaxIncludedPriceList) { if
     * (grossBaseUnitPrice.compareTo(hookObject.getPrice()) != 0) { grossBaseUnitPrice =
     * hookObject.getPrice(); isGrossUnitPriceChanged = true; } } else { if
     * (priceStd.compareTo(hookObject.getPrice()) != 0) { priceStd = hookObject.getPrice();
     * forceSetPriceStd = true; } } } catch (Exception e) { // TODO Auto-generated catch block
     * e.printStackTrace(); } }
     */
    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_Order_Amt';\n\n");
    resultado.append("var respuesta = new Array(");

    Order order = OBDal.getInstance().get(Order.class, strCOrderId);
    Product product = OBDal.getInstance().get(Product.class, strProduct);

    if (strChanged.equals("inpemSsaTaxpriceactual")) {
      // Calculate priceactual from taxpriceactual and tax
      TaxRate taxrate = OBDal.getInstance().get(TaxRate.class, strTaxId);
      if (taxrate != null) {
        System.out.println("Enter SL_Order_Amt  taxxxxx3");
        BigDecimal rate = taxrate.getRate();
        if (rate.compareTo(new BigDecimal(0)) == 0) {
          priceActual = taxpriceactual;
        } else {
          priceActual = (taxpriceactual.multiply(new BigDecimal(100))).divide(
              (rate.add(new BigDecimal(100))), pricePrecision, BigDecimal.ROUND_HALF_UP);
        }
        System.out.println("inppriceactual:" + priceActual.toString());
        resultado.append("new Array(\"inppriceactual\", \"" + priceActual.toString() + "\"),\n");

      }
    }

    if (strChanged.equals("inplinenetamt")) {
      priceActual = lineNetAmt.divide(qtyOrdered, pricePrecision, BigDecimal.ROUND_HALF_UP);
      if (priceActual.compareTo(BigDecimal.ZERO) == 0) {
        lineNetAmt = BigDecimal.ZERO;
        priceActual = lineNetAmt.divide(qtyOrdered, pricePrecision, BigDecimal.ROUND_HALF_UP);
      }
    }

    if (strChanged.equals("inpqtyordered") && !cancelPriceAd) {
      if (isTaxIncludedPriceList) {
        grossUnitPrice = PriceAdjustment.calculatePriceActual(order, product, qtyOrdered,
            grossBaseUnitPrice);
        BigDecimal grossAmount = grossUnitPrice.multiply(qtyOrdered).setScale(stdPrecision,
            RoundingMode.HALF_UP);
        priceActual = FinancialUtils.calculateNetFromGross(strTaxId, grossAmount, pricePrecision,
            taxBaseAmt, qtyOrdered);
        resultado.append("new Array(\"inpgrossUnitPrice\", " + grossUnitPrice.toString() + "),");
      } else {
        priceActual = PriceAdjustment.calculatePriceActual(order, product, qtyOrdered, priceStd);
      }
      
        resultado.append("new Array(\"inppriceactual\", " + priceActual + "),");

        System.out.println("Enter SL_Order_Amt  taxxxxx2");

        // Calculate taxpriceactual from priceactual and tax
        TaxRate taxrate = OBDal.getInstance().get(TaxRate.class, strTaxId);
        if (taxrate != null) {
          BigDecimal rate = taxrate.getRate();
          if (rate.compareTo(new BigDecimal(0)) == 0) {
            taxpriceactual = priceActual;
          } else {
            taxpriceactual = priceActual.add(priceActual.multiply(rate).divide(new BigDecimal(100),
                order.getCurrency().getPricePrecision().intValue(), BigDecimal.ROUND_HALF_UP));
          }
          System.out.println("inpemSsaTaxpriceactual:" + taxpriceactual);

          resultado.append("new Array(\"inpemSsaTaxpriceactual\", \"" + taxpriceactual + "\"),\n");

        }
    }

    // Calculating prices for offers...
    if (strChanged.equals("inppriceactual") || strChanged.equals("inplinenetamt")
        || strChanged.equals("inpemSsaTaxpriceactual") || forceSetPriceStd) {
      log4j.debug("priceActual:" + priceActual.toString());
      if (!cancelPriceAd) {
        priceStd = PriceAdjustment.calculatePriceStd(order, product, qtyOrdered, priceActual);
      } else {
        priceStd = priceActual;
      }
      System.out.println("111  priceActual:" + priceActual + " - priceStd:" + priceStd);
      resultado.append("new Array(\"inppricestd\", " + priceStd.toString() + "),");
    }

    if (strChanged.equals("inpcancelpricead")) {
      if (cancelPriceAd) {
        //if (strClientpolineId == null || strClientpolineId.equals("")) {
          resultado.append("new Array(\"inppriceactual\", " + strPriceStd + "),");

          System.out.println("Enter SL_Order_Amt  taxxxxx2");

          // Calculate taxpriceactual from priceactual and tax
          TaxRate taxrate = OBDal.getInstance().get(TaxRate.class, strTaxId);
          if (taxrate != null) {
            BigDecimal priceactual = new BigDecimal(strPriceStd);
            BigDecimal rate = taxrate.getRate();
            if (rate.compareTo(new BigDecimal(0)) == 0) {
              taxpriceactual = priceactual;
            } else {
              taxpriceactual = priceactual.add(priceactual.multiply(rate).divide(
                  new BigDecimal(100), order.getCurrency().getPricePrecision().intValue(),
                  BigDecimal.ROUND_HALF_UP));
            }
            System.out.println("inpemSsaTaxpriceactual:" + taxpriceactual);

            resultado
                .append("new Array(\"inpemSsaTaxpriceactual\", \"" + taxpriceactual + "\"),\n");

          }
        //}
      }
    }

    // if taxinclusive field is changed then modify net unit price and gross price
    if (isGrossUnitPriceChanged || (strChanged.equals("inpcTaxId") && isTaxIncludedPriceList)) {
      BigDecimal grossAmount = grossUnitPrice.multiply(qtyOrdered).setScale(stdPrecision,
          RoundingMode.HALF_UP);

      final BigDecimal netUnitPrice = FinancialUtils.calculateNetFromGross(strTaxId, grossAmount,
          pricePrecision, taxBaseAmt, qtyOrdered);

      priceActual = netUnitPrice;
      priceStd = netUnitPrice;
      grossBaseUnitPrice = grossUnitPrice;
      resultado.append("new Array(\"inpgrosspricestd\", " + grossBaseUnitPrice.toString() + "),");

      //if (strClientpolineId == null || strClientpolineId.equals("")) {
        resultado.append("new Array(\"inppriceactual\"," + priceActual.toString() + "),");
        resultado.append("new Array(\"inppricelist\"," + netUnitPrice.toString() + "),");
      //}
      resultado.append("new Array(\"inppricelimit\", " + netUnitPrice.toString() + "),");
      resultado.append("new Array(\"inppricestd\"," + netUnitPrice.toString() + "),");
    }

    if (isGrossUnitPriceChanged || (strChanged.equals("inpcTaxId") && isTaxIncludedPriceList)) {
      BigDecimal grossAmount = grossUnitPrice.multiply(qtyOrdered).setScale(stdPrecision,
          RoundingMode.HALF_UP);

      final BigDecimal netUnitPrice = FinancialUtils.calculateNetFromGross(strTaxId, grossAmount,
          pricePrecision, taxBaseAmt, qtyOrdered);

      priceActual = netUnitPrice;
      if (cancelPriceAd) {
        grossBaseUnitPrice = grossUnitPrice;
        priceStd = netUnitPrice;
      } else {
        grossBaseUnitPrice = PriceAdjustment.calculatePriceStd(order, product, qtyOrdered,
            grossUnitPrice);
        BigDecimal baseGrossAmount = grossBaseUnitPrice.multiply(qtyOrdered).setScale(stdPrecision,
            RoundingMode.HALF_UP);
        priceStd = FinancialUtils.calculateNetFromGross(strTaxId, baseGrossAmount, pricePrecision,
            taxBaseAmt, qtyOrdered);
      }

      resultado.append("new Array(\"inpgrosspricestd\", " + grossBaseUnitPrice.toString() + "),");

      //if (strClientpolineId == null || strClientpolineId.equals("")) {
        resultado.append("new Array(\"inppriceactual\"," + priceActual.toString() + "),");
        resultado.append("new Array(\"inppricelist\"," + netUnitPrice.toString() + "),");
      //}
      resultado.append("new Array(\"inppricelimit\", " + netUnitPrice.toString() + "),");
      resultado.append("new Array(\"inppricestd\"," + priceStd.toString() + "),");
    }

    // calculating discount
    if ((isSOTrx.equals("Y") && (strChanged.equals("inppricelist") || strChanged
        .equals("inpemSsaTaxpriceactual")))
        || strChanged.equals("inppriceactual")
        || strChanged.equals("inplinenetamt")
        || strChanged.equals("inpgrosspricelist")
        || strChanged.equals("inpgrossUnitPrice")) { // strChanged.equals("inpqtyordered")
      BigDecimal priceList = BigDecimal.ZERO;
      BigDecimal unitPrice = BigDecimal.ZERO;
      BigDecimal discount;

      System.out.println("calculate discountttttttttttt 1");
      if (isTaxIncludedPriceList) {
        priceList = grossPriceList;
        unitPrice = grossBaseUnitPrice;
      } else {
        priceList = netPriceList;
        unitPrice = priceStd;
      }

      if (priceList.compareTo(BigDecimal.ZERO) == 0) {
        discount = ZERO;
      } else {
        log4j.debug("pricelist:" + priceList.toString());
        log4j.debug("unit price:" + unitPrice.toString());
        discount = priceList.subtract(unitPrice).multiply(new BigDecimal("100"))
            .divide(priceList, stdPrecision, BigDecimal.ROUND_HALF_EVEN);
        System.out.println("calculate discountttttttttttt 2:  priceList:" + priceList
            + " - unitPrice:" + unitPrice + " - discount:" + discount);

      }
      log4j.debug("Discount rounded: " + discount.toString());
      //if (strClientpolineId == null || strClientpolineId.equals("")) {
        System.out.println("calculate discountttttttttttt 3:" + discount);
        resultado.append("new Array(\"inpdiscount\", " + discount.toString() + "),");
      //}

    } else if ((isSOTrx.equals("N") && strChanged.equals("inppricelist"))) { // calculate std and
                                                                             // actual
      BigDecimal origDiscount = null;
      BigDecimal priceList;
      if (isTaxIncludedPriceList) {
        priceList = grossPriceList;
      } else {
        priceList = netPriceList;
      }

      if (priceList.compareTo(BigDecimal.ZERO) != 0) {
        BigDecimal baseUnitPrice = BigDecimal.ZERO;
        if (isTaxIncludedPriceList) {
          baseUnitPrice = grossBaseUnitPrice;
        } else {
          baseUnitPrice = priceStd;
        }
        origDiscount = priceList.subtract(baseUnitPrice).multiply(new BigDecimal("100"))
            .divide(priceList, stdPrecision, BigDecimal.ROUND_HALF_UP);
      } else {
        origDiscount = BigDecimal.ZERO;
      }
      BigDecimal newDiscount = (strDiscount.equals("") ? ZERO : new BigDecimal(strDiscount)
          .setScale(stdPrecision, BigDecimal.ROUND_HALF_UP));

      if (origDiscount.compareTo(newDiscount) != 0) {
        BigDecimal baseUnitPrice = priceList.subtract(
            priceList.multiply(newDiscount).divide(new BigDecimal("100"))).setScale(pricePrecision,
            BigDecimal.ROUND_HALF_UP);
        if (isTaxIncludedPriceList) {
          grossUnitPrice = PriceAdjustment.calculatePriceActual(order, product, qtyOrdered,
              baseUnitPrice);
          resultado.append("new Array(\"inpgrosspricestd\", " + baseUnitPrice.toString() + "),");
          resultado.append("new Array(\"inpgrossUnitPrice\", " + grossUnitPrice.toString() + "),");

          // set also net prices
          BigDecimal grossAmount = grossUnitPrice.multiply(qtyOrdered).setScale(stdPrecision,
              RoundingMode.HALF_UP);

          final BigDecimal netUnitPrice = FinancialUtils.calculateNetFromGross(strTaxId,
              grossAmount, pricePrecision, taxBaseAmt, qtyOrdered);

          priceStd = netUnitPrice;
        } else {
          priceStd = baseUnitPrice;
        }

        if (!cancelPriceAd) {
          priceActual = PriceAdjustment.calculatePriceActual(order, product, qtyOrdered, priceStd);
        } else {
          priceActual = priceStd;
        }
        //if (strClientpolineId == null || strClientpolineId.equals("")) {
          resultado.append("new Array(\"inppriceactual\", " + priceActual.toString() + "),");
        //}
        resultado.append("new Array(\"inppricestd\", " + priceStd.toString() + "),");
      }
    } else if (strChanged.equals("inpdiscount")) { // calculate std and actual
      System.out.println("enter SL_Order_Amt inpdiscount");
      BigDecimal origDiscount = null;
      BigDecimal priceList;
      if (isTaxIncludedPriceList) {
        priceList = grossPriceList;
      } else {
        priceList = netPriceList;
      }

      if (priceList.compareTo(BigDecimal.ZERO) != 0) {
        BigDecimal baseUnitPrice = BigDecimal.ZERO;
        if (isTaxIncludedPriceList) {
          baseUnitPrice = grossBaseUnitPrice;
        } else {
          baseUnitPrice = priceStd;
        }
        origDiscount = priceList.subtract(baseUnitPrice).multiply(new BigDecimal("100"))
            .divide(priceList, stdPrecision, BigDecimal.ROUND_HALF_UP);
      } else {
        origDiscount = BigDecimal.ZERO;
      }
      BigDecimal newDiscount = (strDiscount.equals("") ? ZERO : new BigDecimal(strDiscount)
          .setScale(stdPrecision, BigDecimal.ROUND_HALF_UP));

      if (origDiscount.compareTo(newDiscount) != 0) {
        BigDecimal baseUnitPrice = priceList.subtract(
            priceList.multiply(newDiscount).divide(new BigDecimal("100"))).setScale(pricePrecision,
            BigDecimal.ROUND_HALF_UP);
        if (isTaxIncludedPriceList) {
          grossUnitPrice = PriceAdjustment.calculatePriceActual(order, product, qtyOrdered,
              baseUnitPrice);
          resultado.append("new Array(\"inpgrosspricestd\", " + baseUnitPrice.toString() + "),");
          resultado.append("new Array(\"inpgrossUnitPrice\", " + grossUnitPrice.toString() + "),");

          // set also net prices
          BigDecimal grossAmount = grossUnitPrice.multiply(qtyOrdered).setScale(stdPrecision,
              RoundingMode.HALF_UP);

          final BigDecimal netUnitPrice = FinancialUtils.calculateNetFromGross(strTaxId,
              grossAmount, pricePrecision, taxBaseAmt, qtyOrdered);

          priceStd = netUnitPrice;
        } else {
          priceStd = baseUnitPrice;
        }

        if (!cancelPriceAd) {
          priceActual = PriceAdjustment.calculatePriceActual(order, product, qtyOrdered, priceStd);
        } else {
          priceActual = priceStd;
        }

        if (isSOTrx.equals("Y") && newDiscount.compareTo(BigDecimal.ZERO) > 0) {
          priceStd = priceStd.setScale(stdPrecision, RoundingMode.HALF_UP);
          priceActual = priceActual.setScale(stdPrecision, RoundingMode.HALF_UP);
        }

        //if (strClientpolineId == null || strClientpolineId.equals("")) {
          resultado.append("new Array(\"inppriceactual\", " + priceActual.toString() + "),");
          System.out.println("Enter SL_Order_Amt  taxxxxx211111");

          // Calculate taxpriceactual from priceactual and tax
          TaxRate taxrate = OBDal.getInstance().get(TaxRate.class, strTaxId);
          if (taxrate != null) {
            BigDecimal rate = taxrate.getRate();
            if (rate.compareTo(new BigDecimal(0)) == 0) {
              taxpriceactual = priceActual;
            } else {
              taxpriceactual = priceActual.add(priceActual.multiply(rate).divide(
                  new BigDecimal(100), order.getCurrency().getPricePrecision().intValue(),
                  BigDecimal.ROUND_HALF_UP));
            }
            System.out.println("inpemSsaTaxpriceactual:" + taxpriceactual);

            resultado
                .append("new Array(\"inpemSsaTaxpriceactual\", \"" + taxpriceactual + "\"),\n");

          }
        //}
        resultado.append("new Array(\"inppricestd\", " + priceStd.toString() + "),");
      }
    }

    if (isSOTrx.equals("Y") && !strStockSecurity.equals("0")
        && qtyOrdered.compareTo(BigDecimal.ZERO) != 0) {
      if (strEnforceAttribute.equals("N")) {
        strStockNoAttribute = SLOrderStockData.totalStockNoAttribute(this, strProduct, strUOM);
        stockNoAttribute = new BigDecimal(strStockNoAttribute);
        resultStock = stockNoAttribute.subtract(qtyOrdered);
        if (stockSecurity.compareTo(resultStock) > 0) {
          resultado
              .append("new Array('MESSAGE', \""
                  + FormatUtilities.replaceJS(Utility.messageBD(this, "StockLimit",
                      vars.getLanguage())) + "\"),");
        }
      } else if (!strAttribute.equals("") && strAttribute != null) {
        strStockAttribute = SLOrderStockData.totalStockAttribute(this, strProduct, strUOM,
            strAttribute);
        stockAttribute = new BigDecimal(strStockAttribute);
        resultStock = stockAttribute.subtract(qtyOrdered);
        if (stockSecurity.compareTo(resultStock) > 0) {
          resultado
              .append("new Array('MESSAGE', \""
                  + FormatUtilities.replaceJS(Utility.messageBD(this, "StockLimit",
                      vars.getLanguage())) + "\"),");
        }
      }
    }
    log4j.debug(resultado.toString());
    if (!strChanged.equals("inpqtyordered") || strChanged.equals("inplinenetamt")) {
      // Check PriceLimit
      boolean enforced = SLOrderAmtData.listPriceType(this, strPriceList);
      // Check Price Limit?
      if (enforced && priceLimit.compareTo(BigDecimal.ZERO) != 0
          && priceActual.compareTo(priceLimit) < 0) {
        resultado.append("new Array('MESSAGE', \""
            + Utility.messageBD(this, "UnderLimitPrice", vars.getLanguage()) + "\")");
      }
    }

    // if net unit price changed then modify tax inclusive unit price
    if (strChanged.equals("inppriceactual")) {
      priceActual = new BigDecimal(strPriceActual.trim());
      log4j.debug("Net unit price results: " + resultado.toString());
    }

    // Multiply
    if (cancelPriceAd) {
      lineNetAmt = qtyOrdered.multiply(priceStd);
    } else {
      if (!strChanged.equals("inplinenetamt")) {
        lineNetAmt = qtyOrdered.multiply(priceActual);
        if (lineNetAmt.scale() > stdPrecision)
          lineNetAmt = lineNetAmt.setScale(stdPrecision, BigDecimal.ROUND_HALF_UP);
      }
    }
    if (strChanged.equals("inplinenetamt")) {
      //if (strClientpolineId == null || strClientpolineId.equals("")) {
        resultado.append("new Array(\"inppriceactual\", " + priceActual.toString() + "),");
      //}
      resultado.append("new Array(\"inptaxbaseamt\", " + lineNetAmt.toString() + "),");
    }
    if (!strChanged.equals("inplinenetamt") || priceActual.compareTo(BigDecimal.ZERO) == 0) {
      resultado.append("new Array(\"inplinenetamt\", " + lineNetAmt.toString() + "),");
    }
    if (!strChanged.equals("inplineGrossAmount")) {
      BigDecimal grossLineAmt = grossUnitPrice.multiply(qtyOrdered).setScale(stdPrecision,
          BigDecimal.ROUND_HALF_UP);
      resultado.append("new Array(\"inplineGrossAmount\", " + grossLineAmt.toString() + "),");
    }
    resultado.append("new Array(\"inptaxbaseamt\", " + lineNetAmt.toString() + "),");

    if (strChanged.equals("inpcTaxId")) {
      System.out.println("tax changinggggg");
      System.out.println("Enter SL_Order_Amt  taxxxxx2");

      // Calculate taxpriceactual from priceactual and tax
      TaxRate taxrate = OBDal.getInstance().get(TaxRate.class, strTaxId);
      if (taxrate != null) {
        BigDecimal rate = taxrate.getRate();
        if (rate.compareTo(new BigDecimal(0)) == 0) {
          taxpriceactual = priceActual;
        } else {
          taxpriceactual = priceActual.add(priceActual.multiply(rate).divide(new BigDecimal(100),
              order.getCurrency().getPricePrecision().intValue(), BigDecimal.ROUND_HALF_UP));
        }
        System.out.println("inpemSsaTaxpriceactual:" + taxpriceactual);

        resultado.append("new Array(\"inpemSsaTaxpriceactual\", \"" + taxpriceactual + "\"),\n");

      }
    }

    resultado.append("new Array(\"dummy\", \"\" )");

    resultado.append(");");
    xmlDocument.setParameter("array", resultado.toString());
    log4j.debug("Callout for field changed: " + strChanged + " is " + resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
