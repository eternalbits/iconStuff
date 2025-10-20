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

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import io.github.eternalbits.disk.DiskIcons;
import io.github.eternalbits.disk.DiskIconsView;
import io.github.eternalbits.icons.Static;

class ImageCanvas extends JPanel {
	private static final long serialVersionUID = 4983591518692183668L;
	
	private final JPanel panel = new JPanel();
	private final JScrollPane scroll = new JScrollPane(panel);
	private List<DiskIconsView> local = new ArrayList<DiskIconsView>();
	
	JMenuItem paste = new JMenuItem();
	private ImageItem[] ic_item;
	private String icon = null;
	
	private final FrontEnd app;
	private ListItem image;
	
	/* On Windows, allCombo is 72, but on other operating systems it varies.
	 *  For example, on macOS it is 93 and on Linux it is a scattered amount
	 *  of values. Except for the first time, all other designs were made
	 *  with the correct allCombo.
	 */
	private int allCombo;
	
	ImageCanvas(FrontEnd frontEnd) {
		app = frontEnd;
		setLayout(new BorderLayout());
		allCombo = app.isMac? 93: 72;
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		add(scroll, BorderLayout.CENTER);
	}
	
	void doRepaint(ListItem image) {
		this.image = image;
		if (local.size() > 0) if (ic_item != null) 
		for (int i = 0; i < ic_item.length; i++) {
			panel.remove(ic_item[i]);
		}
		icon = null;
		
		String type = Static.getExtension(image.getFile()).toLowerCase();
		local = new ArrayList<DiskIconsView>();
		
	//	Ensure at least one icon is displayed if all are filtered
		DiskIconsView es = null;
		
		for (int i = 0; i < image.getView().fileIcons.size(); i++) {
			DiskIconsView fs = image.getView().fileIcons.get(i);
			if (fs.isIcon > 0) {	// PNG, BITMAP, APPLE, ARGB
				int ico = -1;
				if (fs.size == 0)
					fs.size = Static.getInteger(fs.layout);
			//	Only file icons have a length, not the copied ones
				if (fs.length != 0) {
					if (app.settings.ignoreIconsLarger256) {
						if (fs.size > 256) { ico = i; es = fs; }
					}
					if (app.settings.ignoreDuplicateIcons) {
						for (DiskIconsView fm: local) {
							if (fs.size == fm.size) ico = i;
						}
					}
				}
				if (ico == i) {
					icon = icon==null ? i+"=*" : icon + ";"+i+"=*";
					if (type.equals("ico")) continue;
				}
				local.add(fs);
			}
		}
		
		if (local.size() == 0 && es != null) {
			local.add(es);
		}	
		if (app.settings.iconsDescendingOrder) {
			Collections.sort(local);
		}
		ic_item = new ImageItem[local.size()];
		
		for (int i = 0; i < local.size(); i++) {
			ic_item[i] = new ImageItem(app, image, this, local.get(i), allCombo);
			ic_item[i].setComponentPopupMenu(); 
			panel.add(ic_item[i]);
		}
		
		validate();
		repaint();
		if (local.size() > 0) {
			allCombo = ic_item[0].allCombo();
		}
		
	}
	
	String getIcon() {
		return icon;
	}
	
	/**
	 * Place the PopupMenu with its dependencies under the ImageCanvas source
	 */
	void setComponentPopupMenu() {
		final JPopupMenu popup = new JPopupMenu();
		pasteComponentPopupMenu(popup);
		adjustComponentPopupMenu(popup);
		panel.setComponentPopupMenu(popup);
		if (!app.isWindows) {
			for (int i = 0; i < local.size(); i++) {
				ic_item[i].setComponentPopupMenu();
			}
		}
		panel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					setComponentPopupMenu();
				}
			}
		});
	}
	
	/**
	 * Adjust the PopupMenu with its dependencies under the ImageCanvas source
	 * 
	 * @param popup	The name of the menu.
	 */
	void pasteComponentPopupMenu(JPopupMenu popup) {
		paste = new JMenuItem(app.res.getString("paste"));
		
		Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);
		paste.setEnabled(t != null && t.isDataFlavorSupported(DataFlavor.imageFlavor));
		popup.add(paste);
		
		paste.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);
				if (t != null && t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
					try {
						DiskIconsView fs = new DiskIconsView();
						fs.image = (BufferedImage) t.getTransferData(DataFlavor.imageFlavor);
						fs.layout = fs.image == null? null: fs.image.getWidth() + (fs.image.getWidth() != 
								fs.image.getHeight()? "x" + fs.image.getHeight(): "") + " PNG";
						fs.description = fs.layout;
						fs.isIcon = DiskIcons.ICON_PNG;
						fs.type = "PNG";
						image.putIcon(fs);
						image.setUndo(true);
						doRepaint(image);
						
					} catch (UnsupportedFlavorException | IOException p) {
						p.printStackTrace();
					}
				}
			}
		});
	}
	
	/**
	 * Adjust the PopupMenu with its dependencies under the ImageCanvas source
	 * 
	 * @param popup	The name of the menu.
	 */
	void adjustComponentPopupMenu(JPopupMenu popup) {
		final JMenuItem refresh = new JMenuItem(app.res.getString("refresh"));
		final JMenuItem close = new JMenuItem(app.res.getString("close"));
		
		popup.add(new JSeparator());
		popup.add(refresh);
		popup.add(close);
		
		refresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				app.refresh(app.list.getSelectedIndex());
			}
		});
		
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				app.close(app.list.getSelectedIndex());
			}
		});
		
	}
	
}
