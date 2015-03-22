package com.jfsanda.sunshine;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by jfsanda on 9/3/15.
 */
public class ViewHolder {

   static ImageView icon;
   static TextView dateTextView;
   static TextView forecastTextView;
   static TextView highView;
   static TextView lowView;


    public ViewHolder(View view) {
        icon = (ImageView)view.findViewById(R.id.list_item_icon);
        dateTextView = (TextView)view.findViewById(R.id.list_item_date_textview);
        forecastTextView = (TextView)view.findViewById(R.id.list_item_forecast_textview);
        highView = (TextView) view.findViewById(R.id.list_item_high_textview);
        lowView = (TextView)view.findViewById(R.id.list_item_low_textview);
    }

}
