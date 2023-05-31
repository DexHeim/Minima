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
	PreparedStatement SQL_INSERT_TOKEN				= null;

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
						+ "  `superblock` int NOT NULL,"
						+ "  `size` bigint NOT NULL,"
						+ "  `burn` int DEFAULT 0 NOT NULL,"
						+ "  PRIMARY KEY(`id`),"
						+ "  CONSTRAINT `uidx_txpow_txpowid` UNIQUE(`txpowid`)"
						+ ")";

		//Run it..
		stmt.execute(tbl_txpow);

		//Create the TxHeader table (TxHeader model from TxBlock-TxPoW-TxHeader)
		String tbl_txheader = "CREATE TABLE IF NOT EXISTS `txheader` ("
						+ "  `id` int NOT NULL AUTO_INCREMENT,"
						+ "  `txpowid` varchar(80) NOT NULL,"
						+ "  `chainid` varchar(20) NOT NULL,"
						+ "  `block` bigint NOT NULL,"
						+ "  `blkdiff` varchar(80) NOT NULL,"
						+ "  `mmr` varchar(80) NOT NULL,"
						+ "  `total` bigint NOT NULL,"
						+ "  `txbodyhash` varchar(80) NOT NULL,"
						+ "  `nonce` varchar(80) NOT NULL,"
						+ "  `timemilli` bigint NOT NULL,"
						+ "  PRIMARY KEY(`id`),"
						+ "  INDEX `idx_txheader_block` (`block`),"
						+ "  CONSTRAINT `uidx_txheader_txpowid` UNIQUE(`txpowid`)"
						+ ")";

		//Run it..
		stmt.execute(tbl_txheader);

		//Create the TxPoW ID list (Transactions TxPoW ID list from TxBlock-TxPoW-TxBody-TxPowIDList)
		String tbl_txpowidlist = "CREATE TABLE IF NOT EXISTS `txpowidlist` ("
						+ "  `id` int NOT NULL AUTO_INCREMENT,"
						+ "  `txpowid` varchar(80) NOT NULL,"
						+ "  `txpowid_txn` varchar(80) NOT NULL,"
						+ "  PRIMARY KEY(`id`),"
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
						+ "  INDEX `idx_txpow_coin_txpowid` (`txpowid`),"
						+ "  INDEX `idx_txpow_coin_coinid` (`coinid`),"
						+ "  CONSTRAINT `uidx_txpow_coin_txpowid_coinid` UNIQUE(`txpowid`, `coinid`)"
						+ ")";

		//Run it..
		stmt.execute(tbl_txpow_coin);

		//Create the coins table
		String coins = "CREATE TABLE IF NOT EXISTS `coins` ("
						+ "  `id` int NOT NULL AUTO_INCREMENT,"
						+ "  `coinid` varchar(80) NOT NULL,"
						+ "  `amount` varchar(60) NOT NULL,"
						+ "  `address` varchar(80) NOT NULL,"
						+ "  `miniaddress` varchar(80) NOT NULL,"
						+ "  `tokenid` varchar(80) NOT NULL,"
						+ "  `mmrentry` varchar(20) NOT NULL,"
						+ "  `created` bigint NOT NULL,"
						+ "  PRIMARY KEY (`id`),"
						+ "  INDEX `idx_coins_address` (`address`),"
						+ "  INDEX `idx_coins_miniaddress` (`miniaddress`),"
						+ "  CONSTRAINT `uidx_coins_coinid` UNIQUE(`coinid`)"
						+ ")";

		//Run it..
		stmt.execute(coins);

		//Create the tokens table
		String tokens = "CREATE TABLE IF NOT EXISTS `tokens` ("
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
						+ "  `totalamount` varchar(60) NOT NULL,"
						+ "  `decimals` int NOT NULL,"
						+ "  `scale` int NOT NULL,"
						+ "  `script` text NOT NULL,"
						+ "  `created` bigint NOT NULL,"
						+ "  PRIMARY KEY (`id`),"
						+ "  INDEX `idx_tokens_coinid` (`coinid`),"
						+ "  CONSTRAINT `uidx_tokens_tokenid` UNIQUE(`tokenid`)"
						+ ")";

		//Run it..
		stmt.execute(tokens);

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

		String insert_txpow = "INSERT INTO txpow ( txpowid, superblock, size, burn ) VALUES ( ?, ?, ?, ? )";
		SQL_INSERT_TXPOW 	= mConnection.prepareStatement(insert_txpow);

		String insert_txheader = "INSERT INTO txheader ( txpowid, chainid, block, blkdiff, mmr, total, txbodyhash, nonce, timemilli ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ? )";
		SQL_INSERT_TXHEADER 	= mConnection.prepareStatement(insert_txheader);

		SQL_INSERT_TXPOWIDLIST 	= mConnection.prepareStatement("INSERT INTO txpowidlist ( txpowid, txpowid_txn ) VALUES ( ?, ? )");
		SQL_INSERT_TXPOWCOIN 	= mConnection.prepareStatement("INSERT INTO txpow_coin ( txpowid, coinid ) VALUES ( ?, ? )");

		String insert_coin = "INSERT INTO coins ( coinid, amount, address, miniaddress, tokenid, mmrentry, created ) VALUES ( ?, ?, ?, ?, ?, ?, ? ) AS new ON DUPLICATE KEY UPDATE mmrentry = new.mmrentry, created = new.created";
		SQL_INSERT_COIN 	= mConnection.prepareStatement(insert_coin);

		String insert_token = "INSERT INTO tokens ( tokenid, coinid, name, description, url, ticker, webvalidate, object, total, totalamount, decimals, scale, script, created ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) ON DUPLICATE KEY UPDATE id=id";
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
		stmt.execute("DROP TABLE txpow");
		stmt.execute("DROP TABLE txheader");
		stmt.execute("DROP TABLE txpowidlist");
		stmt.execute("DROP TABLE txpow_coin");
		stmt.execute("DROP TABLE tokens");

		stmt.close();
	}

	public void deleteIndexes() throws SQLException {

		Statement stmt = mConnection.createStatement();

		String buffSql = " SET SESSION group_concat_max_len=10240;"
			+ " SELECT CONCAT('ALTER TABLE ', `Table`, ' DROP INDEX ', GROUP_CONCAT(`Index` SEPARATOR ', DROP INDEX '),';' ) AS sql_indexes"
			+ " FROM ("
			+ " SELECT table_name AS `Table`,"
			+ "        index_name AS `Index`"
			+ " FROM information_schema.statistics"
			+ " WHERE NON_UNIQUE = 1 AND table_schema = '"+mDatabase+"'"
			+ " GROUP BY `Table`, `Index`) AS tmp"
			+ " GROUP BY `Table`;";

		MinimaLogger.log(buffSql);

		//Run the query
		ResultSet rs = stmt.executeQuery(buffSql);

		//Multiple results
		while(rs.next()) {
			//Get the block
			String res_query = rs.getString("sql_indexes");

			stmt.execute(res_query);
		}
		stmt.close();
	}

	public void createIndexes() throws SQLException {
		Statement stmt = mConnection.createStatement();

		for (String sql_index : mIndexes)
			stmt.execute(sql_index);

		stmt.close();
	}

	public void saveIndexes() throws SQLException {

		Statement stmt = mConnection.createStatement();

		String buffSql = " SET SESSION group_concat_max_len=10240;"
			+ " SELECT CONCAT('ALTER TABLE ', `Table`, ' ADD INDEX ', GROUP_CONCAT(CONCAT(`Index`, '(', `Columns`, ')') SEPARATOR ', ADD INDEX '),';' ) AS sql_indexes"
			+ " FROM ("
			+ " SELECT table_name AS `Table`,"
			+ "        index_name AS `Index`,"
			+ " 			 GROUP_CONCAT(column_name ORDER BY seq_in_index) AS `Columns`"
			+ " FROM information_schema.statistics"
			+ " WHERE NON_UNIQUE = 1 AND table_schema = '"+mDatabase+"'"
			+ " GROUP BY `Table`, `Index`) AS tmp"
			+ " GROUP BY `Table`;";

		MinimaLogger.log(buffSql);

		//Run the query
		ResultSet rs = stmt.executeQuery(buffSql);

		//Multiple results
		while(rs.next()) {

			//Get the block
			String res_query = rs.getString("sql_indexes");

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

	public synchronized boolean saveBlock(TxBlock zBlock) {
		try {

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

//			MinimaLogger.log("MYSQL stored synvblock "+zBlock.getTxPoW().getBlockNumber());

			// Buffer TxPoW
			TxPoW blockTxPoW = zBlock.getTxPoW();

			// Store TxPoW
			SQL_INSERT_TXPOW.setString(1, blockTxPoW.getTxPoWID());
			SQL_INSERT_TXPOW.setInt(2, blockTxPoW.getSuperLevel());
			SQL_INSERT_TXPOW.setLong(3, blockTxPoW.getSizeinBytes());
			SQL_INSERT_TXPOW.setInt(4, blockTxPoW.getBurn().getAsInt());

			//Do it.
			SQL_INSERT_TXPOW.execute();

			// Store TxPoW TxHeader
			SQL_INSERT_TXHEADER.setString(1, blockTxPoW.getTxPoWID());
			SQL_INSERT_TXHEADER.setString(2, blockTxPoW.getChainID().to0xString());
			SQL_INSERT_TXHEADER.setLong(3, blockTxPoW.getBlockNumber().getAsLong());
			SQL_INSERT_TXHEADER.setString(4, blockTxPoW.getBlockDifficulty().to0xString());
			SQL_INSERT_TXHEADER.setString(5, blockTxPoW.getMMRRoot().to0xString());
			SQL_INSERT_TXHEADER.setLong(6, blockTxPoW.getMMRTotal().getAsLong());
			SQL_INSERT_TXHEADER.setString(7, blockTxPoW.getTxHeader().getBodyHash().to0xString());
			SQL_INSERT_TXHEADER.setString(8, blockTxPoW.getNonce().toString());
			SQL_INSERT_TXHEADER.setLong(9, blockTxPoW.getTimeMilli().getAsLong());

			//Do it.
			SQL_INSERT_TXHEADER.execute();

			// Store TxPoW ID List (Transactions)
			for (MiniData txpow_id : zBlock.getTxPoW().getBlockTransactions()) {
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
				SQL_INSERT_COIN.clearParameters();

				SQL_INSERT_COIN.setString(1, cc.getCoinID().to0xString());
				SQL_INSERT_COIN.setString(2, cc.getAmount().toString());
				SQL_INSERT_COIN.setString(3, cc.getAddress().to0xString());
				SQL_INSERT_COIN.setString(4, Address.makeMinimaAddress(cc.getAddress()));
				SQL_INSERT_COIN.setString(5, cc.getTokenID().to0xString());
				SQL_INSERT_COIN.setString(6, cc.getMMREntryNumber().toString());
				SQL_INSERT_COIN.setLong(7, cc.getBlockCreated().getAsLong());

				//Do it.
				SQL_INSERT_COIN.execute();

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
				//Get the Query ready
				SQL_INSERT_COIN.clearParameters();

				Coin buffCoin = incoin.getCoin();

				SQL_INSERT_COIN.setString(1, buffCoin.getCoinID().to0xString());
				SQL_INSERT_COIN.setString(2, buffCoin.getAmount().toString());
				SQL_INSERT_COIN.setString(3, buffCoin.getAddress().to0xString());
				SQL_INSERT_COIN.setString(4, Address.makeMinimaAddress(buffCoin.getAddress()));
				SQL_INSERT_COIN.setString(5, buffCoin.getTokenID().to0xString());
				SQL_INSERT_COIN.setString(6, buffCoin.getMMREntryNumber().toString());
				SQL_INSERT_COIN.setLong(7, buffCoin.getBlockCreated().getAsLong());

				//Do it.
				SQL_INSERT_COIN.execute();

				//Is coin have token
				if (buffCoin.getToken() != null) {
					Token buffToken = buffCoin.getToken();

					SQL_INSERT_TOKEN.setString(1, buffToken.getTokenID().to0xString());
					SQL_INSERT_TOKEN.setString(2, buffToken.getCoinID().to0xString());
					//Is name a JSON
					if(buffToken.getName().toString().trim().startsWith("{")) {
						//Get the JSON
						JSONObject jsonname = null;
						try {
							jsonname = (JSONObject) new JSONParser().parse(buffToken.getName().toString());
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

						SQL_INSERT_TOKEN.setString(8, buffToken.getName().toString());

					} else {
						SQL_INSERT_TOKEN.setString(3, buffToken.getName().toString());
					}
					SQL_INSERT_TOKEN.setLong(9, buffToken.getTotalTokens().getAsLong());
					SQL_INSERT_TOKEN.setLong(10, buffToken.getAmount().getAsLong());
					SQL_INSERT_TOKEN.setInt(11, buffToken.getDecimalPlaces().getAsInt());
					SQL_INSERT_TOKEN.setInt(12, buffToken.getScale().getAsInt());
					SQL_INSERT_TOKEN.setString(13, buffToken.getTokenScript().toString());
					SQL_INSERT_TOKEN.setLong(14, buffToken.getCreated().getAsLong());

					//Do it.
					SQL_INSERT_TOKEN.execute();
				}
			}

			//MinimaLogger.log("Block "+zBlock.getTxPoW().getBlockNumber()+" have a body: "+zBody.toJSON());

			return true;

		} catch (SQLException e) {
			MinimaLogger.log(e);
		}

		return false;
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

		mysql.saveBlock(txblk);

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
