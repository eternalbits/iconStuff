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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.util.List;

import io.github.eternalbits.disk.DiskIcons;
import io.github.eternalbits.disk.DiskIconsView;
import io.github.eternalbits.disk.WrongHeaderException;

/**
 * Implements a {@link IcnsFiles} of type Apple
 *  <a href="https://en.wikipedia.org/wiki/Apple_Icon_Image_format">
 *  Icon Image format</a> (ICNS).
 * <p>
 */
public class IcnsFiles extends DiskIcons {
	static final ByteOrder BYTE_ORDER = ByteOrder.BIG_ENDIAN;
	public static final int ICON_ICNS = 0x69636e73;	// "icns"
	
	static final int ICON_JPEG = 0x6a502020;		// "jP  "
	static final int ICON_ARGB = 0x41524742;		// "ARGB"
	
	final IcnsHeader header;						
	
	/**
	 * ICNS file writing routine.
	 * 
	 * @param file	Write access to ICNS file.
	 * @param image	Abstract class that represents a disk icon.
	 */
	public IcnsFiles(File file, DiskIcons image, String icon) throws IOException, WrongHeaderException {
		media = new RandomAccessFile(file, "rw");
		try { // Always close media on Exception
			media.setLength(0);
			path = file.getPath();
			setType();
			
			header = new IcnsHeader(this, image, icon);
			length = file.length();			
		}
		catch (Exception e) {
			media.close();
			throw e;
		}
	}
	
	/**
	 * ICNS file reading routine.
	 * 
	 * @param file	Read access to ICNS file.
	 * @param mode	String meaning file access.
	 */
	public IcnsFiles(File file, String mode) throws IOException, WrongHeaderException {
		media = new RandomAccessFile(file, mode);
		try { // Always close media on Exception
			path = file.getPath();
			length = file.length();
			setType();
			
			header = new IcnsHeader(this, readIcon(0, IcnsHeader.HEADER_SIZE));
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
		type = "ICNS";
	}

}
