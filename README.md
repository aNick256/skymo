# Skymo Plugin

## Overview

The Skymo plugin is an ImageJ plugin for generating and analyzing kymographs. It allows users to:

- Reslice an image stack into a kymograph
- Visualize individual channels of a multichannel kymograph
- Generate a composite view merging all channels  
- Add scale bars and time bars
- Adjust pixel size calibration
- Calculate movement speeds
- Save annotated kymograph images
- Crop videos along with the axis of the line ROI
- Make time series images along the line ROI

## Usage

To use the Skymo plugin:

1. Open an image stack in ImageJ and draw your ROI. This will be the input for generating the kymograph.

2. Run the plugin using `Plugins > Skymo`. This will reslice the stack into a kymograph image. You can define a shortcut for Skymo so that it immediately run when you press a button on your keyboard.  

3. The plugin interface shows the individual channels of the kymograph as well as a composite merged view.

4. Set the pixel size calibration using the "Pixel Size" field. The image calibration is used by default to extract pixel size.

5. Add a scale bar by setting the "Scale bar length" and clicking "Add Scale Bar". If you want to remove the scalebar enter 0 in the "Scale bar length" field.

6. Add a time bar by setting the "Time Bar Length" and clicking "Add Time Bar". The length can be in seconds or minutes. If you want to remove the timebar enter 0 in the "Time Bar Length" feild.

7. The time bar can be repositioned by clicking and dragging them.

8. The Merged panel is interactive. This means you can draw an ROI there and then calculate its slope. Calculate speeds using the ROI on the Merged panel by clicking "Calculate Speed". If you want an independent merged image right click and select "duplicte".  

9. Save the annotated kymograph by clicking "Save" and selecting a folder. This saves the kymograph and ROI data.

10. The channel text labels can be edited before saving to customize the output image.

11. Close the plugin window when done.

## Implementation Notes

- The kymograph is generated using the ImageJ Reslice command.
- Dragging time bar only updates the y-position to constrain movement. 
- Saving captures the entire plugin window contents as a new image. **Thereby the original grey values are lost**
- Metadata and calibration are updated before saving the image.

## Contributors

This ImageJ plugin was developed by Ali Nick Maleki, MDo Lab, TU DELFT.
