/*
LevelOne.java

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

public class LevelOne extends CDFWriter{

   public LevelOne(
      final String d, final String p, 
      final String f, final String s, final String dir
   ) throws IOException
   {
      super(d, p, f, s, dir, "Level One");
   }
   
   //Saveve the EPHM data to CDF the file
   public void doGpsCdf(int first, int last, int date) throws CDFException{
      CDF cdf;
      Variable var;
      int numOfRecs = last - first;
      int[] 
         frameGroup = new int[numOfRecs],
         q = new int[numOfRecs]; 
      long[] epoch = new long[numOfRecs];
      int[][] gps = new int[4][numOfRecs];

      System.out.println("\nSaving EPHM Level One CDF...");

      //select values for this date
      for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
        //convert lat and lon to physical units
        gps[Constants.ALT_I][rec_i] = CDF_Gen.data.gps[Constants.ALT_I][data_i];
        gps[Constants.TIME_I][rec_i]=CDF_Gen.data.gps[Constants.TIME_I][data_i];
        gps[Constants.LAT_I][rec_i] = CDF_Gen.data.gps[Constants.LAT_I][data_i];
        gps[Constants.LON_I][rec_i] = CDF_Gen.data.gps[Constants.LON_I][data_i];
        frameGroup[rec_i] = CDF_Gen.data.frame_mod4[data_i];
        epoch[rec_i] = CDF_Gen.data.epoch_mod4[data_i] - Constants.SING_ACCUM;
        q[rec_i] = CDF_Gen.data.gps_q[data_i];
      }

      //make sure there is a CDF file to open
      //(copyFile() will not clobber an existing file)
      String srcName = 
         "cdf_skels/l1/barCLL_PP_S_l1_ephm_YYYYMMDD_v++.cdf";
      String destName = 
         outputPath + "/" + date + "/" + "bar1" + flt + "_" + id + "_" + stn + 
         "_l1_" + "ephm" + "_20" + date +  "_v" + revNum + ".cdf";

      copyFile(new File(srcName), new File(destName), false);

      //open EPHM CDF and save the reference in the cdf variable
      cdf = openCDF(destName);
      
      var = cdf.getVariable("GPS_Alt");
      System.out.println("GPS_Alt...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1,
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         gps[Constants.ALT_I]
      );

      var = cdf.getVariable("ms_of_week");
      System.out.println("ms_of_week...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         gps[Constants.TIME_I]
      );

      var = cdf.getVariable("GPS_Lat");
      System.out.println("GPS_Lat...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         gps[Constants.LAT_I] 
      );

      var = cdf.getVariable("GPS_Lon");
      System.out.println("GPS_Lon...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         gps[Constants.LON_I]
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

      System.out.println("Done with EPHM!");
      //close current cdf
      cdf.close();
   }
   
   //write the misc file
   public void doMiscCdf(int first, int last, int date) throws CDFException{
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
      long[] 
         epoch = new long[numOfRecs];
      double[] 
         slope = new double[numOfRecs],
         intercept = new double[numOfRecs];

      System.out.println("\nSaving MISC Level One CDF...");

      for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
        pps[rec_i] = CDF_Gen.data.pps[data_i];
        version[rec_i] = CDF_Gen.data.ver[data_i];
        payID[rec_i] = CDF_Gen.data.payID[data_i];
        slope[rec_i] = CDF_Gen.data.time_model_slope[data_i];
        intercept[rec_i] = CDF_Gen.data.time_model_intercept[rec_i];
        frameGroup[rec_i] = CDF_Gen.data.frame_1Hz[data_i];
        epoch[rec_i] = CDF_Gen.data.epoch_1Hz[data_i] - Constants.SING_ACCUM;
        q[rec_i] = CDF_Gen.data.pps_q[data_i];
      }

      String srcName = 
         "cdf_skels/l1/barCLL_PP_S_l1_misc_YYYYMMDD_v++.cdf";
      String destName = 
         outputPath  + "/" + date + "/" + "bar1" + flt + "_" + id + "_" + stn + 
         "_l1_" + "misc" + "_20" + date +  "_v" + revNum + ".cdf";
      copyFile(new File(srcName), new File(destName), false);

      cdf = openCDF(destName);
      
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

      var = cdf.getVariable("Time_Model_Slope");
      System.out.println("Time_Model_Slope...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1L, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         slope 
      );

      var = cdf.getVariable("Time_Model_Intercept");
      System.out.println("Time_Model_Intercept...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1L, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         intercept
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
         q = new int[numOfRecs], 
         magx = new int[numOfRecs],
         magy = new int[numOfRecs],
         magz = new int[numOfRecs];
      long[] epoch = new long[numOfRecs];

      System.out.println("\nSaving Magnetometer Level One CDF...");

      String srcName = 
         "cdf_skels/l1/barCLL_PP_S_l1_magn_YYYYMMDD_v++.cdf";
      String destName = 
         outputPath + "/" + date + "/" + "bar1" + flt + "_" + id + "_" + stn + 
         "_l1_" + "magn" + "_20" + date +  "_v" + revNum + ".cdf";
      copyFile(new File(srcName), new File(destName), false);

      cdf = openCDF(destName);
     
      for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
         magx[rec_i] = CDF_Gen.data.magx[data_i];
         magy[rec_i] = CDF_Gen.data.magy[data_i];
         magz[rec_i] = CDF_Gen.data.magz[data_i];
         frameGroup[rec_i] = CDF_Gen.data.frame_4Hz[data_i];
         epoch[rec_i] = CDF_Gen.data.epoch_4Hz[data_i] - Constants.SING_ACCUM;
         q[rec_i] = CDF_Gen.data.magn_q[data_i];
      }

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
      long[] hkpg = new long[numOfRecs];

      System.out.println("\nSaving HKPG...");

      String srcName = 
         "cdf_skels/l1/barCLL_PP_S_l1_hkpg_YYYYMMDD_v++.cdf";
      String destName = 
         outputPath + "/" + date + "/" + "bar1" + flt + "_" + id + "_" + stn 
         + "_l1_" + "hkpg" + "_20" + date +  "_v" + revNum + ".cdf";

      copyFile(new File(srcName), new File(destName), false);

      cdf = openCDF(destName);
         
      for(int var_i = 0; var_i < 36; var_i++){
         hkpg = new long[numOfRecs];
         for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
            hkpg[rec_i] =  CDF_Gen.data.hkpg[var_i][data_i];
         }

         var = cdf.getVariable(CDF_Gen.data.hkpg_label[var_i]);
         System.out.println(CDF_Gen.data.hkpg_label[var_i] + "...");
         var.putHyperData(
            var.getNumWrittenRecords(), numOfRecs, 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            hkpg
         );
      }

      for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
         sats[rec_i] = CDF_Gen.data.sats[data_i];
         offset[rec_i] = CDF_Gen.data.offset[data_i];
         termStat[rec_i] = CDF_Gen.data.termStat[data_i];
         modemCnt[rec_i] = CDF_Gen.data.modemCnt[data_i];
         dcdCnt[rec_i] = CDF_Gen.data.dcdCnt[data_i];
         cmdCnt[rec_i] = CDF_Gen.data.cmdCnt[data_i];
         frameGroup[rec_i] = CDF_Gen.data.frame_mod40[data_i];
         weeks[rec_i] = CDF_Gen.data.weeks[data_i];
         epoch[rec_i] = CDF_Gen.data.epoch_mod40[data_i] - Constants.SING_ACCUM;
         q[rec_i] = CDF_Gen.data.hkpg_q[data_i];
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

      int[] 
         frameGroup = new int[numOfRecs],
         q = new int[numOfRecs];
      long[] epoch = new long[numOfRecs];
      int[][]
         lc = new int[4][numOfRecs];

      System.out.println("\nSaving FSPC...");

      String srcName = 
         "cdf_skels/l1/barCLL_PP_S_l1_fspc_YYYYMMDD_v++.cdf";
      String destName = 
         outputPath + "/" + date + "/" + "bar1" + flt + "_" + id + "_" + stn 
         + "_l1_" + "fspc" + "_20" + date +  "_v" + revNum + ".cdf";
      copyFile(new File(srcName), new File(destName), false);

      cdf = openCDF(destName);
      
      for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
         frameGroup[rec_i] = CDF_Gen.data.frame_20Hz[data_i];
         epoch[rec_i] = CDF_Gen.data.epoch_20Hz[data_i] - Constants.SING_ACCUM;
         q[rec_i] = CDF_Gen.data.fspc_q[data_i];
         lc[0][rec_i] = CDF_Gen.data.lc1[data_i];
         lc[1][rec_i] = CDF_Gen.data.lc2[data_i];
         lc[2][rec_i] = CDF_Gen.data.lc3[data_i];
         lc[3][rec_i] = CDF_Gen.data.lc4[data_i];
      }

      var = cdf.getVariable("LC1");
      System.out.println("LC1...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         lc[0]
      );
      
      var = cdf.getVariable("LC2");
      System.out.println("LC2...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         lc[1]
      );

      var = cdf.getVariable("LC3");
      System.out.println("LC3...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         lc[2]
      );

      var = cdf.getVariable("LC4");
      System.out.println("LC4...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         lc[3]
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
      
      int numOfRecs = last - first;
      int[] 
         frameGroup = new int[numOfRecs],
         q = new int[numOfRecs];
      long[] epoch = new long[numOfRecs];

      int[][] mspc = new int[numOfRecs][48];
      
      for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
         frameGroup[rec_i] = CDF_Gen.data.frame_mod4[data_i];
         epoch[rec_i] = CDF_Gen.data.epoch_mod4[data_i] - Constants.QUAD_ACCUM;
         q[rec_i] = CDF_Gen.data.mspc_q[data_i];
         mspc[rec_i] = CDF_Gen.data.mspc[data_i];

      }
      System.out.println("\nSaving MSPC...");

      String srcName = 
         "cdf_skels/l1/barCLL_PP_S_l1_mspc_YYYYMMDD_v++.cdf";
      String destName = 
         outputPath  + "/" + date + "/"+ "bar1" + flt + "_" + id + "_" + stn 
         + "_l1_" + "mspc" + "_20" + date +  "_v" + revNum + ".cdf";

      copyFile(new File(srcName), new File(destName), false);

      cdf = openCDF(destName);

      var = cdf.getVariable("MSPC");
      System.out.println("Spectrum Arrays...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0, 0}, 
         new long[] {48, 1}, 
         new long[] {1, 1}, 
         mspc
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
      
      int numOfRecs = last - first;
      int[] 
         frameGroup = new int[numOfRecs],
         q = new int[numOfRecs];
      long[] epoch = new long[numOfRecs];
      int[][] sspc = new int[numOfRecs][256];

      for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
         frameGroup[rec_i] = CDF_Gen.data.frame_mod32[data_i];
         epoch[rec_i] = CDF_Gen.data.epoch_mod32[data_i] - Constants.SSPC_ACCUM;
         q[rec_i] = CDF_Gen.data.sspc_q[data_i];
         sspc[rec_i] = CDF_Gen.data.sspc[data_i];
      }

      System.out.println("\nSaving SSPC...");

      String srcName = 
         "cdf_skels/l1/barCLL_PP_S_l1_sspc_YYYYMMDD_v++.cdf";
      String destName = 
         outputPath + "/" + date + "/" + "bar1" + flt + "_" + id + "_" + stn +
         "_l1_" + "sspc" + "_20" + date +  "_v" + revNum + ".cdf";
      copyFile(new File(srcName), new File(destName), false);

      cdf = openCDF(destName);

      var = cdf.getVariable("SSPC");
      System.out.println("Spectrum Arrays...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {256, 1}, 
         new long[] {1}, 
         sspc
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
      int[] 
         frameGroup = new int[numOfRecs],
         q = new int[numOfRecs];
      long[] epoch = new long[numOfRecs];
      long[][] rc = new long[4][numOfRecs];

      for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
         frameGroup[rec_i] = CDF_Gen.data.frame_mod4[data_i];
         epoch[rec_i] = CDF_Gen.data.epoch_mod4[data_i] - Constants.QUAD_ACCUM;
         q[rec_i] = CDF_Gen.data.rcnt_q[data_i];
         rc[0][rec_i] = CDF_Gen.data.rcnt[0][data_i];
         rc[1][rec_i] = CDF_Gen.data.rcnt[1][data_i];
         rc[2][rec_i] = CDF_Gen.data.rcnt[2][data_i];
         rc[3][rec_i] = CDF_Gen.data.rcnt[3][data_i];
      }
         
      System.out.println("\nSaving RCNT...");

      String srcName = 
         "cdf_skels/l1/barCLL_PP_S_l1_rcnt_YYYYMMDD_v++.cdf";
      String destName = 
         outputPath + "/" + date + "/"  + "bar1" + flt + "_" + id + "_" + stn
         + "_l1_" + "rcnt" + "_20" + date +  "_v" + revNum + ".cdf";

      copyFile(new File(srcName), new File(destName), false);

      cdf = openCDF(destName);

      var = cdf.getVariable("Interrupt");
      System.out.println("Interrupt...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         rc[0]
      );

      var = cdf.getVariable("LowLevel");
      System.out.println("LowLevel...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         rc[1]
      );

      var = cdf.getVariable("PeakDet");
      System.out.println("PeakDet...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         rc[2]
      );

      var = cdf.getVariable("HighLevel");
      System.out.println("HighLevel...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         rc[3]
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
 }
