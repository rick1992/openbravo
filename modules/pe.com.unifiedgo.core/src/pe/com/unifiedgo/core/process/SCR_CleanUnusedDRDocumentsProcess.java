package pe.com.unifiedgo.core.process;

import java.sql.Connection;

import javax.servlet.ServletException;

import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DalBaseProcess;
import org.openbravo.service.db.DalConnectionProvider;
import org.quartz.JobExecutionException;


public class SCR_CleanUnusedDRDocumentsProcess extends DalBaseProcess {
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
      SCRCleanUnusedDRDocumentsProcessData[] data = SCRCleanUnusedDRDocumentsProcessData.getUnusedDROrders(conn);
      if (data != null && data.length > 0) {
        for (int i = 0; i < data.length; i++) {
          Order order = OBDal.getInstance().get(Order.class, data[i].cOrderId);
          System.out.println("Deleting order: " + order.getIdentifier());
          try {
            SCRCleanUnusedDRDocumentsProcessData.deleteOrderTax(connection, conn, order.getId());
            SCRCleanUnusedDRDocumentsProcessData.deleteOrder(connection, conn, order.getId());
            counter++;
            if (counter % 50 == 0) {
              logger.log("Orders deleted: " + counter + "\n");
            }
            conn.releaseCommitConnection(connection);
          } catch (ServletException e) {
            conn.releaseRollbackConnection(connection);
            e.printStackTrace();
          }
        }
      }

      counter = 0;
      connection = conn.getTransactionConnection();
      data = SCRCleanUnusedDRDocumentsProcessData.getUnusedDRInvoices(conn);
      if (data != null && data.length > 0) {
        for (int i = 0; i < data.length; i++) {
          Invoice inv = OBDal.getInstance().get(Invoice.class, data[i].cInvoiceId);
          System.out.println("Deleting invoice: " + inv.getIdentifier());
          try {
            SCRCleanUnusedDRDocumentsProcessData.deleteInvoiceTax(connection, conn, inv.getId());
            SCRCleanUnusedDRDocumentsProcessData.deleteInvoice(connection, conn, inv.getId());
            counter++;
            if (counter % 50 == 0) {
              logger.log("Invoices deleted: " + counter + "\n");
            }
            conn.releaseCommitConnection(connection);
          } catch (ServletException e) {
            conn.releaseRollbackConnection(connection);
            e.printStackTrace();
          }
        }
      }

      counter = 0;
      connection = conn.getTransactionConnection();
      data = SCRCleanUnusedDRDocumentsProcessData.getUnusedDRInOuts(conn);
      if (data != null && data.length > 0) {
        for (int i = 0; i < data.length; i++) {
          ShipmentInOut inout = OBDal.getInstance().get(ShipmentInOut.class, data[i].mInoutId);
          System.out.println("Deleting inout: " + inout.getIdentifier());
          try {
            SCRCleanUnusedDRDocumentsProcessData.deleteInOut(connection, conn, inout.getId());
            counter++;
            if (counter % 50 == 0) {
              logger.log("Inouts deleted: " + counter + "\n");
            }
            conn.releaseCommitConnection(connection);
          } catch (ServletException e) {
            conn.releaseRollbackConnection(connection);
            e.printStackTrace();
          }
        }
      }

      counter = 0;
      connection = conn.getTransactionConnection();
      data = SCRCleanUnusedDRDocumentsProcessData.getUnusedDRPayments(conn);
      if (data != null && data.length > 0) {
        for (int i = 0; i < data.length; i++) {
          FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, data[i].finPaymentId);
          System.out.println("Deleting payment: " + payment.getIdentifier());
          try {
            SCRCleanUnusedDRDocumentsProcessData.deletePayment(connection, conn, payment.getId());
            counter++;
            if (counter % 50 == 0) {
              logger.log("Payments deleted: " + counter + "\n");
            }
            conn.releaseCommitConnection(connection);
          } catch (ServletException e) {
            conn.releaseRollbackConnection(connection);
            e.printStackTrace();
          }
        }
      }
      OBDal.getInstance().flush();
      logger.log("SCR_CleanUnusedDRDocumentsProcess Executed \n");
    } catch (Exception e) {
      throw new JobExecutionException(e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
