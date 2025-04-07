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

package io.github.eternalbits.icons;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import io.github.eternalbits.disk.DiskIcons;
import io.github.eternalbits.disk.DiskIconsView;

/**
 * Utility static functions for Icons.
 */
public class Static {
	public static int BITMAP_HEADER = 108;

	/**
	 * A PNG image with a DataBuffer that can be represented by an array of bytes or
	 *  integers. I honestly don't know how the difference is made, I just know that 
	 *  the result is the same in both encodings.
	 * 
	 * @param image	A PNG image with a DataBuffer.
	 * @param power	The length of one side.
	 * @return	A bitmap image within a buffer.
	 */
	public static byte[] toBitmap(BufferedImage image, int power) throws IOException {
		byte[] buffer = new byte[4 * power * power];
		DataBuffer data = image.getRaster().getDataBuffer();
		if (data.getDataType() == DataBuffer.TYPE_BYTE) {
			byte[] bitmap = ((DataBufferByte) data).getData();
			for (int i = 0, j = 0; i < buffer.length; i += 4, j += 4) {
				buffer[i+0] = bitmap[j+1];
				buffer[i+1] = bitmap[j+2];
				buffer[i+2] = bitmap[j+3];
				buffer[i+3] = bitmap[j+0];
			}
		} else { // data.getDataType() == DataBuffer.TYPE_INT
			int[] bitmap = ((DataBufferInt) data).getData();
			ByteBuffer tw = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
			for (int i = 0, j = 0; i < buffer.length; i += 4, j++)
				tw.putInt(i, bitmap[j]);
		}
		return buffer;
	}
	
	/**
	 * Constructs the header from a bitmap. Note that with a 108-byte header everything
	 *  is interpreted correctly, that is, with BI_BITFIELDS equal to 3 when transforming
	 *  a bitmap into PNG the bit depth of the PNG is 8 and the color type is 6.
	 * 
	 * @param header	A buffer representing the entire bitmap.
	 * @param length	The length of the entire bitmap.
	 * @param power	The length of one side.
	 */
	public static void headerForBitmap(byte[] header, int length, int power) {
		ByteBuffer tw = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
		tw.putShort ((short) 0x4D42);
		tw.putInt	(length);
		tw.putShort	((short) 0);
		tw.putShort	((short) 0);
		tw.putInt	(14 + BITMAP_HEADER);
		tw.putInt	(BITMAP_HEADER);
		tw.putInt	(power);
		tw.putInt	(power);
		tw.putShort	((short) 1);
		tw.putShort	((short) 32);
		tw.putInt	(BITMAP_HEADER > 40? 3: 0);	// BI_BITFIELDS = 3
		tw.putInt	(4 * power * power);
		tw.putInt	(2835);
		tw.putInt	(2835);
		tw.putInt	(0);
		tw.putInt	(0);
		if (BITMAP_HEADER > 40) {
			tw.putInt	(0x00ff0000);			// Red channel bit mask
			tw.putInt	(0x0000ff00);			// Green channel bit mask
			tw.putInt	(0x000000ff);			// Blue channel bit mask
			tw.putInt	(0xff000000);			// Alpha channel bit mask
			tw.putInt	(0x73524742);			// LCS_sRGB
		}
	}
	
	/**
	 * Try changing the icon layout. There are several errors in this
	 *  arrangement, but that is up to you to decide. Either way, 
	 *  start by resetting all forIcon.
	 *  
	 * @param icon	Can be a sentence as indicated in --help.
	 * @param image	Abstract class that represents a disk icon.
	 * @return	True if there was an error in the input, false otherwise.
	 */
	public static boolean delimiterIcon(String icon, DiskIcons image) {
		for (DiskIconsView fs: image.getFiles()) fs.forIcon = 0;
		if (icon != null) {
			String[] match = icon.split(";");
			for (int i = 0; i < match.length; i++) {
				String[] sub = match[i].split("=");
				try {	// tries to return a number, if it fails returns false
					int p = Integer.parseInt(sub[0]);
					//	returns false if the index does not prescribe or has more than one equal
					if (p < 0 || p >= image.getFiles().size() || sub.length > 2)
						return true;
					sub = sub[1].split(":");
					DiskIconsView fs = image.getFiles().get(p);
					for (int j = 0; j < sub.length; j++) {
						if (sub[j].toLowerCase().equals("png"))
							fs.layout = getInteger(fs.layout) + " PNG";
						else
						if (sub[j].toLowerCase().equals("bit"))
							fs.layout = getInteger(fs.layout) + " 32-bit";
						else
						if (sub[j].length() == 3 || sub[j].length() == 4)
							fs.type = sub[j];
						else
						if (sub[j].equals("*"))
							fs.forIcon = -1;
						else
							return true;
					}
				} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns a simple description of exception {@code e}, like {@link Throwable#toString()}
	 *  with exception simpleName instead of name.
	 *  
	 * @param e	The exception object.
	 * @return	A simple representation of {@code e}.
	 */
	public static String simpleString(Exception e) {
		return e.getClass().getSimpleName()+": "+e.getLocalizedMessage();
	}

	/**
	 * Accepts a {@code path} and returns the extension replaced by {@code ext}.
	 *
	 * @param path	The file path.
	 * @param ext	The replaced extension.
	 * @return	The replacement.
	 */
	public static String replaceExtension(String path, String ext) {
		return new File(path).getPath().replaceFirst("([^.]+)[.][^.]+$", "$1."+ext);
	}
	
	/**
	 * Accepts a {@code path} and returns the extension, if extension does not exist
	 *  returns {@code ""}.
	 *
	 * @param path	The file path.
	 * @return	The extension or {@code ""}.
	 */
	public static String getExtension(String path) {
		return getExtension(new File(path));
	}
	public static String getExtension(File path) {
		String name = path.getName();
		String part = name.replaceFirst("([^.]+)[.][^.]+$", "$1");
		return part.equals(name)? "": name.substring(part.length()+1);
	}
	
	/**
	 * A {@code String} {@code item} is a number possibly followed by several letters,
	 *  from which you simply want to extract the number.
	 *
	 * @param item	Number followed by several letters and numbers.
	 * @return	Just the number or {@code -1}.
	 */
	public static int getInteger(String item) {
		try {	// tries to return a number, if it fails returns -1
			return Integer.parseInt(item.split("[ x²]")[0]);
		} catch (NumberFormatException e) {
			return -1;
		}
	}
	
	/**
	 * A {@code String} {@code item} is a number possibly followed by several letters,
	 *  from which you simply want to extract the first string.
	 *
	 * @param item	Number followed by several letters and numbers.
	 * @return	Just the string or {@code ""}.
	 */
	public static String getSize(String item) {
		return item.split("[ ²]")[0];
	}
	
	/**
	 * A {@code String} {@code item} is a number possibly followed by several letters,
	 *  from which you simply want to extract the second string.
	 *
	 * @param item	Number followed by several letters and numbers.
	 * @return	Just the string or {@code ""}.
	 */
	public static String getIcon(String item) {
		String[] type = item.split(" ");
		return type.length > 1? type[1]: "";
	}
	
	/**
	 * Decodes a {@code String} from the byte buffer {@code in} using at most
	 *  {@code length} bytes. If the resulting string is null terminated then
	 *  the first null and the remaining characters are ignored. The byte
	 *  buffer position is always incremented with {@code length}.
	 * 
	 * @param in		The source byte buffer.
	 * @param length	The maximum number of bytes to decode.
	 * @param charset	The charset to decode the bytes.
	 * @return			The decoded string.
	 */
	public static String getString(ByteBuffer in, int length, Charset charset) {
		byte[] buffer = new byte[length];
		in.get(buffer);
		String text = new String(buffer, charset);
		int i = text.indexOf(0);
		return i == -1? text: text.substring(0, i);
	}

	/**
	 * Returns an integer {@code num} rounded up. Does not work with dividend 0 (zero).
	 *
	 * @param num	An integer value that you want rounded up.
	 * @param div	The number of digits to which you want to round number.
	 * @return	Rounds a number up.
	 */
	public static int roundUp(int num, int div) {
		return (num + div - 1) / div * div;
	}
	
	/**
	 * Returns the length of the bitmap depending on the bitmap header, its length
	 *  and the remaining bitmap.
	 *
	 * @param width	The number of digits to which you want to round number.
	 * @return	Result of expression.
	 */
	public static int bitmapRound(int width) {
		return 40 + 4 * width * width + Static.roundUp(width / 8, 4) * width;
	}
	
	/**
	 * Returns the roundness of the Apple as a function of a number 3 or 4 and
	 *  the size of the side of the square.
	 *
	 * @param num	Number to be rounded up, usually a 3 or a 4.
	 * @param width	The number of digits to which you want to round number.
	 * @return	Result of expression.
	 */
	public static int appleRound(int num, int width) {
		return num * width * width + Static.roundUp(num * width * width, 0x7F) / 0x7F;
	}
	
	/**
	 * Returns a {@code String} with {@code text} inside a fixed width html body, to word wrap
	 *  long lines. Intended for {@code JOptionPane.show*Dialog} messages.
	 * 
	 * @param text	The text to word wrap.
	 * @return	A fixed width representation of the text.
	 */
	public static String wordWrap(String text) {
		return "<html><body style='width:260px;'>" + text
				.replaceAll("&", "&amp;")
				.replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;")
				.replaceAll("\n", "<br>")
				+ "</body></html>";
	}
	
	private static File workDir = null;
	
	/**
	 * Returns the application working directory. The location is operating system dependent.
	 *  
	 * @return	The application working directory. It is created if does not exist.
	 */
	public static File getWorkingDirectory() {
		return workDir;
	}
	
	static { // Get it once at first Static invocation
		String path = System.getenv("AppData"); // windows
		if (path == null || !new File(path).isDirectory()) {
			String home = System.getProperty("user.home");
			if (home != null && new File(home).isDirectory()) {
				path = home + "/Library/Application Support"; // mac
				if (!new File(path).isDirectory()) {
					path = home + "/.config"; // unix
				}
			}
		}
		if (path == null || !new File(path).isDirectory()) {
			path = System.getProperty("java.io.tmpdir"); // other
		}
		if (path != null && new File(path).isDirectory()) {
			File dir = new File(path, "eternalbits");
			if (!dir.exists()) dir.mkdirs();
			if (dir.isDirectory()) {
				workDir = dir;
			}
		}
	}
}
