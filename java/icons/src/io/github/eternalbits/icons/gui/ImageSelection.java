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

package io.github.eternalbits.icons.gui;

import java.awt.Image;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * A {@code Transferable} which implements the capability required to
 *  transfer a {@code Image}. This {@code Transferable} properly supports 
 *  {@code DataFlavor.imageFlavor}.
 */
public class ImageSelection implements Transferable, ClipboardOwner {

	private static final int IMAGE = 0;
	
	private static final DataFlavor[] flavors = {DataFlavor.imageFlavor};

	private Image image;
	
	/**
	 * Creates a {@code Transferable} capable of transferring the specified {@code Image}.
	 *
	 * @param  image The Image to be transferred.
	 */
	public ImageSelection(Image image) {
		this.image = image;
	}
	
	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {}

	/**
	 * Returns an array of flavors in which this {@code Transferable} can provide the image.
	 *  {@code DataFlavor.imageFlavor} is properly supported.
	 *
	 * @return An array of length one, whose elements is {@code DataFlavor.imageFlavor}.
	 */
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return flavors.clone();
	}

	/**
	 * Returns whether the requested flavor is supported by this {@code Transferable}.
	 *
	 * @param  flavor The requested flavor for the image.
	 * @return {@code true} if {@code flavor} is equal to {@code DataFlavor.imageFlavor}.
	 */
	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		for (int i = 0; i < flavors.length; i++)
			if (flavor.equals(flavors[i]))
				return true;
		return false;
	}

	/**
	 * Returns the {@code Transferable}'s image in the requested {@code imageFlavor} if possible.
	 *  If the desired flavor is {@code DataFlavor.imageFlavor}, or an equivalent flavor, the
	 *  {@code Image} representing the selection is returned.
	 *
	 * @param  flavor The requested flavor for the image.
	 * @return The image in the requested flavor, as outlined above.
	 */
	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (flavor.equals(flavors[IMAGE])) {
			return (Object)image;
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}

}
