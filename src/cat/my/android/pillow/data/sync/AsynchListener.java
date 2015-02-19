package cat.my.android.pillow.data.sync;

import java.util.concurrent.CountDownLatch;

import cat.my.android.pillow.Listeners.ErrorListener;
import cat.my.android.pillow.Listeners.Listener;
import cat.my.android.pillow.PillowError;

public class AsynchListener<T> implements Listener<T>, ErrorListener{
	CountDownLatch lock;
	T result; 
	

	PillowError error;
	
	public AsynchListener() {
		lock = new CountDownLatch(1);
	}
	
	@Override
	public void onResponse(T response) {
		this.result = response;
		lock.countDown();
	}

	@Override
	public void onErrorResponse(PillowError error) {
		this.error = error;
		lock.countDown();
	}
	
	public void await(){
		try {
			lock.await();
		} catch (InterruptedException e) {
			if(error!=null)
				error = new PillowError(e);
		}
	}
	
	public T getResult() {
		await();
		return result;
	}

	public PillowError getError() {
		await();
		return error;
	}

}
