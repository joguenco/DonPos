//    uniCenta oPOS  - Touch Friendly Point Of Sale
//    Copyright (c) 2009-2018 uniCenta & previous Openbravo POS works
//    https://unicenta.com
//
//    This file is part of uniCenta oPOS
//
//    uniCenta oPOS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//   uniCenta oPOS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with uniCenta oPOS.  If not, see <http://www.gnu.org/licenses/>.

package com.unicenta.pos.printer.ticket;

import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author JG uniCenta
 */
@Slf4j
public abstract class BasicTicket implements PrintItem {

    /**
     *
     */
    protected java.util.List<PrintItem> m_aCommands;

    /**
     *
     */
    protected PrintItemLine pil;

    /**
     *
     */
    protected int m_iBodyHeight;

    /**
     * Creates a new instance of AbstractTicket
     */
    public BasicTicket() {
// JG 16 May 12 use diamond inference
        m_aCommands = new ArrayList<>();
        pil = null;
        m_iBodyHeight = 0;
    }

    /**
     * @return
     */
    protected abstract Font getBaseFont();

    /**
     * @return
     */
    protected abstract int getFontHeight();

    /**
     * @return
     */
    protected abstract double getImageScale();

    /**
     * @return
     */
    @Override
    public int getHeight() {
        return m_iBodyHeight;
    }

    /**
     * @param g2d
     * @param x
     * @param y
     * @param width
     */
    @Override
    public void draw(Graphics2D g2d, int x, int y, int width) {

        int currenty = y;
        for (PrintItem pi : m_aCommands) {
            pi.draw(g2d, x, currenty, width);
            currenty += pi.getHeight();
        }

        saveTicket(g2d, x, y, width);

    }

    private void saveTicket(Graphics2D g2d, int x, int y, int width) {

        int receiptHeight = 5;

        PrintItem pi;
        for(Iterator i$ = m_aCommands.iterator(); i$.hasNext(); receiptHeight += pi.getHeight()) {
            pi = (PrintItem)i$.next();
        }

        BufferedImage image = new BufferedImage(width + 10, receiptHeight, 1);
        g2d = image.createGraphics();
        int currenty = y;

        for(Iterator i$ = this.m_aCommands.iterator(); i$.hasNext(); currenty += pi.getHeight()) {
            pi = (PrintItem)i$.next();
            if (pi instanceof PrintItemImage)  {
                ((PrintItemImage) pi).setImage(negative(((PrintItemImage) pi).getImage()));
            }
            pi.draw(g2d, x, currenty, width);
        }

        try {
            ImageIO.write(this.negative(image), "png", new File(System.getProperty("user.home") + "/receipt.png"));
        }
        catch (Exception e) {
            System.out.println("Error serialising receipt image !!! : " + e);
        }

    }

    public BufferedImage negative(BufferedImage img) {
        for (int x = 0; x < img.getWidth(); ++x) {
            for (int y = 0; y < img.getHeight(); ++y) {
                int RGBA = img.getRGB(x, y);
                Color col = new Color(RGBA, true);
                col = new Color(Math.abs(col.getRed() - 255), Math.abs(col.getGreen() - 255), Math.abs(col.getBlue() - 255));
                img.setRGB(x, y, col.getRGB());
            }
        }

        return img;
    }

    /**
     * @return
     */
    public java.util.List<PrintItem> getCommands() {
        return m_aCommands;
    }

    // INTERFAZ PRINTER 2

    /**
     * @param image
     */
    public void printImage(BufferedImage image) {

        PrintItem pi = new PrintItemImage(image, getImageScale());
        m_aCommands.add(pi);
        m_iBodyHeight += pi.getHeight();
    }

    /**
     * @param iTextSize
     */
    public void beginLine(int iTextSize) {
        pil = new PrintItemLine(iTextSize, getBaseFont(), getFontHeight());
    }

    /**
     * @param iStyle
     * @param sText
     */
    public void printText(int iStyle, String sText) {
        if (pil != null) {
            pil.addText(iStyle, sText);
        }
    }

    /**
     *
     */
    public void endLine() {
        if (pil != null) {
            m_aCommands.add(pil);
            m_iBodyHeight += pil.getHeight();
            pil = null;
        }
    }
}