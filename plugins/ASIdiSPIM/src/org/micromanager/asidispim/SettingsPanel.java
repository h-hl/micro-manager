///////////////////////////////////////////////////////////////////////////////
//FILE:          SettingsPanel.java
//PROJECT:       Micro-Manager 
//SUBSYSTEM:     ASIdiSPIM plugin
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nico Stuurman, Jon Daniels
//
// COPYRIGHT:    University of California, San Francisco, & ASI, 2013
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.

package org.micromanager.asidispim;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.micromanager.asidispim.Data.Devices;
import org.micromanager.asidispim.Data.MyStrings;
import org.micromanager.asidispim.Data.Prefs;
import org.micromanager.asidispim.Data.Properties;
import org.micromanager.asidispim.Utils.ListeningJPanel;
import org.micromanager.asidispim.Utils.PanelUtils;
import org.micromanager.asidispim.Utils.StagePositionUpdater;

import net.miginfocom.swing.MigLayout;

import org.micromanager.api.ScriptInterface;

/**
 *
 * @author Jon
 */
@SuppressWarnings("serial")
public class SettingsPanel extends ListeningJPanel {
   
   private final Devices devices_;
   private final Properties props_;
   private final Prefs prefs_;
   private final JSpinner positionRefreshInterval_;
   private final JSpinner scannerFilterX_;
   private final JSpinner scannerFilterY_;
   private final StagePositionUpdater stagePosUpdater_;
   private final ScriptInterface gui_;
   
   private final JPanel guiPanel_;
   private final JPanel scannerPanel_;
     
   /**
    * 
    * @param gui - implementation of Micro-Manager ScriptInterface api
    * @param devices the (single) instance of the Devices class
    * @param props 
    * @param prefs
    * @param stagePosUpdater
    */
   public SettingsPanel(ScriptInterface gui, Devices devices, 
           Properties props, Prefs prefs, StagePositionUpdater stagePosUpdater) {    
      super (MyStrings.PanelNames.SETTINGS.toString(), 
            new MigLayout(
              "", 
              "[right]16[center]16[center]",
              "[]16[]"));
     
      devices_ = devices;
      props_ = props;
      prefs_ = prefs;
      gui_ = gui;
      stagePosUpdater_ = stagePosUpdater;
      
      PanelUtils pu = new PanelUtils(gui_, prefs_, props_, devices_);
      
      guiPanel_ = new JPanel(new MigLayout(
            "",
            "[right]16[center]",
            "[]8[]"));
      
      
      // start GUI panel
      
      guiPanel_.setBorder(PanelUtils.makeTitledBorder("GUI"));
      
      final JCheckBox activeTimerCheckBox = new JCheckBox("Update axis positions continually");
      ActionListener ae = new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) { 
            if (activeTimerCheckBox.isSelected()) {
               stagePosUpdater_.start();
            } else {
               stagePosUpdater_.stop();
            }
            prefs_.putBoolean(panelName_, Prefs.Keys.ENABLE_POSITION_UPDATES,
                  activeTimerCheckBox.isSelected());
         }
      }; 
      activeTimerCheckBox.addActionListener(ae);
      activeTimerCheckBox.setSelected(prefs_.getBoolean(panelName_,
            Prefs.Keys.ENABLE_POSITION_UPDATES, true));
      // programmatically click twice to make sure the action handler is called;
      //   it is not called by setSelected unless there is a change in the value
      activeTimerCheckBox.doClick();
      activeTimerCheckBox.doClick();
      guiPanel_.add(activeTimerCheckBox, "center, span 2, wrap");
      
      guiPanel_.add(new JLabel("Position refresh interval (s):"));
      positionRefreshInterval_ = pu.makeSpinnerFloat(0.5, 1000, 0.5,
            new Devices.Keys [] {Devices.Keys.PLUGIN,},
            Properties.Keys.PLUGIN_POSITION_REFRESH_INTERVAL, 1);
      ChangeListener listenerLast = new ChangeListener() {
         @Override
         public void stateChanged(ChangeEvent e) {
            if (stagePosUpdater_.isRunning()) {
               // restart, doing this grabs the interval from the plugin property
               stagePosUpdater_.start();
            }
            prefs_.putFloat(panelName_, Properties.Keys.PLUGIN_POSITION_REFRESH_INTERVAL,
                  PanelUtils.getSpinnerFloatValue(positionRefreshInterval_));
         }
      };
      pu.addListenerLast(positionRefreshInterval_, listenerLast);
      guiPanel_.add(positionRefreshInterval_, "wrap");
      
      // end GUI subpanel
      
      // start scanner panel
      
      scannerPanel_ = new JPanel(new MigLayout(
            "",
            "[right]16[center]",
            "[]8[]"));
      
      
      // start GUI panel
      
      scannerPanel_.setBorder(PanelUtils.makeTitledBorder("Scanner"));

      
      
      scannerPanel_.add(new JLabel("Filter freq, sheet axis [kHz]:"), "cell 3 0");
      scannerFilterX_ = pu.makeSpinnerFloat(0.1, 5, 0.1,
            new Devices.Keys [] {Devices.Keys.GALVOA, Devices.Keys.GALVOB},
            Properties.Keys.SCANNER_FILTER_X, 0.8);
      scannerPanel_.add(scannerFilterX_, "wrap");
      
      scannerPanel_.add(new JLabel("Filter freq, slice axis [kHz]:"), "cell 3 1");
      scannerFilterY_ = pu.makeSpinnerFloat(0.1, 5, 0.1,
            new Devices.Keys [] {Devices.Keys.GALVOA, Devices.Keys.GALVOB},
            Properties.Keys.SCANNER_FILTER_Y, 0.4);
      scannerPanel_.add(scannerFilterY_, "wrap");
      
      // end scanner panel
      
      
      // construct main panel
      add(guiPanel_);
      add(scannerPanel_);
      
      
      
      
   }
   
   @Override
   public void saveSettings() {

   }
   
   
}