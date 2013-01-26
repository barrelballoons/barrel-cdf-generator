package edu.ucsc.barrel.cdf_gen;

import gsfc.nssdc.cdf.CDF;
import gsfc.nssdc.cdf.CDFException;
import gsfc.nssdc.cdf.util.CDFTT2000;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Vector;
import java.util.Arrays;
/*
LevelOne.java v12.11.28

Description:
   Creates level one CDF files

v13.01.18
   -Updated the filename format
   
v12.11.28
   -Modified terminal output to indicate payload and date

v12.11.26
   -Removed epoch calculations
   -Does not add payload onto directory path 

v12.11.20
   -Changed references to Level_Generator to CDF_Gen
   -Moved the declaration of frameGrp and mod variables outside the main loop.   
   -Moved time calculation code to top of loop
   -Changed epoch to being only stored in DataHolder instead of also being a local variable
   -Removed ms_of_week from all files except GPS
   -Uses DataHolder.rc_label to write rc CDF variables instead of the switch statment
   -Wraps primitive variable types stored in DataHolded in proper objects before writing to CDF
   
v12.11.05
   -Saves ints (or longs) to cdf files. CDF's are now full of completely raw variables (except EPOCH and ms_of_week)
   -Changed "Time" CDF variable to "ms_of_week" to avoid TDAS namespace collision
   
v12.10.11
   -Changed version numbers to a date format
   -No longer has any public members or methods other than the constructor.
   -Constructor calls functions to process all data held in DataHolder object and produce CDF files.
   -Removed HexToBit function
   -Moved functions "copyFile()", "putData()" and "openCDF()" to Level_Generator.java
   -Now gets output path from Level_Generator.L1_Dir
   -Types of CDF files are now pulled from a public member of Level_Generator.java

v0.3
   -Epoch is now calculated from "weeks" and "time" variables rather than filename
   -Improved the way Epoch offsets are added to variables that come faster than 1Hz
   
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

public class LevelOne{
   File cdfFile;
   CDF mag_cdf, rcnt_cdf, gps_cdf, fspc_cdf, 
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
      
      //open each CDF file and save the id
      gps_cdf = CDF_Gen.openCDF( 
         outputPath + "bar1" + flt + "_" + id + "_" + stn +
         "_l1_gps-_20" + date +  "_v" + revNum + ".cdf"
      );
      mag_cdf = CDF_Gen.openCDF( 
         outputPath + "bar1" + flt + "_" + id + "_" + stn +
         "_l1_magn_20" + date +  "_v" + revNum + ".cdf"
      );
      pps_cdf = CDF_Gen.openCDF( 
         outputPath + "bar1" + flt + "_" + id + "_" + stn +
         "_l1_pps-_20" + date +  "_v" + revNum + ".cdf"
      );
      hkpg_cdf = CDF_Gen.openCDF( 
         outputPath + "bar1" + flt + "_" + id + "_" + stn +
         "_l1_hkpg_20" + date +  "_v" + revNum + ".cdf"
      ); 
      fspc_cdf = CDF_Gen.openCDF( 
         outputPath + "bar1" + flt + "_" + id + "_" + stn +
         "_l1_fspc_20" + date +  "_v" + revNum + ".cdf"
      );
      mspc_cdf = CDF_Gen.openCDF( 
         outputPath + "bar1" + flt + "_" + id + "_" + stn +
         "_l1_mspc_20" + date +  "_v" + revNum + ".cdf"
      );
      sspc_cdf = CDF_Gen.openCDF( 
         outputPath + "bar1" + flt + "_" + id + "_" + stn +
         "_l1_sspc_20" + date +  "_v" + revNum + ".cdf"
      );
      rcnt_cdf = CDF_Gen.openCDF( 
         outputPath + "bar1" + flt + "_" + id + "_" + stn +
         "_l1_rcnt_20" + date +  "_v" + revNum + ".cdf"
      );
      
      //get data from DataHolder and save them to CDF files
      try{
         saveFrames();
      }catch(CDFException ex){
         System.out.println(ex.getMessage());
      }
   }
   
   //Pull each value out of the frame and store it in the appropriate CDF.
   private void saveFrames() throws CDFException{
         //declare all of the variables needed to build up records
      long
         mag_rec_num = 0, rcnt_rec_num = 0, gps_rec_num = 0, fspc_rec_num = 0,
         mspc_rec_num = 0, sspc_rec_num = 0, hkpg_rec_num = 0, pps_rec_num = 0;
      
      Long[] blank_vals = {
         Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE,
         Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE,
         Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE,
         Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE,
         Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE,
         Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE,
         Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE,
         Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE,
         Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE,
         Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE,
         Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE,
         Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE,
         Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE,
         Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE,
         Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE,
         Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE,
         Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE,
         Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE,
         Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE,
         Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE,
         Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE,
         Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE,
         Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE,
         Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MIN_VALUE
      };
      Integer[] test = {1,1,1,1,1,1};
      Vector
         gps_data = new Vector(
            Arrays.asList(test)
         ),
         pps_data = new Vector(6, 1),
         mag_data = new Vector(6, 1),
         hkpg_data = new Vector(46, 1),
         rcnt_data = new Vector(7, 1),
         fspc_data = new Vector(7, 1),
         mspc_data = new Vector(4, 1),
         sspc_data = new Vector(4, 1);
      Integer[]
         mspc_buff = new Integer[48],
         sspc_buff = new Integer[256];
      
      String[]
         gps_var_names = {
            "GPS_Alt", "ms_of_week", "GPS_Lat", "GPS_Lon",  
            "FrameGroup", "Epoch", "Q"
         },
         pps_var_names = {
            "GPS_PPS", " Payload_ID", "Version",
            "FrameGroup", "Epoch", "Q"
         },
         mag_var_names = {
            "MAG_X", "MAG_Y", "MAG_Z",
            "FrameGroup", "Epoch", "Q"
         },
         rcnt_var_names = {
            "Interrupt", "LowLevel", "PeakDet", "HighLevel",
            "FrameGroup", "Epoch", "Q"
         },
         fspc_var_names = {
            "LC1", "LC2", "LC3", "LC4",
            "FrameGroup", "Epoch", "Q"
         },
         mspc_var_names =
            {"MSPC", "FrameGroup", "Epoch", "Q"},
         sspc_var_names =
            {"SSPC", "FrameGroup", "Epoch", "Q"},
         hkpg_var_names = {
            "V0_VoltAtLoad", "I0_TotalLoad", "V1_Battery", "I1_TotalSolar", 
            "V2_Solar1", "I2_Solar1", "V3_POS_DPU", "I3_POS_DPU",
            "V4_POS_XRayDet", "I4_POS_XRayDet","V5_Modem", "I5_Modem",
            "V6_NEG_XRayDet",  "I6_NEG_XRayDet", "V7_NEG_DPU", "I7_NEG_DPU",
            "T0_Scint", "T8_Solar1", "T1_Mag", "T9_Solar2", "T2_ChargeCont",
            "T10_Solar3", "T3_Battery", "T11_Solar4", "T4_PowerConv",
            "T12_TermTemp", "T5_DPU", "T13_TermBatt", "T6_Modem", "T14_TermCap",
            "T7_Structure", "T15_CCStat", "V8_Mag", "V9_Solar2", "V10_Solar3",
            "V11_Solar4", "numOfSats", "timeOffset", "weeks", "termStatus", 
            "cmdCounter", "dcdCounter", "modemCounter",
            "FrameGroup", "Epoch", "Q"
         };
         
      int gps_q = 0, rcnt_q = 0, mspc_q = 0, sspc_q = 0, hkpg_q = 0;
      int mod4 = 0, mod32 = 0, mod40 = 0;
      int frameGrp4 = 0, frameGrp32 = 0, frameGrp40 = 0;
      
      System.out.println(
         "Creating Level One... (" + data.getSize() + " frames)");
      
      for(int frm_i = 0; frm_i < data.getSize(); frm_i++){
         System.out.println(
            "L1 for " + id + " on " + date + ": " + 
            frm_i + " (" + (100 * frm_i) / data.getSize() + "%)"
         );
         
         //get all the frame groups and mux index
         mod4 = data.frameNum[frm_i] % 4;
         mod32 = data.frameNum[frm_i] % 32;
         mod40 = data.frameNum[frm_i] % 40;
         frameGrp4 = data.frameNum[frm_i] - mod4;
         frameGrp32 = data.frameNum[frm_i] - mod32;
         frameGrp40 = data.frameNum[frm_i] - mod40;
         
         //GPS
         if(mod4 == 0){
            gps_data.add(4, Integer.valueOf(frameGrp4));
            gps_data.add(5, Long.valueOf(data.epoch[frm_i]));
         }else
         //make sure we are still in the same frame group
         if(frameGrp4 != gps_data.get(4)){
            //set the quality bit for an incomplete group
            gps_q |= INCOMPLETE_GROUP;
            gps_data.add(6, Long.valueOf(gps_q));
            
            //write the record we have so far and clear data vector
            gps_cdf.putRecord(gps_rec_num, gps_var_names, gps_data);
            gps_data = new Vector(Arrays.asList(test));
            
            //start a new record 
            gps_rec_num++;
            gps_q = 0;
            gps_data.add(4, Integer.valueOf(frameGrp4));
            gps_data.add(5, Long.valueOf(data.epoch[frm_i]));   
         }
         
         //OR in the quality of this frame
         gps_q |= data.quality[frm_i];
         
         switch(mod4){
            case 0:
               //save altitude
               gps_data.add(0, Long.valueOf(data.gps_raw[frm_i]));
               break;
            case 1:
               //add ms_of_week variable
               gps_data.add(1, Long.valueOf(data.ms_of_week[frm_i]));
               break;
            case 2:
               //add lat variable
               gps_data.add(2, Long.valueOf(data.gps_raw[frm_i]));
               break;
            case 3:
               //add lon variable
               gps_data.add(3, Long.valueOf(data.gps_raw[frm_i]));
               
               //add the quality flag
               gps_data.add(6, Long.valueOf(gps_q));
               
               //This is the last frame in the set, write the record
               gps_cdf.putRecord(gps_rec_num, gps_var_names, gps_data);
               gps_data = new Vector(7, 1);
               gps_q = 0;
               
               gps_rec_num++;
               break; 
            default: break;
         }
         System.out.println("did gps");
         //PPS
         pps_data = new Vector(6, 1);
         pps_data.add(0, Integer.valueOf(data.pps[frm_i]));
         pps_data.add(1, Short.valueOf(data.payID[frm_i]));
         pps_data.add(2, Short.valueOf(data.ver[frm_i]));
         pps_data.add(3, Integer.valueOf(data.frameNum[frm_i]));
         pps_data.add(4, Long.valueOf(data.epoch[frm_i]));
         pps_data.add(5, Long.valueOf(data.quality[frm_i]));
         
         pps_cdf.putRecord(pps_rec_num, pps_var_names, pps_data);
         pps_rec_num++;
         
         //B
         for(int set_i = 0; set_i < 4; set_i++){
            mag_data = new Vector(6, 1);
            
            mag_data.add(0, Long.valueOf(data.magx_raw[frm_i][set_i]));
            mag_data.add(1, Long.valueOf(data.magy_raw[frm_i][set_i]));
            mag_data.add(2, Long.valueOf(data.magz_raw[frm_i][set_i]));
            mag_data.add(3, Integer.valueOf(data.frameNum[frm_i]));
            mag_data.add(
               4, Long.valueOf(data.epoch[frm_i] + (250000000 * set_i))
            );
            mag_data.add(5, Long.valueOf(data.quality[frm_i]));
            
            mag_cdf.putRecord(mag_rec_num, mag_var_names, mag_data);   
            mag_rec_num++;
         }
         
         //HKPG
         if(mod40 == 0){ //start a new record for each new group of frames
            //save the frame group and epoch info for this set
            hkpg_data.add(43, Integer.valueOf(frameGrp40));
            hkpg_data.add(44, Long.valueOf(data.epoch[frm_i]));
         }else if(frameGrp40 != gps_data.get(43)){
            //check to make sure we are still in the same frame group
            //if not, put the incomplete record and start a new one
            
            //set the quality bit for an incomplete group
            hkpg_q |= INCOMPLETE_GROUP;
            hkpg_data.add(45, Long.valueOf(hkpg_q));
            
            //write the record we have so far and clear the arrays
            hkpg_cdf.putRecord(hkpg_rec_num, hkpg_var_names, hkpg_data);
            hkpg_data = new Vector(46, 1);
            
            //start a new record 
            hkpg_rec_num++;
            hkpg_q = 0;
            hkpg_data.add(43, Integer.valueOf(frameGrp40));
            hkpg_data.add(44, Long.valueOf(data.epoch[frm_i]));
         }
            
         //OR in the quality flag
         hkpg_q |= data.quality[frm_i];
         
         //add the appropriate variables
         switch(mod40){
            case 36:
               hkpg_data.add(36, Short.valueOf(data.sats[frm_i]));
               hkpg_data.add(37, Short.valueOf(data.offset[frm_i]));
               break;
            case 37:
               weeks = data.weeks[frm_i];
               hkpg_data.add(38, Integer.valueOf(data.weeks[frm_i]));
               break;
            case 38:
               hkpg_data.add(39, Short.valueOf(data.termStat[frm_i]));
               hkpg_data.add(40, Integer.valueOf(data.cmdCnt[frm_i]));
               break;
            case 39:
               hkpg_data.add(41, Short.valueOf(data.dcdCnt[frm_i]));
               hkpg_data.add(42, Short.valueOf(data.modemCnt[frm_i]));
              
               //last frame for this record
               //add quality flag
               hkpg_data.add(45, Long.valueOf(hkpg_q));
               
               //write the record and clear the data array
               hkpg_cdf.putRecord(hkpg_rec_num, hkpg_var_names, hkpg_data);
               hkpg_data = new Vector(46, 1);
               hkpg_q = data.quality[frm_i];
               hkpg_rec_num++;
               
               break;
            default:
               hkpg_data.add(mod40, Long.valueOf(data.hkpg_raw[frm_i]));
               break;
         }
         
         //FSPC
         for(int set_i = 0; set_i < 20; set_i++){
            //Add epoch and time offsets because data comes at 20Hz
            fspc_data.add(0, data.lc1_raw[frm_i][set_i]);
            fspc_data.add(1, data.lc2_raw[frm_i][set_i]);
            fspc_data.add(2, data.lc3_raw[frm_i][set_i]);
            fspc_data.add(3, data.lc4_raw[frm_i][set_i]);
            fspc_data.add(4, Integer.valueOf(data.frameNum[frm_i]));
            fspc_data.add(
               5, Long.valueOf(data.epoch[frm_i] + (50000000 * set_i))
            );
            fspc_data.add(6, Long.valueOf(data.quality[frm_i]));
            
            fspc_cdf.putRecord(fspc_rec_num, fspc_var_names, fspc_data);
            fspc_data = new Vector(7, 1);
            fspc_rec_num++;
         }
         
         //MSPC
         if(mod4 == 0){
            mspc_data.add(1, Integer.valueOf(frameGrp4));
            mspc_data.add(2, Long.valueOf(data.epoch[frm_i]));
         }else if(frameGrp4 != gps_data.get(1)){
            //set the quality bit for an incomplete group
            mspc_q |= INCOMPLETE_GROUP;
            mspc_data.add(3, Long.valueOf(mspc_q));
            
            //get whatever part of the spectrum buffer we have
            mspc_data.add(0, mspc_buff);
            mspc_buff = new Integer[48];
            
            //write the record we have so far and clear the arrays
            mspc_cdf.putRecord(mspc_rec_num, mspc_var_names, mspc_data);
            mspc_data = new Vector(4, 1);
            
            //start a new record 
            mspc_rec_num++;
            mspc_q = 0;
            mspc_data.add(1, Integer.valueOf(frameGrp40));
            mspc_data.add(2, Long.valueOf(data.epoch[frm_i]));
         }
         
         //save the data from this frame
         for(int chan_i = 0; chan_i < 12; chan_i++){
            mspc_buff[((12 * mod4) + chan_i)] = data.mspc_raw[frm_i][chan_i];
         }
         
         //OR in the quality flag
         mspc_q |= data.quality[frm_i];
         
         if(mod4 == 3){
            //Add the quality flag
            mspc_data.add(3, Long.valueOf(mspc_q));
            
            //add the spectrum buffer into the data record
            mspc_data.add(0, mspc_buff);
            
            mspc_cdf.putRecord(mspc_rec_num, mspc_var_names, mspc_data);
            mspc_data = new Vector(4, 1);
            mspc_buff = new Integer[48];
            mspc_q = 0;
            mspc_rec_num++;
         }
         
         //SSPC
         if(mod32 == 0){
            sspc_data.add(1, Integer.valueOf(frameGrp4));
            sspc_data.add(2, Long.valueOf(data.epoch[frm_i]));
         }else if(frameGrp4 != gps_data.get(1)){
            //set the quality bit for an incomplete group
            sspc_q |= INCOMPLETE_GROUP;
            sspc_data.add(3, Long.valueOf(sspc_q));
            
            //get whatever part of the spectrum buffer we have
            sspc_data.add(0, sspc_buff);
            sspc_buff = new Integer[256];
            
            //write the record we have so far and clear the arrays
            sspc_cdf.putRecord(sspc_rec_num, sspc_var_names, sspc_data);
            sspc_data = new Vector(4, 1);
            
            //start a new record 
            sspc_rec_num++;
            sspc_q = 0;
            sspc_data.add(1, Integer.valueOf(frameGrp40));
            sspc_data.add(2, Long.valueOf(data.epoch[frm_i]));   
         }
         
         //save the data from this frame
         for(int chan_i = 0; chan_i < 8; chan_i++){
            sspc_buff[((8 * mod32) + chan_i)] = data.sspc_raw[frm_i][chan_i];
         }
         
         //OR in the quality flag
         sspc_q |= data.quality[frm_i];
         
         if(mod32 == 31){
            //Add the quality flag
            sspc_data.add(3, Long.valueOf(sspc_q));
            
            //add the spectrum buffer into the data record
            sspc_data.add(0, sspc_buff);
            
            sspc_cdf.putRecord(sspc_rec_num, sspc_var_names, sspc_data);
            sspc_data = new Vector(4, 1);
            sspc_buff = new Integer[256];
            sspc_q = 0;
            sspc_rec_num++;
         }
         
         //RC
         rcnt_data.add(mod4, Long.valueOf(data.rc_raw[frm_i]));
         if(mod4 == 0){
            rcnt_data.add(4, Integer.valueOf(frameGrp4));
            rcnt_data.add(5, Long.valueOf(data.epoch[frm_i]));
         }else if(frameGrp4 != gps_data.get(4)){
            //set the quality bit for an incomplete group
            rcnt_q |= INCOMPLETE_GROUP;
            rcnt_data.add(6, Long.valueOf(rcnt_q));
            
            //write the record we have so far and clear the arrays
            rcnt_cdf.putRecord(rcnt_rec_num, rcnt_var_names, rcnt_data);
            rcnt_data = new Vector(7, 1);
            
            //start a new record 
            rcnt_rec_num++;
            rcnt_q = 0;
            rcnt_data.add(4, Integer.valueOf(frameGrp40));
            rcnt_data.add(5, Long.valueOf(data.epoch[frm_i]));
         }
         
         //save the rate counter data
         rcnt_data.add(mod4, Long.valueOf(data.rc_raw[frm_i]));
         //OR in the quality flag
         rcnt_q |= data.quality[frm_i];
         
         if(mod4 == 3){
            //last frame in this record
            
            //Add the quality flag
            sspc_data.add(3, Long.valueOf(rcnt_q));
            
            rcnt_cdf.putRecord(rcnt_rec_num, rcnt_var_names, rcnt_data);
            rcnt_data = new Vector(7, 1);
            rcnt_q = 0;
            rcnt_rec_num++;
         }
         
         //Done with this frame
         lastFrame = data.frameNum[frm_i];
         
      }
      
      gps_cdf.close();
      mag_cdf.close();
      rcnt_cdf.close();
      fspc_cdf.close();
      mspc_cdf.close();
      sspc_cdf.close();
      hkpg_cdf.close();
      pps_cdf.close();
      
      System.out.println("Created Level One.");
   }
 }
