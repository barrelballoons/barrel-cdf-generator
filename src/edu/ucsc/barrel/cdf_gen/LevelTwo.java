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
   v13.02.06
   -New version of Level Two. An exact copy of Level One for now...
*/


public class LevelTwo{
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
   
   public LevelTwo(
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
         "Creating Level One... (" + data.getSize() + " frames)"
      );
      
      //GPS//
         //open GPS CDF and save the reference in the cur_cdf variable
         cur_cdf = CDF_Gen.openCDF( 
            outputPath + "bar1" + flt + "_" + id + "_" + stn +
            "_l1_gps-_20" + date +  "_v" + revNum + ".cdf"
         );
         
         //put an entire day's worth of data at once for each CDF variable
         cur_var = cur_cdf.getVariable("GPS_Alt");
         cur_var.putHyperData(
            0, (data.getSize() / 4), 1,
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.gps_raw[0]
         );

         cur_var = cur_cdf.getVariable("ms_of_week");
         cur_var.putHyperData(
            0, (data.getSize() / 4), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.gps_raw[1]
         );

         cur_var = cur_cdf.getVariable("GPS_Lat");
         cur_var.putHyperData(
            0, (data.getSize() / 4), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.gps_raw[2]
         );

         cur_var = cur_cdf.getVariable("GPS_Lon");
         cur_var.putHyperData(
            0, (data.getSize() / 4), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.gps_raw[3]
         );

         cur_var = cur_cdf.getVariable("FrameGroup");
         cur_var.putHyperData(
            0, (data.getSize() / 4), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.frame_mod4
         );

         cur_var = cur_cdf.getVariable("Epoch");
         cur_var.putHyperData(
            0, (data.getSize() / 4), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.epoch_mod4
         );

         cur_var = cur_cdf.getVariable("Q");
         cur_var.putHyperData(
            0, (data.getSize() / 4), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.gps_q
         );

         //close current cdf
         cur_cdf.close();

      //PPS//
         cur_cdf = CDF_Gen.openCDF( 
            outputPath + "bar1" + flt + "_" + id + "_" + stn +
            "_l1_pps-_20" + date +  "_v" + revNum + ".cdf"
         );
         
         cur_var = cur_cdf.getVariable("GPS_PPS");
         cur_var.putHyperData(
            0, data.getSize(), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.pps
         );

         cur_var = cur_cdf.getVariable("Version");
         cur_var.putHyperData(
            0, data.getSize(), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.ver
         );

         cur_var = cur_cdf.getVariable("Payload_ID");
         cur_var.putHyperData(
            0, data.getSize(), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.payID
         );

         cur_var = cur_cdf.getVariable("FrameGroup");
         cur_var.putHyperData(
            0, data.getSize(), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.frame_1Hz
         );

         cur_var = cur_cdf.getVariable("Epoch");
         cur_var.putHyperData(
            0, data.getSize(), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.epoch_1Hz
         );

         cur_var = cur_cdf.getVariable("Q");
         cur_var.putHyperData(
            0, data.getSize(), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.pps_q
         );

         cur_cdf.close();
         
      //B//
         cur_cdf = CDF_Gen.openCDF( 
            outputPath + "bar1" + flt + "_" + id + "_" + stn +
            "_l1_magn_20" + date +  "_v" + revNum + ".cdf"
         );
         
         cur_var = cur_cdf.getVariable("MAG_X");
         cur_var.putHyperData(
            0, (data.getSize() * 20), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.magx_raw
         );

         cur_var = cur_cdf.getVariable("MAG_Y");
         cur_var.putHyperData(
            0, (data.getSize() * 20), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.magy_raw
         );

         cur_var = cur_cdf.getVariable("MAG_Z");
         cur_var.putHyperData(
            0, (data.getSize() * 20), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.magz_raw
         );

         cur_var = cur_cdf.getVariable("FrameGroup");
         cur_var.putHyperData(
            0, (data.getSize() * 20), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.frame_4Hz
         );

         cur_var = cur_cdf.getVariable("Epoch");
         cur_var.putHyperData(
            0, (data.getSize() * 20), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.epoch_4Hz
         );

         cur_var = cur_cdf.getVariable("Q");
         cur_var.putHyperData(
            0, (data.getSize() * 20), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.magn_q
         );

         cur_cdf.close();
         
      //HKPG
         cur_cdf = CDF_Gen.openCDF( 
            outputPath + "bar1" + flt + "_" + id + "_" + stn +
            "_l1_hkpg_20" + date +  "_v" + revNum + ".cdf"
         );
            
         for(int var_i = 0; var_i < 36; var_i++){
            cur_var = cur_cdf.getVariable(data.hkpg_label[var_i]);
            cur_var.putHyperData(
               0, (data.getSize() / 40), 1, 
               new long[] {0}, 
               new long[] {1}, 
               new long[] {1}, 
               data.hkpg_raw[var_i]
            );
         }

         cur_var = cur_cdf.getVariable("numOfSats");
         cur_var.putHyperData(
            0, (data.getSize() / 40), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.sats
         );

         cur_var = cur_cdf.getVariable("timeOffset");
         cur_var.putHyperData(
            0, (data.getSize() / 40), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.offset
         );
         
         cur_var = cur_cdf.getVariable("termStatus");
         cur_var.putHyperData(
            0, (data.getSize() / 40), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.termStat
         );

         cur_var = cur_cdf.getVariable("cmdCounter");
         cur_var.putHyperData(
            0, (data.getSize() / 40), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.cmdCnt
         );

         cur_var = cur_cdf.getVariable("modemCounter");
         cur_var.putHyperData(
            0, (data.getSize() / 40), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.modemCnt
         );

         cur_var = cur_cdf.getVariable("dcdCounter");
         cur_var.putHyperData(
            0, (data.getSize() / 40), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.dcdCnt
         );

         cur_var = cur_cdf.getVariable("weeks");
         cur_var.putHyperData(
            0, (data.getSize() / 40), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.weeks
         );

         cur_var = cur_cdf.getVariable("FrameGroup");
         cur_var.putHyperData(
            0, (data.getSize() / 40), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.frame_mod40
         );

         cur_var = cur_cdf.getVariable("Epoch");
         cur_var.putHyperData(
            0, (data.getSize() / 40), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.epoch_mod40
         );

         cur_var = cur_cdf.getVariable("Q");
         cur_var.putHyperData(
            0, (data.getSize() / 40), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.hkpg_q
         );

         cur_cdf.close();
         
      //FSPC//
         cur_cdf = CDF_Gen.openCDF( 
            outputPath + "bar1" + flt + "_" + id + "_" + stn +
            "_l1_fspc_20" + date +  "_v" + revNum + ".cdf"
         );
         
         cur_var = cur_cdf.getVariable("LC1");
         cur_var.putHyperData(
            0, (data.getSize() * 20), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.lc1_raw
         );

         cur_var = cur_cdf.getVariable("LC2");
         cur_var.putHyperData(
            0, (data.getSize() * 20), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.lc2_raw
         );

         cur_var = cur_cdf.getVariable("LC3");
         cur_var.putHyperData(
            0, (data.getSize() * 20), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.lc3_raw
         );

         cur_var = cur_cdf.getVariable("LC4");
         cur_var.putHyperData(
            0, (data.getSize() * 20), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.lc4_raw
         );

         cur_var = cur_cdf.getVariable("FrameGroup");
         cur_var.putHyperData(
            0, (data.getSize() * 20), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.frame_20Hz
         );

         cur_var = cur_cdf.getVariable("Epoch");
         cur_var.putHyperData(
            0, (data.getSize() * 20), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.epoch_20Hz
         );

         cur_var = cur_cdf.getVariable("Q");
         cur_var.putHyperData(
            0, (data.getSize() * 20), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.fspc_q
         );

         cur_cdf.close();
         
      //MSPC//
         cur_cdf = CDF_Gen.openCDF( 
            outputPath + "bar1" + flt + "_" + id + "_" + stn +
            "_l1_mspc_20" + date +  "_v" + revNum + ".cdf"
         );

         cur_var = cur_cdf.getVariable("MSPC");
         cur_var.putHyperData(
            0, (data.getSize() / 4), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.mspc_raw
         );

         cur_var = cur_cdf.getVariable("FrameGroup");
         cur_var.putHyperData(
            0, (data.getSize() / 4), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.frame_mod4
         );

         cur_var = cur_cdf.getVariable("Epoch");
         cur_var.putHyperData(
            0, (data.getSize() / 4), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.epoch_mod4
         );

         cur_var = cur_cdf.getVariable("Q");
         cur_var.putHyperData(
            0, (data.getSize() / 4), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.mspc_q
         );

         cur_cdf.close();
         
      //SSPC//
         cur_cdf = CDF_Gen.openCDF( 
            outputPath + "bar1" + flt + "_" + id + "_" + stn +
            "_l1_sspc_20" + date +  "_v" + revNum + ".cdf"
         );

         cur_var = cur_cdf.getVariable("SSPC");
         cur_var.putHyperData(
            0, (data.getSize() / 32), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.sspc_raw
         );

         cur_var = cur_cdf.getVariable("FrameGroup");
         cur_var.putHyperData(
            0, (data.getSize() / 32), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.frame_mod32
         );

         cur_var = cur_cdf.getVariable("Epoch");
         cur_var.putHyperData(
            0, (data.getSize() / 32), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.epoch_mod32
         );

         cur_var = cur_cdf.getVariable("Q");
         cur_var.putHyperData(
            0, (data.getSize() / 32), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.sspc_q
         );

         cur_cdf.close();
         
      //RC
         cur_cdf = CDF_Gen.openCDF( 
            outputPath + "bar1" + flt + "_" + id + "_" + stn +
            "_l1_rcnt_20" + date +  "_v" + revNum + ".cdf"
         );

         cur_var = cur_cdf.getVariable("Interrupt");
         cur_var.putHyperData(
            0, (data.getSize() / 4), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.rcnt_raw[0]
         );

         cur_var = cur_cdf.getVariable("LowLevel");
         cur_var.putHyperData(
            0, (data.getSize() / 4), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.rcnt_raw[1]
         );

         cur_var = cur_cdf.getVariable("PeakDet");
         cur_var.putHyperData(
            0, (data.getSize() / 4), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.rcnt_raw[2]
         );

         cur_var = cur_cdf.getVariable("HighLevel");
         cur_var.putHyperData(
            0, (data.getSize() / 4), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.rcnt_raw[3]
         );

         cur_var = cur_cdf.getVariable("FrameGroup");
         cur_var.putHyperData(
            0, (data.getSize() / 32), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.frame_mod4
         );

         cur_var = cur_cdf.getVariable("Epoch");
         cur_var.putHyperData(
            0, (data.getSize() / 32), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1},
            data.epoch_mod4
         );

         cur_var = cur_cdf.getVariable("Q");
         cur_var.putHyperData(
            0, (data.getSize() / 32), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.rcnt_q
         );

      cur_cdf.close();
      
      System.out.println("Created Level One.");
   }
 }
