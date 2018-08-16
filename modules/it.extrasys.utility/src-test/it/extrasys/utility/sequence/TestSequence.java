package it.extrasys.utility.sequence;

import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.test.base.BaseTest;

public class TestSequence extends BaseTest {
  public void testsequence1() {
    String role_id = "FF80808133250F46013325BB98AE04FE";
    String client_id = "FF80808133250F46013325BB985304ED";
    String org_id = "0";
    // setTestUserContext();
    OBContext.setOBContext("100", role_id, client_id, org_id);
    OBDal.getInstance().flush();

    try {
      String docno = SequenceUtil.nextDocumentNumber("PROVA_TABELLA", client_id, org_id, "Y");
      System.out.println("SequenceUtil.nextDocumentNumber " + docno);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
}
