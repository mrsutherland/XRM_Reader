/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.sutherland.michael.imagej;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
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
        // width
		int width;
        try {
        	DirectoryEntry imageInfo = (DirectoryEntry)root.getEntry("ImageInfo");
            DocumentEntry document = (DocumentEntry)imageInfo.getEntry("ImageWidth");
            DocumentInputStream stream = new DocumentInputStream(document);
	        width = stream.readInt();
	        stream.close();
		} catch (IOException e) {
			IJ.error("Couldn't find ImageInfo/ImageWidth in XRM file: "+filename+" "+e.toString());
			return;
		}
        
        // height
        int height;
        try {
        	DirectoryEntry imageInfo = (DirectoryEntry)root.getEntry("ImageInfo");
            DocumentEntry document = (DocumentEntry)imageInfo.getEntry("ImageHeight");
        	DocumentInputStream stream = new DocumentInputStream(document);
	        height = stream.readInt();
	        stream.close();
		} catch (IOException e) {
			IJ.error("Couldn't find ImageInfo/ImageHeight in XRM file: "+filename+" "+e.toString());
			return;
		}
	    
        // data type
        int type;
        try {
        	DirectoryEntry imageInfo = (DirectoryEntry)root.getEntry("ImageInfo");
        	DocumentEntry document = (DocumentEntry)imageInfo.getEntry("DataType");
        	DocumentInputStream stream = new DocumentInputStream(document);
        	type = stream.readInt();
        	stream.close();
        } catch (IOException e) {
			IJ.error("Couldn't find ImageInfo/DataType in XRM file: "+filename+" "+e.toString());
			return;
		}
        
        // actual data
        try {
        	DirectoryEntry imageInfo = (DirectoryEntry)root.getEntry("ImageData1");
        	DocumentEntry document = (DocumentEntry)imageInfo.getEntry("Image1");
        	DocumentInputStream stream = new DocumentInputStream(document);
            if (type == FLOAT_TYPE) {
                // float
            	float[] data = new float[width*height];
            	for (int i = 0; i < width*height; i++) {
            		byte[] bytes = new byte[4]; // space to store float
            		stream.read(bytes);
            		data[i] = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
            	}
            } else if (type == INT16_TYPE) {
            	short[] data = new short[width*height];
            	for (int i = 0; i < width*height; i++) {
        			data[i] = stream.readShort();
            	}
                ShortProcessor proc = new ShortProcessor(width, height, data, null); 
                ImagePlus imp = new ImagePlus("XRM Image", proc); 
                imp.show();             	
            } else {
            	IJ.error("Unknown data type in file: "+filename);
            }
        	stream.close();
        } catch (IOException e) {
			IJ.error("Couldn't find ImageInfo/DataType in XRM file: "+filename+" "+e.toString());
			return;
		}
	}
}
