package ru.cocsosoft;

import java.lang.System;
import java.util.Calendar;
import java.util.Hashtable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Binder;
import android.util.Log;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

public class TBDB extends SQLiteOpenHelper {

	public static final String databseName = "TB.db";
	public static final String tableSettings = "Setting";
	public static final String tableVote = "Vote";
	public static final String tableLocaBox = "LocalBox";
	public static final int databaseVersion = 3;
	private static final String TAG = "DebugLogs"; // для тех. логов
	private static final boolean isDEBUG = true; // вкл. логов

	public static final String BOXID = "boxId";
	public static final String CREATED = "createdAt";
	public static final String IMGFILE1 = "imageFileName1";
	public static final String IMGFILE2 = "imageFileName2";
	public static final String VOTES1 = "Votes1";
	public static final String VOTES2 = "Votes2";
	
	
	
	
	
	private static SQLiteDatabase tbdb;

	public TBDB(Context context) {
		super(context, databseName, null, databaseVersion);
		if (isDEBUG)
			Log.d(TAG, "TBDB(Context context)");
		// TODO Auto-generated constructor stub
		tbdb = getWritableDatabase();
	}

	@Override
	public synchronized void close() {
		tbdb.close();
	};

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		tbdb = db;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		if (isDEBUG)
			Log.d(TAG, "onCreate(SQLiteDatabase db)");

		try {
			db.execSQL("CREATE TABLE " + tableSettings
					+ " (key VARCHAR(128), value VARCHAR(255))");
			db.execSQL("CREATE TABLE "
					+ tableVote
					+ " ( boxId VARCHAR(40), createdAt INTEGER DEFAULT CURRENT_TIMESTAMP )");
			db.execSQL("CREATE TABLE "
					+ tableLocaBox
					+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, boxId VARCHAR(40), createdAt INTEGER DEFAULT CURRENT_TIMESTAMP, imageFileName1 VARCHAR(512), imageFileName2 VARCHAR(512), Votes1 INTEGER DEFAULT 0, Votes2 INTEGER DEFAULT 0 )");
		} catch (SQLException ex) {
			Log.d(TAG,
					String.format("onCreate(SQLiteDatabase db) : %s",
							ex.getMessage()));
			ex.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (isDEBUG)
			Log.d(TAG,
					"onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) ");
		try {
			db.execSQL("DROP TABLE IF EXISTS " + tableVote);
			db.execSQL("DROP TABLE IF EXISTS " + tableLocaBox);
			db.execSQL("DROP TABLE IF EXISTS " + tableSettings);
		} catch (SQLException ex) {
			Log.d(TAG,
					String.format("onCreate(SQLiteDatabase db) : %s",
							ex.getMessage()));
			ex.printStackTrace();
		}
		onCreate(db);
	}

	// add new box to database
	public void addLocalBox(LocalBox box) {
		ContentValues values = new ContentValues(1);
		values.put(TBDB.BOXID, box.id);
		values.put(TBDB.IMGFILE1, box.imageFileName1);
		values.put(TBDB.IMGFILE2, box.imageFileName2);
		values.put(TBDB.VOTES1, box.Votes1);
		values.put(TBDB.VOTES2, box.Votes2);
		long rowId = tbdb.insert(tableLocaBox, null, values);
		Log.d(TAG, String.format("add local box rowId = %d", rowId));
		addVote(box.id);
	}

	// add new vote to database
	public void addVote(String boxId) {
		ContentValues values = new ContentValues(1);
		values.put(TBDB.BOXID, boxId);
		long rowId = tbdb.insert(tableVote, null, values);
		Log.d(TAG, String.format("add vote rowId = %d", rowId));
	}

	// фильтрация по времени пока не работает, хотя вроде должна.
	public Profile loadLocalBoxes(boolean fAllRecords) throws Exception {

		Profile boxes = new Profile(this);
		LocalBox lb = new LocalBox();
		Cursor box;

		// load entries from database

		if (fAllRecords) {
			box = tbdb
					.rawQuery(
							"SELECT boxID, createdAt, imageFileName1, imageFileName2, Votes1, Votes2 from " + tableLocaBox, null);
		} else {

			Calendar dayBegin = Calendar.getInstance();
			dayBegin.setTimeInMillis(System.currentTimeMillis());
			clearTime(dayBegin);

			Calendar dayEnd = Calendar.getInstance();
			dayEnd.setTimeInMillis(System.currentTimeMillis());
			dayEnd.add(Calendar.DAY_OF_MONTH, 1);
			clearTime(dayBegin);

			box = tbdb
					.rawQuery(
							"SELECT boxID, createdAt, imageFileName1, imageFileName2, Votes1, Votes2 from " + tableLocaBox + " WHERE createdAt <= ? AND createdAt >= ?",
							new String [] { String.valueOf(dayEnd.getTimeInMillis()), String.valueOf(dayBegin.getTimeInMillis()) });

		}

		while (box.moveToNext()) {

			lb.id = box.getString(0);
			lb.createdAt = box.getLong(1);
			lb.imageFileName1 = box.getString(2);
			lb.imageFileName2 = box.getString(3);
			lb.Votes1 = box.getLong(4);
			lb.Votes2 = box.getLong(5);

			boxes.put(lb.createdAt, lb);
			lb = new LocalBox();
		}

		return boxes;
	}

	
	// column _id for SimpleCursorAdapter http://stackoverflow.com/questions/5812030/java-lang-illegalargumentexception-column-id-does-not-exist
	// фильтрация по времени пока не работает, хотя вроде должна.
	public static Cursor loadLocalBoxesCursors(boolean fAllRecords) throws Exception {

		Cursor box;

		// load entries from database

		if (fAllRecords) {
			box = tbdb
					.rawQuery(
							"SELECT _id, boxId, createdAt, imageFileName1, imageFileName2, Votes1, Votes2 from " + tableLocaBox, null);
		} else {

			Calendar dayBegin = Calendar.getInstance();

			dayBegin.setTimeInMillis(System.currentTimeMillis());
			clearTime(dayBegin);

			Calendar dayEnd = Calendar.getInstance();
			dayEnd.setTimeInMillis(System.currentTimeMillis());
			dayEnd.add(Calendar.DAY_OF_MONTH, 1);
			clearTime(dayBegin);

			box = tbdb
					.rawQuery(
							"SELECT _id, boxID, createdAt, imageFileName1, imageFileName2, Votes1, Votes2 from " + tableLocaBox + " WHERE createdAt <= ? AND createdAt >= ?",
							new String [] { String.valueOf(dayEnd.getTimeInMillis()), String.valueOf(dayBegin.getTimeInMillis()) });

		}

		return box;
	}


    public static void updateVotesInLocalDB(Context cnt) {

        String boxID;
        int lVotes1, lVotes2, rowId;
        boolean bNeedMsq = false;
        
        try {
            Cursor cur = loadLocalBoxesCursors(true);

            cur.moveToFirst();
            while (!cur.isAfterLast()) {

                rowId = cur.getInt(cur.getColumnIndex("_id"));
                boxID = cur.getString(cur.getColumnIndex("boxId"));
                lVotes1 = cur.getInt(cur.getColumnIndex("Votes1"));
                lVotes2 = cur.getInt(cur.getColumnIndex("Votes2"));

                ParseQuery<Box> myQuery = Box.getQuery();

                myQuery.whereEqualTo("boxID", boxID);
                myQuery.setCachePolicy(ParseQuery.CachePolicy.IGNORE_CACHE);


                Box remoteBox = myQuery.getFirst();
                ContentValues values = new ContentValues(1);

                if ( lVotes1 != remoteBox.getVote(1) ) {
                    values.put(TBDB.VOTES1, remoteBox.getVote(1));
                }

                if ( lVotes2 != remoteBox.getVote(2) ) {
                    values.put(TBDB.VOTES2, remoteBox.getVote(2));
                }

                if (values.size() > 0)

                tbdb.beginTransaction();
                try {
                    String sWhere = "_id = " + rowId;
                    tbdb.update(tableLocaBox, values, sWhere, null);
                    if (isDEBUG) Log.d("updateVotesInLocalDB","update votes in local boxID:" + boxID);

                    tbdb.setTransactionSuccessful();
                } finally {
                    tbdb.endTransaction();
                    
                    bNeedMsq = true;                                                          
                }



                cur.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (bNeedMsq)
        	((ThatBetterActivity) cnt).notiMsqNewVote(cnt.getString(R.string.msq_new_vote_title),cnt.getString(R.string.save_error));

    }


	public static void clearTime(Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.clear(Calendar.MINUTE);
		calendar.clear(Calendar.SECOND);
		calendar.clear(Calendar.MILLISECOND);
	}

	/* возвращаем из базы set ID-шников, за которые данный юзер уже проголосовал
	 * 
	 *   ? выборка за период пока не работает
	 * 
	 *  */
	
	public Hashtable<String, Long> loadCurrentUserVotes(boolean fAllRecords) {

		Hashtable<String, Long> setOfVotedBoxes = new Hashtable<String, Long>();

		Cursor votes;
		if (fAllRecords) {
			votes = tbdb.rawQuery("SELECT boxId, createdAt from " + tableVote, null);
		} else {
			Calendar dayBegin = Calendar.getInstance();
			dayBegin.setTimeInMillis(System.currentTimeMillis());
			clearTime(dayBegin);

			Calendar dayEnd = Calendar.getInstance();
			dayEnd.setTimeInMillis(System.currentTimeMillis());
			dayEnd.add(Calendar.DAY_OF_MONTH, 1);
			clearTime(dayBegin);

			votes = tbdb
					.rawQuery(
							"SELECT boxId, createdAt from " + tableVote + " where createdAt <= ? and createdAt >= ?",
							new String[] {
									String.valueOf(dayEnd.getTimeInMillis()),
									String.valueOf(dayBegin.getTimeInMillis()) });
		}

		while (votes.moveToNext()) {
			setOfVotedBoxes.put(votes.getString(0), votes.getLong(1));
		}

		return setOfVotedBoxes;
	}
}
