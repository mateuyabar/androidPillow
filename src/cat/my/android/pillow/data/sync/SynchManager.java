package cat.my.android.pillow.data.sync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import cat.my.android.pillow.AbstractDBHelper;
import cat.my.android.pillow.Listeners.ErrorListener;
import cat.my.android.pillow.Listeners.Listener;
import cat.my.android.pillow.Pillow;
import cat.my.android.pillow.PillowError;
import cat.my.android.pillow.data.db.DBUtil;
import cat.my.android.pillow.util.reflection.RelationGraph;


public class SynchManager {
	RelationGraph relationGraph; 
	
	List<ISynchDataSource<?>> synchDataSources= new ArrayList<ISynchDataSource<?>>();
	List<ISynchDataSource<?>> sortedSynchDataSources;

	SharedPreferences sharedPref;
	AbstractDBHelper dbHelper;
	long downloadTimeInterval = 3600000; //1 hour
	private static final String LAST_DOWNLOAD_DATE = "LAST_DOWNLOAD_DATE";
	
	public SynchManager(Context context, Collection<ISynchDataSource<?>> synchDataSources, AbstractDBHelper dbHelper) {
		super();
		this.synchDataSources = new ArrayList<ISynchDataSource<?>>(synchDataSources);
		this.dbHelper = dbHelper;
		sharedPref = context.getSharedPreferences(Pillow.PREFERENCES_FILE_KEY, Context.MODE_PRIVATE);
		relationGraph = new RelationGraph();
		for(ISynchDataSource<?> dataSource: synchDataSources){
			relationGraph.addClass(dataSource.getModelClass());
		}
	}
	
	public synchronized List<ISynchDataSource<?>> getSortedSynchDataSources() {
		if(sortedSynchDataSources==null){
			List<Class<?>> order = relationGraph.getSynchOrder();
			Map<Class<?>, ISynchDataSource<?>> map = new HashMap<Class<?>, ISynchDataSource<?>>();
			for(ISynchDataSource<?> dataSource : synchDataSources){
				map.put(dataSource.getModelClass(), dataSource);
			}
			sortedSynchDataSources = new ArrayList<ISynchDataSource<?>>();
			for(Class<?> orderItem: order){
				sortedSynchDataSources.add(map.get(orderItem));
			}
		}
		return sortedSynchDataSources;
	}
	
	private void resetTables() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		dbHelper.resetTables(db);
		db.close();
	}
	
	public void reloadData(Listener<Void> listener){
		resetTables();
		download(listener, true);
	}
	
	public void download(Listener<Void> listener, boolean force){
		if(!force && getLastDownload()!=null && isValidDonwload()){
			listener.onResponse(null);
		} else {
			new DownloadTask(listener).start();
		}
	}
	
	private void setLastDownload(Date date) {
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(LAST_DOWNLOAD_DATE, DBUtil.dateTimeToDb(date));
		editor.commit();
	}

	public Date getLastDownload() {
		String value =  sharedPref.getString(LAST_DOWNLOAD_DATE, null);
		return DBUtil.dbToDateTime(value);
	}

	
	public long getDownloadTimeInterval() {
		return downloadTimeInterval;
	}

	/**
	 * downloadTimeInterval indicates the accepted interval (in milliseconds) between two downloads (note that is not used on force=true) 
	 * @param downloadTimeInterval
	 */
	public void setDownloadTimeInterval(long downloadTimeInterval) {
		this.downloadTimeInterval = downloadTimeInterval;
	}

	public void synchronize(Listener<Void> listener, ErrorListener errorListener, boolean force){
		new SynchronizeOperation(listener, errorListener, force).start();
	}
	
	public void sendDirty(Listener<Void> listener, ErrorListener errorListener){
		new SendDirtyOperation(listener, errorListener).start();
	}
	
	private class SynchronizeOperation implements Listener<Void>{
		Listener<Void> listener;
		ErrorListener errorListener;
		boolean force;
		public SynchronizeOperation(Listener<Void> listener, ErrorListener errorListener, boolean force) {
			this.listener = listener;
			this.errorListener = errorListener;
			this.force = force;
		}
		public void start(){
			sendDirty(this, errorListener);
		}
		@Override
		public void onResponse(Void response) {
			download(listener, force);
		}
	}
	
	private class SendDirtyOperation implements Listener<Void>{
		Listener<Void> listener;
		ErrorListener errorListener;
		int i=0;
		public SendDirtyOperation(Listener<Void> listener, ErrorListener errorListener) {
			this.listener = listener;
			this.errorListener = errorListener;
		}
		public void sendNext(){
			if(i<getSortedSynchDataSources().size()){
				ISynchDataSource<?> dataSource = getSortedSynchDataSources().get(i);
				++i;
				dataSource.sendDirty(this, errorListener);
			} else {
				listener.onResponse(null);
			}
		}
		@Override
		public void onResponse(Void response) {
			sendNext();
		}
		public void start(){
			sendNext();
		}
	}
	
	private boolean isValidDonwload() {
		return getLastDownload().getTime() + downloadTimeInterval > new Date().getTime();
	}

	private class DownloadTask implements Listener, ErrorListener{
		int i=0;
		Listener<Void> listener;
		boolean errorFound = false;
		public DownloadTask(Listener<Void> listener) {
			this.listener = listener;
		}

		@Override
		public void onResponse(Object response) {
			i++;
			if(getSortedSynchDataSources().size()>i){
				downloadCurrent();
			} else {
				if(!errorFound){
					setLastDownload(new Date());
					listener.onResponse(null);
				}
			}
		}
		
		private void downloadCurrent(){
			getSortedSynchDataSources().get(i).download(this, this);
		}
		
		public void start(){
			downloadCurrent();
		}

		@Override
		public void onErrorResponse(PillowError error) {
			errorFound = true;
			CommonListeners.defaultErrorListener.onErrorResponse(error);
		}
	}
}
