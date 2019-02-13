// WriteReservoirStationsToList_Command - This class initializes and runs the WriteReservoirStationsToList() command.

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

package DWR.DMI.StateDMI;

/**
This class initializes and runs the WriteReservoirStationsToList() command.
Most functionality is implemented in the base class.
*/
public class WriteReservoirStationsToList_Command extends WriteToList_Command
{

/**
Constructor.
*/
public WriteReservoirStationsToList_Command ()
{	super();
	setCommandName ( "WriteReservoirStationsToList" );
}

}
