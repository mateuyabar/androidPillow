package cat.my.android.restvolley.forms.inputDatas;

import android.content.Context;
import android.view.View;
import cat.my.android.restvolley.forms.InputData;

public abstract class AbstractInputData implements InputData{
	View view;
	
	@Override
	public View getView(Context context){
		if(view == null){
			view = createView(context);
		}
		return view;
	}
	
	protected View getView(){
		return view;
	}

	protected abstract View createView(Context context);
}
