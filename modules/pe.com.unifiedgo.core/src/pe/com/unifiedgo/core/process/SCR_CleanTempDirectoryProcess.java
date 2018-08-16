package pe.com.unifiedgo.core.process;

import java.io.File;
import java.util.Calendar;

import org.openbravo.dal.core.OBContext;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DalBaseProcess;
import org.openbravo.service.db.DalConnectionProvider;
import org.quartz.JobExecutionException;

public class SCR_CleanTempDirectoryProcess extends DalBaseProcess {
  private static ProcessLogger logger;
  private ConnectionProvider connection;

  public void doExecute(ProcessBundle bundle) throws Exception {

    logger = bundle.getLogger();
    connection = bundle.getConnection();

    OBContext.setAdminMode(false);
    ConnectionProvider conn = new DalConnectionProvider();
    int counter = 0;
    int errorcounter = 0;

    long now = Calendar.getInstance().getTimeInMillis();
    long oneDay = 1000L * 60L * 60L * 24L;
    long threeDays = 3L * oneDay;

    try {
      // Locale currentLocale = Locale.getDefault();
      //
      // System.out.println(currentLocale.getDisplayLanguage());
      // System.out.println(currentLocale.getDisplayCountry());
      //
      // System.out.println(currentLocale.getLanguage());
      // System.out.println(currentLocale.getCountry());
      //
      // System.out.println(System.getProperty("user.country"));
      // System.out.println(System.getProperty("user.language"));

      File folder = new File(bundle.getConfig().strFTPDirectory);
      File[] listOfFiles = folder.listFiles();

      for (int i = 0; i < listOfFiles.length; i++) {
        if (listOfFiles[i].isFile()) {
          long diff = now - listOfFiles[i].lastModified();
          if (diff > threeDays) {
            boolean deleted = false;
            try {
              deleted = listOfFiles[i].delete();
            } catch (Exception e) {
            }

            if (!deleted) {
              errorcounter++;
            }
            counter++;
            if (counter % 50 == 0) {
              logger.log("Processed Files: " + counter + "\n");
            }
          }
        }
      }

      File foldertmp = new File(bundle.getConfig().strFTPDirectory + "/tmp");
      File[] listOfFilesTmp = foldertmp.listFiles();

      for (int i = 0; i < listOfFilesTmp.length; i++) {
        if (listOfFilesTmp[i].isFile()) {
          long diff = now - listOfFilesTmp[i].lastModified();
          if (diff > threeDays) {
            boolean deleted = false;
            try {
              deleted = listOfFilesTmp[i].delete();
            } catch (Exception e) {
            }

            if (!deleted) {
              errorcounter++;
            }
            counter++;
            if (counter % 50 == 0) {
              logger.log("Processed Files: " + counter + "\n");
            }
          }
        }
      }

      logger.log("SCR_CleanTempDirectoryProcess Executed with: Processed Files:" + counter
          + " - Error Deleting Files:" + errorcounter);
      System.out.println("SCR_CleanTempDirectoryProcess Executed with: Processed Files:" + counter
          + " - Error Deleting Files:" + errorcounter);
    } catch (Exception e) {
      throw new JobExecutionException(e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
