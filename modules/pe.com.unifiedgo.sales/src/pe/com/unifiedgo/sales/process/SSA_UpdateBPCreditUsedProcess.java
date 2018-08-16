package pe.com.unifiedgo.sales.process;

import java.math.BigDecimal;

import javax.servlet.ServletException;

import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.OBInterceptor;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DalBaseProcess;
import org.openbravo.service.db.DalConnectionProvider;
import org.quartz.JobExecutionException;

public class SSA_UpdateBPCreditUsedProcess extends DalBaseProcess {
  private static ProcessLogger logger;
  private ConnectionProvider connection;

  public void doExecute(ProcessBundle bundle) throws Exception {

    logger = bundle.getLogger();
    connection = bundle.getConnection();

    OBContext.setAdminMode(false);
    ConnectionProvider conn = new DalConnectionProvider();
    try {
      OBInterceptor.setPreventUpdateInfoChange(true);
      int counter = 0;
      SSAUpdateBPCreditUsedProcessData[] data = SSAUpdateBPCreditUsedProcessData.getCreditUpdateableBPartners(conn);
      if (data != null && data.length > 0) {
        for (int i = 0; i < data.length; i++) {
          BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, data[i].cBpartnerId);
          String currencyId = "308";
          if (bp.getSsaCreditcurrency() != null)
            currencyId = bp.getSsaCreditcurrency().getId();
          System.out.println("Processing bp: " + bp.getIdentifier() + "\n");
          try {
            String creditused = SSAUpdateBPCreditUsedProcessData.getBpUsedCredit(conn, bp.getId(), currencyId);
            if (creditused != null && !creditused.isEmpty()) {
              System.out.println("setting to:" + (new BigDecimal(creditused)));
              bp.setSsaCreditused(new BigDecimal(creditused));
              OBDal.getInstance().save(bp);
            }
            counter++;
            OBDal.getInstance().getSession().flush();
            OBDal.getInstance().getSession().clear();
            if (counter % 50 == 0) {
              logger.log("BusinessPartners updated: " + counter + "\n");
            }
          } catch (ServletException e) {
            e.printStackTrace();
          }
        }
      }
      OBDal.getInstance().flush();
      logger.log("SSA_UpdateBPCreditUsedProcess Executed \n");
    } catch (Exception e) {
      throw new JobExecutionException(e.getMessage(), e);
    } finally {
      OBInterceptor.setPreventUpdateInfoChange(false);
      OBContext.restorePreviousMode();
    }
  }
}
