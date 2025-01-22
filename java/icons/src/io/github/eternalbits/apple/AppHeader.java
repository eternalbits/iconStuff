/*
 * Copyright 2025 Rui Baptista
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.eternalbits.apple;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
	 * Constructs the bitmap from a grouped base of red, green and blue, without
	 *  counting transparency.
	 * 
	 * @param from	Read access to RandomAccessFile.
	 * @param original	The reading position.
	 * @param to	Write access to RandomAccessFile.
	 * @param size	Number of bytes to be passed.
	 * @param power	The length of one side.
	 * @param source	The bitmap source in reverse order.
	 * @return	The length of the bitmap.
	 */
	public int AppleToBitmap(RandomAccessFile from, int original, RandomAccessFile to, 
			int size, int power, byte[] source) throws IOException {
		int it32 = power == 128 ? 4 : 0;			// it32 data always starts with a header of four zero-bytes
		from.seek(original + it32);
		byte[] buffer = new byte[size - it32];
		from.read(buffer);
		byte[] icon = new byte[3 * power * power];
		DecodeRgbAndMask(buffer, icon, 0);
		for (int i = 0; i < 3; i++)
			PreparingForBitmap(source, icon, 2 - i, i * power * power);
		return BitmapForIcon(to, power, source);
	}
	
	/**
	 * Adds to the bitmap from a transparency base, without red, green and blue.
	 * 
	 * @param from	Read access to RandomAccessFile.
	 * @param original	The reading position.
	 * @param size	Number of bytes to be passed.
	 * @param source	The bitmap source in reverse order.
	 * @return	The source after applying the mask.
	 */
	public byte[] MaskToBitmap(RandomAccessFile from, int original, int size, 
			byte[] source) throws IOException {
		byte[] icon = new byte[size];
		from.seek(original);
		from.read(icon);
		PreparingForBitmap(source, icon, 3, 0);
		return source;
	}
	
	/**
	 * Constructs the bitmap from a grouped base of transparency, red, green and blue.
	 * 
	 * @param from	Read access to RandomAccessFile.
	 * @param original	The reading position.
	 * @param to	Write access to RandomAccessFile.
	 * @param size	Number of bytes to be passed.
	 * @param power	The length of one side.
	 * @param source	The bitmap source in reverse order.
	 * @return	The length of the bitmap.
	 */
	public int ArgbToBitmap(RandomAccessFile from, int original, RandomAccessFile to, 
			int size, int power, byte[] source) throws IOException {
		from.seek(original + 4);
		byte[] buffer = new byte[size];
		from.read(buffer);
		byte[] icon = new byte[4 * power * power];
		DecodeRgbAndMask(buffer, icon, 0);
		for (int i = 0; i < 4; i++)
			PreparingForBitmap(source, icon, 3 - i, i * power * power);
		return BitmapForIcon(to, power, source);
	}
	
	/**
	 * Creates the bitmap. Typically, pixels are stored "bottom to top", starting
	 *  in the bottom left corner.
	 * 
	 * @param to	Write access to RandomAccessFile.
	 * @param power	The length of one side.
	 * @param source	The bitmap source in reverse order.
	 * @return	The length of the bitmap.
	 */
	int BitmapForIcon(RandomAccessFile to, int power, byte[] source) throws IOException {
		byte[] header = HeaderForIcon(power);
		to.write(header);
		int diff = Static.roundUp(4 * power, 4);
		for (int i = power * diff - diff; i >= 0; i -= diff)
			to.write(source, i, diff);
		byte[] trailer = TrailerForIcon(source, power);
		int padd = Static.roundUp(power / 8, 4);
		for (int i = power * padd - padd; i >= 0; i -= padd)
			to.write(trailer, i, padd);
		return header.length + source.length + trailer.length;
	}
	
	/**
	 * Constructs the header from a bitmap icon. Note the multiplication of the
	 *  vertical offset by 2 and a bitmap remnant that was used by Windows XP.
	 * 
	 * @param power	The length of one side.
	 * @return	The resulting header bitmap.
	 */
	byte[] HeaderForIcon(int power) {
		byte[] header = new byte[40];
		ByteBuffer tw = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
		tw.putInt(40);
		tw.putInt(power);
		tw.putInt(power * 2);
		tw.putShort((short) 1);
		tw.putShort((short) 32);
		tw.putInt(0);
		tw.putInt(4 * power * power + Static.roundUp(power / 8, 4) * power);
		tw.putInt(2835);
		tw.putInt(2835);
		tw.putInt(0);
		tw.putInt(0);
		return header;
	}
	
	/**
	 * Copy the respective bytes from the icon to the source. While in the source the
	 *  bytes are distributed, like rgba, in the icon the bytes are grouped, like
	 *  red, green and blue.
	 * 
	 * @param source	Byte source, always RGBA.
	 * @param icon	Origin of the bytes.
	 * @param index	Color index: 0 for red, 1 for green, 2 for blue and 3 for transparency.
	 * @param posic	Position in the icon table.
	 */
	void PreparingForBitmap(byte[] source, byte[] icon, int index, int posic) {
		for (int i = index, n = posic; i < source.length; i += 4, n++) 
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
	 * @return	The return position.
	 */
	int DecodeRgbAndMask(byte[] buffer, byte[] icon, int index) {	
		int posic = 0;
		while (index < buffer.length && posic < icon.length) {
			int current = 0xFF & buffer[index];
			if (current < 0x80) {
				int count = current + 1;
				if (index + count >= buffer.length) 
					break;
				for (int i = index + 1; i < index + count + 1 && posic < icon.length; i++)
					icon[posic++] = buffer[i];
				index += count + 1;
			} else {
				int count = current - 0x80 + 3;
				if (index + 1 >= buffer.length) 
					break;
				byte repeated = (byte) (0xFF & buffer[index + 1]);
				for (int i = 0; i < count && posic < icon.length; i++) 
					icon[posic++] = repeated;
				index += 2;
			}
		}
		return index;
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
	byte[] TrailerForIcon(byte[] source, int power) {
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

	/**
	 * This routine is limited to passing bytes from one side to the other assuming a maximum of 16384 bytes.
	 * 
	 * @param from	Read access to RandomAccessFile.
	 * @param original	The reading position.
	 * @param to	Write access to RandomAccessFile.
	 * @param size	Number of bytes to be passed.
	 */
	public void WriteImage(RandomAccessFile from, int original, RandomAccessFile to, int size) throws IOException {
		from.seek(original);
		int get = 0, go = size;
		while (get < size) {
			byte[] detail = new byte[go < 0X4000 ? go : 0X4000];
			from.read(detail);
			to.write(detail);
			get += detail.length;
			go -= detail.length;
		}
	}

}
