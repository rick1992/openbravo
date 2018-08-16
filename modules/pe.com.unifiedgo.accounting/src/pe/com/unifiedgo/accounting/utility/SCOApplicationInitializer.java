/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */

package pe.com.unifiedgo.accounting.utility;

import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;
import org.openbravo.client.kernel.ApplicationInitializer;
import org.openbravo.dal.service.OBDal;

public class SCOApplicationInitializer implements ApplicationInitializer {

  @Override
  public void initialize() {
    OBDal.getInstance().registerSQLFunction("sco_doctypename_get", new StandardSQLFunction("sco_doctypename_get", StandardBasicTypes.STRING));
    OBDal.getInstance().registerSQLFunction("sco_getglitem_reqs", new StandardSQLFunction("sco_getglitem_reqs", StandardBasicTypes.STRING));
    OBDal.getInstance().registerSQLFunction("ad_get_org_le_bu", new StandardSQLFunction("ad_get_org_le_bu", StandardBasicTypes.STRING));

  }

}
