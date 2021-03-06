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

package com.mateuyabar.android.pillow.view.forms;


import android.content.Context;
import android.os.Build;
import android.support.v7.widget.GridLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.mateuyabar.android.pillow.Pillow;
import com.mateuyabar.android.pillow.views.R;
import com.mateuyabar.android.pillow.conf.ModelConfiguration;
import com.mateuyabar.android.pillow.data.validator.DefaultValidator;
import com.mateuyabar.android.pillow.data.validator.IValidator;
import com.mateuyabar.android.pillow.data.validator.IValidator.IValidationError;
import com.mateuyabar.android.pillow.data.validator.ValidationErrorUtil;
import com.mateuyabar.android.pillow.view.message.DisplayMessages;
import com.mateuyabar.android.util.MetricUtil;

import java.util.Collection;
import java.util.List;


public class TFormView<T> extends FrameLayout{
	FormInputs formInputs;
	T model;
	boolean editable;
    boolean singleColumn;
	boolean hideLabels = false;
    IValidator<T> validator;
	GridLayout gridLayout;


	public TFormView(Context context, boolean editable) {
		super(context);
		this.editable = editable;
		init();
	}



	private void init() {
        //assuming singleCloumn == false

		//The GridLayout padding from java does not work in some Android versions. Android Bug? We create a dummy layout for this...
		gridLayout = new GridLayout(getContext());

		addView(gridLayout);



		setSingleColumn(singleColumn);
		setFocusable(true);
		setFocusableInTouchMode(true);
		configureLayoutParams();

		setDefaultElevation(this);
	}

	public static void setDefaultElevation(View view){
		//ViewCompat.setElevation(this, MetricUtil.dipToPixels(getContext(), 1));

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			view.setBackgroundColor(view.getResources().getColor(R.color.white));
			view.setElevation(MetricUtil.dipToPixels(view.getContext(), 1));
		} else {
			view.setBackgroundDrawable(view.getResources().getDrawable(R.drawable.rounded_background));
		}
	}

    private void configureLayoutParams(){
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);
        int margin = MetricUtil.dipToPixels(getContext(), 20);
        params.topMargin= margin;
        int marginHorizontal = margin;//getContext().getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        int marginVertical = margin;//getContext().getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin);
		setLayoutParams(params);

		//setPadding(marginHorizontal, marginVertical, marginHorizontal, marginVertical);
		FrameLayout.LayoutParams gridParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);
		gridParams.setMargins(marginHorizontal, marginVertical, marginHorizontal, marginVertical);
		gridLayout.setLayoutParams(gridParams);


    }

    public void setSingleColumn(boolean singleColumn) {
        this.singleColumn = singleColumn;
		if(singleColumn){
			gridLayout.setColumnCount(1);
        } else {
			gridLayout.setColumnCount(2);
        }

    }

	public void setHideLabels(boolean hideLabels) {
		this.hideLabels = hideLabels;
	}

    /**
     * @return validator used. If not specified with a set, the pillow configuration is used or Default validator by default
     */
    private IValidator<T> getValidator() {
        if(validator==null){
            Class modelClass = model.getClass();
            ModelConfiguration modelConf = Pillow.getInstance(getContext()).getModelConfiguration(modelClass);
            if(modelConf!=null)
                validator = modelConf.getValidator();
            else
                validator = new DefaultValidator<T>(modelClass);
        }
        return validator;
    }

    /**
     * Specify an specific validator for the form
     * @param validator
     */
    public void setValidator(IValidator<T> validator) {
        this.validator = validator;
    }

    /**
	 * Returns the model displayed in the fragment.
	 * If validate is set to true, it will check for model validation. If valid will be returned. If not valid null will be returned and errors will be displayed
	 * @param validate if the model must be validated
	 * @return the model or null if validate is true and invalid
	 */
	public T getModel(boolean validate){
		T model = getModel();
		if(validate){
			Class modelClass = model.getClass();

			
			List<IValidationError> errors = getValidator().validate(model);
			if(!errors.isEmpty()){
	            //Error found for now we toast the first one. This could be improved
	            IValidator.IValidationError error = errors.get(0);
	            String string = ValidationErrorUtil.getStringError(getContext(), modelClass, error);
				DisplayMessages.error(getContext(), string);
	            return null;
	        }
		}
		return model;
	}
	
	public T getModel() {
		updateModelFromForm();
		return model;
	}

	public void setModel(T model){
		setModel(model,null);
	}
		
	public void setModel(T model, String[] inputNames) {
		this.model = model;
		formInputs = new FormInputs(model, getContext(), editable);
		formInputs.setInputNames(inputNames);
		generateForm();
	}
	
	private void generateForm() {
		gridLayout.removeAllViews();
		Collection<FormInputRow> inputs = formInputs.getInputs();
		boolean first = true;
		for(FormInputRow rowInput : inputs){
			View label = rowInput.getLabel();
			GridLayout.LayoutParams labelParams = new GridLayout.LayoutParams();
            int rightPadding = getContext().getResources().getDimensionPixelSize(R.dimen.form_label_right_padding);
            int topPadding = label.getPaddingTop();
            if(singleColumn) {
                labelParams.setGravity(Gravity.LEFT);
                if(!first)
                	topPadding = getContext().getResources().getDimensionPixelSize(R.dimen.form_label_single_column_top_padding);
            } else {
                labelParams.setGravity(Gravity.RIGHT);
            }
			label.setLayoutParams(labelParams);
			label.setPadding(label.getPaddingLeft(), topPadding, rightPadding, label.getPaddingBottom());
			
			View input = rowInput.getInput();
			GridLayout.LayoutParams inputParams = new GridLayout.LayoutParams();
            inputParams.setGravity(Gravity.LEFT);
			input.setLayoutParams(inputParams);

			if(hideLabels)
				label.setVisibility(GONE);

			gridLayout.addView(label);
			gridLayout.addView(input);
			first = false;
		}
	}

	private void updateModelFromForm() {
		formInputs.updateModelFromForm();
	}


	
}
