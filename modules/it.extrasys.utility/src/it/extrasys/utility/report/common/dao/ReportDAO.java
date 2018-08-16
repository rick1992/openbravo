package it.extrasys.utility.report.common.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.utility.Attachment;

public class ReportDAO {
  private static ReportDAO singleton;

  private ReportDAO() {
  }

  public static ReportDAO getSingleton() {
    if (singleton == null) {
      singleton = new ReportDAO();
    }
    return singleton;
  }

  public List<Attachment> findAttachment(String table, String record, String lang) {  
	  if ( null != lang && !lang.equalsIgnoreCase("") ) {
		  return getAttachmentLang(table, record, lang);
	  }else {
		  return getAttachment(table, record);
	  }
  }
  
  private List<Attachment> getAttachment(String table, String record) {
	  
	  String hql = " SELECT a FROM ADAttachment a WHERE a.table.id = ? AND a.record = ? ";
	  final Session session = OBDal.getInstance().getSession();
	  final Query query = session.createQuery(hql);
	  query.setParameter(0, table);
	  query.setParameter(1, record);

	  return (List<Attachment>) query.list();
	  
  }
  
  private List<Attachment> getAttachmentLang(String table, String record, String lang) {
	  
	  List<Attachment> result = new ArrayList<Attachment>();
	  
	  String hql = " SELECT a FROM ADAttachment a WHERE a.table.id = ? AND a.record = ? "
			  		+ " AND substr(a.name,length(a.name)-8,5) = ?  order by a.name";
	  Session session = OBDal.getInstance().getSession();
	  Query query = session.createQuery(hql);
	  query.setParameter(0, table);
	  query.setParameter(1, record);
	  query.setParameter(2, lang);

	  result =  (List<Attachment>) query.list();
	  
	  if ( result.isEmpty() ) {
		  hql = " SELECT a FROM ADAttachment a WHERE a.table.id = ? AND a.record = ? "
			  		+ " AND upper(substr(a.name,length(a.name)-8,5)) = upper(?)  order by a.name";
		  query = session.createQuery(hql);
		  query.setParameter(0, table);
		  query.setParameter(1, record);
		  query.setParameter(2, "OTHER");
		  
		  result.addAll((List<Attachment>) query.list());
	  }
	  
	  hql = " SELECT a FROM ADAttachment a WHERE a.table.id = ? AND a.record = ? "
		  		+ " AND upper(substr(a.name,length(a.name)-6,3)) = upper(?)  order by a.name";
	  query = session.createQuery(hql);
	  query.setParameter(0, table);
	  query.setParameter(1, record);
	  query.setParameter(2, "ALL");
	  
	  result.addAll((List<Attachment>) query.list());
	  
	  return result;
	  
  }

}
