package org.angmarch.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/*
 * Copyright (C) 2015 Angelo Marchesin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@SuppressWarnings("unused")
public abstract class NiceSpinnerBaseAdapter<T> extends BaseAdapter {

    private final PopUpTextAlignment horizontalAlignment;
    private final SpinnerTextFormatter spinnerTextFormatter;

    private int textColor;

    TextView textView;

    int selectedIndex;
    private Drawable backgroundDrawable;

    NiceSpinnerBaseAdapter(
            Context context,
            int textColor,
            Drawable backgroundDrawable,
            SpinnerTextFormatter spinnerTextFormatter,
            PopUpTextAlignment horizontalAlignment
    ) {
        this.spinnerTextFormatter = spinnerTextFormatter;
        this.backgroundDrawable = backgroundDrawable;
        this.textColor = textColor;
        this.horizontalAlignment = horizontalAlignment;
    }

    public void setBackgroundDrawable(Drawable backgroundDrawable) {
        this.backgroundDrawable = backgroundDrawable;
        if (textView != null)
            textView.setBackgroundDrawable(backgroundDrawable);
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        if (textView != null)
            textView.setTextColor(textColor);
    }

    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        Context context = parent.getContext();

        if (convertView == null) {
            convertView = View.inflate(context, R.layout.spinner_list_item, null);
            textView = convertView.findViewById(R.id.text_view_spinner);

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (backgroundDrawable != null)
                textView.setBackgroundDrawable(backgroundDrawable);
//            }
            convertView.setTag(new ViewHolder(textView));
        } else {
            textView = ((ViewHolder) convertView.getTag()).textView;
        }

        textView.setText(spinnerTextFormatter.format(getItem(position)));
        textView.setTextColor(textColor);

        setTextHorizontalAlignment(textView);

        return convertView;
    }

    private void setTextHorizontalAlignment(TextView textView) {
        switch (horizontalAlignment) {
            case START:
                textView.setGravity(Gravity.START);
                break;
            case END:
                textView.setGravity(Gravity.END);
                break;
            case CENTER:
                textView.setGravity(Gravity.CENTER_HORIZONTAL);
                break;
        }
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    void setSelectedIndex(int index) {
        selectedIndex = index;
    }

    public abstract T getItemInDataset(int position);

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public abstract T getItem(int position);

    @Override
    public abstract int getCount();

    static class ViewHolder {
        TextView textView;

        ViewHolder(TextView textView) {
            this.textView = textView;
        }
    }
}
