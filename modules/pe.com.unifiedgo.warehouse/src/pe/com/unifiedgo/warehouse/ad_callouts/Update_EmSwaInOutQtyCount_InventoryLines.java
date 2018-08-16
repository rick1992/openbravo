package pe.com.unifiedgo.warehouse.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

public class Update_EmSwaInOutQtyCount_InventoryLines extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    // Enumeration<String> params = info.vars.getParameterNames();
    // while (params.hasMoreElements()) {
    // System.out.println(params.nextElement());
    // }

    String strIsOutTrx = info.vars.getStringParameter("inpemSwaIsouttrx");
    if (strIsOutTrx == null)
      return;

    Integer newQtyCount = 0;
    String qtyBook = info.vars.getNumericParameter("inpqtybook");
    String inOutqtyCount = info.vars.getNumericParameter("inpemSwaInoutqtycount");
    if (strIsOutTrx.equals("Y")) {
      newQtyCount = Integer.parseInt(qtyBook) - Integer.parseInt(inOutqtyCount);
      info.addResult("inpqtycount", newQtyCount.toString());

    } else if (strIsOutTrx.equals("N")) {
      newQtyCount = Integer.parseInt(qtyBook) + Integer.parseInt(inOutqtyCount);
      info.addResult("inpqtycount", newQtyCount.toString());
    }

  }
}
