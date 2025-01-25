# ICO - As seen by Microsoft Windows

ICO is a format developed by Microsoft that groups several icons, initially rectangular, 
in the same format. The file format consists of a 6-byte header, followed by any number 
of 16-byte image entries.
### Header
| Offset | Size | Purpose |
| ------ | ---- | ------- |
| 0 | 2 | Reserved. Must always be 0. |
| 2 | 2 | Specifies image type: 1 for icon image. |
| 4 | 2 | Specifies number of images in the file. |
### Image entry
| Offset | Size | Purpose |
| ------ | ---- | ------- |
| 0 | 1 | Specifies image width in pixels. Can be any number between 0 and 255. |
| 1 | 1 | Specifies image height in pixels. Can be any number between 0 and 255. |
| 2 | 1 | Should be 0 if the image does not use a color palette. |
| 3 | 1 | Reserved. Should be 0. |
| 4 | 2 | Specifies color planes. Should be 0 or 1. |
| 6 | 2 | Specifies bits per pixel. |
| 8 | 4 | Specifies the size of the image's data in bytes. |
| 12 | 4 | Specifies the offset of BMP or PNG data from the beginning of the ICO file. |

Then comes the design of the icons. Up until Windows XP, they all had to be bitmapped, 
if I'm not mistaken, with triple RGB followed by an alpha sample. Starting with Windows 
Vista, they can also be of type PNG and the bitmap can have RGBA, with Windows XP 
keeping the old alpha sample.
### Bitmap information header
| Offset | Size | Purpose |
| ------ | ---- | ------- |
| 0 | 4 | The size of this header, in bytes (40). |
| 4 | 4 | The bitmap width in pixels. |
| 8 | 4 | The bitmap height in pixels multiplied by 2. |
| 12 | 2 | The number of color planes (must be 1). |
| 14 | 2 | The number of bits per pixel, which is the color depth of the image. |
| 16 | 4 | The compression method being used.  It should be 0. |
| 20 | 4 | The image size. A dummy value 0 can be provided. |
| 24 | 4 | The horizontal resolution of the image. |
| 28 | 4 | The vertical resolution of the image. |
| 32 | 4 | The number of colors in the color palette (must be 0). |
| 36 | 4 | The number of important colors used (must be 0). |

Two things are noticeable:
- Doubling the height. This is actually due to the duplication of Windows 
XP, although they have nothing to do with each other in terms of size.
- Height "doubling" remains outside of Windows XP. Bits can be reset, 
but it costs little to fill them.

As for the PNG, it is easy to determine its height, since the IHDR must be the first block.
### PNG - IHDR first chunk
| Offset | Size | Purpose |
| ------ | ---- | ------- |
| 0 | 4 | Width in pixels. |
| 4 | 4 | Height in pixels. |
| 8 | 1 | Bit depth (1, 2, 4, 8 or 16) |
| 9 | 1 | Color type (0, 2, 3, 4 or 6) |
| 10 | 1 | Compression method (must be 0) |
| 11 | 1 | Filter method (must be 0) |
| 12 | 1 | Interlace method (0 or 1) |
