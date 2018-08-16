package pe.com.unifiedgo.accounting.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

public class SCO_Telecredit_Line_CBpartner extends SimpleCallout {
	private static final long serialVersionUID = 1L;

	@Override
	protected void execute(CalloutInfo info) throws ServletException {

		/*
		 * Enumeration<String> params = info.vars.getParameterNames(); while
		 * (params.hasMoreElements()) {
		 * System.out.println(params.nextElement()); }
		 */

		String C_Bpartner_ID = info.vars.getStringParameter("inpcBpartnerId");
		String paymenttype = info.vars.getStringParameter("inppaymenttype");

		if (C_Bpartner_ID == null) {
			return;
		}

		info.addResult("inpdoctyperefId", "");
		info.addResult("inpinvoicerefId", "");

	}

}
