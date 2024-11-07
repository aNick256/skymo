// Ali Nick Maleki, MDo Lab, TU DELFT
// 25/09/2023

import ij.IJ;
import ij.ImagePlus;
import ij.ImageJ;
import ij.gui.NewImage;
import ij.gui.*;
import ij.plugin.PlugIn;
import ij.WindowManager;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import javax.swing.border.Border;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import ij.io.FileInfo;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import ij.process.ColorProcessor;
import ij.io.FileSaver;
import java.text.DecimalFormat;
import javax.swing.JFileChooser;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import java.awt.Point;
import java.awt.geom.PathIterator;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import ij.process.FloatPolygon;
import ij.ImageStack;
import ij.gui.GenericDialog;
import java.awt.geom.AffineTransform;
import java.awt.Polygon;
import ij.gui.PolygonRoi;
import java.awt.geom.Point2D;
import ij.CompositeImage;
import ij.plugin.Duplicator;
import java.awt.Font;
import ij.gui.GenericDialog;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import ij.io.RoiEncoder;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;



public class SKymo_ implements PlugIn, ActionListener {

    private JTextField pixelSizeTextField;
    private ImagePlus kymograph;
    private JPanel ImgParentPanel;
    private JPanel compositePanel;
    private int channels;
    private JPanel scaleBarPanel;
    private JButton addScaleBarButton;
    private JFrame frame;
    private JPanel topPanel;
    private JLabel compositeLabel;
    private int panelHeight;
    private int panelWidth;
    private JPanel CompsPanelParent;
    private JPanel imgFramePanel;
    private JButton addTimeBarButton;
    private JLabel scaleBarLabel;
    private JLabel timeBarLabel ;
    private JTextField timeBarLengthTextField;
    private JComboBox<String> timeUnitDropdown;
    private JTextField frameIntervalTextField;
    private JTextField scaleBarLengthTextField;
    private int savedImageCount = 0; 
    private int separatorWidth = 10; // Width of the separator between images
    private JPanel timeBarPanel;
    private JPanel imagePanel;
    private String imageDirectory;
    private JTextField saveDirectoryTextField;
    private JButton browseButton;
    private JButton pixelSizeButton;
    private JButton calculateSpeedButton;
    private RoiManager roiManager;
    private ImageCanvas imageCanvas;
    private JTextField time_serires_marginTextField;
    private int mouseX;
    private int mouseY;
    private boolean isDragging = false;
    private int timeBarPanelX;
    private int timeBarPanelY;
    private boolean time_bar_location_changed = false ;
    private double timeBarLength ;





    public static void main(String[] args) {
        // Start ImageJ
        //ImageJ imageJ = new ImageJ();

        // Create an instance of the plugin
        SKymo_ plugin = new SKymo_();

        // Run the plugin
        plugin.run(null);
    }



public void run(String arg) {
    // Load the default image

    // String ImagePath = "/Users/anmaleki/Desktop/210914/stackbuilder/210914/PC-L_R-_frap_1_C=TIRF488-1.tif";
    // ImagePlus inp = IJ.openImage(ImagePath);
    // inp.show();
    ImagePlus imp = WindowManager.getCurrentImage();
    

    if (imp == null) {
        IJ.error("No image is currently open.");
        return;
    }


    // Wait for the image to open completely
    while (imp.getWidth() <= 0 || imp.getHeight() <= 0) {
        try {
            Thread.sleep(500); // Sleep for 500 milliseconds before checking again
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    if (imp != null) {
        // Get the file info for the active image
        FileInfo fileInfo = imp.getOriginalFileInfo();

        // Check if the file info is available and contains the directory
        if (fileInfo != null && fileInfo.directory != null) {
            imageDirectory = fileInfo.directory;
            //IJ.log("Image Directory: " + imageDirectory);
        } else {
            IJ.log("Image directory is not available for the active image.");
        }
    } else {
        IJ.log("No active image found.");
    }


    // Now, ImagePath contains the selected directory or an empty string if the user canceled


    // Reslice the image using the "Reslice" plugin
    IJ.run(imp, "KymoResliceWide ", "intensity=Maximum");

    kymograph = WindowManager.getCurrentImage();
    channels = kymograph.getNChannels();

    // Check if kymograph is null (indicating an issue with the Reslice command)
    if (kymograph == null) {
        IJ.error("Error: Kymograph not found. There may be an issue with the Reslice command.");
    } else {
        // Set kymograph visibility to false (hidden)
        kymograph.getWindow().setVisible(false);
    }

    int separatorWidth = 10; // Width of the separator between images

    // Calculate the total width of the panel including separators
    panelWidth = (kymograph.getWidth() + separatorWidth) * (channels + 1) + 3 * separatorWidth ; // +1 for composite view
    if( panelWidth<520 ){
        panelWidth = 520;
    }
    panelHeight = kymograph.getHeight() + 280 ; // Increase panel height to accommodate text boxes

    // Read channel data from channel_data.txt
    // Change these lines:

    String channelDataPath = imageDirectory + "/Kymograph/channel_data.txt";

    // Rest of your code remains the same...

    String[] channelData = readChannelData(channelDataPath, channels);

   

    // Create a new JFrame
    frame = new JFrame("Skymo plugin");
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.setSize(panelWidth, panelHeight);

        // Add a WindowListener to the frame to handle closing events
    frame.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            // Close the kymograph window when the plugin's frame is closed
            if (kymograph != null) {
                kymograph.close();
            }
        }
    });

    // Create the top panel (containing topLeftPanel and topRightPanel)
    topPanel = new JPanel(); // One row, two columns

    // Create the topLeftPanel with BoxLayout in Y direction
    JPanel topLeftPanel = new JPanel();
    topLeftPanel.setLayout(new BoxLayout(topLeftPanel, BoxLayout.Y_AXIS));

    // Create a horizontal panel for "Pixel Size" label and text field
    JPanel pixelSizeHorizontalPanel = new JPanel();
    pixelSizeHorizontalPanel.setLayout(new BoxLayout(pixelSizeHorizontalPanel, BoxLayout.X_AXIS));

    // Get the image calibration
    Calibration calibration = imp.getCalibration();

    // Use the pixel width from calibration as the default pixel size
    double defaultPixelSize = calibration.pixelWidth;



    JLabel pixelSizeLabel = new JLabel("Pixel Size (µm):");
    pixelSizeTextField = new JTextField("1.0", 8);
        // Set the default pixel size in the pixelSizeTextField
    pixelSizeTextField.setText(String.valueOf(defaultPixelSize));
    pixelSizeTextField.setMaximumSize(new Dimension(60, 20));
    // Add the "Pixel Size" label and text field to the horizontal panel
    pixelSizeHorizontalPanel.add(pixelSizeLabel);
    //pixelSizeHorizontalPanel.add(Box.createHorizontalStrut(10)); // Add spacing
    pixelSizeHorizontalPanel.add(pixelSizeTextField);

    // Create the "1.5x" button
    pixelSizeButton = new JButton("1.5x");
    pixelSizeButton.addActionListener(this);
    pixelSizeButton.setMaximumSize(new Dimension(50, 20));
    pixelSizeButton.setPreferredSize(new Dimension(50, 20));
    pixelSizeHorizontalPanel.add(pixelSizeButton); // Add the button
    // Create the Scale Bar Length panel
    JPanel scaleBarLengthPanel = new JPanel();
    scaleBarLengthPanel.setLayout(new BoxLayout(scaleBarLengthPanel, BoxLayout.X_AXIS));
    JLabel scaleBarLengthLabel = new JLabel("Scale bar length (µm):");
    scaleBarLengthTextField = new JTextField("5",8);
    scaleBarLengthTextField.setMaximumSize(new Dimension(40, 20));
    // Add the components to the Scale Bar Length panel
    scaleBarLengthPanel.add(scaleBarLengthLabel);
    scaleBarLengthPanel.add(scaleBarLengthTextField);

    // Add Scale Bar button
    addScaleBarButton = new JButton("Add Scale Bar");

    // Add the "Pixel Size" horizontal panel to the topLeftPanel
    topLeftPanel.add(pixelSizeHorizontalPanel);

    // Add spacing
    topLeftPanel.add(Box.createVerticalStrut(10));

    // Add the Scale Bar Length panel and Add Scale Bar button to the topLeftPanel
    topLeftPanel.add(scaleBarLengthPanel);
    topLeftPanel.add(addScaleBarButton);
    calculateSpeedButton = new JButton("Calculate Speed");
    calculateSpeedButton.addActionListener(this);
    calculateSpeedButton.setMaximumSize(new Dimension(150, 20)); // Adjust the size as needed
    topLeftPanel.add(calculateSpeedButton);


    // Create the topRightPanel
    JPanel topRightPanel = new JPanel();
    topRightPanel.setLayout(new BoxLayout(topRightPanel, BoxLayout.Y_AXIS));

    // Create Frame Interval panel
    JPanel frameIntervalPanel = new JPanel();
    frameIntervalPanel.setLayout(new BoxLayout(frameIntervalPanel, BoxLayout.X_AXIS));
    JLabel frameIntervalLabel = new JLabel("Frame Interval (s):");
        // Get the frame interval from metadata
    frameIntervalTextField = new JTextField("1.0", 8); // Initialize it with a default value
    frameIntervalTextField.setMaximumSize(new Dimension(60, 20));

    // Call readFrameIntervalFromMetadata to get the frame interval value
    double frameInterval = readFrameIntervalFromMetadata(imp);

    // Set the frame interval in the frameIntervalTextField
    frameIntervalTextField.setText(String.valueOf(frameInterval));



    // Create Time Bar Length panel
    JPanel timeBarLengthPanel = new JPanel();
    timeBarLengthPanel.setLayout(new BoxLayout(timeBarLengthPanel, BoxLayout.X_AXIS));
    JLabel timeBarLengthLabel = new JLabel("Time Bar Length:");
    timeBarLengthTextField = new JTextField("1", 3);
    timeBarLengthTextField.setMaximumSize(new Dimension(40, 20));

    // Create Time Unit ComboBox
    String[] timeUnits = {"sec", "min"};
    timeUnitDropdown = new JComboBox<>(timeUnits);
    timeUnitDropdown.setSelectedItem("min"); // Set "min" as the default

    // Add components to the Frame Interval panel
    frameIntervalPanel.add(frameIntervalLabel);
    frameIntervalPanel.add(frameIntervalTextField);

    // Add components to the Time Bar Length panel
    timeBarLengthPanel.add(timeBarLengthLabel);
    timeBarLengthPanel.add(timeBarLengthTextField);
    timeBarLengthPanel.add(timeUnitDropdown);

    // Create Add Time Bar button
    addTimeBarButton = new JButton("Add Time Bar");
    addTimeBarButton.addActionListener(this); // Ensure the ActionListener is set


    // Add components to the topRightPanel
    topRightPanel.add(frameIntervalPanel);
    topRightPanel.add(timeBarLengthPanel);
    topRightPanel.add(addTimeBarButton);
    
    JButton cropVideoButton = new JButton("Crop Video");
    cropVideoButton.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        cropVideo(imp);
    }
    });
    topRightPanel.add(cropVideoButton);

    // Add topLeftPanel and topRightPanel to topPanel
    topLeftPanel.setMaximumSize(new Dimension(20, 10));
    topRightPanel.setMaximumSize(new Dimension(20, 10));
    topPanel.add(topLeftPanel);
    topPanel.add(topRightPanel);
    
    // Set the alignment of components in topLeftPanel to left-aligned
    for (Component component : topLeftPanel.getComponents()) {
        ((JComponent) component).setAlignmentX(Component.LEFT_ALIGNMENT);
    }
    for (Component component : topRightPanel.getComponents()) {
        ((JComponent) component).setAlignmentX(Component.LEFT_ALIGNMENT);
    }
    // Create a black border
    Border blackBorder = BorderFactory.createLineBorder(Color.BLACK);

    // Set the black border to the topPanel
    topPanel.setBorder(blackBorder);

    // Attach action listener to the "Add Scale Bar" button
    addScaleBarButton.addActionListener(this);

    // Create a JPanel to hold the individual frames (images)
    ImgParentPanel = new JPanel();
    // Change the layout manager of ImgParentPanel to GridBagLayout
    ImgParentPanel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.NORTH; 
    //ImgParentPanel.setLayout(new GridLayout(1, channels + 1)); // Rows are dynamically added

    // Loop through each channel and add the visual representation to the panel
    for (int c = 1; c <= channels; c++) {
        // Set the active channel
        kymograph.setActiveChannels(getChannelActivationString(c, channels));

        // Create the BufferedImage for the current channel
        BufferedImage image = kymograph.getBufferedImage();

        // Create a JLabel to display the image with a centered label above it
        imagePanel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(new ImageIcon(image));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        imagePanel.add(label, BorderLayout.CENTER);
        String channelText = getChannelText(channelData[c - 1]);
        JTextField textField = new JTextField(channelText); // Adjust index
        textField.setHorizontalAlignment(JTextField.CENTER);
        imagePanel.add(textField, BorderLayout.NORTH);
        // Set the size of the composite panel
        Dimension imgPanelSize = imagePanel.getPreferredSize();
        imagePanel.setBounds(0, 0, imgPanelSize.width, imgPanelSize.height + separatorWidth);
        JPanel ChannelPanel = new JPanel();

        // Add the label with textbox to the panel
        if(channels != 1){
        ChannelPanel.add(imagePanel);}
            // Add the label with textbox to the ImgParentPanel with GridBagConstraints
    gbc.gridx = c - 1; // Set the column for this component
    ImgParentPanel.add(ChannelPanel, gbc);
    }
    kymograph.setActiveChannels("1111");

    imageCanvas = kymograph.getCanvas();
    compositePanel = new JPanel(new BorderLayout());
    compositePanel.add(imageCanvas, BorderLayout.CENTER);

    String mergedText = "Merged"; // Use last element for "Merged"
    JTextField mergedTextField = new JTextField(mergedText);
    mergedTextField.setHorizontalAlignment(JTextField.CENTER);
    compositePanel.add(mergedTextField, BorderLayout.NORTH);

    // Set the size of the composite panel
    Dimension compositePanelSize = compositePanel.getPreferredSize();
    compositePanel.setBounds(0, 0, compositePanelSize.width +  separatorWidth, compositePanelSize.height +  separatorWidth);
    CompsPanelParent = new JPanel();
    CompsPanelParent.add(compositePanel);
    ImgParentPanel.add(CompsPanelParent);

    JPanel outerPanel = new JPanel();
    imgFramePanel = new JPanel();
    imgFramePanel.add(ImgParentPanel);
    outerPanel.add(topPanel, BorderLayout.CENTER);
    frame.add(outerPanel, BorderLayout.NORTH);
    frame.add(imgFramePanel, BorderLayout.CENTER);
    // Add the MouseListener to the frame


        // Create a bottom panel
JPanel bottomPanel = new JPanel();
JButton saveCompositeButton = new JButton("Save Composite");

String DefaultsaveDirectory = imageDirectory + "Kymograph" + File.separator + "composites";
JTextField savingDirectoryTextField = new JTextField(DefaultsaveDirectory, 20);
savingDirectoryTextField.setEditable(true); // Make it non-editable

saveCompositeButton.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        // Handle saving composite when the "Save Composite" button is clicked
        String saveDirectory = savingDirectoryTextField.getText();
        saveImage(imp, saveDirectory); 
    }
});

JButton saveKymoButton = new JButton("Save Kymo");
saveKymoButton.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        // Handle saving kymograph when the "Save Kymo" button is clicked
        saveKymograph(kymograph, DefaultsaveDirectory);
    }
});

// Create a "Browse" button
JButton browseButton = new JButton("Browse");

// Add an ActionListener to the "Browse" button
browseButton.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        // Open a file dialog for the user to choose a directory
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = fileChooser.showDialog(frame, "Choose Directory");
        if (result == JFileChooser.APPROVE_OPTION) {
            String selectedDirectory = fileChooser.getSelectedFile().getAbsolutePath();
            savingDirectoryTextField.setText(selectedDirectory);
        }
    }
});

// Add the components to the bottom panel
bottomPanel.add(savingDirectoryTextField);
bottomPanel.add(browseButton);
bottomPanel.add(saveCompositeButton);
bottomPanel.add(saveKymoButton);

// Add the bottom panel to the frame at the bottom
frame.add(bottomPanel, BorderLayout.SOUTH);
    // Display the frame
    frame.setVisible(true);
    //kymograph.show();
    addScaleBar();
    addTimeBar();
    timeBarPanelX = timeBarPanel.getX();
    timeBarPanelY = timeBarPanel.getY();
    // Add a component listener to the parent component (CompsPanelParent) to handle resize events
    frame.addComponentListener(new ComponentAdapter() {
    @Override
    public void componentResized(ComponentEvent e) {
        // Calculate the new Y-coordinate based on the stored value and the new height
    if(time_bar_location_changed){
        timeBarPanel.setLocation(timeBarPanelX, timeBarPanelY);
    }
    }
    });

}

private void cropVideo(ImagePlus stack) {
    String stack_name = stack.getShortTitle();
    Roi original_roi = stack.getRoi();
    double roiAngle = original_roi.getAngle();
    double scale = stack.getCalibration().pixelWidth;
    double roiLength = stack.getRoi().getLength() / scale;
    float roi_thickness = original_roi.getStrokeWidth();
    original_roi.setStrokeWidth(50);


    // Get the corner coordinates of the line ROI
    int[] xPoints = stack.getRoi().getPolygon().xpoints;
    int[] yPoints = stack.getRoi().getPolygon().ypoints;

    // Create a rectangle ROI using the line ROI's corner coordinates
    PolygonRoi rectRoi = new PolygonRoi(xPoints, yPoints, xPoints.length, Roi.POLYGON);

    // Set the new ROI on the stack
    stack.setRoi(rectRoi);

    // Create an ImagePlus from the cropped stack
    ImagePlus croppedImagePlus = stack.crop("stack");

    // Rotate the cropped image
    IJ.run(croppedImagePlus, "Rotate... ", "angle=" + roiAngle + " grid=1 interpolation=Bilinear enlarge stack");

    // Add a rectangle ROI to the center of the image
    int centerX = croppedImagePlus.getWidth() / 2;
    int centerY = croppedImagePlus.getHeight() / 2;
    int rectWidth = (int) roiLength; // Length of the original line ROI
    int rectHeight = 30; // Width of the rectangle ROI
    int rectX = centerX - (rectWidth / 2);
    int rectY = centerY - (rectHeight / 2);
    PolygonRoi centerRectRoi = new PolygonRoi(new int[]{rectX, rectX + rectWidth, rectX + rectWidth, rectX},
                                              new int[]{rectY, rectY, rectY + rectHeight, rectY + rectHeight}, 4, Roi.POLYGON);
    croppedImagePlus.setRoi(centerRectRoi);

    ImagePlus croppedImagePlus_final = croppedImagePlus.crop("stack");
    croppedImagePlus.close();
    original_roi.setStrokeWidth(roi_thickness);
    stack.setRoi(original_roi);

    // Show the cropped and rotated image with the center rectangle ROI
    croppedImagePlus_final.setTitle("rotated_cropped_" + stack_name);
    croppedImagePlus_final.show();

    // Create a dialog with "Make time series" button and a text field
    JDialog dialog = new JDialog();
    JButton makeTimeSeriesButton = new JButton("Make time series");
    
    JTextField numberOfSnapshotsField = new JTextField("10"); // Adjust the width as needed
    JLabel numberOfSnapshotsLabel = new JLabel("Number of snapshots");
    

    time_serires_marginTextField = new JTextField("2", 5);
    JLabel time_serires_margin_label= new JLabel("Set the margin");
    JPanel Crp_video_margin_panel = new JPanel();

    Crp_video_margin_panel.add(time_serires_margin_label);
    Crp_video_margin_panel.add(time_serires_marginTextField);

    JPanel Crp_video_panel = new JPanel();
    // Create a label for "Frame range"
    // Create a label for "Frame range"
    JLabel frameRangeLabel = new JLabel("Frame range");

    // Create a text field with the default value "1-number_of_slices"
    int number_of_frames = stack.getStackSize(); 
    int N_channels = stack.getNChannels();
    int number_of_slices = number_of_frames / N_channels;
    JTextField frameRangeTextField = new JTextField("1-" + number_of_slices, 6); // Adjust the width as needed

    // Add the label and text field to a panel
    JPanel frameRangePanel = new JPanel();
    frameRangePanel.add(frameRangeLabel);
    frameRangePanel.add(frameRangeTextField);
    frameRangePanel.setAlignmentX(Component.LEFT_ALIGNMENT);  // Set left alignment

    JPanel N_SnapshotPanel = new JPanel();
    N_SnapshotPanel.add(numberOfSnapshotsLabel);
    N_SnapshotPanel.add(numberOfSnapshotsField);
    N_SnapshotPanel.setAlignmentX(Component.LEFT_ALIGNMENT);  // Set left alignment

    // Inside your code where you add components to Crp_video_panel
    frameRangePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    Crp_video_margin_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    N_SnapshotPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    makeTimeSeriesButton.setAlignmentX(Component.LEFT_ALIGNMENT);
    Crp_video_panel.setAlignmentX(Component.LEFT_ALIGNMENT);

    Crp_video_panel.add(frameRangePanel);
    Crp_video_panel.add(Crp_video_margin_panel);
    Crp_video_panel.add(N_SnapshotPanel);
    Crp_video_panel.add(makeTimeSeriesButton);



    // Add a margin around the Crp_video_panel
    int margin = 15; // You can adjust this value to control the margin size
    Crp_video_panel.setBorder(BorderFactory.createEmptyBorder(margin, margin, margin, margin));
    Crp_video_panel.setLayout(new BoxLayout(Crp_video_panel, BoxLayout.Y_AXIS));

    dialog.add(Crp_video_panel);

    dialog.pack();
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);


    makeTimeSeriesButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            // Handle the button click event (e.g., make a time series)
            // Access the number of snapshots from the text field
            String numberOfSnapshotsText = numberOfSnapshotsField.getText();
            int numberOfSnapshots = Integer.parseInt(numberOfSnapshotsText);

            // Extract frame_start and frame_end from frameRangeTextField
            String frameRangeText = frameRangeTextField.getText();
            String[] frameRangeParts = frameRangeText.split("-");
            if (frameRangeParts.length == 2) {
                try {
                    int frame_start = Integer.parseInt(frameRangeParts[0]);
                    int frame_end = Integer.parseInt(frameRangeParts[1]);

                    // Close the dialog when done
                    makeTimeSeries(croppedImagePlus_final, numberOfSnapshots, frame_start, frame_end, number_of_slices);
                    dialog.dispose();
                } catch (NumberFormatException ex) {
                    // Handle parsing errors here
                    JOptionPane.showMessageDialog(dialog, "Invalid frame range format", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Handle invalid format here
                JOptionPane.showMessageDialog(dialog, "Invalid frame range format", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    });


    dialog.setVisible(true);
}



void makeTimeSeries(ImagePlus stack, int number_of_snapshots, int frame_start, int frame_end, int stack_frames) {
    
    
    int N_channels = stack.getNChannels();
    int number_of_slices = frame_end - frame_start ;
    int frame_width = stack.getWidth();
    int frame_height = stack.getHeight();
    
    // Read the margin size from the JTextField
    int margin = Integer.parseInt(time_serires_marginTextField.getText());
    
    int text_margin = 90;
    String frameIntervalText = frameIntervalTextField.getText();
    double frame_interval = Double.parseDouble(frameIntervalText);

    // Calculate the dimensions of the time series panel
    int timeSeriesWidth = frame_width + 20 + text_margin;
    int timeSeriesHeight = (frame_height + margin) * (number_of_snapshots + 1) + margin;
    
    // Create a new RGB image for the time series panel
    ImagePlus timeSeriesImage = NewImage.createRGBImage("Time Series", timeSeriesWidth, timeSeriesHeight, 1, NewImage.FILL_WHITE);
    ImageProcessor timeSeriesProcessor = timeSeriesImage.getProcessor();
    

    for (int i = 0; i <= number_of_snapshots; i++) {
        int frameIndex = frame_start - 1 + i * (number_of_slices / number_of_snapshots);
        if (frameIndex > stack_frames) {
            break;
        }

        // Copy the frame at the specified index
        ImagePlus copied_frame = new Duplicator().run(stack, 1, N_channels, 1, 1, frameIndex, frameIndex);
        IJ.run(copied_frame, "RGB Color", "");

        int x = text_margin;
        int y = margin + i * (frame_height + margin);

        timeSeriesProcessor.insert(copied_frame.getProcessor(), text_margin, y);

        double timepoint = frameIndex * frame_interval;
        String timepointText = String.format("t = %.1f s", timepoint);

        int textX = 10;
        int textY = y + (frame_height + timeSeriesProcessor.getFontMetrics().getHeight()) / 2;

        Font font = new Font("Arial", Font.PLAIN, 14);
        Color textColor = Color.BLACK;
        timeSeriesProcessor.setFont(font);
        timeSeriesProcessor.setColor(textColor);
        timeSeriesProcessor.drawString(timepointText, textX, textY);
    }

    timeSeriesImage.show();
}





private String[] readChannelData(String path, int totalChannels) {
    try {
        File file = new File(path);

        String[] channelData = new String[totalChannels + 1]; // +1 for "Merged"
        for (int i = 1; i <= totalChannels; i++) {
            channelData[i - 1] = "channel " + i;
        }
        channelData[totalChannels] = "Merged";

        if (!file.exists()) {
            //IJ.error("Channel data file not found: " + path);
            return channelData;
        }

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        //String[] channelData = new String[totalChannels + 1]; // +1 for "Merged"
        int channelIndex = 0;

        while ((line = reader.readLine()) != null && channelIndex <= totalChannels) {
            if (line.startsWith("Channel")) {
                channelData[channelIndex] = line;
                channelIndex++;
            }
        }

        reader.close();
        return channelData;
    } catch (IOException e) {
        e.printStackTrace();
        //IJ.error("Error reading channel data from file: " + e.getMessage());
        return new String[totalChannels];
    }
}



private String getChannelText(String channelLine) {
    if (channelLine == null) {
        return "";
    }
    int colonIndex = channelLine.indexOf(":");
    if (colonIndex >= 0) {
        return channelLine.substring(colonIndex + 1).trim();
    }
    return channelLine;
}

private String getChannelActivationString(int currentChannel, int totalChannels) {
    StringBuilder channelActivation = new StringBuilder();
    for (int i = 1; i <= totalChannels; i++) {
        if (i == currentChannel) {
            channelActivation.append("1");
        } else {
            channelActivation.append("0");
        }
    }
    return channelActivation.toString();
}

@Override
public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    if ("Add Scale Bar".equals(command)) {
        addScaleBar();
    } else if (e.getSource() == addTimeBarButton) {
        addTimeBar();
    } else if ("1.5x".equals(command)) {
        apply1_5xPixelSize();
    } else if ("Calculate Speed".equals(command)) {
        calculateSpeed();
    }
}

private void calculateSpeed() {
    // Get the user-entered frame interval (in seconds)
    String frameIntervalText = frameIntervalTextField.getText();
    double frameInterval = Double.parseDouble(frameIntervalText);

    // Get the user-entered pixel size (in microns)
    String pixelSizeText = pixelSizeTextField.getText();
    double pixelSize = Double.parseDouble(pixelSizeText);

    // Get the selected time unit
    String selectedTimeUnit = (String) timeUnitDropdown.getSelectedItem();

    // Get the ROI from the active image
    Roi roi = kymograph.getRoi();

    if (roi == null) {
        IJ.showMessage("Error", "No ROI is defined.");
        return;
    }

    // Get the coordinates of the ROI
    int xCoordinate = (int) roi.getXBase();
    int yCoordinate = (int) roi.getYBase();
    int roiWidth = (int) roi.getFloatWidth();
    int roiHeight = (int) roi.getFloatHeight();

    // Calculate time based on frame interval
    double time = roiHeight * frameInterval;

    // Calculate distance based on user-entered pixel size
    double distance = roiWidth * pixelSize;

    // Calculate speed in microns per second
    double speedMicronsPerSecond = distance / time;

    // Convert speed to the appropriate time unit (seconds or minutes)
    double speedValue;
    String speedUnit;

    if ("min".equals(selectedTimeUnit)) {
        // Convert to minutes if the selected unit is "min"
        speedValue = speedMicronsPerSecond * 60.0;
        speedUnit = "µm/min";
    } else {
        // Use seconds as the default unit
        speedValue = speedMicronsPerSecond;
        speedUnit = "µm/s";
    }

    // Display the calculated speed in the chosen time unit
    DecimalFormat decimalFormat = new DecimalFormat("0.####"); // Format to four decimal places
    String speedText = decimalFormat.format(speedValue) + " " + speedUnit;
    IJ.showMessage("Speed Calculation", "Speed: " + speedText);
}






private void apply1_5xPixelSize() {
    // Get the current pixel size from the text field
    String pixelSizeText = pixelSizeTextField.getText();
    double currentPixelSize = Double.parseDouble(pixelSizeText);

    // Calculate the new pixel size (divide by 1.5)
    double newPixelSize = currentPixelSize / 1.5;

    // Update the pixel size text field
    pixelSizeTextField.setText(String.valueOf(newPixelSize));

    // Apply the new calibration to the image (if available)
    ImagePlus imp = WindowManager.getCurrentImage();
    if (imp != null) {
        Calibration calibration = imp.getCalibration();
        calibration.pixelWidth = newPixelSize;
        imp.setCalibration(calibration);
    }
}

private void addScaleBar() {


    String pixelSizeText = pixelSizeTextField.getText();
    double pixelSize = Double.parseDouble(pixelSizeText);

    String scaleBarLengthText = scaleBarLengthTextField.getText();
    double scaleBarLengthMicron = Double.parseDouble(scaleBarLengthText);

    if(scaleBarPanel!=null){
        compositePanel.remove(scaleBarPanel);
    }
    if (scaleBarLengthMicron == 0 ){
        compositePanel.remove(scaleBarPanel);
        // Repaint the compositePanel
        compositePanel.revalidate();
        compositePanel.repaint();
    }
    else {
        // Calculate the width of the scale bar in pixels
        int barWidthPixels = (int) Math.round(scaleBarLengthMicron / pixelSize);

        BufferedImage scaleBarImage = new BufferedImage(barWidthPixels, 4, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaleBarImage.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, barWidthPixels, 2);

        // Create a JLabel to display the scale bar image
        scaleBarLabel = new JLabel(new ImageIcon(scaleBarImage));

        // Create a label to display the scale bar length in microns
        JLabel scaleBarLengthLabel = new JLabel(scaleBarLengthText + " µm ");

        // Create a panel to hold both the scale bar and the length label
        scaleBarPanel = new JPanel();
        scaleBarPanel.setLayout(new BoxLayout(scaleBarPanel, BoxLayout.X_AXIS));
        scaleBarPanel.add(Box.createHorizontalGlue()); // Right-align

        // Add the components to the scaleBarPanel
        scaleBarPanel.add(Box.createRigidArea(new Dimension(5, 0))); // Add some spacing
        scaleBarPanel.add(scaleBarLengthLabel);
        scaleBarPanel.add(scaleBarLabel);

        // Add the scaleBarPanel to the compositePanel
        compositePanel.add(scaleBarPanel, BorderLayout.SOUTH);

        // Repaint the compositePanel
        compositePanel.revalidate();
        compositePanel.repaint();
    }
}





private void addTimeBar() {
    // Check if timeBarPanel already exists
    String timeBarLengthText = timeBarLengthTextField.getText();
    timeBarLength = Double.parseDouble(timeBarLengthText);
        
    if(timeBarPanel!=null){
    CompsPanelParent.remove(timeBarPanel);
    }
    if (timeBarLength == 0 ){
        // Repaint the compositePanel
        compositePanel.revalidate();
        compositePanel.repaint();
    }
    else {
        // Create and initialize timeBarPanel only if it doesn't exist

        // Get the values from the textfields
        String frameIntervalText = frameIntervalTextField.getText();
        String selectedTimeUnit = (String) timeUnitDropdown.getSelectedItem();

        // Convert text values to appropriate numeric types
        double frameInterval = Double.parseDouble(frameIntervalText);
        double origTimeBarLength = timeBarLength;
        DecimalFormat decimalFormat = new DecimalFormat("0.#"); // 0.# means up to 1 decimal place, but no trailing .0

        // Convert the time bar length to seconds if the selected unit is "min"
        if ("min".equals(selectedTimeUnit)) {
            timeBarLength *= 60;
        }

        // Calculate the width of the time bar (4 pixels) and height based on frame interval and time bar length
        int timeBarWidth = 4;
        int timeBarHeight = (int) (timeBarLength / frameInterval);

        BufferedImage timeBarImage = new BufferedImage(timeBarWidth, timeBarHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = timeBarImage.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, timeBarWidth, timeBarHeight);

        timeBarLabel = new JLabel(new ImageIcon(timeBarImage));
        // Format the original time bar length as a string with no .0
        String formattedOrigTimeBarLength = decimalFormat.format(origTimeBarLength);
        JComponent verticalTextLabel = createVerticalTextLabel(formattedOrigTimeBarLength + " " + selectedTimeUnit);

        // Create a panel to hold both the time bar and time text
        timeBarPanel = new JPanel();
        timeBarPanel.setLayout(new FlowLayout(FlowLayout.CENTER , 0, 0)); // Center-align components vertically and push to top
        // Add a rigid component to create space above the timeBarPanel
        //timeBarPanel.setLayout(null);
        timeBarPanel.add(Box.createVerticalStrut(100)); // Adjust the height as needed

        timeBarPanel.add(timeBarLabel);
        timeBarPanel.add(verticalTextLabel);


        timeBarPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Store the initial mouse coordinates when clicked
                mouseX = e.getX();
                mouseY = e.getY();
                isDragging = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDragging) {
                    isDragging = false;

                    time_bar_location_changed = true ;

                    // Calculate the new position of the timeBarPanel only in the Y direction
                    int deltaY = e.getY() - mouseY;
                    int newY = timeBarPanel.getY() + deltaY;

                    // Set the new position only in the Y direction
                    timeBarPanel.setLocation(timeBarPanel.getX(), newY);

                                    // Store the new position in instance variables
                    timeBarPanelX = timeBarPanel.getX();
                    timeBarPanelY = newY;
                }
            }
        });


        timeBarPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging) {
                    // Calculate the new position of the timeBarPanel only in the Y direction
                    int deltaY = e.getY() - mouseY;
                    int newY = timeBarPanel.getY() + deltaY;
                    int minY = kymograph.getHeight() - compositePanel.getHeight() + 5;
                    int maxY = (int) compositePanel.getHeight() - Math.round(timeBarPanel.getHeight()/2);
                    // Limit the vertical movement to keep it within the compositePanel


                    if (newY < minY) { 
                        newY = minY;
                        } 
                    else if (newY > maxY) {
                        newY = maxY;
                        }
                    // Set the new position only in the Y direction
                    timeBarPanel.setLocation(timeBarPanel.getX(), newY);

                    // Update the stored mouse Y coordinate
                    mouseY = e.getY();
                }
            }
        });

        // Add the time bar panel to the composite panel

        CompsPanelParent.add(timeBarPanel);

    }
    
    
    // Repaint the compositePanel
    CompsPanelParent.revalidate();
    CompsPanelParent.repaint();
    


}


private void updateTimeBarLength(int newY) {
    // Calculate the new time bar length based on its position
    int timeBarHeight = timeBarPanel.getHeight();
    double frameInterval = Double.parseDouble(frameIntervalTextField.getText());
    String selectedTimeUnit = (String) timeUnitDropdown.getSelectedItem();

    // Convert newY to a time bar length
    double newTimeBarLength = (double) newY / timeBarHeight * frameInterval;

    // Update the time bar length text field
    timeBarLengthTextField.setText(String.valueOf(newTimeBarLength));

    // Convert the new time bar length to the selected time unit
    if ("min".equals(selectedTimeUnit)) {
        newTimeBarLength /= 60.0;
    }

    // Update the time bar length label
    DecimalFormat decimalFormat = new DecimalFormat("0.#");
    String formattedNewTimeBarLength = decimalFormat.format(newTimeBarLength);
    JComponent verticalTextLabel = createVerticalTextLabel(formattedNewTimeBarLength + " " + selectedTimeUnit);
    timeBarPanel.remove(1); // Remove the existing label
    timeBarPanel.add(verticalTextLabel); // Add the updated label

    // Repaint the timeBarPanel
    timeBarPanel.revalidate();
    timeBarPanel.repaint();
}





private void saveImage(ImagePlus imp, String ImagePath) {
    // Get the directory path for saving the image

    if (ImagePath == null || ImagePath.isEmpty()) {
        // If the directory of the original image is not available, ask the user to choose a directory
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = fileChooser.showDialog(frame, "Choose Directory");
        if (result == JFileChooser.APPROVE_OPTION) {
            ImagePath = fileChooser.getSelectedFile().getAbsolutePath();
        } else {
            IJ.showMessage("Error", "Directory not selected. Image not saved.");
            return;
        }
    }

    
    String channel_data_directory = imageDirectory + File.separator + "Kymograph";

    // Create the save directory if it doesn't exist
    File directory = new File(ImagePath);
    if (!directory.exists()) {
        directory.mkdirs();
    }

        File ch_directory = new File(channel_data_directory);
    if (!ch_directory.exists()) {
        ch_directory.mkdirs();
    }

    // Generate a unique file name
    String imageName = imp.getShortTitle() + "_Compositekymo_" + savedImageCount + ".tif";
    String channel_data_file = "channel_data.txt";

    File TXToutputFile = new File(channel_data_directory, channel_data_file);
    File outputFile = new File(ImagePath, imageName);
    FileWriter writer = null; // Declare FileWriter outside try-catch to ensure it's accessible

    try {
        if (channels >1){
        writer = new FileWriter(TXToutputFile);
        for (int c = 1; c <= channels; c++) {
            // Get the text field for the current channel
            JPanel channelPanel = (JPanel) ImgParentPanel.getComponent(c - 1);
            JPanel imagePanel = (JPanel) channelPanel.getComponent(0); // Assuming imagePanel is the first component
            JTextField textField = (JTextField) imagePanel.getComponent(1); // Assuming text field is the first component

            // Get the text from the text field
            String channelText = textField.getText();

            // Write the channel data to the file
            writer.write("Channel " + c + ": " + channelText + "\n");
        }}

        // Check if the file already exists, incrementing savedImageCount if needed
        while (outputFile.exists()) {
            savedImageCount++;
            imageName = imp.getShortTitle() + "_Compositekymo_" + savedImageCount + ".tif";
            outputFile = new File(ImagePath, imageName);
        }

        compositePanel.remove(imageCanvas);
        // Duplicate the composite view and add to the panel with editable "Merged" textbox
        ImagePlus compositeImage = kymograph.duplicate();
        BufferedImage compositeBufferedImage = compositeImage.getBufferedImage();
        compositeLabel = new JLabel(new ImageIcon(compositeBufferedImage));
        compositeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        compositePanel.add(compositeLabel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
        if (time_bar_location_changed){
        timeBarPanel.setLocation(timeBarPanelX, timeBarPanelY);}

        // Capture the content of imgFramePanel as an image
        BufferedImage imgFrameImage = new BufferedImage(imgFramePanel.getWidth(), imgFramePanel.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imgFrameImage.createGraphics();
        imgFramePanel.paint(g2d);
        g2d.dispose();
        ImagePlus tempImagePlus = new ImagePlus("Graphics Image", new ColorProcessor(imgFrameImage));

        // Add Scale Bar Length and Time Bar information to the image metadata
        IJ.run(imp, "Properties...", "unit=µm pixel_width=" + pixelSizeTextField.getText() + " Info=[" +
                scaleBarLengthTextField.getText() + " " + timeBarLengthTextField.getText() + " " +
                timeUnitDropdown.getSelectedItem() + "]");

        if (ImagePath != null && !ImagePath.isEmpty()) {
        FileSaver fileSaver = new FileSaver(tempImagePlus);
        String imagePath = ImagePath + File.separator + imageName;

        boolean saved = fileSaver.saveAsTiff(imagePath);

        // Assuming ImagePath is a String containing the path to the image directory
        String parentDirectoryPath = new File(ImagePath).getParent();

        Roi activeRoi = imp.getRoi();
        // Assuming you have already added ROIs to the ROI Manager
        RoiManager roiManager = RoiManager.getRoiManager();
        roiManager.addRoi(activeRoi);
        if (roiManager != null && roiManager.getCount() > 0) {
            // Define a file name for the ROI file
            String roiFileName = imp.getShortTitle() + "_Compositekymo_" + savedImageCount + "_ROI.roi";

            // Create a File object for the ROI file in the same directory as the image
            File roiFile = new File(ImagePath, roiFileName);

            // Save the ROIs using the ROI Manager
            roiManager.save(ImagePath + File.separator + roiFileName);
            
            //IJ.showMessage("ROIs Saved", "ROI saved as:\n" + roiFile.getAbsolutePath());
            
            // Close the ROI Manager
            roiManager.close();
        } else {
            IJ.showMessage("Error", "No ROIs in the ROI Manager to save.");
        }


        if (saved) {
            //IJ.showMessage("Image Saved", "Image saved as:\n" + imagePath);
        } else {
            IJ.showMessage("Error", "An error occurred while saving the image.");
        }
        
        // Close the ImagePlus after saving
        tempImagePlus.close();
    }
    } catch (IOException e) {
        e.printStackTrace();
        IJ.showMessage("Error", "An error occurred while writing channel data to the txt file.");
    } finally {
        // Close the FileWriter in a finally block to ensure it's always closed
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
                IJ.showMessage("Error", "An error occurred while closing the file writer.");
            }
        }
    }
    compositePanel.remove(compositeLabel);
    compositePanel.add(imageCanvas, BorderLayout.CENTER);
    // frame.revalidate();
    // frame.repaint();

}




private double readFrameIntervalFromMetadata(ImagePlus imp) {

        // Check if the frame interval is available in the metadata
        String metadata  = imp.getInfoProperty();

        if (metadata != null) {
            

            // Split the metadata into lines
            String[] lines = metadata.split("\n");

            // Initialize default frame interval value
            double frameInterval = 1.0; // Default value if not found

            // Search for the frame interval in the lines
            for (String line : lines) {
                if (line.startsWith("Frame interval (s) ")) {
                    // Extract the frame interval value
                    String[] parts = line.split("=");
                    if (parts.length == 2) {
                        String frameIntervalStr = parts[1].trim();
                        try {
                            frameInterval = Double.parseDouble(frameIntervalStr);
                        } catch (NumberFormatException e) {
                            // Handle parsing error if needed
                        }
                    }
                    return frameInterval; // Return the found frame interval
                }
                else if (line.startsWith("Frame interval")) {
                    // Extract the frame interval value
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        String frameIntervalStr = parts[1].trim();
                        try {
                            frameInterval = Double.parseDouble(frameIntervalStr);
                        } catch (NumberFormatException e) {
                            // Handle parsing error if needed
                        }
                    }
                    return frameInterval; // Return the found frame interval
                }
            }
        }
    

    return 1.0; // Return the default value if not found or any error occurs
}


// Create a custom component to display vertical text
private JComponent createVerticalTextLabel(String text) {
    JPanel panel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.rotate(Math.toRadians(-90), getWidth() / 2.0, getHeight() / 2.0);
            g2d.drawString(text, 0 ,  getHeight()/2 );
        }
    };
    panel.setPreferredSize(new Dimension(30, 200)); // Adjust the size as needed
    return panel;
}


private void saveKymograph(ImagePlus kymoImp, String saveDirectory) {
    // Code to save the kymograph ImagePlus
    FileSaver fileSaver = new FileSaver(kymoImp);
    String fileName = kymoImp.getShortTitle() + "_kymo_" + savedImageCount + ".tif";
    String filePath = saveDirectory + File.separator + fileName;

    // Create the save directory if it doesn't exist
    File directory = new File(saveDirectory);
    if (!directory.exists()) {
        directory.mkdirs();
    }

    // Check if the file already exists, incrementing savedImageCount if needed
    File outputFile = new File(filePath);
    while (outputFile.exists()) {
        savedImageCount++;
        fileName = kymoImp.getShortTitle() + "_kymo_" + savedImageCount + ".tif";
        filePath = saveDirectory + File.separator + fileName;
        outputFile = new File(filePath);
    }

    boolean saved = fileSaver.saveAsTiff(filePath);

    if (saved) {
        IJ.showMessage("Kymograph Saved", "Kymograph saved as:\n" + filePath);
    } else {
        IJ.showMessage("Error", "An error occurred while saving the kymograph.");
    }
}


}
