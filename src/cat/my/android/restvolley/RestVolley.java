package cat.my.android.restvolley;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import cat.my.android.restvolley.sync.ISynchDataSource;
import cat.my.android.restvolley.sync.SynchManager;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.database.sqlite.SQLiteOpenHelper;

public class RestVolley {
	public static final String PREFERENCES_FILE_KEY = "cat_my_android_restvolley";
	public static int xmlFileResId;
	List<ISynchDataSource<?>> dataSources;
	RestVolleyConfig config;
	
	AbstractDBHelper dbHelper;
	SynchManager synchManager;
	
	private static RestVolley restVolley;
	public static synchronized RestVolley getInstance(Context context){
		if(restVolley==null){
			restVolley = new RestVolley();
			try {
				restVolley.init(context, xmlFileResId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return restVolley;
	}
	
	public static synchronized void setConfigurationFile(int xmlFileResId){
		RestVolley.xmlFileResId = xmlFileResId;
	}
	
	public static  RestVolley getInstance(){
		//Does not initialize if uninitialized!!
		return restVolley;
	}
	
	public List<ISynchDataSource<?>> getDataSources() {
		return dataSources;
	}

	public AbstractDBHelper getDbHelper() {
		return dbHelper;
	}

	public SynchManager getSynchManager() {
		return synchManager;
	}
	
	private void init(Context context, int xmlFileResId) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, XmlPullParserException, IOException{
		config = new RestVolleyConfig(context, xmlFileResId);
		dbHelper = getClassFor(context, config.getDbHelper());
		dataSources = new ArrayList<ISynchDataSource<?>>();
		for(String dataSourceName: config.getDataSources()){
			ISynchDataSource<?> currentModel = getClassFor(context, dataSourceName);
            dataSources.add(currentModel);
		}
        synchManager = new SynchManager(context, dataSources, dbHelper);
        synchManager.setDownloadTimeInterval(config.getDownloadTimeInterval());
	}
	
	private <T> T  getClassFor(Context context, String className) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
    	Class<T> clazz = (Class<T>) Class.forName(className);
    	Constructor<T> constructor = clazz.getConstructor(new Class[] { Context.class});
    	return constructor.newInstance(context);
	}
}