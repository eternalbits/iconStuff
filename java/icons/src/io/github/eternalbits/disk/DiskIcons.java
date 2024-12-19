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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * 
 * <p>
 */
public abstract class DiskIcons implements AutoCloseable {
	
	protected RandomAccessFile media = null;			// 
	protected boolean readOnly = true;					// 
	protected String path = null;						// 
	protected String type = null;						// 
	protected long length = 0;							// 
	
	public abstract List<DiskIconsView> getFiles();		// 
	public abstract void setType();						// 
	
	public RandomAccessFile getMedia() {				// 
		return media;
	}
	
	public String getPath() {							// 
		return path;
	}
	
	public String getType() {							// 
		return type;
	}
	
	public long getLength() {							// 
		return length;
	}
	
	public DiskImageView getView() {					// 
		return new DiskImageView(this);
	}
	
	/**
	 * 
	 * <p>
	 */
	public ByteBuffer readIcon(long offset, int length) throws IOException {
		media.seek(offset);
		byte[] buffer = new byte[length];
		int read = media.read(buffer, 0, length);
		return ByteBuffer.wrap(buffer, 0, read < 0? 0: read);
	}
	
	/**
	 * 
	 * <p>
	 */
	@Override
	public synchronized void close() throws IOException {
		if (media != null) {
			media.close();
			media = null;
		}
	}
	
}
