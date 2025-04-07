/*
 * Copyright 2025 Rui Baptista
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.eternalbits.icons.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

class SettingsDialog extends JDialog {
	private static final long serialVersionUID = 5868423359756523676L;
	
	private final JCheckBox filterImageFiles;
	private final JCheckBox warnSaveOperation;
	private final JCheckBox iconsDescendingOrder;
	
	private final JCheckBox warnSaveNonStandard;
	
	private final JCheckBox ignoreIconsLarger256;
	private final JCheckBox ignoreDuplicateIcons;
	
	SettingsDialog(final FrontEnd app) {
		super(app, app.res.getString("set_settings"), ModalityType.APPLICATION_MODAL);
		
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		Border boxBorder = BorderFactory.createEtchedBorder();
		
		Box open = Box.createVerticalBox();
		open.setBorder(new TitledBorder(boxBorder, app.res.getString("open")));
		open.add(filterImageFiles	  = new JCheckBox(app.res.getString("set_filter_image"), app.settings.filterImageFiles));
		open.add(warnSaveOperation    = new JCheckBox(app.res.getString("set_save_operation"), app.settings.warnSaveOperation));
		open.add(iconsDescendingOrder = new JCheckBox(app.res.getString("set_descending_order"), app.settings.iconsDescendingOrder));

		Box icns = Box.createVerticalBox();
		icns.setBorder(new TitledBorder(boxBorder, app.res.getString("set_icns")));
		icns.add(warnSaveNonStandard = new JCheckBox(app.res.getString("set_icns_non_standard"), app.settings.warnSaveNonStandard));
		
		Box ico = Box.createVerticalBox();
		ico.setBorder(new TitledBorder(boxBorder, app.res.getString("set_ico")));
		ico.add(ignoreIconsLarger256 = new JCheckBox(app.res.getString("set_ico_larger_256"), app.settings.ignoreIconsLarger256));
		ico.add(ignoreDuplicateIcons = new JCheckBox(app.res.getString("set_ico_duplicate"), app.settings.ignoreDuplicateIcons));
		
		Box cmd = Box.createHorizontalBox();
		JButton apply = new JButton(app.res.getString("apply_text"));
		JButton cancel = new JButton(app.res.getString("cancel_text"));
		cmd.add(new JLabel(app.res.getString("author")));
		cmd.add(Box.createHorizontalGlue());
		cmd.add(apply);
		cmd.add(cancel);
		
		apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				app.settings.filterImageFiles 		= filterImageFiles.isSelected();
				app.settings.warnSaveOperation	 	= warnSaveOperation.isSelected();
				app.settings.iconsDescendingOrder 	= iconsDescendingOrder.isSelected();
				app.settings.warnSaveNonStandard 	= warnSaveNonStandard.isSelected();
				app.settings.ignoreIconsLarger256 	= ignoreIconsLarger256.isSelected();
				app.settings.ignoreDuplicateIcons 	= ignoreDuplicateIcons.isSelected();
				dispose();
			}
		});

		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		int p = 12;
		gbc.insets = new Insets(p,p,p,p);
		getContentPane().add(open, gbc);
		gbc.insets = new Insets(0,p,p,p);
		getContentPane().add(icns, gbc);
		getContentPane().add(ico, gbc);
		getContentPane().add(cmd, gbc);
		
		pack();
		setLocationRelativeTo(app);
		
		setResizable(false);
		setVisible(true);
	}
}
