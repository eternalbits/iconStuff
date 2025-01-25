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
| 0 | 4 | Icon type, see [`Apple Icon Image format`](https://en.wikipedia.org/wiki/Apple_Icon_Image_format#Icon_types) |
| 4 | 4 | Length of data, in bytes (including type and length), msb first |
| 8 | Variable | Icon data |
Leaving aside the icons with less than 8 bits for each of the 4 colors, 
we are left with the following formats:
- The 24-bit RGB icon (example icl4) plus the 8-bit mask (example l8mk)
- The JPEG 2000 (JP2) icon, this is an icon that we will not deal with
- The PNG icon (example icp4), this is an icon we will deal with
- The 32-bit ARGB icon (example ic04)

Note that both 24-bit RGB and 32-bit ARGB are compressed in the ICNS format, 
following the compression designed into the [`Apple Icon Image format`](https://en.wikipedia.org/wiki/Apple_Icon_Image_format#Compression). 
So the ICNS PNG generates an ICO PNG, and the 24-bit RGB icon plus the mask, 
as well as the 32-bit ARPG, result in a bitmap mask.
