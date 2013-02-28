package edu.ucsc.barrel.cdf_gen;

/*
SpectrumExtract.java v13.02.28

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

Change Log:
   v13.02.28
      -Updated bin edge creater, removed rebin routine for now. Added imports 
         for Gaussian fit. 
   v12.11.20
      -Added this documentation
      
*/

import org.apache.commons.math3.fitting.GaussianFitter;
import org.apache.commons.math3.optim.nonlinear.vector.
          jacobian.LevenbergMarquardtOptimizer;

public class SpectrumExtract {
   
   public static double[][] createBinEdges(
      double xtal_temp, double dpu_temp, int bin_511
   ){
      //edge array index reference
      final int FAST = 0;
      final int MED = 1;
      final int SLOW = 2;
      
      //nominal keV/bin
      final double SCALE = 2.5; 
      
      //create uncalibrated bin edges
      double[][] edges = {
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
      
      //calculate scintillator correction factor
      double xtal_factor  = 1 + (0.00014 * xtal_temp - 0.0068) * xtal_temp;
      
      //calulate dpu correction factor
      double dpu_factor = 1 + (23 - dpu_temp) * 0.00034;
      
      //calculate the 511 scale factor
      double scale =
         511 * dpu_factor / xtal_factor / bin_511 /
         (1.0 - 11.6 / (bin_511 + 10.8) + 9.1e-5 * bin_511);
      
      return edges;
   }
   
   public static void rebin(){

   }

   private static void find511(){
      GaussianFitter fitter = 
         new GaussianFitter(new LevenbergMarquardtOptimizer());
   }
}
