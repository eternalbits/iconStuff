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

import java.awt.image.BufferedImage;

/**
 * A read and write view of a {@link DiskImageView}. All fields are public.
 * <p>
 */
public class DiskIconsView {
	
	public int				isIcon;				// This is the input icon type that goes from NOT_AN_ICON to ICON_ARGB
	public int				forIcon;			// This is the output icon type which is PNG, BITMAP, APPLE, MASK, ARGB
	public int				offset;				// The offset goes from the beginning of the file to the beginning of the icon
	public int				length;				// Icon length, in bytes
	public String			type;				// The type can be PNG, ICO or a character set from the Apple macOS
	public String			description;		// A brief description of the icon for the viewer
	public String			layout;				// A detailed description for programming 
	public BufferedImage	image;				// A PNG image
	
}
