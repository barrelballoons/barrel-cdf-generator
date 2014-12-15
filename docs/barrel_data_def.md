BARREL Data Products and Information
====================================
Warren Rexroad - 08/2014

##Table of Contents
[TOC]

---------------------------------------

1. Introduction and Purpose
============================

The purpose of this document is to give those wanting to use BARREL data a quick guide accessing and understanding the different data types. The discussions will cover what data are stored in the BARREL repositories and any processing that has been performed on them.

2. Data Repositories
====================

There are two main repositories of BARREL data: [CDAWeb](http://cdaweb.gsfc.nasa.gov/cdaweb/sp_phys/) and [BARRELData](http://barreldata.ucsc.edu/data_products). CDAWeb archives stable releases of Level Two data only. BARRELData will contain the most recent versions of all data levels.

###Why use data from CDAWeb?
The data stored at CDAWeb is mostly complete and in a stable state. It is most likely an older version of the data, but it will not change out (or disappear) from underneath you.

Another benefit of CDAWeb is you can easily define a date/time range with whichever data products you want and their tools will do the rest. This makes it easy to get one big CDF file with only the data that is important to your work rather than manually downloading a series of smaller CDF files from BARRELData.

###Why use data from BARRELData?
BARRELData is the place where all the testing happens. The most current run of each data set is hosted here for users to test. It should be noted that while each run of our data is mean to be an improvement on the last, it is certainly possible that the most recent data processor changes broke something and the data have suddenly become entirely wrong or have gone missing. 

*Currently there only repository holding data from the 2013-2014 campaign is BARRELData*

3. Filenames, Version, and Levels
=================================

###3.1. Data Product Levels
#### 3.1.1 Level Zero
Level Zero products are daily files containing a all raw telemetry streams. A single file will contain all data from each satellite modem call that *start* in a 24hr period. That is, it includes any call originated between 00:00 and 23:59UTC. It is important to understand that the calls are not ended at midnight. A call that is started on January, 1 at 23:00 and last for three hours will be contained (in its entirety) in the January, 1 file. 

Level Zero products are mostly useful as an archival format and as a "pure" source of data for checking the validity of the higher level products.

The format of the raw data is detailed in a document called "BARREL Telemetry Interface Control Document" located at [http://www.dartmouth.edu/~barrel/documents.html](http://www.dartmouth.edu/~barrel/documents.html).

#### 3.1.2 Level One
Level One data are stored in CDF files which are grouped by date. Unlike Level Zero files, the dates here are midnight-to-midnight. 

The data contained in these files are essentially the raw values extracted from Level Zero and placed into CDF files. The one bit of processing that was done for these files is time stamp correction. The GPS time that is collected with the BARREL ephemeris data is sent once every four seconds and can be up to one second off. An attempt is made to give each data point a time stamp that is accurate to 1ms. 

Level One CDF files are good choice if you plan to process all of the data manually. 

#### 3.1.3 Level Two
Like Level One data, the Level Two products are stored in a series of CDF files and grouped by date. In addition to time correction there is a some processing that calibrates the energy spectra and converts raw data values to engineering units. The following list shows how each type of data are processed:

 + Slow and medium spectra are converted from counts/accumulation to counts/keV/second. The spectra are calibrated based on temperature and 511 line location, then rebinned to a standard set of energy levels.
 + GPS latitude and longitude are converted into degrees.
 + GPS altitude is converted to from mm to km.
 + Magnetometer data are nominally converted to &mu;T(?). The three converted axes are used to calculate a field magnitude.
 + Voltage, temperature, and current are converted to volts, &deg;C, and mA.
 + Rate counters are converted from counts/4seconds to counts/second.

### 3.2 Versions
Version numbers are incremented whenever the dataset officially changes changes. The change can either be to the format or content of the data. After a new version of data is released, it is archived with CDAWeb. 

The first version of BARREL data is v01. The BARRELData repository also holds a test version labeled v00. This version usually contains the most recent additions, but has the stability issues mentioned in section 2.

### 3.3 Filenames
There are three filename formats that you may encounter on the BARRELData server: raw telemetry files, daily telemetry files (Level Zero), and CDF files (Level One and Two). 

Raw telemetry files: 
bar_YYYYMMDD_HHMMSS_PP_G.pkt

Daily telemetry files:
bar_PP_S_LL_YYYYMMDD_VVV.tlm

CDF Files:
bar_PP_S_LL_TTTT_YYYYMMDD_VVV.cdf

*YYYY* 4-digit year
*MM* Month (01-12)
*DD* Day of Month (01-31)
*HH* Hour (00-23)
*MM* Minute (00-59)
*SS* Second (00-59)
*S* Launch station

 + 1 = SANAE
 + 2 = Halley
 + 0 = not part of a campaign (integration, etc.)

*G* Ground station code

 + U = UCSC
 + D = Dartmouth
 + B = UCB
 + S = SANAE
 + H = Halley
 + Z = other

*LL* Data level, l0 - l2
*PP* Payload sequential identifier, 1A-2Z
*VVV* File revision number, v00-v99 (the v in front is from PRBEM)
*TTTT* 4-character code for data type (magn, sspc, fspc, hkpg, etc.)

4. CDF File Contents
====================

Each day of data is represented by eight CDF files for both Level One and Level Two. Each of the eight files stores data that arrives at a single cadence and is logically similar. The 'TTTT' filename code (Section 3.3) identifies which of the eight types a file is.

### 4.1 FSPC - Fast Spectra
Fast spectra files contain 20Hz scintillator count data. During the 2012-2013 campaign, counts were collected in four channels. During the 2013-2014 six channels were used (the first channel was broken up). Their nominal energy range can be seen in Table 4.1.1 These energy ranges are not rebinned in either Level One or Level Two data, rather their calibrated range in tracked in a separate variable. 

The main difference between Level One and Level Two FSPC files are the inclusion of the calibrated energy level variable (FSPC\_EDGES) and the inclusion of error variables (cnt\_error1[a/b/c]/2/3/4). Errors are calculated as the square root of the counts. The contents of the Level One and Level Two FSPC files can be seen in Table 4.1.2 and Table 4.1.3, respectively.

Note: FSPC channels may also be referred to as *light curves*. In the CDF files they are listed as FSPC1-FSPC4, but it is equally valid to talk about LC1-LC4.

*Table 4.1.1* - FSPC Nominal Energy Ranges

| 2012-2013 | 2013-2014| Energy Range (keV) |  
| ----------| ---------| -------------------|  
|           |    FSPC1 |        0.0 - 177.6 |
|    FSPC1a |          |         0.0 - 45.6 |
|    FSPC1b |          |        48.0 - 93.6 |
|    FSPC1c |          |       96.0 - 177.6 |
|    FSPC2  |    FSPC2 |      180.0 - 549.6 |
|    FSPC3  |    FSPC3 |      552.0 - 837.6 |
|    FSPC4  |    FSPC4 |     840.0 - 1485.6 |

*Table 4.1.2* - Level One FSPC Contents

|      Variable | Datatype |     Units |  
| ------------- | -------- | --------- |  
|    FrameGroup |     INT4 |           |  
|         Epoch |   TT2000 |        ns |  
|       Quality |     INT4 |           |
| FSPC1 (a/b/c) |     INT2 | cnts/50ms |  
|         FSPC2 |     INT2 | cnts/50ms |  
|         FSPC3 |     INT2 | cnts/50ms |  
|         FSPC4 |     INT2 | cnts/50ms |  

*Table 4.1.3* - Level Two FSPC Contents

|           Variable | Datatype |     Units |  
| ------------------ | -------- | --------- |  
|         FrameGroup |     INT4 |           |  
|              Epoch |   TT2000 |        ns |  
|            Quality |     INT4 |           |
|      FSPC1 (a/b/c) |     INT2 | cnts/50ms |  
| cnt_error1 (a/b/c) |    FLOAT | cnts/50ms |  
|              FSPC2 |     INT2 | cnts/50ms |  
|         cnt_error2 |    FLOAT | cnts/50ms |  
|              FSPC3 |     INT2 | cnts/50ms |  
|         cnt_error3 |    FLOAT | cnts/50ms |  
|              FSPC4 |     INT2 | cnts/50ms |  
|         cnt_error4 |    FLOAT | cnts/50ms |  
|        FSPC1_Edges | FLOAT[7] |       keV |  

### 4.2 MSPC - Medium Spectra

Medium spectra are the result of accumulating counts in 48 channels for 4 seconds.  Each record contains all 48 channels and is comprised of 4 frames. Unlike the FSPC spectra, all of the channels in the record are stored in an array who's indices are the channel number. A description of the nominal energy binning scheme can be found  in the 'BARREL Telemetry Interface Control Document' located at [http://www.dartmouth.edu/~barrel/documents.html](http://www.dartmouth.edu/~barrel/documents.html).


*Table 4.2.1* - Level One MSPC Contents

|   Variable | Datatype |     Units |  
| ---------- | -------- | --------- |  
| FrameGroup |     INT4 |           |  
|      Epoch |   TT2000 |        ns |  
|    Quality |     INT4 |           |
|       MSPC | INT4[48] | cnts/4sec |  
|        ch* | INT4[48] |           |  

*Table 4.2.2* - Level Two MSPC Contents

|       Variable |  Datatype |        Units |  
| -------------- | --------- | ------------ |  
|     FrameGroup |      INT4 |              |  
|          Epoch |    TT2000 |           ns |  
|        Quality |      INT4 |              |  
| HalfAccumTime* |      INT8 |           ns |  
|           MSPC | FLOAT[48] | cnts/sec/keV |  
|        energy* | FLOAT[48] |          keV |  
|      cnt_error | FLOAT[48] | cnts/sec/keV |  
|       channel* | UINT1[48] |              |  
|  HalfBinWidth* | FLOAT[48] |          keV |  

### 4.3 SSPC - Slow Spectra

SSPC is very similar to MSPC with the main difference that counts are accumulated over 32 seconds and split into 256 channels. Again, a description of the nominal energy binning scheme can be found  in the 'BARREL Telemetry Interface Control Document' located at [http://www.dartmouth.edu/~barrel/documents.html](http://www.dartmouth.edu/~barrel/documents.html).


The differences between L1 and L2 SSPC files are outlined in Tables 7 and 8.

*Table 4.3.1* - Level One SSPC Contents

|    Variable | Datatype |     Units |  
| ----------- | -------- | --------- |  
|  FrameGroup |     INT4 |           |  
|       Epoch |   TT2000 |        ns |  
|     Quality |     INT4 |           |
|        MSPC | INT4[48] | cnts/4sec |  
|         ch* | INT4[48] | cnts/4sec |  

*Table 4.3.2* - Level Two SSPC Contents

|        Variable |   Datatype |        Units |  
| --------------- | ---------- | ------------ |  
|      FrameGroup |       INT4 |              |  
|           Epoch |     TT2000 |           ns |  
|         Quality |       INT4 |              |  
|  HalfAccumTime* |       INT8 |           ns |  
|            SSPC | FLOAT[256] | cnts/sec/keV |  
|          energy | FLOAT[256] |          keV |  
|       cnt_error | FLOAT[256] | cnts/sec/keV |  
|        channel* | UINT1[256] |              |  
|   HalfBinWidth* | FLOAT[256] |          kev |  


### 4.4 RCNT - Rate Counters

High Level, Low Level, and Peak Detector are counted on the analog board. Low Level and Peak Detector are for circuit diagnostics. Low Level counts excursions above a baseline and includes rejected events. Peak Detector counts peaks detected on the ADC board. For low count rate, low-noise environment, and at room temperature: Low Level = Peak Detect + High Level and Peak Detector = Interrupt.
Interrupt counts analyzed (ADC) x-rays as accepted by the DPU board.
The only difference between L1 and L2 data is that in L1 the units are counts/4seconds and in L2 the units are counts/second.


*Table 4.4.1* - Level One RCNT Contents

|    Variable | Datatype |  L1 Units |  
| ----------- | -------- | --------- |  
|  FrameGroup |     INT4 |           |  
|       Epoch |   TT2000 |        ns |  
|     Quality |     INT4 |           |
|     PeakDet |     INT2 | cnts/4sec |  
|    LowLevel |     INT2 | cnts/4sec |  
|   Interrupt |     INT2 | cnts/4sec |  
|   HighLevel |     INT2 | cnts/4sec |  


*Table 4.4.2* - Level Two RCNT Contents

|    Variable | Datatype |  L2 Units |  
| ----------- | -------- | --------- |  
|  FrameGroup |     INT4 |           |  
|       Epoch |   TT2000 |        ns |  
|     Quality |     INT4 |           |
|     PeakDet |    FLOAT |  cnts/sec |  
|    LowLevel |    FLOAT |  cnts/sec |  
|   Interrupt |    FLOAT |  cnts/sec |  
|   HighLevel |    FLOAT |  cnts/sec |  

### 4.5 MAGN - Magnetometer

The analog magnetometer data is encoded by a standalone ADC. The data are collected from the X, Y, and Z axes at 4Hz and are transmitted in each frame. Each frame is split into 4 records. The digital word transmitted by the payload can be decoded with the following formula: $B_{analog} = \frac{B_{digital} - 8388608.0}{83886.070}$.

The magnetometor data has not been "unspun", so there are fluctuations due to payload motion. Furthermore, the 3 axes have not been gain calibrated, so the fuctuations due to motion will present themselves in the |B| variable as well.


*Table 4.5.1* - Level One MAGN Contents

|   Variable | Datatype |     Units |  
| ---------- | -------- | --------- |  
| FrameGroup |     INT4 |           |  
|      Epoch |   TT2000 |        ns |  
|    Quality |     INT4 |           |
|      MAG_X |     INT4 |           |  
|      MAG_Y |     INT4 |           |  
|      MAG_Z |     INT4 |           |  

*Table 4.5.2* - Level Two MAGN Contents

|           Variable | Datatype |     Units |  
| ------------------ | -------- | --------- |  
|         FrameGroup |     INT4 |           |  
|              Epoch |   TT2000 |        ns |  
|            Quality |     INT4 |           |
| MAG_X_uncalibrated |    FLOAT |     &mu;T |  
| MAG_Y_uncalibrated |    FLOAT |     &mu;T |  
| MAG_Z_uncalibrated |    FLOAT |     &mu;T |  
|              Total |    FLOAT |     &mu;T |  

### 4.6 EPHM - Ephemeris

*Table 4.6.1* - Level One EPHM Contents

|    Variable | Datatype |             Units |  
| ----------- | -------- | ----------------- |  
|  FrameGroup |     INT4 |                   |  
|       Epoch |   TT2000 |                ns |  
|     Quality |     INT4 |                   |
|     GPS_Lat | INT4[48] | 2^-31 semi-circle |  
|     GPS_Lon | INT4[48] | 2^-31 semi-circle |  
|     GPS_Alt | INT4[48] |                mm |  
|  ms_of_week | INT4[48] |                ms |  

*Table 4.6.2* - Level Two EPHM Contents

|     Variable |  Datatype |         Units |  
| ------------ | --------- | ------------- |  
|   FrameGroup |     INT4  |               |  
|        Epoch |   TT2000  |            ns |  
|      Quality |     INT4  |               |
|      GPS_Lat | FLOAT[48] | degrees North |  
|      GPS_Lon | FLOAT[48] |  degrees East |  
|      GPS_Alt | FLOAT[48] |            km |  
| MLT_Kp2_T89c | FLOAT[48] |            hr |  
| MLT_Kp2_T89c | FLOAT[48] |            hr |  
|        L_Kp2 | FLOAT[48] |               |  
|        L_Kp2 | FLOAT[48] |               |  

### 4.7 HKPG - Housekeeping

Housekeeping data are transmitted as digital words calculated by an ADC and multiplexed as mod40. These values are saved to the L1 files while the scaled (physical units) values are saved to the L2 files. The 'BARREL Housekeeping Assignments' document gives the conversion factors for scaling the digital data and the 'BARREL Telemetry Interface Control Document' lists the order in which the housekeeping data are transmitted.



*Table 4.7.1* - Level One and Two HKPG Contents

|         Variable | Dattaype |                   Units |  
|----------------- | -------- | ----------------------- |  
|       FrameGroup |     INT4 |                         |
|            Epoch |   TT2000 |                     ns  |
|          Quality |     INT4 |                         |
|        numOfSats |     INT2 |                         |
|       timeOffset |     INT2 |                    sec  |
|       termStatus |     INT2 |                         |
|       cmdcounter |     INT4 |                         |
|     modemcounter |     INT2 |                         |
|       dcdcounter |     INT2 |                         |
|            weeks |     INT4 |                         |
|         T0_Scint |     INT2 | &deg;C (Level Two Only) |
|           T1_Mag |     INT2 | &deg;C (Level Two Only) |
|    T2_ChargeCont |     INT2 | &deg;C (Level Two Only) |
|       T3_Battery |     INT2 | &deg;C (Level Two Only) |
|     T4_PowerConv |     INT2 | &deg;C (Level Two Only) |
|           T5_DPU |     INT2 | &deg;C (Level Two Only) |
|         T6_Modem |     INT2 | &deg;C (Level Two Only) |
|     T7_Structure |     INT2 | &deg;C (Level Two Only) |
|        T8_Solar1 |     INT2 | &deg;C (Level Two Only) |
|        T9_Solar2 |     INT2 | &deg;C (Level Two Only) |
|       T10_Solar3 |     INT2 | &deg;C (Level Two Only) |
|       T11_Solar4 |     INT2 | &deg;C (Level Two Only) |
|     T12_TermTemp |     INT2 | &deg;C (Level Two Only) |
|     T13_TermBatt |     INT2 | &deg;C (Level Two Only) |
|      T14_TermCap |     INT2 | &deg;C (Level Two Only) |
|         T15_Stat |     INT2 | &deg;C (Level Two Only) |
|    V0_VoltAtLoad |     INT2 |      V (Level Two Only) |
|       V1_Battery |     INT2 |      V (Level Two Only) |
|        V2_Solar1 |     INT2 |      V (Level Two Only) |
|     V3\_POS\_DPU |     INT2 |      V (Level Two Only) |
| V4\_POS\_XRayDet |     INT2 |      V (Level Two Only) |
|         V5_Modem |     INT2 |      V (Level Two Only) |
| V6\_NEG_\XRayDet |     INT2 |      V (Level Two Only) |
|     V7\_NEG\_DPU |     INT2 |      V (Level Two Only) |
|           V8_Mag |     INT2 |      V (Level Two Only) |
|        V9_Solar2 |     INT2 |      V (Level Two Only) |
|       V10_Solar3 |     INT2 |      V (Level Two Only) |
|       V11_Solar4 |     INT2 |      V (Level Two Only) |
|     I0_TotalLoad |     INT2 |      A (Level Two Only) |
|    I1_TotalSolar |     INT2 |      A (Level Two Only) |
|        I2_Solar1 |     INT2 |      A (Level Two Only) |
|         I3_POSPU |     INT2 |      A (Level Two Only) |
| I4\_POS\_XRayDet |     INT2 |     mA (Level Two Only) |
|         I5_Modem |     INT2 |     mA (Level Two Only) |
| I6\_NEG\_XRayDet |     INT2 |     mA (Level Two Only) |
|     I7\_NEG\_DPU |     INT2 |     mA (Level Two Only) |
     
### 4.8 MISC - Miscellaneous

The MISC file contains 1Hz data. It holds both the PPS variable and the DPU version number that is transmitted in each frame. In both L1 and L2 the PPS variable is an INT4 that represents the number of milliseconds in to the frame that the GPS PPS signal was received. The DPU Version variable is INT2 in both L1 and L2 and has a valid range from 0-31.


*Table 4.8.1* - Level One and Two MISC Contents

|               Variable | Datatype |     Units |  
| ---------------------- | -------- | --------- |  
|             FrameGroup |     INT4 |           |  
|                  Epoch |   TT2000 |        ns |  
|                GPS_PPS |     INT4 |        ms |  
|                Version |     INT2 |           |
|             Payload_ID |     INT2 |           |
| Time\_Model\_Intercept |   DOUBLE |           |  
|     Time\_Model\_Slope |   DOUBLE |           |  

### 4.9 Variable Definitions

5 Timestamp generation
======================