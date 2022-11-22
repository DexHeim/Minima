package org.minima.tests.cli.verifytest;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import org.minima.system.commands.CommandException;

import org.minima.utils.json.JSONArray;
import org.minima.utils.json.JSONObject;
import org.minima.utils.json.parser.JSONParser;

import org.minima.system.Main;
import org.minima.tests.cli.MinimaTestNode;
import org.minima.tests.cli.MinimaCliTest;

public class VerifyTest extends MinimaCliTest {

    public MinimaTestNode test = new MinimaTestNode();

    @Test
    public void testVerifyWithNoArgs () throws Exception
    {
        String output = test.runCommand("verify");

        runBaseTests(output);        

        test.killMinima();
    }

    @Test 
    public void testVerifyWithValidArgs () throws Exception 
    {
        System.out.println("Starting test");

        String publicKey = test.getPublicKey();

        System.out.println("Public key is " + publicKey);

        String data = "0x26DA9AE7D32A5702FB18D3D3034EF3EE50A8BA1F2DE0D280B0F2BB458B91A5F5"; //random data

        String output = test.runCommand("sign data:"+data+" publickey:"+publicKey);

        runBaseTestsWithValidArgs(output);

        JSONObject json = (JSONObject) new JSONParser().parse(output);

        assertTrue((boolean)json.get("status"));

        assertFalse((boolean)json.get("pending"));

        String signature = json.get("response").toString();

        System.out.println("Veryfiying the signature");

        output = test.runCommand("verify data:"+data+" publickey:"+publicKey + " signature:" + signature);

        System.out.println("Result of the signature verification");

        System.out.println(output.toString());

        runBaseTestsWithValidArgs(output);

        test.killMinima();
    }

    @Test
    public void testVerifyWithInvalidArgs () throws Exception 
    {
        System.out.println("Starting test");

        String publicKey = "0x26DA9AE7D32A5702FB18D3D3034EF3EE50A8BA1F2DE0D280B0F2BB458B91A5F5"; //not my public key

        System.out.println("Public key is " + publicKey);

        String data = "0x26DA9AE7D32A5702FB18D3D3034EF3EE50A8BA1F2DE0D280B0F2BB458B91A5F5"; //random data

        String output = test.runCommand("sign data:"+data+" publickey:"+publicKey);

        JSONObject json = (JSONObject) new JSONParser().parse(output);

        assertFalse((boolean)json.get("status"));

        assertFalse((boolean)json.get("pending"));

        String signature = json.get("response").toString();

        System.out.println("Veryfiying the signature");

        output = test.runCommand("verify data:"+data+" publickey:"+publicKey + " signature:" + signature);

        System.out.println("Result of the signature verification");

        System.out.println(output.toString());

        runBaseTestsWithInvalidArgs(output);

        test.killMinima();
    }
    
    public void runBaseTests (String output) throws Exception
    {
        //The cmd response should be valid JSON
        JSONObject json = (JSONObject) new JSONParser().parse(output);

        //status of the cmd request must be false
        System.out.println("status must be false: " + json.get("status"));
        assertFalse((boolean)json.get("status"));

        //cmd response pending should be false
        System.out.println("pending must be false:" + json.get("pending").toString());
        assertFalse((boolean)json.get("pending"));
    }

    public void runBaseTestsWithValidArgs (String output) throws Exception
    {
        //The cmd response should be valid JSON
        JSONObject json = (JSONObject) new JSONParser().parse(output);

        //status of the cmd request must be true
        System.out.println("status must be true: " + json.get("status"));
        assertTrue((boolean)json.get("status"));

        //cmd response pending should be false
        System.out.println("pending must be false:" + json.get("pending").toString());
        assertFalse((boolean)json.get("pending"));
    }

    public void runBaseTestsWithInvalidArgs (String output) throws Exception
    {
        //The cmd response should be valid JSON
        JSONObject json = (JSONObject) new JSONParser().parse(output);

        //status of the cmd request must be true
        System.out.println("status must be false: " + json.get("status"));
        assertFalse((boolean)json.get("status"));

        //cmd response pending should be false
        System.out.println("pending must be false:" + json.get("pending").toString());
        assertFalse((boolean)json.get("pending"));
    }
}