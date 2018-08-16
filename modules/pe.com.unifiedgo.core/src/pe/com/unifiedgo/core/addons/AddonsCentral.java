package pe.com.unifiedgo.core.addons;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openbravo.base.session.OBPropertiesProvider;

public class AddonsCentral {

  static HashMap<String, Object> buscarCookieLogueo = new HashMap<String, Object>();
  static int counter = 0;

  private static BufferedImage toBufferedImage(Image img) {
    if (img instanceof BufferedImage) {
      return (BufferedImage) img;
    }

    new javax.swing.ImageIcon(img); // force image to load
    // Create a buffered image with transparency
    BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null),
        BufferedImage.TYPE_3BYTE_BGR);

    // Draw the image on to the buffered image
    Graphics2D bGr = bimage.createGraphics();
    bGr.drawImage(img, 0, 0, null);
    bGr.dispose();

    // Return the buffered image
    return bimage;
  }

  public static String getCatchaSunatImage(String randomCode) throws Exception {
    String logeoUrl = "http://e-consultaruc.sunat.gob.pe/cl-ti-itmrconsruc/frameCriterioBusqueda.jsp;";
    String captchaUrl = "http://e-consultaruc.sunat.gob.pe/cl-ti-itmrconsruc/captcha?accion=image";

    buscarCookieLogueo.put(randomCode, new BusquedaCookie().buscarCookieLogueo(logeoUrl).get(0));
    Image buscarCookieLogueoimagenreniec = new PeticionCookie().peticionConCookieImagen(captchaUrl,
        "POST", "", (List<HttpCookie>) buscarCookieLogueo.get(randomCode));

    BufferedImage reniecCaptcha = toBufferedImage(buscarCookieLogueoimagenreniec);
    File outputfile = File.createTempFile("sunat_", ".jpg");
    ImageIO.write(reniecCaptcha, "jpg", outputfile);

    return outputfile.getAbsolutePath();
  }

  public static void getProcessImageForReniec(String imgPath) throws IOException {

    BufferedImage img = ImageIO.read(new File(imgPath));
    SimpleImageProcessing ip = new SimpleImageProcessing(img);
    ip.runErosion();
    img = ip.getErosionResult();

    for (int i = 0; i < 1; i++) {
      ip = new SimpleImageProcessing(img);
      ip.runErosionHeight();
      img = ip.getErosionResult();
    }

    for (int i = 0; i < 2; i++) {
      ip = new SimpleImageProcessing(img);
      ip.runGaussian();
      img = ip.getErosionResult();
    }

    ip = new SimpleImageProcessing(img);
    ip.runBinary();
    img = ip.getErosionResult();

    File outputfile = new File(imgPath);
    ImageIO.write(img, "jpg", outputfile);

  }

  public static String getCatchaReniecImage(String randomCode) throws Exception {

    String logeoUrl = "https://cel.reniec.gob.pe/valreg/valreg.do;";
    String captchaUrl = "https://cel.reniec.gob.pe/valreg/codigo.do";

    buscarCookieLogueo.put(randomCode, new BusquedaCookie().buscarCookieLogueoSSL(logeoUrl).get(0));
    Image buscarCookieLogueoimagenreniec = new PeticionCookie().peticionConCookieImagenSSL(
        captchaUrl, "POST", "", (List<HttpCookie>) buscarCookieLogueo.get(randomCode));

    BufferedImage reniecCaptcha = toBufferedImage(buscarCookieLogueoimagenreniec);
    File outputfile = File.createTempFile("reniec_", ".jpg");
    ImageIO.write(reniecCaptcha, "jpg", outputfile);

    return outputfile.getAbsolutePath();
  }

  public static String[] getNamesReniec(String dni) throws Exception {

    String tesseractPath = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("tesseract.path");

    String tesseractOptions = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("tesseract.options");

    if (tesseractOptions == null)
      tesseractOptions = "";

    if (tesseractPath == null || tesseractPath.equals(""))
      return null;

    try {
      counter++;

      Document doc = Jsoup
          .connect(
              "http://clientes.reniec.gob.pe/padronElectoral2012/consulta.htm?hTipo=2&hDni=" + dni)
          .get();

      Iterator<Element> tdData = doc.select("td[class=txtCuerpo]").iterator();
      tdData.next();

      Element tdNombres = tdData.next();
      String[] nombresApellidos = tdNombres.text().split(",");
      // [0]: apellidos, [1]: nombres
      String[] apellidos = nombresApellidos[0].split(" ");

      String[] rpta = new String[3];
      rpta[0] = nombresApellidos[1].trim();
      rpta[1] = apellidos[0].trim();
      rpta[2] = apellidos[1].trim();
      /*
       * for (int i = 0; i < rpta.length; i++) { System.out.println("rpta_:" + i + ":" + rpta[i]); }
       */
      if (counter > 1000) {
        buscarCookieLogueo.clear();// LIMPIAR DE VEZ EN CUANDO AUNQUE A VECES FALLE
      }

      return rpta;
    } catch (Exception ex) {
      return null;
    }
  }

  public static String[] getNamesSunat(String ruc) throws Exception {

    String tesseractPath = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("tesseract.path");
    String tesseractOptions = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("tesseract.options");
    if (tesseractOptions == null)
      tesseractOptions = "";

    if (tesseractPath == null || tesseractPath.equals(""))
      return null;

    try {
      String randomCode = String.valueOf(Math.round(Math.random() * 1000000));
      String captchaPath = getCatchaSunatImage(randomCode);
      String captchaCode = getOCR(captchaPath, tesseractPath, tesseractOptions);
      counter++;
      StringBuilder sb_parametros = new StringBuilder();

      // accion=consPorRuc&nroRuc=&razSoc=&nrodoc=
      sb_parametros.append("accion=consPorRuc&nroRuc=");
      sb_parametros.append(ruc);
      sb_parametros.append("&");
      sb_parametros.append("codigo=");
      sb_parametros.append(captchaCode.toUpperCase().trim());
      String parametros = sb_parametros.toString();
      StringBuilder sb_url = new StringBuilder();
      sb_url.append("http://e-consultaruc.sunat.gob.pe/cl-ti-itmrconsruc/jcrS00Alias;");

      List<HttpCookie> cookies = (List<HttpCookie>) buscarCookieLogueo.get(randomCode);
      for (HttpCookie httpCookie : cookies) {
        if (httpCookie.getName().compareToIgnoreCase("jsessionid") == 0
            || httpCookie.getName().compareToIgnoreCase("itmrconsrucsession") == 0) {
          sb_url.append(httpCookie.getName());
          sb_url.append("=");
          sb_url.append(httpCookie.getValue());
        }
      }

      String[] rpta;
      rpta = new PeticionCookie().peticionConCookieString(sb_url.toString(), "POST", parametros,
          cookies);
      String razonSocial = rpta[0];
      if (razonSocial == null || razonSocial.trim().equals(""))
        return null;
      rpta[0] = rpta[0].trim();
      rpta[1] = rpta[1].replaceAll("\\s{2,}", " ").trim();

      if (counter > 1000) {
        buscarCookieLogueo.clear();// LIMPIAR DE VEZ EN CUANDO AUNQUE A VECES FALLE
      }

      return rpta;
    } catch (Exception ex) {
      return null;
    }
  }

  // public static String[] getNamesReniec(String dni) throws Exception {
  //
  // String tesseractPath = OBPropertiesProvider.getInstance().getOpenbravoProperties()
  // .getProperty("tesseract.path");
  //
  // String tesseractOptions = OBPropertiesProvider.getInstance().getOpenbravoProperties()
  // .getProperty("tesseract.options");
  //
  // if (tesseractOptions == null)
  // tesseractOptions = "";
  //
  // if (tesseractPath == null || tesseractPath.equals(""))
  // return null;
  //
  // try {
  // String randomCode = String.valueOf(Math.round(Math.random() * 1000000));
  // String captchaPath = getCatchaReniecImage(randomCode);
  // getProcessImageForReniec(captchaPath);
  // String captchaCode = getOCR(captchaPath, tesseractPath, tesseractOptions);
  //
  // counter++;
  //
  // StringBuilder sb_parametros = new StringBuilder();
  //
  // sb_parametros.append("accion=buscar&");
  // sb_parametros.append("nuDni=");
  // sb_parametros.append(dni);
  // sb_parametros.append("&");
  // sb_parametros.append("imagen=");
  // sb_parametros.append(captchaCode.toUpperCase().trim());
  //
  // String parametros = sb_parametros.toString();
  // StringBuilder sb_url = new StringBuilder();
  // sb_url.append("https://cel.reniec.gob.pe/valreg/valreg.do;");
  //
  // List<HttpCookie> cookies = (List<HttpCookie>) buscarCookieLogueo.get(randomCode);
  // for (HttpCookie httpCookie : cookies) {
  // if (httpCookie.getName().compareToIgnoreCase("jsessionid") == 0
  // || httpCookie.getName().compareToIgnoreCase("itmrconsrucsession") == 0) {
  // sb_url.append(httpCookie.getName());
  // sb_url.append("=");
  // sb_url.append(httpCookie.getValue());
  // }
  // }
  //
  // String[] rpta;
  // rpta = new PeticionCookie().peticionConCookieStringSSL(sb_url.toString(), "POST", parametros,
  // cookies);
  //
  // if (counter > 1000) {
  // buscarCookieLogueo.clear();// LIMPIAR DE VEZ EN CUANDO AUNQUE A VECES FALLE
  // }
  //
  // return rpta;
  // } catch (Exception ex) {
  // return null;
  // }
  // }

  // public static TipoCambio getTipoSBS(String sbsUrl, String strYear, String strMonth, String
  // strDay)
  // throws Exception {
  // TipoCambio tipoCambio = null;
  // Document doc = Jsoup.connect(sbsUrl).get();
  //
  // Iterator<Element> tables = doc.select("table[class=APLI_tabla]").iterator();
  //
  // Element table = tables.next();
  // Iterator<Element> dolar = null;
  // if (table != null) {
  // dolar = table.select("td[class=APLI_fila3]").iterator();
  // if (dolar.hasNext() && dolar.next().text().contains("Dólar de N.A")) {
  // tipoCambio = new TipoCambio();
  // tipoCambio.day = Integer.parseInt(strDay);
  // tipoCambio.month = Integer.parseInt(strMonth);
  // tipoCambio.year = Integer.parseInt(strYear);
  //
  // Iterator<Element> dolarTc = table.select("td[class=APLI_fila2]").iterator();
  // tipoCambio.tcCompra = Double.parseDouble(dolarTc.next().text());
  // tipoCambio.tcVenta = Double.parseDouble(dolarTc.next().text());
  // }
  // }
  //
  // return tipoCambio;
  // }

  public static String getOCR(String filepath, String tesseractPath, String tesseractOptions) {

    try {
      String tmpFile = "tmp_" + Math.round(Math.random() * 1000000);
      File outputfile = File.createTempFile(tmpFile, ".txt");

      Process p = null;
      if (tesseractOptions.equals(""))
        p = Runtime.getRuntime().exec(new String[] { tesseractPath, filepath,
            outputfile.getAbsolutePath().replace(".txt", "") });
      else
        p = Runtime.getRuntime().exec(new String[] { tesseractPath, filepath,
            outputfile.getAbsolutePath().replace(".txt", ""), "nobatch", tesseractOptions });
      // SOLO FUNCIONA CON JAVA 8
      /*
       * boolean success = p.waitFor(5000, TimeUnit.MILLISECONDS); if (!success) { return null; }
       */
      int ret = p.waitFor();
      if (ret != 0)
        return null;

      FileInputStream inputStream = new FileInputStream(outputfile.getAbsolutePath());
      try {
        String captchsolved = IOUtils.toString(inputStream);
        return captchsolved;
      } finally {
        inputStream.close();
      }

    } catch (Exception ex) {
      return "";
    }

  }

  public static List<TipoCambio> getListTipoCambio(String strYear, String strMonth)
      throws Exception {
    Document doc = Jsoup.connect(
        "http://www.sunat.gob.pe/cl-at-ittipcam/tcS01Alias?mes=" + strMonth + "&anho=" + strYear)
        .get();

    Iterator<Element> tables = doc.select("table[width=81%]").iterator();
    tables.next();

    Element table = tables.next();
    Iterator<Element> tdDias = null;
    if (table != null) {
      tdDias = table.select("td[class=H3]").iterator();

    }

    List<TipoCambio> listTipoCambio = null;

    if (tdDias != null) {
      listTipoCambio = new ArrayList<TipoCambio>();
      while (tdDias.hasNext()) {
        TipoCambio tipoCambio = new TipoCambio();
        tipoCambio.day = Integer.parseInt(tdDias.next().text());
        tipoCambio.month = Integer.parseInt(strMonth);
        tipoCambio.year = Integer.parseInt(strYear);
        listTipoCambio.add(tipoCambio);
      }

      Iterator<Element> tdTipoCambio = table.select("td[class=tne10]").iterator();

      int index = 0;
      int i = 0;
      while (tdTipoCambio.hasNext()) {
        TipoCambio tipoCambio = listTipoCambio.get(index);
        if (i % 2 == 0) {
          tipoCambio.tcCompra = Double.parseDouble(tdTipoCambio.next().text());
        } else {
          tipoCambio.tcVenta = Double.parseDouble(tdTipoCambio.next().text());
          index++;
        }
        i++;
      }

    }
    return listTipoCambio;
  }

}
