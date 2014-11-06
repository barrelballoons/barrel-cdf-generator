/*
SpectrumExtract.java

Description:
   Creates energy bin edges and rebins spectra.

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.lang.ArrayIndexOutOfBoundsException;
import java.util.Map;
import java.util.HashMap;
import org.apache.commons.math3.fitting.GaussianFitter;
import org.apache.commons.math3.optim.nonlinear.vector.
          jacobian.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;

public class SpectrumExtract {

   private SpectrumExtract(){}

   //create uncalibrated bin edges
   private static final float[][] 
      OLD_RAW_EDGES = {
      {0f, 75f, 230f, 350f, 619f},
      {
         42f, 46f, 50f, 53f, 57f, 60f, 64f, 70f, 78f, 84f, 92f, 100f, 
         106f, 114f, 120f, 128f, 140f, 156f, 168f, 184f, 200f, 212f, 228f, 
         240f, 256f, 280f, 312f, 336f, 368f, 400f, 424f, 456f, 480f, 512f, 
         560f, 624f, 672f, 736f, 800f, 848f, 912f, 960f, 1024f, 1120f, 
         1248f, 1344f, 1472f, 1600f, 1696f
      },
      {
         0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 11f, 12f, 13f, 14f, 15f, 
         16f, 17f, 18f, 19f, 20f, 21f, 22f, 23f, 24f, 25f, 26f, 27f, 28f, 29f, 
         30f, 31f, 32f, 33f, 34f, 35f, 36f, 37f, 38f, 39f, 40f, 41f, 42f, 43f, 
         44f, 45f, 46f, 47f, 48f, 49f, 50f, 51f, 52f, 53f, 54f, 55f, 56f, 57f, 
         58f, 59f, 60f, 61f, 62f, 63f, 64f, 66f, 68f, 70f, 72f, 74f, 76f, 78f, 
         80f, 82f, 84f, 86f, 88f, 90f, 92f, 94f, 96f, 98f, 100f, 102f, 104f, 
         106f, 108f, 110f, 112f, 114f, 116f, 118f, 120f, 122f, 124f, 126f, 
         128f, 132f, 136f, 140f, 144f, 148f, 152f, 156f, 160f, 164f, 168f, 
         172f, 176f, 180f, 184f, 188f, 192f, 196f, 200f, 204f, 208f, 212f, 
         216f, 220f, 224f, 228f, 232f, 236f, 240f, 244f, 248f, 252f, 256f, 
         264f, 272f, 280f, 288f, 296f, 304f, 312f, 320f, 328f, 336f, 344f, 
         352f, 360f, 368f, 376f, 384f, 392f, 400f, 408f, 416f, 424f, 432f, 
         440f, 448f,  456f, 464f, 472f, 480f, 488f, 496f, 504f, 512f, 528f, 
         544f, 560f, 576f, 592f, 608f, 624f, 640f, 656f, 672f, 688f, 704f, 
         720f, 736f, 752f, 768f, 784f, 800f, 816f, 832f, 848f, 864f, 880f, 
         896f, 912f, 928f, 944f, 960f, 976f, 992f, 1008f, 1024f, 1056f, 
         1088f, 1120f, 1152f, 1184f, 1216f, 1248f, 1280f, 1312f, 1344f, 
         1376f, 1408f, 1440f, 1472f, 1504f, 1536f, 1568f, 1600f, 1632f, 
         1664f, 1696f, 1728f, 1760f, 1792f, 1824f, 1856f, 1888f, 1920f, 
         1952f, 1984f, 2016f, 2048f, 2112f, 2176f, 2240f, 2304f, 2368f, 
         2432f, 2496f, 2560f, 2624f, 2688f, 2752f, 2816f, 2880f, 2944f, 
         3008f, 3072f, 3136f, 3200f, 3264f, 3328f, 3392f, 3456f, 3520f, 
         3584f, 3648f, 3712f, 3776f, 3840f, 3904f, 3968f, 4032f, 4096f
      }
      },
      RAW_EDGES = {
      {0f, 20f, 40, 75f, 230f, 350f, 619f},
      {
         42f, 46f, 50f, 53f, 57f, 60f, 64f, 70f, 78f, 84f, 92f, 100f, 
         106f, 114f, 120f, 128f, 140f, 156f, 168f, 184f, 200f, 212f, 228f, 
         240f, 256f, 280f, 312f, 336f, 368f, 400f, 424f, 456f, 480f, 512f, 
         560f, 624f, 672f, 736f, 800f, 848f, 912f, 960f, 1024f, 1120f, 
         1248f, 1344f, 1472f, 1600f, 1696f
      },
      {
         0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 11f, 12f, 13f, 14f, 15f, 
         16f, 17f, 18f, 19f, 20f, 21f, 22f, 23f, 24f, 25f, 26f, 27f, 28f, 29f, 
         30f, 31f, 32f, 33f, 34f, 35f, 36f, 37f, 38f, 39f, 40f, 41f, 42f, 43f, 
         44f, 45f, 46f, 47f, 48f, 49f, 50f, 51f, 52f, 53f, 54f, 55f, 56f, 57f, 
         58f, 59f, 60f, 61f, 62f, 63f, 64f, 66f, 68f, 70f, 72f, 74f, 76f, 78f, 
         80f, 82f, 84f, 86f, 88f, 90f, 92f, 94f, 96f, 98f, 100f, 102f, 104f, 
         106f, 108f, 110f, 112f, 114f, 116f, 118f, 120f, 122f, 124f, 126f, 
         128f, 132f, 136f, 140f, 144f, 148f, 152f, 156f, 160f, 164f, 168f, 
         172f, 176f, 180f, 184f, 188f, 192f, 196f, 200f, 204f, 208f, 212f, 
         216f, 220f, 224f, 228f, 232f, 236f, 240f, 244f, 248f, 252f, 256f, 
         264f, 272f, 280f, 288f, 296f, 304f, 312f, 320f, 328f, 336f, 344f, 
         352f, 360f, 368f, 376f, 384f, 392f, 400f, 408f, 416f, 424f, 432f, 
         440f, 448f,  456f, 464f, 472f, 480f, 488f, 496f, 504f, 512f, 528f, 
         544f, 560f, 576f, 592f, 608f, 624f, 640f, 656f, 672f, 688f, 704f, 
         720f, 736f, 752f, 768f, 784f, 800f, 816f, 832f, 848f, 864f, 880f, 
         896f, 912f, 928f, 944f, 960f, 976f, 992f, 1008f, 1024f, 1056f, 
         1088f, 1120f, 1152f, 1184f, 1216f, 1248f, 1280f, 1312f, 1344f, 
         1376f, 1408f, 1440f, 1472f, 1504f, 1536f, 1568f, 1600f, 1632f, 
         1664f, 1696f, 1728f, 1760f, 1792f, 1824f, 1856f, 1888f, 1920f, 
         1952f, 1984f, 2016f, 2048f, 2112f, 2176f, 2240f, 2304f, 2368f, 
         2432f, 2496f, 2560f, 2624f, 2688f, 2752f, 2816f, 2880f, 2944f, 
         3008f, 3072f, 3136f, 3200f, 3264f, 3328f, 3392f, 3456f, 3520f, 
         3584f, 3648f, 3712f, 3776f, 3840f, 3904f, 3968f, 4032f, 4096f
      }
   };

   private static final double[] SSPC_MIDPOINTS = {
      0.5, 1.5, 2.5, 3.5, 4.5, 5.5, 6.5, 7.5, 8.5, 9.5, 10.5,  
      11.5, 12.5, 13.5, 14.5, 15.5, 16.5, 17.5, 18.5, 19.5, 20.5,  
      21.5, 22.5, 23.5, 24.5, 25.5, 26.5, 27.5, 28.5, 29.5, 30.5,  
      31.5, 32.5, 33.5, 34.5, 35.5, 36.5, 37.5, 38.5, 39.5, 40.5,  
      41.5, 42.5, 43.5, 44.5, 45.5, 46.5, 47.5, 48.5, 49.5, 50.5,  
      51.5, 52.5, 53.5, 54.5, 55.5, 56.5, 57.5, 58.5, 59.5, 60.5,  
      61.5, 62.5, 63.5, 65, 67, 69, 71, 73, 75, 77, 79, 81,  
      83, 85, 87, 89, 91, 93, 95, 97, 99, 101, 103, 105,  
      107, 109, 111, 113, 115, 117, 119, 121, 123, 125,  
      127, 130, 134, 138, 142, 146, 150, 154, 158, 162,  
      166, 170, 174, 178, 182, 186, 190, 194, 198, 202,  
      206, 210, 214, 218, 222, 226, 230, 234, 238, 242,  
      246, 250, 254, 260, 268, 276, 284, 292, 300, 308,  
      316, 324, 332, 340, 348, 356, 364, 372, 380, 388,  
      396, 404, 412, 420, 428, 436, 444, 452, 460, 468,  
      476, 484, 492, 500, 508, 520, 536, 552, 568, 584,  
      600, 616, 632, 648, 664, 680, 696, 712, 728, 744,  
      760, 776, 792, 808, 824, 840, 856, 872, 888, 904,  
      920, 936, 952, 968, 984, 1000, 1016, 1040, 1072, 1104,  
      1136, 1168, 1200, 1232, 1264, 1296, 1328, 1360, 1392,  
      1424, 1456, 1488, 1520, 1552, 1584, 1616, 1648, 1680,  
      1712, 1744, 1776, 1808, 1840, 1872, 1904, 1936, 1968,  
      2000, 2032, 2080, 2144, 2208, 2272, 2336, 2400, 2464,  
      2528, 2592, 2656, 2720, 2784, 2848, 2912, 2976, 3040,  
      3104, 3168, 3232, 3296, 3360, 3424, 3488, 3552, 3616,  
      3680, 3744, 3808, 3872, 3936, 4000, 4064
   };

   //nominal scaling factor for converting raw bins to energy levels
   private static final float
      SCALE_FACTOR = 2.4414f;

   //defines the search window for the 511 line
   private static final int
      PEAK_511_START = 90,
      PEAK_511_WIDTH = 50,
      PEAK_511_END = PEAK_511_START + PEAK_511_WIDTH;

   /*
      determine the max counts per spectrum to accept before assuming the 511 
      line is washed out
      511 line has no more than 2cnts/sec/kev
      In order to see if we are in a saturated area, we will limit the 
      counts for this spectrum to 2*32*[End Energy Level - Start Enrgy level]
   */
   public static final int
      MAX_CNT_FACTOR = (int)
         ((OLD_RAW_EDGES[2][PEAK_511_END] - OLD_RAW_EDGES[2][PEAK_511_START]) * 
         SCALE_FACTOR * 32 * 2);
   
   private Map<Integer, Integer[]> raw_spectra;
   private BarrelFrame[] frames;
   private int numFrames, numRecords;

   public ExtractSpectrum(BarrelFrame[] frames){
      this.frames     = frames;
      this.numFrames  = this.frames.length;
      this.numRecords = (int)Math.ceil((float)this.numFrames / 32);

      this.raw_spectra = getSpectraRecords();
   }

   private HashMap getSpectraRecords(){
      Map <Integer, Integer[]> spectra = new HashMap<Integer, Integer[]>();
      
      for (frame_i = 0, rec_i = 0; frame_i < this.numFrames; frame_i++) {
         fc = this.frames[frame_i].getFrameCounter();
         if(fc == null || fc == BarrelFrame.INT4_FILL){
            continue;
         }

         mod32 = this.frames[frame_i].mod32;
         fg = fc - mod32;

         //check if we are still in the same frame group 
         //(meaning the same spectrum)
         if (frameGroup[rec_i] != fg) {
            epoch[rec_i] = CDF_Gen.barrel_time.getEpoch(frameGroup[rec_i]);
            peak[rec_i] = this.frames[frame_i].getPeak511();
            frameGroup[rec_i] = fg;

            //get the most recent scintillator temperature value
            scint_temp = getTemp(frame_i, HKPG.T0);
            dpu_temp = getTemp(frame_i, HKPG.T5);

            //get the adjusted bin edges
            old_edges = 
               SpectrumExtract.makeedges(2, scint_temp, dpu_temp, peak[rec_i]);

            //rebin the spectrum
            rebin[rec_i] = 
               SpectrumExtract.rebin(raw_spec, old_edges, std_edges);

            //scale the counts and calculate error
            for(int bin_i = 0; bin_i < 256; bin_i++){
               if(rebin[rec_i][bin_i] != BarrelFrame.FLOAT_FILL){
                  width = std_edges[bin_i + 1] - std_edges[bin_i];

                  //divide counts by bin width and adjust the time scale
                  rebin[rec_i][bin_i] /= (width * 32f);
                  //get the count error
                  error[rec_i][bin_i] = 
                     (float)Math.sqrt(rebin[rec_i][bin_i]) / (width * 32f);
               }
            }

            //clear the raw spectrum
            Arrays.fill(raw_spec, BarrelFrame.FLOAT_FILL);

            //update the record number and frameGroup
            rec_i++;            
         }

      return spectra;
   }

   public static void do511Fits(max_recs){

      int length = stop - start;

      int max_cnts = MAX_CNT_FACTOR * length; 

      if(length < 2){return;}
      
      DescriptiveStatistics stats = new DescriptiveStatistics();
      float peak;
      double max_bin, min_bin;
      double[]
         search_spec = new double[PEAK_511_WIDTH],
         bin_num = new double[PEAK_511_WIDTH];
      
      //create array of detector bin numbers we will be searching
      for(int bin_i = 0; bin_i < PEAK_511_WIDTH; bin_i++){
         bin_num[bin_i] = SSPC_MIDPOINTS[bin_i + PEAK_511_START]; 
      }

      //sum up all of the spectra from this group
      for(int spec_i = start; spec_i < stop; spec_i++){

         //only add the spectrum to the sum if it is complete
         if((data.sspc_q[spec_i] & Constants.PART_SPEC) == 0){
            for(int chan_i = 0; chan_i < PEAK_511_WIDTH; chan_i++){
               search_spec[chan_i] += 
                  data.sspc[spec_i][chan_i + PEAK_511_START];
               //check to see if it is likely the 511 line will be washed out
               if(search_spec[chan_i] > max_cnts){
                  for(int peak_i = start; peak_i < stop; peak_i++){
                     data.peak511_bin[peak_i] = Constants.FLOAT_FILL; 
                  }
                  System.out.println(
                     "too many: " + 
                     length + " " + max_cnts + " " + search_spec[chan_i]
                  );
                  return;
               }
            }
         }
      }

      peak = find511(bin_num, search_spec);

      for(int peak_i = start; peak_i < stop; peak_i++){
         data.peak511_bin[peak_i] = peak; 
      }
   }

   private static float find511(double[] x, double[] y){
      GaussianFitter fitter = 
         new GaussianFitter(new LevenbergMarquardtOptimizer());
      double[] 
         fit_params = {10, Constants.DOUBLE_FILL, 1},
         curve = new double[PEAK_511_WIDTH - 4];
      int[]
         high_area = new int[PEAK_511_WIDTH];
      double
         m, b, 
         slope = 0,
         this_low= 0,
         last_low = 0;
      int
         apex = 0,
         high_cnt = 0;
      
      // guess at a linear background
      m = (y[PEAK_511_WIDTH - 1] - y[0]) / (x[PEAK_511_WIDTH - 1] - x[0]);
      b = y[0] - m * x[0];

      //convert y to cnts/bin_width
      for(int bin_i = 0; bin_i < x.length; bin_i++){
         y[bin_i] /= (
            RAW_EDGES[2][bin_i + PEAK_511_START + 1] - 
            RAW_EDGES[2][bin_i + PEAK_511_START]
         );
      }

      //take the second derivitave to find peak
      for(int bin_i = 2; bin_i < x.length - 2; bin_i++){
         curve[bin_i - 2] = y[bin_i + 2] - (2 * y[bin_i]) + y[bin_i - 2];
      }
      
      //find low point of second derivitave using moving average
      this_low  = (curve[0] + curve[1] + curve[2]);
      last_low = this_low;
      for(int bin_i = 2; bin_i < curve.length - 1; bin_i++){
         this_low += (curve[bin_i + 1] - curve[bin_i - 2]);
         if(this_low < last_low){
            apex = bin_i + 2;
            last_low = this_low;
         }
      }

      //do the curve fit
      try{
         fit_params[1] = x[apex]; //guess for peak location
         for(int bin_i = apex - 3; bin_i < apex + 3; bin_i++){
            fitter.addObservedPoint(x[bin_i],  y[bin_i]);
         }
         fit_params = fitter.fit(fit_params);
      }
      catch(ArrayIndexOutOfBoundsException ex){
         System.out.println(
            "Payload ID: " + CDF_Gen.getSetting("currentPayload") + 
            " Date: " + CDF_Gen.getSetting("date"));
         System.out.println("Gaussian out of bounds: " + apex);
         fit_params[1] = Constants.DOUBLE_FILL;
      }
      return (float)fit_params[1];
   }

   public static float[] stdEdges(int spec_i, float scale){
      float[] edges = (
         CDF_Gen.data.getVersion() > 3 ? 
         RAW_EDGES[spec_i] : OLD_RAW_EDGES[spec_i]
      );
      float[] result = new float[edges.length];

      for(int edge_i = 0; edge_i < edges.length; edge_i++){
         result[edge_i] = scale * edges[edge_i];
      }

      return result;
   }

/*
   NAME: binvert

   DESC: invert bin(energy) relation to find energy(bin)

   INPUT_ARGS: 
          start is an initial estimate of bin value(s)
          f is a temperature/detector dependent constant

   OUTPUT: returns an object of the same type and dimension
           as start object contains energy value(s)

   METHOD: two iterations of Newton-Raphson to solve a
          transcendental equation. A tricky part is the
          argument for Math.log() can be negative, due to
          electronics offsets (say bin 5 is 0 keV). Since only
          makeedges() calls, we assume some properties
          of argument start. If start is a scalar, then
          we're working on the 511keV line, so we won't have
          start <= 0. For slo, we can have several early
          start values negative. Force these to be NaN, and
          proceed with calculations, then force these to
          ascending negative values on return.

          An accurate approach is to use complex numbers and
          discard the imaginary part of the result. This gives
          correct negative energy results for bin edges, but
          doubles computational effort. It's not worth it.

   NOTES: binvert() should be used only by makedges().
          Ported from Michael McCarthy's original IDL code
*/
   private static float[] binvert(float[] start, float f){
      int 
         size = start.length,
         bad_vals = 0;

      float[] iter1 = new float[size];
      float[] iter2 = new float[size];
     

      //first iteration of Newton-Raphson  
      for(int i = 0; i < size; i++){
         if(start[i] < 0){
            iter1[i] = Float.NaN;
         }else{
            iter1[i] = 
               (start[i] + f * start[i]) / 
               (1.0f + f * (1.0f + (float)Math.log(start[i])));
            if(iter1[i] < 0){iter1[i] = Float.NaN;}
         }
      }

      //second iteration of Newton-Raphson  
      for(int i = 0; i < size; i++){
         if(Float.isNaN(iter1[i])){
            bad_vals++;
         }else{
            iter2[i] = 
               (start[i] + f * iter1[i]) / 
               (1.0f + f * (1.0f + (float)Math.log(iter1[i])));
            if(Float.isInfinite(iter2[i]) || iter2[i] < 0){
               bad_vals++;
               iter2[i] = Float.NaN;
            }
         }
      }
      
      //turn bad values into negatives ascending to zero
      if(bad_vals > 0){
         for(int i = 0; i < size; i++){
            if(Float.isNaN(iter2[i])){
               bad_vals--;
               iter2[i] = 0 - bad_vals;
            }
         }
      }

      return iter2;
   }
   //overload binvert to accept scalar
   private static float binvert(float start, float f){
      return (binvert(new float[] {start}, f))[0];
   }
   
/*
   NAME: makeedges()
   DESC: construct energy edges for spectrum products

   INPUT_ARGS: 
        spec_i        indicates spectrum for which the edges are to be created
                      (0=slo, 1=med, 2=fst)
        payload      2 char identifier (e.g., '1S')
        dpu_temp      dpu temperature in C
        xtal_temp     scintillator temperature in C
        peak511       sspc location for 511keV; 200 nominal; 0<peak511<4095 

   INPUT_FILES: energy.cal

   OUTPUT: An array containing 1 of the 3 lists of bin edges
        slo is a float[257]
        med is a float[49]
        fst is a float[5]
        For these 3 lists, each pair of edges brackets one of the
          256/48/4 spectrum bins. Energy units are keV.
        Energy values below 0 are forced to 0, so it is
        possible for multiple low-end slo bins to have 0
        energy. Counts in these bins should be ignored.

   METHOD: invert an empirical model of bin(Energy). The model
        is bin(E) = k*(offset + gain*E + nonlin*E*Log(E))
        where offset, gain, and nonlin are linear functions
        of DPU temperature, and k is a quadratic function
        of crystal temperature. The dpu
        temperature-dependent linear functions use coefficients
        extracted from thermal chamber data, and are
        retrieved via a function call. Because the coupling
        between scintillator and crystal can change with air
        pressure, the peak511 parameter should be used to
        improve the model for flight data.

        default values for missing options are probably not
        sufficiently accurate


   CALLS: binvert(start,f) inverts bin(energy) function;
                needs a start value and factor f

   NOTES: Ported from Michael McCarthy's original IDL code
*/
   public static float[] makeedges(int spec_i, float peak511){
      return makeedges(spec_i, 0f, 0f, peak511);
   }
   public static float[] makeedges(
      int spec_i, float xtal_temp, float dpu_temp, float peak511
   ){
      String payload = 
         CDF_Gen.getSetting("currentPayload").substring(0,2);

      float[] edges_in = (
         CDF_Gen.data.getVersion() > 3 ? 
         RAW_EDGES[spec_i] : OLD_RAW_EDGES[spec_i]
      );

      //initialize array for calibrated edges
      float[] edges_out = new float[edges_in.length];

      //get dpu coefficients from calibration file
      float[][] dpu_coeffs = {{-5f, -0.1f}, {-0.5f, -0.001f}, {-0.1f, 0.0001f}};
      boolean payload_found = false;
      File energy_cal = new File("energy.cal");
      if(!energy_cal.exists()){}
      try{
         FileReader fr = new FileReader(energy_cal);
         BufferedReader br = new BufferedReader(fr);
         
         String line;
         String[] line_parts;

         while((line = br.readLine()) != null){
            //split off comments
            line_parts = line.split(";");

            //split the payload ID from the rest
            line_parts = line_parts[0].split(":");
            line_parts[0] = line_parts[0].trim();
            if(!payload.equals(line_parts[0])){continue;}

            //we have the right payload, check for the right number of values
            payload_found = true;
            line_parts[1] = line_parts[1].trim();
            line_parts = line_parts[1].split(",");
            if(line_parts.length != 6){continue;}

            //the correct number of values were in the file
            //overwrite defaults
            dpu_coeffs = new float[][] {
               {Float.valueOf(line_parts[0]), Float.valueOf(line_parts[1])},
               {Float.valueOf(line_parts[2]), Float.valueOf(line_parts[3])},
               {Float.valueOf(line_parts[4]), Float.valueOf(line_parts[5])}
            };
            br.close();
            break;
         }

         br.close();
      }catch(IOException ex){
         System.out.println("Can not find energy calibration file.");
         System.out.println("Using default values.");
      }
      
      //just return standard edges if there are no dpu coefficients
      if(!payload_found){
         return stdEdges(spec_i, SCALE_FACTOR);
      }

      //set model parameters 
      float xtal_compensate = 
         1.022f - 1.0574e-4f * (float)Math.pow(xtal_temp - 10.7f, 2);
      float[] dpu_compensate = new float[]{
         ((dpu_coeffs[0][0]) + (dpu_temp * dpu_coeffs[0][1])),
         ((dpu_coeffs[1][0]) + (dpu_temp * dpu_coeffs[1][1])),
         ((dpu_coeffs[2][0]) + (dpu_temp * dpu_coeffs[2][1]))
      };
      float factor = dpu_compensate[2] / dpu_compensate[1];

      //calculate a correction from 511keV location
      float fac511 = 1.0f;
      if(peak511 != (Float)CDFVar.getIstpVal("FLOAT_FILL")){
         float start = 
            (peak511 / xtal_compensate - dpu_compensate[0]) / dpu_compensate[1];
         fac511 = 511.0f / binvert(start,factor);
      }

      //calculate energies for the desired spectral product
      float[] start = new float[edges_in.length];
      for(int i = 0; i < start.length; i++){
         start[i] = 
            (edges_in[i] / xtal_compensate - dpu_compensate[0]) / 
            dpu_compensate[1];
      }

      edges_out = binvert(start, factor);
      for(int i = 0; i < edges_out.length; i++){
         edges_out[i] *= fac511;
      }

      return edges_out;
   }


   public static float[] createBinEdges(int spec_i, double peak511){
      double factor1, factor2, scale;
      float[] edges_in = (
         CDF_Gen.data.getVersion() > 3 ? 
         RAW_EDGES[spec_i] : OLD_RAW_EDGES[spec_i]
      );
      double[] edges_nonlin = new double[edges_in.length];
      float[] edges_cal = new float[edges_in.length];

      //a rational function approximates energy non-linearity
      for(int edge_i = 0; edge_i < edges_nonlin.length; edge_i++){
         edges_nonlin[edge_i] = 
            edges_in[edge_i] * (
               1 - 11.6 / (edges_in[edge_i] + 10.8) + 
               0.000091 * edges_in[edge_i]
            );
      }
     
      /*
      //quadratic function for crystal gain drift with temperature
      factor1 = 1;
      if(xtal_temp != 0){
         factor1 += (0.00014 * xtal_temp - 0.0068) * xtal_temp;
      }

      //linear function for peak detect gain drift with temperature
      factor2 = 1;
      if(dpu_temp != 0){
         factor2 += (23 - dpu_temp) * 0.00034;
      }*/

      //set an overall scale factor to position 511keV line
      //this also helps compensate incorrect temperature values

      scale = SCALE_FACTOR; //nominal keV/bin
      if(peak511 != Constants.DOUBLE_FILL){
         scale = 
            511.  /* * factor2 / factor1*/ / peak511 / 
            (1.0 - 11.6 / (peak511 + 10.8) + 0.000091 * peak511);
      }

      //apply corrections to energy bin edges
      //scale *= factor1 / factor2;
      for(int edge_i = 0; edge_i < edges_cal.length; edge_i++){
         edges_cal[edge_i] = (float)(scale * (edges_nonlin[edge_i]));
      }

      return edges_cal;
   }
   

   public static float[] rebin(
      int[] specin, float[] edges_in, float[] edges_out
   ){
      int a_cnt, b_cnt, c_cnt, d_cnt;

      //assumes the input and outpus edges are the same length array
      int 
         numOfEdges = edges_in.length,
         numOfBins = specin.length;

      int[] 
         a = new int[numOfEdges],
         b = new int[numOfEdges],
         c = new int[numOfEdges],
         d = new int[numOfEdges];

      float[] 
         ea1 = Arrays.copyOfRange(edges_in, 0, (numOfEdges - 1)),
         ea2 = Arrays.copyOfRange(edges_in, 1, numOfEdges),
         eb1 = Arrays.copyOfRange(edges_out, 0, (numOfEdges - 1)),
         eb2 = Arrays.copyOfRange(edges_out, 1, numOfEdges),
         widths_in = new float[numOfBins],
         widths_out = new float[numOfBins],
         specout = new float[numOfBins];

      //calculate the widths of each bin
      for(int i = 0; i < numOfBins; i++){
         widths_in[i] = ea2[i] - ea1[i];
         widths_out[i] = eb2[i] - eb1[i];
      }

      //This loops over each bin of the OUTPUT spectrum and sees which bins of
      //the the INPUT spectrum overlap it:
      spec_loop:
      for(int i = 0; i < numOfBins; i++){
         //reset the counts for each type of overlap
         a_cnt = 0;
         b_cnt = 0;
         c_cnt = 0;
         d_cnt = 0;

         //loop through each of the new edges looking for overlaps
         for(int j = 0; j < numOfBins; j++){

            //There are four kinds of overlap: 
            //new channel completely covers old,
            //old completely covers new, 
            //and offsets to both sides. 
         
            //Old bins completely contain new bins [exclude specific case
            //where they are identical, but keep cases where one side matches]:
            if(
               ((eb1[i] >= ea1[j]) && (eb2[i] <= ea2[j])) &&
               !((eb1[i] == ea1[j]) && (eb2[i] == ea2[j]))
            ){
               a[a_cnt] = j;
               a_cnt++;
            }

            //New bins completely contain old bins
            if((eb1[i] <= ea1[j]) && (eb2[i] >= ea2[j])){
               b[b_cnt] = j;
               b_cnt++;
            }

            //new bin overlaps lower edge
            if((eb2[i] < ea2[j]) && (eb2[i] > ea1[j]) && (eb1[i] < ea1[j])){
               c[c_cnt] = j;
               c_cnt++;
            }

            // new bin overlaps upper edge
            if((eb1[i] > ea1[j]) && (eb1[i] < ea2[j]) && (eb2[i] > ea2[j])){
               d[d_cnt] = j;
               d_cnt++;
            }
         }

         //Transfer counts from input spectrum into 
         //this bin of the output spectrum
         if (a_cnt > 0){ 
            for(int k = 0; k < a_cnt; k++){
               if(specin[a[k]] < 0){
                  specout[i] = Constants.FLOAT_FILL;
                  continue spec_loop;
               }else{
                  specout[i] += 
                     widths_out[i] / widths_in[a[k]] * specin[a[k]];
               }
            }
         }
         if (b_cnt > 0){
            for(int k = 0; k < b_cnt; k++){
               if(specin[b[k]] < 0){
                  specout[i] = Constants.FLOAT_FILL;
                  continue spec_loop;
               }else{
                  specout[i] += specin[b[k]];
               }
            }
         }
         if (c_cnt > 0){
            for(int k = 0; k < c_cnt; k++){
               if(specin[c[k]] < 0){
                  specout[i] = Constants.FLOAT_FILL;
                  continue spec_loop;
               }else{
                  specout[i] += 
                     (eb2[i] - ea1[c[k]]) / widths_in[c[k]] * specin[c[k]];
               }
            }
         }
         if (d_cnt > 0){
            for(int k = 0; k < d_cnt; k++){
               if(specin[d[k]] < 0){
                  specout[i] = Constants.FLOAT_FILL;
                  continue spec_loop;
               }else{
                  specout[i] += 
                     (ea2[d[k]] - eb1[i]) / widths_in[d[k]] * specin[d[k]];
               }
            }
         }
         
      }
      
      return specout;
   }


}
