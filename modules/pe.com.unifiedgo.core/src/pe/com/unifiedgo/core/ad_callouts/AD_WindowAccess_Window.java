package pe.com.unifiedgo.core.ad_callouts;

import java.util.Enumeration;

import javax.servlet.ServletException;

import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.service.db.DalConnectionProvider;

public class AD_WindowAccess_Window extends SimpleCallout {
	private static final long serialVersionUID = 1L;
	private static final RequestFilter filterYesNo = new ValueListFilter("Y", "N");

	@Override
	protected void execute(CalloutInfo info) throws ServletException {

		Enumeration<String> params = info.vars.getParameterNames();
		while (params.hasMoreElements()) {
			System.out.println(params.nextElement());
		}

		final String adWindowId = info.getStringParameter("inpadWindowId", null);
		if (adWindowId == null) {
			info.addResult("inpemScrMainmenuId", "");
			return;
		}
		if (adWindowId.isEmpty()) {
			info.addResult("inpemScrMainmenuId", "");
			return;
		}

		String mainmenu_id = ADWindowAccessWindowData.getMainmenuFromWindow(new DalConnectionProvider(), adWindowId);
		info.addResult("inpemScrMainmenuId", mainmenu_id);

	}

}
