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
import org.minima.objects.Address;

import org.minima.database.MinimaDB;
import org.minima.database.txpowdb.sql.TxPoWList;
import org.minima.objects.Transaction;

import org.minima.database.mmr.MMR;
import org.minima.database.mmr.MMREntry;
import org.minima.database.mmr.MMREntryNumber;
import org.minima.database.mmr.MMRProof;

public class MySQLConnect {

	public static final int MAX_SYNCBLOCKS = 250;

	String mMySQLHost;
	String mDatabase;
	String mUsername;
	String mPassword;

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

	PreparedStatement SQL_INSERT_COINS		= null;

	PreparedStatement SQL_INSERT_TXP_TXN				= null;
	PreparedStatement SQL_INSERT_TRANSACTION		= null;

	public MySQLConnect(String zHost, String zDatabase, String zUsername, String zPassword) {
		mMySQLHost 	= zHost;
		mDatabase	= zDatabase;
		mUsername	= zUsername;
		mPassword	= zPassword;
	}

	public void init() throws SQLException {
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


		//Create the coins table
		String coins = "CREATE TABLE IF NOT EXISTS `coins` ("
						+ "  `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
						+ "  `block` BIGINT NOT NULL,"
						+ "  `coinid` varchar(80) NOT NULL UNIQUE,"
						+ "  `amount` varchar(80) NULL,"
						+ "  `address` varchar(80) NOT NULL,"
						+ "  `miniaddress` varchar(80) NOT NULL,"
						+ "  `tokenid` varchar(80) NOT NULL,"
						+ "  `mmrentry` varchar(20) NOT NULL,"
						+ "  `created` varchar(20) NOT NULL,"
						+ "  `block_sended` BIGINT DEFAULT 0 NOT NULL"
						+ ")";

		//Run it..
		stmt.execute(coins);

		//Create the coin proofs table
		String coin_proofs = "CREATE TABLE IF NOT EXISTS `coin_proofs` ("
						+ "  `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
						+ "  `coinid` varchar(80) NOT NULL UNIQUE,"
						+ "  `blocktime` BIGINT NOT NULL,"
						+ "  `left` varchar(80) NOT NULL,"
						+ "  `data` varchar(80) NOT NULL,"
						+ "  `value` varchar(80) NOT NULL,"
						+ "  `prooflength` INT NOT NULL"
						+ ")";

		//Run it..
		stmt.execute(coins);

		//Create TxPoW ID link (CALCULATED) Transaction ID
		String txp_txn = "CREATE TABLE IF NOT EXISTS `txp_txn` ("
						+ "  `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
						+ "  `block` BIGINT NOT NULL,"
						+ "  `txpowid` varchar(80) NOT NULL,"
						+ "  `txnid` varchar(80) NOT NULL"
						+ ")";

		//Run it..
		stmt.execute(txp_txn);

		//Create some fast indexes and uniqie link txpowid-transactionid..
		String txp_txn_uindex = "CREATE UNIQUE INDEX IF NOT EXISTS txn_txp_uindex ON `txp_txn` (`txpowid`, `transactionid`)";

		//Run it..
		stmt.execute(txp_txn_uindex);

		//Create (CALCULATED) Transactions IN/OUT
		//This table - grouping coins in transactions
		String transactions = "CREATE TABLE IF NOT EXISTS `transactions` ("
						+ "  `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
						+ "  `txnid` varchar(80) NOT NULL,"
						+ "  `coinid` varchar(80) NOT NULL,"
						//Type of Input(0)/Output(1)
						+ "  `type` BIT NOT NULL"
						+ ")";

		//Run it..
		stmt.execute(txn_txp);

		//Create some fast indexes and uniqie link txpowid-transactionid..
		String transactions_uindex = "CREATE UNIQUE INDEX IF NOT EXISTS transactions_uindex ON `transactions` (`txnid`, `coinid`)";

		//Run it..
		stmt.execute(transactions_uindex);

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

		String insert_coins 			= "INSERT INTO coins ( block, coinid, amount, address, miniaddress, tokenid, mmrentry, created ) VALUES ( ?, ? ,? ,? ,? ,? ,? ,? ) AS new ON DUPLICATE KEY UPDATE mmrentry = new.mmrentry, created = new.created, block_sended = new.block";
		SQL_INSERT_COINS 	= mConnection.prepareStatement(insert_coins);

		SQL_INSERT_TXP_TXN = mConnection.prepareStatement("INSERT INTO txp_txn ( block, txpowid, txnid ) VALUES ( ?, ?, ? )");
		SQL_INSERT_TRANSACTION = mConnection.prepareStatement("INSERT INTO transactions ( txnid, coinid, type ) VALUES ( ?, ?, ? )");
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
		stmt.execute("DROP TABLE coins");
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

			if (zBlock.getTxPoW().getBlockNumber().getAsLong() != 230927)
				return true;

			MinimaLogger.log("zBlock mPreviousPeaks");
			ArrayList<MMREntry> mmrs_block = zBlock.getPreviousPeaks();
			for(MMREntry mmr_entry : mmrs_block) {
				MinimaLogger.log(mmr_entry.toJSON().toString());
			}

			MinimaLogger.log("zBlock mTxPoW");
			MinimaLogger.log(zBlock.getTxPoW().toJSON().toString());

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

			// Init Transactions for Calculate
			Transaction calc_txn = null;
			ArrayList<Transaction> calc_txns = new ArrayList<>();
			// Log it.
			MinimaLogger.log("Created New Transaction for calculate TxPoW");

			MinimaLogger.log("Output coins");

			boolean new_txn = true;
			// Save Created coins from a block
			//Set main params
			ArrayList<Coin> outputs = zBlock.getOutputCoins();

			for(Coin cc : outputs) {
				SQL_INSERT_COINS.clearParameters();

				SQL_INSERT_COINS.setLong(1, zBlock.getTxPoW().getBlockNumber().getAsLong());
				SQL_INSERT_COINS.setString(2, cc.getCoinID().to0xString());
				SQL_INSERT_COINS.setString(3, cc.getAmount().toString());
				SQL_INSERT_COINS.setString(4, cc.getAddress().to0xString());
				SQL_INSERT_COINS.setString(5, Address.makeMinimaAddress(cc.getAddress()));
				SQL_INSERT_COINS.setString(6, cc.getTokenID().to0xString());
				SQL_INSERT_COINS.setString(7, cc.getMMREntryNumber().toString());
				SQL_INSERT_COINS.setString(8, cc.getBlockCreated().toString());

				//Do it.
				SQL_INSERT_COINS.execute();

				// Log it.
				MinimaLogger.log(cc.toJSON().toString());

				// Build Transactions
				if (cc.storeState().isEqual(new_txn)) {
					if (new_txn) {
						new_txn = false;
						calc_txn = new Transaction();
					}
					calc_txn.addOutput(cc);
				} else {
					calc_txns.add(calc_txn);
					new_txn = true;
				}
			}

			MinimaLogger.log("Input coins");
			MiniNumber txn_num = MiniNumber.ZERO;

			// Save Spent coins from a block
			ArrayList<CoinProof> inputs = zBlock.getInputCoinProofs();
			//Set main params
			for(CoinProof incoin : inputs) {
				//Get the Query ready
				SQL_INSERT_COINS.clearParameters();

				Coin buffCoin = incoin.getCoin();

				SQL_INSERT_COINS.setLong(1, zBlock.getTxPoW().getBlockNumber().getAsLong());
				SQL_INSERT_COINS.setString(2, buffCoin.getCoinID().to0xString());
				SQL_INSERT_COINS.setString(3, buffCoin.getAmount().toString());
				SQL_INSERT_COINS.setString(4, buffCoin.getAddress().to0xString());
				SQL_INSERT_COINS.setString(5, Address.makeMinimaAddress(buffCoin.getAddress()));
				SQL_INSERT_COINS.setString(6, buffCoin.getTokenID().to0xString());
				SQL_INSERT_COINS.setString(7, buffCoin.getMMREntryNumber().toString());
				SQL_INSERT_COINS.setString(8, buffCoin.getBlockCreated().toString());

				//Do it.
				SQL_INSERT_COINS.execute();

				// Log it.
				MinimaLogger.log(incoin.toJSON().toString());

				// Update Transactions
				calc_txn = calc_txns.get(txn_num.getAsInt());
				if (calc_txn.sumInputs().add(buffCoin.getAmount()).isEqual(calc_txn.sumOutputs())) {
					calc_txn.addInput(buffCoin);
					calc_txns.set(txn_num.getAsInt(), calc_txn);
					txn_num = txn_num.increment();
				} else if (calc_txn.sumInputs().add(buffCoin.getAmount()).isLess(calc_txn.sumOutputs())) {
					calc_txn.addInput(buffCoin);
					calc_txns.set(txn_num.getAsInt(), calc_txn);
				} else {
					MinimaLogger.log("Incorrect transaction build! @" + zBlock.getTxPoW().getBlockNumber().toString());
				}
			}

			for(Transaction txn : calc_txns) {
				txn.calculateTransactionID();
				MinimaLogger.log("Calculated transaction ID from coins block @"+zBlock.getTxPoW().getBlockNumber().toString()+": "+calc_txn.getTransactionID().to0xString());
			}

			// Transactions in a block

			//TxPoW zPoW = zBlock.getTxPoW();
			//TxBody zBody = zPoW.getTxBody();

			/*
			ArrayList<MiniData> txns = zBlock.getTxPoW().getBlockTransactions();
			ArrayList<String> stxns = new ArrayList<>();
			for(MiniData txn : txns) {
				stxns.add(txn.toString());
				MinimaLogger.log("Parse transactions from block with TxPOW: "+txn.toString());
			}

			ArrayList<TxPoW> txps = MinimaDB.getDB().getTxPoWDB().getAllTxPoW(stxns);

			for(TxPoW txp : txps) {
				MinimaLogger.log("Parse transactions from block "+zBlock.getTxPoW().getBlockNumber()+": "+txp.getTransaction().toJSON());
			}
			*/

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
