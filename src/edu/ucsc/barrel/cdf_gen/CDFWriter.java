/*
CDFWriter.java

Description:
   Superclass for creating a set of CDF files.

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
*/

package edu.ucsc.barrel.cdf_gen;

import gsfc.nssdc.cdf.CDF;
import gsfc.nssdc.cdf.CDFException;
import gsfc.nssdc.cdf.CDFConstants;
import gsfc.nssdc.cdf.util.CDFTT2000;
import gsfc.nssdc.cdf.Variable;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.Vector;
import java.util.Arrays;

public abstract class CDFWriter implements CDFConstants, CDFFillerMethods{
   String
      id = "00",
      flt = "00",
      stn = "0",
      revNum = "00",
      outputPath = "";
   int today, yesterday, tomorrow;
   Calendar dateObj = Calendar.getInstance();
   
   private DataHolder data;
   
   public CDFWriter(
      final String d, final String p, final String f,
      final String s, final String dir, final String lvl
   ) throws IOException
   {
      //get file revision number
      if(CDF_Gen.getSetting("rev") != null){
         revNum = CDF_Gen.getSetting("rev");
      }
      
      //save input arguments
      id = p;
      flt = f;
      stn = s;
      today = Integer.valueOf(d);
      outputPath = dir;

      //calculate yesterday and tomorrow from today's date
      int year, month, day;
      year = today/10000;
      month = (today - (year * 10000)) / 100;
      day = today - (year * 10000) - (month * 100);
      dateObj.clear();
      dateObj.set(year, month - 1, day);
      dateObj.add(Calendar.DATE, -1);

      yesterday = 
         (dateObj.get(Calendar.YEAR) * 10000) + 
         ((dateObj.get(Calendar.MONTH) + 1) * 100) + 
         dateObj.get(Calendar.DATE);

      dateObj.add(Calendar.DATE, 2);

      tomorrow = 
         (dateObj.get(Calendar.YEAR) * 10000) + 
         ((dateObj.get(Calendar.MONTH) + 1) * 100) + 
         dateObj.get(Calendar.DATE);

      //get the data storage object
      data = CDF_Gen.data;
      
      //get data from DataHolder and save them to CDF files
      try{
         System.out.println(
            "Creating " + lvl  + "... (" + data.getSize("1Hz") + " frames)"
         );
      
         writeData();

         System.out.println("Created " + lvl + ".");
      }catch(CDFException ex){
         System.out.println(ex.getMessage());
      }
   }
   
   private void writeData() throws CDFException{
      File outDir;

      //make sure the needed output directories exist
      outDir = new File(outputPath + "/" + yesterday);
      if(!outDir.exists()){outDir.mkdirs();}
      outDir = new File(outputPath + "/" + today);
      if(!outDir.exists()){outDir.mkdirs();}
      outDir = new File(outputPath + "/" + tomorrow);
      if(!outDir.exists()){outDir.mkdirs();}

      //fill CDF files for yesterday, today, and tomorrow
      doAllCdf(yesterday);
      doAllCdf(today);
      doAllCdf(tomorrow);
   }

   private void doAllCdf(int date) throws CDFException{
      int first_i, last_i, size;
      long rec_date = 0;
      long[] tt2000_parts; 

      //find the first and last indicies for this day for the 1Hz file
      first_i = -1;
      size = data.getSize("1Hz");
      for(last_i = 0; last_i < size; last_i++){
         tt2000_parts = CDFTT2000.breakdown(data.epoch_1Hz[last_i]);
         rec_date = 
            tt2000_parts[2] + //day
            (100 * tt2000_parts[1]) + //month
            (10000 * (tt2000_parts[0] - 2000)); //year
         if(first_i == -1) {
            if(rec_date == date){
               //found the first_i index
               first_i = last_i;
            }
         }else if(rec_date > date){
            break;
         }
      }
      //make sure we have a valid start and stop index and 
      //that there are some records to process
      if(first_i != -1 && (last_i - first_i) > 0){
         doMiscCdf(first_i, last_i, date);
      }

      //...for the mod40 file
      first_i = -1;
      size = data.getSize("mod40");
      for(last_i = 0; last_i < size; last_i++){
         tt2000_parts = CDFTT2000.breakdown(data.epoch_mod40[last_i]);
         rec_date = 
            tt2000_parts[2] + //day
            (100 * tt2000_parts[1]) + //month
            (10000 * (tt2000_parts[0] - 2000)); //year
         if(first_i == -1) {
            if(rec_date == date){
               //found the first_i index
               first_i = last_i;
            }
         }else if(rec_date > date){
            break;
         }
      }
      if(first_i != -1 && (last_i - first_i) > 0){
         doHkpgCdf(first_i, last_i, date);  
      }

      //...for the mod32 file
      first_i = -1;
      size = data.getSize("mod32");
      for(last_i = 0; last_i < size; last_i++){
         tt2000_parts = CDFTT2000.breakdown(data.epoch_mod32[last_i]);
         rec_date = 
            tt2000_parts[2] + //day
            (100 * tt2000_parts[1]) + //month
            (10000 * (tt2000_parts[0] - 2000)); //year
         if(first_i == -1) {
            if(rec_date == date){
               //found the first_i index
               first_i = last_i;
            }
         }else if(rec_date > date){
            break;
         }
      }
      if(first_i != -1 && (last_i - first_i) > 0){
         doSspcCdf(first_i, last_i, date);  
      }

      //...for the mod4 file
      first_i = -1;
      size = data.getSize("mod4");
      for(last_i = 0; last_i < size; last_i++){
         tt2000_parts = CDFTT2000.breakdown(data.epoch_mod4[last_i]);
         rec_date = 
            tt2000_parts[2] + //day
            (100 * tt2000_parts[1]) + //month
            (10000 * (tt2000_parts[0] - 2000)); //year
         if(first_i == -1) {
            if(rec_date == date){
               //found the first_i index
               first_i = last_i;
            }
         }else if(rec_date > date){
            break;
         }
      }
      if(first_i != -1 && (last_i - first_i) > 0){
         doMspcCdf(first_i, last_i, date);
         doRcntCdf(first_i, last_i, date);  
         doGpsCdf(first_i, last_i, date);
      }

      //...for the 4Hz file
      first_i = -1;
      size = data.getSize("4Hz");
      for(last_i = 0; last_i < size; last_i += 4){
         tt2000_parts = CDFTT2000.breakdown(data.epoch_4Hz[last_i]);
         rec_date = 
            tt2000_parts[2] + //day
            (100 * tt2000_parts[1]) + //month
            (10000 * (tt2000_parts[0] - 2000)); //year
         if(first_i == -1) {
            if(rec_date == date){
               //found the first_i index
               first_i = last_i;
            }
         }else if(rec_date > date){
            break;
         }
      }
      if(first_i != -1 && (last_i - first_i) > 0){
         //make sure the first and last records are not mid-frame
         //first_i = Math.max(0, (first_i - (first_i % 4)));
         //last_i = Math.min(size, (last_i + 4 - (last_i % 4)));

         doMagCdf(first_i, last_i, date);
      }

      //...for the 20Hz file
      first_i = -1;
      size = data.getSize("20Hz");
      for(last_i = 0; last_i < size; last_i += 20){
         tt2000_parts = CDFTT2000.breakdown(data.epoch_20Hz[last_i]);
         rec_date = 
            tt2000_parts[2] + //day
            (100 * tt2000_parts[1]) + //month
            (10000 * (tt2000_parts[0] - 2000)); //year
         if(first_i == -1) {
            if(rec_date == date){
               //found the first_i index
               first_i = last_i;
            }
         }else if(rec_date > date){
            break;
         }
      }
      if(first_i != -1 && (last_i - first_i) > 0){
         //make sure the first and last records are not mid-frame
         //first_i = Math.max(0, (first_i - (first_i % 20)));
         //last_i = Math.min(size, (last_i + 20 - (last_i % 20)));

         doFspcCdf(first_i, last_i, date); 
      }
   }
   
   public static void copyFile(File sourceFile, File destFile, boolean clobber){
      try{
         if(destFile.exists() && !clobber){
            return;
         }

         if(!destFile.exists()){
            //create the output directory and file if needed
            new File(destFile.getParent()).mkdirs();
            destFile.createNewFile();
         }
   
         FileChannel source = null;
         FileChannel destination = null;
      
         try{
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
         }finally {
            if(source != null) {
               source.close();
            }
            if(destination != null) {
               destination.close();
            }
         }
      }catch(IOException ex){
         System.out.println(
            "Could not copy CDF file: "
               + ex.getMessage()
         );
      }
   }

   public static CDF openCDF(String fileName){

      CDF cdf = null;
      try{
         cdf = CDF.open(fileName);
         
         if (cdf.getStatus() != CDF_OK)
         {
            System.out.print("Error with CDF! ");
            
            if (cdf.getStatus() == CHECKSUM_ERROR){
               System.out.print("Bad checksum!");
            }
            
            if (cdf != null) cdf.close();
            
            System.out.println("");
         }
      }catch(CDFException ex){
         System.out.println(ex.getMessage());
      }
      
      return cdf;
   }
 }

