package cat.my.android.pillow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import cat.my.android.pillow.conf.ModelConfiguration;
import cat.my.android.pillow.data.db.DBUtil;
import cat.my.android.pillow.data.db.IDbMapping;
import cat.my.android.pillow.data.sync.DeletedEntries;

public abstract class AbstractDBHelper extends SQLiteOpenHelper{
	Context context;
	public AbstractDBHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		this.context = context;
	}
		
	public List<IDbMapping<?>> getMappings(){
		List<IDbMapping<?>> result = new ArrayList<IDbMapping<?>>();
		Collection<ModelConfiguration<?>> confs = Pillow.getInstance(context).getModelConfigurationFactory().getModelConfigurations().values();
		for(ModelConfiguration<?> conf : confs){
			result.add(conf.getDbMapping());
		}
		return result;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		createTables(db);
	}
	
	@Override
	public void onOpen(SQLiteDatabase db) {
	    super.onOpen(db);
	    if (!db.isReadOnly()) {
	        // Enable foreign key constraints
	        db.execSQL("PRAGMA foreign_keys=ON;");
	    }
	}
	
	protected void enableForeignKeys(SQLiteDatabase db) {
		if (!db.isReadOnly()) {
		    db.execSQL("PRAGMA foreign_keys=ON;");
		}
	}

	public void resetTables(SQLiteDatabase db){
		dropTables(db);
		createTables(db);
	}
	
	public void createTables(SQLiteDatabase db){
		enableForeignKeys(db);
		
		db.execSQL(DeletedEntries.CREATE_TABLE);
		for(IDbMapping<?> mapping: getMappings()){
			db.execSQL(DBUtil.createTable(mapping));
		}
	}
	
	public void dropTables(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS "+DeletedEntries.TABLE);
		for(IDbMapping<?> mapping: getMappings()){
			db.execSQL(DBUtil.dropTable(mapping));
		}
	}
		
}
