package pe.com.unifiedgo.core;

import org.openbravo.erpCommon.utility.OBError;

public class Result {
  public OBError msg;
  public String command;
  public boolean doRollback;

  public Result() {
    msg = null;
    command = null;
    doRollback = true;
  }
}
