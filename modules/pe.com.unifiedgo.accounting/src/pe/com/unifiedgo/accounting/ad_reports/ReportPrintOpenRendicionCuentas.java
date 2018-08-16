package pe.com.unifiedgo.accounting.ad_reports;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ReportPrintOpenRendicionCuentas extends ReportPrintOpenRendicionCuentasServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

    // post(request, response, SUMMARY);
    postTransaction(request, response);
  }
}