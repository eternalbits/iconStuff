/*
 * Copyright 2026 Rui Baptista
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

package io.github.eternalbits.jp2;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.ImageIcon;

import io.github.eternalbits.disk.DiskIcons;
import io.github.eternalbits.disk.DiskIconsView;
import io.github.eternalbits.disk.DiskImageView;
import io.github.eternalbits.disk.WrongHeaderException;
import io.github.eternalbits.icons.Static;
import io.github.eternalbits.icons.gui.FrontEnd;
import io.github.eternalbits.png.PngFiles;

/**
 * JPEG 2000 File Routine
 * <p>
 */
public
class Jp2Header {
	static final int HEADER_SIZE = 12;
	private static final int ICON_LENGTH = 0x0000000C;		// "...."
	private static final int ICON_BOX_TYPE = 0x6A502020;	// "jP  "
	private static final int ICON_DAMAGE = 0x0D0A870A;		// "...."

	List<DiskIconsView> disk = new ArrayList<DiskIconsView>();

	/**
	 * JPEG 2000 file writing routine.
	 * 
	 * @param jp2	JP2 file access.
	 * @param in	Access to DiskIconsView which is a preview of another result.
	 */
	Jp2Header(Jp2Files jp2, DiskIcons image, String icon) throws IOException, WrongHeaderException {
		if (image.getFiles() == null) return;
		if (Static.delimiterIcon(icon, image))
			throw new WrongHeaderException(getClass(), jp2.getPath());
		
		/**
		 * Start by choosing the largest PNG
		 */
		DiskIconsView es = null;
		int i = 0, m = 0;
		for (DiskIconsView fs: image.getFiles()) {
			if (fs.isIcon == DiskIcons.ICON_JP2 && fs.forIcon != -1) {	// JP2
				if (fs.size == 0)
					fs.size = Static.getInteger(fs.layout);
				i = Static.getInteger(fs.description);
				if (i > m) { es = fs; m = i; }
			}
		}
		
		/**
		 * If you find any, write them down
		 */
		if (es != null) {
			RandomAccessFile to = jp2.getMedia();
			jp2.done = true;
			to.setLength(0);
			to.write(es.jpeg2);
			es.length = es.jpeg2.length;
		}
	}
	
	/**
	 * JPEG 2000 file reading routine.
	 * 
	 * @param jp2	JP2 file access.
	 * @param in	Access to the first 12 characters.
	 */
	public Jp2Header(Jp2Files jp2, ByteBuffer in, String jpeg) throws IOException, WrongHeaderException {
		
		if (in.remaining() >= HEADER_SIZE) {
			in.order(Jp2Files.BYTE_ORDER);
			
			int length			= in.getInt();
			int boxType			= in.getInt();
			int damage			= in.getInt();
			
			if (length == ICON_LENGTH && boxType == ICON_BOX_TYPE && damage == ICON_DAMAGE) { // %PNG....
				DiskIconsView view = new DiskIconsView();
				view.offset = 0;
				view.length = (int) jp2.getLength();
				view.type = jp2.getType();
				JpegToPng(jp2, view, jpeg);
				disk.add(view);
				return;
			}
		}
		
		throw new WrongHeaderException(getClass(), jp2.getPath());
	}
	
	/**
	 * This layout is simply a way to call the other icons to manipulate the
	 *  associated JP2, and it has the variables as null.
	 * <p>
	 */	
	public Jp2Header() {}
	
	/**
	 * This routine checks if JP2 is true and can be called by the 2 routines.
	 * 
	 * @param img	Access to each of the 2 routines: ICNS and JP2.
	 * @param offset	The reading position.
	 * @param size	Number of bytes to be passed.
	 * @return	String representing what was read, something like "256 JPEG 2000".
	 */
	public String ImageHeader(DiskIcons img, int offset, int size) throws IOException, WrongHeaderException {
		
		int[]	dim = {0, 0};			// height, width
		int		length = 0;				// Length of chunk, in bytes, msb first
		int		type = 0;				// Chunk type/name of chunk
		
		long previous = offset;
		ByteBuffer tr = img.readIcon(offset, 4).order(Jp2Files.BYTE_ORDER);
		
		while (offset - previous < size) {
			offset += tr.remaining();
			if (tr.remaining() != 4)
				throw new WrongHeaderException(getClass(), img.getPath());
			length = tr.getInt(0);
		
			tr = img.readIcon(offset, length - 4).order(Jp2Files.BYTE_ORDER);
			offset += tr.remaining();
			if (tr.remaining() != length - 4)
				throw new WrongHeaderException(getClass(), img.getPath());
			type = tr.getInt(0);
			
			// In JP2 format, the ihdr must be the first box contained within the jp2h
			if (type == 0x6A703268 && tr.getInt(8) == 0x69686472) {	// jp2h, ihdr
				dim[0] = tr.getInt(16);
				dim[1] = tr.getInt(12);
				break;
			}
			
			tr = img.readIcon(offset, 4).order(Jp2Files.BYTE_ORDER);
		}
		
		String dimension = String.valueOf(dim[0]);
		if (dim[0] != dim[1])
			dimension += "x" + String.valueOf(dim[1]);
		return dimension + " JPEG 2000";
	}
	
	/**
	 * A program that displays a question mark or converts a JPEG 2000 image to PNG.
	 * 
	 * @param img	Access to each of the 2 routines: ICNS and JP2.
	 * @param view	A read and write view of a {@link DiskImageView}.
	 * @param jpeg	Program that converts JPEG 2000 to PNG. If the program is null, an image
	 *  with a question mark will be displayed, as Java does not recognize a standard reading 
	 *  routine. If filled in, only a JPEG 2000 to PNG conversion routine, published by 
	 *  <a href="https://www.xnview.com/en/nconvert">XnSoft</a>, will be recognized.
	 */
	public void JpegToPng(DiskIcons img, DiskIconsView view, String jpeg) throws IOException, WrongHeaderException {
		view.isIcon = DiskIcons.ICON_JP2;
		view.description = ImageHeader(img, view.offset, view.length);
		view.layout = view.description.replaceFirst("JPEG 2000", "JP2");
		boolean done = false;
		if (jpeg != null && new File(jpeg).canExecute()) {
			String name = Static.getWorkingDirectory().getPath() + File.separator + UUID.randomUUID().toString() + ".png";
			if (!new File(name).exists()) {
				try {
					String width = Static.getSize(view.layout), height = width;
					if (height.contains("x")) height = height.split("x")[1];
					if (width.contains("x")) width = width.split("x")[0];
					Process proc = new ProcessBuilder(jpeg, "-out", "png", "-clevel", "9", 
							"-thumb", width, height, "-o", name, img.getPath()).start();
					proc.waitFor();
					
					File file = new File(name);
					DiskIcons png = new PngFiles(file, "r");
					DiskIconsView ps = png.getView().fileIcons.get(0);
					view.isIcon		= ps.isIcon;
					view.description= ps.description;
					view.layout		= ps.layout;
					view.image		= ps.image;
					png.close();
					
					file.delete();
					done = true;
				} catch (IOException | InterruptedException | WrongHeaderException e) {
					e.printStackTrace();
				}
			}
		}
		if (!done) {
			ImageIcon icon = new ImageIcon(FrontEnd.getResource("jpeg2.png"));
			view.image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
			view.image.getGraphics().drawImage(icon.getImage(), 0, 0, null);
			view.jpeg2 = createJp2(img, view.offset, view.length);
		}
	}
	
	/**
	 * This routine is limited to reading a JP2 file and saving this image
	 *  while maintaining all the characteristics.
	 * 
	 * @param img	Access to each of the 2 routines: ICNS and PNG.
	 * @param offset	The reading position.
	 * @param size	Number of bytes to be passed.
	 * @return	Byte array with an accessible buffer of image data.
	 */
	public byte[] createJp2(DiskIcons jp2, int offset, int size) throws IOException {
		jp2.getMedia().seek(offset);
		byte[] data = new byte[size];
		jp2.getMedia().read(data);
		return data;
	}
}
