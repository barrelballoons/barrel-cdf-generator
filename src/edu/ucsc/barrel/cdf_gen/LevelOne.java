package edu.ucsc.barrel.cdf_gen;

import gsfc.nssdc.cdf.CDF;
import gsfc.nssdc.cdf.CDFException;
import gsfc.nssdc.cdf.util.CDFTT2000;
import gsfc.nssdc.cdf.Variable;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Vector;
import java.util.Arrays;
/*
LevelOne.java v13.02.28

Description:
   Creates level one CDF files from DataHolder.java object.

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
   v13.02.28
      -Uses new format of DataHolder.java object with putHyperdata() for 
         increased speed in writing CDF files.
   v13.01.18
      -Updated the filename format
      
   v12.11.28
      -Modified terminal output to indicate payload and date

   v12.11.26
      -Removed epoch calculations
      -Does not add payload onto directory path 

   v12.11.20
      -Changed references to Level_Generator to CDF_Gen
      -Moved the declaration of frameGrp and mod variables outside the main loop
      -Moved time calculation code to top of loop
      -Changed epoch to being only stored in DataHolder instead of also being a 
         local variable
      -Removed ms_of_week from all files except GPS
      -Uses DataHolder.rc_label to write rc CDF variables instead of the switch 
         statment
      -Wraps primitive variable types stored in DataHolded in proper objects before
         writing to CDF
      
   v12.11.05
      -Saves ints (or longs) to cur_cdf.files. CDF's are now full of completely raw
         variables (except EPOCH and ms_of_week)
      -Changed "Time" CDF variable to "ms_of_week" to avoid TDAS namespace
         collision
      
   v12.10.11
      -Changed version numbers to a date format
      -No longer has any public members or methods other than the constructor.
      -Constructor calls functions to process all data held in DataHolder object 
         and produce CDF files.
      -Removed HexToBit function
      -Moved functions "copyFile()", "putData()" and "openCDF()" to 
         Level_Generator.java
      -Now gets output path from Level_Generator.L1_Dir
      -Types of CDF files are now pulled from a public member of 
         Level_Generator.java

   v0.3
      -Epoch is now calculated from "weeks" and "time" variables rather than 
         filename
      -Improved the way Epoch offsets are added to variables that come faster than 
         1Hz
      
   v0.2
      -added CDF libraries
      -copies the skeleton CDF files to date stamped files in constructor 
      -extracts all data types and writes them to the correct CDF files

   v0.1
      -added empty function that will convert the hex data eventually

   v0.0
      -Does nothing, just an empty object for the main program to create

Planned Changes: 
   -Clean up/ Documentation
   -Add logging
   -Change the way frames are extracted
*/

/*
public class LevelOne{
   File cdfFile;
   CDF mag_cdf, rcnt_cdf, fspc_cdf, 
      mspc_cdf, sspc_cdf, hkpg_cdf, pps_cdf;
   
   String outputPath;
   int lastFrame = -1;
   long ms_of_week = 0;
   int weeks = 0;
   String
      date = "000000",
      id = "00",
      flt = "00",
      stn = "0",
      revNum = "00";
   Calendar dateObj = Calendar.getInstance();
   
   short INCOMPLETE_GROUP = 8196;
   
   private DataHolder data;
   
   public LevelOne(
      final String d, final String p, final String f, final String s
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
      date = d;
      
      //get the data storage object
      data = CDF_Gen.getDataSet();
      
      //set output path
      outputPath = CDF_Gen.L1_Dir;
      File outDir = new File(outputPath);
      if(!outDir.exists()){outDir.mkdirs();}
      
      //copy the CDF skeletons to the new files 
      for(int type_i = 0; type_i < CDF_Gen.fileTypes.length; type_i++){
         String srcName = 
            "cdf_skels/l1/" + "barCLL_PP_S_l1_" + 
            CDF_Gen.fileTypes[type_i] + "_YYYYMMDD_v++.cdf";
         String destName = 
            outputPath + "bar1" + flt + "_" + id + "_" + stn + "_l1_" +
            CDF_Gen.fileTypes[type_i] + "_20" + date +  "_v" + revNum +
            ".cdf";
         CDF_Gen.copyFile(new File(srcName), new File(destName));
      }
      
      //get data from DataHolder and save them to CDF files
      try{
         writeData();
      }catch(CDFException ex){
         System.out.println(ex.getMessage());
      }
   }
   
   //Pull each value out of the frame and store it in the appropriate CDF.
   private void writeData() throws CDFException{
      //create a holder for the current CDF and Variable
      CDF cur_cdf;
      Variable cur_var;

      System.out.println(
         "Creating Level One... (" + data.getSize("1Hz") + " frames)"
      );
      
      //GPS//
         System.out.println("\nSaving GPS CDF...");
         //open GPS CDF and save the reference in the cur_cdf variable
         cur_cdf = CDF_Gen.openCDF( 
            outputPath + "bar1" + flt + "_" + id + "_" + stn +
            "_l1_gps-_20" + date +  "_v" + revNum + ".cdf"
         );

         //put an entire day's worth of data at once for each CDF variable
         cur_var = cur_cdf.getVariable("GPS_Alt");
         System.out.println("GPS_Alt...");
         cur_var.putHyperData(
            0, (data.getSize("mod4")), 1,
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.gps_raw[0]
         );

         cur_var = cur_cdf.getVariable("ms_of_week");
         System.out.println("ms_of_week...");
         cur_var.putHyperData(
            0, (data.getSize("mod4")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.gps_raw[1]
         );

         cur_var = cur_cdf.getVariable("GPS_Lat");
         System.out.println("GPS_Lat...");
         cur_var.putHyperData(
            0, (data.getSize("mod4")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.gps_raw[2]
         );

         cur_var = cur_cdf.getVariable("GPS_Lon");
         System.out.println("GPS_Lon...");
         cur_var.putHyperData(
            0, (data.getSize("mod4")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.gps_raw[3]
         );

         cur_var = cur_cdf.getVariable("FrameGroup");
         System.out.println("FrameGroup...");
         cur_var.putHyperData(
            0, (data.getSize("mod4")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.frame_mod4
         );

         cur_var = cur_cdf.getVariable("Epoch");
         System.out.println("Epoch...");
         cur_var.putHyperData(
            0, (data.getSize("mod4")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.epoch_mod4
         );
         
         cur_var = cur_cdf.getVariable("Q");
         System.out.println("Q...");
         cur_var.putHyperData(
            0, (data.getSize("mod4")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.gps_q
         );

         //close current cdf
         cur_cdf.close();

      //PPS//
         System.out.println("\nSaving PPS CDF...");
         cur_cdf = CDF_Gen.openCDF( 
            outputPath + "bar1" + flt + "_" + id + "_" + stn +
            "_l1_pps-_20" + date +  "_v" + revNum + ".cdf"
         );
         
         cur_var = cur_cdf.getVariable("GPS_PPS");
         System.out.println("GPS_PPS...");
         cur_var.putHyperData(
            0, data.getSize("1Hz"), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.pps
         );

         cur_var = cur_cdf.getVariable("Version");
         System.out.println("Version...");
         cur_var.putHyperData(
            0, data.getSize("1Hz"), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.ver
         );

         cur_var = cur_cdf.getVariable("Payload_ID");
         System.out.println("Payload_ID...");
         cur_var.putHyperData(
            0, data.getSize("1Hz"), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.payID
         );

         cur_var = cur_cdf.getVariable("FrameGroup");
         System.out.println("FrameGroup...");
         cur_var.putHyperData(
            0, data.getSize("1Hz"), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.frame_1Hz
         );

         cur_var = cur_cdf.getVariable("Epoch");
         System.out.println("Epoch...");
         cur_var.putHyperData(
            0, data.getSize("1Hz"), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.epoch_1Hz
         );

         cur_var = cur_cdf.getVariable("Q");
         System.out.println("Q...");
         cur_var.putHyperData(
            0, data.getSize("1Hz"), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.pps_q
         );

         cur_cdf.close();
         
      //B//
         System.out.println("\nSaving Magnetometer CDF...");
         cur_cdf = CDF_Gen.openCDF( 
            outputPath + "bar1" + flt + "_" + id + "_" + stn +
            "_l1_magn_20" + date +  "_v" + revNum + ".cdf"
         );
         
         cur_var = cur_cdf.getVariable("MAG_X");
         System.out.println("MAG_X... ");
         cur_var.putHyperData(
            0, (data.getSize("4Hz")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.magx_raw
         );

         cur_var = cur_cdf.getVariable("MAG_Y");
         System.out.println("MAG_Y...");
         cur_var.putHyperData(
            0, (data.getSize("4Hz")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.magy_raw
         );

         cur_var = cur_cdf.getVariable("MAG_Z");
         System.out.println("MAG_Z...");
         cur_var.putHyperData(
            0, (data.getSize("4Hz")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.magz_raw
         );

         cur_var = cur_cdf.getVariable("FrameGroup");
         System.out.println("FrameGroup...");
         cur_var.putHyperData(
            0, (data.getSize("4Hz")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.frame_4Hz
         );

         cur_var = cur_cdf.getVariable("Epoch");
         System.out.println("Epoch...");
         cur_var.putHyperData(
            0, (data.getSize("4Hz")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.epoch_4Hz
         );

         cur_var = cur_cdf.getVariable("Q");
         System.out.println("Q...");
         cur_var.putHyperData(
            0, (data.getSize("4Hz")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.magn_q
         );

         cur_cdf.close();
         
      //HKPG
         System.out.println("\nSaving HKPG...");
         cur_cdf = CDF_Gen.openCDF( 
            outputPath + "bar1" + flt + "_" + id + "_" + stn +
            "_l1_hkpg_20" + date +  "_v" + revNum + ".cdf"
         );
            
         for(int var_i = 0; var_i < 36; var_i++){
            cur_var = cur_cdf.getVariable(data.hkpg_label[var_i]);
            System.out.println(data.hkpg_label[var_i] + "...");
            cur_var.putHyperData(
               0, (data.getSize("mod40")), 1, 
               new long[] {0}, 
               new long[] {1}, 
               new long[] {1}, 
               data.hkpg_raw[var_i]
            );
         }

         cur_var = cur_cdf.getVariable("numOfSats");
         System.out.println("numOfSats...");
         cur_var.putHyperData(
            0, (data.getSize("mod40")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.sats
         );

         cur_var = cur_cdf.getVariable("timeOffset");
         System.out.println("timeOffset...");
         cur_var.putHyperData(
            0, (data.getSize("mod40")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.offset
         );
         
         cur_var = cur_cdf.getVariable("termStatus");
         System.out.println("termStatus...");
         cur_var.putHyperData(
            0, (data.getSize("mod40")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.termStat
         );

         cur_var = cur_cdf.getVariable("cmdCounter");
         System.out.println("cmdCounter...");
         cur_var.putHyperData(
            0, (data.getSize("mod40")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.cmdCnt
         );

         cur_var = cur_cdf.getVariable("modemCounter");
         System.out.println("modemCounter...");
         cur_var.putHyperData(
            0, (data.getSize("mod40")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.modemCnt
         );

         cur_var = cur_cdf.getVariable("dcdCounter");
         System.out.println("dcdCounter...");
         cur_var.putHyperData(
            0, (data.getSize("mod40")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.dcdCnt
         );

         cur_var = cur_cdf.getVariable("weeks");
         System.out.println("weeks...");
         cur_var.putHyperData(
            0, (data.getSize("mod40")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.weeks
         );

         cur_var = cur_cdf.getVariable("FrameGroup");
         System.out.println("FrameGroup...");
         cur_var.putHyperData(
            0, (data.getSize("mod40")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.frame_mod40
         );

         cur_var = cur_cdf.getVariable("Epoch");
         System.out.println("Epoch...");
         cur_var.putHyperData(
            0, (data.getSize("mod40")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.epoch_mod40
         );

         cur_var = cur_cdf.getVariable("Q");
         System.out.println("Q...");
         cur_var.putHyperData(
            0, (data.getSize("mod40")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.hkpg_q
         );

         cur_cdf.close();
         
      //FSPC//
         System.out.println("\nSaving FSPC...");
         cur_cdf = CDF_Gen.openCDF( 
            outputPath + "bar1" + flt + "_" + id + "_" + stn +
            "_l1_fspc_20" + date +  "_v" + revNum + ".cdf"
         );
         
         cur_var = cur_cdf.getVariable("LC1");
         System.out.println("LC1...");
         cur_var.putHyperData(
            0, (data.getSize("20Hz")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.lc1_raw
         );

         cur_var = cur_cdf.getVariable("LC2");
         System.out.println("LC2...");
         cur_var.putHyperData(
            0, (data.getSize("20Hz")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.lc2_raw
         );

         cur_var = cur_cdf.getVariable("LC3");
         System.out.println("LC3...");
         cur_var.putHyperData(
            0, (data.getSize("20Hz")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.lc3_raw
         );

         cur_var = cur_cdf.getVariable("LC4");
         System.out.println("LC4...");
         cur_var.putHyperData(
            0, (data.getSize("20Hz")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.lc4_raw
         );

         cur_var = cur_cdf.getVariable("FrameGroup");
         System.out.println("FrameGroup...");
         cur_var.putHyperData(
            0, (data.getSize("20Hz")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.frame_20Hz
         );

         cur_var = cur_cdf.getVariable("Epoch");
         System.out.println("Epoch...");
         cur_var.putHyperData(
            0, (data.getSize("20Hz")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.epoch_20Hz
         );

         cur_var = cur_cdf.getVariable("Q");
         System.out.println("Q...");
         cur_var.putHyperData(
            0, (data.getSize("20Hz")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.fspc_q
         );

         cur_cdf.close();
         
      //MSPC//
         System.out.println("\nSaving MSPC...");
         cur_cdf = CDF_Gen.openCDF( 
            outputPath + "bar1" + flt + "_" + id + "_" + stn +
            "_l1_mspc_20" + date +  "_v" + revNum + ".cdf"
         );

         cur_var = cur_cdf.getVariable("MSPC");
         System.out.println("Spectrum Arrays...");
         cur_var.putHyperData(
            0, (data.getSize("mod4")), 1, 
            new long[] {0, 0}, 
            new long[] {48, 1}, 
            new long[] {1, 1}, 
            data.mspc_raw
         );

         cur_var = cur_cdf.getVariable("FrameGroup");
         System.out.println("FrameGroup...");
         cur_var.putHyperData(
            0, (data.getSize("mod4")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.frame_mod4
         );

         cur_var = cur_cdf.getVariable("Epoch");
         System.out.println("Epoch...");
         cur_var.putHyperData(
            0, (data.getSize("mod4")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.epoch_mod4
         );

         cur_var = cur_cdf.getVariable("Q");
         System.out.println("Q...");
         cur_var.putHyperData(
            0, (data.getSize("mod4")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.mspc_q
         );

         cur_cdf.close();
         
      //SSPC//
         System.out.println("\nSaving SSPC...");
         cur_cdf = CDF_Gen.openCDF( 
            outputPath + "bar1" + flt + "_" + id + "_" + stn +
            "_l1_sspc_20" + date +  "_v" + revNum + ".cdf"
         );

         cur_var = cur_cdf.getVariable("SSPC");
         System.out.println("Spectrum Arrays...");
         cur_var.putHyperData(
            0, (data.getSize("mod32")), 1, 
            new long[] {0}, 
            new long[] {256, 1}, 
            new long[] {1}, 
            data.sspc_raw
         );

         cur_var = cur_cdf.getVariable("FrameGroup");
         System.out.println("FrameGroup...");
         cur_var.putHyperData(
            0, (data.getSize("mod32")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.frame_mod32
         );

         cur_var = cur_cdf.getVariable("Epoch");
         System.out.println("Epoch...");
         cur_var.putHyperData(
            0, (data.getSize("mod32")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.epoch_mod32
         );

         cur_var = cur_cdf.getVariable("Q");
         System.out.println("Q...");
         cur_var.putHyperData(
            0, (data.getSize("mod32")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.sspc_q
         );

         cur_cdf.close();
         
      //RCNT
         System.out.println("\nSaving RCNT...");
         cur_cdf = CDF_Gen.openCDF( 
            outputPath + "bar1" + flt + "_" + id + "_" + stn +
            "_l1_rcnt_20" + date +  "_v" + revNum + ".cdf"
         );

         cur_var = cur_cdf.getVariable("Interrupt");
         System.out.println("Interrupt...");
         cur_var.putHyperData(
            0, (data.getSize("mod4")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.rcnt_raw[0]
         );

         cur_var = cur_cdf.getVariable("LowLevel");
         System.out.println("LowLevel...");
         cur_var.putHyperData(
            0, (data.getSize("mod4")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.rcnt_raw[1]
         );

         cur_var = cur_cdf.getVariable("PeakDet");
         System.out.println("PeakDet...");
         cur_var.putHyperData(
            0, (data.getSize("mod4")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.rcnt_raw[2]
         );

         cur_var = cur_cdf.getVariable("HighLevel");
         System.out.println("HighLevel...");
         cur_var.putHyperData(
            0, (data.getSize("mod4")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.rcnt_raw[3]
         );

         cur_var = cur_cdf.getVariable("FrameGroup");
         System.out.println("FrameGroup...");
         cur_var.putHyperData(
            0, (data.getSize("mod4")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.frame_mod4
         );

         cur_var = cur_cdf.getVariable("Epoch");
         System.out.println("Epoch...");
         cur_var.putHyperData(
            0, (data.getSize("mod4")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1},
            data.epoch_mod4
         );

         cur_var = cur_cdf.getVariable("Q");
         System.out.println("Q...");
         cur_var.putHyperData(
            0, (data.getSize("mod4")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.rcnt_q
         );

      cur_cdf.close();
      
      System.out.println("Created Level One.");
   }
 }
 */

public class LevelOne{
   File cdfFile;
   CDF mag_cdf, rcnt_cdf, fspc_cdf, 
      mspc_cdf, sspc_cdf, hkpg_cdf, pps_cdf;
   
   String outputPath;
   int lastFrame = -1;
   long ms_of_week = 0;
   int weeks = 0;
   String
      id = "00",
      flt = "00",
      stn = "0",
      revNum = "00",
      mag_id = "";
   int today = 0;
   Calendar dateObj = Calendar.getInstance();
   
   SpectrumExtract spectrum;
   
   short INCOMPLETE_GROUP = 8196;
   
   private DataHolder data;
   
   public LevelOne(
      final String d, final String p, 
      final String f, final String s
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

      //get the data storage object
      data = CDF_Gen.getDataSet();
     
      //create the spectrum rebinning object
      spectrum = new SpectrumExtract();
     
      //set output path
      outputPath = CDF_Gen.L2_Dir;
      
      //get data from DataHolder and save them to CDF files
      try{
         writeData();
      }catch(CDFException ex){
         System.out.println(ex.getMessage());
      }
   }
   
   //Convert the GPS data and save it to CDF files
   public void doGpsCdf(int first, int last, int date) throws CDFException{
      CDF cdf;
      Variable var;
      int numOfRecs = last - first;
      float[] 
         lat = new float[numOfRecs], 
         lon = new float[numOfRecs], 
         alt = new float[numOfRecs];
      int[] 
         ms = new int[numOfRecs],
         frameGroup = new int[numOfRecs],
         q = new int[numOfRecs]; 
      long[] epoch = new long[numOfRecs];

      System.out.println("\nSaving GPS Level 2 CDF...");

      //convert lat, lon, and alt values and select values for this date
      for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
        //convert mm to km
        alt[rec_i] = (float)data.gps_raw[0][data_i] / 1000000;

        //convert lat and lon to physical units
        lat[rec_i] = (float)data.gps_raw[2][data_i];
        lat[rec_i] *= 
           Float.intBitsToFloat(Integer.valueOf("33B40000",16).intValue());

        lon[rec_i] = (float)data.gps_raw[3][data_i];
        lon[rec_i] *= 
           Float.intBitsToFloat(Integer.valueOf("33B40000",16).intValue());

        //save the values from the other variables
        ms[rec_i] = data.gps_raw[1][data_i];
        frameGroup[rec_i] = data.frame_mod4[data_i];
        epoch[rec_i] = data.epoch_mod4[data_i];
        q[rec_i] = data.gps_q[data_i];
      }

      //make sure there is a CDF file to open
      //(CDF_Gen.copyFile will not clobber an existing file)
      String srcName = 
         "cdf_skels/l2/barCLL_PP_S_l2_gps-_YYYYMMDD_v++.cdf";
      String destName = 
         outputPath + "/" + date + "/" + "bar1" + flt + "_" + id + "_" + stn + 
         "_l2_" + "gps-" + "_20" + date +  "_v" + revNum + ".cdf";

      CDF_Gen.copyFile(new File(srcName), new File(destName), false);

      //open GPS CDF and save the reference in the cdf variable
      cdf = CDF_Gen.openCDF(destName);
      
      
      var = cdf.getVariable("GPS_Alt");
      System.out.println("GPS_Alt...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1,
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         alt
      );

      var = cdf.getVariable("ms_of_week");
      System.out.println("ms_of_week...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         ms
      );

      var = cdf.getVariable("GPS_Lat");
      System.out.println("GPS_Lat...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         lat 
      );

      var = cdf.getVariable("GPS_Lon");
      System.out.println("GPS_Lon...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         lon
      );

      var = cdf.getVariable("FrameGroup");
      System.out.println("FrameGroup...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         frameGroup
      );

      var = cdf.getVariable("Epoch");
      System.out.println("Epoch...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         epoch
      );
      
      var = cdf.getVariable("Q");
      System.out.println("Q...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         q
      );

      System.out.println("Done with GPS!");
      //close current cdf
      cdf.close();
   }
   
   //write the pps file, no processing needed
   public void doPpsCdf(int first, int last, int date) throws CDFException{
      CDF cdf;
      Variable var;
      
      int numOfRecs = last - first;
      short[] 
         version = new short[numOfRecs],
         payID = new short[numOfRecs];
      int[] 
         frameGroup = new int[numOfRecs],
         q = new int[numOfRecs],
         pps = new int[numOfRecs];
      long[] epoch = new long[numOfRecs];

      System.out.println("\nSaving PPS Level Two CDF...");

      for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
        version[rec_i] = data.ver[data_i];
        payID[rec_i] = data.payID[data_i];
        frameGroup[rec_i] = data.frame_1Hz[data_i];
        epoch[rec_i] = data.epoch_1Hz[data_i];
        q[rec_i] = data.pps_q[data_i];
      }

      String srcName = 
         "cdf_skels/l2/barCLL_PP_S_l2_pps-_YYYYMMDD_v++.cdf";
      String destName = 
         outputPath  + "/" + date + "/" + "bar1" + flt + "_" + id + "_" + stn + 
         "_l2_" + "pps-" + "_20" + date +  "_v" + revNum + ".cdf";
      CDF_Gen.copyFile(new File(srcName), new File(destName), false);

      cdf = CDF_Gen.openCDF(destName);
      
      var = cdf.getVariable("GPS_PPS");
      System.out.println("GPS_PPS...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1L, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         pps
      );

      var = cdf.getVariable("Version");
      System.out.println("Version...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1L, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         version
      );

      var = cdf.getVariable("Payload_ID");
      System.out.println("Payload_ID...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1L, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         payID
      );

      var = cdf.getVariable("FrameGroup");
      System.out.println("FrameGroup...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1L, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         frameGroup 
      );
      var = cdf.getVariable("Epoch");
      System.out.println(date + ": " + var.getNumWrittenRecords());
      System.out.println("Epoch...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1L,
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         epoch
      );

      var = cdf.getVariable("Q");
      System.out.println("Q...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1L, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         q
      );

      cdf.close();
   }
   
   public void doMagCdf(int first, int last, int date) throws CDFException{
      CDF cdf;
      Variable var;
      
      int numOfRecs = last - first;
      int[] 
         frameGroup = new int[numOfRecs],
         q = new int[numOfRecs]; 
      long[] epoch = new long[numOfRecs];

      float[] 
         magx = new float[numOfRecs],
         magy = new float[numOfRecs],
         magz = new float[numOfRecs],
         magTot = new float[numOfRecs];

      System.out.println("\nSaving Magnetometer Level Two CDF...");

      String srcName = 
         "cdf_skels/l2/barCLL_PP_S_l2_magn_YYYYMMDD_v++.cdf";
      String destName = 
         outputPath + "/" + date + "/" + "bar1" + flt + "_" + id + "_" + stn + 
         "_l2_" + "magn" + "_20" + date +  "_v" + revNum + ".cdf";
      CDF_Gen.copyFile(new File(srcName), new File(destName), false);

      cdf = CDF_Gen.openCDF(destName);
     
      //extract the nominal magnetometer value and calculate |B|
      for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
         magx[rec_i] = (data.magx_raw[data_i] - 8388608.0f) / 83886.070f;
         magy[rec_i] = (data.magy_raw[data_i] - 8388608.0f) / 83886.070f;
         magz[rec_i] = (data.magz_raw[data_i] - 8388608.0f) / 83886.070f;

         magTot[rec_i] = 
            (float)Math.sqrt(
               (magx[rec_i] * magx[rec_i]) + 
               (magy[rec_i] * magy[rec_i]) +
               (magz[rec_i] * magz[rec_i]) 
            );

         frameGroup[rec_i] = data.frame_4Hz[data_i];
         epoch[rec_i] = data.epoch_4Hz[data_i];
         q[rec_i] = data.magn_q[data_i];
      }

      //store the nominal mag values
      var = cdf.getVariable("MAG_X");
      System.out.println("MAG_X... ");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         magx 
      );

      var = cdf.getVariable("MAG_Y");
      System.out.println("MAG_Y...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         magy
      );

      var = cdf.getVariable("MAG_Z");
      System.out.println("MAG_Z...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         magz
      );

      var = cdf.getVariable("Total");
      System.out.println("Field Magnitude...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         magTot 
      );

      var = cdf.getVariable("FrameGroup");
      System.out.println("FrameGroup...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         frameGroup
      );

      var = cdf.getVariable("Epoch");
      System.out.println("Epoch...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         epoch
      );

      var = cdf.getVariable("Q");
      System.out.println("Q...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         q
      );

      cdf.close();

   }
   
   public void doHkpgCdf(int first, int last, int date) throws CDFException{
      CDF cdf;
      Variable var;
      
      int numOfRecs = last - first;
      short []
         sats = new short[numOfRecs],
         offset = new short[numOfRecs],
         termStat = new short[numOfRecs],
         modemCnt = new short[numOfRecs],
         dcdCnt = new short[numOfRecs];
      int[] 
         frameGroup = new int[numOfRecs],
         q = new int[numOfRecs],
         cmdCnt = new int[numOfRecs],
         weeks = new int[numOfRecs];
      long[] epoch = new long[numOfRecs];

      System.out.println("\nSaving HKPG...");

      String srcName = 
         "cdf_skels/l2/barCLL_PP_S_l2_hkpg_YYYYMMDD_v++.cdf";
      String destName = 
         outputPath + "/" + date + "/" + "bar1" + flt + "_" + id + "_" + stn 
         + "_l2_" + "hkpg" + "_20" + date +  "_v" + revNum + ".cdf";

      CDF_Gen.copyFile(new File(srcName), new File(destName), false);

      cdf = CDF_Gen.openCDF(destName);
         
      for(int var_i = 0; var_i < data.hkpg_scale.length; var_i++){
         //scale all the records for this variable
         double[] hkpg_scaled = new double[numOfRecs];
         for(int rec_i = 0; rec_i < numOfRecs; rec_i++){
            hkpg_scaled[rec_i] = 
               (data.hkpg_raw[var_i][rec_i] * data.hkpg_scale[var_i]) + 
               data.hkpg_offset[var_i];
         }

         var = cdf.getVariable(data.hkpg_label[var_i]);
         System.out.println(data.hkpg_label[var_i] + "...");
         var.putHyperData(
            var.getNumWrittenRecords(), numOfRecs, 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            hkpg_scaled
         );
      }

      for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
         sats[rec_i] = data.sats[data_i];
         offset[rec_i] = data.offset[data_i];
         termStat[rec_i] = data.termStat[data_i];
         modemCnt[rec_i] = data.modemCnt[data_i];
         dcdCnt[rec_i] = data.dcdCnt[data_i];
         cmdCnt[rec_i] = data.cmdCnt[data_i];
         weeks[rec_i] = data.weeks[data_i];
         epoch[rec_i] = data.epoch_mod40[data_i];
         q[rec_i] = data.hkpg_q[data_i];
      }

      var = cdf.getVariable("numOfSats");
      System.out.println("numOfSats...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         sats
      );

      var = cdf.getVariable("timeOffset");
      System.out.println("timeOffset...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         offset
      );
      
      var = cdf.getVariable("termStatus");
      System.out.println("termStatus...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         termStat
      );

      var = cdf.getVariable("cmdCounter");
      System.out.println("cmdCounter...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         cmdCnt
      );

      var = cdf.getVariable("modemCounter");
      System.out.println("modemCounter...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         modemCnt
      );

      var = cdf.getVariable("dcdCounter");
      System.out.println("dcdCounter...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         dcdCnt
      );

      var = cdf.getVariable("weeks");
      System.out.println("weeks...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         weeks
      );

      var = cdf.getVariable("FrameGroup");
      System.out.println("FrameGroup...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         frameGroup
      );

      var = cdf.getVariable("Epoch");
      System.out.println("Epoch...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         epoch
      );

      var = cdf.getVariable("Q");
      System.out.println("Q...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         q
      );

      cdf.close();
   }

   public void doFspcCdf(int first, int last, int date) throws CDFException{
      CDF cdf;
      Variable var;
      int numOfRecs = last - first;

      double[][] 
         chan_edges = new double[numOfRecs][5],
         lc_scaled = new double[4][numOfRecs];
      int[] tempLC = new int[4];
      double scint_temp = 20, dpu_temp = 20, peak = -1;
      
      int[] 
         frameGroup = new int[numOfRecs],
         q = new int[numOfRecs];
      long[] epoch = new long[numOfRecs];

      System.out.println("\nSaving FSPC...");

      String srcName = 
         "cdf_skels/l2/barCLL_PP_S_l2_fspc_YYYYMMDD_v++.cdf";
      String destName = 
         outputPath + "/" + date + "/" + "bar1" + flt + "_" + id + "_" + stn 
         + "_l2_" + "fspc" + "_20" + date +  "_v" + revNum + ".cdf";
      CDF_Gen.copyFile(new File(srcName), new File(destName), false);

      cdf = CDF_Gen.openCDF(destName);
      
      //convert the light curves counts to cnts/sec and 
      //figure out the channel width
      for(int lc_rec = 0, hkpg_rec = 0; lc_rec < numOfRecs; lc_rec++){

         //get temperatures
         hkpg_rec = lc_rec / 20 / 40; //convert from 20Hz to mod40
         if(data.hkpg_raw[data.T0][hkpg_rec] != 0){
            scint_temp = 
               (data.hkpg_raw[data.T0][hkpg_rec] * data.hkpg_scale[data.T0]) + 
               data.hkpg_offset[data.T0];
         }else{
            scint_temp = 20;
         }
         if(data.hkpg_raw[data.T5][hkpg_rec] != 0){
            dpu_temp = 
               (data.hkpg_raw[data.T5][hkpg_rec] * data.hkpg_scale[data.T5]) + 
               data.hkpg_offset[data.T5];
         }else{
            dpu_temp = 20;
         }
         
         //find the bin that contains the 511 line
         //peak = spectrum.find511(mspc_rebin[mspc_rec], offset);

         //get the adjusted bin edges
         chan_edges[lc_rec] = 
            spectrum.createBinEdges(0, scint_temp, dpu_temp, peak);

         //write the spectrum to the new array
         lc_scaled[0][lc_rec] = data.lc1_raw[lc_rec] * 20;
         lc_scaled[1][lc_rec] = data.lc2_raw[lc_rec] * 20;
         lc_scaled[2][lc_rec] = data.lc3_raw[lc_rec] * 20;
         lc_scaled[3][lc_rec] = data.lc4_raw[lc_rec] * 20;
      }

      for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
         frameGroup[rec_i] = data.frame_20Hz[data_i];
         epoch[rec_i] = data.epoch_20Hz[data_i];
         q[rec_i] = data.fspc_q[data_i];
      }

      var = cdf.getVariable("LC1");
      System.out.println("LC1...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         lc_scaled[0]
      );
      
      var = cdf.getVariable("LC2");
      System.out.println("LC2...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         lc_scaled[1]
      );

      var = cdf.getVariable("LC3");
      System.out.println("LC3...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         lc_scaled[2]
      );

      var = cdf.getVariable("LC4");
      System.out.println("LC4...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         lc_scaled[3]
      );

      var = cdf.getVariable("FrameGroup");
      System.out.println("FrameGroup...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         frameGroup
      );

      var = cdf.getVariable("Epoch");
      System.out.println("Epoch...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         epoch
      );

      var = cdf.getVariable("Q");
      System.out.println("Q...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         q
      );

      cdf.close();

   }

   public void doMspcCdf(int first, int last, int date) throws CDFException{
      CDF cdf;
      Variable var;
      
      double peak = -1, scint_temp = 0, dpu_temp = 0;
      
      int offset = 90;

      int numOfRecs = last - first;
      int[] 
         frameGroup = new int[numOfRecs],
         q = new int[numOfRecs];
      long[] epoch = new long[numOfRecs];

      double[][] mspc_rebin = new double[numOfRecs][48];
      double[] old_edges = new double[48];
      double[] std_edges = SpectrumExtract.stdEdges(1, 2.4414);

      
      //rebin the mspc spectra
      for(int mspc_rec = 0, hkpg_rec = 0; mspc_rec < numOfRecs; mspc_rec++){
         
         //get temperatures
         hkpg_rec = mspc_rec * 4 / 40; //convert from mod4 to mod40
         if(data.hkpg_raw[data.T0][hkpg_rec] != 0){
            scint_temp = 
               (data.hkpg_raw[data.T0][hkpg_rec] * data.hkpg_scale[data.T0]) + 
               data.hkpg_offset[data.T0];
         }else{
            scint_temp = 20;
         }
         if(data.hkpg_raw[data.T5][hkpg_rec] != 0){
            dpu_temp = 
               (data.hkpg_raw[data.T5][hkpg_rec] * data.hkpg_scale[data.T5]) + 
               data.hkpg_offset[data.T5];
         }else{
            dpu_temp = 20;
         }
         
         //find the bin that contains the 511 line
         //peak = spectrum.find511(mspc_rebin[mspc_rec], offset);
      
         //get the adjusted bin edges
         old_edges = spectrum.createBinEdges(1, scint_temp, dpu_temp, peak);

         //rebin the spectrum
         mspc_rebin[mspc_rec] = spectrum.rebin(
            data.mspc_raw[mspc_rec], old_edges, std_edges, 49, 49, true 
         );

         //divide counts by bin width and adjust the time scale
         for(int bin_i = 0; bin_i < mspc_rebin[mspc_rec].length; bin_i++){
            mspc_rebin[mspc_rec][bin_i] /= 
               std_edges[bin_i + 1] - std_edges[bin_i];

            mspc_rebin[mspc_rec][bin_i] /= 4;
         }
      }

      for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
         frameGroup[rec_i] = data.frame_mod4[data_i];
         epoch[rec_i] = data.epoch_mod4[data_i];
         q[rec_i] = data.mspc_q[data_i];
      }

      System.out.println("\nSaving MSPC...");

      String srcName = 
         "cdf_skels/l2/barCLL_PP_S_l2_mspc_YYYYMMDD_v++.cdf";
      String destName = 
         outputPath  + "/" + date + "/"+ "bar1" + flt + "_" + id + "_" + stn 
         + "_l2_" + "mspc" + "_20" + date +  "_v" + revNum + ".cdf";

      CDF_Gen.copyFile(new File(srcName), new File(destName), false);

      cdf = CDF_Gen.openCDF(destName);

      var = cdf.getVariable("MSPC");
      System.out.println("Spectrum Arrays...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0, 0}, 
         new long[] {48, 1}, 
         new long[] {1, 1}, 
         mspc_rebin
      );

      var = cdf.getVariable("FrameGroup");
      System.out.println("FrameGroup...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         frameGroup
      );

      var = cdf.getVariable("Epoch");
      System.out.println("Epoch...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         epoch
      );

      var = cdf.getVariable("Q");
      System.out.println("Q...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         q
      );

      cdf.close();
   }

   public void doSspcCdf(int first, int last, int date) throws CDFException{
      CDF cdf;
      Variable var;
      
      double peak = -1, scint_temp = 0, dpu_temp = 0;
      
      int offset = 90;

      int numOfRecs = last - first;
      double[][] sspc_rebin = new double[numOfRecs][256];
      double[] old_edges = new double[257];
      double[] std_edges = SpectrumExtract.stdEdges(2, 2.4414);
      
      int[] 
         frameGroup = new int[numOfRecs],
         q = new int[numOfRecs];
      long[] epoch = new long[numOfRecs];

      //rebin the sspc spectra
      for(int sspc_rec = 0, hkpg_rec = 0; sspc_rec < numOfRecs; sspc_rec++){
         //get temperatures
         hkpg_rec = sspc_rec * 32 / 40; //convert from mod32 to mod40
         if(data.hkpg_raw[data.T0][hkpg_rec] != 0){
            scint_temp = 
               (data.hkpg_raw[data.T0][hkpg_rec] * data.hkpg_scale[data.T0]) + 
               data.hkpg_offset[data.T0];
         }else{
            scint_temp = 20;
         }
         if(data.hkpg_raw[data.T5][hkpg_rec] != 0){
            dpu_temp = 
               (data.hkpg_raw[data.T5][hkpg_rec] * data.hkpg_scale[data.T5]) + 
               data.hkpg_offset[data.T5];
         }else{
            dpu_temp = 20;
         }

         //find the bin that contains the 511 line
         //peak = spectrum.find511(sspc_rebin[sspc_rec], offset);
      
         //get the adjusted bin edges
         old_edges = spectrum.createBinEdges(2, scint_temp, dpu_temp, peak);
         
         //rebin the spectum
         sspc_rebin[sspc_rec] = spectrum.rebin(
            data.sspc_raw[sspc_rec], old_edges, std_edges, 257, 257, false
         );

         //divide counts by bin width and convert the time scale to /sec
         for(int bin_i = 0; bin_i < sspc_rebin[sspc_rec].length; bin_i++){
            sspc_rebin[sspc_rec][bin_i] /= 
               std_edges[bin_i + 1] - std_edges[bin_i];
            sspc_rebin[sspc_rec][bin_i] /= 32;
         }
      }

      for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
         frameGroup[rec_i] = data.frame_mod32[data_i];
         epoch[rec_i] = data.epoch_mod32[data_i];
         q[rec_i] = data.sspc_q[data_i];
      }

      System.out.println("\nSaving SSPC...");

      String srcName = 
         "cdf_skels/l2/barCLL_PP_S_l2_sspc_YYYYMMDD_v++.cdf";
      String destName = 
         outputPath + "/" + date + "/" + "bar1" + flt + "_" + id + "_" + stn +
         "_l2_" + "sspc" + "_20" + date +  "_v" + revNum + ".cdf";
      CDF_Gen.copyFile(new File(srcName), new File(destName), false);

      cdf = CDF_Gen.openCDF(destName);

      var = cdf.getVariable("SSPC");
      System.out.println("Spectrum Arrays...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {256, 1}, 
         new long[] {1}, 
         sspc_rebin
      );

      var = cdf.getVariable("FrameGroup");
      System.out.println("FrameGroup...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         frameGroup
      );

      var = cdf.getVariable("Epoch");
      System.out.println("Epoch...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         epoch
      );

      var = cdf.getVariable("Q");
      System.out.println("Q...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         q
      );

      cdf.close();
   }

   public void doRcntCdf(int first, int last, int date) throws CDFException{
      CDF cdf;
      Variable var;
      
      int numOfRecs = last - first;
      double[][] rc_timeScaled = new double[4][numOfRecs];
      int[] 
         frameGroup = new int[numOfRecs],
         q = new int[numOfRecs];
      long[] epoch = new long[numOfRecs];

      //change all the units from cnts/4sec to cnts/sec
      for(int var_i = 0; var_i < 4; var_i++){
         for(int rec_i = 0; rec_i < numOfRecs; rec_i++){
            rc_timeScaled[var_i][rec_i] = data.rcnt_raw[var_i][rec_i] / 4;
         }
      }

      for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
         frameGroup[rec_i] = data.frame_mod4[data_i];
         epoch[rec_i] = data.epoch_mod4[data_i];
         q[rec_i] = data.rcnt_q[data_i];
      }
         
      System.out.println("\nSaving RCNT...");

      String srcName = 
         "cdf_skels/l2/barCLL_PP_S_l2_rcnt_YYYYMMDD_v++.cdf";
      String destName = 
         outputPath + "/" + date + "/"  + "bar1" + flt + "_" + id + "_" + stn
         + "_l2_" + "rcnt" + "_20" + date +  "_v" + revNum + ".cdf";

      CDF_Gen.copyFile(new File(srcName), new File(destName), false);

      cdf = CDF_Gen.openCDF(destName);

      var = cdf.getVariable("Interrupt");
      System.out.println("Interrupt...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         rc_timeScaled[0]
      );

      var = cdf.getVariable("LowLevel");
      System.out.println("LowLevel...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         rc_timeScaled[1]
      );

      var = cdf.getVariable("PeakDet");
      System.out.println("PeakDet...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         rc_timeScaled[2]
      );

      var = cdf.getVariable("HighLevel");
      System.out.println("HighLevel...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         rc_timeScaled[3]
      );

      var = cdf.getVariable("FrameGroup");
      System.out.println("FrameGroup...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         frameGroup
      );

      var = cdf.getVariable("Epoch");
      System.out.println("Epoch...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1},
         epoch
      );

      var = cdf.getVariable("Q");
      System.out.println("Q...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         q
      );

      cdf.close();
   }

   //Pull each value out of the frame and store it in the appropriate CDF.
   private void writeData() throws CDFException{
      int first_rec, last_rec;
      
      System.out.println(
         "Creating Level Two... (" + data.getSize("1Hz") + " frames)"
      );
      
      //dave data to yesterday, today, and tomorrow's CDF files  
      last_rec = data.day_rollovers[DataHolder.YESTERDAY];
      if(last_rec != -1){
         first_rec = 0;
         
         //make sure the output directory exists
         File outDir = new File(outputPath + "/" + (today - 1));
         if(!outDir.exists()){outDir.mkdirs();}

         doAllCdf(first_rec, last_rec, (today - 1));
      }

      last_rec = data.day_rollovers[DataHolder.TODAY];
      if(last_rec != -1){
         //first index of today is the last index of yesterday
         first_rec = data.day_rollovers[DataHolder.YESTERDAY];
         
         File outDir = new File(outputPath + "/" + today);
         if(!outDir.exists()){outDir.mkdirs();}

         //make sure that the yesterday actually had an index set
         if(first_rec == -1){first_rec = 0;}

         doAllCdf(first_rec, last_rec, today);
      }

      last_rec = data.day_rollovers[DataHolder.TOMORROW];
      if(last_rec != -1){
         first_rec = data.day_rollovers[DataHolder.TODAY];
         
         File outDir = new File(outputPath + "/" + (today + 1));
         if(!outDir.exists()){outDir.mkdirs();}

         if(first_rec == -1){first_rec = 0;}

         doAllCdf(first_rec, last_rec, today + 1);
      }


      System.out.println("Created Level Two.");
   }

   private void doAllCdf(int first, int last, int date) throws CDFException{
      doGpsCdf((first / 4), (last / 4), date);
      doPpsCdf(first, last, date);
      doMagCdf((first * 4), (last * 4), date);
      doHkpgCdf((first / 40), (last / 40), date);  
      doFspcCdf((first * 20), (last * 20), date);  
      doMspcCdf((first / 4), (last / 4), date);  
      doSspcCdf((first / 32), (last / 32), date);  
      doRcntCdf((first / 4), (last / 4), date);  
   }
 }
