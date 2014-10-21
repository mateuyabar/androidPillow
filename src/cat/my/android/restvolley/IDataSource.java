package cat.my.android.restvolley;

import java.util.Collection;

import cat.my.android.restvolley.Listeners.CollectionListener;
import cat.my.android.restvolley.Listeners.ErrorListener;
import cat.my.android.restvolley.Listeners.Listener;



public interface IDataSource <T extends IdentificableModel>{
	/**
	 * Returns all the instances
	 * @param listener to be executed when the instances have been retrieved
	 * @param errorListener to be executed in case of error
	 */
	public void index(CollectionListener<T> listener, ErrorListener errorListener);

	public void show(T model, Listener<T> listener, ErrorListener errorListener);
	
	public void create(T model, Listener<T> listener, ErrorListener errorListener);
	
	public void update(T model, Listener<T> listener, ErrorListener errorListener);
	
	public void destroy(T model, Listener<Void> listener, ErrorListener errorListener);
}
