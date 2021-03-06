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

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.XmlResourceParser;

public class PillowConfigXml {
	String dbHelper;
//	List<String> dataSources = new ArrayList<String>();
	int downloadTimeInterval = 3600000;
	int maxResponseWaitTime = 10000;
	boolean dbMultiThread = false;
	String modelConfigurations;
	String url;

	public PillowConfigXml(Context context, int xmlFileResId){
		try {
			read(context, xmlFileResId);
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void read(Context context, int xmlFileResId) throws XmlPullParserException, IOException {
        XmlResourceParser parser = context.getResources().getXml(xmlFileResId);
        //Check http://stackoverflow.com/questions/8378748/create-a-custom-xml-data-type
        int token;
        while ((token = parser.next()) != XmlPullParser.END_DOCUMENT) {
            if (token == XmlPullParser.START_TAG) {
            	String tagName = parser.getName();
                if ("modelConfigurations".equals(tagName)) {
                	String clazz = parser.getAttributeValue(0);//("class");
                	modelConfigurations = clazz;
                }
                else if("dbhelper".equals(tagName)){
                	String clazz = parser.getAttributeValue(0);//("class");
                	dbHelper = clazz;
                } else if ("downloadTimeInterval".equals(tagName)){
                	downloadTimeInterval = parser.getAttributeIntValue(0, 3600000);//("vale");
                } else if ("maxResponseWaitTime".equals(tagName)){
                	maxResponseWaitTime = parser.getAttributeIntValue(0, 10000);//("vale");
                } else if ("dbMultiThread".equals(tagName)){
                	dbMultiThread = parser.getAttributeBooleanValue(0, false);
                } else if ("url".equals(tagName)){
                	url = parser.getAttributeValue(0);
                }
            }
        }
	}

	public String getDbHelper() {
		return dbHelper;
	}

//	public List<String> getDataSources() {
//		return dataSources;
//	}

	public int getDownloadTimeInterval() {
		return downloadTimeInterval;
	}

	public int getMaxResponseWaitTime() {
		return maxResponseWaitTime;
	}

	public boolean isDbMultiThread() {
		return dbMultiThread;
	}
	
	public String getModelConfigurations() {
		return modelConfigurations;
	}
	public String getUrl() {
		return url;
	}
}
