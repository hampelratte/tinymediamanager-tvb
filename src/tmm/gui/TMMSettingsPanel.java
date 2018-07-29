/*
 * Copyright (c) Henrik Niehaus
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the project (Lazy Bones) nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package tmm.gui;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.WEST;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import tmm.TMM;
import util.ui.ImageUtilities;

/**
 * The root p for the settings tabs
 *
 * @author <a href="hampelratte@users.sf.net">hampelratte@users.sf.net </a>
 */
public class TMMSettingsPanel implements devplugin.SettingsTab {

    private JLabel lTmmPath;
    private JTextField tTmmPath;
    private Properties settings;

    public TMMSettingsPanel(Properties settings) {
        this.settings = settings;
    }

    @Override
    public JPanel createSettingsPanel() {
        GridBagLayout pLayout = new GridBagLayout();
        JPanel p = new JPanel(pLayout);
        Insets defaultInsets = new Insets(5, 5, 5, 5);

        lTmmPath = new JLabel();
        p.add(lTmmPath, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, WEST, NONE, defaultInsets, 0, 0));
        lTmmPath.setText(TMM.getTranslation("tmm_data_dir", "Path to tmm data directory"));

        tTmmPath = new JTextField(settings.getProperty("tmm_data_dir"));
        p.add(tTmmPath, new GridBagConstraints(1, 0, 1, 1, 0.9, 0.0, WEST, HORIZONTAL, defaultInsets, 0, 0));

        // add empty label to push everthing to the top
        p.add(new JLabel(), new GridBagConstraints(0, 1, 2, 1, 1, 1, WEST, BOTH, defaultInsets, 0, 0));

        return p;
    }

    @Override
    public void saveSettings() {
        settings.setProperty("tmm_data_dir", tTmmPath.getText());
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(ImageUtilities.createImageFromJar("tmm/tmm16.png", TMMSettingsPanel.class));
    }

    @Override
    public String getTitle() {
        return "tinyMediaManager";
    }
}