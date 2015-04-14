/**
 * Copyright Mateu Yábar (http://mateuyabar.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package cat.my.android.pillow.data.rest;

import android.content.Context;
import android.util.Log;

import com.android.volley.NoConnectionError;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cat.my.android.pillow.IDataSource;
import cat.my.android.pillow.IdentificableModel;
import cat.my.android.pillow.Listeners.Listener;
import cat.my.android.pillow.Pillow;
import cat.my.android.pillow.PillowConfigXml;
import cat.my.android.pillow.PillowError;
import cat.my.android.pillow.data.core.IPillowResult;
import cat.my.android.pillow.data.core.PillowResultListener;
import cat.my.android.pillow.data.db.MultiThreadDbDataSource.OperationRunnable;
import cat.my.android.pillow.data.rest.IAuthenticationController.NullAuthenticationController;
import cat.my.android.pillow.data.rest.IAuthenticationController.AuthenticationData;
import cat.my.android.pillow.data.rest.requests.GsonCollectionRequest;
import cat.my.android.pillow.data.rest.requests.GsonRequest;

public class RestDataSource<T extends IdentificableModel> implements IDataSource<T> {
	private static ThreadPoolExecutor dbThreadPool = new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	
	public static final String LOG_ID = Pillow.LOG_ID +" - RestDataSource";
	public static boolean SIMULATE_OFFLINE_CONNECTIVITY_ON_TESTING = false;
	
	Context context;
	RequestQueue volleyQueue;
	IRestMapping<T> restMapping;
	
	IAuthenticationController authenticationController;
	
	
	public RestDataSource(IRestMapping<T> restMapping, Context context) {
		this(restMapping, context, new NullAuthenticationController());
	}
	
	public RestDataSource(IRestMapping<T> restMapping, Context context, IAuthenticationController authenticationController) {
		this.restMapping=restMapping;
		this.volleyQueue = VolleyFactory.newRequestQueue(context);
		if(authenticationController==null)
			authenticationController = new NullAuthenticationController();
		this.authenticationController = authenticationController;
		this.context = context;
	}
	
	public IRestMapping<T> getRestMapping() {
		return restMapping;
	}
	
	private PillowConfigXml getConfig(){
		return Pillow.getInstance(context).getConfig();
	}

//	/**
//	 * @return false if authentication is required but not provided
//	 */
//	public boolean checkAuthenticationRequired() {
//		return !serverRequiresAuthentication || authenticationController.isAuthenticated();
//	}
//	public void setServerRequiresAuthentication(boolean serverRequiresAuthentication) {
//		this.serverRequiresAuthentication = serverRequiresAuthentication;
//	}
	
	public IPillowResult<Collection<T>> executeCollectionListOperation(int method, String operation, Map<String, Object> params) {
		Route route = restMapping.getCollectionRoute(method, operation);
		return executeListOperation(route, params);
	}
	
	public IPillowResult<T> executeMemberOperation(T model, int method, String operation, Map<String, Object> params) {
		Route route = restMapping.getMemberRoute(model, method, operation);
		return executeOperation(model, route, params);
	}
	
	public IPillowResult<T> executeCollectionOperation(T model, int method, String operation, Map<String, Object> params) {
		Route route = restMapping.getCollectionRoute(method, operation);
		return executeOperation(model, route, params);
	}
	
	private IPillowResult<Collection<T>> executeListOperation(final Route route, final Map<String, Object> params) {
		final PillowResultListener<Collection<T>> result = new PillowResultListener<Collection<T>>(context);
		
		Log.d(LOG_ID, "Executing operation "+route.method + " "+route.url + " "+params);
		if(SIMULATE_OFFLINE_CONNECTIVITY_ON_TESTING){
			return result.setError(new PillowError(new NoConnectionError()));
		}		
		
		Listener<AuthenticationData> onSessionStarted = new Listener<AuthenticationData>() {
			@Override
			public void onResponse(AuthenticationData sessionData) {
				Map<String, Object> map = sessionData.getData();
				if(params!=null){
					map.putAll(params);
				}
				GsonCollectionRequest<T> gsonRequest = new GsonCollectionRequest<T>(restMapping.getSerializer(), route, restMapping.getCollectionType(), map, result, result, getConfig().getDownloadTimeInterval());
				gsonRequest.setShouldCache(false);
				volleyQueue.add(gsonRequest);
			}
		};
		
		IPillowResult<AuthenticationData> sessionData = authenticationController.getAuthentication();
		sessionData.setListeners(onSessionStarted, result);
		
		return result;
	}
	
	private IPillowResult<T> executeOperation(final T model, final Route route, final Map<String, Object> params) {
		final PillowResultListener<T> result = new PillowResultListener<T>(context);
		Log.d(LOG_ID, "Executing operation "+route.method + " "+route.url + " "+params);

		if(SIMULATE_OFFLINE_CONNECTIVITY_ON_TESTING){
			return result.setError(new PillowError(new NoConnectionError()));
		}
		Listener<AuthenticationData> onSessionStarted = new Listener<AuthenticationData>() {
			@Override
			public void onResponse(AuthenticationData sessionData) {
			Map<String, Object> map = sessionData.getData();
			if(params!=null){
				map.putAll(params);
			}
			if(model!=null){
				map.put(restMapping.getModelName(), model);
			}
			
			GsonRequest<T> gsonRequest = new GsonRequest<T>(restMapping.getSerializer(), route, restMapping.getModelClass(), map, result, result, getConfig().getDownloadTimeInterval());
			gsonRequest.setShouldCache(false);
			volleyQueue.add(gsonRequest);
			}
		};
		IPillowResult<AuthenticationData> sessionData = authenticationController.getAuthentication();
		sessionData.setListeners(onSessionStarted, result);
		
		return result;
	}
	
	private <K> IPillowResult<K> execute(OperationRunnable<K> runnable){
		dbThreadPool.execute(runnable);
		return runnable.getProxyResult();
	}
	
	@Override
	public IPillowResult<Collection<T>> index() {
		Route route = restMapping.getIndexPath();
		return executeListOperation(route, null);
	}

	@Override
	public IPillowResult<T> show(T model) {
		Route route = restMapping.getShowPath(model);
		return executeOperation(model, route, null);
	}
	
	@Override
	public IPillowResult<T> create(T model) {
		Route route = restMapping.getCreatePath(model);
		return executeOperation(model, route, null);
	}
	
	@Override
	public IPillowResult<T> update(T model) {
		//@param listener ATENTION: update operation may return empty result on server. This will result in null T in the listener. Return the T from the server if required
		Route route = restMapping.getUpdatePath(model);
		return executeOperation(model, route, null);
		
	}
	
	@Override
	public IPillowResult<Void> destroy(T model) {
		Route route = restMapping.getDestroyPath(model);
		IPillowResult result = executeOperation(model, route, null);
		//Ugly way of changing T to Void...
		return result;
	}
	
	public class VoidListenerProxy implements Listener<T>{
		Listener<Void> listener;
		public VoidListenerProxy(Listener<Void> listener){
			this.listener = listener;
		}
		@Override
		public void onResponse(T response) {
			listener.onResponse(null);
		}
	}
	
	public static class AuthenticationRequiredException extends VolleyError{
		
	}
	
}