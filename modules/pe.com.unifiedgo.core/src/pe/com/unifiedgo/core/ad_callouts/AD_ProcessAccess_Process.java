package pe.com.unifiedgo.core.ad_callouts;

import java.util.Enumeration;

import javax.servlet.ServletException;

import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.service.db.DalConnectionProvider;

public class AD_ProcessAccess_Process extends SimpleCallout {
	private static final long serialVersionUID = 1L;
	private static final RequestFilter filterYesNo = new ValueListFilter("Y", "N");

	@Override
	protected void execute(CalloutInfo info) throws ServletException {

		Enumeration<String> params = info.vars.getParameterNames();
		while (params.hasMoreElements()) {
			System.out.println(params.nextElement());
		}

		final String adProcessId = info.getStringParameter("inpadProcessId", null);
		if (adProcessId == null) {
			info.addResult("inpemScrMainmenuId", "");
			return;
		}
		if (adProcessId.isEmpty()) {
			info.addResult("inpemScrMainmenuId", "");
			return;
		}

		String mainmenu_id = ADProcessAccessProcessData.getMainmenuFromProcess(new DalConnectionProvider(), adProcessId);
		info.addResult("inpemScrMainmenuId", mainmenu_id);

	}

}
