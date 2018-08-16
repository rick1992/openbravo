package pe.com.unifiedgo.accounting.process;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.financialmgmt.accounting.AccountingFact;
import org.openbravo.model.financialmgmt.payment.DoubtfulDebt;
import org.openbravo.model.financialmgmt.payment.DoubtfulDebtRun;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

import pe.com.unifiedgo.accounting.data.SCOPle5_6_Reg;
import pe.com.unifiedgo.accounting.data.SCOPle8_14_Reg;


public class DeleteRegPLELines extends DalBaseProcess {
	
  public void doExecute(ProcessBundle bundle) throws Exception {
	  final ConnectionProvider connection = bundle.getConnection();
		Connection conn = connection.getTransactionConnection();
	  
    try {
    	final String recordid = (String) bundle.getParams().get("SCO_Doc_For_Regple_V_ID");
    	
    	final VariablesSecureApp vars = bundle.getContext().toVars();
		OBContext.setOBContext(vars.getUser(), vars.getRole(), vars.getClient(), vars.getOrg());
		
    	Map<String, Object> temp = bundle.getParams();
    	DeleteRegPLELinesData[] ids = DeleteRegPLELinesData.selectRegularizationLines(connection, recordid);
    	AccountingFact factacct = OBDal.getInstance().get(AccountingFact.class, recordid);
    	SCOPle5_6_Reg factacctreg = OBDal.getInstance().get(SCOPle5_6_Reg.class, recordid);
    	
    	if(factacct==null){
    		/* terminar, es decir no hay lineas originales y no se puede borrar. Mostrar mensaje personalizado. */
    		throw new OBException("No se puede eliminar el asiento regularizado.");
    	}
    	
    	if(factacctreg==null){
    		final OBError msgnolines = new OBError();
    		msgnolines.setType("Info");
    		msgnolines.setTitle("@Info@");
    		msgnolines.setMessage("No tiene líneas de regularización.");
    		bundle.setResult(msgnolines);
    		OBDal.getInstance().commitAndClose();
    		return;
    	}
    	
    	String documentid = factacct.getRecordID();
    	String tableid = factacct.getTable().getId();
    	
    	SCOPle8_14_Reg record;
    	
    	if(tableid.equalsIgnoreCase("318")){
    		/* eliminar regularizacion 8 - 14 */
    		
    		OBCriteria<SCOPle8_14_Reg> obCriteria = OBDal.getInstance().createCriteria(SCOPle8_14_Reg.class);
        	obCriteria.add(Restrictions.eq(SCOPle8_14_Reg.PROPERTY_INVOICE + ".id", documentid));
        	
        	if(obCriteria.list().size()>0){
        		String recid = obCriteria.list().get(0).getId();
        		
        		record = OBDal.getInstance().get(SCOPle8_14_Reg.class, recid);
        		
        		//DeleteRegPLELinesData.revertStatusReg814(conn, connection, recid);
        		
        		record.setNewOBObject(true);
        		
        		record.setProcessed(false);
        		record.setDocumentAction("CO");
        		record.setDocumentStatus("DR");
        		
        		OBDal.getInstance().save(record);
        		
        		OBDal.getInstance().flush();
        		
        		removeRecordReg4_18PLE(record);
        	}
    	}
    	
    	for(int i=0;i<ids.length;i++){
    		SCOPle5_6_Reg factAcctForReg = OBDal.getInstance().get(SCOPle5_6_Reg.class, ids[i].scople56regid);
    		OBDal.getInstance().remove(factAcctForReg);
    	}
    	
    	OBDal.getInstance().flush();
    	
      final OBError msg = new OBError();
      msg.setType("Success");
      msg.setTitle("@Success@");
      //msg.setMessage("@SCO_GenTelecreditFPCreated@" + telecredit.getDocumentNo());
      msg.setMessage("Líneas eliminadas correctamente.");

      bundle.setResult(msg);
      OBDal.getInstance().commitAndClose();
    } catch (final OBException e) {
      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(e.getMessage());
      msg.setTitle("@Error@");
      OBDal.getInstance().rollbackAndClose();
      bundle.setResult(msg);
    }
  }
  
  
  
  private void removeRecordReg4_18PLE(SCOPle8_14_Reg record) {
	  OBDal.getInstance().remove(record);
	  OBDal.getInstance().flush();
	  }
  
  
  
}
