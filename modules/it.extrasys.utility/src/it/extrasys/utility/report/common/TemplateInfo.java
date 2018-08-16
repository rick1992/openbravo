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
package it.extrasys.utility.report.common;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.erpCommon.utility.reporting.ReportingException;
import org.openbravo.erpCommon.utility.reporting.TemplateData;

public class TemplateInfo {
  protected static Logger log4j = Logger.getLogger(TemplateInfo.class);

  protected String templateLocation;
  protected String templateFilename;
  protected String reportFilename;
  protected TemplateData[] templates;
  protected EmailDefinition defaultEmailDefinition;
  protected Map<String, EmailDefinition> emailDefinitions;

  public class EmailDefinition {
    private String _Subject;
    private String _Body;
    private String _Language;
    private boolean _IsDefault;

    public EmailDefinition(EmailDefinitionData emailDefinitionData) {
      _Subject = emailDefinitionData.getField("subject");
      _Body = emailDefinitionData.getField("body");
      _Language = emailDefinitionData.getField("ad_language");
      if (emailDefinitionData.getField("isdefault") != null) {
        _IsDefault = emailDefinitionData.getField("isdefault") == "Y" ? true : false;
      }
    }

    public String getSubject() {
      return _Subject;
    }

    public String getBody() {
      return _Body;
    }

    public String getLanguage() {
      return _Language;
    }

    public boolean isDefault() {
      return _IsDefault;
    }
  }
  public TemplateInfo(ConnectionProvider connectionProvider, String docTypeId, String orgId,
      String strLanguage, String templateId, String bpLanguage, String prefix,
      String strBaseDesignPath, String strDefaultDesignPath, String isebill) throws ServletException,
      ReportingException {
    templates = TemplateData.getDocumentTemplates(connectionProvider, docTypeId, orgId,isebill);
    final TemplateData template = getSelectedTemplate(templateId);
    if (templates.length > 0) {
      setTemplateLocation(template.getField("template_location"));
      // -----------------------------------------------------------
      // Multilanguage management

      /*
       * if (!bpLanguage.equalsIgnoreCase("en_US")) { templateFilename =
       * template.getField("template_filename");
       * 
       * String templateFilenameBPLang = null; String templatePath =
       * template.getField("template_location");
       * 
       * int dotPosition = templateFilename.indexOf("."); String name =
       * templateFilename.substring(0, dotPosition); templateFilenameBPLang = name + "_" +
       * bpLanguage + ".jrxml";
       * 
       * final String baseDesignPath = prefix + "/" + strBaseDesignPath + "/" +
       * strDefaultDesignPath;
       * 
       * String DesignPath = Replace.replace(templatePath, "@basedesign@", baseDesignPath);
       * 
       * File fileBPLang = new File(DesignPath + "/" + templateFilenameBPLang); if
       * (fileBPLang.exists()) { templateFilename = templateFilenameBPLang; }
       * 
       * } else { templateFilename = template.getField("template_filename"); }
       */
      // -----------------------------------------------------------
      templateFilename = template.getField("template_filename");
      reportFilename = template.getField("report_filename");

      // READ EMAIL DEFINITIONS!!!!
      emailDefinitions = new HashMap<String, EmailDefinition>();
      final EmailDefinitionData[] emailDefinitionsData = EmailDefinitionData.getEmailDefinitions(
          connectionProvider, orgId, template.id);
      if (emailDefinitionsData.length > 0) {
        for (final EmailDefinitionData emailDefinitionData : emailDefinitionsData) {
          final EmailDefinition emailDefinition = new EmailDefinition(emailDefinitionData);
          emailDefinitions.put(emailDefinition.getLanguage(), emailDefinition);

          if (emailDefinition.isDefault())
            defaultEmailDefinition = emailDefinition;
        }
        if (defaultEmailDefinition == null && !emailDefinitions.isEmpty()) {
          defaultEmailDefinition = emailDefinitions.values().iterator().next();
        }
      } else
        throw new ReportingException(Utility.messageBD(connectionProvider, "NoEmailDefinitions",
            strLanguage) + template.id);
    } else
      throw new ServletException(Utility.messageBD(connectionProvider, "NoDocumentTypeTemplate",
          strLanguage) + docTypeId);
  }

  protected TemplateData getSelectedTemplate(String templateId) {
	if ("default".equals(templateId)) {
	      return (templates.length == 0) ? new TemplateData() : templates[0];
	    } else {
	    	for (int i = 0; i < templates.length; i++) {
	    		final TemplateData template = templates[i];
	    		if (templateId.equals(template.id)) {
	    			return template;
	    			}
	    		}
	    	}
	return null;
	
  /*
  if (templates != null) {
      if ("default".equals(templateId)) {
        return templates[0];

      } else {
        for (int i = 0; i < templates.length; i++) {
          final TemplateData template = templates[i];
          if (templateId.equals(template.id)) {
            return template;
          }
        }
      }
    }
  
    return null;
    */
  }

  public TemplateData[] getTemplates() {
    return templates;
  }

  public void setTemplates(TemplateData[] templates) {
    this.templates = templates;
  }

  public String getTemplate() {
    return templateLocation + "/" + templateFilename;
  }

  public void setTemplateLocation(String templateLocation) {
    this.templateLocation = templateLocation;
    // Make sure the location always ends with a / character
    if (!templateLocation.endsWith("/"))
      this.templateLocation = templateLocation + "/";
    if (log4j.isDebugEnabled())
      log4j.debug("Template location is set to: " + templateLocation);
  }

  public EmailDefinition getEmailDefinition(String language) throws ReportingException {
    EmailDefinition emailDefinition = emailDefinitions.get(language);
    if (emailDefinition == null) {
      log4j.info("No email definition found for language " + language
          + ". Using default email definition");
      emailDefinition = defaultEmailDefinition;
    }

    if (emailDefinition == null)
      throw new ReportingException("No email definition available.");
    return emailDefinition;
  }

  public EmailDefinition get_DefaultEmailDefinition() {
    return defaultEmailDefinition;
  }

  public String getTemplateFilename() {
    return templateFilename;
  }

  public void setTemplateFilename(String templateFilename) {
    this.templateFilename = templateFilename;
  }

  public String getReportFilename() {
    return reportFilename;
  }

  public void setReportFilename(String reportFilename) {
    this.reportFilename = reportFilename;
  }

  public EmailDefinition getDefaultEmailDefinition() {
    return defaultEmailDefinition;
  }

  public void setDefaultEmailDefinition(EmailDefinition defaultEmailDefinition) {
    this.defaultEmailDefinition = defaultEmailDefinition;
  }

  public Map<String, EmailDefinition> getEmailDefinitions() {
    return emailDefinitions;
  }

  public void setEmailDefinitions(Map<String, EmailDefinition> emailDefinitions) {
    this.emailDefinitions = emailDefinitions;
  }

  public String getTemplateLocation() {
    return templateLocation;
  }

}
