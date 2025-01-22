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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Abstract class that represents a disk icon. Icon images generally have
 *  multiple images on the same theme with different resolutions.
 * <p>
 */
public abstract class DiskIcons implements AutoCloseable {
	
	public static final int NOT_AN_ICON = -1;			// This entry is not an icon, it is a Table of contents or other unknown entry.
	public static final int TABLE_OF_CONTENTS = 0;		// It's a Table of contents.
	public static final int ICON_PNG = 1;				// It's a PNG type icon.
	public static final int ICON_BITMAP = 2;			// It's a bitmap icon with RGB and a mask from Microsoft.
	public static final int ICON_APPLE = 3;				// It's an icon with two parts, one is the RGB part from Apple.
	public static final int ICON_MASK = 4;				// It's an icon with two parts, other is the mask from Apple.
	public static final int ICON_ARGB = 5;				// It's an icon with ARGB part from Apple.
	
	protected RandomAccessFile media = null;			// All inputs and outputs are done through a RandomAccessFile.
	public RandomAccessFile getMedia() {
		return media;
	}
	
	public abstract List<DiskIconsView> getFiles();		// Represents the structure of an icon through repeated DiskIconsView input.
	public DiskImageView getView() {
		return new DiskImageView(this);
	}

	protected String path = null;						// Converts this abstract pathname into a pathname string.
	public String getPath() {
		return path;
	}
	
	protected String type = null;						// Access type: ICNS (Apple), ICO (Microsoft) or PNG (Boutell).
	public abstract void setType();
	public String getType() {
		return type;
	}
		
	protected long length = 0;							// The length of the image file, in bytes. Information is purely informative.
	public long getLength() {
		return length;
	}
	
	/**
	 * Represents a reading of an icon that is transformed into a ByteBuffer.
	 *  There is no order as it can be used as LITTLE_ENDIAN or BIG_ENDIAN
	 *  depending on the situation.
	 * 
	 * @param offset	The reading position.
	 * @param length	Number of bytes to be read.
	 * @return	The resulting ByteBuffer.
	 */
	public ByteBuffer readIcon(long offset, int length) throws IOException {
		media.seek(offset);
		byte[] buffer = new byte[length];
		int read = media.read(buffer, 0, length);
		return ByteBuffer.wrap(buffer, 0, read < 0? 0: read);
	}
	
	/**
	 * The close() method of an AutoCloseable object is called automatically when
	 *  exiting a try-with-resources block for which the object has been declared
	 *  in the resource specification header.
	 */
	@Override
	public synchronized void close() throws IOException {
		if (media != null) {
			media.close();
			media = null;
		}
	}
	
}
