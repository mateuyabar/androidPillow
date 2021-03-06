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

package com.mateuyabar.android.pillow.data.db;

import android.content.ContentValues;
import android.database.Cursor;

import com.mateuyabar.util.StringUtil;
import com.mateuyabar.util.exceptions.UnimplementedException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class DBUtil {
	public static final String COMMA_SEP = ",";
	public static final int BOOLEAN_TRUE = 1;
	public static final int BOOLEAN_FALSE = 0;
	public static final String STRING_TYPE = " TEXT";
	public static final String BOOLEAN_TYPE = " INTEGER";
	public static final String TIMESTAMP_TYPE = " INTEGER";
	public static final String INT_TYPE = " INTEGER";
	public static final String LONG_TYPE = INT_TYPE;
	public static final String DECIMAL_TYPE = " INTEGER";
	public static final String DOUBLE_TYPE = " DOUBLE";
	public static final String CALENDAR_TYPE = "  TEXT";
	public static final String DATE_TYPE = "  TEXT";
	public static final String ENUM_TYPE = "  INTEGER";
	
	
	public static String createTable(IDbMapping<?> mapping){
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("CREATE TABLE " + mapping.getTableName());
		sqlBuilder.append(" (" + DBModelController.COMMON_MODEL_ATTRIBUTES);
		String attributes = DBModelController.creteAttsString(mapping.getAttributes());
		if(!StringUtil.isBlanck(attributes)){
			sqlBuilder.append(DBUtil.COMMA_SEP + attributes);
		}
		sqlBuilder.append(addForeignKeys(mapping)+");");
		return sqlBuilder.toString();
	} 


	private static String addForeignKeys(IDbMapping<?> mapping) {
//		WE ARE NOT USING FOREIGN KEYS, BUT TRIGGERS (look at problem_solution.txt)
//				DO NOT DELETE
//		List<String> keys = mapping.getForeignKeys();
//		StringBuilder builder = new StringBuilder();
//		for(String key:keys){
//			builder.append(", ");
//			builder.append(key);
//		}
//		return builder.toString();
		return "";
	}

	public static String dropTable(IDbMapping<?> mapping){
		return "DROP TABLE IF EXISTS "+mapping.getTableName();
	}

	public static Integer getInteger(Cursor cursor, int columnIndex){
		if (cursor.isNull (columnIndex))
			return null;
		else
			return cursor.getInt(columnIndex);
	}

//	public static boolean getBoolean(Cursor cursor, int columnIndex){
//		int value = cursor.getInt(columnIndex);
//		return value==DBUtil.BOOLEAN_TRUE ? true : false;
//	}
//
//	public static Date getDate(Cursor cursor, int columnIndex){
//		String value = cursor.getString(columnIndex);
//		return dbToDate(value);
//	}
//
//	public static Calendar getCalendar(Cursor cursor, int columnIndex){
//		String value = cursor.getString(columnIndex);
//		return dbToCalendar(value);
//	}
//
	public static final String DATE_STRING_FORMAT = "yyyy-MM-dd";
	public static final String DATE_TIME_STRING_FORMAT = "yyyy-MM-dd HH:mm:ss";


//
//	public static String calendarToDb(Calendar date){
//		if(date==null) return null;
//		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_STRING_FORMAT);
//		return dateFormat.format(date.getTime());
//	}
//
//	/**
//	 * Converts a date to string for json or database storage
//	 * @param date
//	 * @return
//	 */
//	public static String dateToDb(Object date){
//		if(date==null) return null;
//		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_STRING_FORMAT);
//		return dateFormat.format(date);
//	}
//
//	public static Integer booleanToDb(Object value){
//		if(value==null) return null;
//		return ((Boolean)value) ? DBUtil.BOOLEAN_TRUE : DBUtil.BOOLEAN_FALSE;
//	}
//
//	/**
//	 * Converts a String obtained from json or DB to date for model usage
//	 * @param date
//	 * @return
//	 */
//	public static Date dbToDate(String date){
//		if(date==null) return null;
//		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_STRING_FORMAT);
//		try {
//			return dateFormat.parse(date);
//		} catch (ParseException e) {
//			e.printStackTrace();
//			return null;
//		}
//	}
//
//	/**
//	 * Converts a String obtained from json or DB to date for model usage
//	 * @param date
//	 * @return
//	 */
//	public static Calendar dbToCalendar(String date){
//		if(date==null) return null;
//		Calendar cal = Calendar.getInstance();
//		cal.setTime(dbToDate(date));
//		return cal;
//	}
////
	public static String dateTimeToDb(Date date){
		if(date==null) return null;
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_STRING_FORMAT);
		return dateFormat.format(date);

//		if(date==null) return null;
//		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_STRING_FORMAT);
//		return dateFormat.format(date);
	}
////
	public static Date dbToDateTime(String date){
		if(date==null) return null;
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_STRING_FORMAT);
		try {
			return dateFormat.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
//
//	public static Integer enumToDb(Enum<?> value){
//		if(value==null)
//			return null;
//		if(value instanceof IdentificableEnum){
//			((IdentificableEnum)value).getId();
//		}
//		return value.ordinal();
//	}
//
//	public static <T> T dbToEnum(Cursor cursor, int columnIndex, Class<T> enumClass){
//		if (cursor.isNull (columnIndex))
//			return null;
//		int id = cursor.getInt(columnIndex);
//		T[] values = (T[]) enumClass.getEnumConstants();
//		if(enumClass.isAssignableFrom(IdentificableEnum.class)){
//			for(T value:values){
//				if(((IdentificableEnum)value).getId() == id){
//					return value;
//				}
//			}
//		} else {
//			return (T) values[id];
//		}
//		throw new BreakFastException();
//	}
	
	public static String createUUID() {
		return UUID.randomUUID().toString();
	}

//    public static Object dbValue(Object value) {
//        if(value instanceof Calendar){
//            value = DBUtil.calendarToDb((Calendar)value);
//        } else if(value instanceof Date){
//            value = DBUtil.dateToDb((Date)value);
//        } else if(value instanceof Enum){
//            value = DBUtil.enumToDb((Enum<?>) value);
//        } else if(value instanceof Boolean){
//            value = DBUtil.booleanToDb(value);
//        }
//        return value;
//    }

    /**
     * Helper method to allow to put a value of type Object to ContentValues. It also allows to store complex methods (like embeddable models)
     * @param values
     * @param key
     * @param value
     */
    public static void put(ContentValues values, String key, Object value) {
        if(value==null) return;

        if(value instanceof String)
            values.put(key, (String) value);
        else if(value instanceof Byte)
            values.put(key, (Byte) value);
        else if(value instanceof Short)
            values.put(key, (Short) value);
        else if(value instanceof Integer) //Integer.TYPE
            values.put(key, (Integer) value);
        else if(value instanceof Long)
            values.put(key, (Long) value);
        else if(value instanceof Double)
            values.put(key, (Double) value);
        else if(value instanceof Boolean)
            values.put(key, (Boolean) value);
        else if(value instanceof byte[])
            values.put(key, (byte[]) value);
        else  {
            throw new UnimplementedException("can't save value of type" + value.getClass());
        }
    }
	
}
