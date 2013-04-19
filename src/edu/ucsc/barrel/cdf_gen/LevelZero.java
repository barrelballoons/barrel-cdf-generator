package edu.ucsc.barrel.cdf_gen;

/*
LevelZero.java v13.01.18

Description:
   Copies each data file, byte by byte, to a day-long data file.
   Rejects short frames, long frames, and frames with bad checksums.

   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   This file is part of The BARREL CDF Generator.

   The BARREL CDF Generator is free software: you can redistribute it and/or 
   modify it under the terms of the GNU General Public License as published 
   by the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   The BARREL CDF Generator is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License along with 
   The BARREL CDF Generator.  If not, see <http://www.gnu.org/licenses/>.
   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Change Log:
   v13.01.18
      -Updated the filename format

   v12.11.26
      -Does not set payload directory in outpath here.

   v12.11.20
      -Changed references to Level_Generator to CDF_Gen
      -changed text of output filename to lowercase

   v12.11.05
      -Now uses "getDataSet()" method of Level_Generator
      to add frames to the correct set

   v12.10.11
      -Changed version numbers to a date format
      -No longer interacts with L1 objects
      -Sends each frame as a BigInteger to the DatHolder static object. 
      
   v0.6
      -Changed how output files are stored
      -Added error checking for short or long frames and bad checksums
      -Sends data to Level One object

   v0.5
      -Fixed problem where the hex buffer would clear before using anything that 
        was stored in it

   v0.4
      -Changed constructor to take in date and payload as well
      -Creates files that are of BAR0_YYYYMMDD_SLL_PP_V++.TLM format
      -Fixed incorrect byte counts introduced by buffered read

   v0.3
      -Updated class/filename and added proper package scheme
      -Added constructor so frame properties can be set in the ini file
      -Constructor configures objects
      -No longer static class
      
   v0.2
      -Added buffered and write for binary files.
      -Output file is now sorted correctly.
      -No longer copies the dailyManifest file into the output.
      
   v0.1
      -Just copies the files into a day-long file. 
      -Does not check for valid frames.

Future Plans: 
   -Read launch site/order data from downloaded file names. 
      Can not do this until the original telemetry files start being saved with
      the new file name format.
   -Clean up/ Documentation
   -Add logging
*/

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Arrays;

public class LevelZero{
   
	//declare members
   private String syncWord;
   private int frameLength;
   private String inputPath;
   private String outputPath;
   private String outName;
   private String[] fileList;
   private String revNum;
   private OutputStream outFile;
   
   public LevelZero(
      DataHolder data,
      final int length, 
      final String sync, 
      final String inputDir, 
      final String outputDir,
      final String p,
		final String f,
		final String s,
		final String d
   ){
	   //set object properties
      syncWord = sync;
      frameLength = length;
      inputPath = inputDir;
      outputPath = outputDir;
      
		//get file revision number
      if(CDF_Gen.getSetting("rev") != null){
         revNum = CDF_Gen.getSetting("rev");
      }
		
      //set get a list of input files
      File tempDir = new File(inputPath);
      fileList = tempDir.list();
      Arrays.sort(fileList);
      
      //make sure the output directory exists
      tempDir = new File(outputDir + "/");
      if(!tempDir.exists()){tempDir.mkdirs();}
      
      //set output file name
		outName =
			"bar1" + f + "_" + p + "_" + s +
			"_l0_20" + d +  "_v" + revNum + ".tlm";
   }
   
   public void processRawFiles() throws IOException{
      FileInputStream readFile;
      byte[] bytes;
      StringBuilder hexBuffer;
      
      //keeps track of how many total bytes are transfered
      long byteCount = 0;
      
      //index list of decimal to hex values
      String[] hexSymbols = { 
         "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", 
         "A", "B", "C", "D", "E", "F" 
      };
      
      //create output file
      System.out.println("Generating day-long file...");
      outFile = new FileOutputStream(outputPath + "/" + outName);
      
      //convert each input file to a string, 
      // check it for errors, and save good frames
      for(String file_i : fileList){
         if(!(file_i.equals("dailyManifest"))){ //ignore the manifest file
            //create new buffers to create hex string with
            bytes = new byte[fileList.length];
            hexBuffer = new StringBuilder(bytes.length * 2);
            
            readFile = new FileInputStream(new File(inputPath + "/" + file_i));
            
            for(int value = 0 ; value != -1; value = readFile.read(bytes)){
               //increment byte counter
               byteCount += value;
               
               //for each byte in the buffer, convert to nybbles and lookup hex
               for(int i = 0; i < value; i++){
                  byte leftSymbol = (byte)((bytes[i] >>> 4) & 0x0f);
                  byte rightSymbol = (byte)(bytes[i] & 0x0f);
                  
                  //create a string of all hex values in this buffer-
                  hexBuffer.append(
                     hexSymbols[leftSymbol] + hexSymbols[rightSymbol]
                  );
               }
            }
            
            //Process the hex frames
            splitFrames(hexBuffer.toString(), file_i);
            
            if (readFile != null) {
               readFile.close();  
            }   
         }
      }
      
      System.out.println("Tranfered " + byteCount + " bytes to " + outName);
   }
   
   private void splitFrames(String hexBuff, String file) throws IOException{
      //need to work on salvaging partial frames due to buffer misalignment 
      //and syncwords in the data
      
      String[] frames;
      
      frames = hexBuff.split(syncWord);
      
      for(int frame_i = 0; frame_i < frames.length; frame_i++){
         
         //make sure we have something to work with
         if(frames[frame_i].length() == 0){continue;}
         
         //add sync word back into the frame
         frames[frame_i] = syncWord + frames[frame_i];
         
         //find any extra or shortage in frame length
         //Multiply by 2 because hex format gives 1 char per nybble
         int diff = frames[frame_i].length() - (frameLength * 2);
         
         //Make sure the frame length is correct
         if(diff == 0){
            processFrame(frames[frame_i], file);
         }else if(diff < 0){ //frame is too short
            //Try to build a full length frame from adjacent pieces
            String tempFrame = frames[frame_i];
            int frame_j = frame_i;
            
            while(
                  ((frame_j + 1) < frames.length) && 
                  (tempFrame.length() < (frameLength *2)) 
               
            ){
               frame_j++;
               tempFrame = tempFrame + syncWord + frames[frame_j];
            }
            
            if(tempFrame.length() == (frameLength * 2)){
               frame_i = frame_j;
               processFrame(tempFrame, file);
            }else{
               System.out.println("Short Frame in file " + file + ".");
            }
         }else if(diff > 0){//frame is too long
            //Long frames wont be saved so move on
            System.out.println("Long Frame in file " + file + ".");
         }
      }

      //remove gaps from data structure after all frames were added.
//      CDF_Gen.getDataSet().finalizeFrames();
   }
   
   private void processFrame(String frame, String fileName) throws IOException{
      BigInteger binFrame = new BigInteger(frame, 16);
      
      //check that checksum
      int sum = 0;
      
      int cksm = Integer.parseInt(
         frame.substring((frame.length() - 4), frame.length()), 16
      );
      for(int word_i = 0; word_i < (frame.length() - 4); word_i += 4){
         sum += Integer.parseInt(frame.substring(word_i, (word_i + 4)), 16);
      }
      if(cksm == (0xffff & sum)){ 
         //checksum passed!
         //write to level zero file
         outFile.write(CDF_Gen.hexToByte(frame));
         
         //add frame to data object
         CDF_Gen.getDataSet().addFrame(binFrame);
         
         //try{
         //   //save hex frame to the day-long string
         //   CDF_Gen.L1.extractFrame(frame);
         //}catch(CDFException ex){
         //   System.out.println(ex.getMessage());
         //}
      }else{
         System.out.println("Checksum Failed for frame in  " + fileName + "!");
      }
   }
   
   //close the output file when done and 
   //return a string of all the frames' hex data
   public void finish() throws IOException{
      if (outFile != null) {
         outFile.close();
         //try{
         //   CDF_Gen.L1.finalize();
         //}catch(CDFException ex){
         //   System.out.println(ex.getMessage());
         //}
      }
   }
}
