package DWR.DMI.StateDMI;

import javax.swing.AbstractListModel;

import RTi.Util.IO.Command;
import RTi.Util.IO.CommandListListener;

/**
This class provides a way for the Swing JList and other components to
display time series commands that are managed in a StateDMI_Processor.
 */
public class StateDMI_Processor_ListModel extends AbstractListModel
implements CommandListListener
{

/**
The TSCommand processor that the list model maps to.
*/
private StateDMI_Processor __processor;

/**
Constructor for ListModel for StateDMI_Processor instance.
@param StateDMI_Processor processor A StateDMI_Processor instance that
can be displayed in a JList or other list via this ListModel.
*/
public StateDMI_Processor_ListModel ( StateDMI_Processor processor )
{
	__processor = processor;
	processor.addCommandListListener ( this );
}

/**
Add a command at the end of the list.
@param command_string Command string for command.
*/
public void addElement ( Command command )
{
	__processor.addCommand ( command );
}

/**
Add a command at the end of the list using the string text.  This should currently only be
used for commands that do not have command classes, which perform
additional validation on the commands.  A GenericCommand instance will
be instantiated to maintain the string and allow command status to be
set.
@param command_string Command string for command.
*/
public void addElement ( String command_string )
{
	__processor.addCommand ( command_string );
}

/**
Called when one or more commands have been added in the StateDMI_Processor.
@param index0 The index (0+) of the first command that is added.
@param index1 The index (0+) of the last command that is added.
*/
public void commandAdded ( int index0, int index1 )
{
	fireIntervalAdded ( this, index0, index1 );
}

/**
Called when one or more commands have changed in the StateDMI_Processor,
for example in a change in definition or status.
@param index0 The index (0+) of the first command that is changed.
@param index1 The index (0+) of the last command that is changed.
*/
public void commandChanged ( int index0, int index1 )
{
	fireContentsChanged ( this, index0, index1 );
}

/**
Handle when one or more commands have been removed in the StateDMI_Processor.
@param index0 The index (0+) of the first command that is removed.
@param index1 The index (0+) of the last command that is removed.
*/
public void commandRemoved ( int index0, int index1 )
{
	fireIntervalRemoved ( this, index0, index1 );
}

/**
Finalize the class before garbage collection.
*/
protected void finalize ()
throws Throwable
{	// Remove the listener from the processor
	__processor.removeCommandListListener ( this );
	super.finalize();
}

/**
Get the Command at the requested position.  This simply calls
get().
@param pos Command position, 0+.
@return the Command instance at the requested position.
*/
public Object get ( int pos )
{
	return __processor.get ( pos );
}

/**
Get the Command at the requested position.
@param pos Command position, 0+.
@return the Command instance at the requested position.
*/
public Object getElementAt ( int pos )
{
	return get ( pos );
}

/**
Get the number of Command objects being managed by the StateDMI_Processor.
@return the number of commands being managed by the command processor.
*/
public int getSize()
{	return __processor.size();
}

/**
Add a command using the string text.  This should currently only be
used for commands that do not have command classes, which perform
additional validation on the commands.  A GenericCommand instance will
be instantiated to maintain the string and allow command status to be
set.
@param command_string Command string for command.
@param index Position (0+) at which to add the command.
*/
public void insertElementAt ( String command_string, int index )
{
	__processor.insertCommandAt ( command_string, index );
}

/**
Add a command using a Command instance, for example as created
from TSCommandFactory.
@param command Command to add.
@param index Position (0+) at which to add the command.
*/
public void insertElementAt ( Command command, int index )
{
	__processor.insertCommandAt ( command, index );
}

/**
Remove all commands.
*/
public void removeAllElements ( )
{
	__processor.removeAllCommands ();
}

/**
Remove a command at an index.
@param index Position (0+) at which to remove the command.
*/
public void removeElementAt ( int index )
{
	__processor.removeCommandAt ( index );
}

/**
Get the number of Command objects being managed by the StateDMI_Processor.
This method calls getSize().
@return the number of commands being managed by the command processor.
*/
public int size()
{	return getSize();
}

}

