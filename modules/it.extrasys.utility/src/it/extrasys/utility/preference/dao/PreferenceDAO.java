package it.extrasys.utility.preference.dao;

import java.util.List;

import org.hibernate.Query;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.domain.Preference;

public class PreferenceDAO {

  private static PreferenceDAO singleton;

  private PreferenceDAO() {
  }

  public static PreferenceDAO getSingleton() {
    if (singleton == null) {
      singleton = new PreferenceDAO();
    }
    return singleton;
  }

  public List<Preference> listByAttribute(String attribute) {
    List<Preference> prefs = null;
    OBContext.setAdminMode();
    Query q = null;
    try {
      String sq = "select pref from ADPreference pref where pref.attribute=?";
      q = OBDal.getInstance().getSession().createQuery(sq);
      q.setParameter(0, attribute);
      prefs = q.list();
    } finally {
      OBContext.restorePreviousMode();
    }

    return prefs;
  }

  public Preference readByAttribute(String attribute) {
    List<Preference> prefs = null;
    Preference p = null;
    OBContext.setAdminMode();
    Query q = null;
    try {
      String sq = "select pref from ADPreference pref where pref.attribute=?";
      q = OBDal.getInstance().getSession().createQuery(sq);
      q.setParameter(0, attribute);
      prefs = q.list();
      if (prefs != null && prefs.size() > 0) {
        p = prefs.get(0);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return p;
  }

}
