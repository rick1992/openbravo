package pe.com.unifiedgo.core.addons;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class SimpleImageProcessing {

  BufferedImage out;
  BufferedImage localIn;
  private int sizeFilter = 1;

  public void setNumberOfNeighbours(int num) {
    // being odd
    if (num % 2 != 0) {
      sizeFilter = (num - 1) / 2;
    }
  }

  public BufferedImage getErosionResult() {
    return out;
  }

  public boolean isNear(int val, int closeTo, int range) {
    if (Math.abs((val & 0xff) - (closeTo & 0xff)) > range)
      return false;
    if (Math.abs(((val & 0x0000ff00) >> 8) - ((closeTo & 0x0000ff00) >> 8)) > range)
      return false;
    if (Math.abs(((val & 0x00ff0000) >> 16) - ((closeTo & 0x00ff0000) >> 16)) > range)
      return false;

    return true;
  }

  public SimpleImageProcessing(BufferedImage in) {
    // localIn = in;
    // initing and copying the image
    localIn = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = localIn.createGraphics();
    // drawing the input image inside the new image
    g2d.drawImage(in, 0, 0, null);
    System.out.println(in.getWidth() + " " + in.getHeight());
    // init output Image
    out = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_RGB);
    for (int w = 0; w < in.getWidth(); w++) {
      for (int h = 0; h < in.getHeight(); h++) {

        if (h < (in.getHeight() - 1) && w < (in.getWidth() - 1)
            && isNear(localIn.getRGB(w, h), 0x203040, 30)
            && isNear(localIn.getRGB(w, h + 1), 0x203040, 30)
            && !isNear(localIn.getRGB(w + 1, h), 0x203040, 30))
          localIn.setRGB(w, h, 0x00FFFFFF);
        localIn.setRGB(w, h, grayscale(localIn.getRGB(w, h)));

        out.setRGB(w, h, 0xFFFFFF);
      }
    }
  }

  public void runErosionWidth() {
    // no automatic , only doing
    for (int h = 0; h < out.getHeight(); h++) {
      for (int w = sizeFilter; w < (out.getWidth() - sizeFilter); w++) {
        int accNeigh = 0;
        int total = 0;
        for (int i = -sizeFilter; i < (sizeFilter + 1); i++) {
          accNeigh = accNeigh + tak(localIn.getRGB(w + i, h));
          total++;
        }

        int rgb = accNeigh / total;
        if ((rgb & 0x000000FF) > 200)
          rgb = 255;
        if ((rgb & 0x000000FF) < 10)
          rgb = 0;

        out.setRGB(w, h, (rgb & 0x000000FF) + (rgb << 8 & 0x0000FF00) + (rgb << 16 & 0x00FF0000));

      } // w
    } // h

  }// runErosionWidth

  public void runErosionHeight() {
    for (int w = 0; w < (out.getWidth()); w++) {
      for (int h = sizeFilter; h < (out.getHeight() - sizeFilter); h++) {
        int accNeigh = 0;
        int total = 0;
        for (int i = -sizeFilter; i < (sizeFilter + 1); i++) {
          accNeigh = accNeigh + tak(localIn.getRGB(w, h + i));
          total++;
        }

        int rgb = accNeigh / total;
        if ((rgb & 0x000000FF) > 240)
          rgb = 255;
        if ((rgb & 0x000000FF) < 20)
          rgb = 0;

        out.setRGB(w, h, (rgb & 0x000000FF) + (rgb << 8 & 0x0000FF00) + (rgb << 16 & 0x00FF0000));

      } // w
    } // h

  }

  private int tak(int rgb) {
    return (rgb & 0xFF);
  }

  private int grayscale(int rgb) {
    return (int) ((double) (rgb & 0xFF) * 0.07 + (double) (rgb >> 8 & 0xFF) * 0.21
        + (double) (rgb >> 16 & 0xFF) * 0.7);
  }

  public void runErosion() {
    // no automatic , only doing
    for (int h = sizeFilter; h < (out.getHeight() - sizeFilter); h++) {
      for (int w = sizeFilter; w < (out.getWidth() - sizeFilter); w++) {
        int accNeigh = 0;
        int total = 0;
        /*
         * for(int i=-sizeFilter; i < 0; i++) { for(int j=-sizeFilter; j < 0; j++) { accNeigh =
         * accNeigh + tak(localIn.getRGB(w+i, h+j)); total++; } } for(int i=1; i< ( sizeFilter+1);
         * i++) { for(int j=1; j< ( sizeFilter+1); j++) { accNeigh = accNeigh +
         * tak(localIn.getRGB(w+i, h+j)); total++; } }
         */

        int rgb = localIn.getRGB(w, h);// accNeigh/total;
        if ((rgb & 0x000000FF) > 200)
          rgb = 255;
        if ((rgb & 0x000000FF) < 10)
          rgb = 0;
        out.setRGB(w, h, (rgb & 0x000000FF) + (rgb << 8 & 0x0000FF00) + (rgb << 16 & 0x00FF0000));

      } // w
    } // h
  }// runErosion

  public void runGaussian() {
    // no automatic , only doing
    for (int h = 1; h < (out.getHeight() - 1); h++) {
      for (int w = 1; w < (out.getWidth() - 1); w++) {

        int accNeigh = (localIn.getRGB(w - 1, h - 1) & 0xFF) + (localIn.getRGB(w - 1, h + 1) & 0xFF)
            + (localIn.getRGB(w + 1, h + 1) & 0xFF) + (localIn.getRGB(w + 1, h - 1) & 0xFF);
        accNeigh = accNeigh + (localIn.getRGB(w - 1, h) & 0xFF) * 2
            + (localIn.getRGB(w, h - 1) & 0xFF) * 2 + (localIn.getRGB(w + 1, h) & 0xFF) * 2
            + (localIn.getRGB(w, h + 1) & 0xFF) * 2 + (localIn.getRGB(w, h) & 0xFF) * 4;

        int total = 16;

        int rgb = accNeigh / total;
        out.setRGB(w, h, (rgb & 0x000000FF) + (rgb << 8 & 0x0000FF00) + (rgb << 16 & 0x00FF0000));

      } // w
    } // h
  }// runGaussian

  public void runBinary() {
    // no automatic , only doing
    for (int h = 1; h < (out.getHeight() - 1); h++) {
      for (int w = 1; w < (out.getWidth() - 1); w++) {

        int rgb = (localIn.getRGB(w, h) & 0xFF);
        if (rgb > 150)
          rgb = 0xFF;
        else
          rgb = 0x00;
        out.setRGB(w, h, (rgb & 0x000000FF) + (rgb << 8 & 0x0000FF00) + (rgb << 16 & 0x00FF0000));

      } // w
    } // h
  }// runBinary

}// class