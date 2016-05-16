package DWR.DMI.StateDMI;

import java.io.File;
import java.util.ArrayList;

import junit.framework.TestCase;

public class CoverageTest extends TestCase 
{
	ArrayList tests;

	protected void setUp() throws Exception 
	{
		//tests = new ArrayList();
		//visitAllFiles(new File("test\\regression\\commands\\CDSS_General"));
	}

	public void testCoverage()
	{
		//	make sure to run in batch mode
//		String home = "test\\operational\\CDSS";
//	    IOUtil.isBatch(true);
//	    IOUtil.setProgramWorkingDir(home);
//	    IOUtil.setApplicationHomeDir(home);
//	    JGUIUtil.setLastFileDialogDirectory(home);
//	       
//	    StateDMI_JFrame statecu_jframe = new StateDMI_JFrame(0);
//	    //assertEquals(true, statecu_jframe.runCommandsFromFile(
//	    //new File("test\\regression\\commands\\CDSS_General\\readDiversionRightsFromHydroBase\\simpleRead.StateDMI")));
//		for ( int i = 0; i < tests.size(); i++ )
//		{
//				statecu_jframe.runCommandsFromFile(
//						new File(tests.get(i).toString()));
//		}
	}
	
	public void visitAllFiles(File dir) {
		
		if (dir.isDirectory()) 
		{
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) 
			{
				visitAllFiles(new File(dir, children[i]));
			}
		}
		else 
		{
			//add to list
			if(dir.toString().endsWith(".StateDMI"))
				tests.add(dir.toString());
		}
	}	
	
}
