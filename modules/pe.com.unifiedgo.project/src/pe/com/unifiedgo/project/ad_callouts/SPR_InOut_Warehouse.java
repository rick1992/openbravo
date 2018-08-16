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
package pe.com.unifiedgo.project.ad_callouts;

import java.util.List;

import javax.servlet.ServletException;

import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.project.Project;

public class SPR_InOut_Warehouse extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    // Enumeration<String> params = info.vars.getParameterNames();
    // while (params.hasMoreElements()) {
    // System.out.println(params.nextElement());
    // }

    String mWarehouseId = info.vars.getStringParameter("inpmWarehouseId");

    Warehouse warehouse = OBDal.getInstance().get(Warehouse.class, mWarehouseId);
    if (warehouse != null) {
      OBCriteria<Project> projectFilter = OBDal.getInstance().createCriteria(Project.class);
      projectFilter.add(Restrictions.eq(Project.PROPERTY_WAREHOUSE, warehouse));
      projectFilter.add(Restrictions.eq(Project.PROPERTY_CLIENT, warehouse.getClient()));
      projectFilter.setMaxResults(1);
      List<Project> projects = projectFilter.list();
      if (projects.size() > 0) {
        info.addResult("inpcProjectId", projects.get(0).getId());
      } else {
        info.addResult("inpcProjectId", "");
      }
    } else {
      info.addResult("inpcProjectId", "");
    }

  }

}
