 package DWR.DMI.StateDMI;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;
import DWR.DMI.StateDMI.StateDMI_JFrame;
import java.io.File;
import java.io.IOException;

import RTi.Util.GUI.JGUIUtil;
import RTi.Util.IO.IOUtil;

public class StateDMI_JFrameTest extends TestCase {

	private File cmdFile;
	private File bogusFile;
	private StateDMI_JFrame statecu_jframe;
	private StateDMI_JFrame statemod_jframe;
	private String home = "test\\operational\\CDSS";
	
	
    public StateDMI_JFrameTest(String testname)
    {
        super(testname);
    }

    public StateDMI_JFrameTest()
    {
        
    }

    // Set up some properties and to test
    protected void setUp()
    {
       cmdFile = new File
       ("test\\regression\\commands\\CDSS_General\\readDiversionRightsFromHydroBase\\simpleRead.StateDMI");
       
       bogusFile = new File
       ("test\\regression\\commands\\CDSS_General\\BogusFile");
       
       // make sure to run in batch mode
       IOUtil.isBatch(true);
       IOUtil.setProgramWorkingDir(home);
       IOUtil.setApplicationHomeDir(home);
       JGUIUtil.setLastFileDialogDirectory(home);
       
       statecu_jframe = new StateDMI_JFrame(0);
       statemod_jframe = new StateDMI_JFrame(1);
    }
    /* FIXME SAM 2007-11-15 Test framework needs fixed.
    // runCommandsFromFile() will initialize the Vectors by calling
    // storeCommandsFromFile() and will run the commands by calling
    // runCommands().  This method tests statmod and statecu run types
    // with a good commands file and a non-existent file.
    public void testrunCommandsFromFile() throws IOException
    {
    	assertEquals(true, statecu_jframe.runCommandsFromFile(cmdFile));
    	assertEquals(true,statemod_jframe.runCommandsFromFile(cmdFile));
    	assertEquals(false, statecu_jframe.runCommandsFromFile(bogusFile));
    	assertEquals(false,statemod_jframe.runCommandsFromFile(bogusFile));
    }
    
    // storeCommandsFile() takes a file as an argument and initializes
    // some Vectors with the commands from the file.  This is needed
    // since the runCommands method reads from these Vectors.
    // This method tests with a good commands file and a non-existent one
    // for both statecu and statemod run types.
    public void teststoreCommandsFromFile()
    {
    	assertEquals(true, statecu_jframe.storeCommandsFromFile(cmdFile));
    	assertEquals(true,statemod_jframe.storeCommandsFromFile(cmdFile));
    	assertEquals(false, statecu_jframe.storeCommandsFromFile(bogusFile));
    	assertEquals(false,statemod_jframe.storeCommandsFromFile(bogusFile));
    }
    
    // runCommands runs a vector of commands by initializing a thread
    // of StateDMI_Processer.  This tests the different scenarios that 
    // runCommands might encounter.
    public void testrunCommands()
    {
    	//create some new local StateDMI_JFrame objects
    	StateDMI_JFrame statecu_jframe2 = new StateDMI_JFrame(0);
        StateDMI_JFrame statemod_jframe2 = new StateDMI_JFrame(1);
    	
        // negative tests
        // nothing is initialized before testing
        assertEquals(false, (statecu_jframe2.runCommands(true,true)));
    	assertEquals(false, (statemod_jframe2.runCommands(true,true)));
    	
    	assertEquals(false, (statecu_jframe2.runCommands(true,false)));
    	assertEquals(false, (statemod_jframe2.runCommands(true,false)));
    	
    	assertEquals(false, (statecu_jframe2.runCommands(false,true)));
    	assertEquals(false, (statemod_jframe2.runCommands(false,true)));
        
        assertEquals(false, (statecu_jframe2.runCommands(false,false)));
    	assertEquals(false, (statemod_jframe2.runCommands(false,false)));
    
    	// positive tests
    	// initialize vectors from commands file (needed to test)
    	statecu_jframe2.storeCommandsFromFile(cmdFile);
    	statemod_jframe2.storeCommandsFromFile(cmdFile);
    	
    	assertEquals(true, (statecu_jframe2.runCommands(true,true)));
    	assertEquals(true, (statemod_jframe2.runCommands(true,true)));
    	
    	assertEquals(true, (statecu_jframe2.runCommands(true,false)));
    	assertEquals(true, (statemod_jframe2.runCommands(true,false)));
    	
    	assertEquals(true, (statecu_jframe2.runCommands(false,true)));
    	assertEquals(true, (statemod_jframe2.runCommands(false,true)));
    	
    	assertEquals(true, (statecu_jframe2.runCommands(false,false)));
    	assertEquals(true, (statemod_jframe2.runCommands(false,false)));
    }
    
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTest((new StateDMI_JFrameTest("testrunCommandsFromFile")));
        suite.addTest((new StateDMI_JFrameTest("teststoreCommandsFromFile")));
        suite.addTest((new StateDMI_JFrameTest("testrunCommands")));
        return suite;
    }
    */
    
}

