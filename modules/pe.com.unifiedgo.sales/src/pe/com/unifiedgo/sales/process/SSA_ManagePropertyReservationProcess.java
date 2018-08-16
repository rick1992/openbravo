package pe.com.unifiedgo.sales.process;

import java.sql.Connection;

import javax.servlet.ServletException;

import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DalBaseProcess;
import org.openbravo.service.db.DalConnectionProvider;
import org.quartz.JobExecutionException;

public class SSA_ManagePropertyReservationProcess extends DalBaseProcess {
  private static ProcessLogger logger;
  private ConnectionProvider connection;

  public void doExecute(ProcessBundle bundle) throws Exception {

    logger = bundle.getLogger();
    connection = bundle.getConnection();

    OBContext.setAdminMode(false);
    ConnectionProvider conn = new DalConnectionProvider();
    try {

      int counter = 0;
      Connection connection = conn.getTransactionConnection();
      SSAManagePropertyReservationProcessData[] data = SSAManagePropertyReservationProcessData
          .getReservedProspects(conn);
      if (data != null && data.length > 0) {
        for (int i = 0; i < data.length; i++) {
          System.out.println("Unreserving sales prospect: " + data[i].documentno);
          try {
            SSAManagePropertyReservationProcessData.unreservePropertiesFromProspect(connection,
                conn, data[i].prospectId);
            SSAManagePropertyReservationProcessData.unreserveProspect(connection, conn,
                data[i].prospectId);
            counter++;
            if (counter % 10 == 0) {
              logger.log("Sales prospects unreserved: " + counter + "\n");
            }
            conn.releaseCommitConnection(connection);
          } catch (ServletException e) {
            conn.releaseRollbackConnection(connection);
            e.printStackTrace();
          }
        }
      }

      OBDal.getInstance().flush();
      logger.log("Manage Property Reservation Process Executed \n");
    } catch (Exception e) {
      throw new JobExecutionException(e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
