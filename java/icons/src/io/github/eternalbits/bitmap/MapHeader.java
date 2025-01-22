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
public class MapHeader {

	/**
	 * This layout is simply a way to call the other icons to manipulate the
	 *  associated BITMAP, and it has the variables as null.
	 * <p>
	 */	
	public MapHeader() {}
	
	/**
	 * Returns the 3 types of icons for Apple: ICON is based only on the 3 colors red,
	 *  green and blue applying a simple compression routine. MASK is based on copying
	 *  all items in the mask, without conversion. RGBA takes all the components including
	 *  the colors and the mask and passes it through the same compression routine.
	 * 
	 * @param from	Read access to RandomAccessFile.
	 * @param original	The reading position.
	 * @param size	Number of bytes to be read.
	 * @param function	ICON to read the RGB code, MASK to read the mask and RGBA to read everything.
	 * @return	The resulting bitmap.
	 */
	public byte[] BitmapToApple(RandomAccessFile from, int original, int size, String function) throws IOException {
		byte[] source = new byte[40];
		byte[] detail = new byte[0];
		from.seek(original);
		
		from.read(source);
		ByteBuffer tf = ByteBuffer.wrap(source).order(ByteOrder.LITTLE_ENDIAN);
		int size40	= tf.getInt(0);
		int width	= tf.getInt(4);
		short pixel	= tf.getShort(14);
		
		if (size40 == 40 && width <= 2048 && pixel == 32) {
		//	The size of each row is rounded up to a multiple of 4 bytes by padding.
			int diff = Static.roundUp(4 * width, 4);
			source = new byte[width * diff];
		//	Usually pixels are stored "bottom-up", starting in the lower left corner.
			for (int i = width * diff - diff; i >= 0; i -= diff)
				from.read(source, i, diff);
			
			if (function.equals("ICON")) {
				byte[] apple = new byte[Static.appleRound(3, width)];
				int posic = 0;
				for (int i = 0; i < 3; i++) {
					byte[] icon = new byte[width * width];
					PreparingForApple(source, icon, 2 - i);
					posic = EncodeRgbAndMask(icon, apple, posic);
				}
				int it32 = width == 128 ? 4 : 0;
				detail = new byte[it32 + posic];
				System.arraycopy(apple, 0, detail, it32, posic);
			}
			
			else
			if (function.equals("MASK")) {
				detail = new byte[width * width];
				PreparingForApple(source, detail, 3);
			}
			
			else {
				byte[] apple = new byte[Static.appleRound(4, width)];
				int posic = 0;
				for (int i = 0; i < 4; i++) {
					byte[] icon = new byte[width * width];
					PreparingForApple(source, icon, 3 - i);
					posic = EncodeRgbAndMask(icon, apple, posic);
				}
				detail = new byte[4 + posic];
				ByteBuffer tw = ByteBuffer.wrap(detail).order(ByteOrder.BIG_ENDIAN);
				System.arraycopy(apple, 0, detail, 4, posic);
				tw.putInt(0, 0x41524742);	// ARGB
			}

		}
		
		return detail;
	}
	
	/**
	 *  Copy the respective bytes from the source to the icon. While in the source the
	 *   bytes are distributed, like rgba, in the icon the bytes are grouped, like
	 *   red, green and blue.
	 * 
	 * @param source	Byte source, always RGBA.
	 * @param icon	Destination of the bytes.
	 * @param index	Color index: 0 for red, 1 for green, 2 for blue and 3 for transparency.
	 */
	void PreparingForApple(byte[] source, byte[] icon, int index) {
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
	 * @param apple	The resulting image following the same order.
	 * @param posic	The old position.
	 * @return	The new position.
	 */
	int EncodeRgbAndMask(byte[] icon, byte[] apple, int posic) {
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
			if (count != 0 && posic < apple.length) {
				apple[posic++] = (byte) (count - 1);
				for (int i = 0; i < count && posic < apple.length; )
					apple[posic++] = seq[i++];
			}
			if (index >= icon.length) 
				break;
			byte repeated = icon[index];
			count = 0;
			while (count <= 0x7F && index < icon.length && icon[index] == repeated) {
				index++;
				count++;
			}
			if (count >= 3 && posic + 1 < apple.length) {
				apple[posic++] = (byte) (0x80 + count - 3);
				apple[posic++] = repeated;
			} else {
			// There are less than 3 repeating bytes, drop the result
				index -= count;
			}
		}
		return posic;
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
