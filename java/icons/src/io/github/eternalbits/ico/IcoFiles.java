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

package io.github.eternalbits.ico;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.util.List;

import io.github.eternalbits.disk.DiskIcons;
import io.github.eternalbits.disk.DiskIconsView;
import io.github.eternalbits.disk.WrongHeaderException;

/**
 * Implements a {@link IcoFiles} of type Microsoft
 *  <a href="https://en.wikipedia.org/wiki/ICO_(file_format)">
 *  ICO File Format</a> (ICO).
 * <p>
 */
public class IcoFiles extends DiskIcons {
	static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
	public static final int ICON_ICO = 0x00000100;	// "...."
	
	final IcoHeader header;
	
	/**
	 * ICO file writing routine.
	 * 
	 * @param file	Write access to ICO file.
	 * @param image	Abstract class that represents a disk icon.
	 */
	public IcoFiles(File file, DiskIcons image, String icon) throws IOException, WrongHeaderException {
		media = new RandomAccessFile(file, "rw");
		try { // Always close media on Exception
			media.setLength(0);
			path = file.getPath();
			setType();
			
			header = new IcoHeader(this, image, icon);
			length = file.length();
		}
		catch (Exception e) {
			media.close();
			throw e;
		}
	}
	
	/**
	 * ICO file reading routine.
	 * 
	 * @param file	Read access to ICO file.
	 * @param mode	String meaning file access.
	 */
	public IcoFiles(File file, String mode) throws IOException, WrongHeaderException {
		media = new RandomAccessFile(file, mode);
		try { // Always close media on Exception
			path = file.getPath();
			length = file.length();
			setType();
			
			header = new IcoHeader(this, readIcon(0, IcoHeader.HEADER_SIZE));
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
	public void setType() {
		type = "ICO";
	}
	
}
