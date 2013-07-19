/*
LevelTwo.java

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
*/

package edu.ucsc.barrel.cdf_gen;

import gsfc.nssdc.cdf.CDF;
import gsfc.nssdc.cdf.CDFException;
import gsfc.nssdc.cdf.CDFConstants;
import gsfc.nssdc.cdf.util.CDFTT2000;
import gsfc.nssdc.cdf.Variable;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.Vector;
import java.util.Arrays;

public class LevelTwo extends CDFWriter{

   public LevelTwo(
      final String d, final String p, 
      final String f, final String s, final String dir 
   ) throws IOException
   {
      super(d, p, f, s, dir, "Level Two");
   }
   
   //Convert the EPHM data and save it to CDF files
   public void doGpsCdf(int first, int last, int date) throws CDFException{
      CDF cdf;
      Variable var;
      Calendar d = Calendar.getInstance();
      Logger geo_coord_file = new Logger("pay" + id + "_" + date + "_gps.txt");

      int 
         year, month, day, day_of_year, hour, min, sec,
         numOfRecs = last - first;
      double
         sec_of_day = 0;
      float
         east_lon = 0;
      String[] mag_coords;
      float[] 
         lat = new float[numOfRecs], 
         lon = new float[numOfRecs], 
         alt = new float[numOfRecs],
         mlt2 = new float[numOfRecs],
         mlt6 = new float[numOfRecs],
         l2 = new float[numOfRecs],
         l6 = new float[numOfRecs];
      int[] 
         frameGroup = new int[numOfRecs],
         q = new int[numOfRecs]; 
      long[] 
         epoch_parts = new long[9],
         epoch = new long[numOfRecs],
         gps_time = new long[numOfRecs];

      System.out.println("\nSaving EPHM Level Two CDF...");

      //calculate the day of year
      year = date / 10000;
      month = (date - (year * 10000)) / 100;
      day = date - (year * 10000) - (month * 100);
      d.set(Calendar.YEAR, year + 2000);
      d.set(Calendar.MONTH, month - 1);
      d.set(Calendar.DAY_OF_MONTH, day);
      day_of_year = d.get(Calendar.DAY_OF_YEAR);

      //convert lat, lon, and alt values and select values for this date
      for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
         //convert mm to km
         alt[rec_i] = (float)CDF_Gen.data.gps[Constants.ALT_I][data_i];
         if(alt[rec_i] != Constants.ALT_RAW_FILL){
            alt[rec_i] /= 1000000;
         }else{
            alt[rec_i] = (float)Constants.ALT_FILL;
         }

         //convert lat and lon to physical units
         lat[rec_i] = (float)CDF_Gen.data.gps[Constants.LAT_I][data_i];
         if(lat[rec_i] != Constants.LAT_RAW_FILL){
            lat[rec_i] *= 
               Float.intBitsToFloat(Integer.valueOf("33B40000", 16).intValue());
         }else{
            lat[rec_i] = (float)Constants.LAT_FILL;
         }

         lon[rec_i] = (float)CDF_Gen.data.gps[Constants.LON_I][data_i];
         if(lon[rec_i] != Constants.LON_RAW_FILL){
            lon[rec_i] *= 
               Float.intBitsToFloat(Integer.valueOf("33B40000", 16).intValue());
         }else{
            lon[rec_i] = (float)Constants.LON_FILL;
         }

         //calculate the GPS time
         if(CDF_Gen.data.ms_of_week[data_i] != Constants.MS_WEEK_FILL){
            sec = CDF_Gen.data.ms_of_week[data_i] / 1000; //convert ms to sec
            sec %= 86400; //remove any complete days
            hour = sec / 3600;
            sec %= 3600;
            min = sec / 60;
            sec %= 60;
            gps_time[rec_i] = CDFTT2000.compute(
               (long)(year + 2000), (long)(month - 1), (long)day, (long)hour, 
               (long)min, (long)sec, 0L, 0L, 0L
            );  
         }else{
            gps_time[rec_i] = 0;
         }
         
         //save the values from the other variables
         frameGroup[rec_i] = CDF_Gen.data.frame_mod4[data_i];
         epoch[rec_i] = CDF_Gen.data.epoch_mod4[data_i] - Constants.SING_ACCUM;
         q[rec_i] = CDF_Gen.data.gps_q[data_i];

         //make sure we have a complete gps record before generating mag coords
         if(
            (alt[rec_i] != Constants.ALT_FILL) && 
            (lat[rec_i] != Constants.LAT_FILL) && 
            (lon[rec_i] != Constants.LON_FILL)
         ){
         
            //calculate the current time in seconds of day
            epoch_parts = CDFTT2000.breakdown(epoch[rec_i]);
            sec_of_day = 
               (epoch_parts[3] * 3600) + // hours
               (epoch_parts[4] * 60) + //minutes
               epoch_parts[5] + //seconds
               (epoch_parts[6] * 0.001) + //ms
               (epoch_parts[7] * 0.000001) + //us
               (epoch_parts[8] * 0.000000001); //ns
            //convert signed longitude to east longitude
            east_lon = (lon[rec_i] > 0) ? lon[rec_i] : lon[rec_i] + 360;

            geo_coord_file.writeln(
               String.format(
                  "%07d %02.6f %03.6f %03.6f %04d %03d %02.3f", 
                  frameGroup[rec_i], alt[rec_i], lat[rec_i], lon[rec_i],
                  (year + 2000), day_of_year, sec_of_day
               )
            );
         }else{
            geo_coord_file.writeln("NaN NaN NaN NaN NaN NaN NaN");
         }
      }
      geo_coord_file.close();

      //get the magnetic field info for this location
      try{
         String command = 
            CDF_Gen.getSetting("mag_gen") + " " + 
            "pay" + id + "_" + date + "_gps.txt";

         Process p = Runtime.getRuntime().exec(command);
         BufferedReader input =                                           
            new BufferedReader(new InputStreamReader(p.getInputStream()));
         System.out.println(input.readLine()); 
      }catch(IOException ex){
         System.out.println("Could not read gps coordinate file:");
         System.out.println(ex.getMessage());
      }
      
      //Read the magnetic coordinates into a set of arrays
      try{
         BufferedReader mag_coord_file = 
            new BufferedReader(
               new FileReader(
                  "pay" + id + "_" + date + "_gps_out.txt"
               )
            );
         String line;
         int 
            rec_i = 0,
            this_frame = 0,
            last_frame = 0;

         while((line = mag_coord_file.readLine()) != null){
            line = line.trim();
            mag_coords = line.split("\\s+");

            //check for repeated frame
            this_frame = Integer.parseInt(mag_coords[0]);
            if(this_frame != last_frame){
            //make sure the mag coordinates were calculated correctly
               if(mag_coords[8].indexOf("*") == -1){
                  l2[rec_i] = Math.abs(Float.parseFloat(mag_coords[8]));
               }else{
                  l2[rec_i] = Constants.FLOAT_FILL;
               }
               if(mag_coords[9].indexOf("*") == -1){
                  mlt2[rec_i] = Float.parseFloat(mag_coords[9]);
               }else{
                  mlt2[rec_i] = Constants.FLOAT_FILL;
               }
               if(mag_coords[11].indexOf("*") == -1){
                  l6[rec_i] = Math.abs(Float.parseFloat(mag_coords[11]));
               }else{
                  l6[rec_i] = Constants.FLOAT_FILL;
               }
               if(mag_coords[12].indexOf("*") == -1){
                  mlt6[rec_i] = Float.parseFloat(mag_coords[12]);
               }else{
                  mlt6[rec_i] = Constants.FLOAT_FILL;
               }

               last_frame = this_frame;
            }
            else{
               l2[rec_i] = Constants.FLOAT_FILL; 
               l6[rec_i] = Constants.FLOAT_FILL; 
               mlt2[rec_i] = Constants.FLOAT_FILL; 
               mlt6[rec_i] = Constants.FLOAT_FILL; 
            } 

            rec_i++;
         }

         mag_coord_file.close();

         //clean up after ourselves
         geo_coord_file.delete();
         (new File("pay" + id + "_" + date + "_gps_out.txt")).delete();

         CDF_Gen.log.writeln("test");
      }catch(IOException ex){
         System.out.println("Could not read magnetic coordinate file:");
         System.out.println(ex.getMessage());
      }

      //make sure there is a CDF file to open
      //(copyFile will not clobber an existing file)
      String srcName = 
         "cdf_skels/l2/barCLL_PP_S_l2_ephm_YYYYMMDD_v++.cdf";
      String destName = 
         outputPath + "/" + date + "/" + "bar1" + flt + "_" + id + "_" + stn + 
         "_l2_" + "ephm" + "_20" + date +  "_v" + revNum + ".cdf";

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
         alt
      );

      var = cdf.getVariable("GPS_Time");
      System.out.println("GPS_Time");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         gps_time
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

      var = cdf.getVariable("L_Kp2");
      System.out.println("L_Kp2...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         l2
      );

      var = cdf.getVariable("MLT_Kp2");
      System.out.println("MLT_Kp2...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         mlt2
      );

      var = cdf.getVariable("L_Kp6");
      System.out.println("L_Kp6...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         l6
      );

      var = cdf.getVariable("MLT_Kp6");
      System.out.println("MLT_Kp6...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {1}, 
         new long[] {1}, 
         mlt6
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
        pps[rec_i] = CDF_Gen.data.pps[data_i];
        version[rec_i] = CDF_Gen.data.ver[data_i];
        payID[rec_i] = CDF_Gen.data.payID[data_i];
        frameGroup[rec_i] = CDF_Gen.data.frame_1Hz[data_i];
        epoch[rec_i] = CDF_Gen.data.epoch_1Hz[data_i] - Constants.SING_ACCUM;
        q[rec_i] = CDF_Gen.data.pps_q[data_i];
      }

      String srcName = 
         "cdf_skels/l2/barCLL_PP_S_l2_pps-_YYYYMMDD_v++.cdf";
      String destName = 
         outputPath  + "/" + date + "/" + "bar1" + flt + "_" + id + "_" + stn + 
         "_l2_" + "pps-" + "_20" + date +  "_v" + revNum + ".cdf";
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
      copyFile(new File(srcName), new File(destName), false);

      cdf = openCDF(destName);
     
      //extract the nominal magnetometer value and calculate |B|
      for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
         if(CDF_Gen.data.magx[data_i] != Constants.FLOAT_FILL){
            magx[rec_i] = (CDF_Gen.data.magx[data_i] - 8388608.0f) / 83886.070f;
         }else{
            magx[rec_i] = Constants.FLOAT_FILL;
         }
         if(CDF_Gen.data.magy[data_i] != Constants.FLOAT_FILL){
            magy[rec_i] = (CDF_Gen.data.magy[data_i] - 8388608.0f) / 83886.070f;
         }else{
            magx[rec_i] = Constants.FLOAT_FILL;
         }
         if(CDF_Gen.data.magz[data_i] != Constants.FLOAT_FILL){
            magz[rec_i] = (CDF_Gen.data.magz[data_i] - 8388608.0f) / 83886.070f;
         }else{
            magx[rec_i] = Constants.FLOAT_FILL;
         }
         
         if(
            magx[rec_i] != Constants.FLOAT_FILL &&
            magy[rec_i] != Constants.FLOAT_FILL &&
            magz[rec_i] != Constants.FLOAT_FILL 
         ){
            magTot[rec_i] = 
               (float)Math.sqrt(
                  (magx[rec_i] * magx[rec_i]) + 
                  (magy[rec_i] * magy[rec_i]) +
                  (magz[rec_i] * magz[rec_i]) 
               );
         }else{
            magTot[rec_i] = Constants.FLOAT_FILL;
         }

         frameGroup[rec_i] = CDF_Gen.data.frame_4Hz[data_i];
         epoch[rec_i] = CDF_Gen.data.epoch_4Hz[data_i] - Constants.SING_ACCUM;
         q[rec_i] = CDF_Gen.data.magn_q[data_i];
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

      copyFile(new File(srcName), new File(destName), false);

      cdf = openCDF(destName);
         
      for(int var_i = 0; var_i < 36; var_i++){
         //scale all the records for this variable
         double[] hkpg_scaled = new double[numOfRecs];
         for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
            if(CDF_Gen.data.hkpg[var_i][data_i] != Constants.HKPG_FILL){
               hkpg_scaled[rec_i] = 
                  (
                     CDF_Gen.data.hkpg[var_i][data_i] * 
                     CDF_Gen.data.hkpg_scale[var_i]
                  ) + CDF_Gen.data.hkpg_offset[var_i];
            }else{
               hkpg_scaled[rec_i] = Constants.DOUBLE_FILL;
            }
         }

         var = cdf.getVariable(CDF_Gen.data.hkpg_label[var_i]);
         System.out.println(CDF_Gen.data.hkpg_label[var_i] + "...");
         var.putHyperData(
            var.getNumWrittenRecords(), numOfRecs, 1, 
            new long[] {0}, 
            new long[] {1}, 
            new long[] {1}, 
            hkpg_scaled
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

      double[][] 
         chan_edges = new double[numOfRecs][5],
         lc_scaled = new double[4][numOfRecs];
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
      copyFile(new File(srcName), new File(destName), false);

      cdf = openCDF(destName);
      
      //convert the light curves counts to cnts/sec and 
      //figure out the channel width
      for(int lc_rec = 0, hkpg_rec = 0; lc_rec < numOfRecs; lc_rec++){

         //get temperatures
         hkpg_rec = (lc_rec + first) / 20 / 40; //convert from 20Hz to mod40
         if(CDF_Gen.data.hkpg[Constants.T0][hkpg_rec] != Constants.DOUBLE_FILL){
            scint_temp = 
               (CDF_Gen.data.hkpg[Constants.T0][hkpg_rec] * 
               CDF_Gen.data.hkpg_scale[Constants.T0]) + 
               CDF_Gen.data.hkpg_offset[Constants.T0];
         }else{
            scint_temp = 20;
         }
         if(CDF_Gen.data.hkpg[Constants.T5][hkpg_rec] != Constants.DOUBLE_FILL){
            dpu_temp = 
               (CDF_Gen.data.hkpg[Constants.T5][hkpg_rec] * 
               CDF_Gen.data.hkpg_scale[Constants.T5]) + 
               CDF_Gen.data.hkpg_offset[Constants.T5];
         }else{
            dpu_temp = 20;
         }
         
         //get the adjusted bin edges
         //chan_edges[lc_rec] = 
         //   SpectrumExtract.createBinEdges(0, /*scint_temp, dpu_temp, */peak);

         //write the spectrum to the new array
         if(CDF_Gen.data.lc1[lc_rec + first] != Constants.FSPC_RAW_FILL){
            lc_scaled[0][lc_rec] = CDF_Gen.data.lc1[lc_rec + first] * 20;
         }else{
            lc_scaled[0][lc_rec] = Constants.DOUBLE_FILL;
         }
         if(CDF_Gen.data.lc2[lc_rec + first] != Constants.FSPC_RAW_FILL){
            lc_scaled[1][lc_rec] = CDF_Gen.data.lc2[lc_rec + first] * 20;
         }else{
            lc_scaled[1][lc_rec] = Constants.DOUBLE_FILL;
         }
         if(CDF_Gen.data.lc3[lc_rec + first] != Constants.FSPC_RAW_FILL){
            lc_scaled[2][lc_rec] = CDF_Gen.data.lc3[lc_rec + first] * 20;
         }else{
            lc_scaled[2][lc_rec] = Constants.DOUBLE_FILL;
         }
         if(CDF_Gen.data.lc4[lc_rec + first] != Constants.FSPC_RAW_FILL){
            lc_scaled[3][lc_rec] = CDF_Gen.data.lc4[lc_rec + first] * 20;
         }else{
            lc_scaled[3][lc_rec] = Constants.DOUBLE_FILL;
         }
      }

      for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
         frameGroup[rec_i] = CDF_Gen.data.frame_20Hz[data_i];
         epoch[rec_i] = CDF_Gen.data.epoch_20Hz[data_i] - Constants.SING_ACCUM;
         q[rec_i] = CDF_Gen.data.fspc_q[data_i];
      }

      var = cdf.getVariable("LC1");
      System.out.println("LC1...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
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
      for(int mspc_rec = 0, sspc_rec = 0; mspc_rec < numOfRecs; mspc_rec++){
        
         /*
         //get temperatures
         hkpg_rec = (mspc_rec + first) * 4 / 40; //convert from mod4 to mod40
         if(CDF_Gen.data.hkpg[Constants.T0][hkpg_rec] != Constants.HKPG_FILL){
            scint_temp = 
               (CDF_Gen.data.hkpg[Constants.T0][hkpg_rec] * 
               CDF_Gen.data.hkpg_scale[Constants.T0]) + 
               CDF_Gen.data.hkpg_offset[Constants.T0];
         }else{
            scint_temp = 20;
         }
         if(CDF_Gen.data.hkpg[Constants.T5][hkpg_rec] != Constants.HKPG_FILL){
            dpu_temp = 
               (CDF_Gen.data.hkpg[Constants.T5][hkpg_rec] * 
               CDF_Gen.data.hkpg_scale[Constants.T5]) + 
               CDF_Gen.data.hkpg_offset[Constants.T5];
         }else{
            dpu_temp = 20;
         }*/
         
         //incremint sspc_rec if needed
         if(
            (CDF_Gen.data.frame_mod4[mspc_rec] - 
            CDF_Gen.data.frame_mod4[mspc_rec] % 32) != 
            CDF_Gen.data.frame_mod32[sspc_rec]
         ){
            sspc_rec++;
         }

         //get the adjusted bin edges
         old_edges = SpectrumExtract.createBinEdges(
            1, /*scint_temp, dpu_temp, */ CDF_Gen.data.peak511_bin[sspc_rec]
         );

         //rebin the spectrum
         mspc_rebin[mspc_rec] = SpectrumExtract.rebin(
            CDF_Gen.data.mspc[mspc_rec + first], old_edges, std_edges 
         );

         //divide counts by bin width and adjust the time scale
         for(int bin_i = 0; bin_i < mspc_rebin[mspc_rec].length; bin_i++){
            if(mspc_rebin[mspc_rec][bin_i] != Constants.DOUBLE_FILL){
               mspc_rebin[mspc_rec][bin_i] /= 
                  MSPC.BIN_WIDTHS[bin_i];
               mspc_rebin[mspc_rec][bin_i] /= 4;
            }
         }
      }

      System.out.println("\nSaving MSPC...");

      for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
         frameGroup[rec_i] = CDF_Gen.data.frame_mod4[data_i];
         epoch[rec_i] = CDF_Gen.data.epoch_mod4[data_i] - Constants.QUAD_ACCUM;
         q[rec_i] = CDF_Gen.data.mspc_q[data_i];
      }

      String destName = 
         outputPath + "/" + date + "/" + "bar1" + flt + "_" + id + "_" + stn +
         "_l2_" + "mspc" + "_20" + date +  "_v" + revNum + ".cdf";

      MSPC mspc = new MSPC(destName, date, 2);
      System.out.println("mspc");
      mspc.writeData("MSPC", mspc_rebin);
      System.out.println("FrameGroup");
      mspc.writeData("FrameGroup", frameGroup);
      System.out.println("Epoch");
      mspc.writeData("Epoch", epoch);
      System.out.println("Q");
      mspc.writeData("Q", q);

      mspc.close();
   }

   public void doSspcCdf(int first, int last, int date) throws CDFException{
      //CDF cdf;
      Variable var;
      
      double scint_temp = 0, dpu_temp = 0;

      int numOfRecs = last - first;
      double[][] sspc_rebin = new double[numOfRecs][256];
      double[] 
         old_edges, 
         std_edges = SpectrumExtract.stdEdges(2, 2.4414);
      
      double[] peak = new double[numOfRecs];
      int[] 
         frameGroup = new int[numOfRecs],
         q = new int[numOfRecs];
      long[] epoch = new long[numOfRecs];

      System.out.println("\nSaving SSPC...");

      //rebin the sspc spectra
      for(int sspc_rec = 0, hkpg_rec = 0; sspc_rec < numOfRecs; sspc_rec++){
/*         //get temperatures
         hkpg_rec = (sspc_rec + first) * 32 / 40; //convert from mod32 to mod40
         if(CDF_Gen.data.hkpg[Constants.T0][hkpg_rec] != Constants.HKPG_FILL){
            scint_temp = 
               (CDF_Gen.data.hkpg[Constants.T0][hkpg_rec] * 
               CDF_Gen.data.hkpg_scale[Constants.T0]) + 
               CDF_Gen.data.hkpg_offset[Constants.T0];
         }else{
            scint_temp = 20;
         }
         if(CDF_Gen.data.hkpg[Constants.T5][hkpg_rec] != Constants.HKPG_FILL){
            dpu_temp = 
               (CDF_Gen.data.hkpg[Constants.T5][hkpg_rec] * 
               CDF_Gen.data.hkpg_scale[Constants.T5]) + 
               CDF_Gen.data.hkpg_offset[Constants.T5];
         }else{
            dpu_temp = 20;
         }
*/    
         //get the adjusted bin edges
         old_edges = 
            SpectrumExtract.createBinEdges(
               2, CDF_Gen.data.peak511_bin[sspc_rec]
            );
         
         //rebin the spectum
         sspc_rebin[sspc_rec] = SpectrumExtract.rebin(
            CDF_Gen.data.sspc[sspc_rec + first], old_edges, std_edges
         );

         //divide counts by bin width and convert the time scale to /sec
         for(int bin_i = 0; bin_i < sspc_rebin[sspc_rec].length; bin_i++){
            if(sspc_rebin[sspc_rec][bin_i] != Constants.DOUBLE_FILL){
               sspc_rebin[sspc_rec][bin_i] /= 
                  std_edges[bin_i + 1] - std_edges[bin_i];
               sspc_rebin[sspc_rec][bin_i] /= 32;
            }
         }
      }

      for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
         peak[rec_i] = CDF_Gen.data.peak511_bin[data_i];
         frameGroup[rec_i] = CDF_Gen.data.frame_mod32[data_i];
         epoch[rec_i] = CDF_Gen.data.epoch_mod32[data_i] - Constants.SSPC_ACCUM;
         q[rec_i] = CDF_Gen.data.sspc_q[data_i];
      }

      String destName = 
         outputPath + "/" + date + "/" + "bar1" + flt + "_" + id + "_" + stn +
         "_l2_" + "sspc" + "_20" + date +  "_v" + revNum + ".cdf";

      SSPC sspc = new SSPC(destName, date, 2);
      System.out.println("sspc");
      sspc.writeData("SSPC", sspc_rebin);
      System.out.println("Peak_511");
      sspc.writeData("Peak_511", peak);
      System.out.println("FrameGroup");
      sspc.writeData("FrameGroup", frameGroup);
      System.out.println("Epoch");
      sspc.writeData("Epoch", epoch);
      System.out.println("Q");
      sspc.writeData("Q", q);

      sspc.close();
/*
      String srcName = 
         "cdf_skels/l2/barCLL_PP_S_l2_sspc_YYYYMMDD_v++.cdf";
      copyFile(new File(srcName), new File(destName), false);
      cdf = openCDF(destName);

      var = cdf.getVariable("SSPC");
      System.out.println("Spectrum Arrays...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {256, 1}, 
         new long[] {1}, 
         sspc_rebin
      );

      var = cdf.getVariable("SSPC_ch");
      System.out.println("Spectrum Arrays...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {256, 1}, 
         new long[] {1}, 
         sspc_rebin
      );

      var = cdf.getVariable("Peak_511");
      System.out.println("Peak_511...");
      var.putHyperData(
         var.getNumWrittenRecords(), numOfRecs, 1, 
         new long[] {0}, 
         new long[] {256, 1}, 
         new long[] {1}, 
         peak
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
*/
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
            if(CDF_Gen.data.rcnt[var_i][rec_i + first] != Constants.RCNT_FILL){
               rc_timeScaled[var_i][rec_i] = 
                  CDF_Gen.data.rcnt[var_i][rec_i + first] / 4;
            }else{
               rc_timeScaled[var_i][rec_i] = Constants.DOUBLE_FILL;
            }
         }
      }

      for(int rec_i = 0, data_i = first; data_i < last; rec_i++, data_i++){
         frameGroup[rec_i] = CDF_Gen.data.frame_mod4[data_i];
         epoch[rec_i] = CDF_Gen.data.epoch_mod4[data_i] - Constants.QUAD_ACCUM;
         q[rec_i] = CDF_Gen.data.rcnt_q[data_i];
      }
         
      System.out.println("\nSaving RCNT...");

      String srcName = 
         "cdf_skels/l2/barCLL_PP_S_l2_rcnt_YYYYMMDD_v++.cdf";
      String destName = 
         outputPath + "/" + date + "/"  + "bar1" + flt + "_" + id + "_" + stn
         + "_l2_" + "rcnt" + "_20" + date +  "_v" + revNum + ".cdf";

      copyFile(new File(srcName), new File(destName), false);

      cdf = openCDF(destName);

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
 }
