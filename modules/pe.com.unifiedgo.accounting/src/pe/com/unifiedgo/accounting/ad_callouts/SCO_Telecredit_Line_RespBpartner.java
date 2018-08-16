package pe.com.unifiedgo.accounting.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

public class SCO_Telecredit_Line_RespBpartner extends SimpleCallout {
	private static final long serialVersionUID = 1L;

	@Override
	protected void execute(CalloutInfo info) throws ServletException {

		/*
		 * Enumeration<String> params = info.vars.getParameterNames(); while
		 * (params.hasMoreElements()) {
		 * System.out.println(params.nextElement()); }
		 */

		String C_Respbpartner_ID = info.vars.getStringParameter("inpcRespbpartnerId");
		String paymenttype = info.vars.getStringParameter("inppaymenttype");

		if (C_Respbpartner_ID == null) {
			return;
		}

		info.addResult("inpcBpBankaccountId", "");

	}

}
