/*
 * To the extent possible under law, Michael Sutherland has waived all
 * copyright and related or neighboring rights to this plugin code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.nullsquared.imagej;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import ij.io.OpenDialog;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;

/**
 * A plugin for loading xrm files into ImageJ.  
 * These are proprietary files for Zeiss Xradia XRay microscopes.
 *
 * @author Michael Sutherland
 */
public class XRM_Reader implements PlugIn {

    // -- Constants --
    private static final int FLOAT_TYPE = 10;
    private static final int INT16_TYPE = 5;    
    
    @Override
    public void run(String args) {
        OpenDialog od = new OpenDialog("Load XRM","","");
        String filename = od.getPath();
        String short_filename = od.getFileName();
        
        POIFSFileSystem fs;
        try
        {
            InputStream inputStream = new FileInputStream(filename);
            fs = new POIFSFileSystem(inputStream);
        }
        catch (IOException e)
        {
            IJ.error("Error reading XRM file: "+filename);
            return;
        }
        DirectoryEntry root = fs.getRoot();            
        // read width, height, data type, and number of images
        int width;
        int height;
        int type;
        int numberOfImages = 1;
        try {
            DirectoryEntry imageInfo = (DirectoryEntry)root.getEntry("ImageInfo");
            //width
            DocumentEntry document = (DocumentEntry)imageInfo.getEntry("ImageWidth");
            DocumentInputStream stream = new DocumentInputStream(document);
            width = stream.readInt();
            stream.close();
            //height
            document = (DocumentEntry)imageInfo.getEntry("ImageHeight");
            stream = new DocumentInputStream(document);
            height = stream.readInt();
            stream.close();
            // data type
            document = (DocumentEntry)imageInfo.getEntry("DataType");
            stream = new DocumentInputStream(document);
            type = stream.readInt();
            stream.close();
            // number of images
            document = (DocumentEntry)imageInfo.getEntry("NoOfImages");
            stream = new DocumentInputStream(document);
            numberOfImages = stream.readInt();
            stream.close();
        } catch (IOException e) {
            IJ.error("Couldn't read parameter in XRM file: "+filename+" "+e.toString());
            return;
        }   
        
        // actual data
        // create stack even if there is only a single image (will open correctly).
        ImageStack stack = new ImageStack(width, height, numberOfImages);
        // NOTE: ImageStack and XRM directory naming are both one indexed
        for (int imageNumber=1; imageNumber <= numberOfImages; imageNumber++)
        {
            try {
            	// NOTE: ImageData# increments every 100
                DirectoryEntry imageInfo = (DirectoryEntry)root.getEntry("ImageData"+((int)Math.ceil(imageNumber/100.0)));
                DocumentEntry document = (DocumentEntry)imageInfo.getEntry("Image"+(imageNumber));
                DocumentInputStream stream = new DocumentInputStream(document);
                ImageProcessor proc = null;
                if (type == FLOAT_TYPE) {
                    // float
                    float[] data = new float[width*height];
                    for (int i = 0; i < width*height; i++) {
                        byte[] bytes = new byte[4]; // space to store float
                        stream.read(bytes);
                        data[i] = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                    }
                    proc = new FloatProcessor(width, height, data, null); 
                } else if (type == INT16_TYPE) {
                    short[] data = new short[width*height];
                    for (int i = 0; i < width*height; i++) {
                        data[i] = stream.readShort();
                    }
                    proc = new ShortProcessor(width, height, data, null); 
                } else {
                    IJ.error("Unknown data type in file: "+filename);
                }
                stream.close();
            	stack.setProcessor(proc, imageNumber);
            } catch (IOException e) {
                IJ.error("Couldn't find ImageData1/Image1 in XRM file: "+filename+" "+e.toString());
                return;
            }
        }
        ImagePlus imp = new ImagePlus(short_filename, stack);
        imp.show();
    }
}
