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
//    along with uniCenta oPOS.  If not, see <http://www.gnu.org/licenses/>

package com.unicenta.pos.forms;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Locale;
import java.util.Properties;


/**
 * Creation and Editing of stored settings
 *
 * @author JG uniCenta
 */
@Slf4j
public class AppConfig implements AppProperties {

  private static AppConfig m_instance = null;
  private Properties properties;
  private File configfile;

  /**
   * Set configuration array
   *
   * @param args array strings
   */
  public AppConfig(String[] args) {
    if (args.length == 0) {
      init(getDefaultConfig());
    } else {
      init(new File(args[0]));
    }
  }

  /**
   * unicenta resources file
   *
   * @param configfile resource file
   */
  public AppConfig(File configfile) {
    init(configfile);
    this.configfile = configfile;
    properties = new Properties();
    load();
    //log.debug("Reading configuration file: {}", configfile.getAbsolutePath());
  }

  private void init(File configfile) {
    this.configfile = configfile;
    properties = new Properties();
    //log.debug("Reading configuration file: {}", configfile.getAbsolutePath());
  }

  private File getDefaultConfig() {
    return new File(new File(System.getProperty("user.home"))
            , AppLocal.APP_ID + ".properties");
  }

  /**
   * Get key pair value from properties resource
   *
   * @param sKey key pair value
   * @return key pair from .properties filename
   */
  @Override
  public String getProperty(String sKey) {
    return properties.getProperty(sKey);
  }

  /**
   * @return Machine name
   */
  @Override
  public String getHost() {
    return getProperty("machine.hostname");
  }

  /**
   * @return .properties filename
   */
  @Override
  public File getConfigFile() {
    return configfile;
  }


  public String getTicketHeaderLine1() {
    return getProperty("tkt.header1");
  }

  public String getTicketHeaderLine2() {
    return getProperty("tkt.header2");
  }

  public String getTicketHeaderLine3() {
    return getProperty("tkt.header3");
  }

  public String getTicketHeaderLine4() {
    return getProperty("tkt.header4");
  }

  public String getTicketHeaderLine5() {
    return getProperty("tkt.header5");
  }

  public String getTicketHeaderLine6() {
    return getProperty("tkt.header6");
  }

  public String getTicketFooterLine1() {
    return getProperty("tkt.footer1");
  }

  public String getTicketFooterLine2() {
    return getProperty("tkt.footer2");
  }

  public String getTicketFooterLine3() {
    return getProperty("tkt.footer3");
  }

  public String getTicketFooterLine4() {
    return getProperty("tkt.footer4");
  }

  public String getTicketFooterLine5() {
    return getProperty("tkt.footer5");
  }

  public String getTicketFooterLine6() {
    return getProperty("tkt.footer6");
  }

  /**
   * Update .properties resource key pair values
   *
   * @param sKey   key pair left side
   * @param sValue key pair right side value
   */
  public void setProperty(String sKey, String sValue) {
    if (sValue == null) {
      properties.remove(sKey);
    } else {
      properties.setProperty(sKey, sValue);
    }
  }

  /**
   * Local machine identity
   *
   * @return Machine name from OS
   */
  private String getLocalHostName() {
    try {
      return java.net.InetAddress.getLocalHost().getHostName();
    } catch (java.net.UnknownHostException eUH) {
      return "localhost";
    }
  }

  public static AppConfig getInstance() {

    if (m_instance == null) {
      m_instance = new AppConfig(new File(System.getProperty("user.home")
              , AppLocal.APP_ID + ".properties"));
    }
    return m_instance;
  }

  public Boolean getBoolean(String sKey) {
    return Boolean.valueOf(properties.getProperty(sKey));
  }

  public void setBoolean(String sKey, Boolean sValue) {
    if (sValue == null) {
      properties.remove(sKey);
    } else if (sValue) {
      properties.setProperty(sKey, "true");
    } else {
      properties.setProperty(sKey, "false");
    }
  }

  /**
   * @return Delete .properties filename
   */
  public boolean delete() {
    loadDefault();
    return configfile.delete();
  }

  /**
   * Get instance settings
   */
  public void load() {

    loadDefault();

    try {
      InputStream in = new FileInputStream(configfile);
      if (in != null) {
        properties.load(in);
        in.close();
      }
    } catch (IOException e) {
      loadDefault();
    }

  }

  private void pullLatestProperties() {
    try {
      InputStream in = new FileInputStream(configfile);
      if (in != null) {
        properties.load(in);
        in.close();
      }
    } catch (IOException e) {
      loadDefault();
    }
  }

  /**
   * @return 0 or 00 number keypad boolean true/false
   */
  public Boolean isPriceWith00() {
    String prop = getProperty("pricewith00");
    if (prop == null) {
      return false;
    } else {
      return prop.equals("true");
    }
  }

  public void setLastTicket(String lastTicketNumber, String lastTicketType, String lastTicketSerieNumber) throws IOException {

    pullLatestProperties();
    properties.setProperty("lastticket.number", lastTicketNumber);
    properties.setProperty("lastticket.type", lastTicketType);
    properties.setProperty("lastticket.serieNumber", lastTicketSerieNumber);

    OutputStream out = new FileOutputStream(configfile);
    if (out != null) {
      properties.store(out, AppLocal.APP_NAME + ". Configuration file.");
      out.close();
    }
  }

  /**
   * Save values to properties file
   *
   * @throws java.io.IOException explicit on OS
   */
  public void save() throws IOException {

    OutputStream out = new FileOutputStream(configfile);
    if (out != null) {
      properties.store(out, AppLocal.APP_NAME + ". Configuration file.");
      out.close();
    }
  }


  private void loadDefault() {

    properties = new Properties();
    String dirname = System.getProperty("dirname.path");
    dirname = dirname == null ? "./" : dirname;

    setDatabaseProperties(properties, dirname);
    setPrinterProperties(properties);
    setLocalProperties(properties);
    setPaymentProperties(properties);
    setDeviceProperties(properties);
    setTableProperties(properties);

  }


  private void setTableProperties(Properties properties){
    properties.setProperty("table.showcustomerdetails", "true");
    properties.setProperty("table.customercolour", "#58B000");
    properties.setProperty("table.showwaiterdetails", "true");
    properties.setProperty("table.waitercolour", "#258FB0");
    properties.setProperty("table.tablecolour", "#D62E52");
    properties.setProperty("till.amountattop", "true");
    properties.setProperty("till.hideinfo", "true");
  }

  private void setDeviceProperties(Properties properties) {
    properties.setProperty("machine.display", "screen");
    properties.setProperty("machine.scale", "Not defined");
    properties.setProperty("machine.screenmode", "fullscreen");
    properties.setProperty("machine.ticketsbag", "standard");
    properties.setProperty("machine.scanner", "Not defined");
    properties.setProperty("machine.iButton", "false");
    properties.setProperty("machine.iButtonResponse", "5");
    properties.setProperty("machine.uniqueinstance", "true");

    properties.setProperty("machine.hostname", getLocalHostName());
    properties.setProperty("swing.defaultlaf", System.getProperty("swing.defaultlaf","javax.swing.plaf.metal.MetalLookAndFeel"));
  }

  private void setPaymentProperties(Properties properties) {
    properties.setProperty("payment.gateway", "external");
    properties.setProperty("payment.magcardreader", "Not defined");
    properties.setProperty("payment.testmode", "true");
    properties.setProperty("payment.commerceid", "");
    properties.setProperty("payment.commercepassword", "password");
  }

  private void setLocalProperties(Properties properties) {
    Locale l = Locale.getDefault();
    properties.setProperty("user.language", l.getLanguage());
    properties.setProperty("user.country", l.getCountry());
    properties.setProperty("user.variant", l.getVariant());
  }

  private void setPrinterProperties(Properties properties) {
    // Receipt printer paper set to 72mmx200mm
    properties.setProperty("paper.receipt.x", "10");
    properties.setProperty("paper.receipt.y", "10");
    properties.setProperty("paper.receipt.width", "190");
    properties.setProperty("paper.receipt.height", "546");
    properties.setProperty("paper.receipt.mediasizename", "A4");

    // Normal printer paper for A4
    properties.setProperty("paper.standard.x", "72");
    properties.setProperty("paper.standard.y", "72");
    properties.setProperty("paper.standard.width", "451");
    properties.setProperty("paper.standard.height", "698");
    properties.setProperty("paper.standard.mediasizename", "A4");

    properties.setProperty("tkt.header1", "uniCenta oPOS");
    properties.setProperty("tkt.header2", "Touch Friendly Point Of Sale");
    properties.setProperty("tkt.header3", "Copyright (c) 2009-2018 uniCenta");
    properties.setProperty("tkt.header4", "Change header text in Configuration");

    properties.setProperty("tkt.footer1", "Change footer text in Configuration");
    properties.setProperty("tkt.footer2", "Thank you for your custom");
    properties.setProperty("tkt.footer3", "Please Call Again");

    properties.setProperty("machine.printer", "screen");
    properties.setProperty("machine.printer.2", "Not defined");
    properties.setProperty("machine.printer.3", "Not defined");
    properties.setProperty("machine.printer.4", "Not defined");
    properties.setProperty("machine.printer.5", "Not defined");
    properties.setProperty("machine.printer.6", "Not defined");

    properties.setProperty("machine.printername", "(Default)");
    properties.setProperty("screen.receipt.columns", "42");
  }

  private void setDatabaseProperties(Properties properties, String dirname) {

    properties.setProperty("db.multi", "false");
    properties.setProperty("override.check", "false");
    properties.setProperty("override.pin", "");

    // Default database
    properties.setProperty("db.driverlib", new File(new File(dirname), "mariadb-java-client-3.5.0.jar").getAbsolutePath());
    properties.setProperty("db.engine", "MariaDB");
    properties.setProperty("db.driver", "org.mariadb.jdbc.Driver");

    properties.setProperty("db.name", "Main DB");
    properties.setProperty("db.URL", "jdbc:mariadb://localhost:3306/");
    properties.setProperty("db.schema", "donpos");
    properties.setProperty("db.options", "?zeroDateTimeBehavior=convertToNull");
    properties.setProperty("db.user", "donpos");
    properties.setProperty("db.password", "");


// secondary DB
    properties.setProperty("db1.name", "");
    properties.setProperty("db1.URL", "jdbc:mariadb://localhost:3306/");
    properties.setProperty("db1.schema", "donpos");
    properties.setProperty("db1.options", "?zeroDateTimeBehavior=convertToNull");
    properties.setProperty("db1.user", "");
    properties.setProperty("db1.password", "");

  }

}
