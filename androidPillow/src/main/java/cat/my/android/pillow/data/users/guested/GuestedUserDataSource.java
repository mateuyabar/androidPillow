package cat.my.android.pillow.data.users.guested;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.Request.Method;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import cat.my.android.pillow.Listeners.Listener;
import cat.my.android.pillow.Pillow;
import cat.my.android.pillow.data.core.IPillowResult;
import cat.my.android.pillow.data.core.PillowResult;
import cat.my.android.pillow.data.core.PillowResultListener;
import cat.my.android.pillow.data.core.PillowResultProxyType;
import cat.my.android.pillow.data.rest.IAuthenticationController;
import cat.my.android.pillow.data.rest.IRestMapping;
import cat.my.android.pillow.data.rest.RestDataSource;
import cat.my.util.exceptions.BreakFastException;

/**
 * DataSource for a User that enables guest users. #GuestedUserDataSource.getAuthentication returns the authenticationController
 * The authentication works the following:
 *
 * 1. If nothing done it will try to create a guest user (signUpAsGuest).
 * 2. It it registers, the guest account will be 'upgraded' to registered signUp
 * 3. If signed in, the guest user will be discarted
 *
 * @param <T>
 */
public class GuestedUserDataSource<T extends IGuestedUser> extends RestDataSource<T> implements IAuthenticationController{
	SharedPreferences sharedPref;
	private static final String AUTH_TOKEN = "logged_auth_token";
	private static final String USER_DATA = "logged_user_data";
    private static final String LOGGED_VERSION = "logged_version";
    private static final String AUTH_TOKEN_SESSION_PARAM = "auth_token";
	
//	RestDataSource<T> userDataSource;
	Context context;
    Class<T> userClass;
    int version = 0;

    public GuestedUserDataSource(Context context, IRestMapping<T> restMapping){
        this(context, restMapping, 0);
    }
	
	public GuestedUserDataSource(Context context, IRestMapping<T> restMapping, int version){
		super(restMapping, context);
		this.context = context;
		//userDataSource = new RestDataSource<T>(restMapping, context);
		String preferencesFileKey = Pillow.PREFERENCES_FILE_KEY;
		sharedPref = context.getSharedPreferences(preferencesFileKey, Context.MODE_PRIVATE);
        userClass= getRestMapping().getModelClass();

        this.version=version;
        checkAuthenticationVersion();
	}
	
	/**
	 * Creates a guest user.
	 * @return
	 */
	private T createGuestUser(){
		T user;
		try {
			user = userClass.newInstance();
		} catch (Exception e) {
			throw new BreakFastException(e);
		}
		user.setGuest(true);
		return user;
	}
	
	/**
	 * Should be called just after created.
	 * It connects to the server and authenticates (if authentication stored) or creates a guest user
	 */
	public IPillowResult<Void> init(){
//		resetInTesting();
		String token = getAuthToken();
		if(token==null){
			return new PillowResultProxyType<Void, T>(context, null, signUpAsGuest());
		} else {
			return PillowResult.newVoidResult(context);
		}
	}
	
	/**
	 * Creates a guest user on the server
	 * @return 
	 */
	private IPillowResult<T> signUpAsGuest(){
		final PillowResultListener<T> result = new PillowResultListener<T>(context);
		Listener<T> onCreateListener = new Listener<T>() {
			@Override
			public void onResponse(T response) {
				storeAuthToken(response);
				result.setResult(response);
			}
		};
		create(createGuestUser()).setListeners(onCreateListener, result);
		return result;
	}
	
	/**
	 * Signs up to the application. (updates user and password for previous guest user )
	 * @param user
	 * @return 
	 */
	public IPillowResult<T> signUp(T user){
		final PillowResultListener<T> result = new PillowResultListener<T>(context);
		user.setAuthToken(getAuthToken());
		Listener<T> onSignUpListener = new Listener<T>() {
			@Override
			public void onResponse(T response) {
				storeAuthToken(response);
				result.setResult(response);
			}
		};
        executeCollectionOperation(user, Method.POST, "sign_up", null).setListeners(onSignUpListener, result);
		return result;
	}
	
	/**
	 * Signs in to the application. It will try to sign-in to the server, and update the current user accordingly.
	 * Data stored (if any) will be reloaded.
	 * 
	 * @param user
	 * @return 
	 */
	public PillowResultListener<Void> signIn(T user){
		final PillowResultListener<Void> result = new PillowResultListener<Void>(context);
		
		Listener<T> onSignInListener = new Listener<T>() {
			@Override
			public void onResponse(T response) {
				storeAuthToken(response);
				reloadData().setListeners(result,result);
			}
		};
		executeCollectionOperation(user, Method.POST, "sign_in", null).setListeners(onSignInListener, result);
		return result;
	}
	
	public PillowResult<Void> signOut() {
		final PillowResultListener<Void> result = new PillowResultListener<Void>(context);
		Listener<T> onCreateListener = new Listener<T>() {
			@Override
			public void onResponse(T response) {
				storeAuthToken(response);
				reloadData().setListeners(result,result);
			}
		};
		create(createGuestUser()).setListeners(onCreateListener, result);
		return result;
	}
	
	/**
	 * Called after a user has signed in to one accout.
	 * Deletes all the user realated data and donwload the new user one. 
	 * @return 
	 */
	protected IPillowResult<Void> reloadData(){
		return Pillow.getInstance().getSynchManager().reloadData();
	}
	
	
	public Context getContext() {
		return context;
	}
	
	private void checkAuthenticationVersion() {
		int version = getAuthenticationVersion();
		int current = sharedPref.getInt(LOGGED_VERSION, 0);
		if(current<version){
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.remove(AUTH_TOKEN);
            editor.remove(USER_DATA);
			editor.putInt(LOGGED_VERSION, version);
			editor.commit();
		}
	}


	protected int getAuthenticationVersion() {
		return version;
	}

	/**
	 * Stores the given auth_token in the shared preferences
	 * @param user
	 */
	private void storeAuthToken(T user) {
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(AUTH_TOKEN, user.getAuthToken());
		String userJson = new Gson().toJson(user);
		editor.putString(USER_DATA, userJson);
		editor.commit();
	}
	
	/**
	 * @return stored auth_token in the shared preferences
	 */
	public String getAuthToken(){
		return sharedPref.getString(AUTH_TOKEN, null);
	}
	
	public T getLoggedUser(){
		String userJson = sharedPref.getString(USER_DATA, null);
		if(userJson==null)
			return null;
		return new Gson().fromJson(userJson, getRestMapping().getModelClass());
	}

	@Override
	public IPillowResult<AuthenticationData> getAuthentication() {
		final PillowResultListener<AuthenticationData> result = new PillowResultListener<AuthenticationData>(context);

		Listener<Void> onInitListener = new Listener<Void>() {
				@Override
				public void onResponse(Void response) {
				Map<String, Object> session = new HashMap<String, Object>();
				String authToken = getAuthToken();
				if(authToken!=null){
					session.put(AUTH_TOKEN_SESSION_PARAM, authToken);
				}
				result.setResult(new AuthenticationData(session));
			}
			};

		init().setListeners(onInitListener, result);

		return result;
	}

}