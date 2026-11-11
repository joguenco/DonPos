//    uniCenta oPOS  - Touch Friendly Point Of Sale
//    Copyright (c) 2009-2018 uniCenta
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

package com.unicenta.pos.config;

import com.unicenta.data.gui.JMessageDialog;
import com.unicenta.data.gui.MessageInf;
import com.unicenta.data.loader.Session;
import com.unicenta.data.user.DirtyManager;
import com.unicenta.pos.forms.AppConfig;
import com.unicenta.pos.forms.AppLocal;
import com.unicenta.pos.forms.DriverWrapper;
import com.unicenta.pos.util.AltEncrypter;
import com.unicenta.pos.util.DirectoryEvent;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.*;

/**
 * @author Jack Gerrard
 * @author adrianromero
 */
@Slf4j
public class JPanelConfigDatabase extends javax.swing.JPanel implements PanelConfig {

    private final DirtyManager dirty = new DirtyManager();

    /** Creates new form JPanelConfigDatabase */
    public JPanelConfigDatabase() {

        initComponents();

        dbDriverLibraryValue.getDocument().addDocumentListener(dirty);
        dbDriverClassValue.getDocument().addDocumentListener(dirty);
        dbDriverLibrarySelect.addActionListener(new DirectoryEvent(dbDriverLibraryValue));
        jcboDBDriver.addActionListener(dirty);
        jcboDBDriver.addItem("MariaDB");
        jcboDBDriver.setSelectedIndex(0);
        multiDBButton.addActionListener(dirty);

// primary DB        
        dbNameValue.getDocument().addDocumentListener(dirty);
        dbURLValue.getDocument().addDocumentListener(dirty);
        jtxtDbSchema.getDocument().addDocumentListener(dirty);
        jtxtDbOptions.getDocument().addDocumentListener(dirty);
        dbPasswordValue.getDocument().addDocumentListener(dirty);
        dbUserValue.getDocument().addDocumentListener(dirty);
        jCBSchema.addActionListener(dirty);

// secondary DB        
        db2NameValue.getDocument().addDocumentListener(dirty);
        db2URLValue.getDocument().addDocumentListener(dirty);
        jtxtDbSchema1.getDocument().addDocumentListener(dirty);
        db2OptionsValue.getDocument().addDocumentListener(dirty);
        db2PasswordValue.getDocument().addDocumentListener(dirty);
        db2UserValue.getDocument().addDocumentListener(dirty);
        jCBSchema1.addActionListener(dirty);

        dbConfigPanel.setVisible(false);
        validUserAlert.setVisible(true);
        dbVersionLabel.setVisible(false);
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasChanged() {
        return dirty.isDirty();
    }

    /**
     *
     * @return
     */
    @Override
    public Component getConfigComponent() {
        return this;
    }

    /**
     *
     * @param config
     */
    @Override
    public void loadProperties(AppConfig config) {

        multiDBButton.setSelected(Boolean.parseBoolean(config.getProperty("db.multi")));

        jcboDBDriver.setSelectedItem(config.getProperty("db.engine"));
        dbDriverLibraryValue.setText(config.getProperty("db.driverlib"));
        dbDriverClassValue.setText(config.getProperty("db.driver"));

// primary DB
        dbNameValue.setText(config.getProperty("db.name"));
        dbURLValue.setText("jdbc:mysql://localhost:3306/");
        dbURLValue.setText(config.getProperty("db.URL"));
        jtxtDbSchema.setText(config.getProperty("db.schema"));
        jtxtDbOptions.setText(config.getProperty("db.options"));
        String sDBUser = config.getProperty("db.user");
        String sDBPassword = config.getProperty("db.password");

        if (sDBUser != null && sDBPassword != null && sDBPassword.startsWith("crypt:")) {
            AltEncrypter cypher = new AltEncrypter("cypherkey" + sDBUser);
            sDBPassword = cypher.decrypt(sDBPassword.substring(6));
        }
        dbUserValue.setText(sDBUser);
        dbPasswordValue.setText(sDBPassword);

// secondary DB
        db2NameValue.setText(config.getProperty("db1.name"));
        db2URLValue.setText(config.getProperty("db1.URL"));
        jtxtDbSchema1.setText(config.getProperty("db1.schema"));
        jtxtDbSchema1.setEnabled(false);
        db2OptionsValue.setText(config.getProperty("db1.options"));
        String sDBUser1 = config.getProperty("db1.user");
        String sDBPassword1 = config.getProperty("db1.password");

        if (sDBUser1 != null && sDBPassword1 != null && sDBPassword1.startsWith("crypt:")) {
            AltEncrypter cypher = new AltEncrypter("cypherkey" + sDBUser1);
            sDBPassword1 = cypher.decrypt(sDBPassword1.substring(6));
        }
        db2UserValue.setText(sDBUser1);
        db2PasswordValue.setText(sDBPassword1);

        dirty.setDirty(false);
    }

    /**
     *
     * @param config
     */
    @Override
    public void saveProperties(AppConfig config) {

// multi-db
        config.setProperty("db.multi",Boolean.toString(multiDBButton.isSelected()));

        config.setProperty("db.engine", comboValue(jcboDBDriver.getSelectedItem()));
        config.setProperty("db.driverlib", dbDriverLibraryValue.getText());
        config.setProperty("db.driver", dbDriverClassValue.getText());

// primary DB
        config.setProperty("db.name", dbNameValue.getText());
        config.setProperty("db.URL", dbURLValue.getText());
        config.setProperty("db.schema", jtxtDbSchema.getText());
        config.setProperty("db.options", jtxtDbOptions.getText());
        config.setProperty("db.user", dbUserValue.getText());
        AltEncrypter cypher = new AltEncrypter("cypherkey" + dbUserValue.getText());
        config.setProperty("db.password", "crypt:" +
                cypher.encrypt(new String(dbPasswordValue.getPassword())));

// secondary DB
        config.setProperty("db1.name", db2NameValue.getText());
        config.setProperty("db1.URL", db2URLValue.getText());
        config.setProperty("db1.schema", jtxtDbSchema1.getText());
        config.setProperty("db1.options", db2OptionsValue.getText());
        config.setProperty("db1.user", db2UserValue.getText());
        cypher = new AltEncrypter("cypherkey" + db2UserValue.getText());
        config.setProperty("db1.password", "crypt:" +
                cypher.encrypt(new String(db2PasswordValue.getPassword())));

        dirty.setDirty(false);
    }

    private String comboValue(Object value) {
        return value == null ? "" : value.toString();
    }

    public void fillSchema() {
        /* Use existing session credentials but declare new session and connection
         * to keep separated from current session instance as database could
         * be a different server
         */

        if (jCBSchema.getItemCount() >= 1 ) {
            jCBSchema.removeAllItems();
        }

        try {
            String driverlib = dbDriverLibraryValue.getText();
            String driver = dbDriverClassValue.getText();
            String url = dbURLValue.getText();
            String user = dbUserValue.getText();
            String password = new String(dbPasswordValue.getPassword());

            ClassLoader cloader = new URLClassLoader(new URL[]{new File(driverlib).toURI().toURL()});
            DriverManager.registerDriver(new DriverWrapper((Driver) Class.forName(driver, true, cloader).newInstance()));

            Session session1 =  new Session(url, user, password);
            Connection connection1 = session1.getConnection();
            ResultSet rs = connection1.getMetaData().getCatalogs();

            while (rs.next()) {
                jCBSchema.addItem(rs.getString("TABLE_CAT"));
            }

            jCBSchema.setEnabled(true);
            jCBSchema.setSelectedIndex(0);

        } catch (MalformedURLException | ClassNotFoundException | SQLException
                 | InstantiationException | IllegalAccessException ex) {
            log.error(ex.getMessage());
        }
    }
    public void fillSchema1() {
        /* Use existing session credentials but declare new session and connection
         * to keep separated from current session instance as database could
         * be a different server
         */

        if (jCBSchema1.getItemCount() >= 1 ) {
            jCBSchema1.removeAllItems();
        }

        try {
            String driverlib = dbDriverLibraryValue.getText();
            String driver = dbDriverClassValue.getText();
            String url = db2URLValue.getText();
            String user = db2UserValue.getText();
            String password = new String(db2PasswordValue.getPassword());

            ClassLoader cloader = new URLClassLoader(new URL[]{new File(driverlib).toURI().toURL()});
            DriverManager.registerDriver(new DriverWrapper((Driver) Class.forName(driver, true, cloader).newInstance()));

            Session session1 =  new Session(url, user, password);
            Connection connection1 = session1.getConnection();
            ResultSet rs1 = connection1.getMetaData().getCatalogs();

            while (rs1.next()) {
                jCBSchema1.addItem(rs1.getString("TABLE_CAT"));
            }

            jCBSchema1.setEnabled(true);
            jCBSchema1.setSelectedIndex(0);

        } catch (MalformedURLException | ClassNotFoundException | SQLException
                 | InstantiationException | IllegalAccessException ex) {
            log.error(ex.getMessage());
        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        headerPanel = new javax.swing.JPanel();
        dbTypeLabel = new javax.swing.JLabel();
        jcboDBDriver = new javax.swing.JComboBox();
        dbConfigPanel = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jCBSchema = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        jtxtDbOptions = new javax.swing.JTextField();
        jbtnCreateDB = new javax.swing.JButton();
        jtxtDbSchema = new javax.swing.JTextField();
        jbtnSetDB = new javax.swing.JButton();
        dbDriverLibrarySelect = new javax.swing.JButton();
        dbURLLabel = new javax.swing.JLabel();
        dbDriverClassLabel = new javax.swing.JLabel();
        dbPasswordLabel = new javax.swing.JLabel();
        dbNameLabel = new javax.swing.JLabel();
        dbPasswordValue = new javax.swing.JPasswordField();
        dbDriverLibraryLabel = new javax.swing.JLabel();
        dbDriverLibraryValue = new javax.swing.JTextField();
        dbUserValue = new javax.swing.JTextField();
        dbURLValue = new javax.swing.JTextField();
        dbDriverClassValue = new javax.swing.JTextField();
        dbUserLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        dbNameValue = new javax.swing.JTextField();
        dbVersionLabel = new javax.swing.JLabel();
        dbConnectButton = new javax.swing.JButton();
        dbResetButton = new javax.swing.JButton();
        validUserAlert = new javax.swing.JLabel();
        db2ConfigPanel = new javax.swing.JPanel();
        db2NameLabel = new javax.swing.JLabel();
        db2NameValue = new javax.swing.JTextField();
        db2URLLabel = new javax.swing.JLabel();
        db2URLValue = new javax.swing.JTextField();
        db2UserValue = new javax.swing.JTextField();
        db2UserLabel = new javax.swing.JLabel();
        db2PasswordLabel = new javax.swing.JLabel();
        db2PasswordValue = new javax.swing.JPasswordField();
        jbtnConnect1 = new javax.swing.JButton();
        jbtnReset1 = new javax.swing.JButton();
        jLblDBServerversion1 = new javax.swing.JLabel();
        db2DBLabel = new javax.swing.JLabel();
        jCBSchema1 = new javax.swing.JComboBox<>();
        db2OptionsLabel = new javax.swing.JLabel();
        db2OptionsValue = new javax.swing.JTextField();
        jbtnSetDB1 = new javax.swing.JButton();
        jtxtDbSchema1 = new javax.swing.JTextField();
        multiDBPanel = new javax.swing.JPanel();
        multiDBButton = new javax.swing.JCheckBox();
        LblMultiDB = new javax.swing.JLabel();
        dbInfoLabel = new javax.swing.JLabel();

        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(900, 500));

        dbTypeLabel.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        dbTypeLabel.setText(bundle.getString("label.Database")); // NOI18N
        dbTypeLabel.setPreferredSize(new java.awt.Dimension(125, 30));

        jcboDBDriver.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jcboDBDriver.setToolTipText(bundle.getString("tooltip.config.db.dbtype")); // NOI18N
        jcboDBDriver.setPreferredSize(new java.awt.Dimension(160, 30));
        jcboDBDriver.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcboDBDriverActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dbTypeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jcboDBDriver, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(2872, Short.MAX_VALUE))
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jcboDBDriver, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dbTypeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 6, Short.MAX_VALUE))
        );

        dbConfigPanel.setBackground(new java.awt.Color(255, 255, 255));

        jLabel7.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel7.setText(AppLocal.getIntString("label.DBName")); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(125, 30));

        jCBSchema.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jCBSchema.setToolTipText(bundle.getString("tooltip.config.db.schema")); // NOI18N
        jCBSchema.setPreferredSize(new java.awt.Dimension(160, 30));
        jCBSchema.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBSchemaActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel8.setText(AppLocal.getIntString("label.DbOptions")); // NOI18N
        jLabel8.setPreferredSize(new java.awt.Dimension(125, 30));

        jtxtDbOptions.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtDbOptions.setToolTipText(bundle.getString("tooltip.config.db.options")); // NOI18N
        jtxtDbOptions.setPreferredSize(new java.awt.Dimension(330, 30));

        jbtnCreateDB.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jbtnCreateDB.setText("CREATE DEFAULT");
        jbtnCreateDB.setToolTipText(bundle.getString("message.databasecreate")); // NOI18N
        jbtnCreateDB.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jbtnCreateDB.setPreferredSize(new java.awt.Dimension(160, 45));
        jbtnCreateDB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnCreateDBActionPerformed(evt);
            }
        });

        jtxtDbSchema.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtDbSchema.setEnabled(false);
        jtxtDbSchema.setPreferredSize(new java.awt.Dimension(250, 30));

        jbtnSetDB.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jbtnSetDB.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/unicenta/images/btn2.png"))); // NOI18N
        jbtnSetDB.setText("SET");
        jbtnSetDB.setToolTipText(bundle.getString("tooltip.config.db.databaseset")); // NOI18N
        jbtnSetDB.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jbtnSetDB.setPreferredSize(new java.awt.Dimension(160, 45));
        jbtnSetDB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnSetDBActionPerformed(evt);
            }
        });

        dbDriverLibrarySelect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/unicenta/images/fileopen.png"))); // NOI18N
        dbDriverLibrarySelect.setText("  ");
        dbDriverLibrarySelect.setToolTipText(bundle.getString("tooltip.config.db.file")); // NOI18N
        dbDriverLibrarySelect.setMaximumSize(new java.awt.Dimension(64, 32));
        dbDriverLibrarySelect.setMinimumSize(new java.awt.Dimension(64, 32));
        dbDriverLibrarySelect.setPreferredSize(new java.awt.Dimension(80, 30));

        dbURLLabel.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        dbURLLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        dbURLLabel.setText(AppLocal.getIntString("label.DbURL")); // NOI18N
        dbURLLabel.setPreferredSize(new java.awt.Dimension(125, 30));

        dbDriverClassLabel.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        dbDriverClassLabel.setText(AppLocal.getIntString("label.DbDriver")); // NOI18N
        dbDriverClassLabel.setPreferredSize(new java.awt.Dimension(125, 30));

        dbPasswordLabel.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        dbPasswordLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        dbPasswordLabel.setText(AppLocal.getIntString("label.DbPassword")); // NOI18N
        dbPasswordLabel.setPreferredSize(new java.awt.Dimension(125, 30));

        dbNameLabel.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        dbNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        dbNameLabel.setText(AppLocal.getIntString("label.DbName")); // NOI18N
        dbNameLabel.setPreferredSize(new java.awt.Dimension(125, 30));

        dbPasswordValue.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        dbPasswordValue.setToolTipText(bundle.getString("tooltip.config.db.password")); // NOI18N
        dbPasswordValue.setPreferredSize(new java.awt.Dimension(160, 30));

        dbDriverLibraryLabel.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        dbDriverLibraryLabel.setText(AppLocal.getIntString("label.dbdriverlib")); // NOI18N
        dbDriverLibraryLabel.setPreferredSize(new java.awt.Dimension(125, 30));

        dbDriverLibraryValue.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        dbDriverLibraryValue.setToolTipText(bundle.getString("tooltip.config.db.driverlib")); // NOI18N
        dbDriverLibraryValue.setPreferredSize(new java.awt.Dimension(500, 30));

        dbUserValue.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        dbUserValue.setText(bundle.getString("tooltip.config.db.user")); // NOI18N
        dbUserValue.setToolTipText(bundle.getString("tooltip.config.db.user")); // NOI18N
        dbUserValue.setPreferredSize(new java.awt.Dimension(160, 30));

        dbURLValue.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        dbURLValue.setToolTipText(bundle.getString("tooltip.config.db.url")); // NOI18N
        dbURLValue.setPreferredSize(new java.awt.Dimension(320, 30));

        dbDriverClassValue.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        dbDriverClassValue.setToolTipText(bundle.getString("tooltip.config.db.driverclass")); // NOI18N
        dbDriverClassValue.setPreferredSize(new java.awt.Dimension(150, 30));
        dbDriverClassValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbDriverClassValueActionPerformed(evt);
            }
        });

        dbUserLabel.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        dbUserLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        dbUserLabel.setText(AppLocal.getIntString("label.DbUser")); // NOI18N
        dbUserLabel.setPreferredSize(new java.awt.Dimension(125, 30));

        dbNameValue.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        dbNameValue.setToolTipText(bundle.getString("tooltip.config.db.name")); // NOI18N
        dbNameValue.setPreferredSize(new java.awt.Dimension(160, 30));

        dbVersionLabel.setBackground(new java.awt.Color(51, 204, 255));
        dbVersionLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        dbVersionLabel.setForeground(new java.awt.Color(255, 255, 255));
        dbVersionLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        dbVersionLabel.setOpaque(true);
        dbVersionLabel.setPreferredSize(new java.awt.Dimension(170, 30));

        dbConnectButton.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        dbConnectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/unicenta/images/btn1.png"))); // NOI18N
        dbConnectButton.setText(bundle.getString("button.connect")); // NOI18N
        dbConnectButton.setToolTipText(bundle.getString("tooltip.config.db.connect")); // NOI18N
        dbConnectButton.setActionCommand(bundle.getString("Button.Test")); // NOI18N
        dbConnectButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        dbConnectButton.setPreferredSize(new java.awt.Dimension(160, 45));
        dbConnectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbConnectButtonActionPerformed(evt);
            }
        });

        dbResetButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        dbResetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/unicenta/images/reload.png"))); // NOI18N
        dbResetButton.setToolTipText(AppLocal.getIntString("tooltip.config.db.reset")); // NOI18N
        dbResetButton.setPreferredSize(new java.awt.Dimension(80, 45));
        dbResetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbResetButtonActionPerformed(evt);
            }
        });

        validUserAlert.setBackground(new java.awt.Color(255, 0, 51));
        validUserAlert.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14)); // NOI18N
        validUserAlert.setForeground(new java.awt.Color(255, 255, 255));
        validUserAlert.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        validUserAlert.setText(bundle.getString("message.dbalert")); // NOI18N
        validUserAlert.setOpaque(true);
        validUserAlert.setPreferredSize(new java.awt.Dimension(570, 30));

        javax.swing.GroupLayout dbConfigPanelLayout = new javax.swing.GroupLayout(dbConfigPanel);
        dbConfigPanel.setLayout(dbConfigPanelLayout);
        dbConfigPanelLayout.setHorizontalGroup(
            dbConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dbConfigPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dbConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(dbConfigPanelLayout.createSequentialGroup()
                        .addComponent(validUserAlert, javax.swing.GroupLayout.DEFAULT_SIZE, 992, Short.MAX_VALUE)
                        .addGap(786, 786, 786))
                    .addGroup(dbConfigPanelLayout.createSequentialGroup()
                        .addGroup(dbConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(dbConfigPanelLayout.createSequentialGroup()
                                .addComponent(dbDriverLibraryLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dbDriverLibraryValue, javax.swing.GroupLayout.PREFERRED_SIZE, 326, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dbDriverLibrarySelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dbDriverClassLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dbDriverClassValue, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(dbConfigPanelLayout.createSequentialGroup()
                                .addComponent(dbNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dbNameValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dbURLLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dbURLValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dbVersionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(dbConfigPanelLayout.createSequentialGroup()
                                .addComponent(dbUserLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dbUserValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dbPasswordLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dbPasswordValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dbConnectButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dbResetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(dbConfigPanelLayout.createSequentialGroup()
                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(dbConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(dbConfigPanelLayout.createSequentialGroup()
                                        .addComponent(jCBSchema, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jbtnSetDB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(dbConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(dbConfigPanelLayout.createSequentialGroup()
                                        .addComponent(jtxtDbSchema, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(32, 32, 32)
                                        .addComponent(jbtnCreateDB, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jtxtDbOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(dbConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(dbConfigPanelLayout.createSequentialGroup()
                    .addGap(898, 898, 898)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 880, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        dbConfigPanelLayout.setVerticalGroup(
            dbConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dbConfigPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dbConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(dbConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(dbDriverLibraryValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dbDriverLibrarySelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dbDriverClassLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dbDriverClassValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(dbDriverLibraryLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dbConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(dbConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(dbNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dbNameValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dbURLLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dbURLValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(dbVersionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dbConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(dbConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(dbUserLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dbUserValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dbPasswordLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dbPasswordValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dbConnectButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(dbResetButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dbConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCBSchema, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jtxtDbOptions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dbConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jbtnSetDB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jbtnCreateDB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jtxtDbSchema, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(validUserAlert, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(9, Short.MAX_VALUE))
            .addGroup(dbConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(dbConfigPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(246, Short.MAX_VALUE)))
        );

        db2ConfigPanel.setBackground(new java.awt.Color(255, 255, 255));

        db2NameLabel.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        db2NameLabel.setText(AppLocal.getIntString("label.DbName1")); // NOI18N
        db2NameLabel.setEnabled(false);
        db2NameLabel.setPreferredSize(new java.awt.Dimension(125, 30));

        db2NameValue.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        db2NameValue.setToolTipText(bundle.getString("tooltip.config.db.name1")); // NOI18N
        db2NameValue.setEnabled(false);
        db2NameValue.setPreferredSize(new java.awt.Dimension(160, 30));

        db2URLLabel.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        db2URLLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        db2URLLabel.setText(AppLocal.getIntString("label.DbURL")); // NOI18N
        db2URLLabel.setEnabled(false);
        db2URLLabel.setPreferredSize(new java.awt.Dimension(125, 30));

        db2URLValue.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        db2URLValue.setToolTipText(bundle.getString("tooltip.config.db.url1")); // NOI18N
        db2URLValue.setEnabled(false);
        db2URLValue.setPreferredSize(new java.awt.Dimension(320, 30));
        db2URLValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                db2URLValueActionPerformed(evt);
            }
        });

        db2UserValue.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        db2UserValue.setToolTipText(bundle.getString("tooltip.config.db.user1")); // NOI18N
        db2UserValue.setEnabled(false);
        db2UserValue.setPreferredSize(new java.awt.Dimension(160, 30));

        db2UserLabel.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        db2UserLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        db2UserLabel.setText(AppLocal.getIntString("label.DbUser")); // NOI18N
        db2UserLabel.setEnabled(false);
        db2UserLabel.setPreferredSize(new java.awt.Dimension(125, 30));

        db2PasswordLabel.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        db2PasswordLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        db2PasswordLabel.setText(AppLocal.getIntString("label.DbPassword")); // NOI18N
        db2PasswordLabel.setEnabled(false);
        db2PasswordLabel.setPreferredSize(new java.awt.Dimension(125, 30));

        db2PasswordValue.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        db2PasswordValue.setToolTipText(bundle.getString("tooltip.config.db.password1")); // NOI18N
        db2PasswordValue.setEnabled(false);
        db2PasswordValue.setPreferredSize(new java.awt.Dimension(160, 30));

        jbtnConnect1.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jbtnConnect1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/unicenta/images/btn1.png"))); // NOI18N
        jbtnConnect1.setText(bundle.getString("button.connect")); // NOI18N
        jbtnConnect1.setToolTipText(bundle.getString("tooltip.config.db.connect")); // NOI18N
        jbtnConnect1.setActionCommand(bundle.getString("Button.Test")); // NOI18N
        jbtnConnect1.setEnabled(false);
        jbtnConnect1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jbtnConnect1.setPreferredSize(new java.awt.Dimension(160, 45));
        jbtnConnect1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnConnect1ActionPerformed(evt);
            }
        });

        jbtnReset1.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jbtnReset1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/unicenta/images/reload.png"))); // NOI18N
        jbtnReset1.setToolTipText(AppLocal.getIntString("tooltip.config.db.reset1")); // NOI18N
        jbtnReset1.setEnabled(false);
        jbtnReset1.setPreferredSize(new java.awt.Dimension(80, 45));
        jbtnReset1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnReset1ActionPerformed(evt);
            }
        });

        jLblDBServerversion1.setBackground(new java.awt.Color(51, 204, 255));
        jLblDBServerversion1.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jLblDBServerversion1.setForeground(new java.awt.Color(255, 255, 255));
        jLblDBServerversion1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLblDBServerversion1.setOpaque(true);
        jLblDBServerversion1.setPreferredSize(new java.awt.Dimension(170, 30));

        db2DBLabel.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        db2DBLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        db2DBLabel.setText(AppLocal.getIntString("label.DBName")); // NOI18N
        db2DBLabel.setEnabled(false);
        db2DBLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        db2DBLabel.setPreferredSize(new java.awt.Dimension(125, 30));

        jCBSchema1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jCBSchema1.setToolTipText(bundle.getString("tooltip.config.db.schema1")); // NOI18N
        jCBSchema1.setPreferredSize(new java.awt.Dimension(160, 30));
        jCBSchema1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCBSchema1ActionPerformed(evt);
            }
        });

        db2OptionsLabel.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        db2OptionsLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        db2OptionsLabel.setText(AppLocal.getIntString("label.DbOptions")); // NOI18N
        db2OptionsLabel.setEnabled(false);
        db2OptionsLabel.setPreferredSize(new java.awt.Dimension(125, 30));

        db2OptionsValue.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        db2OptionsValue.setToolTipText(bundle.getString("tooltip.config.db.options")); // NOI18N
        db2OptionsValue.setEnabled(false);
        db2OptionsValue.setPreferredSize(new java.awt.Dimension(330, 30));

        jbtnSetDB1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jbtnSetDB1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/unicenta/images/btn2.png"))); // NOI18N
        jbtnSetDB1.setText("SET");
        jbtnSetDB1.setToolTipText(bundle.getString("tooltip.config.db.databaseset1")); // NOI18N
        jbtnSetDB1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jbtnSetDB1.setPreferredSize(new java.awt.Dimension(160, 45));
        jbtnSetDB1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnSetDB1ActionPerformed(evt);
            }
        });

        jtxtDbSchema1.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jtxtDbSchema1.setEnabled(false);
        jtxtDbSchema1.setPreferredSize(new java.awt.Dimension(250, 30));

        javax.swing.GroupLayout db2ConfigPanelLayout = new javax.swing.GroupLayout(db2ConfigPanel);
        db2ConfigPanel.setLayout(db2ConfigPanelLayout);
        db2ConfigPanelLayout.setHorizontalGroup(
            db2ConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(db2ConfigPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(db2ConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(db2ConfigPanelLayout.createSequentialGroup()
                        .addComponent(db2NameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(db2NameValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(db2URLLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(db2URLValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLblDBServerversion1, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(db2ConfigPanelLayout.createSequentialGroup()
                        .addComponent(db2UserLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(db2UserValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(db2PasswordLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(db2PasswordValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jbtnConnect1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jbtnReset1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(db2ConfigPanelLayout.createSequentialGroup()
                        .addComponent(db2DBLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(db2ConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(db2ConfigPanelLayout.createSequentialGroup()
                                .addComponent(jbtnSetDB1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(137, 137, 137)
                                .addComponent(jtxtDbSchema1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(db2ConfigPanelLayout.createSequentialGroup()
                                .addComponent(jCBSchema1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(db2OptionsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(db2OptionsValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        db2ConfigPanelLayout.setVerticalGroup(
            db2ConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(db2ConfigPanelLayout.createSequentialGroup()
                .addGroup(db2ConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(db2NameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(db2NameValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(db2ConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(db2URLLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(db2URLValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLblDBServerversion1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(db2ConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jbtnConnect1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(db2ConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(db2PasswordValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(db2PasswordLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jbtnReset1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(db2UserValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(db2UserLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(db2ConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(db2DBLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCBSchema1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(db2OptionsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(db2OptionsValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(db2ConfigPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(db2ConfigPanelLayout.createSequentialGroup()
                        .addComponent(jtxtDbSchema1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(12, Short.MAX_VALUE))
                    .addGroup(db2ConfigPanelLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(jbtnSetDB1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))))
        );

        multiDBButton.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        multiDBButton.setPreferredSize(new java.awt.Dimension(80, 30));
        multiDBButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                multiDBButtonActionPerformed(evt);
            }
        });

        LblMultiDB.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        LblMultiDB.setText(AppLocal.getIntString("label.multidb")); // NOI18N
        LblMultiDB.setPreferredSize(new java.awt.Dimension(125, 30));

        dbInfoLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        dbInfoLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        dbInfoLabel.setText(bundle.getString("message.DBDefault")); // NOI18N
        dbInfoLabel.setToolTipText("");
        dbInfoLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        dbInfoLabel.setPreferredSize(new java.awt.Dimension(889, 120));
        dbInfoLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout multiDBPanelLayout = new javax.swing.GroupLayout(multiDBPanel);
        multiDBPanel.setLayout(multiDBPanelLayout);
        multiDBPanelLayout.setHorizontalGroup(
            multiDBPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(multiDBPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(LblMultiDB, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(multiDBButton, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dbInfoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 740, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(72, Short.MAX_VALUE))
        );
        multiDBPanelLayout.setVerticalGroup(
            multiDBPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(multiDBPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(multiDBPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(LblMultiDB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(multiDBButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(multiDBPanelLayout.createSequentialGroup()
                .addComponent(dbInfoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(headerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dbConfigPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(multiDBPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(db2ConfigPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(headerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dbConfigPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(multiDBPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(db2ConfigPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(68, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void dbDriverClassValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbDriverClassValueActionPerformed

    }//GEN-LAST:event_dbDriverClassValueActionPerformed

    private void jcboDBDriverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcboDBDriverActionPerformed

        String dirname = System.getProperty("dirname.path");
        dirname = dirname == null ? "./" : dirname;

        if ("MariaDB".equals(jcboDBDriver.getSelectedItem())) {
            dbDriverLibraryValue.setText(new File(new File(dirname), "mariadb-java-client-3.5.10.jar").getAbsolutePath());
            dbDriverClassValue.setText("org.mariadb.jdbc.Driver");
            dbURLValue.setText("jdbc:mariadb://localhost:3306/");
            jtxtDbSchema.setText("unicentaopos");
            jtxtDbOptions.setText("?zeroDateTimeBehavior=convertToNull");
            dbConfigPanel.setVisible(true);
            multiDBPanel.setVisible(true);            
        }
    }//GEN-LAST:event_jcboDBDriverActionPerformed

    private void dbConnectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbConnectButtonActionPerformed
        try {
            String driverlib = dbDriverLibraryValue.getText();
            String driver = dbDriverClassValue.getText();
            String url = dbURLValue.getText();
            String user = dbUserValue.getText();
            String password = new String(dbPasswordValue.getPassword());

            ClassLoader cloader = new URLClassLoader(new URL[]{new File(driverlib).toURI().toURL()});
            DriverManager.registerDriver(new DriverWrapper((Driver) Class.forName(driver, true, cloader).newInstance()));

            Session session =  new Session(url, user, password);
            Connection connection = session.getConnection();

            boolean isValid;
            isValid = connection != null && connection.isValid(1000);

            if (isValid) {
                JOptionPane.showMessageDialog(this,
                        AppLocal.getIntString("message.databasesuccess"),
                        "Connection Test", JOptionPane.INFORMATION_MESSAGE);
                fillSchema();
                validUserAlert.setVisible(false);
                dbConfigPanel.setVisible(true);
            } else {
                validUserAlert.setVisible(true);
                dbConfigPanel.setVisible(false);
                JMessageDialog.showMessage(this,
                        new MessageInf(MessageInf.SGN_WARNING, "Connection Error"));
            }

            ResultSet rs = connection.getMetaData().getCatalogs();
            String SQL="SELECT LEFT(VERSION(),3)  ";
            Statement stmt = (Statement) connection.createStatement();
            rs = stmt.executeQuery(SQL);
            rs.next();
            dbVersionLabel.setVisible(true);
            dbVersionLabel.setText(" MySQL Server : " + rs.getString(1));

            //if (!rs.getString(1).equals("5.7")) {
            //    jLblDBServerversion.setBackground(Color.RED);
            //    JOptionPane.showMessageDialog(this,
            //    AppLocal.getIntString("message.databasefail"),
            //    "Connection Test", JOptionPane.WARNING_MESSAGE);
            //}

        } catch (InstantiationException | IllegalAccessException | MalformedURLException | ClassNotFoundException e) {
            JMessageDialog.showMessage(this,
                    new MessageInf(MessageInf.SGN_WARNING,
                            AppLocal.getIntString("message.databasedrivererror"), e));
        } catch (SQLException e) {
            validUserAlert.setVisible(true);
            dbVersionLabel.setText("");
            JMessageDialog.showMessage(this,
                    new MessageInf(MessageInf.SGN_WARNING,
                            AppLocal.getIntString("message.databaseconnectionerror"), e));
        } catch (Exception e) {
            JMessageDialog.showMessage(this,
                    new MessageInf(MessageInf.SGN_WARNING, "Unknown exception", e));
        }
    }//GEN-LAST:event_dbConnectButtonActionPerformed

    private void jbtnConnect1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnConnect1ActionPerformed
        /* Even though TEST & TEST1 could be consolidated into method am deliberately
         *        keeping this separate as plan is to also include alternative
         */
        try {
            String driverlib = dbDriverLibraryValue.getText();
            String driver = dbDriverClassValue.getText();
            String url = db2URLValue.getText();
            String user = db2UserValue.getText();
            String password = new String(db2PasswordValue.getPassword());

            ClassLoader cloader = new URLClassLoader(new URL[]{new File(driverlib).toURI().toURL()});
            DriverManager.registerDriver(new DriverWrapper((Driver) Class.forName(driver, true, cloader).newInstance()));

            Session session =  new Session(url, user, password);
            Connection connection = session.getConnection();

            boolean isValid;
            isValid = (connection == null) ? false : connection.isValid(1000);

            if (isValid) {
                JOptionPane.showMessageDialog(this,
                        AppLocal.getIntString("message.databasesuccess"),
                        "Connection Test", JOptionPane.INFORMATION_MESSAGE);
                fillSchema1();
                validUserAlert.setVisible(false);
            } else {
                validUserAlert.setVisible(true);
                JMessageDialog.showMessage(this,
                        new MessageInf(MessageInf.SGN_WARNING, "Connection Error"));
            }

            ResultSet rs = connection.getMetaData().getCatalogs();
            String SQL="SELECT LEFT(VERSION(),3)  ";
            Statement stmt = (Statement) connection.createStatement();
            rs = stmt.executeQuery(SQL);
            rs.next();
            jLblDBServerversion1.setText(" MySQL Server : " + rs.getString(1));

        } catch (InstantiationException | IllegalAccessException | MalformedURLException | ClassNotFoundException e) {
            JMessageDialog.showMessage(this,
                    new MessageInf(MessageInf.SGN_WARNING,
                            AppLocal.getIntString("message.databasedrivererror"), e));
        } catch (SQLException e) {
            validUserAlert.setVisible(true);
            jLblDBServerversion1.setText("");
            JMessageDialog.showMessage(this,
                    new MessageInf(MessageInf.SGN_WARNING,
                            AppLocal.getIntString("message.databaseconnectionerror"), e));
        } catch (Exception e) {
            JMessageDialog.showMessage(this,
                    new MessageInf(MessageInf.SGN_WARNING, "Unknown exception", e));
        }
    }//GEN-LAST:event_jbtnConnect1ActionPerformed

    private void multiDBButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_multiDBButtonActionPerformed
        if (multiDBButton.isSelected()) {
            dbConfigPanel.setVisible(true);
            db2ConfigPanel.setVisible(true);
            multiDBPanel.setVisible(true);
        } else {
            db2ConfigPanel.setVisible(false);
        }
    }//GEN-LAST:event_multiDBButtonActionPerformed

    private void dbResetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbResetButtonActionPerformed
        if (jCBSchema.getItemCount() >= 1 ) {
            jCBSchema.removeAllItems();
        }

        String dirname = System.getProperty("dirname.path");
        dirname = dirname == null ? "./" : dirname;

        dbDriverLibraryValue.setText(new File(new File(dirname), "lib/mysql-connector-java-5.1.39.jar").getAbsolutePath());
        dbDriverClassValue.setText("com.mysql.jdbc.Driver");

        dbNameValue.setText("Main DB");
        dbURLValue.setText("jdbc:mysql://localhost:3306/");
        jtxtDbSchema.setText("unicentaopos");
        jtxtDbOptions.setText("?zeroDateTimeBehavior=convertToNull");
        dbUserValue.setText(null);
        dbPasswordValue.setText(null);
        dbVersionLabel.setText(null);
        dbVersionLabel.setVisible(false);
        dbConfigPanel.setVisible(false);
    }//GEN-LAST:event_dbResetButtonActionPerformed

    private void jbtnReset1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnReset1ActionPerformed
        if (jCBSchema1.getItemCount() >= 1 ) {
            jCBSchema1.removeAllItems();
        }

        db2NameValue.setText("Other DB");
        db2URLValue.setText("jdbc:mysql://localhost:3306/");
        jtxtDbSchema1.setText("unicentaopos1");
        db2OptionsValue.setText("?zeroDateTimeBehavior=convertToNull");
        db2UserValue.setText(null);
        db2PasswordValue.setText(null);
        jLblDBServerversion1.setText(null);
    }//GEN-LAST:event_jbtnReset1ActionPerformed

    private void jCBSchemaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBSchemaActionPerformed
/*
        if (jCBSchema.getItemCount() > 0 ) {
            String selected = jCBSchema.getSelectedItem().toString();
            if(!selected.equals(null)) {
                jtxtDbSchema.setText(selected);
            }
        }
*/
    }//GEN-LAST:event_jCBSchemaActionPerformed

    private void jCBSchema1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCBSchema1ActionPerformed
/*
        if (jCBSchema1.getItemCount() > 0 ) {
            String selected1 = jCBSchema1.getSelectedItem().toString();
            if(!selected1.equals(null)) {
                jtxtDbSchema1.setText(selected1);
            }
        }
*/
    }//GEN-LAST:event_jCBSchema1ActionPerformed

    private void db2URLValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_db2URLValueActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_db2URLValueActionPerformed

    private void jbtnCreateDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnCreateDBActionPerformed
        try {
            String driverlib = dbDriverLibraryValue.getText();
            String driver = dbDriverClassValue.getText();
            String url = dbURLValue.getText();
            String user = dbUserValue.getText();
            String password = new String(dbPasswordValue.getPassword());

            ClassLoader cloader = new URLClassLoader(new URL[]{new File(driverlib).toURI().toURL()});
            DriverManager.registerDriver(new DriverWrapper((Driver) Class.forName(driver, true, cloader).newInstance()));

            Session session =  new Session(url, user, password);
            Connection connection = session.getConnection();

            boolean isValid;
            isValid = (connection == null) ? false : connection.isValid(1000);

            if (isValid) {
                String SQL="CREATE DATABASE if not exists unicentaopos";
                Statement stmt = (Statement) connection.createStatement();
                stmt.executeUpdate(SQL);

                fillSchema();
                validUserAlert.setVisible(false);
                jtxtDbSchema.setText("unicentaopos");

                JOptionPane.showMessageDialog(this,
                        AppLocal.getIntString("message.createdefaultdb"),
                        "Create Default Database", JOptionPane.INFORMATION_MESSAGE);
            } else {
                validUserAlert.setVisible(true);
                dbConfigPanel.setVisible(false);
                JMessageDialog.showMessage(this,
                        new MessageInf(MessageInf.SGN_WARNING, "Connection Error"));
            }

        } catch (MalformedURLException ex) {
            log.error(ex.getMessage());
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            JMessageDialog.showMessage(this,
                    new MessageInf(MessageInf.SGN_WARNING,
                            AppLocal.getIntString("message.databasedrivererror"), e));
        } catch (SQLException e) {
            validUserAlert.setVisible(true);
            dbVersionLabel.setText("");
            JMessageDialog.showMessage(this,
                    new MessageInf(MessageInf.SGN_WARNING,
                            AppLocal.getIntString("message.databaseconnectionerror"), e));
        } catch (Exception e) {
            JMessageDialog.showMessage(this,
                    new MessageInf(MessageInf.SGN_WARNING, "Unknown exception", e));
        }
    }//GEN-LAST:event_jbtnCreateDBActionPerformed

    private void jbtnSetDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnSetDBActionPerformed

        if (jCBSchema.getItemCount() > 0 ) {
            String selected = jCBSchema.getSelectedItem().toString();
            if(selected != null) {
                jtxtDbSchema.setText(selected);
            }
        }
    }//GEN-LAST:event_jbtnSetDBActionPerformed

    private void jbtnSetDB1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnSetDB1ActionPerformed

        if (jCBSchema1.getItemCount() > 0 ) {
            String selected1 = jCBSchema1.getSelectedItem().toString();
            if(selected1 != null) {
                jtxtDbSchema1.setText(selected1);
            }
        }
    }//GEN-LAST:event_jbtnSetDB1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel LblMultiDB;
    private javax.swing.JPanel db2ConfigPanel;
    private javax.swing.JLabel db2DBLabel;
    private javax.swing.JLabel db2NameLabel;
    private javax.swing.JTextField db2NameValue;
    private javax.swing.JLabel db2OptionsLabel;
    private javax.swing.JTextField db2OptionsValue;
    private javax.swing.JLabel db2PasswordLabel;
    private javax.swing.JPasswordField db2PasswordValue;
    private javax.swing.JLabel db2URLLabel;
    private javax.swing.JTextField db2URLValue;
    private javax.swing.JLabel db2UserLabel;
    private javax.swing.JTextField db2UserValue;
    private javax.swing.JPanel dbConfigPanel;
    private javax.swing.JButton dbConnectButton;
    private javax.swing.JLabel dbDriverClassLabel;
    private javax.swing.JTextField dbDriverClassValue;
    private javax.swing.JLabel dbDriverLibraryLabel;
    private javax.swing.JButton dbDriverLibrarySelect;
    private javax.swing.JTextField dbDriverLibraryValue;
    private javax.swing.JLabel dbInfoLabel;
    private javax.swing.JLabel dbNameLabel;
    private javax.swing.JTextField dbNameValue;
    private javax.swing.JLabel dbPasswordLabel;
    private javax.swing.JPasswordField dbPasswordValue;
    private javax.swing.JButton dbResetButton;
    private javax.swing.JLabel dbTypeLabel;
    private javax.swing.JLabel dbURLLabel;
    private javax.swing.JTextField dbURLValue;
    private javax.swing.JLabel dbUserLabel;
    private javax.swing.JTextField dbUserValue;
    private javax.swing.JLabel dbVersionLabel;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JComboBox<String> jCBSchema;
    private javax.swing.JComboBox<String> jCBSchema1;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLblDBServerversion1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton jbtnConnect1;
    private javax.swing.JButton jbtnCreateDB;
    private javax.swing.JButton jbtnReset1;
    private javax.swing.JButton jbtnSetDB;
    private javax.swing.JButton jbtnSetDB1;
    private javax.swing.JComboBox jcboDBDriver;
    private javax.swing.JTextField jtxtDbOptions;
    private javax.swing.JTextField jtxtDbSchema;
    private javax.swing.JTextField jtxtDbSchema1;
    private javax.swing.JCheckBox multiDBButton;
    private javax.swing.JPanel multiDBPanel;
    private javax.swing.JLabel validUserAlert;
    // End of variables declaration//GEN-END:variables

}
