package jadex.android.clientapp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import jadex.android.standalone.clientapp.ClientAppFragment;

public class MySendActivity extends ClientAppFragment
{
	
	private String path;
	private Uri uri;

	@Override
	public void onPrepare(Activity mainActivity)
	{
		super.onPrepare(mainActivity);
		Intent intent = getIntent();
		String action = intent.getAction();
		System.out.println("Intent action: " + action);
		
		Bundle extras = intent.getExtras();

		String text = (String) extras.get(Intent.EXTRA_TEXT);
		uri = (Uri) extras.get(Intent.EXTRA_STREAM);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		setTitle(R.string.app_sendtitle);
		View inflate = inflater.inflate(R.layout.sendlayout, container);
		
		TextView textView = (TextView) inflate.findViewById(R.id.sendActivityTextView);
		
		textView.setText("This is the send activity. File path: " + path);
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//
		// Convert the image URI to the direct
		// file system path of the image file
		String[] proj = new String[]
		{ MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, proj, // Which
												// columns
												// to
												// return
				null, // WHERE clause; which
						// rows to return (all
						// rows)
				null, // WHERE clause selection
						// arguments (none)
				null); // Order-by clause
						// (ascending by name)
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		final String path = cursor.getString(column_index);

		this.path = path;
	}

}
