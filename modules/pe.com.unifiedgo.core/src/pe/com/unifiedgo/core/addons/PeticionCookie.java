package pe.com.unifiedgo.core.addons;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URL;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

public class PeticionCookie {

  public Image peticionConCookieImagenSSL(String urlp, String metodo, String parametros,
      List<HttpCookie> cookies) throws Exception {
    System.setProperty("http.agent", BusquedaCookie.USER_AGENT_FIREFOX);
    try {
      CookieManager manager = new CookieManager();
      CookieHandler.setDefault(manager);
      CookieStore cookieJar = manager.getCookieStore();
      URL url = new URL(urlp);
      javax.net.ssl.HttpsURLConnection connection = (javax.net.ssl.HttpsURLConnection) url
          .openConnection();
      connection.setSSLSocketFactory(SSLByPass.getInstancia().getSslSocketFactory());
      cookieJar.add(url.toURI(), cookies.get(0));
      // cookieJar.add(url.toURI(), cookies.get(1));

      connection.setRequestMethod(metodo);
      // -----------------------------------------------------------------------------------------
      connection.connect(); // Conectar
      connection.getContent();
      BufferedInputStream reader = new BufferedInputStream(connection.getInputStream());
      ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
      int c;
      while ((c = reader.read()) != -1) {
        byteArrayOut.write(c);
      }
      Image image = Toolkit.getDefaultToolkit().createImage(byteArrayOut.toByteArray());
      reader.close();
      return image;
    } catch (Exception e) {
      /*
       * JOptionPane.showMessageDialog(null, "No se puede obtener Cookies ",
       * "Consulta DNI - peticionConCookieImagenSSL", JOptionPane.ERROR_MESSAGE);
       */
      throw e;
    }
  }

  public String[] peticionConCookieStringSSL(String urlp, String metodo, String parametros,
      List<HttpCookie> cookies) throws Exception {
    System.setProperty("http.agent", BusquedaCookie.USER_AGENT_FIREFOX);
    try {

      CookieManager manager = new CookieManager();
      CookieHandler.setDefault(manager);
      CookieStore cookieJar = manager.getCookieStore();
      URL url = new URL(urlp);
      javax.net.ssl.HttpsURLConnection connection = (javax.net.ssl.HttpsURLConnection) url
          .openConnection();
      connection.setSSLSocketFactory(SSLByPass.getInstancia().getSslSocketFactory());
      cookieJar.add(url.toURI(), cookies.get(0));
      // cookieJar.add(url.toURI(), cookies.get(1));

      connection.setRequestMethod(metodo);
      connection.setDoOutput(true); // Esto permite agregar los parametros
      // Escribiendo las variables
      OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
      writer.write(parametros);
      writer.flush();
      // -----------------------------------------------------------------------------------------
      connection.connect(); // Conectar
      connection.getContent();// error
      StringBuilder answer = new StringBuilder();
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(connection.getInputStream(), "ISO-8859-1"));
      String line;
      int linea = 1;
      while ((line = reader.readLine()) != null) {
        if (linea >= 154 && linea <= 156) {
          if (linea != 154)
            answer.append(" ");
          answer.append(line.trim());
        }
        // System.out.println(linea + " == " + line.trim());
        linea++;
      }
      writer.close();
      reader.close();

      String[] rpta = new String[3];
      String nombresYApellidos = StringEscapeUtils
          .unescapeHtml(answer.toString().replace("<br>", "").replace("<br/>", "")
              .replace("</td>", "").replace("</tr>", "").trim());

      String[] rptaTmp = nombresYApellidos.split("\\s+");
      String nombres = "";
      String apMat = "";
      String apPat = "";
      for (int i = 0; i < rptaTmp.length - 2; i++) {
        nombres = nombres + " " + rptaTmp[i];
      }
      try {
        apMat = rptaTmp[rptaTmp.length - 2];
        apPat = rptaTmp[rptaTmp.length - 1];
        nombres = nombres.substring(1);
      } catch (Exception ex) {
      }

      rpta[0] = nombres;
      rpta[1] = apMat;
      rpta[2] = apPat;

      return rpta;

    } catch (Exception e) {
      /*
       * JOptionPane.showMessageDialog(null, "No se puede obtener Cookies ",
       * "Consulta DNI - peticionConCookieStringSSL", JOptionPane.ERROR_MESSAGE);
       */
      throw e;
    }
  }

  public Image peticionConCookieImagen(String urlp, String metodo, String parametros,
      List<HttpCookie> cookies) throws Exception {
    System.setProperty("http.agent", BusquedaCookie.USER_AGENT_FIREFOX);
    try {
      CookieManager manager = new CookieManager();
      CookieHandler.setDefault(manager);
      CookieStore cookieJar = manager.getCookieStore();
      URL url = new URL(urlp);
      java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
      cookieJar.add(url.toURI(), cookies.get(0));
      // cookieJar.add(url.toURI(), cookies.get(1));

      connection.setRequestMethod(metodo);
      // -----------------------------------------------------------------------------------------
      connection.connect(); // Conectar
      connection.getContent();
      BufferedInputStream reader = new BufferedInputStream(connection.getInputStream());
      ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
      int c;
      while ((c = reader.read()) != -1) {
        byteArrayOut.write(c);
      }
      Image image = Toolkit.getDefaultToolkit().createImage(byteArrayOut.toByteArray());
      reader.close();
      return image;
    } catch (Exception e) {
      /*
       * JOptionPane.showMessageDialog(null, "No se puede obtener Cookies ",
       * "Consulta RUC - peticionConCookieImagen", JOptionPane.ERROR_MESSAGE);
       */
      throw e;
    }
  }

  public String[] peticionConCookieString(String urlp, String metodo, String parametros,
      List<HttpCookie> cookies) throws Exception {
    System.setProperty("http.agent", BusquedaCookie.USER_AGENT_FIREFOX);
    try {
      CookieManager manager = new CookieManager();
      CookieHandler.setDefault(manager);
      CookieStore cookieJar = manager.getCookieStore();
      URL url = new URL(urlp);
      java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
      cookieJar.add(url.toURI(), cookies.get(0));
      // cookieJar.add(url.toURI(), cookies.get(1));

      connection.setRequestMethod(metodo);
      connection.setDoOutput(true); // Esto permite agregar los parametros
      // Escribiendo las variables
      OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
      writer.write(parametros);
      writer.flush();

      // -----------------------------------------------------------------------------------------
      connection.connect(); // Conectar
      connection.getContent();
      String[] answer = new String[2];
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(connection.getInputStream(), "ISO-8859-1"));
      String line;
      int linea = 1;
      String tipocontr = "";
      while ((line = reader.readLine()) != null) {
        if (linea == 132)// RUC - RAZ SOCIAL - antes:122
        {
          answer[0] = line.trim().replaceAll("<td  class=\"bg\" colspan=3>", "")
              .replaceAll("</td>", "").substring(14);

        } else if (linea == 136)// TIPO DE CONTRIBUYENTE+12 - antes:126
        {
          tipocontr = line.trim().replaceAll("<td class=\"bg\" colspan=3>", "").replaceAll("</td>",
              "");// 25+12
        }

        if (tipocontr.equalsIgnoreCase("PERSONA NATURAL SIN NEGOCIO")
            || tipocontr.equalsIgnoreCase("PERSONA NATURAL CON NEGOCIO")) {
          if (linea == 183)// Direccion - antes:167
            answer[1] = line.trim().replaceAll("<td class=\"bg\" colspan=3>", "")
                .replaceAll("</td>", "");
          ;
        } else {
          if (linea == 171)// Direccion - antes:158
            answer[1] = line.trim().replaceAll("<td class=\"bg\" colspan=3>", "")
                .replaceAll("</td>", "");
          ;
        }

        // System.out.println(linea + " == " + line.trim());

        linea++;
      }
      writer.close();
      reader.close();

      return answer;

    } catch (Exception e) {
      /*
       * JOptionPane.showMessageDialog(null, "No se puede obtener Cookies ",
       * "Consulta RUC - peticionConCookieString", JOptionPane.ERROR_MESSAGE);
       */
      throw e;
    }
  }
}
