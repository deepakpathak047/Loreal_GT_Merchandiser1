package com.cpm.upload;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cpm.Constants.CommonString1;
import com.cpm.Loreal_GT.MainMenuActivity;
import com.cpm.capitalfoods.R;
import com.cpm.database.GSKDatabase;
import com.cpm.delegates.CoverageBean;
import com.cpm.delegates.StoreBean;
import com.cpm.message.AlertMessage;
import com.cpm.xmlGetterSetter.AssetInsertdataGetterSetter;
import com.cpm.xmlGetterSetter.COMPETITORGetterSetter;
import com.cpm.xmlGetterSetter.ChecklistGetterSetter;
import com.cpm.xmlGetterSetter.ChecklistInsertDataGetterSetter;
import com.cpm.xmlGetterSetter.CompetitionPromotionGetterSetter;
import com.cpm.xmlGetterSetter.FacingCompetitorGetterSetter;
import com.cpm.xmlGetterSetter.FailureGetterSetter;
import com.cpm.xmlGetterSetter.POSMDATAGetterSetter;
import com.cpm.xmlGetterSetter.StockNewGetterSetter;
import com.cpm.xmlGetterSetter.StoreSignAgeGetterSetter;
import com.cpm.xmlGetterSetter.WindowSKUEntryGetterSetter;
import com.cpm.xmlHandler.FailureXMLHandler;

import org.json.JSONArray;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

@SuppressWarnings("deprecation")
public class UploadDataActivity extends Activity {

    private Dialog dialog;
    private ProgressBar pb;
    private TextView percentage, message;
    String app_ver;
    private String visit_date, username;
    private SharedPreferences preferences;
    private GSKDatabase database;
    private String reasonid, faceup, stock, length;
    private int factor, k;
    String datacheck = "";
    String[] words;
    String validity, storename;
    int mid;
    String sod = "";
    String total_sku = "";
    String sku = "";
    String sos_data = "";
    String category_data = "";
    Data data;

    private ArrayList<CoverageBean> coverageBeanlist = new ArrayList<CoverageBean>();

    private FailureGetterSetter failureGetterSetter = null;
    StoreBean storestatus = new StoreBean();
    static int counter = 1;
    private ArrayList<StoreBean> store_detail = new ArrayList<StoreBean>();

    /*private ArrayList<AttendenceBean> attendenceData = new ArrayList<AttendenceBean>();
    private ArrayList<GATEbEAN> gateBean = new ArrayList<GATEbEAN>();
    private ArrayList<DeepFreezerTypeGetterSetter> deepfreezerData = new ArrayList<DeepFreezerTypeGetterSetter>();
    private ArrayList<FacingCompetitorGetterSetter> facingCompetitorData = new ArrayList<FacingCompetitorGetterSetter>();
    */
    private ArrayList<AssetInsertdataGetterSetter> assetInsertdata = new ArrayList<AssetInsertdataGetterSetter>();
    private ArrayList<StoreSignAgeGetterSetter> SignAgeData = new ArrayList<StoreSignAgeGetterSetter>();

    private ArrayList<WindowSKUEntryGetterSetter> window1 = new ArrayList<WindowSKUEntryGetterSetter>();
    private ArrayList<WindowSKUEntryGetterSetter> window2 = new ArrayList<WindowSKUEntryGetterSetter>();
    private ArrayList<ChecklistGetterSetter> check1 = new ArrayList<ChecklistGetterSetter>();


    private ArrayList<POSMDATAGetterSetter> POSMdata = new ArrayList<POSMDATAGetterSetter>();


    private ArrayList<COMPETITORGetterSetter> COMPETITORDATA = new ArrayList<COMPETITORGetterSetter>();

    //private ArrayList<FoodStoreInsertDataGetterSetter> foodStoredata = new ArrayList<FoodStoreInsertDataGetterSetter>();
    private ArrayList<StockNewGetterSetter> stockData = new ArrayList<StockNewGetterSetter>();
    private ArrayList<StockNewGetterSetter> stockImgData = new ArrayList<StockNewGetterSetter>();
    ArrayList<StoreSignAgeGetterSetter> poiData = new ArrayList<StoreSignAgeGetterSetter>();
    ArrayList<StoreSignAgeGetterSetter> competitionpoiData = new ArrayList<StoreSignAgeGetterSetter>();
    ArrayList<FacingCompetitorGetterSetter> facingcompetition = new ArrayList<FacingCompetitorGetterSetter>();
    ArrayList<CompetitionPromotionGetterSetter> promotioncompetitionData = new ArrayList<CompetitionPromotionGetterSetter>();

    private ArrayList<ChecklistInsertDataGetterSetter> assetChecklistInsertdata = new ArrayList<ChecklistInsertDataGetterSetter>();

    //private ArrayList<CallsGetterSetter> callsData = new ArrayList<CallsGetterSetter>();

    boolean upload_status;
    String result;
    String Path;
    boolean image_valid;

    String errormsg = "", status;
    boolean up_success_flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        visit_date = preferences.getString(CommonString1.KEY_DATE, null);
        username = preferences.getString(CommonString1.KEY_USERNAME, null);
        app_ver = preferences.getString(CommonString1.KEY_VERSION, "");
        database = new GSKDatabase(this);
        database.open();

        Intent i = getIntent();
        upload_status = i.getBooleanExtra("UploadAll", false);

        Path = CommonString1.FILE_PATH;

        new UploadTask(this).execute();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        database.close();
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub

        Intent i = new Intent(this, MainMenuActivity.class);
        startActivity(i);
        UploadDataActivity.this.finish();
    }

    private class UploadTask extends AsyncTask<Void, Data, String> {
        private Context context;

        UploadTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            dialog = new Dialog(context);
            dialog.setContentView(R.layout.custom_upload);
            dialog.setTitle("Uploading Data");
            dialog.setCancelable(false);
            dialog.show();
            pb = (ProgressBar) dialog.findViewById(R.id.progressBar1);
            percentage = (TextView) dialog.findViewById(R.id.percentage);
            message = (TextView) dialog.findViewById(R.id.message);
        }

        @Override
        protected String doInBackground(Void... params) {
            // TODO Auto-generated method stub

            try {

                data = new Data();

                if (upload_status == false)

                {
                    coverageBeanlist = database.getCoverageData(visit_date);

                } else {
                    coverageBeanlist = database.getCoverageData(null);

                }


                if (coverageBeanlist.size() > 0) {

                    if (coverageBeanlist.size() == 1) {
                        factor = 50;
                    } else {

                        factor = 100 / (coverageBeanlist.size());
                    }
                }

                for (int i = 0; i < coverageBeanlist.size(); i++) {


                    if (!coverageBeanlist.get(i).getStatus()
                            .equalsIgnoreCase(CommonString1.KEY_U)) {


                        String onXML = "[DATA][USER_DATA][STORE_CD]"
                                + coverageBeanlist.get(i).getStoreId()
                                + "[/STORE_CD]" + "[VISIT_DATE]"
                                + coverageBeanlist.get(i).getVisitDate()
                                + "[/VISIT_DATE][LATITUDE]"
                                + coverageBeanlist.get(i).getLatitude()
                                + "[/LATITUDE][APP_VERSION]"
                                + app_ver
                                + "[/APP_VERSION][LONGITUDE]"
                                + coverageBeanlist.get(i).getLongitude()
                                + "[/LONGITUDE][IN_TIME]"
                                + coverageBeanlist.get(i).getInTime()
                                + "[/IN_TIME][OUT_TIME]"
                                + coverageBeanlist.get(i).getOutTime()
                                + "[/OUT_TIME][UPLOAD_STATUS]"
                                + "N"
                                + "[/UPLOAD_STATUS][USER_ID]" + username
                                + "[/USER_ID][IMAGE_URL]" + coverageBeanlist.get(i).getImage()
                                + "[/IMAGE_URL][REASON_ID]"
                                + coverageBeanlist.get(i).getReasonid()
                                + "[/REASON_ID]"
                                + "[SUBREASON_ID]"
                                + coverageBeanlist.get(i).getSub_reasonId()
                                + "[/SUBREASON_ID]"
                                + "[INFORM_TO]"
                                + coverageBeanlist.get(i).getInformto()
                                + "[/INFORM_TO]"
                                + "[REASON_REMARK]"
                                + coverageBeanlist.get(i).getRemark()
                                + "[/REASON_REMARK][/USER_DATA][/DATA]";


                        SoapObject request = new SoapObject(
                                CommonString1.NAMESPACE,
                                CommonString1.METHOD_UPLOAD_DR_STORE_COVERAGE);
                        request.addProperty("onXML", onXML);

                        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                                SoapEnvelope.VER11);
                        envelope.dotNet = true;
                        envelope.setOutputSoapObject(request);

                        HttpTransportSE androidHttpTransport = new HttpTransportSE(
                                CommonString1.URL);

                        androidHttpTransport.call(
                                CommonString1.SOAP_ACTION + CommonString1.METHOD_UPLOAD_DR_STORE_COVERAGE,
                                envelope);
                        Object result = (Object) envelope.getResponse();

                        datacheck = result.toString();
                        datacheck = datacheck.replace("\"", "");
                        words = datacheck.split("\\;");
                        validity = (words[0]);

                        if (validity
                                .equalsIgnoreCase(CommonString1.KEY_SUCCESS)) {
                            database.updateCoverageStatus(coverageBeanlist
                                    .get(i).getMID(), CommonString1.KEY_P);

                            database.updateStoreStatusOnLeave(
                                    coverageBeanlist.get(i).getStoreId(),
                                    coverageBeanlist.get(i).getVisitDate(),
                                    CommonString1.KEY_P);
                        } else {


                            if (!result.toString().equalsIgnoreCase(
                                    CommonString1.KEY_SUCCESS)) {

                                return CommonString1.METHOD_UPLOAD_DR_STORE_COVERAGE;
                            }

                            if (result.toString().equalsIgnoreCase(
                                    CommonString1.KEY_FALSE)) {
                                return CommonString1.METHOD_UPLOAD_DR_STORE_COVERAGE;
                            }
                            if (result.toString().equalsIgnoreCase(
                                    CommonString1.KEY_FAILURE)) {
                                return CommonString1.METHOD_UPLOAD_DR_STORE_COVERAGE;
                            }

                        }

                        mid = Integer.parseInt((words[1]));

                        data.value = 10;
                        data.name = "Uploading";

                        publishProgress(data);

                        String final_xml = "";


/*
                        // stock data

                        final_xml = "";
                        onXML = "";
                        stockData = database.getStockUpload(coverageBeanlist.get(i).getStoreId());

                        if (stockData.size() > 0) {

                            for (int j = 0; j < stockData.size(); j++) {

								*/
/*String actual_listed;
								if(stockData.get(j).getActual_listed().equalsIgnoreCase("yes")){
									actual_listed="1";
								}else{
									actual_listed="0";
								}*//*


                                onXML = "[STOCK_DATA][MID]"
                                        + mid
                                        + "[/MID]"
										*/
/*+ "[BRAND_CD]"
										+ stockData.get(j).getBrand_cd()
										+ "[/BRAND_CD]"*//*


                                        + "[CREATED_BY]"
                                        + username
                                        + "[/CREATED_BY]"
                                        + "[OPENING_TOTAL_STOCK]"
                                        + stockData.get(j).getOpenning_total_stock()
                                        + "[/OPENING_TOTAL_STOCK]"
                                        + "[FACING]"
                                        + stockData.get(j).getOpening_facing()
                                        + "[/FACING]"
                                        + "[ONE_TO_SIX]"
                                        + stockData.get(j).getStock_under45days()
                                        + "[/ONE_TO_SIX]"


                                        + "[SEVEN_TO_TWELVE]"
                                        + stockData.get(j).getStockunder12()
                                        + "[/SEVEN_TO_TWELVE]"
                                        + "[MORE_THIRTEEN]"
                                        + stockData.get(j).getStockgreater13()
                                        + "[/MORE_THIRTEEN]"

                                        + "[SKU_CD]"
                                        + stockData.get(j).getSku_cd()
                                        + "[/SKU_CD]"

                                        + "[/STOCK_DATA]";

                                final_xml = final_xml + onXML;

                            }

                            final String sos_xml = "[DATA]" + final_xml
                                    + "[/DATA]";

                            request = new SoapObject(
                                    CommonString1.NAMESPACE,
                                    CommonString1.METHOD_UPLOAD_XML);
                            request.addProperty("XMLDATA", sos_xml);
                            request.addProperty("KEYS", "STOCK_DATA");
                            request.addProperty("USERNAME", username);
                            request.addProperty("MID", mid);

                            envelope = new SoapSerializationEnvelope(
                                    SoapEnvelope.VER11);
                            envelope.dotNet = true;
                            envelope.setOutputSoapObject(request);

                            androidHttpTransport = new HttpTransportSE(
                                    CommonString1.URL);

                            androidHttpTransport.call(
                                    CommonString1.SOAP_ACTION + CommonString1.METHOD_UPLOAD_XML,
                                    envelope);
                            result = (Object) envelope.getResponse();


                            if (!result.toString().equalsIgnoreCase(
                                    CommonString1.KEY_SUCCESS)) {

                                return "STOCK_DATA";
                            }


                            if (result.toString().equalsIgnoreCase(
                                    CommonString1.KEY_NO_DATA)) {
                                return CommonString1.METHOD_UPLOAD_XML;
                            }

                            if (result.toString().equalsIgnoreCase(
                                    CommonString1.KEY_FAILURE)) {
                                return CommonString1.METHOD_UPLOAD_XML;
                            }


                        }
*/


                     /*   data.value = 20;
                        data.name = "STOCK_DATA";

                        publishProgress(data);*/

/*

//------------------------------new----------------------
						// stock present data

						final_xml = "";
						onXML = "";
						stockData = database.getStockPresentUpload(coverageBeanlist.get(i).getStoreId());

						if (stockData.size()>0) {

							for (int j = 0; j < stockData.size(); j++) {

								String booleanvalue = "0";
								if(stockData.get(j).getSkuChecked()==1)
								{
									booleanvalue = "1";
								}
								else if(stockData.get(j).getSkuChecked()==0)
								{
									booleanvalue = "0";
								}
								else
								{
									booleanvalue = "-1";
								}

								onXML = "[STOCK_DATA][MID]"
										+ mid
										+ "[/MID]"
										+ "[CREATED_BY]"
										+ username
										+ "[/CREATED_BY]"
										+ "[SKU_CD]"
										+ stockData.get(j).getSku_cd()
										+ "[/SKU_CD]"
										+ "[OPENING_STOCK_PRESENT]"
										+ booleanvalue
										+ "[/OPENING_STOCK_PRESENT]"
										+ "[/STOCK_DATA]";

								final_xml = final_xml + onXML;

							}

							final String sos_xml = "[DATA]" + final_xml
									+ "[/DATA]";

							request = new SoapObject(
									CommonString1.NAMESPACE,
									CommonString1.METHOD_UPLOAD_XML);
							request.addProperty("XMLDATA", sos_xml);
							request.addProperty("KEYS", "STOCK_DATA_PRESENT_NEW");
							request.addProperty("USERNAME", username);
							request.addProperty("MID", mid);

							envelope = new SoapSerializationEnvelope(
									SoapEnvelope.VER11);
							envelope.dotNet = true;
							envelope.setOutputSoapObject(request);

							androidHttpTransport = new HttpTransportSE(
									CommonString1.URL);

							androidHttpTransport.call(
									CommonString1.SOAP_ACTION+ CommonString1.METHOD_UPLOAD_XML,
									envelope);
							result = (Object) envelope.getResponse();


							if (!result.toString().equalsIgnoreCase(
									CommonString1.KEY_SUCCESS)) {

								return "STOCK_DATA_PRESENT";
							}


							if (result.toString().equalsIgnoreCase(
									CommonString1.KEY_NO_DATA)) {
								return CommonString1.METHOD_UPLOAD_XML;
							}

							if (result.toString().equalsIgnoreCase(
									CommonString1.KEY_FAILURE)) {
								return CommonString1.METHOD_UPLOAD_XML;
							}


						}

						data.value =20;
						data.name = "STOCK_DATA_PRESENT";

						publishProgress(data);

*/


//							uploading SIGNAGE_DATA

                        final_xml = "";
                        onXML = "";
                        SignAgeData = database.getSFTData(coverageBeanlist.get(i).getStoreId());

                        if (SignAgeData.size() > 0) {

                            for (int j = 0; j < SignAgeData.size(); j++) {

                                onXML = "[SIGNAGE_DATA][MID]"
                                        + mid
                                        + "[/MID]"

                                        + "[CREATED_BY]"
                                        + username
                                        + "[/CREATED_BY]"
                                        + "[SIGN_EXIST]"
                                        + SignAgeData.get(j).getExistSpinner_CD()
                                        + "[/SIGN_EXIST]"
                                        + "[WORKING]"
                                        + SignAgeData.get(j).getWorkingsppiner_CD()
                                        + "[/WORKING]"
                                        + "[IMAGE_URL]"
                                        + SignAgeData.get(j).getImage()
                                        + "[/IMAGE_URL]"
                                        + "[/SIGNAGE_DATA]";

                                final_xml = final_xml + onXML;

                            }

                            final String sos_xml = "[DATA]" + final_xml
                                    + "[/DATA]";

                            request = new SoapObject(
                                    CommonString1.NAMESPACE,
                                    CommonString1.METHOD_UPLOAD_XML);
                            request.addProperty("XMLDATA", sos_xml);
                            request.addProperty("KEYS", "SIGNAGE_DATA");
                            request.addProperty("USERNAME", username);
                            request.addProperty("MID", mid);

                            envelope = new SoapSerializationEnvelope(
                                    SoapEnvelope.VER11);
                            envelope.dotNet = true;
                            envelope.setOutputSoapObject(request);

                            androidHttpTransport = new HttpTransportSE(
                                    CommonString1.URL);

                            androidHttpTransport.call(
                                    CommonString1.SOAP_ACTION + CommonString1.METHOD_UPLOAD_XML,
                                    envelope);
                            result = (Object) envelope.getResponse();


                            if (!result.toString().equalsIgnoreCase(
                                    CommonString1.KEY_SUCCESS)) {

                                return "SIGNAGE_DATA";
                            }


                            if (result.toString().equalsIgnoreCase(
                                    CommonString1.KEY_NO_DATA)) {
                                return CommonString1.METHOD_UPLOAD_XML;
                            }

                            if (result.toString().equalsIgnoreCase(
                                    CommonString1.KEY_FAILURE)) {
                                return CommonString1.METHOD_UPLOAD_XML;
                            }

                        }


                        data.value = 30;
                        data.name = "SIGNAGE_DATA";

                        publishProgress(data);

/*

//							uploading POSM_DATA data

                        final_xml = "";
                        onXML = "";
                        POSMdata = database.getPOSMData(coverageBeanlist.get(i).getStoreId());

                        if (POSMdata.size() > 0) {

                            for (int j = 0; j < POSMdata.size(); j++) {

                                onXML = "[POSM_DATA][MID]"
                                        + mid
                                        + "[/MID]"

                                        + "[CREATED_BY]"
                                        + username
                                        + "[/CREATED_BY]"


                                        + "[QUANTITY]"
                                        + POSMdata.get(j).getEdText()
                                        + "[/QUANTITY]"

                                        + "[POSM_CD]"
                                        + POSMdata.get(j).getPOSM_CD().get(0)
                                        + "[/POSM_CD]"

                                        + "[Image_URL]"
                                        + POSMdata.get(j).getImage_Url()
                                        + "[/Image_URL]"

                                        + "[/POSM_DATA]";

                                final_xml = final_xml + onXML;

                            }

                            final String sos_xml = "[DATA]" + final_xml
                                    + "[/DATA]";

                            request = new SoapObject(
                                    CommonString1.NAMESPACE,
                                    CommonString1.METHOD_UPLOAD_XML);
                            request.addProperty("XMLDATA", sos_xml);
                            request.addProperty("KEYS", "POSM_DATA");
                            request.addProperty("USERNAME", username);
                            request.addProperty("MID", mid);

                            envelope = new SoapSerializationEnvelope(
                                    SoapEnvelope.VER11);
                            envelope.dotNet = true;
                            envelope.setOutputSoapObject(request);

                            androidHttpTransport = new HttpTransportSE(
                                    CommonString1.URL);

                            androidHttpTransport.call(
                                    CommonString1.SOAP_ACTION + CommonString1.METHOD_UPLOAD_XML,
                                    envelope);
                            result = (Object) envelope.getResponse();


                            if (!result.toString().equalsIgnoreCase(
                                    CommonString1.KEY_SUCCESS)) {

                                return "POSM_DATA";
                            }


                            if (result.toString().equalsIgnoreCase(
                                    CommonString1.KEY_NO_DATA)) {
                                return CommonString1.METHOD_UPLOAD_XML;
                            }

                            if (result.toString().equalsIgnoreCase(
                                    CommonString1.KEY_FAILURE)) {
                                return CommonString1.METHOD_UPLOAD_XML;
                            }

                        }

                        data.value = 45;
                        data.name = "POSM_DATA";

                        publishProgress(data);
*/
                        ///// COMPETITOR DATA
                        final_xml = "";
                        onXML = "";
                        COMPETITORDATA = database.getCOMPETITORData(coverageBeanlist.get(i).getStoreId());

                        if (COMPETITORDATA.size() > 0) {

                            for (int j = 0; j < COMPETITORDATA.size(); j++) {

                                onXML = "[COMPETITOR_DATA][MID]"
                                        + mid
                                        + "[/MID]"

                                        + "[CREATED_BY]"
                                        + username
                                        + "[/CREATED_BY]"
                                        + "[COMPANY_CD]"
                                        + COMPETITORDATA.get(j).getCOMPANY_CD()
                                        + "[/COMPANY_CD]"
                                        + "[REMARKS]"
                                        + COMPETITORDATA.get(j).getEdText()
                                        + "[/REMARKS]"
                                        + "[WINDOW]"
                                        + COMPETITORDATA.get(j).getSpinnerExists_CD()
                                        + "[/WINDOW]"
                                        + "[Image_URL]"
                                        + COMPETITORDATA.get(j).getImage()
                                        + "[/Image_URL]"


                                        + "[/COMPETITOR_DATA]";

                                final_xml = final_xml + onXML;

                            }

                            final String sos_xml = "[DATA]" + final_xml
                                    + "[/DATA]";

                            request = new SoapObject(
                                    CommonString1.NAMESPACE,
                                    CommonString1.METHOD_UPLOAD_XML);
                            request.addProperty("XMLDATA", sos_xml);
                            request.addProperty("KEYS", "COMPETITOR_DATA");
                            request.addProperty("USERNAME", username);
                            request.addProperty("MID", mid);

                            envelope = new SoapSerializationEnvelope(
                                    SoapEnvelope.VER11);
                            envelope.dotNet = true;
                            envelope.setOutputSoapObject(request);

                            androidHttpTransport = new HttpTransportSE(
                                    CommonString1.URL);

                            androidHttpTransport.call(
                                    CommonString1.SOAP_ACTION + CommonString1.METHOD_UPLOAD_XML,
                                    envelope);
                            result = (Object) envelope.getResponse();


                            if (!result.toString().equalsIgnoreCase(
                                    CommonString1.KEY_SUCCESS)) {

                                return "COMPETITOR_DATA";
                            }

                            if (result.toString().equalsIgnoreCase(
                                    CommonString1.KEY_NO_DATA)) {
                                return CommonString1.METHOD_UPLOAD_XML;
                            }

                            if (result.toString().equalsIgnoreCase(
                                    CommonString1.KEY_FAILURE)) {
                                return CommonString1.METHOD_UPLOAD_XML;
                            }

                        }


                        data.value = 50;
                        data.name = "COMPETITOR_DATA";

                        publishProgress(data);


                        ///////////////////////////////

///////window1 DATA
                        final_xml = "";
                        onXML = "";
                        window1 = database.getwindowdat(coverageBeanlist.get(i).getStoreId());

                        if (window1.size() > 0) {

                            for (int j = 0; j < window1.size(); j++) {

                                onXML = "[WINDOW_DATA][MID]"
                                        + mid
                                        + "[/MID]"

                                        + "[CREATED_BY]"
                                        + username
                                        + "[/CREATED_BY]"
                                        + "[WINDOW_CD]"
                                        + window1.get(j).getWindow_cd()
                                        + "[/WINDOW_CD]"
                                        + "[KEY_ID]"
                                        + window1.get(j).getKeyID()
                                        + "[/KEY_ID]"
                                        + "[WINDOW_IMAGE]"
                                        + window1.get(j).getWindow_image()
                                        + "[/WINDOW_IMAGE]"
                                        + "[REASON_ID]"
                                        + window1.get(j).getReason_id()
                                        + "[/REASON_ID]"
                                        + "[EXISTORNOT]"
                                        + window1.get(j).getExistOrnot()
                                        + "[/EXISTORNOT]"
                                        + "[/WINDOW_DATA]";


                                final_xml = final_xml + onXML;

                            }

                            final String sos_xml = "[DATA]" + final_xml
                                    + "[/DATA]";

                            request = new SoapObject(
                                    CommonString1.NAMESPACE,
                                    CommonString1.METHOD_UPLOAD_XML);
                            request.addProperty("XMLDATA", sos_xml);
                            request.addProperty("KEYS", "WINDOW_DATA_NEW");
                            request.addProperty("USERNAME", username);
                            request.addProperty("MID", mid);

                            envelope = new SoapSerializationEnvelope(
                                    SoapEnvelope.VER11);
                            envelope.dotNet = true;
                            envelope.setOutputSoapObject(request);

                            androidHttpTransport = new HttpTransportSE(
                                    CommonString1.URL);

                            androidHttpTransport.call(
                                    CommonString1.SOAP_ACTION + CommonString1.METHOD_UPLOAD_XML,
                                    envelope);
                            result = (Object) envelope.getResponse();


                            if (!result.toString().equalsIgnoreCase(
                                    CommonString1.KEY_SUCCESS)) {

                                return "WINDOW_DATA";
                            }


                            if (result.toString().equalsIgnoreCase(
                                    CommonString1.KEY_NO_DATA)) {
                                return CommonString1.METHOD_UPLOAD_XML;
                            }

                            if (result.toString().equalsIgnoreCase(
                                    CommonString1.KEY_FAILURE)) {
                                return CommonString1.METHOD_UPLOAD_XML;
                            }

                        }


                        data.value = 80;
                        data.name = "WINDOW_DATA";

                        publishProgress(data);

///////////////////////////
/*
                        ///////WINDOW_SKU DATA
                        final_xml = "";
                        onXML = "";
                        window2 = database.getwindowDATA1(coverageBeanlist.get(i).getStoreId());

                        if (window2.size() > 0) {

                            for (int j = 0; j < window2.size(); j++) {

                                onXML = "[WINDOW_SKU][MID]"
                                        + mid
                                        + "[/MID]"

                                        + "[CREATED_BY]"
                                        + username
                                        + "[/CREATED_BY]"

										*//*+ "[CATEGORY_ID]"
										+ window2.get(j).getCategory_cd()
										+ "[/CATEGORY_ID]"*//*



									*//*	+ "[BRAND_ID]"
										+ window2.get(j).getBrand_cd()
										+ "[/BRAND_ID]"


										+ "[DATE]"
										+  2.get(j).getVisit_date()
										+ "[/DATE]"
*//*
                                        + "[SKU_CD]"
                                        + window2.get(j).getSku_cd()
                                        + "[/SKU_CD]"


                                        + "[WINDOW_CD]"
                                        + window2.get(j).getWindow_cd()
                                        + "[/WINDOW_CD]"

                                        + "[stocksonetosix]"
                                        + window2.get(j).getStocksonetosix()
                                        + "[/stocksonetosix]"

                                        + "[stockseventotwelve]"
                                        + window2.get(j).getSeventotwelve()
                                        + "[/stockseventotwelve]"


                                        + "[stockabovethirteen]"
                                        + window2.get(j).getStockabovethirteen()
                                        + "[/stockabovethirteen]"

                                        + "[STOCK]"
                                        + window2.get(j).getStock()
                                        + "[/STOCK]"


                                        + "[/WINDOW_SKU]";


                                final_xml = final_xml + onXML;

                            }

                            final String sos_xml = "[DATA]" + final_xml
                                    + "[/DATA]";

                            request = new SoapObject(
                                    CommonString1.NAMESPACE,
                                    CommonString1.METHOD_UPLOAD_XML);
                            request.addProperty("XMLDATA", sos_xml);
                            request.addProperty("KEYS", "WINDOW_SKU");
                            request.addProperty("USERNAME", username);
                            request.addProperty("MID", mid);

                            envelope = new SoapSerializationEnvelope(
                                    SoapEnvelope.VER11);
                            envelope.dotNet = true;
                            envelope.setOutputSoapObject(request);

                            androidHttpTransport = new HttpTransportSE(
                                    CommonString1.URL);

                            androidHttpTransport.call(
                                    CommonString1.SOAP_ACTION + CommonString1.METHOD_UPLOAD_XML,
                                    envelope);
                            result = (Object) envelope.getResponse();


                            if (!result.toString().equalsIgnoreCase(
                                    CommonString1.KEY_SUCCESS)) {

                                return "WINDOW_SKU";
                            }


                            if (result.toString().equalsIgnoreCase(
                                    CommonString1.KEY_NO_DATA)) {
                                return CommonString1.METHOD_UPLOAD_XML;
                            }

                            if (result.toString().equalsIgnoreCase(
                                    CommonString1.KEY_FAILURE)) {
                                return CommonString1.METHOD_UPLOAD_XML;
                            }

                        }


                        data.value = 80;
                        data.name = "WINDOW_SKU";

                        publishProgress(data);*/


///////CheckList_DATA DATA

                        final_xml = "";
                        onXML = "";

                        check1 = database.getchecklist(coverageBeanlist.get(i).getStoreId());

                        if (check1.size() > 0) {

                            for (int j = 0; j < check1.size(); j++) {

                                onXML = "[CHECK_LIST][MID]"
                                        + mid
                                        + "[/MID]"

                                        + "[CREATED_BY]"
                                        + username
                                        + "[/CREATED_BY]"

                                        + "[WINDOW_CD]"
                                        + check1.get(j).getWINDOW_CD()
                                        + "[/WINDOW_CD]"
                                        + "[COMMON_ID]"
                                        + check1.get(j).getCOMMON_ID()
                                        + "[/COMMON_ID]"
                                        + "[CHECKLIST_CD]"
                                        + check1.get(j).getCHECKLIST_CD()
                                        + "[/CHECKLIST_CD]"

                                        + "[ANSWER_CD]"
                                        + check1.get(j).getANSWER_CD()
                                        + "[/ANSWER_CD]"

                                        + "[/CHECK_LIST]";

                                final_xml = final_xml + onXML;

                            }

                            final String sos_xml = "[DATA]" + final_xml
                                    + "[/DATA]";

                            request = new SoapObject(
                                    CommonString1.NAMESPACE,
                                    CommonString1.METHOD_UPLOAD_XML);
                            request.addProperty("XMLDATA", sos_xml);
                            request.addProperty("KEYS", "CHECK_LIST_NEW");
                            request.addProperty("USERNAME", username);
                            request.addProperty("MID", mid);

                            envelope = new SoapSerializationEnvelope(
                                    SoapEnvelope.VER11);
                            envelope.dotNet = true;
                            envelope.setOutputSoapObject(request);

                            androidHttpTransport = new HttpTransportSE(
                                    CommonString1.URL);

                            androidHttpTransport.call(
                                    CommonString1.SOAP_ACTION + CommonString1.METHOD_UPLOAD_XML,
                                    envelope);
                            result = (Object) envelope.getResponse();


                            if (!result.toString().equalsIgnoreCase(
                                    CommonString1.KEY_SUCCESS)) {

                                return "CHECK_LIST";
                            }


                            if (result.toString().equalsIgnoreCase(
                                    CommonString1.KEY_NO_DATA)) {
                                return CommonString1.METHOD_UPLOAD_XML;
                            }

                            if (result.toString().equalsIgnoreCase(
                                    CommonString1.KEY_FAILURE)) {
                                return CommonString1.METHOD_UPLOAD_XML;
                            }

                        }


                        data.value = 80;
                        data.name = "CHECK_LIST";

                        publishProgress(data);

                        // SET COVERAGE STATUS

                        final_xml = "";
                        onXML = "";

                        onXML = "[COVERAGE_STATUS][STORE_ID]"
                                + coverageBeanlist.get(i).getStoreId()
                                + "[/STORE_ID]"
                                + "[VISIT_DATE]"
                                + coverageBeanlist.get(i).getVisitDate()
                                + "[/VISIT_DATE]"
                                + "[USER_ID]"
                                + coverageBeanlist.get(i).getUserId()
                                + "[/USER_ID]"
                                + "[STATUS]"
                                + CommonString1.KEY_D
                                + "[/STATUS]"
                                + "[/COVERAGE_STATUS]";

                        final_xml = final_xml + onXML;

                        final String sos_xml = "[DATA]" + final_xml
                                + "[/DATA]";

                        SoapObject request1 = new SoapObject(
                                CommonString1.NAMESPACE,
                                CommonString1.MEHTOD_UPLOAD_COVERAGE_STATUS);
                        request1.addProperty("onXML", sos_xml);


                        SoapSerializationEnvelope envelope1 = new SoapSerializationEnvelope(
                                SoapEnvelope.VER11);
                        envelope1.dotNet = true;
                        envelope1.setOutputSoapObject(request1);

                        HttpTransportSE androidHttpTransport1 = new HttpTransportSE(
                                CommonString1.URL);

                        androidHttpTransport1.call(
                                CommonString1.SOAP_ACTION + CommonString1.MEHTOD_UPLOAD_COVERAGE_STATUS,
                                envelope1);
                        Object result1 = (Object) envelope1.getResponse();


                        if (result1.toString().equalsIgnoreCase(
                                CommonString1.KEY_SUCCESS)) {

                            database.open();

                            database.updateCoverageStatus(coverageBeanlist.get(i)
                                    .getMID(), CommonString1.KEY_D);
                            database.updateStoreStatusOnLeave(coverageBeanlist.get(i)
                                    .getStoreId(), coverageBeanlist.get(i)
                                    .getVisitDate(), CommonString1.KEY_D);

                        }
//////////////////////////////////////

                        if (coverageBeanlist.get(i).getImage() != null && !coverageBeanlist.get(i).getImage().equals("")) {

                            if (new File(CommonString1.FILE_PATH + coverageBeanlist.get(i).getImage()).exists()) {

                                result = UploadImage(coverageBeanlist.get(i).getImage(), "StoreImages");


                                if (!result
                                        .toString()
                                        .equalsIgnoreCase(
                                                CommonString1.KEY_SUCCESS)) {

                                    return "StoreImages";
                                } else if (result
                                        .toString()
                                        .equalsIgnoreCase(
                                                CommonString1.KEY_FALSE)) {

                                    return "StoreImages";
                                } else if (result.toString().equalsIgnoreCase(CommonString1.KEY_FAILURE)) {

                                    return "StoreImages"
                                            + "," + errormsg;
                                }

                                runOnUiThread(new Runnable() {

                                    public void run() {

                                        message.setText("Store Image Uploaded");
                                    }
                                });

                            }
                        }


                        data.value = 60;
                        data.name = "StoreImages";

                        publishProgress(data);


                        if (SignAgeData.size() > 0) {

                            for (int j = 0; j < SignAgeData.size(); j++) {

                                if (SignAgeData.get(j).getImage() != null && !SignAgeData.get(j).getImage().equals("")) {

                                    if (new File(CommonString1.FILE_PATH + SignAgeData.get(j).getImage()).exists()) {

                                        result = UploadImage(SignAgeData.get(j).getImage(), "SignageImages");


                                        if (!result
                                                .toString()
                                                .equalsIgnoreCase(
                                                        CommonString1.KEY_SUCCESS)) {

                                            return "SignageImages";
                                        } else if (result
                                                .toString()
                                                .equalsIgnoreCase(
                                                        CommonString1.KEY_FALSE)) {

                                            return "SignageImages";
                                        } else if (result.toString().equalsIgnoreCase(CommonString1.KEY_FAILURE)) {

                                            return "SignageImages"
                                                    + "," + errormsg;
                                        }

                                        runOnUiThread(new Runnable() {

                                            public void run() {

                                                message.setText("SignageImages Uploaded");
                                            }
                                        });

                                    }
                                }
                            }
                        }

                        data.value = 70;
                        data.name = "SignageImages";

                        publishProgress(data);

//////////////////////////


                     /*   if (POSMdata.size() > 0) {

                            for (int j = 0; j < POSMdata.size(); j++) {

                                if (POSMdata.get(j).getImage_Url() != null && !POSMdata.get(j).getImage_Url().equals("")) {

                                    if (new File(CommonString1.FILE_PATH + POSMdata.get(j).getImage_Url()).exists()) {

                                        result = UploadImage(POSMdata.get(j).getImage_Url(), "PosmImages");


                                        if (!result
                                                .toString()
                                                .equalsIgnoreCase(
                                                        CommonString1.KEY_SUCCESS)) {

                                            return "PosmImages";
                                        } else if (result
                                                .toString()
                                                .equalsIgnoreCase(
                                                        CommonString1.KEY_FALSE)) {

                                            return "PosmImages";
                                        } else if (result.toString().equalsIgnoreCase(CommonString1.KEY_FAILURE)) {

                                            return "PosmImages"
                                                    + "," + errormsg;
                                        }

                                        runOnUiThread(new Runnable() {

                                            public void run() {

                                                message.setText("PosmImages Uploaded");
                                            }
                                        });

                                    }
                                }
                            }
                        }

                        data.value = 80;
                        data.name = "PosmImages";

                        publishProgress(data);
*/

                        //////////////////////////


                        if (COMPETITORDATA.size() > 0) {

                            for (int j = 0; j < COMPETITORDATA.size(); j++) {

                                if (COMPETITORDATA.get(j).getImage() != null && !COMPETITORDATA.get(j).getImage().equals("")) {

                                    if (new File(CommonString1.FILE_PATH + COMPETITORDATA.get(j).getImage()).exists()) {

                                        result = UploadImage(COMPETITORDATA.get(j).getImage(), "CompetitionImages");


                                        if (!result
                                                .toString()
                                                .equalsIgnoreCase(
                                                        CommonString1.KEY_SUCCESS)) {

                                            return "CompetitionImages";
                                        } else if (result
                                                .toString()
                                                .equalsIgnoreCase(
                                                        CommonString1.KEY_FALSE)) {

                                            return "CompetitionImages";
                                        } else if (result.toString().equalsIgnoreCase(CommonString1.KEY_FAILURE)) {

                                            return "CompetitionImages"
                                                    + "," + errormsg;
                                        }

                                        runOnUiThread(new Runnable() {

                                            public void run() {

                                                message.setText("CompetitionImages Uploaded");
                                            }
                                        });

                                    }
                                }
                            }
                        }

                        data.value = 90;
                        data.name = "CompetitionImages";

                        publishProgress(data);


                        ///////////////


                        if (window1.size() > 0) {

                            for (int j = 0; j < window1.size(); j++) {

                                if (window1.get(j).getWindow_image() != null && !window1.get(j).getWindow_image().equals("")) {

                                    if (new File(CommonString1.FILE_PATH + window1.get(j).getWindow_image()).exists()) {

                                        result = UploadImage(window1.get(j).getWindow_image(), "WindowImages");


                                        if (!result
                                                .toString()
                                                .equalsIgnoreCase(
                                                        CommonString1.KEY_SUCCESS)) {

                                            return "WindowImages";
                                        } else if (result
                                                .toString()
                                                .equalsIgnoreCase(
                                                        CommonString1.KEY_FALSE)) {

                                            return "WindowImages";
                                        } else if (result.toString().equalsIgnoreCase(CommonString1.KEY_FAILURE)) {

                                            return "WindowImages"
                                                    + "," + errormsg;
                                        }

                                        runOnUiThread(new Runnable() {

                                            public void run() {

                                                message.setText("WindowImages Uploaded");
                                            }
                                        });

                                    }
                                }
                            }
                        }

                        data.value = 95;
                        data.name = "WindowImages";

                        publishProgress(data);


                        // SET COVERAGE STATUS

                        final_xml = "";
                        onXML = "";

                        onXML = "[COVERAGE_STATUS][STORE_ID]"
                                + coverageBeanlist.get(i).getStoreId()
                                + "[/STORE_ID]"
                                + "[VISIT_DATE]"
                                + coverageBeanlist.get(i).getVisitDate()
                                + "[/VISIT_DATE]"
                                + "[USER_ID]"
                                + coverageBeanlist.get(i).getUserId()
                                + "[/USER_ID]"
                                + "[STATUS]"
                                + CommonString1.KEY_U
                                + "[/STATUS]"
                                + "[/COVERAGE_STATUS]";

                        final_xml = final_xml + onXML;

                        final String sos_xml2 = "[DATA]" + final_xml
                                + "[/DATA]";

                        request1 = new SoapObject(
                                CommonString1.NAMESPACE,
                                CommonString1.MEHTOD_UPLOAD_COVERAGE_STATUS);
                        request1.addProperty("onXML", sos_xml2);


                        envelope1 = new SoapSerializationEnvelope(
                                SoapEnvelope.VER11);
                        envelope1.dotNet = true;
                        envelope1.setOutputSoapObject(request1);

                        androidHttpTransport1 = new HttpTransportSE(
                                CommonString1.URL);

                        androidHttpTransport1.call(
                                CommonString1.SOAP_ACTION + CommonString1.MEHTOD_UPLOAD_COVERAGE_STATUS,
                                envelope1);
                        result1 = (Object) envelope1.getResponse();


                        if (result1.toString().equalsIgnoreCase(
                                CommonString1.KEY_SUCCESS)) {

                            database.open();

                            database.updateCoverageStatus(coverageBeanlist.get(i)
                                    .getMID(), CommonString1.KEY_U);
                            database.updateStoreStatusOnLeave(coverageBeanlist.get(i)
                                    .getStoreId(), coverageBeanlist.get(i)
                                    .getVisitDate(), CommonString1.KEY_U);

                        }


                        if (!result1
                                .toString()
                                .equalsIgnoreCase(
                                        CommonString1.KEY_SUCCESS)) {

                            return "COVERAGE_STATUS";
                        }


                        if (result1.toString().equalsIgnoreCase(
                                CommonString1.KEY_NO_DATA)) {
                            return CommonString1.MEHTOD_UPLOAD_COVERAGE_STATUS;
                        }

                        if (result1.toString().equalsIgnoreCase(
                                CommonString1.KEY_FAILURE)) {
                            return CommonString1.MEHTOD_UPLOAD_COVERAGE_STATUS;
                        }

                        data.value = factor * (i + 1);
                        data.name = "COVERAGE_STATUS";

                        publishProgress(data);


                    }

                }


            } catch (MalformedURLException e) {

                up_success_flag = false;

                final AlertMessage message = new AlertMessage(
                        UploadDataActivity.this,
                        AlertMessage.MESSAGE_EXCEPTION, "download", e);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        message.showMessage();
                    }
                });

            } catch (IOException e) {

                up_success_flag = false;

                final AlertMessage message = new AlertMessage(
                        UploadDataActivity.this,
                        AlertMessage.MESSAGE_SOCKETEXCEPTION, "socket", e);

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        message.showMessage();

                    }
                });
            } catch (Exception e) {

                up_success_flag = false;

                final AlertMessage message = new AlertMessage(
                        UploadDataActivity.this,
                        AlertMessage.MESSAGE_EXCEPTION, "download", e);

                e.getMessage();
                e.printStackTrace();
                e.getCause();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        message.showMessage();
                    }
                });
            }
            if (up_success_flag == true) {
                return CommonString1.KEY_SUCCESS;
            } else {
                return CommonString1.KEY_FAILURE;
            }


        }

        @Override
        protected void onProgressUpdate(Data... values) {
            // TODO Auto-generated method stub

            pb.setProgress(values[0].value);
            percentage.setText(values[0].value + "%");
            message.setText(values[0].name);

        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            dialog.dismiss();

            if (result.contains(CommonString1.KEY_SUCCESS)) {

                if (upload_status == true) {


                    Toast.makeText(getApplicationContext(), "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    AlertMessage message = new AlertMessage(
                            UploadDataActivity.this,
                            AlertMessage.MESSAGE_UPLOAD_DATA, "success", null);
                    message.showMessage();

                    database.deleteAllTables();


                }
            } else {

                AlertMessage message = new AlertMessage(
                        UploadDataActivity.this, "Error in uploading :" + result, "success", null);
                message.showMessage();
            }


        }
    }

    class Data {
        int value;
        String name;
    }


    String makeJson(String json) {
        json = json.replace("\\", "");
        json = json.replace("\"[", "[");
        json = json.replace("]\"", "]");

        return json;
    }


    public JSONArray makeJsonArray(JSONArray json) {
        JSONArray jason = new JSONArray();

        for (int i = 0; i < json.length(); i++) {


        }
        return json;
    }

    public String UploadImage(String path, String folder_path) throws Exception {

        errormsg = "";
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(Path + path, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 1024;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;

        while (true) {
            if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        Bitmap bitmap = BitmapFactory.decodeFile(
                Path + path, o2);

        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bao);
        byte[] ba = bao.toByteArray();
        String ba1 = Base64.encodeBytes(ba);

        SoapObject request = new SoapObject(CommonString1.NAMESPACE,
                CommonString1.METHOD_UPLOAD_IMAGE);

        String[] split = path.split("/");
        String path1 = split[split.length - 1];

        request.addProperty("img", ba1);
        request.addProperty("name", path1);
        request.addProperty("FolderName", folder_path);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);

        HttpTransportSE androidHttpTransport = new HttpTransportSE(
                CommonString1.URL);

        androidHttpTransport
                .call(CommonString1.SOAP_ACTION_UPLOAD_IMAGE,
                        envelope);
        Object result = (Object) envelope.getResponse();

        if (!result.toString().equalsIgnoreCase(CommonString1.KEY_SUCCESS)) {

            if (result.toString().equalsIgnoreCase(CommonString1.KEY_FALSE)) {
                return CommonString1.KEY_FALSE;
            }

            SAXParserFactory saxPF = SAXParserFactory.newInstance();
            SAXParser saxP = saxPF.newSAXParser();
            XMLReader xmlR = saxP.getXMLReader();

            // for failure
            FailureXMLHandler failureXMLHandler = new FailureXMLHandler();
            xmlR.setContentHandler(failureXMLHandler);

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(result.toString()));
            xmlR.parse(is);

            failureGetterSetter = failureXMLHandler
                    .getFailureGetterSetter();

            if (failureGetterSetter.getStatus().equalsIgnoreCase(
                    CommonString1.KEY_FAILURE)) {
                errormsg = failureGetterSetter.getErrorMsg();
                return CommonString1.KEY_FAILURE;
            }
        } else {
            new File(Path + path).delete();
            SharedPreferences.Editor editor = preferences
                    .edit();
            editor.putString(CommonString1.KEY_STOREVISITED_STATUS, "");
            editor.commit();
        }

        return result.toString();
    }
}
