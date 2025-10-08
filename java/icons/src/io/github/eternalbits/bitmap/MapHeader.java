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

package io.github.eternalbits.bitmap;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.imageio.ImageIO;

import io.github.eternalbits.icons.Static;

/**
 * A bitmap is an array of bits that specify the color of each pixel in a rectangular
 *  array of pixels. Although many resolutions are found only a 256-bit representation
 *  with 4 colors is considered here: red, green, blue and transparency.
 * <p>
 */
public class MapHeader {

	/**
	 * This layout is simply a way to call the other icons to manipulate the
	 *  associated BITMAP, and it has the variables as null.
	 * <p>
	 */	
	public MapHeader() {}
	
	/**
	 * Write BufferedImage in PNG format to output RandomAccessFile and returns
	 *  the length of that BufferedImage.
	 * 
	 * @param image	An access to the BufferedImage.
	 * @param to	Write access to RandomAccessFile.
	 * @return	The length of the BufferedImage.
	 */
	public int writePng(BufferedImage image, RandomAccessFile to) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "png", baos);
		to.write(baos.toByteArray());
		return baos.size();
	}
	
	/**
	 * Writes the Bitmap. Note the reminiscence used by Windows XP.
	 * 
	 * @param image	An access to the BufferedImage.
	 * @param to	Write access to RandomAccessFile.
	 * @param power	The length of one side.
	 * @return	The length of the Bitmap.
	 */
	public int writeBitmap(BufferedImage image, RandomAccessFile to, int power) throws IOException {
		int length = 40 + 4 * power * power + power * Static.roundUp(power / 8, 4);
		byte[] buffer = Static.toBitmap(image, power);
		to.write(headerForIcon(length, power));
		int diff = 4 * power;
		for (int i = power * diff - diff; i >= 0; i -= diff)
			to.write(buffer, i, diff);
		int padd = Static.roundUp(power / 8, 4);
		byte[] trailer = trailerForIcon(buffer, power);
		for (int i = power * padd - padd; i >= 0; i -= padd)
			to.write(trailer, i, padd);
		return length;
	}
	
	/**
	 * Reads the Bitmap. In this case a header of 108 bytes in length
	 *  is used to pay attention to the mask.
	 * 
	 * @param from	Read access to RandomAccessFile.
	 * @param original	The reading position.
	 * @param size	Number of bytes to be passed.
	 * @param power	The length of one side.
	 * @return	Image with an accessible buffer of image data.
	 */
	public BufferedImage createBitmap(RandomAccessFile from, int original, int size, int power) throws IOException {
		int header = 14 + Static.BITMAP_HEADER;			// It uses 108 bytes instead of 40 because this allows the mask to be used
		int length = header + 4 * power * power;		// length of the image as a bitmap
		byte[] buffer = new byte[length];				// buffer the image as a bitmap
		from.seek(original + 40);
		from.read(buffer, header, 4 * power * power);	// read the image directly into the buffer
		Static.headerForBitmap(buffer, length, power);	// finalizes the bitmap
		return ImageIO.read(new ByteArrayInputStream(buffer));
	}

	/**
	 * Constructs the header from a bitmap icon. Note the multiplication of the
	 *  vertical offset by 2 and a bitmap remnant that was used by Windows XP.
	 * 
	 * @param length	The length of the header plus the length of the bit sequence.
	 * @param power	The length of one side.
	 * @return	The resulting header bitmap.
	 */
	private byte[] headerForIcon(int length, int power) {
		byte[] header = new byte[40];
		ByteBuffer tw = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
		tw.putInt	(40);
		tw.putInt	(power);
		tw.putInt	(power * 2);
		tw.putShort	((short) 1);
		tw.putShort	((short) 32);
		tw.putInt	(0);
		tw.putInt	(length - 40);
		tw.putInt	(2835);
		tw.putInt	(2835);
		tw.putInt	(0);
		tw.putInt	(0);
		return header;
	}
	
	/**
	 * Although there are currently 256 pixels to distinguish the transparency color,
	 *  bitmaps until Windows XP used a smaller set of colors. These colors are not
	 *  currently used, but the icon only works with this transparency. In Windows XP
	 *  the only transparency used are this bitmap generated here.
	 * 
	 * @param source	The default image with 4 colors specifying the RGBA.
	 * @param power	The length of one side.
	 * @return	The resulting bitmap.
	 */
	private byte[] trailerForIcon(byte[] source, int power) {
		int padd = Static.roundUp(power / 8, 4);
		byte[] trailer = new byte[power * padd];
		ByteBuffer tw = ByteBuffer.wrap(trailer).order(ByteOrder.LITTLE_ENDIAN);
		ByteBuffer tr = ByteBuffer.wrap(source).order(ByteOrder.LITTLE_ENDIAN);
		for (int y = 0; y < power; y++) {
			for (int x = 0; x < padd; x++) {
				byte maskValue = 0;
				for (int bit = 0; bit < 8; bit++) {
					if ((x * 8) + bit < power) {
						int srcPixel = tr.getInt(4 * ((y * power) + (x * 8) + bit));
						if ((srcPixel >>> 24) < 128)
							maskValue |= (1 << (7 - bit));
					}
				}
				tw.put((y * padd) + x, maskValue);
			}
		}
		return trailer;
	}
	
}
