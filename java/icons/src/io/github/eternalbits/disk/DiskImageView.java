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

package io.github.eternalbits.disk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A read-only view of a {@link DiskIcons}. All fields are public and final.
 * <p>
 */
public class DiskImageView {
	
	public final String filePath;					// Converts this abstract pathname into a pathname string.
	public final String fileType;					// Access type: ICNS (Apple), ICO (Microsoft) or PNG (Boutell).
	public final long fileLength;					// The length of the image file, in bytes. Information is purely informative.
	
	public final List<DiskIconsView> fileIcons;		// Represents the structure of an icon through repeated DiskIconsView input.
	
	/**
	 * Outputs --dump to a file as described in {@link DiskImageView}.
	 * 
	 * @param image	The disk image to be created.
	 */
	DiskImageView(DiskIcons image) {
		filePath 		= image.path;
		fileType 		= image.getType();
		fileLength 		= image.length;
		
		List<DiskIconsView> local = new ArrayList<DiskIconsView>();
		if (image.getFiles() != null) {
			for (DiskIconsView fs: image.getFiles()) {
				local.add(fs);
			}
		}
		fileIcons = Collections.unmodifiableList(local);
	}
	
}
