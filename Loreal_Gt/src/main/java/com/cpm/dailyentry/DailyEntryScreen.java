package com.cpm.dailyentry;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.cpm.Constants.CommonString1;
import com.cpm.capitalfoods.R;
import com.cpm.database.GSKDatabase;
import com.cpm.delegates.CoverageBean;
import com.cpm.xmlGetterSetter.JourneyPlanGetterSetter;
import com.cpm.xmlGetterSetter.MappingPromotionGetterSetter;
import com.cpm.xmlGetterSetter.WindowListGetterSetter;
import com.cpm.xmlGetterSetter.WindowSKUEntryGetterSetter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DailyEntryScreen extends AppCompatActivity implements OnItemClickListener,LocationListener{

	Button pjpdeviation,callcycle;
	GSKDatabase database;
	ArrayList<JourneyPlanGetterSetter> jcplist;
	ArrayList<CoverageBean> coverage;
	private SharedPreferences preferences;
	private String date,store_intime,store_id,state_cd,store_type_cd;
	ListView lv;
	//String store_cd;
	 String store_cd;
	private SharedPreferences.Editor editor = null;

	private Dialog dialog;

	String storeVisited=null;

	public static String currLatitude = "0.0";
	public static String currLongitude = "0.0";
	boolean coverageflag= true;
	String user_type;
	boolean result_flag=false,leaveflag=false;
	CardView cardView;
	LinearLayout parent_linear,nodata_linear;
	private ArrayList<WindowListGetterSetter> windowdata = new ArrayList<WindowListGetterSetter>();

	List<WindowSKUEntryGetterSetter> WINDOWSIZE= new ArrayList<WindowSKUEntryGetterSetter>();
	MappingPromotionGetterSetter lgs = null;
	List<MappingPromotionGetterSetter> datalist=new ArrayList<MappingPromotionGetterSetter>();
	String window_cd,window;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.storelistlayout);
		lv=(ListView) findViewById(R.id.list);
		nodata_linear = (LinearLayout) findViewById(R.id.no_data_lay);
		parent_linear = (LinearLayout) findViewById(R.id.parent_linear);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		database = new GSKDatabase(this);
		database.open();

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		date = preferences.getString(CommonString1.KEY_DATE, null);
		store_intime = preferences.getString(CommonString1.KEY_STORE_IN_TIME, "");

		store_id = preferences.getString(CommonString1.KEY_STORE_CD, "");
		//store_type_cd = preferences.getString(CommonString1.KEY_STORE_TYPE_CD, null);
		//state_cd = preferences.getString(CommonString1.KEY_STATE_CD, null);
		editor = preferences.edit();


		user_type = preferences.getString(CommonString1.KEY_USER_TYPE, null);

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

	}
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		jcplist=database.getJCPData(date);
		coverage=database.getCoverageData(date);


		if(jcplist.size()>0){

			setCheckOutData();

			lv.setAdapter(new MyAdapter());
			lv.setOnItemClickListener(this);
		}
		else{
			lv.setVisibility(View.GONE);
			parent_linear.setBackgroundColor((getResources().getColor(R.color.grey_light)));
			nodata_linear.setVisibility(View.VISIBLE);
		}

	}

	public void setCheckOutData(){
		boolean flag=true;
		boolean flag1=true;

		for(int i=0;i<jcplist.size();i++){
			String storeCd=jcplist.get(i).getStore_cd().get(0);
			String GEO_TAG=jcplist.get(i).getGEO_TAG().get(0);
			String state_cd=jcplist.get(i).getSTATE_CD().get(0);
			String store_type_cd =jcplist.get(i).getSTORETYPE_CD().get(0);

			windowdata = database.getWindowListData(state_cd, store_type_cd);

			WINDOWSIZE =database.getwindowdat(storeCd);

			if(!jcplist.get(i).getCheckOutStatus().get(0)
					.equals(CommonString1.KEY_C) && !jcplist.get(i).getCheckOutStatus().get(0)
					.equals(CommonString1.KEY_VALID)){
				
				if(  database.getCOMPETITORData(storeCd).size()>0 && database.getSFTData(storeCd).size()>0   && database.getwindowdat(storeCd).size()>0 ){

					if(GEO_TAG.equalsIgnoreCase(CommonString1.KEY_GEO_N)){

					if(database.getGeotaggingData(storeCd).size()>0 ){

							flag=true;
					}
					else{
						flag=false;
					}
					}
					else{
						flag=true;
					}

					//if(windowdata.size()== WINDOWSIZE.size())
					if(isEachWindowFilled())
					{
						flag1=true;
					}
					else
					{
						flag1=false;
					}

					if(flag&&flag1){
						database.updateStoreStatusOnCheckout(storeCd, date, CommonString1.KEY_VALID);
						jcplist=database.getJCPData(date);
					}
				}
			}

		}

	}

	public boolean isEachWindowFilled()
	{
		boolean isFilled = true;
		for(int i=0;i<windowdata.size();i++)
		{
			if(!database.getwindowDataForEachWindow(store_cd,windowdata.get(i).getWindow_cd().get(0)))
			{
				isFilled = false;
				break;
			}
		}

		return isFilled;
	}

	private class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return jcplist.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();

				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.storelistrow, null);

				holder.storename = (TextView) convertView
						.findViewById(R.id.tvstorename);
				holder.city = (TextView) convertView
						.findViewById(R.id.tvcity);
				holder.keyaccount = (TextView) convertView
						.findViewById(R.id.tvkeyaccount);
				holder.img = (ImageView) convertView
						.findViewById(R.id.img);

				holder.checkout = (Button) convertView
						.findViewById(R.id.chkout);

				holder.checkinclose = (ImageView) convertView
						.findViewById(R.id.closechkin);
				holder.imgtag = (ImageView) convertView
						.findViewById(R.id.imgtag);


				convertView.setTag(holder);

			} else {
				holder = (ViewHolder) convertView.getTag();
			}


			holder.imgtag.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					if(jcplist.get(position).getGEO_TAG().get(0).equalsIgnoreCase(CommonString1.KEY_GEO_Y))
					{
						Snackbar.make(lv, "Store already GEOTAG", Snackbar.LENGTH_SHORT)
								.setAction("Action", null).show();
					}
					else
					{

					}


				}
			});

			holder.checkout.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					AlertDialog.Builder builder = new AlertDialog.Builder(
							DailyEntryScreen.this);
					builder.setMessage("Are you sure you want to Checkout")
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
						public void onClick(
								DialogInterface dialog, int id) {
							if (CheckNetAvailability()) {

								editor = preferences.edit();

								editor.putString(CommonString1.KEY_STORE_CD, jcplist.get(position).getStore_cd().get(0));

								editor.putString(
										CommonString1.KEY_STORE_NAME,
										jcplist.get(position)
										.getStore_name().get(0));

								editor.commit();

								Intent i = new Intent(
										DailyEntryScreen.this,
										CheckOutStoreActivity.class);
								startActivity(i);
							} else {
								Snackbar.make(lv, "No Network", Snackbar.LENGTH_SHORT)
										.setAction("Action", null).show();

							}

						}
					})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
						public void onClick(
								DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
					AlertDialog alert = builder.create();
					alert.show();

				}
			});



			if(jcplist.get(position).getGEO_TAG().get(0).equalsIgnoreCase(CommonString1.KEY_GEO_Y))
			{
				holder.imgtag.setVisibility(View.VISIBLE);
			}
			else
			{
				holder.imgtag.setVisibility(View.GONE);
			}


			String storecd=jcplist.get(position).getStore_cd().get(0);

			if (jcplist.get(position).getUploadStatus().get(0).equals(CommonString1.KEY_D)) {

				holder.img.setVisibility(View.VISIBLE);
				holder.img.setBackgroundResource(R.drawable.tick_d);
				holder.checkout.setVisibility(View.INVISIBLE);
				holder.checkinclose.setVisibility(View.INVISIBLE);


			}
			else if (jcplist.get(position).getUploadStatus().get(0).equals(CommonString1.KEY_U)) {

				holder.img.setVisibility(View.VISIBLE);
				holder.img.setBackgroundResource(R.drawable.tick_u);
				holder.checkout.setVisibility(View.INVISIBLE);
				holder.checkinclose.setVisibility(View.INVISIBLE);


			}
			else if(preferences.getString(CommonString1.KEY_STOREVISITED_STATUS+storecd, "").equals("No")){
				holder.img.setBackgroundResource(R.drawable.leave_tick);
				holder.img.setVisibility(View.VISIBLE);
				holder.checkout.setVisibility(View.INVISIBLE);
				holder.checkinclose.setVisibility(View.INVISIBLE);

			}
			else if ((jcplist.get(position).getCheckOutStatus().get(0)
					.equals(CommonString1.KEY_C))) {

				holder.img.setVisibility(View.INVISIBLE);
				holder.checkinclose.setBackgroundResource(R.drawable.tick_c);
				holder.checkinclose.setVisibility(View.VISIBLE);
				holder.checkout.setVisibility(View.INVISIBLE);

			}
			else if ((jcplist.get(position).getCheckOutStatus().get(0)
					.equals(CommonString1.KEY_VALID))) {

				holder.checkout.setBackgroundResource(R.drawable.checkout);
				holder.checkout.setVisibility(View.VISIBLE);
				holder.checkout.setEnabled(true);
				holder.checkinclose.setVisibility(View.INVISIBLE);
				holder.img.setVisibility(View.INVISIBLE);
			}
			else if ((jcplist.get(position).getCheckOutStatus().get(0)
					.equals(CommonString1.KEY_INVALID))) {

				if (coverage.size() > 0) {

					int i;

					for (i = 0; i < coverage.size(); i++) {


						if (coverage.get(i).getInTime() != null) {

							if (coverage.get(i).getOutTime() == null) {


								if (storecd.equals(coverage.get(i).getStoreId())) {

									holder.img.setVisibility(View.INVISIBLE);
									holder.checkout.setEnabled(false);
									holder.checkout.setVisibility(View.INVISIBLE);
									holder.checkinclose.setBackgroundResource(R.drawable.checkin_ico);
									holder.checkinclose.setVisibility(View.VISIBLE);
								}
								break;
							}
						}
					}

				}
			}

			else {
				holder.checkout.setEnabled(false);
				holder.checkout.setVisibility(View.INVISIBLE);
				holder.img.setVisibility(View.INVISIBLE);
				holder.img.setBackgroundResource(R.drawable.store);

				holder.checkinclose.setEnabled(false);
				holder.checkinclose.setVisibility(View.INVISIBLE);

			}

			holder.storename.setText(jcplist.get(position).getStore_name().get(0));
			holder.city.setText(jcplist.get(position).getCity().get(0));
			holder.keyaccount.setText(jcplist.get(position).getKey_account().get(0));

			return convertView;
		}

		private class ViewHolder {
			TextView storename, city,keyaccount;
			ImageView img,checkinclose,imgtag;

			Button checkout;
		}

	}

	@Override
	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
	}



	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		// TODO Auto-generated method stub

		store_cd=jcplist.get(position).getStore_cd().get(0);
		final String upload_status=jcplist.get(position).getUploadStatus().get(0);
		final String checkoutstatus=jcplist.get(position).getCheckOutStatus().get(0);
		final String STORETYPE_CD=jcplist.get(position).getSTORETYPE_CD().get(0);
		final String STATE_CD=jcplist.get(position).getSTATE_CD().get(0);
		final String GeoTag=jcplist.get(position).getGEO_TAG().get(0);

		editor = preferences.edit();

		editor.putString(CommonString1.KEY_GEO_TAG, GeoTag);
		editor.putString(CommonString1.KEY_STORE_TYPE_CD, STORETYPE_CD);
		editor.putString(CommonString1.KEY_STATE_CD, STATE_CD);
		editor.commit();

		if(upload_status.equals(CommonString1.KEY_D)){

			Snackbar.make(lv, "All Data Uploaded", Snackbar.LENGTH_SHORT)
					.setAction("Action", null).show();
		}
		else if (((checkoutstatus.equals(CommonString1.KEY_C)))) {

			Snackbar.make(lv, "Store already checked out", Snackbar.LENGTH_SHORT)
					.setAction("Action", null).show();
		}else if(preferences.getString(CommonString1.KEY_STOREVISITED_STATUS+store_cd, "").equals("No")){

			Snackbar.make(lv, "Store Already Closed", Snackbar.LENGTH_SHORT)
					.setAction("Action", null).show();
		}
		else{
			if(!setcheckedmenthod(store_cd)){

				boolean enteryflag = true;

				if(coverage.size()>0) {

					int i;

					for (i = 0; i < coverage.size(); i++) {


						if (coverage.get(i).getInTime() != null) {

							if (coverage.get(i).getOutTime() == null) {
								if (!store_cd.equals(coverage.get(i).getStoreId())) {

									Snackbar.make(lv, "Please checkout from current store", Snackbar.LENGTH_SHORT)
											.setAction("Action", null).show();
									enteryflag =false;
								}
								break;
							}
						}
					}
				}

				if(enteryflag){
					showMyDialog(store_cd, jcplist.get(position).getStore_name().get(0), "Yes", jcplist.get(position).getVISIT_DATE().get(0), jcplist.get(position).getCheckOutStatus().get(0),jcplist.get(position).getCHANNEL_CD().get(0));


				}

			}
			else {

				Snackbar.make(lv, "Data already filled ", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
			}

			}

	}




	public String getCurrentTimeX() {

		Calendar m_cal = Calendar.getInstance();
		int hour = m_cal.get(Calendar.HOUR_OF_DAY);
		int min = m_cal.get(Calendar.MINUTE);

		String intime = "";

		if (hour == 0) {
			intime = "" + 12 + ":" + min + " AM";
		} else if (hour == 12) {
			intime = "" + 12 + ":" + min + " PM";
		} else {

			if (hour > 12) {
				hour = hour - 12;
				intime = "" + hour + ":" + min + " PM";
			} else {
				intime = "" + hour + ":" + min + " AM";
			}
		}
		return intime;
	}

	public String getCurrentTime() {

		Calendar m_cal = Calendar.getInstance();

		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		String cdate = formatter.format(m_cal.getTime());

		return cdate;

	}

	public boolean CheckNetAvailability() {

		boolean connected = false;
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
				.getState() == NetworkInfo.State.CONNECTED
				|| connectivityManager.getNetworkInfo(
						ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
			// we are connected to a network
			connected = true;
		}
		return connected;
	}

	void showMyDialog(final String storeCd, final String storeName, final String status, final String visitDate, final String checkout_status, final String channel_cd){
		dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialogbox);
		// dialog.setTitle("About Android Dialog Box");
		RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.radiogrpvisit);

		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// find which radio button is selected
				if(checkedId == R.id.yes) {
					editor = preferences.edit();

					editor.putString(CommonString1.KEY_STOREVISITED, storeCd);
					editor.putString(CommonString1.KEY_STOREVISITED_STATUS, "Yes");
					editor.putString(CommonString1.KEY_LATITUDE, currLatitude);
					editor.putString(CommonString1.KEY_LONGITUDE, currLongitude);
					editor.putString(CommonString1.KEY_STORE_NAME, storeName);
					editor.putString(CommonString1.KEY_STORE_CD, storeCd);
					editor.putString(CommonString1.KEY_CHANNEL_CD, channel_cd);

					if(!visitDate.equals("")){
						editor.putString(CommonString1.KEY_VISIT_DATE, visitDate);
					}
				
					if(status.equals("Yes")){
						editor.putString(CommonString1.KEY_STOREVISITED_STATUS, "Yes");
					}
					
					database.updateStoreStatusOnCheckout(storeCd, date, CommonString1.KEY_INVALID);
					
					editor.commit();
					
					if (store_intime.equalsIgnoreCase("")) {

						SharedPreferences.Editor editor = preferences.edit();
						editor.putString(CommonString1.KEY_STORE_IN_TIME,
								getCurrentTime());
						editor.putString(CommonString1.KEY_STOREVISITED_STATUS, "Yes");

						editor.commit();

					}

					dialog.cancel();

					boolean flag=true;

					if(coverage.size()>0) {

						for (int i = 0; i < coverage.size(); i++) {

							if (store_cd.equals(coverage.get(i).getStoreId())) {
								flag=false;
								break;
							}
						}
					}
					if(flag==true)
					{
						Intent in  = new Intent(DailyEntryScreen.this, StoreimageActivity.class);
						startActivity(in);
						overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
					}
					else
					{
						if(channel_cd.equalsIgnoreCase("1"))
						{
							Intent in=new Intent(DailyEntryScreen.this,StoreEntry.class);
							startActivity(in);
						}
						else
						{
							Intent in=new Intent(DailyEntryScreen.this,StoreEntryForChannel2Activity.class);
							startActivity(in);
						}


					}
				} else if(checkedId == R.id.no) {
					dialog.cancel();
					
					if(checkout_status.equals(CommonString1.KEY_INVALID) || checkout_status.equals(CommonString1.KEY_VALID)){
						AlertDialog.Builder builder = new AlertDialog.Builder(DailyEntryScreen.this);
						builder.setMessage(CommonString1.DATA_DELETE_ALERT_MESSAGE)
						.setCancelable(false) 
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int id) {
								
								UpdateData(storeCd);
								
								SharedPreferences.Editor editor = preferences
										.edit();
								editor.putString(CommonString1.KEY_STORE_CD, storeCd);
								editor.putString(
										CommonString1.KEY_STORE_IN_TIME,
										"");
								editor.putString(
										CommonString1.KEY_STOREVISITED,
										"");
								editor.putString(
										CommonString1.KEY_STOREVISITED_STATUS,
										"");
								editor.putString(
										CommonString1.KEY_LATITUDE, "");
								editor.putString(
										CommonString1.KEY_LONGITUDE, "");

								editor.commit();


								Intent in  = new Intent(DailyEntryScreen.this, NonWorkingReason.class);
								startActivity(in);
								
							}
						})
						.setNegativeButton("No",
								new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int id) {
							
								
								dialog.cancel();
							}
						});
						AlertDialog alert = builder.create();

						alert.show();
					}
					else{
						UpdateData(storeCd);

						SharedPreferences.Editor editor = preferences
								.edit();
						editor.putString(CommonString1.KEY_STORE_CD, storeCd);
						editor.putString(
								CommonString1.KEY_STORE_IN_TIME,
								"");
						editor.putString(
								CommonString1.KEY_STOREVISITED,
								"");
						editor.putString(
								CommonString1.KEY_STOREVISITED_STATUS,
								"");
						editor.putString(
								CommonString1.KEY_LATITUDE, "");
						editor.putString(
								CommonString1.KEY_LONGITUDE, "");

						editor.commit();
						
						Intent in  = new Intent(DailyEntryScreen.this, NonWorkingReason.class);
						startActivity(in);
					}
					//finish();
				} 
			}

		});

		dialog.show();
	}


	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		currLatitude = Double.toString(location.getLatitude());
		currLongitude = Double.toString(location.getLongitude());
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}



	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

	public void UpdateData(String storeCd) {

		database.open();
		database.deleteSpecificStoreData(storeCd);
		database.updateStoreStatusOnCheckout(storeCd, jcplist.get(0).getVISIT_DATE().get(0),
				"N");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.empty_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if(id==android.R.id.home){
			finish();
			overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
		}
		return super.onOptionsItemSelected(item);
	}

	public boolean setcheckedmenthod(String store_cd)
	{
		for(int i=0;i<coverage.size();i++) {

			if (store_cd.equals(coverage.get(i).getStoreId())) {

				if(coverage.get(i).getOutTime()!=null)
				{
					result_flag=true;

					break;
				}
			}

			else
			{
				result_flag=false;
			}
		}
		return result_flag;
	}

}
