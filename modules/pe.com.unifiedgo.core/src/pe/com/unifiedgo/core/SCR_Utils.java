package pe.com.unifiedgo.core;

import java.util.List;

import javax.servlet.ServletException;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.reporting.printing.PrintController;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Warehouse;

import pe.com.unifiedgo.core.data.ScrUserwarehouseDocserie;
import pe.com.unifiedgo.ebilling.data.BILLPhyDocSequence;
import pe.com.unifiedgo.ebilling.data.BILLPhyDocType;

public class SCR_Utils {

  final public static String defPhyDocNo = "000-0000000";
  final public static int phyDocRDigits = 7;
  final public static int phyDocLDigits = 3;

  final public static String eDefPhyDocNo = "F000-00000000";
  final public static int ePhyDocRDigits = 8;
  final public static int ePhyDocLDigits = 4;

  public static String getInvPhysicalDocumentNo(ConnectionProvider connectionProvider,
      VariablesSecureApp vars, String adUserId, String adClientId, String adOrgId,
      String mWarehouseId, String strSpecialDocType) throws ServletException {
    BILLPhyDocSequence phyDocSeq = null;
    String strPhyDocNo = "";
    boolean isPhyDocNoSetted = false;

    OBCriteria<ScrUserwarehouseDocserie> usrwhs_serie = OBDal.getInstance()
        .createCriteria(ScrUserwarehouseDocserie.class);
    usrwhs_serie.add(Restrictions.eq(ScrUserwarehouseDocserie.PROPERTY_CLIENT,
        OBDal.getInstance().get(Client.class, adClientId)));
    usrwhs_serie.add(Restrictions.eq(ScrUserwarehouseDocserie.PROPERTY_USER,
        OBDal.getInstance().get(User.class, adUserId)));
    if (strSpecialDocType.compareTo("SCOARINVOICE") == 0 && mWarehouseId != null) {
      usrwhs_serie.add(Restrictions.eq(ScrUserwarehouseDocserie.PROPERTY_WAREHOUSE,
          OBDal.getInstance().get(Warehouse.class, mWarehouseId)));
    }
    usrwhs_serie.add(Restrictions.sqlRestriction(
        "AD_ISORGINCLUDED( '" + adOrgId + "', orgwarehouse_id, ad_client_id ) > -1"));
    usrwhs_serie.addOrderBy(ScrUserwarehouseDocserie.PROPERTY_CREATIONDATE, false);
    List<ScrUserwarehouseDocserie> serie_array = usrwhs_serie.list();

    for (int i = 0; !isPhyDocNoSetted && i < serie_array.size(); i++) {
      phyDocSeq = serie_array.get(i).getBillPhyDocSequence();
      if (phyDocSeq.isEbill())
        continue;

      for (int j = 0; j < phyDocSeq.getBILLPhyDocTypeList().size(); j++) {
        BILLPhyDocType phyDocType = phyDocSeq.getBILLPhyDocTypeList().get(j);

        if (phyDocType.getDocumentType().getScoSpecialdoctype().compareTo(strSpecialDocType) == 0) {
          isPhyDocNoSetted = true;
          strPhyDocNo = getAutoPhyDocNo(phyDocSeq.getPrefix(), phyDocSeq.getNextAssignedNumber());
          break;
        }
      }
    }

    return strPhyDocNo;
  }

  public static String getElectronicInvPhysicalDocumentNo(ConnectionProvider connectionProvider,
      VariablesSecureApp vars, String adUserId, String adClientId, String adOrgId,
      String mWarehouseId, String strSpecialDocType, String strSpecialDocType_Reference)
      throws ServletException {
    BILLPhyDocSequence phyDocSeq = null;
    String strPhyDocNo = "";
    boolean isPhyDocNoSetted = false;

    OBCriteria<ScrUserwarehouseDocserie> usrwhs_serie = OBDal.getInstance()
        .createCriteria(ScrUserwarehouseDocserie.class);
    usrwhs_serie.add(Restrictions.eq(ScrUserwarehouseDocserie.PROPERTY_CLIENT,
        OBDal.getInstance().get(Client.class, adClientId)));
    usrwhs_serie.add(Restrictions.eq(ScrUserwarehouseDocserie.PROPERTY_USER,
        OBDal.getInstance().get(User.class, adUserId)));
    if (strSpecialDocType.compareTo("SCOARINVOICE") == 0 && mWarehouseId != null) {
      usrwhs_serie.add(Restrictions.eq(ScrUserwarehouseDocserie.PROPERTY_WAREHOUSE,
          OBDal.getInstance().get(Warehouse.class, mWarehouseId)));
    }
    usrwhs_serie.add(Restrictions.sqlRestriction(
        "AD_ISORGINCLUDED( '" + adOrgId + "', orgwarehouse_id, ad_client_id ) > -1"));
    usrwhs_serie.addOrderBy(ScrUserwarehouseDocserie.PROPERTY_CREATIONDATE, false);
    List<ScrUserwarehouseDocserie> serie_array = usrwhs_serie.list();

    for (int i = 0; !isPhyDocNoSetted && i < serie_array.size(); i++) {
      phyDocSeq = serie_array.get(i).getBillPhyDocSequence();
      if (!phyDocSeq.isEbill()) {
        continue;
      }
      for (int j = 0; j < phyDocSeq.getBILLPhyDocTypeList().size(); j++) {
        BILLPhyDocType phyDocType = phyDocSeq.getBILLPhyDocTypeList().get(j);
        if (phyDocType.getDocumentType().getScoSpecialdoctype().compareTo(strSpecialDocType) == 0) {
          if (strSpecialDocType.compareTo("SCOARCREDITMEMO") == 0
              || strSpecialDocType.compareTo("SCOARINVOICERETURNMAT") == 0
              || strSpecialDocType.compareTo("SCOARDEBITMEMO") == 0) {
            if (phyDocType.getDoctypeMemoRef() == null)
              continue;
            if (phyDocType.getDoctypeMemoRef().getScoSpecialdoctype()
                .compareTo(strSpecialDocType_Reference) != 0)
              continue;
          }

          isPhyDocNoSetted = true;
          strPhyDocNo = getAutoElectronicPhyDocNo(phyDocSeq.getPrefix(),
              phyDocSeq.getNextAssignedNumber());
          break;
        }
      }
    }

    return strPhyDocNo;
  }

  public static BILLPhyDocSequence getElectronicInvPhysicalSerie(
      ConnectionProvider connectionProvider, VariablesSecureApp vars, String adUserId,
      String adClientId, String adOrgId, String mWarehouseId, String strSpecialDocType,
      String strSpecialDocType_Reference, String prevPhysDocNoSerieIdSelected)
      throws ServletException {
    OBCriteria<ScrUserwarehouseDocserie> usrwhs_serie = OBDal.getInstance()
        .createCriteria(ScrUserwarehouseDocserie.class);
    usrwhs_serie.add(Restrictions.eq(ScrUserwarehouseDocserie.PROPERTY_CLIENT,
        OBDal.getInstance().get(Client.class, adClientId)));
    usrwhs_serie.add(Restrictions.eq(ScrUserwarehouseDocserie.PROPERTY_USER,
        OBDal.getInstance().get(User.class, adUserId)));
    if (strSpecialDocType.compareTo("SCOARINVOICE") == 0 && mWarehouseId != null) {
      usrwhs_serie.add(Restrictions.eq(ScrUserwarehouseDocserie.PROPERTY_WAREHOUSE,
          OBDal.getInstance().get(Warehouse.class, mWarehouseId)));
    }
    usrwhs_serie.add(Restrictions.sqlRestriction(
        "AD_ISORGINCLUDED( '" + adOrgId + "', orgwarehouse_id, ad_client_id ) > -1"));
    usrwhs_serie.addOrderBy(ScrUserwarehouseDocserie.PROPERTY_CREATIONDATE, false);
    List<ScrUserwarehouseDocserie> serie_array = usrwhs_serie.list();

    // previously selected serie
    if (!"".equals(prevPhysDocNoSerieIdSelected) && prevPhysDocNoSerieIdSelected != null) {
      for (int i = 0; i < serie_array.size(); i++) {
        BILLPhyDocSequence phyDocSeq = serie_array.get(i).getBillPhyDocSequence();
        if (!phyDocSeq.isEbill())
          continue;

        for (int j = 0; j < phyDocSeq.getBILLPhyDocTypeList().size(); j++) {
          BILLPhyDocType phyDocType = phyDocSeq.getBILLPhyDocTypeList().get(j);
          if (phyDocType.getDocumentType().getScoSpecialdoctype()
              .compareTo(strSpecialDocType) == 0) {
            if (phyDocSeq.getId().compareTo(prevPhysDocNoSerieIdSelected) == 0)
              return phyDocSeq;
          }
        }
      }
    }

    for (int i = 0; i < serie_array.size(); i++) {
      BILLPhyDocSequence phyDocSeq = serie_array.get(i).getBillPhyDocSequence();
      if (!phyDocSeq.isEbill())
        continue;

      for (int j = 0; j < phyDocSeq.getBILLPhyDocTypeList().size(); j++) {
        BILLPhyDocType phyDocType = phyDocSeq.getBILLPhyDocTypeList().get(j);
        if (phyDocType.getDocumentType().getScoSpecialdoctype().compareTo(strSpecialDocType) == 0) {
          if (strSpecialDocType.compareTo("SCOARCREDITMEMO") == 0
              || strSpecialDocType.compareTo("SCOARINVOICERETURNMAT") == 0
              || strSpecialDocType.compareTo("SCOARDEBITMEMO") == 0) {
            if (phyDocType.getDoctypeMemoRef() == null)
              continue;
            if (phyDocType.getDoctypeMemoRef().getScoSpecialdoctype()
                .compareTo(strSpecialDocType_Reference) != 0)
              continue;
          }
          return phyDocSeq;
        }
      }
    }

    return null;
  }

  public static String getInOutPhysicalDocumentNo(ConnectionProvider connectionProvider,
      VariablesSecureApp vars, String adUserId, String adClientId, String adOrgId,
      String mWarehouseId) throws ServletException {
    BILLPhyDocSequence phyDocSeq = null;
    String strPhyDocNo = "";
    boolean isPhyDocNoSetted = false;

    OBCriteria<ScrUserwarehouseDocserie> usrwhs_serie = OBDal.getInstance()
        .createCriteria(ScrUserwarehouseDocserie.class);
    usrwhs_serie.add(Restrictions.eq(ScrUserwarehouseDocserie.PROPERTY_CLIENT,
        OBDal.getInstance().get(Client.class, adClientId)));
    usrwhs_serie.add(Restrictions.eq(ScrUserwarehouseDocserie.PROPERTY_USER,
        OBDal.getInstance().get(User.class, adUserId)));
    usrwhs_serie.add(Restrictions.eq(ScrUserwarehouseDocserie.PROPERTY_WAREHOUSE,
        OBDal.getInstance().get(Warehouse.class, mWarehouseId)));
    usrwhs_serie.add(Restrictions.sqlRestriction(
        "AD_ISORGINCLUDED( '" + adOrgId + "', orgwarehouse_id, ad_client_id ) > -1"));
    usrwhs_serie.addOrderBy(ScrUserwarehouseDocserie.PROPERTY_CREATIONDATE, false);
    List<ScrUserwarehouseDocserie> serie_array = usrwhs_serie.list();
    for (int i = 0; !isPhyDocNoSetted && i < serie_array.size(); i++) {
      phyDocSeq = serie_array.get(i).getBillPhyDocSequence();

      for (int j = 0; j < phyDocSeq.getBILLPhyDocTypeList().size(); j++) {
        BILLPhyDocType phyDocType = phyDocSeq.getBILLPhyDocTypeList().get(j);
        if (phyDocType.getDocumentType().getScoSpecialdoctype().compareTo("SCOMMSHIPMENT") == 0) {
          isPhyDocNoSetted = true;
          strPhyDocNo = getAutoPhyDocNo(phyDocSeq.getPrefix(), phyDocSeq.getNextAssignedNumber());
          break;
        }
      }
    }

    return strPhyDocNo;
  }

  private static String getAutoPhyDocNo(String prefix, Long nextDocNo) {
    if ("".equals(prefix) || prefix == null) {
      return PrintController.defPhyDocNo;
    }
    if (nextDocNo == null) {
      return PrintController.defPhyDocNo;
    }

    String nextPhyDocNo = String.format("%0" + phyDocRDigits + "d", nextDocNo);
    return prefix + nextPhyDocNo;
  }

  public static String getAutoElectronicPhyDocNo(String prefix, Long nextDocNo) {
    if ("".equals(prefix) || prefix == null) {
      return PrintController.eDefPhyDocNo;
    }
    if (nextDocNo == null) {
      return PrintController.eDefPhyDocNo;
    }

    String nextPhyDocNo = String.format("%0" + ePhyDocRDigits + "d", nextDocNo);
    return prefix + nextPhyDocNo;
  }
}
