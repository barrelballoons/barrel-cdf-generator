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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;
import java.lang.ArrayIndexOutOfBoundsException;
import org.apache.commons.math3.fitting.GaussianFitter;
import org.apache.commons.math3.optim.nonlinear.vector.
          jacobian.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;

public class SpectrumExtract {

   private SpectrumExtract(){}

   //create uncalibrated bin edges
   private static final double[][] RAW_EDGES = {
      {0, 75, 230, 350, 620},
      {
         42, 46, 50, 53, 57, 60, 64, 70, 78, 84, 92, 100, 
         106 , 114, 120, 128, 140, 156, 168, 184, 200, 212, 228, 
         240, 256, 280, 312, 336, 368, 400, 424, 456, 480, 512, 
         560, 624, 672, 736, 800, 848, 912, 960, 1024, 1120, 
         1248, 1344, 1472, 1600, 1696
      },
      {
         0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 
         16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 
         30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 
         44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 
         58, 59, 60, 61, 62, 63, 64, 66, 68, 70, 72, 74, 76, 78, 
         80, 82, 84, 86, 88, 90, 92, 94, 96, 98, 100, 102, 104, 106, 
         108, 110, 112, 114, 116, 118, 120, 122, 124, 126, 128, 132, 
         136, 140, 144, 148, 152, 156, 160, 164, 168, 172, 176, 
         180, 184, 188, 192, 196, 200, 204, 208, 212, 216, 220, 
         224, 228, 232, 236, 240, 244, 248, 252, 256, 264, 272, 
         280, 288, 296, 304, 312, 320, 328, 336, 344, 352, 360, 
         368, 376, 384, 392, 400, 408, 416, 424, 432, 440, 448, 
         456, 464, 472, 480, 488, 496, 504, 512, 528, 544, 560, 
         576, 592, 608, 624, 640, 656, 672, 688, 704, 720, 736, 
         752, 768, 784, 800, 816, 832, 848, 864, 880, 896, 912, 
         928, 944, 960, 976, 992, 1008, 1024, 1056, 1088, 1120, 
         1152, 1184, 1216, 1248, 1280, 1312, 1344, 1376, 1408, 
         1440, 1472, 1504, 1536, 1568, 1600, 1632, 1664, 1696, 
         1728, 1760, 1792, 1824, 1856, 1888, 1920, 1952, 1984, 
         2016, 2048, 2112, 2176, 2240, 2304, 2368, 2432, 2496, 
         2560, 2624, 2688, 2752, 2816, 2880, 2944, 3008, 3072, 
         3136, 3200, 3264, 3328, 3392, 3456, 3520, 3584, 3648, 
         3712, 3776, 3840, 3904, 3968, 4032, 4096
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

   //defines the search window for the 511 line
   private static final int
      PEAK_511_START = 90,
      PEAK_511_WIDTH = 50;

   public static void do511Fits(int start, int stop){ 
      DataHolder data = CDF_Gen.data;

      int length = stop - start;
      int max_cnts = 250 * length;

      if(length < 2){return;}
      
      DescriptiveStatistics stats = new DescriptiveStatistics();
      double max_bin, min_bin, peak;
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
                     data.peak511_bin[peak_i] = Constants.DOUBLE_FILL; 
                  }
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

   private static double find511(double[] x, double[] y){
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
      return fit_params[1];
   }

   public static double[] stdEdges(int spec_i, double scale){
      int length = RAW_EDGES[spec_i].length;
      double[] result = new double[length];

      for(int edge_i = 0; edge_i < length; edge_i++){
         result[edge_i] = scale * RAW_EDGES[spec_i][edge_i];
      }

      return result;
   }

/*
   NAME: binvert

   DESC: invert bin(energy) relation to find energy(bin)

   INPUT: start is an initial estimate of bin value(s)
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
   private double[] binvert(double[] start, double f){
      int 
         size = start.length,
         bad_vals = 0;

      double[] iter1 = new double[size];
      double[] iter2 = new double[size];
     

      //first iteration of Newton-Raphson  
      for(int i = 0; i < size; i++){
         if(start[i] < 0){
            iter1[i] = Double.NaN;
         }else{
            iter1[i] = 
               (start[i] + f * start[i]) / 
               (1 + f * (1 + Math.log(start[i])));
         }
      }

      //second iteration of Newton-Raphson  
      for(int i = 0; i < size; i++){
         if(Double.isNaN(iter1[i])){
            bad_vals++;
         }else{
            iter2[i] = 
               (iter1[i] + f * iter1[i]) / 
               (1.0 + f * (1.0 + Math.log(iter1[i])));
            if(Double.isInfinite(iter2[i])){
               bad_vals++;
            }
         }
      }
      
      //turn bad values into negatives ascending to zero
      if(bad_vals > 0){
         for(int i = 0; i < size; i++){
            if(Double.isNaN(iter2[i]) || Double.isInfinite(iter2[i])){
               bad_vals--;
               iter2[i] = 0 - bad_vals;
            }
         }
      }

      return iter2;
   }
   

   public double[] makeedges(){
      return new double[] {};
   }


   public static double[] createBinEdges(
      int spec_i, double peak511// double xtal_temp, double dpu_temp, double peak511
   ){
      double factor1, factor2, scale;
      double[] edges_nonlin = new double[RAW_EDGES[spec_i].length];
      double[] edges_cal = new double[RAW_EDGES[spec_i].length];

      //a rational function approximates energy non-linearity
      for(int edge_i = 0; edge_i < edges_nonlin.length; edge_i++){
         edges_nonlin[edge_i] = 
            RAW_EDGES[spec_i][edge_i] * (
               1 - 11.6 / (RAW_EDGES[spec_i][edge_i] + 10.8) + 
               0.000091 * RAW_EDGES[spec_i][edge_i]
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

      scale = 2.4414; //nominal keV/bin
      if(peak511 != Constants.DOUBLE_FILL){
         scale = 
            511.  /* * factor2 / factor1*/ / peak511 / 
            (1.0 - 11.6 / (peak511 + 10.8) + 0.000091 * peak511);
      }

      //apply corrections to energy bin edges
      //scale *= factor1 / factor2;
      for(int edge_i = 0; edge_i < edges_cal.length; edge_i++){
         edges_cal[edge_i] = scale * (edges_nonlin[edge_i]);
      }

      return edges_cal;
   }
   

   public static double[] rebin(
      int[] specin, double[] edges_in, double[] edges_out
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

      double[] 
         ea1 = Arrays.copyOfRange(edges_in, 0, (numOfEdges - 1)),
         ea2 = Arrays.copyOfRange(edges_in, 1, numOfEdges),
         eb1 = Arrays.copyOfRange(edges_out, 0, (numOfEdges - 1)),
         eb2 = Arrays.copyOfRange(edges_out, 1, numOfEdges),
         widths_in = new double[numOfBins],
         widths_out = new double[numOfBins],
         specout = new double[numOfBins];

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
                  specout[i] = Constants.DOUBLE_FILL;
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
                  specout[i] = Constants.DOUBLE_FILL;
                  continue spec_loop;
               }else{
                  specout[i] += specin[b[k]];
               }
            }
         }
         if (c_cnt > 0){
            for(int k = 0; k < c_cnt; k++){
               if(specin[c[k]] < 0){
                  specout[i] = Constants.DOUBLE_FILL;
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
                  specout[i] = Constants.DOUBLE_FILL;
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
