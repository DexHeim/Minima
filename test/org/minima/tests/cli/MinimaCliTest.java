package org.minima.tests.cli;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.minima.utils.json.JSONObject;
import org.minima.utils.json.parser.JSONParser;

public class MinimaCliTest {

    public static MinimaTestNode minimaTestNode;
    
    @BeforeClass
    public static void initTest() throws Exception {
        //Start up Minima
        minimaTestNode = new MinimaTestNode();
    }
    
    @AfterClass
    public static void finishTest() throws Exception {
        //Shut her down
        minimaTestNode.killMinima();
    }

    public void runBaseTests (String output) throws Exception {
        System.out.println("Printing the output of the command:");
        System.out.println(output);

        //The cmd response should be valid JSON
        JSONObject json = (JSONObject) new JSONParser().parse(output);

        //status of the cmd request must be true
        System.out.println("status must be true: " + json.get("status"));
        assertTrue((boolean)json.get("status"));

        //cmd response pending should be false
        System.out.println("pending must be false:" + json.get("pending").toString());
        assertFalse((boolean)json.get("pending"));
    }

}