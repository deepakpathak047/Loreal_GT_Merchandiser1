package com.cpm.dailyentry;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cpm.Constants.CommonString1;
import com.cpm.GetterSetter.NavMenuItemGetterSetter;
import com.cpm.capitalfoods.R;
import com.cpm.database.GSKDatabase;
import com.cpm.geotag.LocationActivity;
import com.cpm.xmlGetterSetter.MiddayStockInsertData;
import com.cpm.xmlGetterSetter.OpeningStockInsertDataGetterSetter;
import com.cpm.xmlGetterSetter.StockGetterSetter;
import com.cpm.xmlGetterSetter.WindowListGetterSetter;
import com.cpm.xmlGetterSetter.WindowSKUEntryGetterSetter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class StoreEntry extends AppCompatActivity implements OnClickListener{

	Button btnDeepfreez,btnOpeningStock,btnClosingStock,btnMiddayStock,btnPromotion,btnAsset,btnfoodstr,btnfacingcomp,btncalls;
   Button performance;
	GSKDatabase db;
	private SharedPreferences preferences;
	String store_cd,state_cd,store_type_cd;
	
	boolean food_flag,user_flag=false;
	
	String user_type="",GEO_TAG="",FIRST_VISIT="";

	private ArrayList<StockGetterSetter> stockData = new ArrayList<StockGetterSetter>();
	
	HashMap<OpeningStockInsertDataGetterSetter, List<MiddayStockInsertData>> listDataChild;
	
	List<OpeningStockInsertDataGetterSetter> listDataHeader;
	ArrayList<MiddayStockInsertData> skuData;
	
	LinearLayout layout_mid_close,gap_layout,gap_calls,food_layout;

	ValueAdapter adapter;
	boolean count;
	RecyclerView recyclerView;
	private ArrayList<WindowListGetterSetter> windowdata = new ArrayList<WindowListGetterSetter>();

	List<WindowSKUEntryGetterSetter> WINDOWSIZE= new ArrayList<WindowSKUEntryGetterSetter>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.menu_item_recycle_layout);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		db=new GSKDatabase(getApplicationContext());
		db.open();

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		store_cd = preferences.getString(CommonString1.KEY_STORE_CD, null);
		store_type_cd = preferences.getString(CommonString1.KEY_STORE_TYPE_CD, null);

		state_cd = preferences.getString(CommonString1.KEY_STATE_CD, null);
		food_flag=preferences.getBoolean(CommonString1.KEY_FOOD_STORE, false);

		GEO_TAG=preferences.getString(CommonString1.KEY_GEO_TAG, null);

		user_type = preferences.getString(CommonString1.KEY_USER_TYPE, null);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();


		windowdata = db.getWindowListData(state_cd, store_type_cd);

		WINDOWSIZE =db.getwindowdat(store_cd);

		recyclerView=(RecyclerView) findViewById(R.id.drawer_layout_recycle);

		adapter=new ValueAdapter(getApplicationContext(),getdata());
		recyclerView.setAdapter(adapter);
		recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));

	}




	public void validate(){
		
		db.open();
		
		if(db.isClosingDataFilled(store_cd)){
			btnClosingStock.setBackgroundResource(R.drawable.closing_stock_done);
		}
		else{
			btnClosingStock.setBackgroundResource(R.drawable.closing_stock);
		}
		
		if(db.isCompetitionDataFilled(store_cd)){
			btnfacingcomp.setBackgroundResource(R.drawable.competition_done);
		}

		if(db.isMiddayDataFilled(store_cd)){
			btnMiddayStock.setBackgroundResource(R.drawable.mid_stock_done);
		}
		else{
			btnMiddayStock.setBackgroundResource(R.drawable.mid_stock);
		}


		if(db.isOpeningDataFilled(store_cd)){
			btnOpeningStock.setBackgroundResource(R.drawable.opening_stock_done);
		}
		else{
			btnOpeningStock.setBackgroundResource(R.drawable.opening_stock);
		}
		
		if(db.isPOSMDataFilled(store_cd)){
			btnAsset.setBackgroundResource(R.drawable.asset_done);
		}
		if(db.isWINDOWSDataFilled(store_cd)){
			btnPromotion.setBackgroundResource(R.drawable.promotion);
		}
	}


	@Override
	public void onBackPressed() {
		/*Intent i = new Intent(this, DailyEntryScreen.class);
		startActivity(i);*/
		
		finish();
		
		
		overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
	}


	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub

		int id=view.getId();

		switch (id) {
		case R.id.openingstock:
			
			if(!db.isClosingDataFilled(store_cd)){

					
				Intent in1=new Intent(getApplicationContext(),StockAvailability2Activity.class);

					startActivity(in1);

					overridePendingTransition(R.anim.activity_in, R.anim.activity_out);

			}
			else{
				Toast.makeText(getApplicationContext(), "Data cannot be changed", Toast.LENGTH_SHORT).show();
			}
			
			
			break;

		case R.id.closingstock:
			
			stockData=db.getOpeningStock(store_cd);
			if((stockData.size()<=0) || (stockData.get(0).getOpen_stock_cold_room()==null) || (stockData.get(0).getOpen_stock_cold_room().equals(""))){
			
				Toast.makeText(getApplicationContext(),
						"First Fill Opening Stock and Midday Stock Data", Toast.LENGTH_SHORT).show();
			
		}
		else{
			stockData=db.getMiddayStock(store_cd);
			
			if((stockData.size()<=0) || (stockData.get(0).getMidday_stock()==null) || (stockData.get(0).getMidday_stock().equals(""))){
				
				Toast.makeText(getApplicationContext(),
						"First Fill Opening Stock and Midday Stock Data", Toast.LENGTH_SHORT).show();
				
				}
			else{
	
				Intent in2=new Intent(getApplicationContext(),ClosingStock.class);

				startActivity(in2);

				overridePendingTransition(R.anim.activity_in, R.anim.activity_out);

			}
		}

			
			break;


		case R.id.midstock:

			if(!db.isClosingDataFilled(store_cd)){
				
				Intent in3=new Intent(getApplicationContext(),MidDayStock.class);

				startActivity(in3);

				overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
			}
			else{
				Toast.makeText(getApplicationContext(), "Data cannot be changed", Toast.LENGTH_SHORT).show();
			}
			
			

			break;
			
		case R.id.prommotion:

			Intent in4=new Intent(getApplicationContext(),WindowsActivity.class);

			startActivity(in4);

			overridePendingTransition(R.anim.activity_in, R.anim.activity_out);

			break;
			
		case R.id.assets:

			Intent in5=new Intent(getApplicationContext(),AssetActivity.class);

			startActivity(in5);

			overridePendingTransition(R.anim.activity_in, R.anim.activity_out);

			break;
			
		case R.id.foodstore:

			Intent in6=new Intent(getApplicationContext(),FoodStore.class);

			startActivity(in6);

			overridePendingTransition(R.anim.activity_in, R.anim.activity_out);

			break;
			
		case R.id.facingcompetitor:

			Intent in7=new Intent(getApplicationContext(),FacingCompetitor.class);

			startActivity(in7);

			overridePendingTransition(R.anim.activity_in, R.anim.activity_out);

			break;
			
		case R.id.calls:

			Intent in8=new Intent(getApplicationContext(),CallsActivity.class);

			startActivity(in8);

			overridePendingTransition(R.anim.activity_in, R.anim.activity_out);

			break;
			
			
		case R.id.performance:
			
			Intent startPerformance = 	new Intent(StoreEntry.this,Performance.class);
			startActivity(startPerformance);
			 overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
		}

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

			// NavUtils.navigateUpFromSameTask(this);
			finish();

			overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);

		}

		return super.onOptionsItemSelected(item);
	}

	public class ValueAdapter extends RecyclerView.Adapter<ValueAdapter.MyViewHolder>{

		private LayoutInflater inflator;

		List<NavMenuItemGetterSetter> data= Collections.emptyList();

		public ValueAdapter(Context context, List<NavMenuItemGetterSetter> data){

			inflator = LayoutInflater.from(context);
			this.data=data;

		}

		@Override
		public ValueAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int i) {

			View view=inflator.inflate(R.layout.custom_row,parent,false);

			MyViewHolder holder=new MyViewHolder(view);

			return holder;
		}

		@Override
		public void onBindViewHolder(final ValueAdapter.MyViewHolder viewHolder, final int position) {

			final NavMenuItemGetterSetter current=data.get(position);

			//viewHolder.txt.setText(current.txt);

			viewHolder.icon.setImageResource(current.getIconImg());
			viewHolder.icon.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					if(current.getIconImg()==R.drawable.stock1 || current.getIconImg()==R.drawable.stock_done1){

						if(!db.isClosingDataFilled(store_cd)){


							Intent in1=new Intent(getApplicationContext(),StockAvailability2Activity.class);
							//Intent in1=new Intent(getApplicationContext(),StockAvailability.class);

							startActivity(in1);

							overridePendingTransition(R.anim.activity_in, R.anim.activity_out);

						}
						else{

							Snackbar.make(recyclerView, "Data cannot be changed", Snackbar.LENGTH_SHORT).show();

						}


					}
					if(current.getIconImg()==R.drawable.midday_stock || current.getIconImg()==R.drawable.midday_stock_done){

						if(!db.isClosingDataFilled(store_cd)){

							Intent in3=new Intent(getApplicationContext(),MidDayStock.class);

							startActivity(in3);

							overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
						}
						else{

							Snackbar.make(recyclerView, "Data cannot be changed", Snackbar.LENGTH_SHORT).show();

						}


					}
					if(current.getIconImg()==R.drawable.windows || current.getIconImg()==R.drawable.window_done){
						Intent in4=new Intent(getApplicationContext(),SecondaryWindowActivity.class);

						startActivity(in4);

						overridePendingTransition(R.anim.activity_in, R.anim.activity_out);

					}
					if(current.getIconImg()==R.drawable.posm1 || current.getIconImg()==R.drawable.posm_done1){
						Intent in5=new Intent(getApplicationContext(),POSMActivity.class);

						startActivity(in5);

						overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
					}


					if(current.getIconImg()==R.drawable.geotag1 || current.getIconImg()==R.drawable.geotag_done1){


						if(GEO_TAG.equalsIgnoreCase(CommonString1.KEY_GEO_Y))
						{
							Toast.makeText(getApplicationContext(),"GoeTag Already Done",Toast.LENGTH_LONG).show();
						}

						else if(db.getGeotaggingData(store_cd).size()>0){

							 if(db.getGeotaggingData(store_cd).get(0).getGEO_TAG().equalsIgnoreCase(CommonString1.KEY_GEO_Y))
							{
								Toast.makeText(getApplicationContext(),"GoeTag Already Done",Toast.LENGTH_LONG).show();
							}
						}



						else
						{
							Intent in4=new Intent(getApplicationContext(),LocationActivity.class);

							startActivity(in4);

							overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
						}


					}


					if(current.getIconImg()==R.drawable.closing_stock || current.getIconImg()==R.drawable.closing_stock_done){

						if(db.isOpeningDataFilled(store_cd)){

							if(db.isMiddayDataFilled(store_cd)){

								Intent in2=new Intent(getApplicationContext(),ClosingStock.class);

								startActivity(in2);

								overridePendingTransition(R.anim.activity_in, R.anim.activity_out);

							}
							else{

								Snackbar.make(recyclerView,"First fill Midday Stock Data",Snackbar.LENGTH_SHORT).show();
							}

						}else{

							Snackbar.make(recyclerView,"First fill Opening Stock data",Snackbar.LENGTH_SHORT).show();

						}

					}
					if(current.getIconImg()==R.drawable.storesignage1 || current.getIconImg()==R.drawable.storesignage_done1){

							Intent in5=new Intent(getApplicationContext(),StoreSignAgeActivity.class);

							startActivity(in5);

							overridePendingTransition(R.anim.activity_in, R.anim.activity_out);

					}

					if(current.getIconImg()==R.drawable.competition1 || current.getIconImg()==R.drawable.cpmpetition_done1){

						Intent in7=new Intent(getApplicationContext(),COMPETITORActivity.class);

						startActivity(in7);

						overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
					}

				}
			});

		}

		@Override
		public int getItemCount() {
			return data.size();
		}


		class MyViewHolder extends RecyclerView.ViewHolder{

			//TextView txt;
			ImageView icon;

			public MyViewHolder(View itemView) {
				super(itemView);
				//txt=(TextView) itemView.findViewById(R.id.list_txt);
				icon=(ImageView) itemView.findViewById(R.id.list_icon);
			}
		}

	}

	public List<NavMenuItemGetterSetter> getdata(){
		List<NavMenuItemGetterSetter> data=new ArrayList<>();

		int Stock, middayImg, closingImg, windows, assetImg, additionalImg, competitionImg, geotag;

		if(db.isClosingDataFilled(store_cd)){
			closingImg = R.drawable.closing_stock_done;
		}
		else{
			closingImg = R.drawable.closing_stock;
		}

		if(db.isMiddayDataFilled(store_cd)){
			middayImg = R.drawable.midday_stock_done;
		}
		else{
			middayImg = R.drawable.midday_stock;
		}

//		if(db.isOpeningDataFilled(store_cd)){
//			Stock = R.drawable.stock_done1;
//		}
//		else{
//			Stock = R.drawable.stock1;
//		}

		if(db.isPOSMDataFilled(store_cd)){
			assetImg = R.drawable.posm_done1;
		}
		else{
			assetImg = R.drawable.posm1;

		}

		if(isEachWindowFilled())
		{
			windows = R.drawable.window_done;
		}
		else
		{
			windows = R.drawable.windows;
		}



		if(db.getCOMPETITORData(store_cd).size()>0){
			competitionImg = R.drawable.cpmpetition_done1;
		}
		else{
			competitionImg = R.drawable.competition1;

		}

		if(db.getSFTData(store_cd).size()>0){
			additionalImg = R.drawable.storesignage_done1;
		}
		else{
			additionalImg = R.drawable.storesignage1;

		}

		if(db.getGeotaggingData(store_cd).size()>0){

			if(db.getGeotaggingData(store_cd).get(0).getGEO_TAG().equalsIgnoreCase(CommonString1.KEY_GEO_Y)){

				geotag = R.drawable.geotag_done1;
			}

			else{

				geotag = R.drawable.geotag1;
			}
		}

		else{

			if(GEO_TAG.equalsIgnoreCase(CommonString1.KEY_GEO_Y))
			{
				geotag = R.drawable.geotag_done1;
			}
			else
			{
				geotag = R.drawable.geotag1;
			}


		}

		if(user_type.equals("Promoter")){
			//int img[]={Stock, middayImg, windows, assetImg, closingImg, additionalImg, competitionImg};
			int img[]={ middayImg, windows, closingImg, additionalImg, competitionImg};
			for(int i=0;i<img.length;i++){

				NavMenuItemGetterSetter recData=new NavMenuItemGetterSetter();
				recData.setIconImg(img[i]);
				//recData.setIconName(text[i]);

				data.add(recData);
			}
		}
		else if(user_type.equals("Merchandiser")){
			//int img[]={Stock, windows, assetImg,, , };
			//int img[]={Stock, geotag,additionalImg,assetImg,competitionImg,windows};
			int img[]={geotag,additionalImg,windows,competitionImg};

			for(int i=0;i<img.length;i++){

				NavMenuItemGetterSetter recData=new NavMenuItemGetterSetter();
				recData.setIconImg(img[i]);
				//recData.setIconName(text[i]);

				data.add(recData);
			}
		}

		return  data;
	}

	public boolean isEachWindowFilled()
	{
		boolean isFilled = true;
		for(int i=0;i<windowdata.size();i++)
		{
			if(!db.getwindowDataForEachWindow(store_cd,windowdata.get(i).getWindow_cd().get(0)))
			{
				isFilled = false;
				break;
			}
		}

		return isFilled;
	}

}
