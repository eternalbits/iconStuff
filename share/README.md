# How to install on Windows, macOS and Linux

This application requires a Java Runtime Environment equal to or greater than 11.
 Go to [`Java`](https://www.oracle.com/java/technologies/downloads/) to get the download you want.

### Install on Microsoft Windows

From the [`Releases`](https://github.com/eternalbits/icons/releases/) page download `icons-x.y-win.zip`. You have 3 options:
* Copy to `C:\Program Files`. You have to give permission to `Continue`.
* Copy to another folder. You have to change the link in at least in one place.
* Go to [`Launch4j`](http://launch4j.sourceforge.net/) and bring the file. Run as administrator. Open `Icons.xml`. Build wrapper.

### Install on Apple macOS

From the [`Releases`](https://github.com/eternalbits/icons/releases/) page download `icons-x.y-mac.dmg`. Copy `Icons` to the `Applications` folder.
 
When first opened, close the window and go to `System Settings > Privacy & Security`. Click on `Security` and then press `Open Anyway`.

### Install on Linux

From the [`Releases`](https://github.com/eternalbits/icons/releases/) page download `icons-x.y-linux.tar.gz`. Write the following in `Terminal`:
````
sudo tar -xvf ~/Downloads/icons-x.y-linux.tar.gz -C /opt/
sudo cp /opt/Icons/io.github.eternalbits.icons.desktop /usr/share/applications/
````
If you don't have `sudo` access, or don't want to use `sudo`, you have the following option:
````
tar -xvf ~/Downloads/icons-x.y-linux.tar.gz -C ~/
nano ~/Icons/io.github.eternalbits.icons.desktop
Replace on 2 sides /opt/ with /home/<user>/
cp ~/Icons/io.github.eternalbits.icons.desktop ~/.local/share/applications/
````
