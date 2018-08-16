/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package it.extrasys.utility.report.common;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.openbravo.base.provider.OBConfigFileProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.system.ClientInformation;
import org.openbravo.model.ad.system.SystemInformation;
import org.openbravo.model.ad.utility.Image;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.utils.FileUtility;

public class Utility {

  static Logger log4j = Logger.getLogger(Utility.class);

  /**
   * Provides the image logo as a InputStream object.
   * 
   * @param logo
   *          The name of the logo to display
   * @param org
   *          The organization id used to get the logo
   * @return The image requested
   * @throws IOException
   * @see #getImageLogo(String,String)
   */
  public static InputStream getImageLogoInputStream(String logo, String org) {
    return new ByteArrayInputStream(getImageLogo(logo, org));
  }

  /**
   * Provides the image logo as a BufferedImage object.
   * 
   * @param logo
   *          The name of the logo to display
   * @param org
   *          The organization id used to get the logo
   * @return The image requested
   * @throws IOException
   * @see #getImageLogo(String,String)
   */
  public static BufferedImage showImageLogo(String logo, String org) throws IOException {
    return ImageIO.read(new ByteArrayInputStream(getImageLogo(logo, org)));
  }

  /**
   * Provides the image logo as a byte array for the indicated parameters.
   * 
   * @param logo
   *          The name of the logo to display This can be one of the following: yourcompanylogin,
   *          youritservicelogin, yourcompanymenu, yourcompanybig or yourcompanydoc
   * @param org
   *          The organization id used to get the logo In the case of requesting the yourcompanydoc
   *          logo you can indicate the organization used to request the logo.
   * @return The image requested
   */
  public static byte[] getImageLogo(String logo, String org) {

    // OBContext.setAdminMode();
    OBContext.setAdminContext();
    try {
      Image img = null;

      if ("yourcompanylogin".equals(logo)) {
        img = OBDal.getInstance().get(SystemInformation.class, "0").getYourCompanyLoginImage();
        return defaultImageLogo(img, "web/images/CompanyLogo_big.png");
      } else if ("youritservicelogin".equals(logo)) {
        img = OBDal.getInstance().get(SystemInformation.class, "0").getYourItServiceLoginImage();
        return defaultImageLogo(img, "web/images/SupportLogo_big.png");
      } else if ("yourcompanymenu".equals(logo)) {
        img = OBDal.getInstance()
            .get(ClientInformation.class, OBContext.getOBContext().getCurrentClient().getId())
            .getYourCompanyMenuImage();
        if (img == null) {
          img = OBDal.getInstance().get(SystemInformation.class, "0").getYourCompanyMenuImage();
        }
        return defaultImageLogo(img, "web/images/CompanyLogo_small.png");
      } else if ("yourcompanybig".equals(logo)) {
        img = OBDal.getInstance()
            .get(ClientInformation.class, OBContext.getOBContext().getCurrentClient().getId())
            .getYourCompanyBigImage();
        if (img == null) {
          img = OBDal.getInstance().get(SystemInformation.class, "0").getYourCompanyBigImage();
        }
        return defaultImageLogo(img, "web/skins/ltr/Default/Login/initialOpenbravoLogo.png");
      } else if ("yourcompanydoc".equals(logo)) {
        if (org != null && !org.equals("")) {
          Organization organization = OBDal.getInstance().get(Organization.class, org);
          img = organization.getOrganizationInformationList().get(0).getYourCompanyDocumentImage();
        }
        if (img == null) {
          img = OBDal.getInstance().get(SystemInformation.class, "0").getYourCompanyDocumentImage();
        }
        return defaultImageLogo(img, "web/images/CompanyLogo_big.png");
        // } else if ("banner-production".equals(logo)) {
        // img = OBDal.getInstance().get(SystemInformation.class, "0").getProductionBannerImage();
        // return defaultImageLogo(img, "web/images/blank.gif");
      } else if ("yourcompanylegal".equals(logo)) {
        if (org != null && !org.equals("")) {
          Organization organization = OBDal.getInstance().get(Organization.class, org);
          img = organization.getOrganizationInformationList().get(0).getYourCompanyDocumentImage();
        }
        if (img == null) {

          img = OBDal.getInstance()
              .get(ClientInformation.class, OBContext.getOBContext().getCurrentClient().getId())
              .getYourCompanyDocumentImage();

          if (img == null) {
            img = OBDal.getInstance().get(SystemInformation.class, "0")
                .getYourCompanyDocumentImage();
          }
        }
        return defaultImageLogo(img, "web/images/CompanyLogo_big.png");
      } else {
        log4j.error("Logo key does not exist: " + logo);
        return getBlankImage();
      }
    } catch (Exception e) {
      log4j.error("Could not load logo from database: " + logo + ", " + org, e);
      return getBlankImage();
    } finally {
      // OBContext.restorePreviousMode();
      OBContext.resetAsAdminContext();
    }
  }

  private static byte[] defaultImageLogo(Image img, String path) throws IOException {

    if (img == null) {
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      new FileUtility(OBConfigFileProvider.getInstance().getServletContext().getRealPath("/"),
          path, false, true).dumpFile(bout);
      bout.close();
      return bout.toByteArray();
    } else {
      return img.getBindaryData();
    }
  }

  private static byte[] getBlankImage() {

    try {
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      new FileUtility(OBConfigFileProvider.getInstance().getServletContext().getRealPath("/"),
          "web/images/blank.gif", false, true).dumpFile(bout);
      bout.close();
      return bout.toByteArray();
    } catch (IOException ex) {
      log4j.error("Could not load blank image.");
      return new byte[0];
    }
  }

  public static String getRealPath() {
    return OBConfigFileProvider.getInstance().getServletContext().getRealPath("/");
  }

  public static String getMessage(String messageId, String language) {
    return org.openbravo.erpCommon.utility.Utility.messageBD(new DalConnectionProvider(),
        messageId, language);
  }

  public static String getOrderNumbersByShipmentId(String mInoutId) {
    String result = "";
    ShipmentInOut minout = OBDal.getInstance().get(ShipmentInOut.class, mInoutId);
    List<ShipmentInOutLine> lines = minout.getMaterialMgmtShipmentInOutLineList();
    Map<String, Order> docNumMap = new HashMap<String, Order>();
    if (lines != null) {
      for (ShipmentInOutLine line : lines) {
        OrderLine orderLine = line.getSalesOrderLine();
        if (orderLine != null) {
          docNumMap.put(orderLine.getSalesOrder().getId(), orderLine.getSalesOrder());
        }
      }
    }
    if (docNumMap.size() > 0) {
      Set<Entry<String, Order>> keys = docNumMap.entrySet();
      for (Entry<String, Order> entry : keys) {
        Order order = entry.getValue();
        SimpleDateFormat sdf = new SimpleDateFormat(OBPropertiesProvider.getInstance()
            .getOpenbravoProperties().getProperty("dateFormat.java"));// dateFormat.java
        if (order.getOrderReference() != null) {
          result = result + order.getDocumentNo() + " Del " + sdf.format(order.getOrderDate())
              + " ( Vs. Ordine n. " + order.getOrderReference() + ") " + ",";
        } else {
          result = result + order.getDocumentNo() + " Del " + sdf.format(order.getOrderDate())
              + ",";
        }

      }
      result = result.substring(0, result.length() - 1);
    }

    return result;
  }

  public static String getShipmentNumbersByInvoiceId(String cInvoiceId) {
    String result = "";
    Invoice invoice = OBDal.getInstance().get(Invoice.class, cInvoiceId);
    List<InvoiceLine> lines = invoice.getInvoiceLineList();
    Map<String, ShipmentInOut> docNumMap = new HashMap<String, ShipmentInOut>();
    if (lines != null) {
      for (InvoiceLine line : lines) {
        ShipmentInOutLine shipmentLine = line.getGoodsShipmentLine();
        if (shipmentLine != null) {
          docNumMap.put(shipmentLine.getShipmentReceipt().getId(),
              shipmentLine.getShipmentReceipt());
        }
      }
    }
    if (docNumMap.size() > 0) {
      Set<Entry<String, ShipmentInOut>> keys = docNumMap.entrySet();
      for (Entry<String, ShipmentInOut> entry : keys) {
        ShipmentInOut shipment = entry.getValue();
        SimpleDateFormat sdf = new SimpleDateFormat(OBPropertiesProvider.getInstance()
            .getOpenbravoProperties().getProperty("dateFormat.java"));// dateFormat.java
        if (shipment.getOrderReference() != null) {
          result = result + shipment.getDocumentNo() + " Del "
              + sdf.format(shipment.getOrderDate()) + " ( Vs. Ordine n. "
              + shipment.getOrderReference() + ") " + ",";
        } else {
          result = result + shipment.getDocumentNo() + " Del "
              + sdf.format(shipment.getOrderDate()) + ",";
        }
      }
      result = result.substring(0, result.length() - 1);
    }

    return result;
  }

  public static String getOrderNumbersByInvoiceId(String cInvoiceId) {
    String result = "";
    Invoice invoice = OBDal.getInstance().get(Invoice.class, cInvoiceId);
    List<InvoiceLine> lines = invoice.getInvoiceLineList();
    Map<String, Order> docNumMap = new HashMap<String, Order>();
    if (lines != null) {
      for (InvoiceLine line : lines) {
        OrderLine orderLine = line.getSalesOrderLine();
        if (orderLine != null) {
          docNumMap.put(orderLine.getSalesOrder().getId(), orderLine.getSalesOrder());
        }
      }
    }
    if (docNumMap.size() > 0) {
      Set<Entry<String, Order>> keys = docNumMap.entrySet();
      for (Entry<String, Order> entry : keys) {
        Order order = entry.getValue();
        SimpleDateFormat sdf = new SimpleDateFormat(OBPropertiesProvider.getInstance()
            .getOpenbravoProperties().getProperty("dateFormat.java"));// dateFormat.java
        if (order.getOrderReference() != null) {
          result = result + order.getDocumentNo() + " Del " + sdf.format(order.getOrderDate())
              + " ( Vs. Ordine n. " + order.getOrderReference() + ") " + ",";
        } else {
          result = result + order.getDocumentNo() + " Del " + sdf.format(order.getOrderDate())
              + ",";
        }
      }
      result = result.substring(0, result.length() - 1);
    }

    return result;
  }
  
  public static BufferedImage showImage(Image im) throws IOException {
    return ImageIO.read(new ByteArrayInputStream(im.getBindaryData()));
  }
  
  public static BufferedImage showImageByURL(String urlPath) throws IOException {
    return ImageIO.read(new ByteArrayInputStream(defaultImageLogo(null,urlPath)));
  }

}
