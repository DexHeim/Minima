package org.minima.database.userdb.java;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.minima.database.MinimaDB;
import org.minima.objects.TxPOW;
import org.minima.objects.base.MiniData;
import org.minima.objects.base.MiniNumber;
import org.minima.objects.proofs.TokenProof;
import org.minima.utils.Streamable;
import org.minima.utils.json.JSONArray;
import org.minima.utils.json.JSONObject;

public class reltxpow implements Streamable {
	
	TxPOW 	mTxPow;
	
	Hashtable<String, MiniNumber> mTokenValues;
	
	public reltxpow() {}
	
	public reltxpow(TxPOW zTxPow, Hashtable<String, MiniNumber> zValues) {
		mTxPow        = zTxPow;
		mTokenValues  = zValues;
	}
	
	public JSONObject toJSON(MinimaDB zDB) {
		JSONObject ret = new JSONObject();
		
		ret.put("txpow", mTxPow.toJSON());
		
		JSONArray tokarray = new JSONArray();
		Enumeration<String> tokens = mTokenValues.keys();
		while(tokens.hasMoreElements()) {
			String token   = tokens.nextElement();
			MiniNumber amt = mTokenValues.get(token);
			
			JSONObject json = new JSONObject();
			json.put("token", token);
			
			//Get the Token Name
			MiniNumber scale = MiniNumber.ONE;
			if(token.equals("0x00")) {
				json.put("name", "Minima");	
			}else if(token.equals("0xFF")) {
				json.put("name", "Create Token");	
			
			}else {
				//Get the Token Proof..
				TokenProof tp = zDB.getUserDB().getTokenDetail(new MiniData(token));
				
				if(tp == null) {
					json.put("name", "null");
				}else {
					json.put("name", tp.getName());
					scale = tp.getScaleFactor();
				}
			}
			json.put("amount", amt.mult(scale).toString());
			tokarray.add(json);
		}
		ret.put("values", tokarray);
		
		return ret;
	}

	@Override
	public void writeDataStream(DataOutputStream zOut) throws IOException {
		mTxPow.writeDataStream(zOut);
		
		//Write out the hashtable..
		int len = mTokenValues.size();
		zOut.writeInt(len);
		Enumeration<String> tokens = mTokenValues.keys();
		while(tokens.hasMoreElements()) {
			String token   = tokens.nextElement();
			MiniNumber amt = mTokenValues.get(token);
			
			zOut.writeUTF(token);
			amt.writeDataStream(zOut);
		}
	}

	@Override
	public void readDataStream(DataInputStream zIn) throws IOException {
		mTxPow = new TxPOW();
		mTxPow.readDataStream(zIn);
		
		//read in the HashTable..
		mTokenValues = new Hashtable<>();
		int len = zIn.readInt();
		for(int i=0;i<len;i++) {
			String token = zIn.readUTF();
			MiniNumber amt = MiniNumber.ReadFromStream(zIn);
			
			mTokenValues.put(token, amt);
		}
	}
}
