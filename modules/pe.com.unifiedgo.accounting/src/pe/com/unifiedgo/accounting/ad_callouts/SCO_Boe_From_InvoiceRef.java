package pe.com.unifiedgo.accounting.ad_callouts;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

import javax.servlet.ServletException;

import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;

import pe.com.unifiedgo.accounting.SCO_Utils;
import pe.com.unifiedgo.accounting.data.SCOBillofexchange;

public class SCO_Boe_From_InvoiceRef extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */
    DecimalFormat df = new DecimalFormat("#0.00");
    df.setRoundingMode(RoundingMode.HALF_UP);

    String stradOrgId = info.vars.getStringParameter("inpadOrgId");
    String stradClientId = info.vars.getStringParameter("inpadClientId");
    String C_InvoiceRef_ID = info.vars.getStringParameter("inpinvoicerefId");
    String strScoBillofexchangeId = info.vars.getStringParameter("inpscoBillofexchangeId");
    String strSpecialDocType = info.vars.getStringParameter("inpspecialdoctype");
    String strPercentRenewalType = info.vars.getStringParameter("inppercentrenewalType");
    if (C_InvoiceRef_ID == null)
      return;

    Invoice inv = OBDal.getInstance().get(Invoice.class, C_InvoiceRef_ID);
    if (inv == null)
      return;

    Organization org = OBDal.getInstance().get(Organization.class, stradOrgId);
    BusinessPartner bpartner = inv.getBusinessPartner();
    SCOBillofexchange boe = OBDal.getInstance()
        .get(SCOBillofexchange.class, strScoBillofexchangeId);
    Currency pencurrency = OBDal.getInstance().get(Currency.class, "308");

    BigDecimal amount = inv.getOutstandingAmount();
    //WITHHOLDING VALIDATION ARE NOW APPLIED IN THE SCO_BILLOFEXCHANGE_POST FUNCTION
/*    BigDecimal withholdingperc;
    BigDecimal withholdingamt = new BigDecimal(0);
    Organization parentOrg = OBContext.getOBContext().getOrganizationStructureProvider()
        .getLegalEntity(inv.getOrganization());
    boolean apply_withholding = false;

    // IF THE DOCUMENT IS DETRACCTION AFFECTED THEN IT CANNOT BE WITHHOLDED
    if (!inv.isScoIsdetractionAffected()) {
      if (!parentOrg.isScoRetencionagent() && !parentOrg.isScoHasgoodrep()) {
        if (bpartner.isScoRetencionagent()) {
          if (!apply_withholding) {
            // First CASE : Apply withholding if the amount of the invoice >=
            // v_org_withholding_minamt
            BigDecimal convertedamt = SCO_Utils.currencyConvertTable(inv.getGrandTotalAmount(), inv
                .getTransactionDocument().getTable().getId(), inv.getId(), inv.getCurrency(),
                pencurrency, boe.getGeneratedDate(), stradClientId, stradOrgId);
            if (convertedamt != null) {
              if (convertedamt.compareTo(parentOrg.getScoWithholdingMinamt()) >= 0) {
                apply_withholding = true;
              }
            } else {
              info.addResult("WARNING",
                  OBMessageUtils.getI18NMessage("SCO_BOEFromConvertError", null));
            }
          }

          if (!apply_withholding) {
            // Second CASE : Apply withholding if the grandtotal of any of the order related to the
            // invoice >= v_org_withholding_minamt
            BigDecimal orderGrandTotalAmount, orderConvertedamt = BigDecimal.ZERO;
            List<InvoiceLine> invlines = inv.getInvoiceLineList();
            for (int i = 0; i < invlines.size(); i++) {
              if (invlines.get(i).getSalesOrderLine() != null) {
                orderGrandTotalAmount = invlines.get(i).getSalesOrderLine().getSalesOrder()
                    .getGrandTotalAmount();
                BigDecimal tmp_convertedamt = SCO_Utils.currencyConvertTable(orderGrandTotalAmount,
                    inv.getTransactionDocument().getTable().getId(), inv.getId(),
                    inv.getCurrency(), pencurrency, boe.getGeneratedDate(), stradClientId,
                    stradOrgId);
                if (tmp_convertedamt != null) {
                  if (tmp_convertedamt.compareTo(orderConvertedamt) > 0) {
                    orderConvertedamt = tmp_convertedamt;
                  }
                } else {
                  info.addResult("WARNING",
                      OBMessageUtils.getI18NMessage("SCO_BOEFromConvertError", null));
                }
              }
            }
            if (!apply_withholding
                && orderConvertedamt.compareTo(parentOrg.getScoWithholdingMinamt()) >= 0) {
              apply_withholding = true;
            }
          }
        }
      }
    }

    if (apply_withholding) {
      withholdingperc = parentOrg.getScoWithholdingPerc();
      if (withholdingperc == null) {
        withholdingperc = new BigDecimal(3);
      }
      withholdingamt = amount.multiply(withholdingperc).divide(new BigDecimal(100.00))
          .setScale(pencurrency.getStandardPrecision().intValue(), BigDecimal.ROUND_HALF_UP);
      amount = amount.subtract(withholdingamt);
      info.addResult("inpwithholdingamt", withholdingamt);

    } else {
      info.addResult("inpwithholdingamt", new BigDecimal(0));
    }*/

    // Renovaciones / Refinanciaciones
    if ("SCOBOEXBOE".equals(strSpecialDocType)) {
      if (!"".equals(strPercentRenewalType) && strPercentRenewalType != null) {
        if ("SCO_Renewal30Perc".equals(strPercentRenewalType)) {
          amount = new BigDecimal(30.0 / 100.0).multiply(amount);
        } else if ("SCO_Renewal40Perc".equals(strPercentRenewalType)) {
          amount = new BigDecimal(40.0 / 100.0).multiply(amount);
        } else if ("SCO_Renewal50Perc".equals(strPercentRenewalType)) {
          amount = new BigDecimal(50.0 / 100.0).multiply(amount);
        }
      }
    }
    info.addResult("inpamount", amount);

  }
}
