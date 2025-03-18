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

package io.github.eternalbits.png;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

import javax.imageio.ImageIO;

import io.github.eternalbits.disk.DiskIcons;
import io.github.eternalbits.disk.DiskIconsView;
import io.github.eternalbits.disk.WrongHeaderException;
import io.github.eternalbits.icons.Static;

/**
 * PNG File Routine
 * <p>
 */
public
class PngHeader {
	static final int HEADER_SIZE = 8;
	
	List<DiskIconsView> disk = new ArrayList<DiskIconsView>();

	int		signature;				// Magic literal, must be "%PNG" (0x89, 0x50, 0x4E, 0x47)
	int		dosUnix;				// A style line ending, must be 0x0D, 0x0A, 0x1A, 0x0A
	
	/**
	 * PNG file writing routine.
	 * 
	 * @param png	PNG file access.
	 * @param in	Access to DiskIconsView which is a preview of another result.
	 */
	PngHeader(PngFiles png, DiskIcons image, String icon) throws IOException, WrongHeaderException {
		if (image.getFiles() == null) return;
		if (Static.delimiterIcon(icon, image))
			throw new WrongHeaderException(getClass(), png.getPath());
		
		/**
		 * Start by choosing the largest PNG
		 */
		DiskIconsView es = null;
		int i = 0, m = 0;
		for (DiskIconsView fs: image.getFiles()) {
			if (fs.isIcon > 0 && fs.forIcon != -1) {	// PNG, BITMAP, APPLE, ARGB
				i = Static.getInteger(fs.description);
				if (i > m) { es = fs; m = i; }
			}
		}
		
		/**
		 * If you find any, write them down
		 */
		if (es != null) {
			RandomAccessFile to = png.getMedia();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();					// Passing bytes from a saved image to PNG
			ImageIO.write(es.image, "png", baos);
			to.write(baos.toByteArray());
			es.length = baos.size();
		}
	}
	
	/**
	 * PNG file reading routine.
	 * 
	 * @param png	PNG file access.
	 * @param in	Access to the first 8 characters.
	 */
	PngHeader(PngFiles png, ByteBuffer in) throws IOException, WrongHeaderException {
		
		if (in.remaining() >= HEADER_SIZE) {
			in.order(PngFiles.BYTE_ORDER);
			
			signature			= in.getInt();
			dosUnix				= in.getInt();
			
			if (signature == PngFiles.ICON_PGN && dosUnix == PngFiles.DOS_UNIX) { // %PNG....
				DiskIconsView view = new DiskIconsView();
				view.isIcon = DiskIcons.ICON_PNG;
				view.offset = 0;
				view.length = (int) png.getLength();
				view.type = png.getType();
				view.description = this.ImageHeader(png, HEADER_SIZE, view.length);
				view.image = this.createPng(png, view.offset, view.length);
				view.layout = view.description;
				disk.add(view);
				return;
			}
		}
		
		throw new WrongHeaderException(getClass(), png.getPath());
	}
		
	/**
	 * This layout is simply a way to call the other icons to manipulate the
	 *  associated PNG, and it has the variables as null.
	 * <p>
	 */	
	public PngHeader() {}
	
	/**
	 * This routine checks if PNG is true and can be called by the 3 routines.
	 * 
	 * @param img	Access to each of the 3 routines: ICO, ICNS and PNG.
	 * @param offset	The reading position.
	 * @param size	Number of bytes to be passed.
	 * @return	String representing what was read, something like "256 PNG".
	 */
	public String ImageHeader(DiskIcons img, int offset, int size) throws IOException, WrongHeaderException {
		
		int[]	dim = {0, 0, 0, 0};		// width, height, ppu X, ppu Y
		int		length = 0;				// Length of chunk, in bytes, msb first
		int		type = 0;				// Chunk type/name of chunk
		
		CRC32 crc = new CRC32();				
		long previous = offset;
		ByteBuffer tr = img.readIcon(offset, 4).order(PngFiles.BYTE_ORDER);
		
		while (type != 0x49454E44) {	// IEND
			offset += tr.remaining();
			if (tr.remaining() != 4)
				throw new WrongHeaderException(getClass(), img.getPath());
			length = tr.getInt();
			
			tr = img.readIcon(offset, length + 4).order(PngFiles.BYTE_ORDER);
			offset += tr.remaining();
			if (tr.remaining() != length + 4)
				throw new WrongHeaderException(getClass(), img.getPath());
			type = tr.getInt(0);
			
			if (type == 0x49484452) {	// IHDR
				dim[0] = tr.getInt(4);
				dim[1] = tr.getInt(8);
			} else 
			if (type == 0x70485973) {	// pHYs
				dim[2] = tr.getInt(4);
				dim[3] = tr.getInt(8);
			}
			
			crc.reset();
			crc.update(tr);
			
			tr = img.readIcon(offset, 4 + 4).order(PngFiles.BYTE_ORDER);			
			if (tr.remaining() < 4 || (int)crc.getValue() != tr.getInt())
				throw new WrongHeaderException(getClass(), img.getPath());
			offset += 4;
		}
		
		if (offset - previous != size - 8)
			throw new WrongHeaderException(DiskIcons.class, img.getPath());
		
		String dimension = String.valueOf(dim[0]);
		if (dim[0] != dim[1])
			dimension += "x" + String.valueOf(dim[1]);
		if (dim[2] == dim[3] && dim[3] == 5669)
			dimension += "²";
		return dimension + " PNG";
	}
	
	/**
	 * This routine is limited to reading a PNG file and saving this image
	 *  while maintaining all the characteristics.
	 * 
	 * @param img	Access to each of the 3 routines: ICO, ICNS and PNG.
	 * @param offset	The reading position.
	 * @param size	Number of bytes to be passed.
	 * @return	Image with an accessible buffer of image data.
	 */
	public BufferedImage createPng(DiskIcons img, int offset, int size) throws IOException {
		img.getMedia().seek(offset);
		byte[] data = new byte[size];
		img.getMedia().read(data);
		return ImageIO.read(new ByteArrayInputStream(data));
	}
	
	/**
	 * This routine is limited to passing PNG bytes from one side to the other.
	 * 
	 * @param from	Read access to RandomAccessFile.
	 * @param original	The reading position.
	 * @param to	RandomAccessFile write access.
	 * @param size	Number of bytes to be passed.
	 */
	public void writeImage(RandomAccessFile from, int original, RandomAccessFile to, int size) throws IOException {
		byte[] buffer = new byte[8];
		ByteBuffer tr = null;
		from.seek(original);
		
		from.read(buffer);
		to.write(buffer);
		int get = 8;
		
		while (get < size) {
			from.read(buffer);
			to.write(buffer);
			
			tr = ByteBuffer.wrap(buffer).order(PngFiles.BYTE_ORDER);
			byte[] detail = new byte[tr.getInt(0) + 4];
			get += 8 + tr.getInt(0) + 4;
			from.read(detail);
			to.write(detail);
		}
	}
	
}
