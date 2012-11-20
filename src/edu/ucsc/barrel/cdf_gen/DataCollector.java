package edu.ucsc.barrel.cdf_gen;

/*
dataCollector.java v12.10.11

Description:
   Downloads files from each server listed in ini file.
   Saves all files to a directory tree as ./DATAROOT/payloadX/DATE
v12.11.15
   -Changed references to Level_Generator to CDF_Gen
   
v12.10.11
   -Changed version numbers to a date format

v0.3
   -Changed how output files are stored

v0.2
	-updated class/filename and added proper package scheme
	-fixed resource leak caused by output stream not being closed
	-Uses a constructor to get information about output path, payload, servers 
      and date rather than having the calling class set it directly
   -No longer a static class

v0.1
   -Downloads all data files.
   
Planned Changes:
   -Add logging
*/


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;

public class DataCollector{
   private ArrayList<String> urls = new ArrayList<String>();
   private ArrayList<String> serverList = new ArrayList<String>();
   private String outRoot = ".";
   private String outDir = ".";
   private String outFile = "default";
   private String currentPayload;
   private int currentDate;
   
   public DataCollector(
      final String path, final ArrayList<String> servers, 
      final String payload, final int date
   ){
      outRoot = path;
      serverList = servers;
      currentPayload = payload;
      currentDate = date;
   }
   
   //Opens a stream from specified url and saves it to a local file. 
   //Local file is named with the with the last section of the url after the 
   // last "/".
   //Specified url should not have a trailing "/" 
   private void downloadFile(final String url){
	   try {
         System.out.println("Getting: " + url);
         
         URL dlFile = new URL(url);
         ReadableByteChannel rbc = Channels.newChannel(dlFile.openStream());
         
         FileOutputStream fos = new FileOutputStream(outDir + "/" + outFile);
         fos.getChannel().transferFrom(rbc, 0, 1 << 24);
         
         fos.close();
      }catch (MalformedURLException ex){
         System.out.println("Error Downloading File: " + ex.getMessage()); 
      }catch(IOException ex){
         System.out.println("Error Downloading File: " + ex.getMessage());
      }
   }
   
   //If the output directory exists, get a listing and delete each file
   private void testOutputDir(File tempDir){
      if(tempDir.exists()){
         String[] list = tempDir.list();
         for(String list_i : list){
            File tempFile = new File(outDir + "/" + list_i);
            tempFile.delete();
         }
      }else{
         tempDir.mkdirs();
      }
   }
   
   //Downloads the repository index, opens it and parses each file name.
   //File names and repo location are used to build url file list
   //Does this for each repository in the repo ArrayList
   public void getFileList(){
      //make sure the output directory exists and is empty
      outDir = outRoot + "/" + currentPayload + "/" + currentDate;
      testOutputDir(new File(outDir));
      
      for(String server_i : serverList){
         //repository entries are saved in the current directory 
         // and named "dailyManifest"
         outFile = "dailyManifest";
         downloadFile(
            server_i + "/cgi-bin/fileLister.pl?date=" + currentDate + 
            "&payload=payload" + currentPayload
         );
         
         //read the file manifest and add each file to the URL list
         try{
            FileReader fr = new FileReader(outDir + "/dailyManifest");
            BufferedReader manifest = new BufferedReader(fr);   
         
            String fileName;
            while((fileName = manifest.readLine()) != null){
               urls.add(
                  server_i + "/moc-nas/barrel/payload" + 
                  currentPayload + "/" + currentDate + "/dat/" + fileName
               );
            }
            manifest.close();
         }catch(IOException ex){
            System.out.println(
               "Could not read the file manifest: " + ex.getMessage()
            );
         }
      }
   }
   
   //Loop through the url list and download each file to 
   // the current date directory
   public void getFiles(){
      for(String url_i : urls){
         //each data file is named based on the url
         String[] temp = url_i.split("/");
         outFile = temp[temp.length - 1];
         
         downloadFile(url_i);
      }
   }
}
