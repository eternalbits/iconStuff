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

package io.github.eternalbits.disk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

/**
 * A read-only view of a {@link DiskIcons}. All fields are public and final.
 * <p>
 */
public class DiskImageShow {
	
	public final String filePath;					// Converts this abstract pathname into a pathname string.
	public final String fileType;					// Access type: ICNS (Apple), ICO (Microsoft) or PNG (Boutell).
	public final long fileLength;					// The length of the image file, in bytes. Information is purely informative.
	
	public final List<DiskIconsShow> fileIcons;		// Represents the structure of an icon through repeated DiskIconsShow input.
	
	private static Hashtable<Integer, String> icon = new Hashtable<>();	// A copy of the code plus the description of DiskIcons
	static {
		icon.put(-1, "NOT_AN_ICON");
		icon.put(0, "TABLE_OF_CONTENTS");
		icon.put(1, "ICON_PNG");
		icon.put(2, "ICON_BITMAP");
		icon.put(3, "ICON_APPLE");
		icon.put(4, "ICON_MASK");
		icon.put(5, "ICON_ARGB");
	}
	
	/**
	 * Outputs --dump to a file as described in {@link DiskImageShow}.
	 * 
	 * @param image	The disk image to be created.
	 */
	DiskImageShow(DiskIcons image) {
		filePath 		= image.path;
		fileType 		= image.getType();
		fileLength 		= image.length;
		
		List<DiskIconsShow> local = new ArrayList<DiskIconsShow>();
		if (image.getFiles() != null) {
			for (DiskIconsView fs: image.getFiles()) {
				DiskIconsShow show = new DiskIconsShow();
				show.isIcon		= icon.get(fs.isIcon);
				show.offset		= fs.offset;
				show.length		= fs.length;
				show.type		= fs.type;
				show.description= fs.description;
				show.image		= fs.image == null? null: "Buffered " + fs.image.getWidth() + 
						(fs.image.getWidth() != fs.image.getHeight()? "x" + fs.image.getHeight(): "");
				local.add(show);
			}
		}
		fileIcons = Collections.unmodifiableList(local);
	}
	
	public class DiskIconsShow {
		
		public String			isIcon;				// This is the type of icon input that goes from NOT_AN_ICON to ICON_ARGB
		public int				offset;				// The offset goes from the beginning of the file to the beginning of the icon
		public int				length;				// Icon length, in bytes
		public String			type;				// The type can be PNG, ICO or a character set from the Apple macOS
		public String			description;		// A brief description of the icon for the viewer
		public String			image;				// A PNG image
		
	}
	
}
