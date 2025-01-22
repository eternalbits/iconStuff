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

import io.github.eternalbits.bitmap.MapHeader;
import io.github.eternalbits.disk.DiskIcons;
import io.github.eternalbits.disk.DiskIconsView;
import io.github.eternalbits.disk.WrongHeaderException;
import io.github.eternalbits.icons.Static;
import io.github.eternalbits.png.PngFiles;
import io.github.eternalbits.png.PngHeader;

/**
 * Apple ICNS File Routine.
 * <p>
 */
public
class IcnsHeader {
	static final int HEADER_SIZE = 8;
	
	private final PngHeader img;	// Parent object to associated PNG
	private final MapHeader map;	// Parent object to associated BITMAP
	
	List<DiskIconsView> disk = new ArrayList<DiskIconsView>();

	int		signature;				// Magic literal, must be "icns" (0x69, 0x63, 0x6e, 0x73)
	int		fileLength;				// Length of file, in bytes, msb first
	
	byte[]	type = new byte[4];		// Icon type, see OSType below
	int		length;					// Length of data, in bytes (including type and length), msb first
	
	/**
	 * These are the types of icons used by Apple and a detailed description.
	 */
	static private class OSType {
		private String type;
		private String description;	
		public OSType(String type, String description) {
			this.type = type;
			this.description = description;
		}
	}
	
	static private final OSType[] osType = new OSType[] {
			new OSType("TOC ", "Table of contents"),
			new OSType("ICON", "32 1-bit mono"),
			new OSType("ICN#", "32 1-bit mono"),
			new OSType("icm#", "16×12 1 bit mono"),
			new OSType("icm4", "16x12 4 bit"),
			new OSType("icm8", "16x12 8 bit"),
			new OSType("ics#", "16 1-bit mono"),
			new OSType("ics4", "16 4-bit"),
			new OSType("ics8", "16 8 bit"),
			new OSType("is32", "16 24-bit RGB"),
			new OSType("s8mk", "16 8-bit mask"),
			new OSType("icl4", "32 4-bit"),
			new OSType("icl8", "32 8-bit"),
			new OSType("il32", "32 24-bit RGB"),
			new OSType("l8mk", "32 8-bit mask"),
			new OSType("ich#", "48 1-bit mono"),
			new OSType("ich4", "48 4-bit"),
			new OSType("ich8", "48 8-bit"),
			new OSType("ih32", "48 24-bit RGB"),
			new OSType("h8mk", "48 8-bit mask"),
			new OSType("it32", "128 24-bit RGB"),
			new OSType("t8mk", "128 8-bit mask"),
			new OSType("icp4", "16 JPEG or PNG or 24-bit RGB"),
			new OSType("icp5", "32 JPEG or PNG or 24-bit RGB"),
			new OSType("icp6", "48 JPEG or PNG"),
			new OSType("ic07", "128 JPEG or PNG"),
			new OSType("ic08", "256 JPEG or PNG"),
			new OSType("ic09", "512 JPEG or PNG"),
			new OSType("ic10", "1024 JPEG or PNG"),
			new OSType("ic11", "32 JPEG or PNG"),
			new OSType("ic12", "64 JPEG or PNG"),
			new OSType("ic13", "256 JPEG or PNG"),
			new OSType("ic14", "512 JPEG or PNG"),
			new OSType("ic04", "16 ARGB or JPEG or PNG"),
			new OSType("ic05", "32 ARGB or JPEG or PNG"),
			new OSType("icsb", "18 ARGB or JPEG or PNG"),
			new OSType("icsB", "36 JPEG or PNG"),
			new OSType("sb24", "24 JPEG or PNG"),
			new OSType("SB24", "48 JPEG or PNG"),
			new OSType("info", "Info binary plist"),
		};
	
	static String OSType(String type) {
		for (OSType array : osType) {
			if (array.type.equals(type)) {
				return array.description;
			}
		}
		return null;
	}
	
	/**
	 * Here is a list of really interesting icons. We are only interested in
	 *  PNG icons, ARGB icons, and 24-bit plus 8-bit mask icons. Note that
	 *  we folded the PNG for "retina" so that it is easy to translate.
	 */
	static private class OSMatch {
		private String type;
		private String match;
		private String mask;
		public OSMatch(String type, String match, String mask) {
			this.type = type;
			this.match = match;
			this.mask = mask;
		}
	}
	
	static private final OSMatch[] osMatch = new OSMatch[] {
			new OSMatch("is32", "16 32-bit", "s8mk"),
			new OSMatch("il32", "32 32-bit", "l8mk"),
			new OSMatch("ih32", "48 32-bit", "h8mk"),
			new OSMatch("it32", "128 32-bit", "t8mk"),
			new OSMatch("icp4", "16 PNG", null),
			new OSMatch("icp4", "16² PNG", null),
			new OSMatch("icp4", "16 32-bit", "s8mk"),
			new OSMatch("icp5", "32 PNG", null),
			new OSMatch("icp5", "32 32-bit", "l8mk"),
			new OSMatch("icp6", "48 PNG", null),
			new OSMatch("ic07", "128 PNG", null),
			new OSMatch("ic07", "128² PNG", null),
			new OSMatch("ic08", "256 PNG", null),
			new OSMatch("ic09", "512 PNG", null),
			new OSMatch("ic10", "1024² PNG", null),
			new OSMatch("ic10", "1024 PNG", null),
			new OSMatch("ic11", "32² PNG", null),
			new OSMatch("ic12", "64² PNG", null),
			new OSMatch("ic12", "64 PNG", null),
			new OSMatch("ic13", "256² PNG", null),
			new OSMatch("ic14", "512² PNG", null),
			new OSMatch("ic04", "16 PNG", null),
			new OSMatch("ic04", "16 32-bit", null),
			new OSMatch("ic05", "32² PNG", null),
			new OSMatch("ic05", "32 32-bit", null),
			new OSMatch("icsb", "18 PNG", null),
			new OSMatch("icsb", "18² PNG", null),
			new OSMatch("icsb", "18 32-bit", null),
			new OSMatch("icsB", "36² PNG", null),
			new OSMatch("icsB", "36 PNG", null),
			new OSMatch("sb24", "24 PNG", null),
			new OSMatch("sb24", "24² PNG", null),
			new OSMatch("SB24", "48² PNG", null),
		};
	
	static String[] OSMatch(String match) {
		for (OSMatch array : osMatch) {
			if (array.match.equals(match)) {
				String[] fs_type = {array.type, array.mask};
				return fs_type;
			}
		}
		return null;
	}
	
	public static String OSMask(String type) {
		for (OSMatch array : osMatch) {
			if (array.type.equals(type)) {
				return array.mask;
			}
		}
		return null;
	}
	
	/**
	 * Apple ICNS file writing routine.
	 * <p>
	 */
	IcnsHeader(IcnsFiles icns, DiskIcons image) throws IOException {
		img = new PngHeader();
		map = new MapHeader();
		if (image.getFiles() == null) return;
		
		/**
		 * Start by searching the available icons for the corresponding fs.type and fs.mask
		 */
		List<DiskIconsView> local = new ArrayList<DiskIconsView>();
		for (DiskIconsView fs: image.getFiles()) {
			if (fs.isIcon > 0) {	// PNG, BITMAP, APPLE, MASK, ARGB
				String[] fs_type = OSMatch(fs.layout);					// Search for fs.type and fs.mask in OSMatch according to fs.layout
				if (fs_type != null) {									// If found the result cannot be null
					fs.type = fs_type[0];
					fs.length += 8;
					local.add(fs);
					if (fs_type[1] != null) {							// If fs.mask is not null
						if (fs.isIcon == DiskIcons.ICON_BITMAP) {		// If fs.isIcon comes from bitmap (Windows)
							DiskIconsView fm = new DiskIconsView();		// creates a new DiskIconsView
							fm.isIcon = DiskIcons.ICON_BITMAP;
							fm.type = fs_type[1];
							fm.offset = fs.offset;
							fm.length = fs.length;
							fm.layout = fs.layout;
							fm.description = fs.description.replaceFirst("RGBA", "MASK");
							fs.description = fs.description.replaceFirst("RGBA", "ICON");
							local.add(fm);
						}
						else
						if (fs.isIcon == DiskIcons.ICON_APPLE) {		// If fs.isIcon comes from Apple
							for (DiskIconsView fm: image.getFiles()) {	// searches for the respective bitmap
								if (fm.type.equals(fs_type[1])) {
									fm.isIcon = DiskIcons.ICON_MASK;
									fm.length += 8;
									local.add(fm);
									break;
								}
							}
						}
					}
				}
			}
		}
		
		if (local.isEmpty()) return;
		
		RandomAccessFile from = image.getMedia();
		RandomAccessFile to = icns.getMedia();
		
		/**
		 * If you have more than one icon you create the Table of contents
		 */
		DiskIconsView es = new DiskIconsView();
		if (local.size() > 1) {
			es.isIcon		= DiskIcons.TABLE_OF_CONTENTS;
			es.length 		= local.size() * 8 + 8;
			es.type			= osType[0].type;
			es.description 	= osType[0].description;
			local.add(0, es);
		}
		
		/**
		 * Writes the header and possibly the Table of contents empty
		 */
		byte[] header = new byte[HEADER_SIZE];
		to.write(header);
		
		byte[] toc = new byte[8 * local.size()];
		if (local.size() > 1) {
			to.write(toc);
		}
		
		/**
		 * Then write the icons
		 */
		for (DiskIconsView fs: local) {
			if (fs.isIcon > 0) {	// PNG, BITMAP, APPLE, MASK, ARGB
				byte[] buffer = null;
				if (fs.isIcon == DiskIcons.ICON_BITMAP) {
					String function = fs.description.substring(Math.max(fs.description.length() - 4, 0));
					buffer = map.BitmapToApple(from, fs.offset, fs.length, function);
					fs.length = buffer.length + 8;
				}
				
				ByteBuffer tw = ByteBuffer.wrap(header).order(IcnsFiles.BYTE_ORDER);
				tw.put(fs.type.getBytes(StandardCharsets.US_ASCII));
				tw.putInt(fs.length);
				to.write(header);
				
				if (fs.isIcon == DiskIcons.ICON_PNG) {
					img.WriteImage(from, fs.offset, to, fs.length - 8);
				}
				else
				if (fs.isIcon == DiskIcons.ICON_BITMAP) {
					to.write(buffer);
				}
				else
				if (fs.isIcon == DiskIcons.ICON_APPLE || fs.isIcon == DiskIcons.ICON_MASK || fs.isIcon == DiskIcons.ICON_ARGB) {
					map.WriteImage(from, fs.offset, to, fs.length - 8);
				}
			}
		}
		
		/**
		 * Finally write the full header and Table of contents
		 */
		to.seek(0);
		ByteBuffer tw = ByteBuffer.wrap(header).order(IcnsFiles.BYTE_ORDER);
		tw.putInt(IcnsFiles.ICON_ICNS);
		tw.putInt((int) to.length());
		to.write(header);
		
		if (local.size() > 1) {
			tw = ByteBuffer.wrap(toc).order(IcnsFiles.BYTE_ORDER);
			for (DiskIconsView fs: local) {
				tw.put(fs.type.getBytes(StandardCharsets.US_ASCII));
				tw.putInt(fs.length);
			}
			to.write(toc);
		}
	}
	
	/**
	 * Apple ICNS file reading routine.
	 * <p>
	 */
	IcnsHeader(IcnsFiles icns, ByteBuffer in) throws IOException, WrongHeaderException {
		img = new PngHeader();
		map = new MapHeader();
				
		if (in.remaining() >= HEADER_SIZE) {
			in.order(IcnsFiles.BYTE_ORDER);
			
			signature			= in.getInt();
			fileLength			= in.getInt();
			
			if (signature == IcnsFiles.ICON_ICNS && fileLength == icns.getLength()) {
				
				int offset = HEADER_SIZE;
				ByteBuffer tr = null;
								
				while (offset < icns.getLength()) {
					tr = icns.readIcon(offset, 16).order(IcnsFiles.BYTE_ORDER);
					if (tr.remaining() < 8)
						throw new WrongHeaderException(getClass(), icns.getPath());
					tr.get(type);
					length = tr.getInt(4);
					if (length < 8 || offset + length > icns.getLength())
						throw new WrongHeaderException(getClass(), icns.getPath());
					
					DiskIconsView view = new DiskIconsView();
					view.isIcon = DiskIcons.NOT_AN_ICON;
					view.offset = offset + 8;
					view.length = length - 8;
					view.type = new String(type);
					view.description = OSType(view.type);
					
					tr.order(ByteOrder.BIG_ENDIAN);
					if (tr.limit() >= 16 && tr.getInt(8) == PngFiles.ICON_PGN && tr.getInt(12) == PngFiles.DOS_UNIX) { // %PNG....
						view.isIcon = DiskIcons.ICON_PNG;
						view.description = img.ImageHeader(icns, offset + 16, length - 8);
						view.layout = view.description;
					} 
					else 
					if (tr.limit() >= 16 && tr.getInt(12) == IcnsFiles.ICON_JPEG) { // jP: JPEG 2000
						view.description = Static.getInteger(view.description) + " JPEG 2000";
					} 
					else 
					if (view.type.equals("is32") || view.type.equals("il32") || view.type.equals("ih32") || view.type.equals("it32") 
							|| view.type.equals("icp4") || view.type.equals("icp5")) {  // APPLE: 24-bit RGB and possibly an 8-bit mask
						view.isIcon = DiskIcons.ICON_APPLE;
						view.description = Static.getInteger(view.description) + " 24-bit APPLE";
						view.layout = view.description.replaceFirst("24-bit APPLE", "32-bit");
					}
					else 
					if ((view.type.equals("ic04") || view.type.equals("ic05") || view.type.equals("icsb")) 
							&& tr.limit() >= 16 && tr.getInt(8) == IcnsFiles.ICON_ARGB) { // ARGB: 8-bit mask and 24-bit RGB
						view.isIcon = DiskIcons.ICON_ARGB;
						view.description = Static.getInteger(view.description) + " 32-bit ARGB";
						view.layout = view.description.replaceFirst(" ARGB", "");
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
