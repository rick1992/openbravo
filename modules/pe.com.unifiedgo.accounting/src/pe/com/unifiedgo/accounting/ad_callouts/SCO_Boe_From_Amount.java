package pe.com.unifiedgo.accounting.ad_callouts;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.servlet.ServletException;

import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.invoice.Invoice;

import pe.com.unifiedgo.accounting.data.SCOBillofexchange;
import pe.com.unifiedgo.accounting.data.SCOPrepayment;

public class SCO_Boe_From_Amount extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    String C_InvoiceRef_ID = info.vars.getStringParameter("inpinvoicerefId");
    String SCO_Prepayment_ID = info.vars.getStringParameter("inpscoPrepaymentId");
    String isprepayment = info.vars.getStringParameter("inpisprepayment");

    String strScoBillOfExchangeId = info.vars.getStringParameter("inpscoBillofexchangeId");
    String strAmount = info.vars.getNumericParameter("inpamount");

    SCOBillofexchange boe = OBDal.getInstance().get(SCOBillofexchange.class,
        strScoBillOfExchangeId);
    if (boe == null) {
      info.addResult("inpconvertedamount", "");
      return;
    }

    Currency currencydoc = null;
    if (isprepayment.equals("Y")) {

      if (SCO_Prepayment_ID.isEmpty()) {
        info.addResult("inpconvertedamount", "");
        return;
      }

      SCOPrepayment prep = OBDal.getInstance().get(SCOPrepayment.class, SCO_Prepayment_ID);

      if (prep == null) {
        info.addResult("inpconvertedamount", "");
        return;
      }

      currencydoc = prep.getCurrency();
    } else {

      if (C_InvoiceRef_ID.isEmpty()) {
        info.addResult("inpconvertedamount", "");
        return;
      }

      Invoice inv = OBDal.getInstance().get(Invoice.class, C_InvoiceRef_ID);

      if (inv == null) {
        info.addResult("inpconvertedamount", "");
        return;
      }

      currencydoc = inv.getCurrency();
    }

    BigDecimal amount = new BigDecimal(strAmount);
    BigDecimal convertedamount = amount;

    if (!currencydoc.getId().equals(boe.getCurrency().getId()))
      convertedamount = amount.multiply(boe.getConvertRate()).setScale(2, RoundingMode.HALF_UP);

    info.addResult("inpconvertedamount", convertedamount);

  }

}
