package org.minima.utils.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.minima.database.cascade.Cascade;
import org.minima.objects.TxBlock;
import org.minima.objects.TxPoW;
import org.minima.objects.TxBody;
import org.minima.objects.base.MiniData;
import org.minima.objects.base.MiniNumber;
import org.minima.utils.MinimaLogger;

import org.minima.utils.Streamable;
import org.minima.objects.Coin;
import org.minima.objects.StateVariable;
import org.minima.objects.CoinProof;
import org.minima.objects.Token;
import org.minima.objects.Address;
import org.minima.objects.TxPoW;

import org.minima.utils.json.JSONArray;
import org.minima.utils.json.JSONObject;
import org.minima.utils.json.parser.JSONParser;
import org.minima.utils.json.parser.ParseException;

public class MySQLConnect {

	public static final int MAX_SYNCBLOCKS = 250;

	String mMySQLHost;
	String mDatabase;
	String mUsername;
	String mPassword;

	ArrayList<String> mIndexes;

	Connection mConnection;

	/**
	 * PreparedStatements
	 */
	PreparedStatement SQL_INSERT_SYNCBLOCK 		= null;
	PreparedStatement SQL_FIND_SYNCBLOCK_ID 	= null;
	PreparedStatement SQL_FIND_SYNCBLOCK_NUM 	= null;
	PreparedStatement SQL_SELECT_RANGE			= null;

	PreparedStatement SQL_SELECT_LAST_BLOCK		= null;
	PreparedStatement SQL_SELECT_FIRST_BLOCK	= null;

	PreparedStatement SAVE_CASCADE				= null;
	PreparedStatement LOAD_CASCADE				= null;

	PreparedStatement SQL_INSERT_TXPOW				= null;
	PreparedStatement SQL_INSERT_TXHEADER			= null;
	PreparedStatement SQL_INSERT_TXPOWIDLIST	= null;
	PreparedStatement SQL_INSERT_TXPOWCOIN		= null;
	PreparedStatement SQL_INSERT_COIN					= null;
	PreparedStatement SQL_INSERT_COIN_STATE		= null;
	PreparedStatement SQL_INSERT_TOKEN				= null;

	PreparedStatement SQL_DELETE_TXPOW				= null;
	PreparedStatement SQL_DELETE_TXHEADER			= null;
	PreparedStatement SQL_DELETE_TXPOWIDLIST	= null;
	PreparedStatement SQL_DELETE_TXPOWCOIN		= null;
	PreparedStatement SQL_DELETE_COIN					= null;
	PreparedStatement SQL_DELETE_COIN_STATE		= null;

	PreparedStatement SQL_SELECT_LAST_TXPOW		= null;

	public MySQLConnect(String zHost, String zDatabase, String zUsername, String zPassword) {
		mMySQLHost 	= zHost;
		mDatabase	= zDatabase;
		mUsername	= zUsername;
		mPassword	= zPassword;
	}

	public void init() throws SQLException {
		//Init variable
		mIndexes = new ArrayList<>();

		//MYSQL JDBC connection
		String mysqldb = "jdbc:mysql://"+mMySQLHost+"/"+mDatabase+"?autoReconnect=true";

		mConnection = DriverManager.getConnection(mysqldb,mUsername,mPassword);

		Statement stmt = mConnection.createStatement();

		//Create a new DB
		String create = "CREATE TABLE IF NOT EXISTS `syncblock` ("
						+ "  `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
						+ "  `txpowid` varchar(80) NOT NULL UNIQUE,"
						+ "  `block` bigint NOT NULL UNIQUE,"
						+ "  `timemilli` bigint NOT NULL,"
						+ "  `syncdata` mediumblob NOT NULL"
						+ ")";

		//Run it..
		stmt.execute(create);

		//Create the cascade table
		String cascade = "CREATE TABLE IF NOT EXISTS `cascadedata` ("
						+ "		`id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
						+ "		`cascadetip` BIGINT NOT NULL,"
						+ "		`fulldata` mediumblob NOT NULL"
						+ ")";

		//Run it..
		stmt.execute(cascade);

		//Create the TxPoW table (TxPoW model from TxBlock-TxPoW)
		String tbl_txpow = "CREATE TABLE IF NOT EXISTS `txpow` ("
						+ "  `id` int NOT NULL AUTO_INCREMENT,"
						+ "  `txpowid` varchar(80) NOT NULL,"
						+ "  `block` bigint NOT NULL,"
						+ "  `superblock` int NOT NULL,"
						+ "  `size` bigint NOT NULL,"
						+ "  `burn` varchar(64) DEFAULT '0' NOT NULL,"
						+ "  `timemilli` bigint NOT NULL,"
						+ "  PRIMARY KEY(`id`),"
						+ "  INDEX `idx_txpow_txpowid` (`txpowid`),"
						+ "  INDEX `idx_txpow_timemilli` (`timemilli`),"
						+ "  CONSTRAINT `uidx_txpow_block` UNIQUE(`block`)"
						+ ")";

		//Run it..
		stmt.execute(tbl_txpow);

		//Create the coin table
		String coin = "CREATE TABLE IF NOT EXISTS `coin` ("
						+ "  `id` int NOT NULL AUTO_INCREMENT,"
						+ "  `coinid` varchar(80) NOT NULL,"
						+ "  `amount` varchar(64) NOT NULL,"
						+ "  `address` varchar(80) NOT NULL,"
						+ "  `miniaddress` varchar(80) NOT NULL,"
						+ "  `tokenid` varchar(80) NOT NULL,"
						+ "  `mmrentry` varchar(20) NOT NULL,"
						+ "  `created` bigint NOT NULL,"
						+ "  PRIMARY KEY (`id`),"
						+ "  INDEX `idx_coin_coinid` (`coinid`),"
						+ "  INDEX `idx_coin_address` (`address`),"
						+ "  INDEX `idx_coin_miniaddress` (`miniaddress`),"
						+ "  CONSTRAINT `uidx_coin_coinid` UNIQUE(`coinid`)"
						+ ")";

		//Run it..
		stmt.execute(coin);

		//Create the TxHeader table (TxHeader model from TxBlock-TxPoW-TxHeader)
		String tbl_txheader = "CREATE TABLE IF NOT EXISTS `txheader` ("
						+ "  `id` int NOT NULL AUTO_INCREMENT,"
						+ "  `txpowid` varchar(80) NOT NULL,"
						+ "  `chainid` varchar(20) NOT NULL,"
						+ "  `blkdiff` varchar(80) NOT NULL,"
						+ "  `mmr` varchar(80) NOT NULL,"
						+ "  `total` bigint NOT NULL,"
						+ "  `txbodyhash` varchar(80) NOT NULL,"
						+ "  `nonce` varchar(80) NOT NULL,"
						+ "  PRIMARY KEY(`id`),"
						+ "  FOREIGN KEY (`txpowid`) REFERENCES txpow(`txpowid`),"
						+ "  INDEX `idx_txpow_txpowid` (`txpowid`)"
						+ ")";

		//Run it..
		stmt.execute(tbl_txheader);

		//Create the TxPoW ID list (Transactions TxPoW ID list from TxBlock-TxPoW-TxBody-TxPowIDList)
		String tbl_txpowidlist = "CREATE TABLE IF NOT EXISTS `txpowidlist` ("
						+ "  `id` int NOT NULL AUTO_INCREMENT,"
						+ "  `txpowid` varchar(80) NOT NULL,"
						+ "  `txpowid_txn` varchar(80) NOT NULL,"
						+ "  PRIMARY KEY(`id`),"
						+ "  FOREIGN KEY (`txpowid`) REFERENCES txpow(`txpowid`),"
						+ "  INDEX `idx_txpowidlist_txpowid` (`txpowid`),"
						+ "  INDEX `idx_txpowidlist_txpowid_txn` (`txpowid_txn`),"
						+ "  CONSTRAINT `uidx_txpowidlist_txpowid_txpowid_txn` UNIQUE(`txpowid`, `txpowid_txn`)"
						+ ")";

		//Run it..
		stmt.execute(tbl_txpowidlist);

		//Create the TxPoW-Coin link
		String tbl_txpow_coin = "CREATE TABLE IF NOT EXISTS `txpow_coin` ("
						+ "  `id` int NOT NULL AUTO_INCREMENT,"
						+ "  `txpowid` varchar(80) NOT NULL,"
						+ "  `coinid` varchar(80) NOT NULL,"
						+ "  PRIMARY KEY(`id`),"
						+ "  FOREIGN KEY (`txpowid`) REFERENCES txpow(`txpowid`),"
						+ "  FOREIGN KEY (`coinid`) REFERENCES coin(`coinid`),"
						+ "  INDEX `idx_txpow_coin_txpowid` (`txpowid`),"
						+ "  INDEX `idx_txpow_coin_coinid` (`coinid`),"
						+ "  CONSTRAINT `uidx_txpow_coin_txpowid_coinid` UNIQUE(`txpowid`, `coinid`)"
						+ ")";

		//Run it..
		stmt.execute(tbl_txpow_coin);

		//Create the coin states table
		String coin_state = "CREATE TABLE IF NOT EXISTS `coin_state` ("
						+ "  `id` int NOT NULL AUTO_INCREMENT,"
						+ "  `coinid` varchar(80) NOT NULL,"
						+ "  `port` int NOT NULL,"
						+ "  `type` int NOT NULL,"
						+ "  `data` text NULL,"
						+ "  PRIMARY KEY (`id`),"
						+ "  FOREIGN KEY (`coinid`) REFERENCES coin(`coinid`),"
						+ "  INDEX `idx_coin_state_coinid` (`coinid`)"
						+ ")";

		//Run it..
		stmt.execute(coin_state);

		//Create the token table
		String token = "CREATE TABLE IF NOT EXISTS `token` ("
						+ "  `id` int NOT NULL AUTO_INCREMENT,"
						+ "  `tokenid` varchar(80) NOT NULL,"
						+ "  `coinid` varchar(80) NOT NULL,"
						+ "  `name` varchar(1000) NULL,"
						+ "  `description` varchar(10000) NULL,"
						+ "  `url` text NULL,"
						+ "  `ticker` varchar(80) NULL,"
						+ "  `webvalidate` varchar(1000) NULL,"
						+ "  `object` text NULL,"
						+ "  `total` bigint NOT NULL,"
						+ "  `totalamount` varchar(64) NOT NULL,"
						+ "  `decimals` int NOT NULL,"
						+ "  `scale` int NOT NULL,"
						+ "  `script` text NOT NULL,"
						+ "  `created` bigint NOT NULL,"
						+ "  PRIMARY KEY (`id`),"
						+ "  INDEX `idx_token_coinid` (`coinid`),"
						+ "  CONSTRAINT `uidx_token_tokenid` UNIQUE(`tokenid`)"
						+ ")";

		//Run it..
		stmt.execute(token);

		//All done..
		stmt.close();

		//Create some prepared statements..
		String insert 			= "INSERT IGNORE INTO syncblock ( txpowid, block, timemilli, syncdata ) VALUES ( ?, ? ,? ,? )";
		SQL_INSERT_SYNCBLOCK 	= mConnection.prepareStatement(insert);
		SQL_FIND_SYNCBLOCK_ID 	= mConnection.prepareStatement("SELECT syncdata FROM syncblock WHERE txpowid=?");
		SQL_FIND_SYNCBLOCK_NUM 	= mConnection.prepareStatement("SELECT syncdata FROM syncblock WHERE block=?");
		SQL_SELECT_RANGE		= mConnection.prepareStatement("SELECT syncdata FROM syncblock WHERE block>=? ORDER BY block ASC LIMIT "+MAX_SYNCBLOCKS);

		SQL_SELECT_LAST_BLOCK	= mConnection.prepareStatement("SELECT block FROM syncblock ORDER BY block ASC LIMIT 1");
		SQL_SELECT_FIRST_BLOCK	= mConnection.prepareStatement("SELECT block FROM syncblock ORDER BY block DESC LIMIT 1");

		SAVE_CASCADE = mConnection.prepareStatement("INSERT INTO cascadedata ( cascadetip, fulldata ) VALUES ( ?, ? )");
		LOAD_CASCADE = mConnection.prepareStatement("SELECT fulldata FROM cascadedata ORDER BY cascadetip ASC LIMIT 1");

		String insert_txpow = "INSERT IGNORE INTO txpow ( txpowid, block, superblock, size, burn, timemilli ) VALUES ( ?, ?, ?, ?, ?, ? )";
		SQL_INSERT_TXPOW 	= mConnection.prepareStatement(insert_txpow);
		SQL_DELETE_TXPOW 	= mConnection.prepareStatement("DELETE FROM txpow WHERE txpowid=?");
		SQL_SELECT_LAST_TXPOW 	= mConnection.prepareStatement("SELECT FROM txpow ORDER BY block DESC LIMIT 1");

		String insert_txheader = "INSERT IGNORE INTO txheader ( txpowid, chainid, blkdiff, mmr, total, txbodyhash, nonce ) VALUES ( ?, ?, ?, ?, ?, ?, ? )";
		SQL_INSERT_TXHEADER 	= mConnection.prepareStatement(insert_txheader);
		SQL_DELETE_TXHEADER 	= mConnection.prepareStatement("DELETE FROM txheader WHERE txpowid=?");

		SQL_INSERT_TXPOWIDLIST 	= mConnection.prepareStatement("INSERT IGNORE INTO txpowidlist ( txpowid, txpowid_txn ) VALUES ( ?, ? )");
		SQL_DELETE_TXPOWIDLIST 	= mConnection.prepareStatement("DELETE FROM txpowidlist WHERE txpowid=?");
		SQL_INSERT_TXPOWCOIN 	= mConnection.prepareStatement("INSERT IGNORE INTO txpow_coin ( txpowid, coinid ) VALUES ( ?, ? )");
		SQL_DELETE_TXPOWCOIN 	= mConnection.prepareStatement("DELETE FROM txpow_coin WHERE txpowid=?");

		String insert_coin = "INSERT INTO coin ( coinid, amount, address, miniaddress, tokenid, mmrentry, created ) VALUES ( ?, ?, ?, ?, ?, ?, ? ) AS new ON DUPLICATE KEY UPDATE mmrentry = new.mmrentry, created = new.created";
		SQL_INSERT_COIN 	= mConnection.prepareStatement(insert_coin);
		SQL_DELETE_COIN 	= mConnection.prepareStatement("DELETE FROM coin WHERE coinid=?");

		SQL_INSERT_COIN_STATE = mConnection.prepareStatement("INSERT IGNORE INTO coin_state ( coinid, port, type, data ) VALUES ( ?, ?, ?, ? )");
		SQL_DELETE_COIN_STATE	= mConnection.prepareStatement("DELETE FROM coin_state WHERE coinid=?");

		String insert_token = "INSERT IGNORE INTO token ( tokenid, coinid, name, description, url, ticker, webvalidate, object, total, totalamount, decimals, scale, script, created ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
		SQL_INSERT_TOKEN 	= mConnection.prepareStatement(insert_token);
	}

	public void shutdown() {
		try {
			if(!mConnection.isClosed()) {
				mConnection.close();
			}
		} catch (SQLException e) {
			MinimaLogger.log(e);
		}
	}

	public void wipeAll() throws SQLException {
		Statement stmt = mConnection.createStatement();
		stmt.execute("DROP TABLE syncblock");
		stmt.execute("DROP TABLE cascadedata");

		stmt.execute("DROP TABLE txheader");
		stmt.execute("DROP TABLE txpowidlist");
		stmt.execute("DROP TABLE coin_state");
		stmt.execute("DROP TABLE txpow_coin");

		stmt.execute("DROP TABLE txpow");
		stmt.execute("DROP TABLE coin");
		stmt.execute("DROP TABLE token");

		stmt.close();
	}

	public void deleteIndexes() throws SQLException {

		Statement stmt = mConnection.createStatement();

		String buffSql = " SELECT CONCAT('ALTER TABLE ', tbl_name, ' DROP INDEX ', GROUP_CONCAT(tbl_index SEPARATOR ', DROP INDEX '),';' ) AS sql_indexes "
			+ " FROM ( "
			+ " SELECT table_name AS tbl_name, "
			+ "        index_name AS tbl_index "
			+ " FROM information_schema.statistics "
			+ " WHERE NON_UNIQUE = 1 AND table_schema = '"+mDatabase+"' "
			+ " GROUP BY tbl_name, tbl_index) AS tmp "
			+ " GROUP BY tbl_name; ";

		//Run the query
		ResultSet rs = stmt.executeQuery(buffSql);

		ArrayList<String> res_queries = new ArrayList<>();
		//Multiple results
		while(rs.next())
			res_queries.add(rs.getString("sql_indexes"));

		//Exec all queries
		for (String res_query : res_queries) {
			MinimaLogger.log(res_query);
			stmt.execute(res_query);
		}

		stmt.close();
	}

	public void createIndexes() throws SQLException {
		Statement stmt = mConnection.createStatement();

		for (String sql_index : mIndexes) {
			MinimaLogger.log(sql_index);
			stmt.execute(sql_index);
		}

		stmt.close();
	}

	public void saveIndexes() throws SQLException {

		Statement stmt = mConnection.createStatement();

		String buffSql = " SELECT CONCAT('ALTER TABLE ', tbl_name, ' ADD INDEX ', GROUP_CONCAT(CONCAT(tbl_index, '(', tbl_cols, ')') SEPARATOR ', ADD INDEX '),';' ) AS sql_indexes "
			+ " FROM ( SELECT table_name AS tbl_name, "
			+ "	 		index_name AS tbl_index, "
			+ "	 		GROUP_CONCAT(column_name ORDER BY seq_in_index) AS tbl_cols "
			+ "	   FROM information_schema.statistics "
			+ "	   WHERE NON_UNIQUE = 1 AND table_schema = '"+mDatabase+"' "
			+ "	   GROUP BY tbl_name, tbl_index) AS tmp "
			+ " GROUP BY tbl_name; ";

		//Run the query
		ResultSet rs = stmt.executeQuery(buffSql);

		String res_query;
		//Multiple results
		while(rs.next()) {

			//Get the block
			res_query = rs.getString("sql_indexes");

			mIndexes.add(res_query);
		}

		stmt.close();
	}

	public boolean saveCascade(Cascade zCascade) throws SQLException {

		//get the MiniData version..
		MiniData cascdata = MiniData.getMiniDataVersion(zCascade);

		//Get the Query ready
		SAVE_CASCADE.clearParameters();

		//Set main params
		SAVE_CASCADE.setLong(1, zCascade.getTip().getTxPoW().getBlockNumber().getAsLong());

		//And finally the actual bytes
		SAVE_CASCADE.setBytes(2, cascdata.getBytes());

		//Do it.
		SAVE_CASCADE.execute();

		return true;
	}


	public Cascade loadCascade() throws SQLException {

		LOAD_CASCADE.clearParameters();

		ResultSet rs = LOAD_CASCADE.executeQuery();

		//Is there a valid result.. ?
		if(rs.next()) {

			//Get the details..
			byte[] syncdata 	= rs.getBytes("fulldata");

			//Create MiniData version
			MiniData minisync = new MiniData(syncdata);

			//Convert
			Cascade casc = Cascade.convertMiniDataVersion(minisync);

			return casc;
		}

		return null;
	}

	public synchronized boolean saveBlock(TxBlock zBlock, boolean zSynced) {

		boolean needClear = zSynced;

		try {

			if (zSynced) {

				long last_block = loadLastBlock();
				long last_txpow = loadLastTxPoW();

				if (last_block = last_txpow)
					needClear = false;

				if (needClear)
					//Clear unsynced data
					clearUnsynced(zBlock.getTxPoW().getTxPoWID());

				//get the MiniData version..
				MiniData syncdata = MiniData.getMiniDataVersion(zBlock);

				//Get the Query ready
				SQL_INSERT_SYNCBLOCK.clearParameters();

				//Set main params
				SQL_INSERT_SYNCBLOCK.setString(1, zBlock.getTxPoW().getTxPoWID());
				SQL_INSERT_SYNCBLOCK.setLong(2, zBlock.getTxPoW().getBlockNumber().getAsLong());
				SQL_INSERT_SYNCBLOCK.setLong(3, System.currentTimeMillis());

				//And finally the actual bytes
				SQL_INSERT_SYNCBLOCK.setBytes(4, syncdata.getBytes());

				//Do it.
				SQL_INSERT_SYNCBLOCK.execute();
			}

//			MinimaLogger.log("MYSQL stored synvblock "+zBlock.getTxPoW().getBlockNumber());

			// Buffer TxPoW
			TxPoW blockTxPoW = zBlock.getTxPoW();

			//Get the Query ready
			SQL_INSERT_TXPOW.clearParameters();

			// Store TxPoW
			SQL_INSERT_TXPOW.setString(1, blockTxPoW.getTxPoWID());
			SQL_INSERT_TXPOW.setLong(2, blockTxPoW.getBlockNumber().getAsLong());
			SQL_INSERT_TXPOW.setInt(3, blockTxPoW.getSuperLevel());
			SQL_INSERT_TXPOW.setLong(4, blockTxPoW.getSizeinBytes());
			SQL_INSERT_TXPOW.setString(5, blockTxPoW.getBurn().toString());
			SQL_INSERT_TXPOW.setLong(6, blockTxPoW.getTimeMilli().getAsLong());

			//Do it.
			SQL_INSERT_TXPOW.execute();

			//Get the Query ready
			SQL_INSERT_TXHEADER.clearParameters();

			// Store TxPoW TxHeader
			SQL_INSERT_TXHEADER.setString(1, blockTxPoW.getTxPoWID());
			SQL_INSERT_TXHEADER.setString(2, blockTxPoW.getChainID().to0xString());
			SQL_INSERT_TXHEADER.setString(3, blockTxPoW.getBlockDifficulty().to0xString());
			SQL_INSERT_TXHEADER.setString(4, blockTxPoW.getMMRRoot().to0xString());
			SQL_INSERT_TXHEADER.setLong(5, blockTxPoW.getMMRTotal().getAsLong());
			SQL_INSERT_TXHEADER.setString(6, blockTxPoW.getTxHeader().getBodyHash().to0xString());
			SQL_INSERT_TXHEADER.setString(7, blockTxPoW.getNonce().toString());

			//Do it.
			SQL_INSERT_TXHEADER.execute();

			// Store TxPoW ID List (Transactions)
			for (MiniData txpow_id : zBlock.getTxPoW().getBlockTransactions()) {
				//Get the Query ready
				SQL_INSERT_TXPOWIDLIST.clearParameters();

				SQL_INSERT_TXPOWIDLIST.setString(1, blockTxPoW.getTxPoWID());
				SQL_INSERT_TXPOWIDLIST.setString(2, txpow_id.to0xString());

				//Do it.
				SQL_INSERT_TXPOWIDLIST.execute();
			}

			// Save coins from a block
			// Created coins
			// Set main params
			ArrayList<Coin> outputs = zBlock.getOutputCoins();

			for(Coin cc : outputs) {

				// Store coin
				saveCoin(buffCoin);

				// Store coin state
				if (buffCoin.getState().size() > 0)
					saveCoinState(buffCoin.getCoinID().to0xString(), buffCoin.getState());

				//Get the Query ready
				SQL_INSERT_TXPOWCOIN.clearParameters();

				//Store link TxPoW-Coin
				SQL_INSERT_TXPOWCOIN.setString(1, blockTxPoW.getTxPoWID());
				SQL_INSERT_TXPOWCOIN.setString(2, cc.getCoinID().to0xString());

				//Do it.
				SQL_INSERT_TXPOWCOIN.execute();

			}

			// Spent coins

			ArrayList<CoinProof> inputs = zBlock.getInputCoinProofs();
			//Set main params
			for(CoinProof incoin : inputs) {

				Coin buffCoin = incoin.getCoin();

				// Store coin
				saveCoin(buffCoin);

				// Store coin state
				if (buffCoin.getState().size() > 0)
					saveCoinState(buffCoin.getCoinID().to0xString(), buffCoin.getState());

				// Is coin have token
				if (buffCoin.getToken() != null)
					saveToken(buffCoin.getToken());

			}

			//MinimaLogger.log("Block "+zBlock.getTxPoW().getBlockNumber()+" have a body: "+zBody.toJSON());

			return true;

		} catch (SQLException e) {
			MinimaLogger.log(e);
		}

		return false;
	}

	public synchronized boolean saveCoin(Coin zCoin) {
		try {
			//Get the Query ready
			SQL_INSERT_COIN.clearParameters();

			Coin buffCoin = incoin.getCoin();

			SQL_INSERT_COIN.setString(1, zCoin.getCoinID().to0xString());
			SQL_INSERT_COIN.setString(2, zCoin.getAmount().toString());
			SQL_INSERT_COIN.setString(3, zCoin.getAddress().to0xString());
			SQL_INSERT_COIN.setString(4, Address.makeMinimaAddress(zCoin.getAddress()));
			SQL_INSERT_COIN.setString(5, zCoin.getTokenID().to0xString());
			SQL_INSERT_COIN.setString(6, zCoin.getMMREntryNumber().toString());
			SQL_INSERT_COIN.setLong(7, zCoin.getBlockCreated().getAsLong());

			//Do it.
			SQL_INSERT_COIN.execute();
		} catch (SQLException e) {
			MinimaLogger.log(e);
		}
	}

	public synchronized boolean saveCoinState(String zCoinID, ArrayList<StateVariable> zCoinState) {
		try {
			for (StateVariable coin_state : zCoinState) {
				//Get the Query ready
				SQL_INSERT_COIN_STATE.clearParameters();

				SQL_INSERT_COIN_STATE.setString(1, zCoinID);
				SQL_INSERT_COIN_STATE.setInt(2, coin_state.getPort());
				SQL_INSERT_COIN_STATE.setInt(3, coin_state.getType().getValue());
				SQL_INSERT_COIN_STATE.setString(4, coin_state.getData().toString());

				//Do it.
				SQL_INSERT_COIN_STATE.execute();
			}
		} catch (SQLException e) {
			MinimaLogger.log(e);
		}
	}

	public synchronized boolean saveToken(Token zToken) {
		try {
			//Get the Query ready
			SQL_INSERT_TOKEN.clearParameters();

			SQL_INSERT_TOKEN.setString(1, zToken.getTokenID().to0xString());
			SQL_INSERT_TOKEN.setString(2, zToken.getCoinID().to0xString());
			//Is name a JSON
			if(zToken.getName().toString().trim().startsWith("{")) {
				//Get the JSON
				JSONObject jsonname = null;
				try {
					jsonname = (JSONObject) new JSONParser().parse(zToken.getName().toString());
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (jsonname.containsKey("name"))
					SQL_INSERT_TOKEN.setString(3, jsonname.get("name").toString());
				else
					SQL_INSERT_TOKEN.setNull(3, java.sql.Types.VARCHAR);
				if (jsonname.containsKey("description"))
					SQL_INSERT_TOKEN.setString(4, jsonname.get("description").toString());
				else
					SQL_INSERT_TOKEN.setNull(4, java.sql.Types.VARCHAR);
				if (jsonname.containsKey("url"))
					SQL_INSERT_TOKEN.setString(5, jsonname.get("url").toString());
				else
					SQL_INSERT_TOKEN.setNull(5, java.sql.Types.VARCHAR);
				if (jsonname.containsKey("ticker"))
					SQL_INSERT_TOKEN.setString(6, jsonname.get("ticker").toString());
				else
					SQL_INSERT_TOKEN.setNull(6, java.sql.Types.VARCHAR);
				if (jsonname.containsKey("webvalidate"))
					SQL_INSERT_TOKEN.setString(7, jsonname.get("webvalidate").toString());
				else
					SQL_INSERT_TOKEN.setNull(7, java.sql.Types.VARCHAR);

				SQL_INSERT_TOKEN.setString(8, zToken.getName().toString());

			} else {
				SQL_INSERT_TOKEN.setString(3, zToken.getName().toString());
			}
			SQL_INSERT_TOKEN.setLong(9, zToken.getTotalTokens().getAsLong());
			SQL_INSERT_TOKEN.setLong(10, zToken.getAmount().getAsLong());
			SQL_INSERT_TOKEN.setInt(11, zToken.getDecimalPlaces().getAsInt());
			SQL_INSERT_TOKEN.setInt(12, zToken.getScale().getAsInt());
			SQL_INSERT_TOKEN.setString(13, zToken.getTokenScript().toString());
			SQL_INSERT_TOKEN.setLong(14, zToken.getCreated().getAsLong());

			//Do it.
			SQL_INSERT_TOKEN.execute();
		} catch (SQLException e) {
			MinimaLogger.log(e);
		}
	}

	public synchronized boolean clearUnsynced(String zTxPoWID) {
		try {
			SQL_DELETE_TXPOW.clearParameters();
			SQL_DELETE_TXPOW.setString(1, zTxPoWID);
			SQL_DELETE_TXPOW.execute();

			SQL_DELETE_TXHEADER.clearParameters();
			SQL_DELETE_TXHEADER.setString(1, zTxPoWID);
			SQL_DELETE_TXHEADER.execute();

			SQL_DELETE_TXPOWIDLIST.clearParameters();
			SQL_DELETE_TXPOWIDLIST.setString(1, zTxPoWID);
			SQL_DELETE_TXPOWIDLIST.execute();

			SQL_DELETE_TXPOWCOIN.clearParameters();
			SQL_DELETE_TXPOWCOIN.setString(1, zTxPoWID);
			SQL_DELETE_TXPOWCOIN.execute();

			SQL_DELETE_COIN.clearParameters();
			SQL_DELETE_COIN.setString(1, zTxPoWID);
			SQL_DELETE_COIN.execute();

			SQL_DELETE_COIN_STATE.clearParameters();
			SQL_DELETE_COIN_STATE.setString(1, zTxPoWID);
			SQL_DELETE_COIN_STATE.execute();
		} catch (SQLException e) {
			MinimaLogger.log(e);
		}
	}

	public synchronized TxBlock loadBlockFromID(String zTxPoWID) {

		try {

			//Set search params
			SQL_FIND_SYNCBLOCK_ID.clearParameters();
			SQL_FIND_SYNCBLOCK_ID.setString(1, zTxPoWID);

			//Run the query
			ResultSet rs = SQL_FIND_SYNCBLOCK_ID.executeQuery();

			//Is there a valid result.. ?
			if(rs.next()) {

				//Get the details..
				byte[] syncdata 	= rs.getBytes("syncdata");

				//Create MiniData version
				MiniData minisync = new MiniData(syncdata);

				//Convert
				TxBlock sb = TxBlock.convertMiniDataVersion(minisync);

				return sb;
			}

		} catch (SQLException e) {
			MinimaLogger.log(e);
		}

		return null;
	}

	public synchronized TxBlock loadBlockFromNum(long zBlocknumber) {

		try {

			//Set search params
			SQL_FIND_SYNCBLOCK_NUM.clearParameters();
			SQL_FIND_SYNCBLOCK_NUM.setLong(1, zBlocknumber);

			//Run the query
			ResultSet rs = SQL_FIND_SYNCBLOCK_NUM.executeQuery();

			//Is there a valid result.. ?
			if(rs.next()) {

				//Get the details..
				byte[] syncdata 	= rs.getBytes("syncdata");

				//Create MiniData version
				MiniData minisync = new MiniData(syncdata);

				//Convert
				TxBlock sb = TxBlock.convertMiniDataVersion(minisync);

				return sb;
			}

		} catch (SQLException e) {
			MinimaLogger.log(e);
		}

		return null;
	}

	public synchronized long loadFirstBlock() {

		try {

			//Set search params
			SQL_SELECT_FIRST_BLOCK.clearParameters();

			//Run the query
			ResultSet rs = SQL_SELECT_FIRST_BLOCK.executeQuery();

			//Is there a valid result.. ?
			if(rs.next()) {

				//Get the block
				long block = rs.getLong("block");

				return block;
			}

		} catch (SQLException e) {
			MinimaLogger.log(e);
		}

		return -1;
	}

	public synchronized long loadLastBlock() {

		try {

			//Set search params
			SQL_SELECT_LAST_BLOCK.clearParameters();

			//Run the query
			ResultSet rs = SQL_SELECT_LAST_BLOCK.executeQuery();

			//Is there a valid result.. ?
			if(rs.next()) {

				//Get the block
				long block = rs.getLong("block");

				return block;
			}

		} catch (SQLException e) {
			MinimaLogger.log(e);
		}

		return -1;
	}

	public synchronized long loadLastTxPoW() {

		try {

			//Set search params
			SQL_SELECT_LAST_TXPOW.clearParameters();

			//Run the query
			ResultSet rs = SQL_SELECT_LAST_TXPOW.executeQuery();

			//Is there a valid result.. ?
			if(rs.next()) {

				//Get the block
				long block = rs.getLong("block");

				return block;
			}

		} catch (SQLException e) {
			MinimaLogger.log(e);
		}

		return -1;
	}

	public synchronized ArrayList<TxBlock> loadBlockRange(MiniNumber zStartBlock) {

		ArrayList<TxBlock> blocks = new ArrayList<>();

		try {

			//Set Search params
			SQL_SELECT_RANGE.clearParameters();
			SQL_SELECT_RANGE.setLong(1,zStartBlock.getAsLong());

			//Run the query
			ResultSet rs = SQL_SELECT_RANGE.executeQuery();

			//Multiple results
			while(rs.next()) {

				//Get the details..
				byte[] syncdata 	= rs.getBytes("syncdata");

				//Create MiniData version
				MiniData minisync = new MiniData(syncdata);

				//Convert
				TxBlock sb = TxBlock.convertMiniDataVersion(minisync);

				//Add to our list
				blocks.add(sb);
			}

		} catch (SQLException e) {
			MinimaLogger.log(e);
		}

		return blocks;
	}

//	/**
//	 * Non Synchronized version of LoadBlockRange
//	 * @throws SQLException
//	 */
//	public ArrayList<TxBlock> loadBlockRangeNoSync(MiniNumber zStartBlock) throws SQLException {
//
//		ArrayList<TxBlock> blocks = new ArrayList<>();
//
//		//Set Search params
//		SQL_SELECT_RANGE.clearParameters();
//		SQL_SELECT_RANGE.setLong(1,zStartBlock.getAsLong());
//
//		//Run the query
//		ResultSet rs = SQL_SELECT_RANGE.executeQuery();
//
//		//Multiple results
//		while(rs.next()) {
//
//			//Get the details..
//			byte[] syncdata 	= rs.getBytes("syncdata");
//
//			//Create MiniData version
//			MiniData minisync = new MiniData(syncdata);
//
//			//Convert
//			TxBlock sb = TxBlock.convertMiniDataVersion(minisync);
//
//			//Add to our list
//			blocks.add(sb);
//		}
//
//		return blocks;
//	}

	public static void main(String[] zArgs) throws SQLException {

		//Load the required classes
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}

		MySQLConnect mysql = new MySQLConnect("localhost:3306", "mydatabase", "myuser", "myuser");
		mysql.init();

		//Add some TxPoW..
		TxPoW txp = new TxPoW();
		txp.setBlockNumber(MiniNumber.ZERO);
		txp.calculateTXPOWID();
		TxBlock txblk = new TxBlock(txp);

		txp = new TxPoW();
		txp.setBlockNumber(MiniNumber.ONE);
		txp.calculateTXPOWID();
		txblk = new TxBlock(txp);

		mysql.saveBlock(txblk, true);

		//Now search for the top block..
		long firstblock = mysql.loadFirstBlock();
		long lastblock 	= mysql.loadLastBlock();

		System.out.println("FIRST : "+firstblock);
		System.out.println("LAST  : "+lastblock);

//		String txpid = "0x0003E914FBF1C04C9E1B52E37A171CA870E5310B33E50B9DFA9DF0C044A24150";
//		TxBlock block = mysql.loadBlockFromID(txpid);

//		TxBlock block = mysql.loadBlockFromNum(3);
//		MinimaLogger.log(block.getTxPoW().toJSON().toString());

		ArrayList<TxBlock> blocks = mysql.loadBlockRange(MiniNumber.ZERO);
		MinimaLogger.log("FOUND : "+blocks.size());
		for(TxBlock block : blocks) {
			MinimaLogger.log(block.getTxPoW().getBlockNumber().toString());
		}

		mysql.shutdown();
	}

}
