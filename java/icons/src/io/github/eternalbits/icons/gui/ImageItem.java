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

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;

import io.github.eternalbits.disk.DiskIcons;
import io.github.eternalbits.disk.DiskIconsView;
import io.github.eternalbits.icns.IcnsHeader;
import io.github.eternalbits.icons.Static;

public class ImageItem extends JPanel {
	private static final long serialVersionUID = 8178977464035716981L;

	private final String[] sz_type = {"ICO", "PNG"};
//	private final String[] sz_size = {"1024", "512", "256", "128", "64", "48", "40", "36", "32", "24", "20", "18", "16"};
	private final String[] sz_icon = {"PNG", "32-bit"};
	
	private JLabel st_image;
	private JComboBox<String> cb_type;
	private JComboBox<String> cb_size;
	private JComboBox<String> cb_icon;
	
	private final FrontEnd app;
	private final ListItem image;
	private final ImageCanvas canvas;
	private final DiskIconsView fs;
	
	ImageItem(FrontEnd frontEnd, ListItem image, ImageCanvas canvas, DiskIconsView fs, int allCombo) {
		app = frontEnd;
		this.image = image;
		this.canvas = canvas;
		this.fs = fs;
		Dimension dim = new Dimension(fs.image.getWidth() > 256? 256: fs.image.getWidth(), fs.image.getHeight() > 256? 256: fs.image.getHeight());
		st_image = new JLabel(new ImageIcon(fs.image.getScaledInstance(dim.width, dim.height, Image.SCALE_DEFAULT)));			
		String fs_size = Static.getSize(fs.layout);
		String fs_icon = Static.getIcon(fs.layout);
		String[] sz_size = {fs_size};
		cb_type = new JComboBox<String>(copyIcns(fs.layout));
		cb_type.setSelectedItem(fs.type);
		cb_size = new JComboBox<String>(sz_size);
		cb_size.setSelectedItem(fs_size);
		cb_icon = new JComboBox<String>(sz_icon);
		cb_icon.setSelectedItem(fs_icon);
		
		doRepaint(st_image, cb_type, cb_size, cb_icon, dim, allCombo);
		
		cb_type.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fs.type = String.valueOf(cb_type.getSelectedItem());
				image.setUndo(true);
			}
        });
		cb_size.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fs.layout = cb_size.getSelectedItem()+" "+Static.getIcon(fs.layout);
				canvas.doRepaint(image);
				image.setUndo(true);
			}
        });
		cb_icon.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fs.layout = Static.getSize(fs.layout)+" "+cb_icon.getSelectedItem();
				canvas.doRepaint(image);
				image.setUndo(true);
			}
        });
		
	}
	
	int allCombo() {
		return cb_icon.getY() + cb_icon.getHeight() - cb_type.getY();
	}
	
	private String[] copyIcns(String layout) {
		ArrayList<String> type = IcnsHeader.OSMatch(layout);
		type.add(0, sz_type[0]);
		type.add(type.size(), sz_type[1]);
		return type.toArray(new String[0]);
	}
	
	private void doRepaint(JLabel image, JComboBox<String> type, JComboBox<String> size, JComboBox<String> icon, Dimension dim, int allCombo) {
		int rightImage = (256 - dim.width) / 5;
		int leftImage = (256 - dim.width) - rightImage;
		int outImage = allCombo > dim.height? (allCombo - dim.height) / 2: 0;;
		int outCombo = dim.height > allCombo? (dim.width - allCombo) / 2: 0;
		
		GroupLayout layout = new GroupLayout(this);
		layout.setHorizontalGroup(
			layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
					.addGap(12 + leftImage)
					.addComponent(image, GroupLayout.PREFERRED_SIZE, dim.width, GroupLayout.PREFERRED_SIZE)
					.addGap(12 + rightImage)
					.addGroup(layout.createParallelGroup(Alignment.LEADING)
						.addComponent(type, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(size, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(icon, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
		);
		layout.setVerticalGroup(
			layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(Alignment.LEADING)
						.addGroup(layout.createSequentialGroup()
							.addGap(12 + outImage)
							.addComponent(image, GroupLayout.PREFERRED_SIZE, dim.height, GroupLayout.PREFERRED_SIZE)
							.addGap(12 + outImage))
						.addGroup(layout.createSequentialGroup()
							.addGap(12 + outCombo)
							.addComponent(type, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(size, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(icon, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addGap(12 + outCombo))))
		);
		setLayout(layout);
	}

	/**
	 * Place the PopupMenu with its dependencies under the ImageItem source
	 */
	void setComponentPopupMenu() {
		final JPopupMenu popup = new JPopupMenu();
		final JMenuItem delete = new JMenuItem(app.res.getString("delete"));
		delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
		
		popup.add(delete);
		popup.add(new JSeparator());
		canvas.adjustComponentPopupMenu(popup);
		setComponentPopupMenu(popup);

		delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fs.isIcon = DiskIcons.NOT_AN_ICON;
				canvas.doRepaint(image);
				image.setUndo(true);
			}
		});
		
	}
	
}
