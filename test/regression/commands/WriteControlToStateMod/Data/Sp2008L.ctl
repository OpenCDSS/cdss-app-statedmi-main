# File edited by Steve Malers to clean up as a standard test
# 
# Control file for StateMod *.ctl
#             
#             
#
  Lower South Platte StateMod Historic Monthly Simulation
  May 15, 2005
    1950     : iystr   Starting year of simulation
    1950     : iyend   Ending year of simulation
       2     : iresop  Output units (1=cfs,2=acft,3=KAF,4=cfs day and acft month,5=cms)
       0     : moneva  Type of evap/precip data (0=monthly, 1=average)
       1     : iopflo  Type of stream inflow (1=total, 2=gains)
       2     : numpre  Number of precipitation stations
       2     : numeva  Number of evaporation stations
      -1     : interv  Intervals in delay table (n=fixed, %;-1=var, %; -100=var, fraction)
  1.9835     : factor  Factor to convert cfs to acft/day (1.9835)
  1.9835     : rfacto  Divisor for streamflow data (0 for data in cfs, 1.9835 for acft/month)
  1.9835     : dfacto  Divisor for diversion data (0 for data in cfs, 1.9835 for acft/month)
  0.0000     : ffacto  Divisor for instream flow data (0 for data in cfs, 1.9835 for acft/month)
  1.0000     : cfacto  Factor to convert reservoir content to acft
  0.0833     : efacto  Factor to convert evaporation data to feet
  0.0833     : pfacto  Factor to convert precipitation data to feet
  CYR        : cyr1    Year type (a5, all caps, right justified: CYR, WYR, or IYR)
       1     : icondem Demand type (1=Historical Demand,2=Historical Sum,3=Structure Demand,4=Supply Demand,5=Decreed Demand)
     145     : ichk    Detailed print (0=off,1=net,4=calls,5=dem,6=day,7=ret,91=well,92=soil,-NodeId,see documentation)
    -100     : ireopx  Re-operation switch (0=re-operate,1=no re-operation,-10=reoperate for releases>10 acft/month)
       1     : ireach  Instream flow approach (0=no reach,1=reach approach,2=0+monthly demands,3=1+monthly demands)
       0     : icall   Detailed call data (0=no, 1=yes)
64039060.04  : ccall   Detailed call water right ID (if icall != 0)
       0     : iday    Daily calculations (0=monthly,1=daily)
       1     : iwell   Wells (-1=no but files in .rsp,0=no,1=well with no gwmaxrc,2=wells with gwmaxrc,3=wells with var gwmaxrc)
     0.0     : gwmaxrc Maximum recharge limit, cfs (if iwell=2)
       0     : isjrip  (SJRIP) sediment file (-1=no but file in .rsp,0=no,1=yes)
      10     : itsfile Use *.ipy file (-1=no but file in .rsp,0=no,1=annual GW lim,2=annual well cap only,10=all data)
       1     : ieffmax Use consumptive water requirement file (-1=no but file in .rsp,0=no,1=yes,2=see documentation)
       1     : isprink Use sprinkler area and efficiency data (0=no, 1=yes)
     3.0     : soild   Soil moist accounting (-1=no but file in .rsp,0=no,+n=soil zone depth in ft)
       0     : isig    Digits after decimal point in output (0=none,1=one,2=two)
