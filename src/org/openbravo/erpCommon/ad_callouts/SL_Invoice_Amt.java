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
 * All portions are Copyright (C) 2001-2013 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.businessUtility.PriceAdjustment;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.financial.FinancialUtils;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.core.data.SCRComboCategory;
import pe.com.unifiedgo.core.data.SCRComboItem;

public class SL_Invoice_Amt extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  private static final BigDecimal ZERO = new BigDecimal(0.0);

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strChanged = vars.getStringParameter("inpLastFieldChanged");
      if (log4j.isDebugEnabled())
        log4j.debug("CHANGED: " + strChanged);
      String strQtyInvoice = vars.getNumericParameter("inpqtyinvoiced");
      String strPriceActual = vars.getNumericParameter("inppriceactual");
      String strTaxPriceActual = vars.getNumericParameter("inpemSsaTaxpriceactual");
      String strPriceLimit = vars.getNumericParameter("inppricelimit");
      String strInvoiceId = vars.getStringParameter("inpcInvoiceId");
      String strProduct = vars.getStringParameter("inpmProductId");
      String strTabId = vars.getStringParameter("inpTabId");
      String strPriceList = vars.getNumericParameter("inppricelist");
      String strPriceStd = vars.getNumericParameter("inppricestd");
      String strLineNetAmt = vars.getNumericParameter("inplinenetamt");
      String strTaxId = vars.getStringParameter("inpcTaxId");
      String strGrossUnitPrice = vars.getNumericParameter("inpgrossUnitPrice");
      String strBaseGrossUnitPrice = vars.getNumericParameter("inpgrosspricestd");
      String strtaxbaseamt = vars.getNumericParameter("inptaxbaseamt");
      String strWindowId = vars.getStringParameter("inpwindowId");
      String strFinancialInvoiceLine = vars.getStringParameter("inpfinancialInvoiceLine");
      String strISOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);

      try {
        printPage(response, vars, strChanged, strQtyInvoice, strPriceActual, strInvoiceId, strProduct, strPriceLimit, strTabId, strPriceList, strPriceStd, strLineNetAmt, strTaxId, strGrossUnitPrice, strBaseGrossUnitPrice, strtaxbaseamt, strISOTrx, strTaxPriceActual, strFinancialInvoiceLine);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  void printPage(HttpServletResponse response, VariablesSecureApp vars, String strChanged, String strQtyInvoice, String strPriceActual, String strInvoiceId, String strProduct, String strPriceLimit, String strTabId, String strPriceList, String strPriceStd, String strLineNetAmt, String strTaxId, String strGrossUnitPrice, String strBaseGrossUnitPrice, String strTaxBaseAmt, String strISOTrx, String strTaxPriceActual, String strFinancialInvoiceLine) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    SLInvoiceAmtData[] data = SLInvoiceAmtData.select(this, strInvoiceId);
    String strPrecision = "0", strPricePrecision = "0";
    boolean enforcedLimit = false;
    if (data != null && data.length > 0) {
      strPrecision = data[0].stdprecision.equals("") ? "0" : data[0].stdprecision;
      strPricePrecision = data[0].priceprecision.equals("") ? "0" : data[0].priceprecision;
      enforcedLimit = (data[0].enforcepricelimit.equals("Y") ? true : false);
    }
    int StdPrecision = Integer.valueOf(strPrecision).intValue();
    int PricePrecision = Integer.valueOf(strPricePrecision).intValue();

    SLInvoiceTaxAmtData[] dataTax = SLInvoiceTaxAmtData.select(this, strTaxId, strInvoiceId);
    BigDecimal taxRate = BigDecimal.ZERO;
    Integer taxScale = new Integer(0);
    if (dataTax.length > 0) {
      taxRate = (dataTax[0].rate.equals("") ? new BigDecimal(1) : new BigDecimal(dataTax[0].rate));
      taxScale = new Integer(dataTax[0].priceprecision);
    }
    if (log4j.isDebugEnabled())
      log4j.debug("strPriceActual: " + strPriceActual);
    if (log4j.isDebugEnabled())
      log4j.debug("strPriceLimit: " + strPriceLimit);
    if (log4j.isDebugEnabled())
      log4j.debug("strLineNetAmt: " + strLineNetAmt);
    if (log4j.isDebugEnabled())
      log4j.debug("taxRate: " + taxRate);

    BigDecimal qtyInvoice, priceActual, lineNetAmt, priceLimit, priceStd, taxBaseAmt, taxpriceactual;

    qtyInvoice = (!Utility.isBigDecimal(strQtyInvoice) ? ZERO : new BigDecimal(strQtyInvoice));
    priceStd = (!Utility.isBigDecimal(strPriceStd) ? ZERO : new BigDecimal(strPriceStd));
    priceActual = (!Utility.isBigDecimal(strPriceActual) ? ZERO : (new BigDecimal(strPriceActual))).setScale(PricePrecision, BigDecimal.ROUND_HALF_UP);
    taxpriceactual = (!Utility.isBigDecimal(strTaxPriceActual) ? ZERO : (new BigDecimal(strTaxPriceActual))).setScale(PricePrecision, BigDecimal.ROUND_HALF_UP);
    priceLimit = (!Utility.isBigDecimal(strPriceLimit) ? ZERO : (new BigDecimal(strPriceLimit))).setScale(PricePrecision, BigDecimal.ROUND_HALF_UP);
    lineNetAmt = (!Utility.isBigDecimal(strLineNetAmt) ? ZERO : new BigDecimal(strLineNetAmt));
    taxBaseAmt = (strTaxBaseAmt.equals("") ? ZERO : (new BigDecimal(strTaxBaseAmt))).setScale(PricePrecision, BigDecimal.ROUND_HALF_UP);

    Invoice invoice = OBDal.getInstance().get(Invoice.class, strInvoiceId);
    Product product = OBDal.getInstance().get(Product.class, strProduct);
    boolean priceIncludeTaxes = invoice.getPriceList().isPriceIncludesTax();
    boolean returnvendorline = false;
    if (invoice.getTransactionDocument().getScoSpecialdoctype() != null && invoice.getTransactionDocument().getScoSpecialdoctype().compareTo("SCOAPCREDITMEMO") == 0 && strFinancialInvoiceLine.compareTo("Y") != 0) {
      returnvendorline = true;
    }

    StringBuffer resultado = new StringBuffer();

    resultado.append("var calloutName='SL_Invoice_Amt';\n\n");
    resultado.append("var respuesta = new Array(");

    if (strChanged.equals("inpemSsaTaxpriceactual")) {
      // Calculate priceactual from taxpriceactual and tax
      TaxRate taxrate = OBDal.getInstance().get(TaxRate.class, strTaxId);
      if (taxrate != null) {
        System.out.println("Enter SL_Invoice_Amt  taxxxxx3");
        BigDecimal rate = taxrate.getRate();
        if (rate.compareTo(new BigDecimal(0)) == 0) {
          priceActual = taxpriceactual;
        } else {
          priceActual = (taxpriceactual.multiply(new BigDecimal(100))).divide((rate.add(new BigDecimal(100))), PricePrecision, BigDecimal.ROUND_HALF_UP);
        }
        System.out.println("inppriceactual:" + priceActual.toString());
        resultado.append("new Array(\"inppriceactual\", \"" + priceActual.toString() + "\"),");

      }
    }

    if (strChanged.equals("inplinenetamt")) {
      if (qtyInvoice.compareTo(BigDecimal.ZERO) == 0) {
        priceActual = BigDecimal.ZERO;
      } else {
        priceActual = lineNetAmt.divide(qtyInvoice, PricePrecision, BigDecimal.ROUND_HALF_UP);
      }
    }
    if (priceActual.compareTo(BigDecimal.ZERO) == 0) {
      lineNetAmt = BigDecimal.ZERO;
    }
    // If unit price (actual price) changes, recalculates standard price
    // (std price) applying price adjustments (offers) if any
    if (strChanged.equals("inppriceactual") || strChanged.equals("inplinenetamt") || strChanged.equals("inpemSsaTaxpriceactual")) {
      if (log4j.isDebugEnabled())
        log4j.debug("priceActual:" + Double.toString(priceActual.doubleValue()));

      priceStd = PriceAdjustment.calculatePriceStd(invoice, product, qtyInvoice, priceActual);
      resultado.append("new Array(\"inppricestd\", " + priceStd.toString() + "),");
      resultado.append("new Array(\"inptaxbaseamt\", " + priceActual.multiply(qtyInvoice) + "),");
    }

    // If quantity changes, recalculates unit price (actual price) applying
    // price adjustments (offers) if any
    if (strChanged.equals("inpqtyinvoiced")) {
      if (log4j.isDebugEnabled())
        log4j.debug("strPriceList: " + strPriceList.replace("\"", "") + " product:" + strProduct + " qty:" + qtyInvoice.toString());

      if (priceIncludeTaxes) {
        BigDecimal baseGrossUnitPrice = new BigDecimal(strBaseGrossUnitPrice.trim());
        BigDecimal grossUnitPrice = PriceAdjustment.calculatePriceActual(invoice, product, qtyInvoice, baseGrossUnitPrice);
        BigDecimal grossAmount = grossUnitPrice.multiply(new BigDecimal(strQtyInvoice.trim()));
        priceActual = FinancialUtils.calculateNetFromGross(strTaxId, grossAmount, invoice.getCurrency().getPricePrecision().intValue(), taxBaseAmt, qtyInvoice);
        resultado.append("new Array(\"inpgrossUnitPrice\", " + grossUnitPrice.toString() + "),");
        resultado.append("new Array(\"inplineGrossAmount\", " + grossAmount.toString() + "),");
      } else {
        priceActual = PriceAdjustment.calculatePriceActual(invoice, product, qtyInvoice, priceStd);

        System.out.println("Enter SL_Order_Amt  taxxxxx2");

        // Calculate taxpriceactual from priceactual and tax
        TaxRate taxrate = OBDal.getInstance().get(TaxRate.class, strTaxId);
        if (taxrate != null) {
          BigDecimal rate = taxrate.getRate();
          if (rate.compareTo(new BigDecimal(0)) == 0) {
            taxpriceactual = priceActual;
          } else {
            taxpriceactual = priceActual.add(priceActual.multiply(rate).divide(new BigDecimal(100), PricePrecision, BigDecimal.ROUND_HALF_UP));
          }
          System.out.println("inpemSsaTaxpriceactual:" + taxpriceactual);

          resultado.append("new Array(\"inpemSsaTaxpriceactual\", \"" + taxpriceactual + "\"),");

        }
      }
    }
    // if taxRate field is changed
    if (strChanged.equals("inpgrossUnitPrice") || (strChanged.equals("inpcTaxId") && priceIncludeTaxes)) {
      BigDecimal grossUnitPrice = new BigDecimal(strGrossUnitPrice.trim());
      BigDecimal baseGrossUnitPrice = PriceAdjustment.calculatePriceStd(invoice, product, qtyInvoice, grossUnitPrice);
      BigDecimal grossAmount = grossUnitPrice.multiply(qtyInvoice);
      BigDecimal netUnitPrice = FinancialUtils.calculateNetFromGross(strTaxId, grossAmount, PricePrecision, taxBaseAmt, qtyInvoice);
      priceActual = netUnitPrice;
      priceStd = netUnitPrice;

      resultado.append("new Array(\"inpgrosspricestd\", " + baseGrossUnitPrice.toString() + "),");

      resultado.append("new Array(\"inppriceactual\"," + netUnitPrice.toString() + "),");
      resultado.append("new Array(\"inppricelimit\", " + netUnitPrice.toString() + "),");
      resultado.append("new Array(\"inppricestd\", " + netUnitPrice.toString() + "),");

      // if taxinclusive field is changed then modify net unit price and gross price
      if (strChanged.equals("inpgrossUnitPrice")) {
        resultado.append("new Array(\"inplineGrossAmount\"," + grossAmount.toString() + "),");
      }
    }

    if (!strChanged.equals("inplinenetamt")) {
      if (returnvendorline) {
        // Net amount of a line equals quantity x unit price (actual price) x -1 (return from
        // vendor)
        lineNetAmt = qtyInvoice.multiply(priceActual).multiply(new BigDecimal(-1));
      } else {
        // Net amount of a line equals quantity x unit price (actual price)
        lineNetAmt = qtyInvoice.multiply(priceActual);
      }
    }

    if (strChanged.equals("inplinenetamt")) {
      DecimalFormat priceEditionFmt = Utility.getFormat(vars, "priceEdition");
      DecimalFormat euroEditionFmt = Utility.getFormat(vars, "euroEdition");
      BigDecimal CalculatedLineNetAmt = qtyInvoice.multiply(priceActual.setScale(priceEditionFmt.getMaximumFractionDigits(), BigDecimal.ROUND_HALF_UP)).setScale(euroEditionFmt.getMaximumFractionDigits(), BigDecimal.ROUND_HALF_UP);
      if (!lineNetAmt.setScale(priceEditionFmt.getMaximumFractionDigits(), BigDecimal.ROUND_HALF_UP).equals(CalculatedLineNetAmt)) {
        StringBuffer strMessage = new StringBuffer(Utility.messageBD(this, "NotCorrectAmountProvided", vars.getLanguage()));
        strMessage.append(": ");
        strMessage.append((strLineNetAmt.equals("") ? BigDecimal.ZERO : new BigDecimal(strLineNetAmt)));
        strMessage.append(". ");
        strMessage.append(Utility.messageBD(this, "CosiderUsing", vars.getLanguage()));
        strMessage.append(" " + CalculatedLineNetAmt);
        resultado.append("new Array('MESSAGE', \"" + strMessage.toString() + "\"),");
      }
    }

    if (lineNetAmt.scale() > StdPrecision)
      lineNetAmt = lineNetAmt.setScale(StdPrecision, BigDecimal.ROUND_HALF_UP);

    // Check price limit
    if (enforcedLimit) {
      if (priceLimit.compareTo(BigDecimal.ZERO) != 0 && priceActual.compareTo(priceLimit) < 0)
        resultado.append("new Array('MESSAGE', \"" + FormatUtilities.replaceJS(Utility.messageBD(this, "UnderLimitPrice", vars.getLanguage())) + "\"), ");
    }
    BigDecimal taxAmt = ((lineNetAmt.multiply(taxRate)).divide(new BigDecimal("100"), 12, BigDecimal.ROUND_HALF_EVEN)).setScale(taxScale, BigDecimal.ROUND_HALF_UP);

    String scoSpecialTax = "";
    if (strTaxId != null || !"".equals(strTaxId)) {
      TaxRate tax = OBDal.getInstance().get(TaxRate.class, strTaxId);
      if (tax != null) {
        scoSpecialTax = tax.getScoSpecialtax();
      }
    }
    resultado.append("new Array(\"inpemScrSpecialtax\", \"" + scoSpecialTax + "\"),");
    resultado.append("new Array(\"inpemScrComboItemId\", \"\"),");

    if (!strChanged.equals("inplinenetamt") || lineNetAmt.compareTo(BigDecimal.ZERO) == 0)
      resultado.append("new Array(\"inplinenetamt\", " + lineNetAmt.toString() + "),");
    resultado.append("new Array(\"inptaxbaseamt\", " + lineNetAmt.toString() + "),");
    resultado.append("new Array(\"inptaxamt\", " + taxAmt.toPlainString() + "),");
    resultado.append("new Array(\"inppriceactual\", " + priceActual.toString() + "),");

    if (strChanged.equals("inpcTaxId")) {
      System.out.println("tax changinggggg");
      System.out.println("Enter SL_Invoice_Amt  taxxxxx2");

      // Calculate taxpriceactual from priceactual and tax
      TaxRate taxrate = OBDal.getInstance().get(TaxRate.class, strTaxId);
      if (taxrate != null) {
        BigDecimal rate = taxrate.getRate();
        if (rate.compareTo(new BigDecimal(0)) == 0) {
          taxpriceactual = priceActual;
        } else {
          taxpriceactual = priceActual.add(priceActual.multiply(rate).divide(new BigDecimal(100), PricePrecision, BigDecimal.ROUND_HALF_UP));
        }
        System.out.println("inpemSsaTaxpriceactual:" + taxpriceactual);

        resultado.append("new Array(\"inpemSsaTaxpriceactual\", \"" + taxpriceactual + "\"),");

      }
    }

    TaxRate tax = OBDal.getInstance().get(TaxRate.class, strTaxId);
    if (tax != null) {
      String specialTaxRate = tax.getScoSpecialtax();
      if (specialTaxRate != null) {
        if (strISOTrx.equals("Y")) {

          if (specialTaxRate.equals("SCOEXEMPT")) {
            final StringBuilder whereClause = new StringBuilder();
            whereClause.append(" as cmbi ");
            whereClause.append(" where cmbi.");
            whereClause.append(SCRComboItem.PROPERTY_COMBOCATEGORY);
            whereClause.append(".");
            whereClause.append(SCRComboCategory.PROPERTY_SEARCHKEY);
            whereClause.append(" ='docVentaTaxExcento'");
            whereClause.append(" order by cmbi.");
            whereClause.append(SCRComboItem.PROPERTY_SEQUENCENUMBER);
            final OBQuery<SCRComboItem> obqComboItem = OBDal.getInstance().createQuery(SCRComboItem.class, whereClause.toString());
            List<SCRComboItem> comboitems = obqComboItem.list();

            if (comboitems.size() > 0) {
              resultado.append("new Array(\"inpemScrComboItemId\", \"" + comboitems.get(0).getId() + "\")");
            } else {
              resultado.append("new Array(\"inpemScrComboItemId\", \"\")");
            }
          } else {
            resultado.append("new Array(\"inpemScrComboItemId\", \"\")");
          }
        } else {

          if (specialTaxRate.equals("SCOEXEMPT")) {
            final StringBuilder whereClause = new StringBuilder();
            whereClause.append(" as cmbi ");
            whereClause.append(" where cmbi.");
            whereClause.append(SCRComboItem.PROPERTY_COMBOCATEGORY);
            whereClause.append(".");
            whereClause.append(SCRComboCategory.PROPERTY_SEARCHKEY);
            whereClause.append(" ='docCompraTaxExcento'");
            whereClause.append(" order by cmbi.");
            whereClause.append(SCRComboItem.PROPERTY_SEQUENCENUMBER);
            final OBQuery<SCRComboItem> obqComboItem = OBDal.getInstance().createQuery(SCRComboItem.class, whereClause.toString());
            List<SCRComboItem> comboitems = obqComboItem.list();

            if (comboitems.size() > 0) {
              resultado.append("new Array(\"inpemScrComboItemId\", \"" + comboitems.get(0).getId() + "\")");
            } else {
              resultado.append("new Array(\"inpemScrComboItemId\", \"\")");
            }
          } else if (specialTaxRate.equals("SCOIGV")) {
            final StringBuilder whereClause = new StringBuilder();
            whereClause.append(" as cmbi ");
            whereClause.append(" where cmbi.");
            whereClause.append(SCRComboItem.PROPERTY_COMBOCATEGORY);
            whereClause.append(".");
            whereClause.append(SCRComboCategory.PROPERTY_SEARCHKEY);
            whereClause.append(" ='docCompraTaxIGV'");
            whereClause.append(" order by cmbi.");
            whereClause.append(SCRComboItem.PROPERTY_SEQUENCENUMBER);
            final OBQuery<SCRComboItem> obqComboItem = OBDal.getInstance().createQuery(SCRComboItem.class, whereClause.toString());
            List<SCRComboItem> comboitems = obqComboItem.list();

            if (comboitems.size() > 0) {
              resultado.append("new Array(\"inpemScrComboItemId\", \"" + comboitems.get(0).getId() + "\")");
            } else {
              resultado.append("new Array(\"inpemScrComboItemId\", \"\")");
            }
          } else {
            resultado.append("new Array(\"inpemScrComboItemId\", \"\")");
          }

        }
      } else {
        resultado.append("new Array(\"inpemScrComboItemId\", \"\")");
      }
    } else {
      resultado.append("new Array(\"inpemScrComboItemId\", \"\")");
    }

    resultado.append(");");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
