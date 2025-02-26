# About Icons - Convert ICO and ICNS disk images

### How to Install "Icons"

You can find a brief description on the [`share`](https://github.com/eternalbits/iconStuff/tree/main/share/) 
page. Currently only command line input is available, but I rely on this to project 
a screen similar to that of CompactVD.

### What is "Icons"?

"Icons" is primarily a version that allows you to translate from [Microsoft Windows
ICO](https://en.wikipedia.org/wiki/ICO_(file_format)) to [Apple macOS
ICNS](https://en.wikipedia.org/wiki/Apple_Icon_Image_format) and vice versa.
Currently, the application gives up switching between PNG images, as both 
functions have this tool, [`Microsoft's bitmap`](https://github.com/eternalbits/iconStuff/tree/main/java/icons/src/io/github/eternalbits/bitmap/)
that is present in ICO and [`Apple's compression`](https://github.com/eternalbits/iconStuff/tree/main/java/icons/src/io/github/eternalbits/apple/) 
that is present in ICNS.

The --icon parameter means a format of choice for your icons.

Both files can be viewed with the [XnView MP](https://www.xnview.com/en/xnviewmp/) tool.

#### Microsoft's ICO
`--icon 0=png;1=png;2=*;3=*;4=png;5=*;6=*;7=bit`

The phrase for the ICO consists of
- Number that appears in front of fileIcons
- Description of the respective icon: BIT, PNG or * (which means that
this icon should be deleted)

#### Apple's ICNS
`--icon 1=ic13:png;2=ic11:png;3=ic04:bit;4=png`

The sentence for the ICNS consists of
- Number that appears in front of fileIcons
- Description of the respective icon: BIT, PNG or *
- Description of [`Icon types`](https://en.wikipedia.org/wiki/Apple_Icon_Image_format#Icon_types).
In reality this is a description of length 3 or 4, and nothing is
validated regarding its origin. It only works if the icon type is
correct.

#### PNG
`--icon 1=*;3=*;4=png;6=*`

The sentence for PNG consists of
- Number that appears in front of fileIcons
- Description of the respective icon: PNG or * (it is always the
largest icon that is selected)

### Future advancements of "Icons":
- Adding a window.

### Copyright Notices

Copyright © 2024-2025 Rui Baptista

Licensed under the Apache License, Version 2.0. You may obtain a copy of the
 License released under the Apache License 2.0. Software distributed under
 the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 CONDITIONS OF ANY KIND, either express or implied.

This product includes Apache Commons CLI software Copyright 2002-2024
 The Apache Software Foundation.