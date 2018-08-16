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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

//Sqlc generated V1.O00-1
package it.extrasys.utility.report.common;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.data.FieldProvider;
import org.openbravo.data.UtilSql;
import org.openbravo.database.ConnectionProvider;

public class EmailDefinitionData implements FieldProvider {
  static Logger log4j = Logger.getLogger(EmailDefinitionData.class);
  private String InitRecordNumber = "0";
  public String position;
  public String adLanguage;
  public String subject;
  public String body;
  public String isdefault;

  public String getInitRecordNumber() {
    return InitRecordNumber;
  }

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("position"))
      return position;
    else if (fieldName.equalsIgnoreCase("ad_language") || fieldName.equals("adLanguage"))
      return adLanguage;
    else if (fieldName.equalsIgnoreCase("subject"))
      return subject;
    else if (fieldName.equalsIgnoreCase("body"))
      return body;
    else if (fieldName.equalsIgnoreCase("isdefault"))
      return isdefault;
    else {
      log4j.debug("Field does not exist: " + fieldName);
      return null;
    }
  }

  public static EmailDefinitionData[] getEmailDefinitions(ConnectionProvider connectionProvider,
      String adOrgId, String docTypeTemplateId) throws ServletException {
    return getEmailDefinitions(connectionProvider, adOrgId, docTypeTemplateId, 0, 0);
  }

  public static EmailDefinitionData[] getEmailDefinitions(ConnectionProvider connectionProvider,
      String adOrgId, String docTypeTemplateId, int firstRegister, int numberRegisters)
      throws ServletException {
    String strSql = "";
    strSql = strSql
        + "		select"
        + "		    '1' as position,		"
        + "			emaildefinitions.ad_language as ad_language,"
        + "			emaildefinitions.subject as subject,"
        + "			emaildefinitions.body as body,"
        + "			emaildefinitions.isdefault as isdefault"
        + "		from"
        + "			c_poc_emaildefinition emaildefinitions"
        + "		where"
        // + "			emaildefinitions.ad_org_id = ? and"
        + "			emaildefinitions.c_poc_doctype_template_id = ?" + "        union" + "		select"
        + "		    '2' as position,		" + "			emaildefinitions.ad_language as ad_language,"
        + "			emaildefinitions.subject as subject," + "			emaildefinitions.body as body,"
        + "			emaildefinitions.isdefault as isdefault" + "		from"
        + "			c_poc_emaildefinition emaildefinitions" + "		where"
        + "			emaildefinitions.ad_org_id = '0' and"
        + "			emaildefinitions.c_poc_doctype_template_id = ?        			"
        + "		order by position asc, isdefault desc";

    ResultSet result;
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      // iParameter++;
      // UtilSql.setValue(st, iParameter, 12, null, adOrgId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, docTypeTemplateId);
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, docTypeTemplateId);

      result = st.executeQuery();
      long countRecord = 0;
      long countRecordSkip = 1;
      boolean continueResult = true;
      while (countRecordSkip < firstRegister && continueResult) {
        continueResult = result.next();
        countRecordSkip++;
      }
      while (continueResult && result.next()) {
        countRecord++;
        EmailDefinitionData objectEmailDefinitionData = new EmailDefinitionData();
        objectEmailDefinitionData.position = UtilSql.getValue(result, "position");
        objectEmailDefinitionData.adLanguage = UtilSql.getValue(result, "ad_language");
        objectEmailDefinitionData.subject = UtilSql.getValue(result, "subject");
        objectEmailDefinitionData.body = UtilSql.getValue(result, "body");
        objectEmailDefinitionData.isdefault = UtilSql.getValue(result, "isdefault");
        objectEmailDefinitionData.InitRecordNumber = Integer.toString(firstRegister);
        vector.addElement(objectEmailDefinitionData);
        if (countRecord >= numberRegisters && numberRegisters != 0) {
          continueResult = false;
        }
      }
      result.close();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    EmailDefinitionData objectEmailDefinitionData[] = new EmailDefinitionData[vector.size()];
    vector.copyInto(objectEmailDefinitionData);
    return (objectEmailDefinitionData);
  }
}
