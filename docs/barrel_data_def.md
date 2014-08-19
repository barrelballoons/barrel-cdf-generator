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
|    FSPC1a |    FSPC1 |         0.0 - 45.6 |
|    FSPC1b |    FSPC1 |        48.0 - 93.6 |
|    FSPC1c |    FSPC1 |       96.0 - 177.6 |
|    FSPC2  |    FSPC2 |      180.0 - 549.6 |
|    FSPC3  |    FSPC3 |      552.0 - 837.6 |
|    FSPC4  |    FSPC4 |     840.0 - 1485.6 |

*Table 4.1.2* - Level One FSPC Contents

|      Variable | Datatype |     Units |  
| ------------- | -------- | --------- |  
|    FrameGroup |     INT4 |           |  
|         Epoch |   TT2000 |        ns |  
|       Quality |     INT4 |           |
| FSPC1 (a/b/c) |     INT4 | cnts/50ms |  
|         FSPC2 |     INT4 | cnts/50ms |  
|         FSPC3 |     INT4 | cnts/50ms |  
|         FSPC4 |     INT4 | cnts/50ms |  

*Table 4.1.3* - Level Two FSPC Contents

|           Variable | Datatype |     Units |  
| ------------------ | -------- | --------- |  
|         FrameGroup |     INT4 |           |  
|              Epoch |   TT2000 |        ns |  
|            Quality |     INT4 |           |
|      FSPC1 (a/b/c) |     INT4 | cnts/50ms |  
| cnt_error1 (a/b/c) |    FLOAT | cnts/50ms |  
|              FSPC2 |     INT4 | cnts/50ms |  
|         cnt_error2 |    FLOAT | cnts/50ms |  
|              FSPC3 |     INT4 | cnts/50ms |  
|         cnt_error3 |    FLOAT | cnts/50ms |  
|              FSPC4 |     INT4 | cnts/50ms |  
|         cnt_error4 |    FLOAT | cnts/50ms |  
|        FSPC1_Edges | FLOAT[7] |       keV |  

### 4.2 MSPC - Medium Spectra
Medium spectra are the result of accumulating counts in 48 channels for 4 seconds. 

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

*Table 4.4.1* - Level One RCNT Contents

|    Variable | Datatype |     Units |  
| ----------- | -------- | --------- |  
|  FrameGroup |     INT4 |           |  
|       Epoch |   TT2000 |        ns |  
|     Quality |     INT4 |           |
|     PeakDet |     INT8 | cnts/4sec |  
|    LowLevel |     INT8 | cnts/4sec |  
|   Interrupt |     INT8 | cnts/4sec |  
|   HighLevel |     INT8 | cnts/4sec |  

*Table 4.4.2* - Level Two RCNT Contents

|    Variable | Datatype |     Units |  
| ----------- | -------- | --------- |  
|  FrameGroup |     INT4 |           |  
|       Epoch |   TT2000 |        ns |  
|     Quality |    FLOAT |           |
|     PeakDet |    FLOAT |  cnts/sec |  
|    LowLevel |    FLOAT |  cnts/sec |  
|   Interrupt |    FLOAT |  cnts/sec |  
|   HighLevel |    FLOAT |  cnts/sec |  

### 4.5 MAGN - Magnetometer

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

|     Variable | Datatype |         Units |  
| ------------ | -------- | ------------- |  
|   FrameGroup |     INT4 |               |  
|        Epoch |   TT2000 |            ns |  
|      Quality |     INT4 |               |
|      GPS_Lat | INT4[48] | degrees North |  
|      GPS_Lon | INT4[48] |  degrees East |  
|      GPS_Alt | INT4[48] |            km |  
| MLT_Kp2_T89c | INT4[48] |            hr |  
| MLT_Kp2_T89c | INT4[48] |            hr |  
|        L_Kp2 | INT4[48] |               |  
|        L_Kp2 | INT4[48] |               |  

### 4.7 HKPG - Housekeeping

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
|         T0_Scint |     INT8 | &deg;C (Level Two Only) |
|           T1_Mag |     INT8 | &deg;C (Level Two Only) |
|    T2_ChargeCont |     INT8 | &deg;C (Level Two Only) |
|       T3_Battery |     INT8 | &deg;C (Level Two Only) |
|     T4_PowerConv |     INT8 | &deg;C (Level Two Only) |
|           T5_DPU |     INT8 | &deg;C (Level Two Only) |
|         T6_Modem |     INT8 | &deg;C (Level Two Only) |
|     T7_Structure |     INT8 | &deg;C (Level Two Only) |
|        T8_Solar1 |     INT8 | &deg;C (Level Two Only) |
|        T9_Solar2 |     INT8 | &deg;C (Level Two Only) |
|       T10_Solar3 |     INT8 | &deg;C (Level Two Only) |
|       T11_Solar4 |     INT8 | &deg;C (Level Two Only) |
|     T12_TermTemp |     INT8 | &deg;C (Level Two Only) |
|     T13_TermBatt |     INT8 | &deg;C (Level Two Only) |
|      T14_TermCap |     INT8 | &deg;C (Level Two Only) |
|         T15_Stat |     INT8 | &deg;C (Level Two Only) |
|    V0_VoltAtLoad |     INT8 |      V (Level Two Only) |
|       V1_Battery |     INT8 |      V (Level Two Only) |
|        V2_Solar1 |     INT8 |      V (Level Two Only) |
|     V3\_POS\_DPU |     INT8 |      V (Level Two Only) |
| V4\_POS\_XRayDet |     INT8 |      V (Level Two Only) |
|         V5_Modem |     INT8 |      V (Level Two Only) |
| V6\_NEG_\XRayDet |     INT8 |      V (Level Two Only) |
|     V7\_NEG\_DPU |     INT8 |      V (Level Two Only) |
|           V8_Mag |     INT8 |      V (Level Two Only) |
|        V9_Solar2 |     INT8 |      V (Level Two Only) |
|       V10_Solar3 |     INT8 |      V (Level Two Only) |
|       V11_Solar4 |     INT8 |      V (Level Two Only) |
|     I0_TotalLoad |     INT8 |      A (Level Two Only) |
|    I1_TotalSolar |     INT8 |      A (Level Two Only) |
|        I2_Solar1 |     INT8 |      A (Level Two Only) |
|         I3_POSPU |     INT8 |      A (Level Two Only) |
| I4\_POS\_XRayDet |     INT8 |     mA (Level Two Only) |
|         I5_Modem |     INT8 |     mA (Level Two Only) |
| I6\_NEG\_XRayDet |     INT8 |     mA (Level Two Only) |
|     I7\_NEG\_DPU |     INT8 |     mA (Level Two Only) |
     
### 4.8 MISC - Miscellaneous

*Table 4.8.1* - Level One and Two MISC Contents

|               Variable | Datatype |     Units |  
| ---------------------- | -------- | --------- |  
|             FrameGroup |     INT4 |           |  
|                  Epoch |   TT2000 |        ns |  
|                GPS_PPS |     INT4 |        ms |
|             Payload_ID |     INT4 |           |
| Time\_Model\_Intercept |   DOUBLE |           |  
|     Time\_Model\_Slope |   DOUBLE |           |  

### 4.9 Variable Definitions
