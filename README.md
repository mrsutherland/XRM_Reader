This is an Maven project implementing an ImageJ 1.x plugin to load XRM, TXRM, and TXM files.

These file extension are used by Zeiss Xradia on their X-ray microscopes.  This file format 
uses Microsoft OLE2 to store images and metadata.  This plugin allows ImageJ to directly load
the images.

To Build from source:
1) Load into eclipse for your favorite IDE as a maven project.
2) Build jar

To Install:
1) Copy XRM_Reader-*.jar and poi-3.7.jar into ImageJ/plugins folder (or Fiji.app\plugins)
2) Restart ImageJ

To Run:
Go to File>Import>XRM... and select the xrm, txrm, or txm file you'd like to load.
