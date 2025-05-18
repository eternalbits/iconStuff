/*
 * Copyright 2024 Rui Baptista
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
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import io.github.eternalbits.disk.DiskIcons;
import io.github.eternalbits.disk.DiskImage;
import io.github.eternalbits.disk.WrongHeaderException;
import io.github.eternalbits.icons.Static;

/**
 * Convert ICO and ICNS disk images. A list with known disk icons, a visual
 *  description of the selected image, and three command buttons: Open, Save 
 *  and Save As.
 * <p>
 */
public class FrontEnd extends JFrame {
	private static final long serialVersionUID = 2265801119904942445L;

	private static final String DEFAULT_FILE_FILTER = ".+\\.(?i:ico|icns|png)";
	private static final String WINDOWS_FILE_FILTER = "*.ico;*.icns;*.png";
	
	/* The window: a tool bar with command buttons, a list with known images,
	 *  and a main area with the selected image or a help/about dialog. 
	 */
	private JButton openButton = null;
	private JButton saveButton = null;
	private JButton saveAsButton = null;
	private JButton settingsButton = null;
	private JButton aboutButton = null;
	
	private ImageIcon iconSave = new ImageIcon(getResource("save.png"));
	private ImageIcon iconUndo = new ImageIcon(getResource("undo.png"));
	
	final DefaultListModel<ListItem> listData = new DefaultListModel<ListItem>();
	final JList<ListItem> list = new JList<ListItem>(listData);
	
	private final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	
	private final CardLayout deck = new CardLayout();
	private final JPanel main = new JPanel(deck);
	private final JEditorPane about = new JEditorPane();
	private final JToolBar tb = new JToolBar();

	private final ImageCanvas view = new ImageCanvas(this);
	private final FileDialog dialog = new FileDialog(this);
	
	private transient int savedListIndex = -1;
	transient final boolean isWindows;
	transient final boolean isMac;
	
	final Settings settings;
	JFileChooser chooser;
	ResourceBundle res;
	
	/**
	 * The convert ICO and ICNS disk icons graphical user interface.
	 */
	public FrontEnd() {
		setIconImage(new ImageIcon(getResource("monkey.png")).getImage());
		String osName = System.getProperty("os.name").toLowerCase();
		isWindows = osName.indexOf("windows") >= 0;
		isMac = osName.indexOf("mac") >= 0;
		if (isMac) 
			new MacAdapter(this);
		
		setLayout(new BorderLayout());
		settings = Settings.read();
		onLocaleChange();
		
		setupFrame();
		
		JComponent about = setupAboutDialog();
		deck.addLayoutComponent(about, "about");
		deck.addLayoutComponent(view, "view");
		main.add(about);
		main.add(view);
		
		dialog.setDirectory(settings.lastDirectory);
		setupFileDialog(settings.filterImageFiles);
		setupFileDrop();
		
		split.setLeftComponent(setupFileList());
		split.setRightComponent(main);
		
		adjustBounds();
		setLocation(settings.windowRect.getLocation());
		setPreferredSize(settings.windowRect.getSize());
		setMinimumSize(new Dimension(580, 420));
		setExtendedState(settings.windowState);

		main.setMinimumSize(new Dimension(420, 0));
		split.setDividerLocation(settings.splitLocation);
		getContentPane().add(split, BorderLayout.CENTER);
		
		// Display the window
		pack();
		setVisible(true);
		onSelectListItem();
	}

	/**
	 *  Sets the window according to the Language Interface option that appears in Settings.
	 */
	private void onLocaleChange() {
		res = ResourceBundle.getBundle("res.bundle", 
				SettingsDialog.Languages(settings.selectedLanguage, settings.selectedCountry));
		UIManager.put("OptionPane.cancelButtonText", res.getString("cancel_text"));
		UIManager.put("OptionPane.yesButtonText", res.getString("yes_text"));
		UIManager.put("OptionPane.noButtonText", res.getString("no_text"));
		UIManager.put("OptionPane.okButtonText", res.getString("ok_text"));
		
		setFileChooser();
		setTitle(res.getString("title"));
		getContentPane().add(setupToolBar(tb), BorderLayout.PAGE_START);
		view.setComponentPopupMenu();
		try {
			about.setContentType("text/html; charset=utf-8");
			about.setPage(getResource(res.getString("about_html")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 *  Create a new JFileChooser.
	 */
	private void setFileChooser() {
		UIManager.put("FileChooser.acceptAllFileFilterText", res.getString("accept_all"));
		UIManager.put("FileChooser.directoryOpenButtonText", res.getString("open_text"));
		UIManager.put("FileChooser.openButtonText", res.getString("open_text"));
		UIManager.put("FileChooser.saveButtonText", res.getString("save_text"));
		UIManager.put("FileChooser.cancelButtonText", res.getString("cancel_text"));
		UIManager.put("FileChooser.lookInLabelText", res.getString("file_look"));
		UIManager.put("FileChooser.saveInLabelText", res.getString("file_save"));
		UIManager.put("FileChooser.fileNameLabelText", res.getString("file_name"));
		UIManager.put("FileChooser.filesOfTypeLabelText", res.getString("file_type"));
		UIManager.put("FileChooser.win32.newFolder", res.getString("new_folder"));
		UIManager.put("FileChooser.win32.newFolder.subsequent", res.getString("new_folder") + " ({0})");
		UIManager.put("FileChooser.saveDialogFileNameLabelText", res.getString("file_save"));	// macOS
		UIManager.put("FileChooser.newFolderButtonText", res.getString("new_folder"));			// macOS
		UIManager.put("FileChooser.byNameText", res.getString("name_text"));					// macOS
		UIManager.put("FileChooser.byDateText", res.getString("date_text"));					// macOS
		
		chooser = new JFileChooser(settings.lastDirectory) {
			private static final long serialVersionUID = 1L;
			@Override
			public void approveSelection() {
				if (getDialogType() == OPEN_DIALOG) {
					for (File file : getSelectedFiles()) {
						if (!file.exists()) {
							JOptionPane.showConfirmDialog(this, 
								String.format(res.getString("error_not_found"), file.getName()), 
								res.getString("open_msg"), JOptionPane.DEFAULT_OPTION, 
								JOptionPane.WARNING_MESSAGE);
							return;
						}
					}
				} else { // getDialogType() == SAVE_DIALOG
					File file = getSelectedFile();
					if (file.exists()) {
						if (JOptionPane.showConfirmDialog(this, 
								String.format(res.getString("error_same_copy"), file.getName()), 
								res.getString("save_as_msg"), JOptionPane.YES_NO_OPTION, 
								JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
							return;							
						}						
					} else {
						String type = Static.getExtension(file).toUpperCase();
						if (!type.isEmpty() && !ListItem.IMAGE_TYPE.contains(type)) {
							JOptionPane.showMessageDialog(this, 
									String.format(res.getString("error_image_type"), type), 
									res.getString("save_as_msg"), JOptionPane.ERROR_MESSAGE);
							return;
						}
					}
				}
				super.approveSelection();
			}
		};
		chooser.setFileFilter(new FileNameExtensionFilter
				(res.getString("accept_disk"), "ico", "icns", "png", "lnk"));
	}
	
	/**
	 *  Sets the Frame behavior.
	 */
	private void setupFrame() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent e) {
				if (canCloseNow()) {
					settings.windowState = getExtendedState();
					setExtendedState(NORMAL);
					saveSettings();
					System.exit(0);
				}
			}
		});
		
	}

	/**
	 * Sets the File List content and behavior and returns an abstract swing
	 *  {@code JComponent} that can be an enclosing Container, the content 
	 *  scroll bars or the content component itself.
	 *  
	 * @return	The component to be added to the Window hierarchy.
	 */
	private Component setupFileList() {
		
		list.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting())
					onSelectListItem();
			}
		});
		
		list.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, 
					int index, boolean isSelected, boolean cellHasFocus) {
				Component c = super.getListCellRendererComponent(list, value, 
						index, isSelected, cellHasFocus);
				if (c instanceof JLabel) {
					JLabel j = (JLabel) c;
					j.setBorder(new CompoundBorder(j.getBorder(), new EmptyBorder(4, 4, 4, 0)));
				}
				return c;
			}
		});
		
		list.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE && list.getSelectedIndex() != -1) {
					close(list.getSelectedIndex());
				}
			}
		});
		
		JScrollPane jsp = new JScrollPane(list);
		jsp.setMinimumSize(new Dimension(0, 0));
		return jsp;
	}
	
	/**
	 * Sets a file transfer handle for the whole window, to act as an Open alternative.
	 */
	private void setupFileDrop() {
		
		setTransferHandler(new TransferHandler() {
			private static final long serialVersionUID = 4476434757526293417L;

			@Override
			public boolean canImport(TransferHandler.TransferSupport support) {
				if (support.isDrop() 
					&& support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					if ((LINK & support.getSourceDropActions()) == LINK) {
						support.setDropAction(LINK);
					}
					return true;
				}
				return false;
			}

			@Override
			public boolean importData(TransferHandler.TransferSupport support) {
				if (canImport(support)) try {
					for (Object item: (List<?>)support.getTransferable()
						.getTransferData(DataFlavor.javaFileListFlavor)) {
						if (item instanceof File) {
							addToList((File)item);
						}
					}
					return true;
				}
				catch (UnsupportedFlavorException | IOException e) {
					e.printStackTrace();
				}
				return false;
			}
		});
	}
	
	/**
	 * The open file to make a translation
	 */
	void addToList(File file) {
		if (file.isFile()) {
			if (isWindows)
				file = WindowShortcut.linkFile(file);
			
			for (int i = 0, s = listData.getSize(); i < s; i++) {
				if (listData.get(i).getFile().equals(file)) {
					refresh(i);
					return;
				}
			}
			
			try (DiskIcons image = DiskImage.open(file, "r")) {

				listData.addElement(new ListItem(this, image, file));
				list.setSelectedIndex(listData.getSize() - 1);
				settings.lastDirectory = file.getParent();
				
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, 
						Static.wordWrap(Static.simpleString(e)), 
						res.getString("error"), JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	/**
	 * The refresh result on a file
	 */
	void refresh(int i) {
		if (list.getSelectedIndex() != -1) {
			
			if (listData.get(i).stopRun(view.getIcon(), i, false)) {
				File file = listData.get(i).getFile();
				
				try (DiskIcons image = DiskImage.open(file, "r")) {
					listData.set(i, new ListItem(this, image, file));
					list.setSelectedIndex(i);
					updateDiskIcon();
					saveButton(i);
					
				} catch (IOException e) {
					JOptionPane.showMessageDialog(this, 
							Static.wordWrap(Static.simpleString(e)), 
							res.getString("error"), JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}
	
	/**
	 * The closing result of a file
	 */
	void close(int i) {
		if (list.getSelectedIndex() != -1) {
			
			if (listData.get(i).stopRun(view.getIcon(), i, false)) {
				listData.remove(i);
				if (listData.size() > 0) {
					if (i == listData.size())
						i--;
					list.setSelectedIndex(i);
				}
			}
		}
	}
	
	/**
	 * Returns {@code true} if there are no operations changed. Otherwise, 
	 *  the user responds to the message if they want to save the changed 
	 *  operations and returns {@code true}. If you respond with cancel, 
	 *  the operation will be canceled with {@code false}.
	 * 
	 * @return	true if is safe to close the application, false otherwise.
	 */
	boolean canCloseNow() {
		for (int i = 0, s = listData.getSize(); i < s; i++) {
			if (!listData.get(i).stopRun(view.getIcon(), i, true)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Saves the window geometry and current settings.
	 */
	void saveSettings() {
		settings.windowRect = getBounds();
		settings.splitLocation = split.getDividerLocation();
		settings.write();
	}

	/**
	 * About the selected item.
	 */
	void onSelectListItem() {
		int s = list.getSelectedIndex();
		boolean sel = s != -1;
		if (sel) {
			view.doRepaint(listData.get(s));
			deck.show(main, "view");
			saveButton(s);
		} else {
			about.requestFocusInWindow();
			saveButton.setIcon(iconSave);
			saveButton.setEnabled(false);
			deck.show(main, "about");
		}
		saveAsButton.setEnabled(sel);
		aboutButton.setEnabled(sel);
	}

	/**
	 * Treat saveButton according to undo.
	 */
	void saveButton() {
		int s = list.getSelectedIndex();
		if (s != -1) saveButton(s);
	}
	private void saveButton(int s) {
		boolean undo = listData.get(s).getUndo();
		saveButton.setIcon(undo? iconUndo: iconSave);
		saveButton.setEnabled(undo);
	}
	
	/**
	 * First, the screen that mostly intersects the window is selected. 
	 *  Then, if necessary, the window is moved and resized to entirely
	 *  fit in the screen.
	 */
	private void adjustBounds() {
		Rectangle screen = null, frame = settings.windowRect;
		long fit = 0;
		for (GraphicsDevice gd: GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
			GraphicsConfiguration gc = gd.getDefaultConfiguration();
			Insets i = Toolkit.getDefaultToolkit().getScreenInsets(gc);
			Rectangle b = gc.getBounds();
			b.x += i.left;
			b.y += i.top;
			b.width -= i.right + i.left;
			b.height -= i.bottom + i.top;
			Rectangle f = frame.intersection(b);
			if (screen == null || (long)f.width * f.height > fit) {
				fit = (long)f.width * f.height;
				screen = b;
			}
		}
		if (screen != null) {
			frame.x = Math.max(frame.x, screen.x);
			frame.y = Math.max(frame.y, screen.y);
			frame.width = Math.min(frame.width, screen.width);
			frame.height = Math.min(frame.height, screen.height);
			frame.x = Math.min(frame.x, screen.x + screen.width - frame.width);
			frame.y = Math.min(frame.y, screen.y + screen.height - frame.height);
			settings.windowRect = frame;
		}
	}

	/**
	 *  The JToolBar is a group of commonly used components such as buttons
	 *   or drop down menu.
	 * 
	 * @return	The component to be added to the Window hierarchy.
	 */
	private JToolBar setupToolBar(JToolBar tb) {
		tb.invalidate();
		tb.removeAll();
		validate();
		
		tb.add(openButton = new JToolButton(res.getString("open"), "open.png", 
				res.getString("open_msg") + ": " + res.getString("open_txt")));
		openButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openDiskIcon();
			}
		});
		
		tb.add(saveButton = new JToolButton(res.getString("save"), "save.png", 
				res.getString("save_msg") + ": " + res.getString("save_txt")));
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					saveDiskIcon();
				} catch (IOException | WrongHeaderException t) {
					t.printStackTrace();
				}
			}
		});
		
		tb.add(saveAsButton = new JToolButton(res.getString("save_as"), "save_as.png", 
				res.getString("save_as_msg") + ": " + res.getString("save_as_txt")));
		saveAsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					saveAsDiskIcon();
				} catch (IOException | WrongHeaderException t) {
					t.printStackTrace();
				}
			}
		});
		
		tb.add(settingsButton = new JToolButton(res.getString("settings"), "settings.png", 
				res.getString("settings_msg") + ": " + res.getString("settings_txt")));
		settingsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				editSettings();
			}
		});
		
		tb.add(aboutButton = new JToolButton(res.getString("about"), "about.png", 
				res.getString("about_msg") + "" + ""));
		aboutButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showAboutDialog();
			}
		});
		
		tb.setFloatable(false);
		tb.setFocusable(false);
		return tb;
	}

	private class JToolButton extends JButton {
		private static final long serialVersionUID = -6228916287454044833L;

		public JToolButton(String text, String icon, String tip) {
			super(new ImageIcon(getResource(icon)));
			setHorizontalTextPosition(SwingConstants.CENTER);
			setVerticalTextPosition(SwingConstants.BOTTOM);
			setRequestFocusEnabled(false);
			setFocusable(false);
			setToolTipText(tip);
			setText(text);
		}
		
	}

	private void openDiskIcon() {
		if (settings.filterImageFiles) {
			dialog.setFile(isWindows ? WINDOWS_FILE_FILTER : null);
			dialog.setTitle(res.getString("open_msg"));
			dialog.setMode(FileDialog.LOAD);
			dialog.setMultipleMode(true);
			dialog.setVisible(true);
		} else {
			chooser.setDialogTitle(res.getString("open_msg"));
			chooser.setMultiSelectionEnabled(true);
			if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
				return;
		}
		for (File file : settings.filterImageFiles ? dialog.getFiles() : chooser.getSelectedFiles()) {
			addToList(file);
		}
	}
	
	void updateDiskIcon() {
		int s = list.getSelectedIndex();
		if (s != -1) {
			view.doRepaint(listData.get(s));
		}
	}
	
	void saveDiskIcon() throws IOException, WrongHeaderException {
		int s = list.getSelectedIndex();
		if (s != -1) {
			File file = listData.get(s).getFile();
			if (settings.warnSaveOperation) {
				if (JOptionPane.showConfirmDialog(this, 
						String.format(res.getString("confirm_save"), file.getName()), 
						res.getString("save_msg"), JOptionPane.YES_NO_OPTION, 
						JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) 
					return;
			}
			String type = Static.getExtension(file);
			listData.get(s).copy(file, type, view.getIcon());
		}
	}

	void saveAsDiskIcon() throws IOException, WrongHeaderException {
		int s = list.getSelectedIndex();
		if (s != -1) {
			String source = listData.get(s).getFile().getName();
			String ext = Static.getExtension(source).toLowerCase();
			String target;
			if (ext.equals("icns"))
				target = Static.replaceExtension(source, "ico");
			else
			if (ext.equals("ico"))
				target = Static.replaceExtension(source, "icns");
			else
				target = String.format(res.getString("save_as_dup"), source);
			File file;
			if (settings.filterImageFiles) {
				dialog.setFile(target);
				dialog.setTitle(res.getString("save_as_msg"));
				dialog.setMode(FileDialog.SAVE);
				dialog.setMultipleMode(false);
				dialog.setVisible(true);
				if (dialog.getFile() == null)
					return;
				file = new File(dialog.getDirectory(), dialog.getFile());
			} else {
				chooser.setSelectedFile(new File(target));
				chooser.setDialogTitle(res.getString("save_as_msg"));
				chooser.setMultiSelectionEnabled(false);
				if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
					return;
				file = chooser.getSelectedFile();
			}
			if (Static.getExtension(file).length() == 0)
				file = new File(file.getPath()+"."+Static.getExtension(source));
			if (file.compareTo(listData.get(s).getFile()) == 0) {
				JOptionPane.showMessageDialog(this, 
						String.format(res.getString("error_old_image"), file.getName()), 
						res.getString("save_as_msg"), JOptionPane.ERROR_MESSAGE);
				return;
			}
			String type = Static.getExtension(file).toUpperCase();
			if (!ListItem.IMAGE_TYPE.contains(type)) {
				JOptionPane.showMessageDialog(this, 
						String.format(res.getString("error_image_type"), type), 
						res.getString("save_as_msg"), JOptionPane.ERROR_MESSAGE);
				return;
			}
			listData.get(s).copy(file, type, view.getIcon());
		}
	}

	void editSettings() {
		new SettingsDialog(this);
		onLocaleChange();
		
		setupFileDialog(settings.filterImageFiles);
		updateDiskIcon();
		
		onSelectListItem();
	}

	void showAboutDialog() {
		savedListIndex = list.getSelectedIndex();
		list.clearSelection();
	}

	/**
	 * Sets the About content and behavior and returns an abstract swing
	 *  {@code JComponent} that can be an enclosing Container, the content 
	 *  scroll bars or the content component itself.
	 *  
	 * @return	The component to be added to the Window hierarchy.
	 */
	private JComponent setupAboutDialog() {
		about.setBorder(BorderFactory.createEmptyBorder());
		about.setTransferHandler(null);
		about.setEditable(false);
		
		about.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) try {
					if (e.getURL().getProtocol().matches("file|jar")) {
						about.setPage(e.getURL());
					} else
					if (Desktop.isDesktopSupported()) {
						Desktop.getDesktop().browse(e.getURL().toURI());
					}
				} catch (IOException | URISyntaxException t) {
					t.printStackTrace();
				}
			}
		});
		
		about.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE && savedListIndex != -1) {
					list.setSelectedIndex(savedListIndex);
					list.requestFocusInWindow();
				}
			}
		});
		
		return new JScrollPane(about);
	}

	/**
	 * Enables or disables the file name filter. This doesn't work on Windows,
	 *  as a workaround the files are filtered with a wild card pattern in the
	 *  open dialog with {@link FileDialog#setFile(String)}.
	 *  
	 * @param filter {@code true} to enable the file name filter, {@code false} to disable
	 */
	private void setupFileDialog(boolean filter) {
		if (filter)
			dialog.setFilenameFilter(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.matches(DEFAULT_FILE_FILTER);
				}
			});
		else dialog.setFilenameFilter(null);
	}

	/**
	 * Returns the URL of the resource {@code name} that is in the same folder
	 *  of the class used to create the FronEnd.
	 * 
	 * @param name	The name of the resource.
	 * @return	The resource URL, or {@code null} if the resource does not exist.
	 */
	static URL getResource(String name) {
		String path = FrontEnd.class.getPackage().getName().replace('.', '/');
		return FrontEnd.class.getClassLoader().getResource(path + '/' + name);
	}
}
