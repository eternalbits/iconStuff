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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import io.github.eternalbits.disk.DiskIcons;
import io.github.eternalbits.disk.DiskIconsView;
import io.github.eternalbits.disk.DiskImage;
import io.github.eternalbits.disk.DiskImageView;
import io.github.eternalbits.disk.WrongHeaderException;
import io.github.eternalbits.icns.IcnsHeader;
import io.github.eternalbits.icons.Icons;
import io.github.eternalbits.icons.Static;

class ListItem {
	static final List<String> IMAGE_TYPE = Arrays.asList(new String[] {"ICO", "ICNS", "PNG"});
	
	private final FrontEnd app;
	private final DiskIcons image;
	private final File file;
	
	ListItem(FrontEnd frontEnd, DiskIcons image, File file) {
		this.app  = frontEnd;
		this.image = image;
		this.file = file;
		Icons.dump(image.getShow());
	}
	
	@Override
	public String toString() {
		return file.getName();
	}
	
	DiskImageView getView() {
		return image.getView();
	}
	
	void putIcon(DiskIconsView fs) {
		image.putIcon(fs);
	}
	
	File getFile() {
		return file;
	}
	
	boolean getUndo() {
		return image.getUndo();
	}
	void setUndo(boolean undo) {
		image.setUndo(undo);
		app.saveButton();
	}
	
	/**
	 * The user responds to the message if they want to save the changed 
	 *  operations and returns {@code true}. If you respond with cancel, 
	 *  the operation will be canceled with {@code false}.
	 *   
	 * @param icon	A list with the icon and output.
	 * @param index	Index of the image to be processed.
	 * @param stop	True if coming from canCloseNow or false if coming from update or close.
	 * @return	true if is all right to proceed, false otherwise.
	 */
	boolean stopRun(String icon, int index, boolean stop) {
		if (image.getUndo()) {
			app.list.setSelectedIndex(index);
			switch (JOptionPane.showConfirmDialog(app, 
					String.format(app.res.getString("confirm_save"), getFile().getName()), 
					app.res.getString("save_msg"), 
					stop? JOptionPane.YES_NO_CANCEL_OPTION: JOptionPane.YES_NO_OPTION, 
					JOptionPane.WARNING_MESSAGE)) {
			case JOptionPane.YES_OPTION:
				String type = Static.getExtension(file);
				copy(file, type, icon);
				return true;
			case JOptionPane.NO_OPTION:
				return true;
			default:
				return false;
			}
		}
		return true;
	}

	/**
	 * This routine saves the changes to saveDiskIcon and saveAsDiskIcon.
	 * 
	 * @param to	File we want to overlay.
	 * @param type	Extension type: ico, icns or png.
	 * @param icon	A list with the icon and output.
	 */
	void copy(File to, String type, String icon) {
		if (type.toLowerCase().equals("icns") && app.settings.warnSaveNonStandard) {
			for (DiskIconsView fs: getView().fileIcons) {
				if (fs.isIcon > 0) {	// PNG, BITMAP, APPLE, ARGB
					String fs_layout = fs.size+" "+Static.getIcon(fs.layout);
					String[] fs_type = IcnsHeader.OSMatch(fs_layout, fs.type);
					if (fs_type == null) {
						JOptionPane.showMessageDialog(app, 
								String.format(app.res.getString("error_standard"), fs.layout), 
								app.res.getString("save_as_msg"), JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
			}
		}
		File copy = null;
		boolean save = image.getPath().equals(to.getPath());
		try (DiskIcons clone = DiskImage.create(type, to, image, type.equals("ICO") ? icon : null)) {
			copy = to; // copy open by DiskImage
			if (save) 
				image.setUndo(false);
		} catch (IOException | WrongHeaderException e) {
			e.printStackTrace();
		}
		finally {
			if (copy != null) {
				if (copy.isFile() && copy.length() == 0) 
					copy.delete();
				else app.addToList(to);
				String result = save ? "changed" : "created";
				System.out.println(to != null && to.isFile()? String.format(app.res.getString("image_"+result), 
						to.getName(), to.getAbsoluteFile().getParent()): app.res.getString("image_not_"+result));
			}
		}
	}
}
