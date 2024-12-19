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

package io.github.eternalbits.ico;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import io.github.eternalbits.disk.DiskIcons;
import io.github.eternalbits.disk.DiskIconsView;
import io.github.eternalbits.disk.WrongHeaderException;
import io.github.eternalbits.png.PngFiles;
import io.github.eternalbits.png.PngHeader;

/**
 * 
 * <p>
 */
class IcoHeader {
	static final int HEADER_SIZE = 6;
	
	private final PngHeader img;	// Parent object
	
	List<DiskIconsView> disk = new ArrayList<DiskIconsView>();

	short	alwaysBe0;				// Reserved, must always be 0
	short	signature;				// Specifies image type 1 for icon, lsb first
	short	number;					// Specifies number of images in the file, lsb first
	
	int		width;					// Specifies image width in pixels
	int		height;					// Specifies image height in pixels
	byte	colors;					// Specifies number of colors in the color palette
	byte	reserved;				// Reserved, should be 0
	short	planes;					// Specifies color planes, should be 0 or 1
	short	bits;					// Specifies bits per pixel
	int		size;					// Specifies the size of the image's data in bytes
	int		position;				// Specifies the offset of data from the beginning
	
	/**
	 * 
	 * <p>
	 */
	private String dirEntry() {
		String dirEntry = String.valueOf(width);
		if (width != height)
			dirEntry += "x" + String.valueOf(height);
		if (colors == 0)
			dirEntry += " " + String.valueOf(bits) + "-bit";
		else dirEntry += " mask";
		return dirEntry + " sRGB";
	}
	
	/**
	 * 
	 * <p>
	 */
	IcoHeader(IcoFiles ico, DiskIcons image) throws IOException {
		img = new PngHeader();
		if (image.getFiles() == null) return;
		
		/**
		 * 
		 */
		List<DiskIconsView> local = new ArrayList<DiskIconsView>();
		for (DiskIconsView fs: image.getFiles()) {
			if (fs.isPng) {
				local.add(fs);
			}
		}
		
		if (local.isEmpty()) return;
		
		RandomAccessFile from = image.getMedia();
		RandomAccessFile to = ico.getMedia();
		
		/**
		 * 
		 */
		byte[] header = new byte[HEADER_SIZE];
		ByteBuffer tw = ByteBuffer.wrap(header).order(IcoFiles.BYTE_ORDER);
		tw.putShort((short) 0);
		tw.putShort((short) 1);
		tw.putShort((short) local.size());
		to.write(header);
		
		/**
		 * 
		 */
		int offset = HEADER_SIZE + 16 * local.size();
		byte[] detail = new byte[16 * local.size()];
		tw = ByteBuffer.wrap(detail).order(IcoFiles.BYTE_ORDER);
		for (DiskIconsView fs: local) {
			tw.put((byte) 0);
			tw.put((byte) 0);
			tw.put((byte) 0);
			tw.put((byte) 0);
			tw.putShort((short) 1);
			tw.putShort((short) 32);
			tw.putInt(fs.length);
			tw.putInt(offset);
			offset += fs.length;
		}
		to.write(detail);
		
		/**
		 * 
		 */
		for (DiskIconsView fs: local) {
			img.WriteImage(from, fs.offset, to, fs.length);
		}
		
	}
	
	/**
	 * 
	 * <p>
	 */
	IcoHeader(IcoFiles ico, ByteBuffer in) throws IOException, WrongHeaderException {
		img = new PngHeader();
		
		if (in.remaining() >= HEADER_SIZE) {
			in.order(IcoFiles.BYTE_ORDER);
			
			alwaysBe0			= in.getShort();
			signature			= in.getShort();
			number				= in.getShort();
			
			if (alwaysBe0 == 0 && signature == 1) {
				
				long offset = HEADER_SIZE;
				ByteBuffer tr = null;
				
				for (int i = 0; i < number; i++) {
					tr = ico.readIcon(offset, 16).order(IcoFiles.BYTE_ORDER);
					if (tr.remaining() != 16)
						throw new WrongHeaderException(getClass(), ico.getPath());
					offset += tr.remaining();
					width	= tr.get() & 0xff;
					height	= tr.get() & 0xff;
					colors	= tr.get();
					reserved= tr.get();
					planes	= tr.getShort();
					bits	= tr.getShort();
					size	= tr.getInt();
					position= tr.getInt();
					if (position < 0 || size < 0 || (position + size) > ico.getLength())
						throw new WrongHeaderException(getClass(), ico.getPath());
					
					DiskIconsView view = new DiskIconsView();
					view.isPng = false;
					view.offset = position;
					view.length = size;
					view.type = ico.getType();
					view.description = dirEntry();
					
					tr = ico.readIcon(position, 8).order(ByteOrder.BIG_ENDIAN);
					if (tr.getInt() == PngFiles.ICON_PGN && tr.getInt() == PngFiles.DOS_UNIX) { // %PNG....
						view.isPng = true;
						view.description = img.ImageHeader(ico, position + 8, size);
					} else {
						tr.order(ByteOrder.LITTLE_ENDIAN);
						if (width == 0 && height == 0 && tr.getInt(0) == 40) { // width and height = 0
							view.description = tr.getInt(4) + view.description.substring(1);
						}
					}
					disk.add(view);
				}
				
				return;
			}
		}
		
		throw new WrongHeaderException(getClass(), ico.getPath());
	}
}
