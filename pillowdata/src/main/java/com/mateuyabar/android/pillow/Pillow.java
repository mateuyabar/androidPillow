/*
 * Copyright (c) Mateu Yabar Valles (http://mateuyabar.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 */

package com.mateuyabar.android.pillow;

import android.content.Context;
import android.content.SharedPreferences;

import com.mateuyabar.android.pillow.conf.IModelConfigurations;
import com.mateuyabar.android.pillow.conf.ModelConfiguration;
import com.mateuyabar.android.pillow.conf.ModelConfigurationFactory;
import com.mateuyabar.android.pillow.data.IDataSource;
import com.mateuyabar.android.pillow.data.models.IdentificableModel;
import com.mateuyabar.android.pillow.data.sync.SynchManager;
import com.mateuyabar.android.pillow.util.reflection.RelationGraph;
import com.mateuyabar.util.exceptions.UnimplementedException;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class Pillow {
	public static final String LOG_ID = "pillow";
	public static final String PREFERENCES_FILE_KEY = "com_mateuyabar_android_pillow";


	public static int xmlFileResId;
//	Map<Class<?>, ISynchDataSource<?>> dataSources;
	PillowConfigXml config;
	
	AbstractDBHelper dbHelper;
	SynchManager synchManager;
	ModelConfigurationFactory modelConfigurationFactory;
	RelationGraph relationGraph;
	List<IDataSource<?>> sortedSynchDataSources;
	Context context;
	IModelConfigurations modelConfigurations;
	
	private static Pillow pillow;
	public static synchronized Pillow getInstance(Context context){
		if(pillow==null){
			pillow = new Pillow();
			try {
				pillow.init(context, xmlFileResId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return pillow;
	}
	
	public static synchronized void setConfigurationFile(int xmlFileResId){
		Pillow.xmlFileResId = xmlFileResId;
	}
	
	public static  Pillow getInstance(){
		//Does not initialize if uninitialized!!
		return pillow;
	}
	
//	public Collection<ISynchDataSource<?>> getDataSources() {
//		return dataSources.values();
//	}

	public SharedPreferences getSharedPreferences(){
		return context.getSharedPreferences(PREFERENCES_FILE_KEY, Context.MODE_PRIVATE);
	}

	public AbstractDBHelper getDbHelper() {
		return dbHelper;
	}

	public SynchManager getSynchManager() {
		return synchManager;
	}
	
	private void init(Context context, int xmlFileResId) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, XmlPullParserException, IOException{
		this.context=context;
		config = new PillowConfigXml(context, xmlFileResId);
		if(config.getDbHelper()!=null)
			dbHelper = getClassFor(context, config.getDbHelper());
		Class<IModelConfigurations> modelConfigurationsclazz = (Class<IModelConfigurations>) Class.forName(config.getModelConfigurations());
		modelConfigurations = modelConfigurationsclazz.newInstance();
		modelConfigurationFactory = new ModelConfigurationFactory(context, config, modelConfigurations);
		relationGraph = new RelationGraph();
		for(Class<?> modelClass: modelConfigurationFactory.getModelConfigurations().keySet()){
			relationGraph.addClass(modelClass);
		}
		synchManager = new SynchManager(context, dbHelper);
        synchManager.setDownloadTimeInterval(config.getDownloadTimeInterval());
	}

	protected IModelConfigurations getModelConfigurations() {
		return modelConfigurations;
	}

	public synchronized List<IDataSource<?>> getSortedSynchDataSources() {
		if(sortedSynchDataSources==null){
			List<Class<?>> order = relationGraph.getSynchOrder();
			sortedSynchDataSources = new ArrayList<IDataSource<?>>();
			for(Class<?> orderItem: order){
				sortedSynchDataSources.add(getDataSource((Class<? extends IdentificableModel>)orderItem));
			}
		}
		return sortedSynchDataSources;
	}
	public Context getContext() {
		return context;
	}
	
	private <T> T  getClassFor(Context context, String className) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
    	Class<T> clazz = (Class<T>) Class.forName(className);
    	Constructor<T> constructor = clazz.getConstructor(new Class[] { Context.class});
    	return constructor.newInstance(context);
	}

	public PillowConfigXml getConfig() {
		return config;
	}
	
	/**
	 * Shortcut for getModelConfiguration(modelClass).getDataSource();
	 */
	public <T extends IdentificableModel> IDataSource<T> getDataSource(Class<T> modelClass){
		ModelConfiguration<T> conf = getModelConfiguration(modelClass);
		if(conf==null)
			throw new UnimplementedException("Pillow not found for "+modelClass);
		return conf.getDataSource();
	}
	
	public <T extends IdentificableModel> ModelConfiguration<T> getModelConfiguration(Class<T> modelClass){
		ModelConfiguration<T> result = modelConfigurationFactory.getModelConfiguration(modelClass);
		return result;
	}
	
	public ModelConfigurationFactory getModelConfigurationFactory() {
		return modelConfigurationFactory;
	}



}
