package caricoos_app_mobile.main;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;



import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity {
	 
    private GoogleMap googleMap;
    private static final LatLng LOCATION = new LatLng(18.450927,-66.109972);
    private String mapType = "satellite";    
	private JSONArray data;
	ArrayList<Marker> markers = new ArrayList<Marker>();
	ArrayList<String> filters = new ArrayList<String>();
	TabHost tabHost;
	CheckBox caricoos_check;
	CheckBox nsw_check;
	CheckBox wflow_check;
	CheckBox ndbc_check;
	CheckBox swan_check;
	CheckBox wave_check;
	boolean caricoos_checked = true;
	boolean nsw_checked = true;
	boolean wflow_checked = true;
	boolean ndbc_checked = true;
	boolean swan_checked = true;
	boolean wave_checked = true;
  
    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int titleId = getResources().getIdentifier("action_bar_title", "id","android");
        TextView title = (TextView) findViewById(titleId);
       Typeface typeface = Typeface.createFromAsset(this.getAssets(), "Roboto-LightItalic.ttf");
          title.setTypeface(typeface);

          
        try {
        	setUpMapIfNeeded(); 
        } catch (Exception e) { e.printStackTrace();}
        
        try {			
        	data = new JSONArray(readFile("data.json"));
            Log.i("caricoos", data.toString());
		} catch (JSONException e) { e.printStackTrace();Log.e("caricoos", readFile("data.json").toString());
}
        
        try {
        for(int i = 0 ; i< data.length() ; i++){
        	
        	if(data.getJSONObject(i).get("name").equals("CariCOOS Buoy Rincon")) {
        		data.getJSONObject(i).put("plataform", "caricoos");
        	}
        	
        	createMarkers(data.getJSONObject(i));
        	}
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
		if (itemId == R.id.type) {
			changeType();
			return true;
		} else if(itemId == R.id.filter) {
			generateFilters();
			return true;
		} else if(itemId == R.id.about) {
			showAbout();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
    }

    public void showAbout() {
    	final Dialog dialog = new Dialog(MainActivity.this);
    	dialog.setCancelable(true);    	
    	dialog.setContentView(R.layout.about);
    	dialog.setTitle("About");
    	
    	TextView about_content = (TextView) dialog.findViewById(R.id.about_content);
    	about_content.setText(	
    	"CariCOOS Coastal Weather app available for Android � is an ad-free app providing users an " +
    	"integrated view of ocean conditions in the US Caribbean.\n\n" +
    	"This app provides reliable information including: \n" +
    	"\t\t� Real time life buoy data (waves, wind and currents). \n" +
    	"\t\t�Coastal Mesonet wind stations\n\n" +
    	"Excellent tool for surfing, boating, fishing, sailing and marine operations. \n" +
    	"Includes all US Caribbean coastal regions (Puerto Rico and US Virgin Islands)."
    	); 
    	
    	
    	TextView website = (TextView) dialog.findViewById(R.id.website);
    	website.setText("CariCOOS.org");
    	website.setTextColor(getResources().getColor(R.color.blue));
    	website.setOnClickListener(new OnClickListener() {
			/**
			 * Open Internet browser if notification pressed.
			 */
    		@Override
			public void onClick(View v) {
				String url = "http://www.caricoos.org/drupal/";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});
    	dialog.show();
    }
    
    private void setUpMapIfNeeded() {
        if (googleMap == null) {
        	googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            if (googleMap != null) {
            	googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            	googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LOCATION, 8));
            }
        }
    }

 
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }
    

    @Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// startActivity(new Intent( MainActivity.this, splashActivity.class));
		// finish();
	}

	public void changeType() {
    	if(mapType.equals("satellite")) {
    		googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    		mapType = "normal";
    	}
    	else if(mapType.equals("normal")) {
    		googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    		mapType = "terrain";
    	}
    	else if (mapType.equals("terrain")) {
    		googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
    		mapType = "satellite";
    	}
    }
    
    private String readFile(String filename) {
    	String data;
    	try {
    	    BufferedReader inputReader = new BufferedReader(
    	    		new InputStreamReader(openFileInput(filename)));
    	    String inputString;
    	    StringBuffer stringBuffer = new StringBuffer();                
    	    while ((inputString = inputReader.readLine()) != null) {
    	        stringBuffer.append(inputString + "\n");
    	    }
    	    data = stringBuffer.toString();
    	    inputReader.close();
    	} catch (IOException e) {
    	    e.printStackTrace();
    	    data = null;
    	}
    	return data;
    }
    
    public void createPopUp(String buoy, int tab) {
    	Dialog dialog = new Dialog(MainActivity.this);
    	dialog.setCancelable(true);    	
    	dialog.setContentView(R.layout.popup);
    	dialog.setTitle(buoy);
    	
    	boolean isUpdate = true;
    	
    	TabHost tabs = (TabHost) dialog.findViewById(R.id.tabHost);
    	tabs.setup();
    	

    	TabSpec tspec11 = tabs.newTabSpec("Tab1");
    	tspec11.setIndicator("Wave");

    	tspec11.setContent(R.id.wave);
    	tabs.addTab(tspec11);

    	TabSpec tspec2 = tabs.newTabSpec("Tab2");
    	tspec2.setIndicator("Meteo");

    	tspec2.setContent(R.id.meteo);
    	tabs.addTab(tspec2);
    	
    	TabSpec tspec3 = tabs.newTabSpec("Tab3");
    	tspec3.setIndicator("Current");
    	
    	tspec3.setContent(R.id.current);
    	tabs.addTab(tspec3);
    	
    	TextView wave_direction = (TextView) dialog.findViewById(R.id.wave_direction);
    	TextView wave_height = (TextView) dialog.findViewById(R.id.wave_height);
    	TextView dominant_Wave_Period = (TextView) dialog.findViewById(R.id.mean_wave_period); //
    	TextView wind_speed = (TextView) dialog.findViewById(R.id.wind_speed);
    	TextView wind_gust = (TextView) dialog.findViewById(R.id.wind_gust);
    	TextView wind_direction = (TextView) dialog.findViewById(R.id.wind_direction);
    	TextView air_temperature = (TextView) dialog.findViewById(R.id.air_temperature);
    	TextView salinity = (TextView) dialog.findViewById(R.id.salinity);
    	TextView water_temperature = (TextView) dialog.findViewById(R.id.water_temperature);
    	TextView atmospheric_pressure = (TextView) dialog.findViewById(R.id.atmospheric_pressure);
    	//TextView surface_current = (TextView) dialog.findViewById(R.id.surface_current);
    	TextView current_speed = (TextView) dialog.findViewById(R.id.current_speed);
    	TextView current_direction = (TextView) dialog.findViewById(R.id.current_direction);
    	
    	//Bergo add dataDate
    	TextView dateWaveData = (TextView) dialog.findViewById(R.id.date_wave_data);
    	TextView dateMeteoData = (TextView) dialog.findViewById(R.id.date_meteo_data);
    	TextView dateCurrentData = (TextView) dialog.findViewById(R.id.date_current_data);
    	
    	TextView dateWave = (TextView) dialog.findViewById(R.id.date_wave);
    	TextView dateMeteo = (TextView) dialog.findViewById(R.id.date_meteo);
    	TextView dateCurrent = (TextView) dialog.findViewById(R.id.date_current);
    	
    	String date = readFile("date.txt");
    	
    	dateWave.setText(date);
    	dateMeteo.setText(date);
    	dateCurrent.setText(date);
    	
    	try {
			JSONObject Bouy = getBouy(buoy);
			try {
			wave_direction.setText(roundZero(String.format(Locale.US,"%.2f", Double.parseDouble(Bouy.getString("Mean Wave Direction (0m)")))));
			} catch(JSONException e) {}
			try {
	    	wave_height.setText(roundZero(String.format(Locale.US,"%.2f", Double.parseDouble(Bouy.getString("Significant Wave Height (0m)"))*3.28084)));
			} catch(JSONException e) {}
	    	try {
	    		dominant_Wave_Period.setText(roundZero(String.format(Locale.US,"%.2f", Double.parseDouble(Bouy.getString("Dominant Wave Period (0m)")))));
	    	} catch(JSONException e) {}
	    	try {
	    	wind_speed.setText(roundZero(String.format(Locale.US,"%.2f", Double.parseDouble(Bouy.getString("Wind Speed (-4m)")))));
	    	} catch(JSONException e) {}
	    	try {
	    	wind_gust.setText(roundZero(String.format(Locale.US,"%.2f", Double.parseDouble(Bouy.getString("Wind Gust (-4m)")))));
	    	} catch(JSONException e) {}
	    	try {
	    		
	    	wind_direction.setText(roundZero(String.format(Locale.US,"%.2f", Double.parseDouble(Bouy.getString("Wind Direction (-4m)")))));
	    	} catch(JSONException e) {}
	    	try {
	    		if(!Bouy.getString("Air Temperature (-3m)").contains("0.000"))
	    	air_temperature.setText(roundZero(String.format(Locale.US,"%.2f", Double.parseDouble(Bouy.getString("Air Temperature (-3m)")))));
	    	} catch(JSONException e) {}
	    	try {
	    	salinity.setText(roundZero(String.format(Locale.US,"%.2f", Double.parseDouble(Bouy.getString("Salinity (1m)")))));
	    	} catch(JSONException e) {}
	    	try {
	    	water_temperature.setText(roundZero(String.format(Locale.US,"%.2f", Double.parseDouble(Bouy.getString("Water Temperature (1m)")))));
	    	} catch(JSONException e) {}
	    	try {
	    	atmospheric_pressure.setText(roundZero(String.format(Locale.US,"%.2f", Double.parseDouble(Bouy.getString("Barometric Pressure (-3m)")))));
	    	} catch(JSONException e) {}
	    	/*try {
	    	surface_current.setText(roundZero(Bouy.getString("Current Direction (2m)")));
	    	} catch(JSONException e) {}*/
	    	try {
	    	current_speed.setText(roundZero(String.format(Locale.US,"%.2f", Double.parseDouble(Bouy.getString("Current Speed (2m)")))));
	    	} catch(JSONException e) {}
	    	try {
	    	current_direction.setText(roundZero(String.format(Locale.US,"%.2f", Double.parseDouble(Bouy.getString("Current Direction (2m)")))));
	    	} catch(JSONException e) {}
	    	
	    	//Bergo: dataDate set
	    	try {
				dateWaveData.setText(fixDateTime(Bouy.getString("date"), Bouy.getString("time")));
				} catch(JSONException e) {}
	    	try {
				dateMeteoData.setText(fixDateTime(Bouy.getString("date"), Bouy.getString("time")));
				} catch(JSONException e) {}
	    	try {
				dateCurrentData.setText(fixDateTime(Bouy.getString("date"), Bouy.getString("time")));
				} catch(JSONException e) {}
	    	
	    	
	    	//Bergo: to compare dates
			Calendar c = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String ThisDate = sdf.format(c.getTime()); 
	    		try{
	    		     SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	    		     Date date1 = formatter.parse(ThisDate);
	    		     Date date2 = formatter.parse(Bouy.getString("date"));
	    		       if (date1.compareTo(date2) > 0 && Bouy.getString("plataform").equals("caricoos"))
		       		    {
		       		     isUpdate = false;                          
		       		    }
	    		    }catch (ParseException e1) 
	    		      { e1.printStackTrace();}
	    		//Bergo
	    		tab = 1;
	    		if(Bouy.getString("plataform").equals("caricoos"))
	    			tab = 0;
	    		
    	} catch (JSONException e) {	
			e.printStackTrace(); 
		}
    	tabs.setCurrentTab(tab);	
    	
    	if(isUpdate)
    		dialog.show();
    }
    
    JSONObject getBouy(String name) throws JSONException {
    	for(int i = 0 ; i< data.length() ; i++){
    		String target = data.getJSONObject(i).getString("name");
    		if(target.equals(name)) {
        		return data.getJSONObject(i);
        	}
        }
    	return null;
    }
    
    public void generateFilters() {
    	final Dialog dialog = new Dialog(MainActivity.this);
    	dialog.setCancelable(true);    	
    	dialog.setContentView(R.layout.filter);
    	dialog.setTitle("Platform Filters");
    	caricoos_check = (CheckBox) dialog.findViewById(R.id.caricoos_check);
    	//nsw_check = (CheckBox) dialog.findViewById(R.id.nsw_check);
    	wflow_check = (CheckBox) dialog.findViewById(R.id.wflow_check);
    	//ndbc_check = (CheckBox) dialog.findViewById(R.id.ndbc_check);
    	//swan_check = (CheckBox) dialog.findViewById(R.id.swan_check);
    	//wave_check = (CheckBox) dialog.findViewById(R.id.wave_check);
    	    	
    	if(caricoos_checked) {
    		caricoos_check.setChecked(true);
    	} else {
    		caricoos_check.setChecked(false);
    	}
//		if(nsw_checked) {
//			nsw_check.setChecked(true);
//		} else {
//			nsw_check.setChecked(false);
//		}
		if(wflow_checked) {
			wflow_check.setChecked(true); 			
		} else {
			wflow_check.setChecked(false);  			
			

		}
		/*if(ndbc_checked) {
			ndbc_check.setChecked(true);
		} else {
			ndbc_check.setChecked(false);
		}
		if(swan_checked) {
			swan_check.setChecked(true);
		} else {
			swan_check.setChecked(false);
		}

		if(wave_checked) {
			wave_check.setChecked(true);
		} else {
			wave_check.setChecked(false);
		}*/

    	Button filter_button = (Button) dialog.findViewById(R.id.filter_button);
    	filter_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				filters.clear();
				if(caricoos_check.isChecked()) {
		        	filters.add("caricoos");
		        	caricoos_checked = true;
		        } else {
		        	caricoos_checked = false;
		        }
//				if(nsw_check.isChecked()) {
//		        	filters.add("nsw");
//		    		nsw_checked = true;
//		    	} else {
//		    		nsw_checked = false;
//		        }
				if(wflow_check.isChecked()) {
		        	filters.add("wflow");
		    		wflow_checked = true;
		    		//b
		    		filters.add("ndbc");
		        	//ndbc_checked = true;
		        	//
		    		
		    	} else {
		    		wflow_checked = false;
		    		//b
		        	//ndbc_checked = false;
		    		//
		    	}
				/*if(ndbc_check.isChecked()) {
		        	filters.add("ndbc");
		        	ndbc_checked = true;
		        } else {
		        	ndbc_checked = false;
		        }b*/
/*				if(swan_check.isChecked()) {
		        	filters.add("swan");
		        	swan_checked = true;
		        } else {
		        	swan_checked = false;
		        }
*/				/*if(wave_check.isChecked()) {
		        	filters.add("wave");
		        	wave_checked = true;
		        } else {
		        	wave_checked = false;
		        }b*/

				setMarkersVisible();
		    	createFilter();
		    	dialog.dismiss();
			}
		});
    	dialog.show();
    }
    
    public void createFilter() {
    	for(int i = 0 ; i < filters.size() ; i++) {
    		String filter = filters.get(i);
    		for(int j = 0 ; j < markers.size() ; j++) {
    			if(((markers.get(j)).getSnippet().toString()).equals(filter + " (more info)")) {
    				markers.get(j).setVisible(true);
    			}
    		}
    	}
    }
    
    public void setMarkersVisible() {
		for(int i = 0 ; i < markers.size() ; i++) {
			(markers.get(i)).setVisible(false);
		}
	}
    
    @SuppressLint("SimpleDateFormat")
	public void createMarkers(JSONObject object) {
    	String objPlatform = "";
    	String objName = "";
    	String lat = "";
    	String log = "";
    	
    	boolean isUpdate = true;
    	int icon = 0;
//    	Log.i("caricoos", "bergo");
    	try {
    		lat = object.getString("latitude");
	    	log = object.getString("longitude");
			objPlatform = object.getString("plataform");
			objName = object.getString("name");
			
			//Bergo: to compare dates
			Calendar c = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String ThisDate = sdf.format(c.getTime()); 
	    		try{

	    		      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	    		     
	    		       Date date1 = formatter.parse(ThisDate);
	    		      
	    		       Date date2 = formatter.parse(object.getString("date"));
	    		       
	    		    // Toast.makeText(getApplicationContext() ,date1.toString(), Toast.LENGTH_LONG).show();

	    		       if (date1.compareTo(date2) > 0)
	       		    {
	       		     isUpdate = false;                          
	       		    }
	    		    } catch (ParseException e1) 
	    		      {
	    		    // TODO Auto-generated catch block
	    		    e1.printStackTrace();
	    		                        }
	    		//Bergo
		} catch (JSONException e) {	}
    	
	    if(/*objPlatform.equals("nws") ||*/ objPlatform.equals("wflow") || objPlatform.equals("ndbc")) {
	    	     icon = R.drawable.buoy_red;
	    	 if(!isUpdate)
	    		icon = R.drawable.travel_warning; 
	    	
	       	LatLng location = new LatLng(Double.valueOf(lat), Double.valueOf(log));
	       	Marker marker = googleMap.addMarker(new MarkerOptions()
	       		.position(location)
        		.title(objName)
	       	    .snippet(objPlatform + " (more info)")
	       	    .icon(BitmapDescriptorFactory.fromResource(icon)));
	   		markers.add(marker);
	   		/*googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
	   			@Override
				public void onInfoWindowClick(com.google.android.gms.maps.model.Marker marker) {	
	   				createPopUp(marker.getTitle(),1);
	   			}});*/
	    		
	    }/* else if((objPlatform).equals("wave")) { //to add forecast add (objPlatform).equals("swan")
	    	LatLng location = new LatLng(Double.valueOf(lat), Double.valueOf(log));
		    Marker marker = googleMap.addMarker(new MarkerOptions()
		    	.position(location)
		    	.title(objName)
	        	.snippet(objPlatform + " (more info)")
	        	.icon(BitmapDescriptorFactory.fromResource(R.drawable.buoy_blue)));
    		markers.add(marker);
	    	googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
	    		@Override
				public void onInfoWindowClick(com.google.android.gms.maps.model.Marker marker) {	
	    			createPopUp(marker.getTitle());
	    		}});	    		
	    }*/ else if((objPlatform).equals("caricoos")){
	    	    icon = R.drawable.buoy_coos;
	    	if(!isUpdate)
	    		icon = R.drawable.travel_warning;
	    	
	        LatLng location = new LatLng(Double.valueOf(lat), Double.valueOf(log));
	       	Marker marker = googleMap.addMarker(new MarkerOptions()
	       		.position(location)
	       		.title(objName)
	        	.snippet(objPlatform + " (more info)")
	        	.icon(BitmapDescriptorFactory.fromResource(icon)));
    		markers.add(marker);

	    }
	    	googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
	    		@Override
				public void onInfoWindowClick(com.google.android.gms.maps.model.Marker marker) {	
	    			createPopUp(marker.getTitle(),0);
	    		}});
    }
    
    String roundZero(String num){
    	int i = num.length()-1;
    	String newNum = "";
    	for(; i >= 0; i--){
    		if(num.charAt(i) != '0' )
    			if(num.charAt(i) == ',' || num.charAt(i) == '.'){
    				i--;
        			break;
    			}
    			else
    			break;
    	}
    	for(int j = 0; j <= i ; j++){
    		newNum += num.charAt(j);
    	}
    	return newNum;
    }
    String fixDateTime(String numDate, String numTime){
    	String[] newDate = numDate.split("-");
    	String[] newTime = numTime.split(":");
    	
    	
    	 
    	Calendar DateTime = Calendar.getInstance();
    	DateTime.set(Integer.parseInt(newDate[0]), Integer.parseInt(newDate[1])-1, Integer.parseInt(newDate[2]), Integer.parseInt(newTime[0]), Integer.parseInt(newTime[1]),Integer.parseInt(newTime[2]));
    	//Toast.makeText(getApplicationContext() , DateTime.getTime().toString(), Toast.LENGTH_LONG).show();
    	DateTime.add(Calendar.HOUR, -4);
    	Date date = new Date();
    	date = DateTime.getTime();
    	SimpleDateFormat format = new SimpleDateFormat();
    	format.applyLocalizedPattern("LL/d/yyyy kk:mm:ss");
    	
    	
    	
    	//Toast.makeText(getApplicationContext() , date.toString(), Toast.LENGTH_LONG).show();
    	return format.format(date);
    }
    
}
