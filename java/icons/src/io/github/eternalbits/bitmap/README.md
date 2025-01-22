# ICO - As seen by Windows Microsoft

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
