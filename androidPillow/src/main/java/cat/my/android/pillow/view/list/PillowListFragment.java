package cat.my.android.pillow.view.list;



import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import cat.my.android.pillow.IDataSource;
import cat.my.android.pillow.IdentificableModel;
import cat.my.android.pillow.Listeners.Listener;
import cat.my.android.pillow.Pillow;
import cat.my.android.pillow.R;
import cat.my.android.pillow.util.BundleUtils;
import cat.my.android.pillow.view.NavigationUtil;
import cat.my.android.pillow.view.base.IModelListAdapter;
import cat.my.android.pillow.view.forms.views.FormActivity;
import cat.my.util.exceptions.BreakFastException;


public class PillowListFragment<T extends IdentificableModel> extends Fragment {
	T filter;
	boolean hideButtons;
	Class<T> clazz;
	IModelListAdapter<T> listAdapter;
	IDataSource<T> ops;
	
	Listener<T> refreshListListener = new Listener<T>(){
		@Override
		public void onResponse(T post) {
			listAdapter.refreshList();
		}
	};
	
	public IModelListAdapter<T> getListAdapter() {
		return listAdapter;
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		
		filter = BundleUtils.getModel(getArguments());
		hideButtons = BundleUtils.getHideButtons(getArguments());
		clazz = BundleUtils.getModelClass(getArguments());
		
		View rootView = inflater.inflate(R.layout.list_fragment, container, false);
		
		
		Pillow pillow = Pillow.getInstance(getActivity());
		ops = pillow.getDataSource(clazz);
		

		ListView listview = (ListView) rootView.findViewById(R.id.listview);
		listAdapter = pillow.getViewConfiguration(clazz).getListAdapter(getActivity());
		if(filter!=null)
			listAdapter.setFilter(filter);
		listview.setAdapter(listAdapter);
		
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				T model = listAdapter.getItem(position);
				new NavigationUtil(PillowListFragment.this).displayShowModel(model);
			}
		});
		
		
		ImageButton createButton = (ImageButton)rootView.findViewById(R.id.create_model_button);
		if(hideButtons){
			createButton.setVisibility(View.GONE);
		} else {
			createButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					createModel();
				}
			});
		}
		
		
		return rootView;
	}
	
	public T getFilter() {
		return filter;
	}
	
//	////We are not using menu anymore but floating button
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setHasOptionsMenu(true);
//	};
//	
//	@Override
//	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//		super.onCreateOptionsMenu(menu, inflater);
//		inflater.inflate(R.menu.list_menu, menu);
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle presses on the action bar items
//		if(item.getItemId() == R.id.menu_action_new){
//			createPost();
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}

	@Override
	public void onResume() {
		super.onResume();
		listAdapter.refreshList();
	}
	
	protected void createModel(){
		try {
			Intent intent = new Intent(getActivity(), FormActivity.class);
			T model = clazz.newInstance();
			Bundle bundle = BundleUtils.createIdBundle(model);
			intent.putExtras(bundle);
			getActivity().startActivity(intent);
		} catch (Exception e) {
			new BreakFastException(e);
		}
		
	}
	
	

}