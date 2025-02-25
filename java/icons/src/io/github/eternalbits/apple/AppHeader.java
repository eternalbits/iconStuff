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

package io.github.eternalbits.apple;

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
public class AppHeader {

	/**
	 * This layout is simply a way to call the other icons to manipulate the
	 *  associated APPLE and ARGB, and it has the variables as null.
	 * <p>
	 */	
	public AppHeader() {}
	
	/**
	 * Returns the BufferedImage in PNG format in bytes format.
	 * 
	 * @param image	An access to the BufferedImage.
	 * @return	The PNG format in bytes format.
	 */
	public byte[] writePng(BufferedImage image) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "png", baos);
		return baos.toByteArray();
	}
	
	/**
	 * Returns Apple format in bytes format.
	 * 
	 * @param image	An access to the BufferedImage.
	 * @param power	The length of one side.
	 * @return	The Apple format in bytes format.
	 */
	public byte[] writeApple(BufferedImage image, int power) throws IOException {
		byte[] apple = new byte[Static.appleRound(3, power)];
		byte[] buffer = Static.toBitmap(image, power);
		int posic = 0;
		for (int i = 0; i < 3; i++) {
			byte[] icon = new byte[power * power];
			preparingForApple(buffer, icon, 2 - i);
			posic = encodeRgbAndMask(icon, apple, posic);
		}
		int it32 = power == 128 ? 4 : 0;
		byte[] detail = new byte[it32 + posic];
		System.arraycopy(apple, 0, detail, it32, posic);
		return detail;
	}
	
	/**
	 * Returns Mask format in bytes format.
	 * 
	 * @param image	An access to the BufferedImage.
	 * @param power	The length of one side.
	 * @return	The Mask format in bytes format.
	 */
	public byte[] writeMask(BufferedImage image, int power) throws IOException {
		byte[] buffer = Static.toBitmap(image, power);
		byte[] detail = new byte[power * power];
		preparingForApple(buffer, detail, 3);
		return detail;
	}
	
	/**
	 * Returns ARGB format in bytes format.
	 * 
	 * @param image	An access to the BufferedImage.
	 * @param power	The length of one side.
	 * @return	The ARGB format in bytes format.
	 */
	public byte[] writeArgb(BufferedImage image, int power) throws IOException {
		byte[] apple = new byte[Static.appleRound(4, power)];
		byte[] buffer = Static.toBitmap(image, power);
		int posic = 0;
		for (int i = 0; i < 4; i++) {
			byte[] icon = new byte[power * power];
			preparingForApple(buffer, icon, 3 - i);
			posic = encodeRgbAndMask(icon, apple, posic);
		}
		byte[] detail = new byte[4 + posic];
		ByteBuffer tw = ByteBuffer.wrap(detail).order(ByteOrder.BIG_ENDIAN);
		System.arraycopy(apple, 0, detail, 4, posic);
		tw.putInt(0, 0x41524742);	// ARGB
		return detail;
	}
	
	/**
	 *  Copy the respective bytes from the source to the icon. While in the source the
	 *   bytes are distributed, like RGBA, in the icon the bytes are grouped, like
	 *   red, green and blue.
	 * 
	 * @param source	Byte source, always RGBA.
	 * @param icon	Destination of the bytes.
	 * @param index	Color index: 0 for red, 1 for green, 2 for blue and 3 for transparency.
	 */
	private void preparingForApple(byte[] source, byte[] icon, int index) {
		for (int i = index, n = 0; n < icon.length; i += 4, n++) 
			icon[n] = source[i];
	}
	
	/**
	 *  This directive follows a very linear order: it is 1 byte up to 0x7F followed
	 *   by the number of bytes to be repeated or 1 byte greater than or equal to 0x80
	 *   which, decreased by 3, is followed by 1 byte that comes immediately after.
	 *   This usually equates to a smaller percentage.
	 * 
	 * @param icon	The default image that specifies 3 colors (RGB) or 4 colors (ARGB).
	 * @param bytes	The resulting image following the same order.
	 * @param posic	The old position.
	 * @return	The new position.
	 */
	private int encodeRgbAndMask(byte[] icon, byte[] bytes, int posic) {
		byte[] seq = new byte[0x80];
		int index = 0;
		while (index < icon.length) {
			int count = 0;
			while (count <= 0x7F && index < icon.length) {
				if (index + 2 < icon.length && icon[index] == icon[index + 1] && icon[index] == icon[index + 2]) 
					break;
				seq[count] = icon[index];
				index++;
				count++;
			}
			if (count != 0 && posic < bytes.length) {
				bytes[posic++] = (byte) (count - 1);
				for (int i = 0; i < count && posic < bytes.length; )
					bytes[posic++] = seq[i++];
			}
			if (index >= icon.length) 
				break;
			byte repeated = icon[index];
			count = 0;
			while (count <= 0x7F && index < icon.length && icon[index] == repeated) {
				index++;
				count++;
			}
			if (count >= 3 && posic + 1 < bytes.length) {
				bytes[posic++] = (byte) (0x80 + count - 3);
				bytes[posic++] = repeated;
			} else {
			// There are less than 3 repeating bytes, drop the result
				index -= count;
			}
		}
		return posic;
	}
	
	/**
	 * Reads the ARGB. In this case a header of 108 bytes in length
	 *  is used to pay attention to the mask.
	 * 
	 * @param from	Read access to RandomAccessFile.
	 * @param original	The reading position.
	 * @param size	Number of bytes to be passed.
	 * @param power	The length of one side.
	 * @return	Image with an accessible buffer of image data.
	 */
	public BufferedImage createArgb(RandomAccessFile from, int original, int size, int power) throws IOException {
		int header = 14 + Static.BITMAP_HEADER;			// It uses 108 bytes instead of 40 because this allows the mask to be used
		int length = header + 4 * power * power;		// length of the image as a bitmap
		byte[] bytes = new byte[4 * power * power]; 	// image buffer after expanded
		byte[] icon = new byte[size - 4];				// the 32-bit ARGB of the image while it is compressed
		byte[] buffer = new byte[length];				// buffer the image as a bitmap
		from.seek(original + 4);			
		from.read(icon);
		decodeRgbAndMask(icon, bytes, 0);				// add the expanded 32-bit ARGB to it
		for (int i = 0; i < 4; i++)						// finally prepare the bitmap
			preparingForBitmap(buffer, bytes, header + 3 - i, i * power * power, power);
		Static.headerForBitmap(buffer, length, power);	// finalizes the bitmap
		return ImageIO.read(new ByteArrayInputStream(buffer));
	}
	
	/**
	 * Reads the Apple and the Mask. This is read at the end because Apple
	 *  and the Mask can come in any order.
	 * 
	 * @param from	Read access to RandomAccessFile.
	 * @param original	The reading position for 24-bit RGB.
	 * @param size	Number of bytes to be passed for 24-bit RGB.
	 * @param duplicate	The reading position for mask.
	 * @param mask	Number of bytes to be passed for mask.
	 * @param power	The length of one side.
	 * @return	Image with an accessible buffer of image data.
	 */
	public BufferedImage createApple(RandomAccessFile from, int original, int size, int duplicate, int mask, int power) throws IOException {
		int header = 14 + Static.BITMAP_HEADER;			// It uses 108 bytes instead of 40 because this allows the mask to be used
		int it32 = power == 128 ? 4 : 0;				// it32 data always starts with a header of four zero-bytes
		int length = header + 4 * power * power;		// length of the image as a bitmap
		byte[] bytes = new byte[4 * power * power];		// image buffer after expanded
		byte[] icon = new byte[size - it32];			// the 24-bit RGB portion of the image while it is compressed
		byte[] buffer = new byte[length];				// buffer the image as a bitmap
		from.seek(original + it32);
		from.read(icon);
		from.seek(duplicate);
		from.read(bytes, 0, mask);						// read the image bitmap directly into the bytes
		decodeRgbAndMask(icon, bytes, mask);			// then add the expanded 24-bit RGB to it
		for (int i = 0; i < 4; i++)						// finally prepare the bitmap
			preparingForBitmap(buffer, bytes, header + 3 - i, i * power * power, power);
		Static.headerForBitmap(buffer, length, power);	// finalizes the bitmap
		return ImageIO.read(new ByteArrayInputStream(buffer));
	}
	
	/**
	 * Copy the respective bytes from the icon to the source. While in the source the
	 *  bytes are distributed, like rgba, in the icon the bytes are grouped, like
	 *  red, green and blue. Also note that while on MacOS it is "top to bottom", on 
	 *  Windows pixels are stored "bottom to top", starting in the bottom left corner.
	 * 
	 * @param source	Byte source, always RGBA.
	 * @param icon	Origin of the bytes.
	 * @param index	Color index: 0 for transparency, 1 for red, 2 for green and 3 for blue.
	 * @param posic	Position in the icon table.
	 * @param power	The length of one side.
	 */
	private void preparingForBitmap(byte[] source, byte[] icon, int index, int posic, int power) {
		int padd = 4 * power;
		for (int j = power * padd - padd,  m = 0; j >= 0; j -= padd, m += power)
			for (int i = index + j, n = posic + m; i < index + j + padd; i += 4, n++) 
				source[i] = icon[n];
	}
	
	/**
	 * This directive follows a very linear order: it is 1 byte up to 0x7F followed
	 *  by the number of bytes to be repeated or 1 byte greater than or equal to 0x80
	 *  which, decreased by 3, is followed by 1 byte that comes immediately after.
	 *  This usually equates to a smaller percentage.
	 * 
	 * @param buffer	The default image that specifies 3 colors (RGB) or 4 colors (ARGB).
	 * @param icon	The resulting image following the same order.
	 * @param posic	The starting position.
	 * @return	The return position.
	 */
	private int decodeRgbAndMask(byte[] icon, byte[] bytes, int posic) {	
		int index = 0;
		while (index < icon.length && posic < bytes.length) {
			int current = 0xFF & icon[index];
			if (current < 0x80) {
				int count = current + 1;
				if (index + count >= icon.length) 
					break;
				for (int i = index + 1; i < index + count + 1 && posic < bytes.length; i++)
					bytes[posic++] = icon[i];
				index += count + 1;
			} else {
				int count = current - 0x80 + 3;
				if (index + 1 >= icon.length) 
					break;
				byte repeated = (byte) (0xFF & icon[index + 1]);
				for (int i = 0; i < count && posic < bytes.length; i++) 
					bytes[posic++] = repeated;
				index += 2;
			}
		}
		return posic;
	}

}
