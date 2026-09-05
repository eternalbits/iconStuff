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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.util.List;

import io.github.eternalbits.disk.DiskIcons;
import io.github.eternalbits.disk.DiskIconsView;
import io.github.eternalbits.disk.WrongHeaderException;

/**
 * Implements a {@link Jp2Files} of type
 *  <a href="https://en.wikipedia.org/wiki/JPEG_2000">
 *  JPEG 2000</a> (JP2).
 * <p>
 */
public class Jp2Files extends DiskIcons {
	static final ByteOrder BYTE_ORDER = ByteOrder.BIG_ENDIAN;
	public static final int ICON_JP2 = 0x0000000C;		// "...."

	final Jp2Header header;
	
	/**
	 * JPEG 2000 file writing routine.
	 * 
	 * @param file	Write access to JP2 file.
	 * @param image	Abstract class that represents a disk icon.
	 */
	public Jp2Files(File file, DiskIcons image, String icon) throws IOException, WrongHeaderException {
		media = new RandomAccessFile(file, "rw");
		try { // Always close media on Exception
			path = file.getPath();
			done = false;
			setType();
			
			header = new Jp2Header(this, image, icon);
			length = file.length();
		}
		catch (Exception e) {
			media.close();
			throw e;
		}
	}

	/**
	 * JPEG 2000 file reading routine.
	 * 
	 * @param file	Read access to JP2 file.
	 * @param mode	String meaning file access.
	 */
	public Jp2Files(File file, String mode, String jpeg) throws IOException, WrongHeaderException {
		media = new RandomAccessFile(file, mode);
		try { // Always close media on Exception
			path = file.getPath();
			length = file.length();
			setType();
			
			header = new Jp2Header(this, readIcon(0, Jp2Header.HEADER_SIZE), jpeg);
		}
		catch (Exception e) {
			media.close();
			throw e;
		}
	}
	
	@Override
	public List<DiskIconsView> getFiles() {
		return header.disk;
	}

	@Override
	public void putIcon(DiskIconsView fs) {
		header.disk.add(fs);
	}

	@Override
	public void setType() {
		type = "JP2";
	}
	
}
