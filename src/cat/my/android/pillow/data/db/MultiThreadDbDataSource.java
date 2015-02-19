package cat.my.android.pillow.data.db;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cat.my.android.pillow.IdentificableModel;
import cat.my.android.pillow.Listeners.ErrorListener;
import cat.my.android.pillow.Listeners.Listener;
import cat.my.android.pillow.util.concurrency.FullStackThreadPoolExecutor;


public class MultiThreadDbDataSource<T extends IdentificableModel> implements IDBDataSource<T>{
	static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
//	static ThreadPoolExecutor threadPoolExecutor = new FullStackThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

	IDBDataSource<T> dataSource;

	public MultiThreadDbDataSource(IDBDataSource<T> dataSource) {
		super();
		this.dataSource=dataSource;
	}

	public abstract static class OperationRunnable<L> implements Runnable{
		L listener;
		ErrorListener errorListener;
		public OperationRunnable(L listener, ErrorListener errorListener) {
			super();
			this.listener = listener;
			this.errorListener = errorListener;
		}
		public L getListener() {
			return listener;
		}
		public ErrorListener getErrorListener() {
			return errorListener;
		}
	}
	
	public class SimpleIndexRunnable extends OperationRunnable<Listener<Collection<T>>>{
		public SimpleIndexRunnable(Listener<Collection<T>> listener, ErrorListener errorListener) {
			super(listener, errorListener);
		}
		@Override
		public void run() {
			dataSource.index(listener, errorListener);
		}
	}
	
	@Override
	public void index(T model, Listener<Collection<T>> listener, ErrorListener errorListener) {
		threadPoolExecutor.execute(new FilterIndexRunnable(model, listener, errorListener));
	}
	
	public class FilterIndexRunnable extends OperationRunnable<Listener<Collection<T>>>{
		T model;
		public FilterIndexRunnable(T model, Listener<Collection<T>> listener, ErrorListener errorListener) {
			super(listener, errorListener);
			this.model = model;
		}
		@Override
		public void run() {
			dataSource.index(model, listener, errorListener);
		}
	}
	
	@Override
	public void index(Listener<Collection<T>> listener, ErrorListener errorListener) {
		threadPoolExecutor.execute(new SimpleIndexRunnable(listener, errorListener));
	}
	
	public class ComplexIndexRunnable extends OperationRunnable<Listener<Collection<T>>>{
		String selection; String[] selectionArgs; String order;
		public ComplexIndexRunnable(String selection, String[] selectionArgs, String order, Listener<Collection<T>> listener, ErrorListener errorListener) {
			super(listener, errorListener);
			this.selection=selection; this.selectionArgs=selectionArgs; this.order=order;
		}
		@Override
		public void run() {
			dataSource.index(selection, selectionArgs, order, listener, errorListener);
		}
	}
	
	@Override
	public void index(String selection, String[] selectionArgs, String order, Listener<Collection<T>> listener, ErrorListener errorListener) {
		threadPoolExecutor.execute(new ComplexIndexRunnable(selection, selectionArgs, order, listener, errorListener));
	}
	
	public class ShowRunnable extends OperationRunnable<Listener<T>>{
		T model;
		public ShowRunnable(T model, Listener<T> listener, ErrorListener errorListener) {
			super(listener, errorListener);
			this.model = model;
		}
		@Override
		public void run() {
			dataSource.show(model, listener, errorListener);
		}
	}

	@Override
	public void show(T model, Listener<T> listener, ErrorListener errorListener) {
		threadPoolExecutor.execute(new ShowRunnable(model, listener, errorListener));
	}

	public class CreateRunnable extends OperationRunnable<Listener<T>>{
		T model;
		public CreateRunnable(T model, Listener<T> listener, ErrorListener errorListener) {
			super(listener, errorListener);
			this.model = model;
		}
		@Override
		public void run() {
			dataSource.create(model, listener, errorListener);
		}
	}
	
	@Override
	public void create(T model, Listener<T> listener, ErrorListener errorListener) {
		threadPoolExecutor.execute(new CreateRunnable(model, listener, errorListener));
	}
	
	public class UpdateRunnable extends OperationRunnable<Listener<T>>{
		T model;
		public UpdateRunnable(T model, Listener<T> listener, ErrorListener errorListener) {
			super(listener, errorListener);
			this.model = model;
		}
		@Override
		public void run() {
			dataSource.update(model, listener, errorListener);
		}
	}

	@Override
	public void update(T model, Listener<T> listener, ErrorListener errorListener) {
		threadPoolExecutor.execute(new UpdateRunnable(model, listener, errorListener));
	}
	
	public class DestroyRunnable extends OperationRunnable<Listener<Void>>{
		T model;
		public DestroyRunnable(T model, Listener<Void> listener, ErrorListener errorListener) {
			super(listener, errorListener);
			this.model = model;
		}
		@Override
		public void run() {
			dataSource.destroy(model, listener, errorListener);
		}
	}

	@Override
	public void destroy(T model, Listener<Void> listener,
			ErrorListener errorListener) {
		threadPoolExecutor.execute(new DestroyRunnable(model, listener, errorListener));
	}
	
	public class CountRunnable extends OperationRunnable<Listener<Integer>>{
		String selection;
		String[] selectionArgs;
		public CountRunnable(String selection, String[] selectionArgs,Listener<Integer> listener, ErrorListener errorListener) {
			super(listener, errorListener);
			this.selection = selection;
			this.selectionArgs = selectionArgs;
		}
		@Override
		public void run() {
			dataSource.count(selection, selectionArgs, listener, errorListener);
		}
	}
	
	@Override
	public void count(String selection, String[] selectionArgs, Listener<Integer> listener,
			ErrorListener errorListener) {
		threadPoolExecutor.execute(new CountRunnable(selection, selectionArgs, listener, errorListener));
	}

	
	public DBModelController<T> getDbModelController(){
		return dataSource.getDbModelController();
	}

	
	public ThreadPoolExecutor getThreadPoolExecutor() {
		return threadPoolExecutor;
	}

}
