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
        if((data.gps_raw[2][rec_i] >> 31) > 0){
           lat[rec_i] -=  0x100000000L;
        }
        lat[rec_i] *= 
           Float.intBitsToFloat(Integer.valueOf("33B40000",16).intValue());

        lon[rec_i] = (float)data.gps_raw[3][rec_i];
        if((data.gps_raw[3][rec_i] >> 31) > 0){
           lon[rec_i] -=  0x100000000L;
        }
        lon[rec_i] *= 
           Float.intBitsToFloat(Integer.valueOf("33B40000",16).intValue());
      }

      //open GPS CDF and save the reference in the cur_cdf variable
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
         0, data.getSize("1Hz"), 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.pps
      );

      var = cdf.getVariable("Version");
      System.out.println("Version...");
      var.putHyperData(
         0, data.getSize("1Hz"), 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.ver
      );

      var = cdf.getVariable("Payload_ID");
      System.out.println("Payload_ID...");
      var.putHyperData(
         0, data.getSize("1Hz"), 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.payID
      );

      var = cdf.getVariable("FrameGroup");
      System.out.println("FrameGroup...");
      var.putHyperData(
         0, data.getSize("1Hz"), 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.frame_1Hz
      );

      var = cdf.getVariable("Epoch");
      System.out.println("Epoch...");
      var.putHyperData(
         0, data.getSize("1Hz"), 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.epoch_1Hz
      );

      var = cdf.getVariable("Q");
      System.out.println("Q...");
      var.putHyperData(
         0, data.getSize("1Hz"), 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         data.pps_q
      );

      cdf.close();
   }
   
   public void doMagCdf() throws CDFException{
      int numOfRecs = data.getSize("1Hz");

      CDF cdf;
      Variable var;
      
      double[] 
         magx = new double[numOfRecs],
         magy = new double[numOfRecs],
         magz = new double[numOfRecs],
         magTot = new double[numOfRecs];

      float slopex = 0.0f, slopey = 0.0f, slopez = 0.0f;

      System.out.println("\nSaving Magnetometer Level Two CDF...");
      cdf = CDF_Gen.openCDF( 
         outputPath + "bar1" + flt + "_" + id + "_" + stn +
         "_l2_magn_20" + date +  "_v" + revNum + ".cdf"
      );
     
      //get gain correction slope for this payload
	   try{
         FileReader fr = new FileReader("magGain.cal");
         BufferedReader iniFile = new BufferedReader(fr);
         String line;

         while((line = iniFile.readLine()) != null){
            String[] fields = line.split(",");
            if(fields[0].equals(mag_id)){
               slopex = Float.parseFloat(fields[1]);
               slopey = Float.parseFloat(fields[2]);
               slopez = Float.parseFloat(fields[3]);
               break;
            }
         }      
      }catch(IOException ex){
         System.out.println(
            "Could not read config file: " + ex.getMessage()
         );
      }

      //extract the nominal magnetometer value and calculate |B|
      for(int rec_i = 0; rec_i < numOfRecs; rec_i++){
         magx[rec_i] = (data.magx_raw[rec_i] - 8388608.0) / 83886.070;
         magy[rec_i] = (data.magy_raw[rec_i] - 8388608.0) / 83886.070;
         magz[rec_i] = (data.magz_raw[rec_i] - 8388608.0) / 83886.070;

         magTot[rec_i] = 
            Math.sqrt(
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

      //do gain correction on nominal values
      for(int mag_rec = 0; mag_rec < numOfRecs; mag_rec++){
         int hkpg_rec = mag_rec / 160; //convert from 4Hz to mod40
         float magTemp = data.hkpg_raw[data.T1][hkpg_rec];

         magTemp = (magTemp != 0) ? magTemp * data.hkpg_scale[data.T1] : 20;
System.out.println("Using mag temp: " + magTemp + ". Using x slope: " + slopex + ". Correction: " + (slopex * (magTemp - 20) + 1));
         magx[mag_rec] = magx[mag_rec] * (slopex * (magTemp - 20) + 1);
         magy[mag_rec] = magy[mag_rec] * (slopey * (magTemp - 20) + 1);
         magz[mag_rec] = magz[mag_rec] * (slopez * (magTemp - 20) + 1);
         magTot[mag_rec] = 
            Math.sqrt(
               (magx[mag_rec] * magx[mag_rec]) + 
               (magy[mag_rec] * magy[mag_rec]) +
               (magz[mag_rec] * magz[mag_rec]) 
            );
      }

      //store the gain adjusted values
      var = cdf.getVariable("MAG_X_ADJ");
      System.out.println("Gain Adjusted MAG_X... ");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         magx 
      );

      var = cdf.getVariable("MAG_Y_ADJ");
      System.out.println("Gain Adjusted MAG_Y...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         magy
      );

      var = cdf.getVariable("MAG_Z_ADJ");
      System.out.println("Gain Adjusted MAG_Z...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         magz
      );

      var = cdf.getVariable("Total_ADJ");
      System.out.println("Gain Adjusted Field Magnitude...");
      var.putHyperData(
         0, numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         magTot 
      );


      //save the rest of the file
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

   //Pull each value out of the frame and store it in the appropriate CDF.
   private void writeData() throws CDFException{
      //create a holder for the current CDF and Variable
      CDF cur_cdf;
      Variable cur_var;

      System.out.println(
         "Creating Level Two... (" + data.getSize("1Hz") + " frames)"
      );
      
      doGpsCdf();
      doPpsCdf();
      doMagCdf();
         
         
      //HKPG
         System.out.println("\nSaving HKPG...");
         cur_cdf = CDF_Gen.openCDF( 
            outputPath + "bar1" + flt + "_" + id + "_" + stn +
            "_l2_hkpg_20" + date +  "_v" + revNum + ".cdf"
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
            "_l2_fspc_20" + date +  "_v" + revNum + ".cdf"
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
            "_l2_mspc_20" + date +  "_v" + revNum + ".cdf"
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
            "_l2_sspc_20" + date +  "_v" + revNum + ".cdf"
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
            "_l2_rcnt_20" + date +  "_v" + revNum + ".cdf"
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
            0, (data.getSize("mod32")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.frame_mod4
         );

         cur_var = cur_cdf.getVariable("Epoch");
         System.out.println("Epoch...");
         cur_var.putHyperData(
            0, (data.getSize("mod32")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1},
            data.epoch_mod4
         );

         cur_var = cur_cdf.getVariable("Q");
         System.out.println("Q...");
         cur_var.putHyperData(
            0, (data.getSize("mod32")), 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            data.rcnt_q
         );

      cur_cdf.close();
      
      System.out.println("Created Level Two.");
   }
 }
