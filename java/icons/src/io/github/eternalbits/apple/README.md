# ICNS - As seen by Apple macOS

ICNS is a format developed by Apple that groups several icons, initially rectangular, 
in the same format. The file format consists of an 8-byte header, followed by any 
number of icons.
### Header
| Offset | Size | Purpose |
| ------ | ---- | ------- |
| 0 | 4 | Magic literal, must be "icns" |
| 4 | 4 | Length of file, in bytes, msb first |
### Icon data
| Offset | Size | Purpose |
| ------ | ---- | ------- |
| 0 | 4 | Icon type, see [`Apple Icon Image format`](https://en.wikipedia.org/wiki/Apple_Icon_Image_format#toc-Icon_data) |
| 4 | 4 | Length of data, in bytes (including type and length), msb first |
| 8 | Variable | Icon data |
