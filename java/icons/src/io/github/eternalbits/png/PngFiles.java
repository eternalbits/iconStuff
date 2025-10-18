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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.util.List;

import io.github.eternalbits.disk.DiskIcons;
import io.github.eternalbits.disk.DiskIconsView;
import io.github.eternalbits.disk.WrongHeaderException;

/**
 * Implements a {@link PngFiles} of type
 *  <a href="https://en.wikipedia.org/wiki/PNG">
 *  Portable Network Graphics</a> (PNG).
 * <p>
 */
public class PngFiles extends DiskIcons {
	static final ByteOrder BYTE_ORDER = ByteOrder.BIG_ENDIAN;
	public static final int ICON_PGN = 0x89504E47;		// "%PNG"
	public static final int DOS_UNIX = 0x0D0A1A0A;		// "...."
	
	final PngHeader header;
	
	/**
	 * PNG file writing routine.
	 * 
	 * @param file	Write access to PNG file.
	 * @param image	Abstract class that represents a disk icon.
	 */
	public PngFiles(File file, DiskIcons image, String icon) throws IOException, WrongHeaderException {
		media = new RandomAccessFile(file, "rw");
		try { // Always close media on Exception
			path = file.getPath();
			done = false;
			setType();
			
			header = new PngHeader(this, image, icon);
			length = file.length();
		}
		catch (Exception e) {
			media.close();
			throw e;
		}
	}
	
	/**
	 * PNG file reading routine.
	 * 
	 * @param file	Read access to PNG file.
	 * @param mode	String meaning file access.
	 */
	public PngFiles(File file, String mode) throws IOException, WrongHeaderException {
		media = new RandomAccessFile(file, mode);
		try { // Always close media on Exception
			path = file.getPath();
			length = file.length();
			setType();
			
			header = new PngHeader(this, readIcon(0, PngHeader.HEADER_SIZE));
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
		type = "PNG";
	}

}
