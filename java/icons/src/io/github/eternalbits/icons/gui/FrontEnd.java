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

package io.github.eternalbits.icons.gui;

import java.awt.FlowLayout;
import java.awt.Font;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * 
 * <p>
 */
public class FrontEnd extends JFrame {
	private static final long serialVersionUID = 2265801119904942445L;

	public FrontEnd() {
//		setIconImage(new ImageIcon(getResource("icons.png")).getImage());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new FlowLayout());
		JLabel label = new JLabel("<html><br>use<br>java -jar Icons.jar -h<br>in a command line");
		label.setFont(new Font(getName(), Font.PLAIN, 32));
		add(label);
		setSize(460, 300);
		setVisible(true);
	}

	/**
	 * Returns the URL of the resource {@code name} that is in the same folder
	 *  of the class used to create the FronEnd.
	 * 
	 * @param name	The name of the resource.
	 * @return	The resource URL, or {@code null} if the resource does not exist.
	 */
	static URL getResource(String name) {
		String path = FrontEnd.class.getPackage().getName().replace('.', '/');
		return FrontEnd.class.getClassLoader().getResource(path + '/' + name);
	}
}
