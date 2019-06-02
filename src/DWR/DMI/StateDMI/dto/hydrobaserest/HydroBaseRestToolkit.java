// HydroBaseRestToolkit - functions used by HydroBase REST API

/* NoticeStart

StateDMI
StateDMI is a part of Colorado's Decision Support Systems (CDSS)
Copyright (C) 1997-2019 Colorado Department of Natural Resources

StateDMI is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

StateDMI is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

You should have received a copy of the GNU General Public License
    along with StateDMI.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package DWR.DMI.StateDMI.dto.hydrobaserest;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import DWR.DMI.HydroBaseDMI.HydroBase_NetAmts;
import DWR.DMI.HydroBaseDMI.HydroBase_ParcelUseTS;
import cdss.dmi.hydrobase.rest.dao.ParcelUseTimeSeries;
import cdss.dmi.hydrobase.rest.dao.WaterRightsNetAmount;

public class HydroBaseRestToolkit {
	
	private static HydroBaseRestToolkit instance;
	
	private HydroBaseRestToolkit(){}
	
	//Lazy initialization of a singleton class instance
	public static HydroBaseRestToolkit getInstance(){
		if(instance == null){
			instance = new HydroBaseRestToolkit();
		}
		return instance;
	}
	
	/*
	 * Convert a WaterRightsNetAmount POJO converted from web services using Jackson 
	 * and further convert into a HydroBase_NetAmts POJO to match the rest of code.
	 * @param {object} netAmountsRest - a WaterRightsNetAmount Plain Old Java Object
	 */
	public HydroBase_NetAmts toHydroBaseNetAmounts(WaterRightsNetAmount netAmountsRest){
		HydroBase_NetAmts netAmountsHB = new HydroBase_NetAmts();
		//TODO @jurentie this might need to be reconsidered. What if date is returned in different format
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		//ab
		
		//action comment
		netAmountsHB.setAction_comment(netAmountsRest.getComments());
		
		//adj date
		//netAmountsHB.setAdj_date(Timestamp.valueOf(netAmountsRest.getAdjudicationDate()));
		
		//adj type
		
		//admin no
		netAmountsHB.setAdmin_no(netAmountsRest.getAdminNumber());
		
		//apex
		//TODO @jurentie 08-06-2018 difference between net apex
		
		//apro date
		try {
			netAmountsHB.setApro_date(format.parse(netAmountsRest.getAppropriationDate().toString()));
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//collection id part type
		
		//common id
		netAmountsHB.setCommonID(netAmountsRest.getWdid());
		
		//cod
		
		//cty
		//TODO @jurentie 08-07-2018 this is not county...
		//netAmountsHB.setCty(Integer.parseInt(netAmountsRest.getCounty()));
		//TODO @jurentie 08-06-18 Rest: string -> HB: int
		
		//div 
		netAmountsHB.setDiv(netAmountsRest.getDivision());
		
		//id 
		netAmountsHB.setID(Integer.parseInt(netAmountsRest.getWdid().substring(2, netAmountsRest.getWdid().length())));
		//TODO @jurentie 08-16-18 Rest: string -> HB: int
		
		//net abs
		netAmountsHB.setNet_abs(netAmountsRest.getNetAbsolute());
		
		//net apex
		netAmountsHB.setNet_apex(netAmountsRest.getNetApexAbsolute());
		
		//net cond
		netAmountsHB.setNet_cond(netAmountsRest.getNetConditional());
		
		//net rate abs
		netAmountsHB.setNet_rate_abs(netAmountsRest.getNetAbsolute());
		
		//net rate apex
		netAmountsHB.setNet_rate_apex(netAmountsRest.getNetApexAbsolute());
		
		//net rate cond
		netAmountsHB.setNet_rate_cond(netAmountsRest.getNetConditional());
		
		//net num 
		
		//net vol abs
		netAmountsHB.setNet_vol_abs(netAmountsRest.getNetAbsolute());
		
		//net vol apex
		netAmountsHB.setNet_vol_apex(netAmountsRest.getNetApexAbsolute());
		
		//net vol cond
		netAmountsHB.setNet_vol_cond(netAmountsRest.getNetConditional());
		
		//order no
		netAmountsHB.setOrder_no(netAmountsRest.getOrderNumber());
		
		//padj date
		try {
			netAmountsHB.setPadj_date(format.parse(netAmountsRest.getPriorAdjudicationDate().toString()));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//parcel id
		
		//parcel match class
		
		//pm
		netAmountsHB.setPM(netAmountsRest.getPm());
		
		//pri case no
		
		//q160
		netAmountsHB.setQ160(netAmountsRest.getQ160());
		
		//q40
		netAmountsHB.setQ40(netAmountsRest.getQ40());
		
		//q10
		netAmountsHB.setQ10(netAmountsRest.getQ10());
		
		//r dir
		
		//right num
		
		//rng
		netAmountsHB.setRng(netAmountsRest.getRange());
		
		//rng a 
		
		//sec
		if(netAmountsRest.getSection() != null){
			netAmountsHB.setSec(Integer.parseInt(netAmountsRest.getSection()));
		}
		//TODO @jurentie 08-16-18 Rest: string -> HB: int
		
		//sec a
		
		//str type
		netAmountsHB.setStrtype(netAmountsRest.getDecreedUses());
		
		//structure num
		
		//tab trib 
		
		//t dir
		
		//ts
		netAmountsHB.setTS(netAmountsRest.getTownship());
		
		//ts a
		
		//unit
		netAmountsHB.setUnit(netAmountsRest.getDecreedUnits());
		
		//use
		netAmountsHB.setUse(netAmountsRest.getDecreedUses());
		
		//wd 
		netAmountsHB.setWD(netAmountsRest.getWaterDistrict());
		
		//wdid
		netAmountsHB.setWDID(netAmountsRest.getWdid());
		
		//wd stream name
		netAmountsHB.setWd_stream_name(netAmountsRest.getCounty());
		
		//wr stream no
		
		//wr name 
		netAmountsHB.setWr_name(netAmountsRest.getStructureName());
		
		//x str type
		
		//x wr stream no
		
		//x permit receipt
		
		//x yield gpm
		
		//x yield apex gpm
		
		//x permit date
		
		//x appro date
		
		//x prorated yield
		
		//x fraction yield
		
		//x ditch fraction
		
		return netAmountsHB;
	}
	
	/*
	 * Convert a ParcelUseTimeSeries POJO converted from web services using Jackson 
	 * and further convert into a HydroBase_PacelUseTS POJO to match the rest of code.
	 * @param {object} parcelUseTSRest - a ParcelUseTimeSeries Plain Old Java Object
	 */
	public HydroBase_ParcelUseTS toHydroBaseParcelUseTS( ParcelUseTimeSeries parcelUseTSRest ){
		HydroBase_ParcelUseTS parcelUseTSHB = new HydroBase_ParcelUseTS();
		
		//parcel num
		//parcelUseTSHB.setParcel_num(parcelUseTSRest.getParcelId());
		
		//div
		parcelUseTSHB.setDiv(parcelUseTSRest.getDiv());
		
		//cal year
		parcelUseTSHB.setCal_year(parcelUseTSRest.getCalYear());
		
		//parcel id
		parcelUseTSHB.setParcel_id(parcelUseTSRest.getParcelId());
		
		//perimeter
		
		//area
		parcelUseTSHB.setArea(parcelUseTSRest.getAcresTotal());
		
		//land use
		parcelUseTSHB.setLand_use(parcelUseTSRest.getLandUse());
		
		//irrig type
		parcelUseTSHB.setIrrig_type(parcelUseTSRest.getIrrigType());
		
		return parcelUseTSHB;
	}
	
}
