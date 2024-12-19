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

package io.github.eternalbits.icns;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
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
class IcnsHeader {
	static final int HEADER_SIZE = 8;
	
	static final int ICON_ICNS = 0x69636e73;	// icns
		
	private final PngHeader img;	// Parent object
	
	List<DiskIconsView> disk = new ArrayList<DiskIconsView>();

	int		signature;				// Magic literal, must be "icns" (0x69, 0x63, 0x6e, 0x73)
	int		fileLength;				// Length of file, in bytes, msb first
	
	byte[]	type = new byte[4];		// Icon type, see OSType below
	int		length;					// Length of data, in bytes (including type and length), msb first
	
	static private final OSType[] osType = new OSType[] {
			new OSType("TOC ", "Table of contents", ""),
			new OSType("ICON", "32 1-bit mono", ""),
			new OSType("ICN#", "32 1-bit mono", ""),
			new OSType("icm#", "16×12 1 bit mono", ""),
			new OSType("icm4", "16x12 4 bit", ""),
			new OSType("icm8", "16x12 8 bit", ""),
			new OSType("ics#", "16 1-bit mono", ""),
			new OSType("ics4", "16 4-bit", ""),
			new OSType("ics8", "16 8 bit", ""),
			new OSType("is32", "16 24-bit RGB", "s8mk"),
			new OSType("s8mk", "16 8-bit mask", "is32"),
			new OSType("icl4", "32 4-bit", ""),
			new OSType("icl8", "32 8-bit", ""),
			new OSType("il32", "32 24-bit RGB", "l8mk"),
			new OSType("l8mk", "32 8-bit mask", "il32"),
			new OSType("ich#", "48 1-bit mono", ""),
			new OSType("ich4", "48 4-bit", ""),
			new OSType("ich8", "48 8-bit", ""),
			new OSType("ih32", "48 24-bit RGB", "h8mk"),
			new OSType("h8mk", "48 8-bit mask", "ih32"),
			new OSType("it32", "128 24-bit RGB", "t8mk"),
			new OSType("t8mk", "128 8-bit mask", "it32"),
			new OSType("icp4", "16 JPEG or PNG or 24-bit RGB", "16 PNG"),
			new OSType("icp5", "32 JPEG or PNG or 24-bit RGB", "32 PNG"),
			new OSType("icp6", "48 JPEG or PNG", "48 PNG"),
			new OSType("ic07", "128 JPEG or PNG", "128 PNG"),
			new OSType("ic08", "256 JPEG or PNG", "256 PNG"),
			new OSType("ic09", "512 JPEG or PNG", "512 PNG"),
			new OSType("ic10", "1024 JPEG or PNG", "1024² PNG"),
			new OSType("ic11", "32 JPEG or PNG", "32² PNG"),
			new OSType("ic12", "64 JPEG or PNG", "64² PNG"),
			new OSType("ic13", "256 JPEG or PNG", "256² PNG"),
			new OSType("ic14", "512 JPEG or PNG", "512² PNG"),
			new OSType("ic04", "16 sRGB or JPEG or PNG", "16 PNG"),
			new OSType("ic05", "32 sRGB or JPEG or PNG", "32² PNG"),
			new OSType("icsb", "18 sRGB or JPEG or PNG", "18 PNG"),
			new OSType("icsB", "36 JPEG or PNG", "32² PNG"),
			new OSType("sb24", "24 JPEG or PNG", "24 PNG"),
			new OSType("SB24", "48 JPEG or PNG", "48² PNG"),
		};
	
	/**
	 * 
	 * <p>
	 */
	static private class OSType {
		private String type;
		private String description;	
		private String match;
		public OSType(String type, String description, String match) {
			this.type = type;
			this.description = description;
			this.match = match;
		}
		public String toString() {
			return description;
		}
	}
	
	/**
	 * 
	 * <p>
	 */
	static String OSType(String type) {
		for (OSType array : osType) {
			if (array.type.equals(type)) {
				return array.description;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * <p>
	 */
	static String OSMatch(String match) {
		for (OSType array : osType) {
			if (array.match.equals(match)) {
				return array.type;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * <p>
	 */
	IcnsHeader(IcnsFiles icns, DiskIcons image) throws IOException {
		img = new PngHeader();
		if (image.getFiles() == null) return;
		
		/**
		 * 
		 */
		List<DiskIconsView> local = new ArrayList<DiskIconsView>();
		for (DiskIconsView fs: image.getFiles()) {
			if (fs.isPng) {
				fs.type = OSMatch(fs.description);
				if (fs.type != null) {
					fs.length += 8;
					local.add(fs);
				}
			}
		}
		
		if (local.isEmpty()) return;
		
		RandomAccessFile from = image.getMedia();
		RandomAccessFile to = icns.getMedia();
		
		/**
		 * 
		 */
		DiskIconsView es = new DiskIconsView();
		if (local.size() > 1) {
			es.isPng		= false;
			es.length 		= local.size() * 8 + 8;
			es.type			= osType[0].type;
			es.description 	= osType[0].description;
			local.add(0, es);
		}
		
		/**
		 * 
		 */
		int offset = HEADER_SIZE;
		for (DiskIconsView fs: local) {
			offset += fs.length;
		}
		
		/**
		 * 
		 */
		byte[] header = new byte[HEADER_SIZE];
		ByteBuffer tw = ByteBuffer.wrap(header).order(IcnsFiles.BYTE_ORDER);
		tw.putInt(ICON_ICNS);
		tw.putInt(offset);
		to.write(header);
		
		/**
		 * 
		 */
		if (local.size() > 1) {
			byte[] detail = new byte[8 * local.size()];
			tw = ByteBuffer.wrap(detail).order(IcnsFiles.BYTE_ORDER);
			for (DiskIconsView fs: local) {
				tw.put(fs.type.getBytes(StandardCharsets.US_ASCII));
				tw.putInt(fs.length);
			}
			to.write(detail);
		}
		
		/**
		 * 
		 */
		offset = HEADER_SIZE;
		for (DiskIconsView fs: local) {
			if (fs.isPng) {
				tw = ByteBuffer.wrap(header).order(IcnsFiles.BYTE_ORDER);
				tw.put(fs.type.getBytes(StandardCharsets.US_ASCII));
				tw.putInt(fs.length);
				to.write(header);
				
				img.WriteImage(from, fs.offset, to, fs.length - 8);
			}
		}
		
	}
	
	/**
	 * 
	 * <p>
	 */
	IcnsHeader(IcnsFiles icns, ByteBuffer in) throws IOException, WrongHeaderException {
		img = new PngHeader();
				
		if (in.remaining() >= HEADER_SIZE) {
			in.order(IcnsFiles.BYTE_ORDER);
			
			signature			= in.getInt();
			fileLength			= in.getInt();
			
			if (signature == ICON_ICNS && fileLength == icns.getLength()) {
				
				int offset = HEADER_SIZE;
				ByteBuffer tr = null;
								
				while (offset < icns.getLength()) {
					tr = icns.readIcon(offset, 16).order(IcnsFiles.BYTE_ORDER);
					if (tr.remaining() != 16)
						throw new WrongHeaderException(getClass(), icns.getPath());
					tr.get(type);
					length = tr.getInt();
					if (length < 8 || offset + length > icns.getLength())
						throw new WrongHeaderException(getClass(), icns.getPath());
					
					DiskIconsView view = new DiskIconsView();
					view.isPng = false;
					view.offset = offset + 8;
					view.length = length - 8;
					view.type = new String(type);
					view.description = OSType(view.type);
					
					tr.order(ByteOrder.BIG_ENDIAN);
					if (tr.getInt() == PngFiles.ICON_PGN && tr.getInt() == PngFiles.DOS_UNIX) { // %PNG....
						view.isPng = true;
						view.description = img.ImageHeader(icns, offset + 16, length - 8);
					}
					offset += length;
					disk.add(view);
				}

				return;
			}
		}
		
		throw new WrongHeaderException(getClass(), icns.getPath());
	}
}
