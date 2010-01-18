package org.liberty.android.fantasisichmemo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	private final String DB_PATH;
	private final String DB_NAME;
	private SQLiteDatabase myDatabase;
	private final Context myContext;
	public DatabaseHelper(Context context, String dbPath, String dbName){
		super(context, dbName, null, 1);
		DB_PATH = dbPath;
		DB_NAME = dbName;
		this.myContext = context;
		this.openDatabase();
	}
	
	public void createDatabase() throws IOException{
		boolean dbExist = checkDatabase();
		if(dbExist){
		}
		else{
			this.getReadableDatabase();
			try{
				copyDatabase();
			}
			catch(IOException e){
				throw new Error("Error copying database");
			}
			
		}
	}
	
	public boolean checkDatabase(){
		SQLiteDatabase checkDB = null;
		try{
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
		}
		catch(SQLiteException e){
		}
		
		if(checkDB != null){
			checkDB.close();
		}
		return checkDB != null ? true : false;
	}
	
	private void copyDatabase() throws IOException{
		InputStream myInput = myContext.getAssets().open(DB_NAME);
		String outFilename = DB_PATH + DB_NAME;
		OutputStream myOutput = new FileOutputStream(outFilename);
		byte[] buffer = new byte[1024];
		int length;
		while((length = myInput.read(buffer)) > 0){
			myOutput.write(buffer, 0, length);
		}
		myOutput.flush();
		myOutput.close();
		myInput.close();
	}
	
	public void openDatabase() throws SQLException{
		String myPath = DB_PATH + DB_NAME;
		Cursor result;
		int count_dict = 0, count_learn = 0;
		try{
		myDatabase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
		}
		catch(Exception e){
			Log.e("First", "Database error first here!: " + e.toString());
			
		}
		try{
			result = myDatabase.rawQuery("SELECT _id FROM dict_tbl", null);
			count_dict = result.getCount();
		}
		catch(Exception e){
			//new AlertDialog.Builder(this).setMessage(e.toString()).show();	
			Log.e("Second", "Database error here!: " + e.toString());
		}
		
		if(count_dict == 0){
			return;
		}
		result = myDatabase.rawQuery("SELECT _id FROM learn_tbl", null);
		count_learn = result.getCount();
		if(count_learn != count_dict){
			this.myDatabase.execSQL("DELETE FROM learn_tbl");
			this.myDatabase.execSQL("INSERT INTO learn_tbl(_id) SELECT _id FROM dict_tbl");
			this.myDatabase.execSQL("UPDATE learn_tbl SET date_learn = '2010-01-01', interval = 0, grade = 0, easiness = 0.0, acq_reps = 0, ret_reps  = 0, lapses = 0, acq_reps_since_lapse = 0, ret_reps_since_lapse = 0");
		}
		
		
	}
	//@Override
	public synchronized void close(){
		if(myDatabase != null){
			myDatabase.close();
		}
		super.close();
	}
	//@Override
	public void onCreate(SQLiteDatabase db){
		
	}
	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		
	}
	
	public Item getItemById(int id, int flag){
		// These function are related to read db operation
		// flag = 0 means no condition
		// flag = 1 means new items, the items user have never seen
		// flag = 2 means item due, they need to be reviewed.
		HashMap hm = new HashMap();
		//ArrayList<String> list = new ArrayList<String>();
		String query = "SELECT learn_tbl._id, date_learn, interval, grade, easiness, acq_reps, ret_reps, lapses, acq_reps_since_lapse, ret_reps_since_lapse, question, answer, note FROM dict_tbl INNER JOIN learn_tbl ON dict_tbl._id=learn_tbl._id WHERE dict_tbl._id >= " + id + " ";
		if(flag == 1){
			query += "AND acq_reps = 0 LIMIT 1";
		}
		else if(flag == 2){
			query += "AND round((julianday(date('now', 'localtime')) - julianday(date_learn))) - interval >= 0 AND acq_reps > 0 LIMIT 1";
		}
		else{
			query += "LIMIT 1";
			
		}
		Cursor result;
		//result = myDatabase.query(true, "dict_tbl", null, querySelection, null, null, null, "_id", null);
		//result = myDatabase.query("dict_tbl", null, querySelection, null, null, null, "_id");
		//result = myDatabase.query(true, "dict_tbl", null, querySelection, null, null, null, null, "1");
		try{
			result = myDatabase.rawQuery(query, null);
		}
		catch(Exception e){
			Log.e("Query item error", e.toString());
			return null;
		}
		
		//System.out.println("The result is: " + result.getString(0));
		//return result.getString(1);
		if(result.getCount() == 0){
			return null; 
		}
		
		result.moveToFirst();
		//int resultId =	result.getInt(result.getColumnIndex("_id"));
		hm.put("_id", new Integer(result.getInt(result.getColumnIndex("_id"))));
		hm.put("question", result.getString(result.getColumnIndex("question")));
		hm.put("answer", result.getString(result.getColumnIndex("answer")));
		hm.put("note", result.getString(result.getColumnIndex("note")));
		
		//querySelection = " _id = " + resultId;
		//result = myDatabase.query(true, "learn_tbl", null, querySelection, null, null, null, null, "1");
		//if(result.getCount() == 0){
		//	return null;
		//}
		//result.moveToFirst();
		hm.put("date_learn", result.getString(result.getColumnIndex("date_learn")));
		hm.put("interval", new Integer(result.getInt(result.getColumnIndex("interval"))));
		hm.put("grade", new Integer(result.getInt(result.getColumnIndex("grade"))));
		hm.put("easiness", new Double(result.getDouble(result.getColumnIndex("grade"))));
		hm.put("acq_reps", new Integer(result.getInt(result.getColumnIndex("acq_reps"))));
		hm.put("ret_reps", new Integer(result.getInt(result.getColumnIndex("ret_reps"))));
		hm.put("acq_reps_since_lapse", new Integer(result.getInt(result.getColumnIndex("acq_reps_since_lapse"))));
		hm.put("ret_reps_since_lapse", new Integer(result.getInt(result.getColumnIndex("ret_reps_since_lapse"))));
		
		
		Item resultItem = new Item();
		resultItem.setData(hm);
		return resultItem;
	}
	
	public void updateItem(Item item){
		// Only update the learn_tbl
		try{
			myDatabase.execSQL("UPDATE learn_tbl SET date_learn = ?, interval = ?, grade = ?, easiness = ?, acq_reps = ?, ret_reps = ?, lapses = ?, acq_reps_since_lapse = ?, ret_reps_since_lapse = ? WHERE _id = ?", item.getLearningData());

		}
		catch(Exception e){
			Log.e("Query error in updateItem!", e.toString());
			
		}
		
	}
	
	public int getScheduledCount(){
		Cursor result = myDatabase.rawQuery("SELECT count(_id) FROM learn_tbl WHERE round((julianday(date('now', 'localtime')) - julianday(date_learn))) - interval >= 0 AND acq_reps > 0", null);
		result.moveToFirst();
		return result.getInt(0);
	}
	
	public int getNewCount(){
		Cursor result = myDatabase.rawQuery("SELECT count(_id) FROM learn_tbl WHERE acq_reps = 0", null);
		result.moveToFirst();
		return result.getInt(0);
	}
	
	public HashMap getSettings(){
		// Dump all the key/value pairs from the learn_tbl
		String key;
		String value;
		HashMap hm = new HashMap();
		
		
		Cursor result = myDatabase.rawQuery("SELECT * FROM control_tbl", null);
		int count = result.getCount();
		for(int i = 0; i < count; i++){
			if(i == 0){
				result.moveToFirst();
			}
			else{
				result.moveToNext();
			}
			key = result.getString(result.getColumnIndex("ctrl_key"));
			value = result.getString(result.getColumnIndex("value"));
			hm.put(key, value);
		}
		return hm;
	}
	
	public void setSettings(HashMap hm){
		// Update the control_tbl in database using the hm
		Set set = hm.entrySet();
		Iterator i = set.iterator();
		while(i.hasNext()){
			Map.Entry me = (Map.Entry)i.next();
			myDatabase.execSQL("REPLACE INTO control_tbl values(?, ?)", new String[]{me.getKey().toString(), me.getValue().toString()});
		}
	}
	
	public void wipeLearnData(){
		this.myDatabase.execSQL("UPDATE learn_tbl SET date_learn = '2010-01-01', interval = 0, grade = 0, easiness = 0.0, acq_reps = 0, ret_reps  = 0, lapses = 0, acq_reps_since_lapse = 0, ret_reps_since_lapse = 0");
	}

	
}