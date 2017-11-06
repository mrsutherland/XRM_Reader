This is an Maven project implementing an ImageJ 1.x plugin to load XRM files.

The .xrm file extension is used by Zeiss Xradia on their X-ray microscopes.  This file format 
uses Microsoft OLE2 to store images and metadata.  This plugin allows ImageJ to directly load
the images.

To Build:
1) Load into eclipse for your favorite IDE as a maven project.
2) Build jar

To Install:
1) Copy XRM_Reader-*.jar into ImageJ/plugins folder (or Fiji.app\plugins)
2) Copy poi-3.7.jar into ImageJ/jre/lib/ext folder (or Fiji.app\java\win64\jdk1.8.0_66\jre\lib\ext)

To Run:
Go to File>Import>XRM... and select the xrm file you'd like to load.
