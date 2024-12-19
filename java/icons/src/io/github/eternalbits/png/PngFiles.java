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
	public static final int ICON_PGN = 0x89504E47;		// %PNG
	public static final int DOS_UNIX = 0x0D0A1A0A;		// ....
	
	final PngHeader header;
	
	/**
	 * 
	 * <p>
	 */
	public PngFiles(File file, DiskIcons image) throws IOException {
		media = new RandomAccessFile(file, "rw");
		try { // Always close media on Exception
			media.setLength(0);
			path = file.getPath();
			readOnly = false;
			setType();
			
			header = new PngHeader(this, image);
		}
		catch (Exception e) {
			media.close();
			throw e;
		}
	}
	
	/**
	 * 
	 * <p>
	 */
	public PngFiles(File file, String mode) throws IOException, WrongHeaderException {
		media = new RandomAccessFile(file, mode);
		try { // Always close media on Exception
			readOnly = mode.equals("r");
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
	public void setType() {
		type = "PNG";
	}

}
