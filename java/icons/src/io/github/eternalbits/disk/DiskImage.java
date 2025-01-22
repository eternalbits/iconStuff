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

package io.github.eternalbits.disk;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import io.github.eternalbits.icns.IcnsFiles;
import io.github.eternalbits.ico.IcoFiles;
import io.github.eternalbits.png.PngFiles;

/**
 * Represents reading and writing DiskIcons.
 * <p>
 */
public class DiskImage {
	protected static String INVALID_VALUE = "%s (Mising or invalid value found in %s)";
	protected static String CANT_INITIALIZE = "Can't initialize %s from %s.";	
	private static String UNKNOWN_TYPE = "Unknown disk image type";

	/**
	 * Reading DiskIcons.
	 * 
	 * @param path/file	String/path you want to access DiskIcons.
	 * @param mode	Access type: "r" for read or "rw" for both.
	 */
	public static DiskIcons open(String path, String mode) throws IOException {
		return open(new File(path), mode);
	}

	public static DiskIcons open(File file, String mode) throws IOException {
		
		if (file.length() >= 4) {
			try (RandomAccessFile media = new RandomAccessFile(file, "r")) {
				switch (media.readInt()) {
				case IcnsFiles.ICON_ICNS:							// 'icns' for ICNS
					return new IcnsFiles(file, mode);
				case IcoFiles.ICON_ICO:								// '....' for ICO
					return new IcoFiles(file, mode);
				case PngFiles.ICON_PGN:								// '%PNG' for PNG
					return new PngFiles(file, mode);
				}
			} catch (WrongHeaderException e) {}
		}
		
		throw new InitializationException(DiskIcons.class, file.getPath());
	}

	/**
	 * Writing DiskIcons.
	 * 
	 * @param type	Access type: icns, ico or png.
	 * @param path/file	String/path you want to read from DiskIcons.
	 * @param image	The disk image to be created.
	 */
	public static DiskIcons create(String type, String path, DiskIcons image) throws IOException {
		return create(type, new File(path), image);
	}

	public static DiskIcons create(String type, File file, DiskIcons image) throws IOException {
		
		if ("icns".equalsIgnoreCase(type)) {
			return new IcnsFiles(file, image);
		}
		if ("ico".equalsIgnoreCase(type)) {
			return new IcoFiles(file, image);
		}
		if ("png".equalsIgnoreCase(type)) {
			return new PngFiles(file, image);
		}
		
		throw new IllegalArgumentException(String.format("%s: %s", UNKNOWN_TYPE, type));
	}
}
