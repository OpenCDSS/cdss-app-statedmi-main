package DWR.DMI.StateDMI;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import RTi.Util.Test.TestCollector;

public class StateDMITestSuite extends TestCase {

    private static List<String> testList;
    
    public StateDMITestSuite(String testname)
    {
        super(testname);
    }

    public StateDMITestSuite()
    {   
    }   
    
    public static Test suite() throws ClassNotFoundException
    {
        testList = new ArrayList<String>();
        TestSuite suite = new TestSuite();
        TestCollector tests = new TestCollector();
        File path = new File("test\\unit\\src");
        System.out.println(path.toString());
        tests.visitAllFiles(path);
        testList = tests.getTestList();
        
        for(int i = 0; i < testList.size(); i++)
        {
            String testName = (testList.get(i).toString());
            System.out.println(testName);
            String test = tests.formatFileName(testName);
            suite.addTestSuite(Class.forName(test));
        }
        
        return suite;
    } 
}

