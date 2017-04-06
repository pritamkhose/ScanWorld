package com.pritam.scanworld;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SimpleAdapterBGColor extends SimpleAdapter {
    private List<HashMap<String, String>> items = new ArrayList();
    private Context context;

    public SimpleAdapterBGColor(Context context, List<HashMap<String, String>> items, int resource, String[] from, int[] to) {
        super(context, items, resource, from, to);
        this.items = items;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View view = super.getView(position, convertView, parent);
   	  ((TextView) view.findViewById(R.id.txt)).setTypeface(null, Typeface.BOLD);
	  ((TextView) view.findViewById(R.id.cur)).setTypeface(null, Typeface.NORMAL);

	  if(items.get(position).get("color").equals("0"))
      {
		  view.setBackgroundColor(context.getResources().getColor(R.color.white));
     }else
          view.setBackgroundColor(context.getResources().getColor(R.color.blue));
      
      return view;
    }
}