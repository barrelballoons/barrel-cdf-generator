package edu.ucsc.barrel.cdf_gen;

import gsfc.nssdc.cdf.CDF;
import gsfc.nssdc.cdf.CDFException;
import gsfc.nssdc.cdf.util.CDFTT2000;
import gsfc.nssdc.cdf.Variable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.Vector;
import java.util.Arrays;

/*
LevelTwo.java v13.02.28

Description:
   LevelTwo.java pulls data from the DataHolder.java object and processes it 
   into physical units (when needed) and outputs CDF files in the L2 directory.

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
   v13.03.28
      -Added rebin routines to MSPC and SSPC
   v13.02.28
      -Now outputs correct L2 values for all variables except spectra (Still
         needs rebin)
   v13.02.15
      -Updated to match the current version of Level One
   
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
      revNum = "00",
      mag_id = "";
   Calendar dateObj = Calendar.getInstance();
   
   short INCOMPLETE_GROUP = 8196;
   
   private DataHolder data;
   
   public LevelTwo(
      final String d, final String p, 
      final String f, final String s, final String m
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
      mag_id = m;

      //get the data storage object
      data = CDF_Gen.getDataSet();
      
      //set output path
      outputPath = CDF_Gen.L2_Dir;
      File outDir = new File(outputPath);
      if(!outDir.exists()){outDir.mkdirs();}
      
      //copy the CDF skeletons to the new files 
      for(int type_i = 0; type_i < CDF_Gen.fileTypes.length; type_i++){
         String srcName = 
            "cdf_skels/l2/" + "barCLL_PP_S_l2_" + 
            CDF_Gen.fileTypes[type_i] + "_YYYYMMDD_v++.cdf";
         String destName = 
            outputPath + "bar1" + flt + "_" + id + "_" + stn + "_l2_" +
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
   
   //Convert the GPS data and save it to CDF files
   public void doGpsCdf() throws CDFException{
      int numOfRecs = data.getSize("mod4");
      CDF cdf;
      Variable var;

      float[] 
         lat = new float[numOfRecs], 
         lon = new float[numOfRecs], 
         alt = new float[numOfRecs];
      
      System.out.println("\nSaving GPS Level 2 CDF...");

      //convert lat, lon, and alt values
      for(int rec_i = 0; rec_i < numOfRecs; rec_i++){
        //convert mm to km
        alt[rec_i] = (float)data.gps_raw[0][rec_i] / 1000000;

        //convert lat and lon to physical units
        lat[rec_i] = (float)data.gps_raw[2][rec_i];
        lat[rec_i] *= 
           Float.intBitsToFloat(Integer.valueOf("33B40000",16).intValue());

        lon[rec_i] = (float)data.gps_raw[3][rec_i];
        lon[rec_i] *= 
           Float.intBitsToFloat(Integer.valueOf("33B40000",16).intValue());
      }

      //open GPS CDF and save the reference in the cdf variable
      cdf = CDF_Gen.openCDF( 
         outputPath + "bar1" + flt + "_" + id + "_" + stn +
         "_l2_gps-_20" + date +  "_v" + revNum + ".cdf"
      );

      var = cdf.getVariable("GPS_Alt");
      System.out.println("GPS_Alt...");
      var.putHyperData(
         0, numOfRecs, 1,
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         alt
      );

      var = cdf.getVariable("ms_of_week");
      System.out.println("ms_of_week...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.gps_raw[1]
      );

      var = cdf.getVariable("GPS_Lat");
      System.out.println("GPS_Lat...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         lat 
      );

      var = cdf.getVariable("GPS_Lon");
      System.out.println("GPS_Lon...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         lon
      );

      var = cdf.getVariable("FrameGroup");
      System.out.println("FrameGroup...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.frame_mod4
      );

      var = cdf.getVariable("Epoch");
      System.out.println("Epoch...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.epoch_mod4
      );
      
      var = cdf.getVariable("Q");
      System.out.println("Q...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.gps_q
      );

      System.out.println("Done with GPS!");
      //close current cdf
      cdf.close();
   }
   
   //write the pps file, no processing needed
   public void doPpsCdf() throws CDFException{
      int numOfRecs = data.getSize("1Hz");
      CDF cdf;
      Variable var;
      
      System.out.println("\nSaving PPS Level Two CDF...");

      cdf = CDF_Gen.openCDF( 
         outputPath + "bar1" + flt + "_" + id + "_" + stn +
         "_l2_pps-_20" + date +  "_v" + revNum + ".cdf"
      );
      
      var = cdf.getVariable("GPS_PPS");
      System.out.println("GPS_PPS...");
      var.putHyperData(
         0L, numOfRecs, 1L, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.pps
      );

      var = cdf.getVariable("Version");
      System.out.println("Version...");
      var.putHyperData(
         0L, numOfRecs, 1L, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.ver
      );

      var = cdf.getVariable("Payload_ID");
      System.out.println("Payload_ID...");
      var.putHyperData(
         0L, numOfRecs, 1L, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.payID
      );

      var = cdf.getVariable("FrameGroup");
      System.out.println("FrameGroup...");
      var.putHyperData(
         0L, numOfRecs, 1L, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.frame_1Hz
      );
      var = cdf.getVariable("Epoch");
      System.out.println("Epoch...");
      var.putHyperData(
         0L, numOfRecs, 1L,
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.epoch_1Hz
      );

      var = cdf.getVariable("Q");
      System.out.println("Q...");
      var.putHyperData(
         0L, numOfRecs, 1L, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.pps_q
      );

      cdf.close();
   }
   
   public void doMagCdf() throws CDFException{
      int numOfRecs = data.getSize("4Hz");

      CDF cdf;
      Variable var;
      
      float[] 
         magx = new float[numOfRecs],
         magy = new float[numOfRecs],
         magz = new float[numOfRecs],
         magTot = new float[numOfRecs];

      System.out.println("\nSaving Magnetometer Level Two CDF...");
      cdf = CDF_Gen.openCDF( 
         outputPath + "bar1" + flt + "_" + id + "_" + stn +
         "_l2_magn_20" + date +  "_v" + revNum + ".cdf"
      );
     
      //extract the nominal magnetometer value and calculate |B|
      for(int rec_i = 0; rec_i < numOfRecs; rec_i++){
         magx[rec_i] = (data.magx_raw[rec_i] - 8388608.0f) / 83886.070f;
         magy[rec_i] = (data.magy_raw[rec_i] - 8388608.0f) / 83886.070f;
         magz[rec_i] = (data.magz_raw[rec_i] - 8388608.0f) / 83886.070f;

         magTot[rec_i] = 
            (float)Math.sqrt(
               (magx[rec_i] * magx[rec_i]) + 
               (magy[rec_i] * magy[rec_i]) +
               (magz[rec_i] * magz[rec_i]) 
            );
      }

      //store the nominal mag values
      var = cdf.getVariable("MAG_X");
      System.out.println("MAG_X... ");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         magx 
      );

      var = cdf.getVariable("MAG_Y");
      System.out.println("MAG_Y...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         magy
      );

      var = cdf.getVariable("MAG_Z");
      System.out.println("MAG_Z...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         magz
      );

      var = cdf.getVariable("Total");
      System.out.println("Field Magnitude...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         magTot 
      );

      var = cdf.getVariable("FrameGroup");
      System.out.println("FrameGroup...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.frame_4Hz
      );

      var = cdf.getVariable("Epoch");
      System.out.println("Epoch...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.epoch_4Hz
      );

      var = cdf.getVariable("Q");
      System.out.println("Q...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.magn_q
      );

      cdf.close();

   }
   
   public void doHkpgCdf() throws CDFException{
      CDF cdf;
      Variable var;
      
      int numOfRecs = data.getSize("mod40");

      System.out.println("\nSaving HKPG...");
      cdf = CDF_Gen.openCDF( 
         outputPath + "bar1" + flt + "_" + id + "_" + stn +
         "_l2_hkpg_20" + date +  "_v" + revNum + ".cdf"
      );
         
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
            0, numOfRecs, 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            hkpg_scaled
         );
      }

      var = cdf.getVariable("numOfSats");
      System.out.println("numOfSats...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.sats
      );

      var = cdf.getVariable("timeOffset");
      System.out.println("timeOffset...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.offset
      );
      
      var = cdf.getVariable("termStatus");
      System.out.println("termStatus...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.termStat
      );

      var = cdf.getVariable("cmdCounter");
      System.out.println("cmdCounter...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.cmdCnt
      );

      var = cdf.getVariable("modemCounter");
      System.out.println("modemCounter...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.modemCnt
      );

      var = cdf.getVariable("dcdCounter");
      System.out.println("dcdCounter...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.dcdCnt
      );

      var = cdf.getVariable("weeks");
      System.out.println("weeks...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.weeks
      );

      var = cdf.getVariable("FrameGroup");
      System.out.println("FrameGroup...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.frame_mod40
      );

      var = cdf.getVariable("Epoch");
      System.out.println("Epoch...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.epoch_mod40
      );

      var = cdf.getVariable("Q");
      System.out.println("Q...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.hkpg_q
      );

      cdf.close();
   }

   public void doFspcCdf() throws CDFException{
      CDF cdf;
      Variable var;
      int numOfRecs = data.getSize("20Hz");
      double[][] lc_rebin = new double[4][numOfRecs];
      double[] new_edges = SpectrumExtract.stdEdges(0, 2.4);
      int[] tempLC = new int[20];

      System.out.println("\nSaving FSPC...");
      cdf = CDF_Gen.openCDF( 
         outputPath + "bar1" + flt + "_" + id + "_" + stn +
         "_l2_fspc_20" + date +  "_v" + revNum + ".cdf"
      );
      
       
      //rebin and save the light curves
      for(int rec_i = 0; rec_i < numOfRecs; rec_i++){
         //rebin each spectrum created from the 4 light curves
         for(int spc_i = 0; spc_i < 20; spc_i++){
            //create the spectrum
            tempLC[0] = data.lc1_raw[rec_i];
            tempLC[1] = data.lc2_raw[rec_i];
            tempLC[2] = data.lc3_raw[rec_i];
            tempLC[3] = data.lc4_raw[rec_i];
                        
            double[] lc_spec = new double[4]; 

            //write the spectrum to the new array
            lc_rebin[0][rec_i] = lc_spec[0];
            lc_rebin[1][rec_i] = lc_spec[1];
            lc_rebin[2][rec_i] = lc_spec[2];
            lc_rebin[3][rec_i] = lc_spec[3];
         }   
      }
      var = cdf.getVariable("LC1");
      System.out.println("LC1...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         lc_rebin[0]
      );
      
      var = cdf.getVariable("LC2");
      System.out.println("LC2...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         lc_rebin[1]
      );

      var = cdf.getVariable("LC3");
      System.out.println("LC3...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         lc_rebin[2]
      );

      var = cdf.getVariable("LC4");
      System.out.println("LC4...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         lc_rebin[3]
      );

      var = cdf.getVariable("FrameGroup");
      System.out.println("FrameGroup...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.frame_20Hz
      );

      var = cdf.getVariable("Epoch");
      System.out.println("Epoch...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.epoch_20Hz
      );

      var = cdf.getVariable("Q");
      System.out.println("Q...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.fspc_q
      );

      cdf.close();

   }

   public void doMspcCdf() throws CDFException{
      CDF cdf;
      Variable var;
      
      double peak = -1, scint_temp = 0, dpu_temp = 0;
      int hkpg_rec = 0;
      
      SpectrumExtract spectrum = new SpectrumExtract();
      int offset = 90;

      int numOfRecs = data.getSize("mod4");

      double[][] mspc_rebin = new double[numOfRecs][48];

      double[] new_edges = new double[49];
      
      //rebin the mspc spectra
      for(int mspc_rec = 0; mspc_rec < numOfRecs; mspc_rec++){
         /*
         //copy the int array into a double array and convert to cnts/sec
         for(int val_i = 0; val_i < 48; val_i++){
            mspc_rebin[mspc_rec][val_i] = data.mspc_raw[mspc_rec][val_i];
         }
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
         */ 
         //find the bin that contains the 511 line
         //peak = spectrum.find511(mspc_rebin[mspc_rec], offset);
      
         //get the adjusted bin edges
         //new_edges = spectrum.createBinEdges(1, scint_temp, dpu_temp, peak);
         //mspc_rebin[mspc_rec] = spectrum.rebin(
         //   mspc_rebin[mspc_rec], SpectrumExtract.edges_raw[1], new_edges, 
         //   49, 49, true 
         //);

         //copy the int array into a double array and convert to cnts/keV/sec
         new_edges = SpectrumExtract.stdEdges(1, 2.4);
            
         mspc_rebin[mspc_rec] = new double[48];
      }

      System.out.println("\nSaving MSPC...");
      cdf = CDF_Gen.openCDF( 
         outputPath + "bar1" + flt + "_" + id + "_" + stn +
         "_l2_mspc_20" + date +  "_v" + revNum + ".cdf"
      );

      var = cdf.getVariable("MSPC");
      System.out.println("Spectrum Arrays...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0, 0}, 
         new long[] {48, 1}, 
         new long[] {1, 1}, 
         mspc_rebin
      );

      var = cdf.getVariable("FrameGroup");
      System.out.println("FrameGroup...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.frame_mod4
      );

      var = cdf.getVariable("Epoch");
      System.out.println("Epoch...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.epoch_mod4
      );

      var = cdf.getVariable("Q");
      System.out.println("Q...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.mspc_q
      );

      cdf.close();
   }

   public void doSspcCdf() throws CDFException{
      CDF cdf;
      Variable var;
      
      double peak = -1, scint_temp = 0, dpu_temp = 0;
      int hkpg_rec = 0;
      
      SpectrumExtract spectrum = new SpectrumExtract();
      int offset = 90;

      int numOfRecs = data.getSize("mod32");
      double[][] sspc_rebin = new double[numOfRecs][256];
      double[] old_edges = new double[257];
      double[] std_edges = SpectrumExtract.stdEdges(2, 2.4);
      
      //rebin the sspc spectra
      for(int sspc_rec = 0; sspc_rec < numOfRecs; sspc_rec++){
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
         sspc_rebin[sspc_rec] = spectrum.rebin(
            data.sspc_raw[sspc_rec], old_edges, std_edges, 257, 257, true 
         );
      }

      System.out.println("\nSaving SSPC...");
      cdf = CDF_Gen.openCDF( 
         outputPath + "bar1" + flt + "_" + id + "_" + stn +
         "_l2_sspc_20" + date +  "_v" + revNum + ".cdf"
      );

      var = cdf.getVariable("SSPC");
      System.out.println("Spectrum Arrays...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {256, 1}, 
         new long[] {1}, 
         sspc_rebin
      );

      var = cdf.getVariable("FrameGroup");
      System.out.println("FrameGroup...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.frame_mod32
      );

      var = cdf.getVariable("Epoch");
      System.out.println("Epoch...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.epoch_mod32
      );

      var = cdf.getVariable("Q");
      System.out.println("Q...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.sspc_q
      );

      cdf.close();
   }

   public void doRcntCdf() throws CDFException{
      CDF cdf;
      Variable var;
      
      int numOfRecs = data.getSize("mod4");
      double[][] rc_timeScaled = new double[4][numOfRecs];

      //change all the units from cnts/4sec to cnts/sec
      for(int var_i = 0; var_i < 4; var_i++){
         for(int rec_i = 0; rec_i < numOfRecs; rec_i++){
            rc_timeScaled[var_i][rec_i] = data.rcnt_raw[var_i][rec_i] / 4;
         }
      }

      System.out.println("\nSaving RCNT...");
      cdf = CDF_Gen.openCDF( 
         outputPath + "bar1" + flt + "_" + id + "_" + stn +
         "_l2_rcnt_20" + date +  "_v" + revNum + ".cdf"
      );

      var = cdf.getVariable("Interrupt");
      System.out.println("Interrupt...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         rc_timeScaled[0]
      );

      var = cdf.getVariable("LowLevel");
      System.out.println("LowLevel...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         rc_timeScaled[1]
      );

      var = cdf.getVariable("PeakDet");
      System.out.println("PeakDet...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         rc_timeScaled[2]
      );

      var = cdf.getVariable("HighLevel");
      System.out.println("HighLevel...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         rc_timeScaled[3]
      );

      var = cdf.getVariable("FrameGroup");
      System.out.println("FrameGroup...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.frame_mod4
      );

      var = cdf.getVariable("Epoch");
      System.out.println("Epoch...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1},
         data.epoch_mod4
      );

      var = cdf.getVariable("Q");
      System.out.println("Q...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.rcnt_q
      );

      cdf.close();
   }

   //Pull each value out of the frame and store it in the appropriate CDF.
   private void writeData() throws CDFException{
      System.out.println(
         "Creating Level Two... (" + data.getSize("1Hz") + " frames)"
      );
      
      doGpsCdf();
      doPpsCdf();
      doMagCdf();
      doHkpgCdf();   
      doFspcCdf();   
      doMspcCdf();   
      doSspcCdf();   
      doRcntCdf();   
         
      System.out.println("Created Level Two.");
   }
 }
