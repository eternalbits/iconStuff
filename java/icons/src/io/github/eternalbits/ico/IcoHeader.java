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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import io.github.eternalbits.bitmap.MapHeader;
import io.github.eternalbits.disk.DiskIcons;
import io.github.eternalbits.disk.DiskIconsView;
import io.github.eternalbits.disk.WrongHeaderException;
import io.github.eternalbits.icons.Static;
import io.github.eternalbits.png.PngFiles;
import io.github.eternalbits.png.PngHeader;

/**
 * Microsoft ICO File Routine.
 * <p>
 */
class IcoHeader {
	static final int HEADER_SIZE = 6;
	
	private final PngHeader img;	// Parent object to associated PNG
	private final MapHeader map;	// Parent object to associated BITMAP
	
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
	 * In the case of a bitmap, this is the value to be inserted in the description.
	 */
	private String dirEntry() {
		String dirEntry = String.valueOf(width);
		if (width != height)
			dirEntry += "x" + String.valueOf(height);
		if (colors == 0)
			dirEntry += " " + String.valueOf(bits) + "-bit RGB" + (bits == 32? "A": "");
		else dirEntry += " " + String.valueOf(colors) + " mask";
		return dirEntry;
	}
	
	/**
	 * Microsoft ICO file writing routine.
	 * <p>
	 */
	IcoHeader(IcoFiles ico, DiskIcons image, String icon) throws IOException, WrongHeaderException {
		img = new PngHeader();
		map = new MapHeader();
		if (image.getFiles() == null) return;
		if (Static.delimiterIcon(icon, image))
			throw new WrongHeaderException(getClass(), ico.getPath());
		
		/**
		 * Start by searching the available icons
		 */
		List<DiskIconsView> local = new ArrayList<DiskIconsView>();
		for (DiskIconsView fs: image.getFiles()) {
			if (fs.isIcon > 0 && fs.forIcon != -1) {	// PNG, BITMAP, APPLE, ARGB
				if (fs.size == 0)
					fs.size = Static.getInteger(fs.layout);
				fs.forIcon = fs.layout.endsWith("PNG")? DiskIcons.ICON_PNG: DiskIcons.ICON_BITMAP;
				local.add(fs);
			}
		}
		
		if (local.isEmpty()) return;
		
		RandomAccessFile to = ico.getMedia();
		ico.done = true;
		to.setLength(0);
		
		/**
		 * Writes the header and the structure of image directory empty
		 */
		byte[] header = new byte[HEADER_SIZE];
		ByteBuffer tw = ByteBuffer.wrap(header).order(IcoFiles.BYTE_ORDER);
		tw.putShort((short) 0);
		tw.putShort((short) 1);
		tw.putShort((short) local.size());
		to.write(header);
		
		byte[] structure = new byte[16 * local.size()];
		to.write(structure);
		
		/**
		 * Then write the icons
		 */
		for (DiskIconsView fs: local) {
			BufferedImage fs_image = Static.copyPng(fs.image, fs.size, fs.layout);
			int power = fs.size;
			
			if (fs.forIcon == DiskIcons.ICON_BITMAP) {
				fs.length = map.writeBitmap(fs_image, to, power);						// Passing bytes from a saved image to a bitmap
			}
			else {
				fs.length = map.writePng(fs_image, to);									// Passing bytes from a saved image to PNG
			}
		}
		
		/**
		 * Finally write the full structure of image directory
		 */
		to.seek(HEADER_SIZE);
		int offset = HEADER_SIZE + 16 * local.size();
		tw = ByteBuffer.wrap(structure).order(IcoFiles.BYTE_ORDER);
		for (DiskIconsView fs: local) {
			int power = fs.size;
			tw.put((byte) power);
			tw.put((byte) power);
			tw.put((byte) 0);
			tw.put((byte) 0);
			tw.putShort((short) 1);
			tw.putShort((short) 32);
			tw.putInt(fs.length);
			tw.putInt(offset);
			offset += fs.length;
		}
		to.write(structure);
		
	}
	
	/**
	 * Microsoft ICO file reading routine.
	 * <p>
	 */
	IcoHeader(IcoFiles ico, ByteBuffer in) throws IOException, WrongHeaderException {
		img = new PngHeader();
		map = new MapHeader();
		
		if (in.remaining() >= HEADER_SIZE) {
			in.order(IcoFiles.BYTE_ORDER);
			
			alwaysBe0			= in.getShort();
			signature			= in.getShort();
			number				= in.getShort();
			
			if (alwaysBe0 == 0 && signature == 1 && number > 0 && number < 256) {
				
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
					view.isIcon = DiskIcons.NOT_AN_ICON;
					view.offset = position;
					view.length = size;
					view.type = ico.getType();
					view.description = dirEntry();
					
					tr = ico.readIcon(position, 8).order(ByteOrder.BIG_ENDIAN);
					if (tr.limit() >= 8 && tr.getInt(0) == PngFiles.ICON_PGN && tr.getInt(4) == PngFiles.DOS_UNIX) { // %PNG....
						view.isIcon = DiskIcons.ICON_PNG;
						view.description = img.ImageHeader(ico, position + 8, size);
						view.image = img.createPng(ico, view.offset, view.length);
						view.layout = view.description;
					} else {
						tr.order(ByteOrder.LITTLE_ENDIAN);
						if (tr.limit() >= 8 && tr.getInt(0) == 40) { // BITMAP
							if (width == 0 && height == 0)  // width and height = 0
								view.description = tr.getInt(4) + view.description.substring(1);
							view.layout = view.description.replaceFirst(" RGBA?", "");
							
							int power = Static.getInteger(view.layout);
							if (view.length == Static.bitmapRound(power)) {
								view.image = map.createBitmap(ico.getMedia(), view.offset, view.length, power);
								if (view.image != null)
									view.isIcon = DiskIcons.ICON_BITMAP;
							}
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
