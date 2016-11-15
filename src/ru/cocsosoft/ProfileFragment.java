package ru.cocsosoft;

import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.ListFragment;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

public class ProfileFragment extends ListFragment implements OnClickListener,
		AnimationListener {

	private SimpleCursorAdapter dataAdapter;
	private View profileView;
	Context appCont;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {

		if (parent == null) {
			// We have different layouts, and in one of them this
			// fragment's containing frame doesn't exist. The fragment
			// may still be created from its saved state, but there is
			// no reason to try to create its view hierarchy because it
			// won't be displayed. Note this is not needed -- we could
			// just run the code below, where we would create and return
			// the view hierarchy; it would just never be used.
			return null;
		}

		profileView = inflater.inflate(R.layout.profile, null);
		
		appCont = (ThatBetter) getActivity().getApplicationContext();
		
		super.onCreateView(inflater, parent, savedInstanceState);

		DisplayListView();

		return profileView;
	}

	// http://www.mysamplecode.com/2012/07/android-listview-cursoradapter-sqlite.html

	public void DisplayListView() {
		// The desired columns to be bound
		String[] columns = new String[] { TBDB.CREATED,
				TBDB.IMGFILE1, TBDB.IMGFILE2, TBDB.VOTES1, TBDB.VOTES2 };

		// the XML defined views which the data will be bound to
		int[] to = new int[] { R.id.created, R.id.filename1,
				R.id.filename2, R.id.votes1, R.id.votes2 };

		// create the adapter using the cursor pointing to the desired data
		// as well as the layout information
		try {

			dataAdapter = new SimpleCursorAdapter(getActivity()
					.getApplicationContext(), R.layout.profile_item,
					ThatBetter.db.loadLocalBoxesCursors(true), columns, to, 0);

			dataAdapter.setViewBinder(new ViewBinder() {
/*
                public void setViewText(TextView v, String text)
                {
                    if (v.getId() == R.id.created) {

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        java.util.Date created = new java.util.Date(0);

                        TextView viewDate = (TextView) v;

                        try {
                            created = dateFormat.parse(viewDate.getText().toString());
                            viewDate.setText(created.toString());
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
*/

			    public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex) {

                     if (aView.getId() == R.id.created) {

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        java.util.Date created = new java.util.Date(0);
                        TextView viewDate = (TextView) aView;

                        try {
                            created = dateFormat.parse(aCursor.getString(aColumnIndex));
                            viewDate.setText(DateFormat.getLongDateFormat(getActivity().getApplicationContext()).format(created).toString());
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }

                        return true;
                    }

			        if (aView.getId() == R.id.votes1 || aView.getId() == R.id.votes2) {
			        	TextView viewItemVotes = (TextView) aView;
		                String numItemVotes = getActivity().getApplicationContext().getString(R.string.voteCount) + ": " + aCursor.getString(aColumnIndex);
		                viewItemVotes.setText( numItemVotes);
                        return true;
			        }
			        
			        if (aView.getId() == R.id.filename1 || aView.getId() == R.id.filename2) {
			                String imageFileName = aCursor.getString(aColumnIndex);
			                ImageView imageView = (ImageView) aView;
			                
			                File imgFile = new File(LocalFile.getFullPath(imageFileName,appCont));
			                
			                if(imgFile.exists())
			                	imageView.setImageURI(Uri.fromFile(imgFile));
			                else
			    				Log.d(ThatBetterActivity.TAG, LocalFile.getFullPath(imageFileName,appCont) + " does not exist.");

			                return true;
			         }

			         return false;
			    }
			});
			
			ListView listView = (ListView) profileView
					.findViewById(R.id.profile_list);
			// Assign adapter to ListView
			listView.setAdapter(dataAdapter);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onAnimationEnd(Animation arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

}
