package caricoos_app_mobile.main;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import caricoos_app_mobile.main.Analytics.TrackerName;

public class MainActivity extends FragmentActivity {
	 
    private GoogleMap googleMap;
    private String mapType;
	private JSONArray data;
    private int mapLocationPreConfig;
	ArrayList<Marker> markers = new ArrayList<Marker>();
	ArrayList<String> filters = new ArrayList<String>();
	CheckBox caricoos_check;
	CheckBox wflow_check;
	boolean caricoos_checked = true;
	boolean wflow_checked = true;
    private CheckBox pr_preconfig;
    private CheckBox west_preconfig;
    private CheckBox sj_preconfig;
    private CheckBox vi_preconfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            Tracker t = ((Analytics) getApplication()).getTracker(
                    TrackerName.APP_TRACKER);
            t.setScreenName("Main Activity");
            t.send(new HitBuilders.AppViewBuilder().build());
            Log.i("Google Analytics Succeed", "Worked!!");
        }
        catch(Exception  e) {
            Log.e("Google Analytics Error", "" + e.getMessage());
        }

        int titleId = getResources().getIdentifier("action_bar_title", "id","android");
        TextView title = (TextView) findViewById(titleId);
        Typeface typeface = Typeface.createFromAsset(this.getAssets(), "Roboto-LightItalic.ttf");
        title.setTypeface(typeface);
        setUpMapIfNeeded();
        setMapPreConfig();

        try {			
        	data = new JSONArray(readFile("data.json"));
		} catch (JSONException e) { e.printStackTrace(); }
        
        try {
            for(int i = 0 ; i< data.length() ; i++){
                if(data.getJSONObject(i).get("name").equals("CariCOOS Buoy Rincon")) {
                    data.getJSONObject(i).put("plataform", "caricoos");
                }
                createMarkers(data.getJSONObject(i));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
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
		} else if(itemId == R.id.presetTab) {
            showPreSetsOpts();
            return true;
        } else if(itemId == R.id.radarView) {
            Intent i = new Intent(getApplicationContext(), radar.class);
            startActivity(i);
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
            "\t\t - Real time life buoy data (waves, wind and currents). \n" +
            "\t\t - Coastal Mesonet wind stations\n\n" +
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
            googleMap.getUiSettings().setZoomControlsEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setMapPreConfig() {
        if (googleMap != null) {
                String mapPreSet = setPreSet();

                Log.i("PreConfig", "" + mapPreSet);

                String[] parts = mapPreSet.split(":");
                String mapTypePreSet = parts[0];
                String mapLocationPreSet = parts[1];

                mapLocationPreConfig = Integer.parseInt(mapLocationPreSet);

                if(Integer.parseInt(mapTypePreSet) == 1){
                    googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    mapType = "terrain";
                } else if(Integer.parseInt(mapTypePreSet) == 2) {
                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    mapType = "normal";
                } else {
                    googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    mapType = "satellite";
                }

                if(Integer.parseInt(mapLocationPreSet) == 1) { //Default
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(18.450927, -66.109972), 8));
                } else if(Integer.parseInt(mapLocationPreSet) == 2) { //West Coast
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(18.251524, -67.028961), 10));
                } else if(Integer.parseInt(mapLocationPreSet) == 3) { //San Juan
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(18.430108,-66.108856), 12));
                } else { //Virgin Islands
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(18.025751,-64.761658), 9));
                }
            }
    }

    private String setPreSet(){

        File preSet_file = getFileStreamPath("preSet.txt");
        if(!preSet_file.exists()){
            Log.i("FILE", "Doesnt exist");
            String preSetVar = "1:1:";
            createFile("preSet.txt", preSetVar);
            return preSetVar;
        } else {
            Log.i("FILE", "Exist");
            return readFile("preSet.txt");
        }
    }

    @Override
	protected void onPause() {
		super.onPause();
	}

	public void changeType() {
    	if(mapType.equals("terrain")) {
    		googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
    		mapType = "satellite";
            updatePreSets(3, -1);
    	}
    	else if(mapType.equals("satellite")) {
    		googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    		mapType = "normal";
            updatePreSets(2, -1);
    	}
    	else if (mapType.equals("normal")) {
    		googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            mapType = "terrain";
            updatePreSets(1, -1);
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
    	TextView dominant_Wave_Period = (TextView) dialog.findViewById(R.id.mean_wave_period);
    	TextView wind_speed = (TextView) dialog.findViewById(R.id.wind_speed);
    	TextView wind_gust = (TextView) dialog.findViewById(R.id.wind_gust);
    	TextView wind_direction = (TextView) dialog.findViewById(R.id.wind_direction);
    	TextView air_temperature = (TextView) dialog.findViewById(R.id.air_temperature);
    	TextView salinity = (TextView) dialog.findViewById(R.id.salinity);
    	TextView water_temperature = (TextView) dialog.findViewById(R.id.water_temperature);
    	TextView atmospheric_pressure = (TextView) dialog.findViewById(R.id.atmospheric_pressure);
    	TextView current_speed = (TextView) dialog.findViewById(R.id.current_speed);
    	TextView current_direction = (TextView) dialog.findViewById(R.id.current_direction);
    	
    	//Add dataDate
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
	    	try {
	    	current_speed.setText(roundZero(String.format(Locale.US,"%.2f", Double.parseDouble(Bouy.getString("Current Speed (2m)")))));
	    	} catch(JSONException e) {}
	    	try {
	    	current_direction.setText(roundZero(String.format(Locale.US,"%.2f", Double.parseDouble(Bouy.getString("Current Direction (2m)")))));
	    	} catch(JSONException e) {}
	    	
	    	//dataDate set
	    	try {
				dateWaveData.setText(fixDateTime(Bouy.getString("date"), Bouy.getString("time")));
				} catch(JSONException e) {}
	    	try {
				dateMeteoData.setText(fixDateTime(Bouy.getString("date"), Bouy.getString("time")));
				} catch(JSONException e) {}
	    	try {
				dateCurrentData.setText(fixDateTime(Bouy.getString("date"), Bouy.getString("time")));
				} catch(JSONException e) {}
	    	
	    	
	    	//Compare dates
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
	    		    }catch (ParseException e1) { e1.printStackTrace();}
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
    
    private JSONObject getBouy(String name) throws JSONException {
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
    	wflow_check = (CheckBox) dialog.findViewById(R.id.wflow_check);

    	if(caricoos_checked) {
    		caricoos_check.setChecked(true);
    	} else {
    		caricoos_check.setChecked(false);
    	}

		if(wflow_checked) {
			wflow_check.setChecked(true); 			
		} else {
			wflow_check.setChecked(false);
		}

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

				if(wflow_check.isChecked()) {
		        	filters.add("wflow");
		    		wflow_checked = true;
		    		filters.add("ndbc");
		    	} else {
		    		wflow_checked = false;
		    	}
				setMarkersVisible();
		    	createFilter();
		    	dialog.dismiss();
			}
		});
    	dialog.show();
    }

    private void showPreSetsOpts() {

        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.presets);
        dialog.setTitle("Location Pre-Settings");

        pr_preconfig = (CheckBox) dialog.findViewById(R.id.PR_preset_check);
        west_preconfig = (CheckBox) dialog.findViewById(R.id.WEST_preset_check);
        sj_preconfig = (CheckBox) dialog.findViewById(R.id.SJ_preset_check);
        vi_preconfig = (CheckBox) dialog.findViewById(R.id.VI_preset_check);

        if(mapLocationPreConfig == 1) {
            pr_preconfig.setChecked(true);
        } else if(mapLocationPreConfig == 2) {
            west_preconfig.setChecked(true);
        } else if(mapLocationPreConfig == 3) {
            sj_preconfig.setChecked(true);
        } else {
            vi_preconfig.setChecked(true);
        }

        pr_preconfig.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    west_preconfig.setChecked(false);
                    sj_preconfig.setChecked(false);
                    vi_preconfig.setChecked(false);
                    mapLocationPreConfig = 1;
                    updatePreSets(-1,mapLocationPreConfig);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(18.450927, -66.109972), 8));
                } else {
                    //pr_preconfig.setChecked(true);
                   // Toast.makeText(getApplicationContext(), "PR Called", Toast.LENGTH_SHORT).show();
                }
            }
        });

        west_preconfig.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    pr_preconfig.setChecked(false);
                    sj_preconfig.setChecked(false);
                    vi_preconfig.setChecked(false);
                    mapLocationPreConfig = 2;
                    updatePreSets(-1,mapLocationPreConfig);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(18.251524, -67.028961), 10));
                } else {
                    //west_preconfig.setChecked(true);
                   // Toast.makeText(getApplicationContext(), "WEST Called", Toast.LENGTH_SHORT).show();
                }
            }
        });

        sj_preconfig.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    pr_preconfig.setChecked(false);
                    west_preconfig.setChecked(false);
                    vi_preconfig.setChecked(false);
                    mapLocationPreConfig = 3;
                    updatePreSets(-1,mapLocationPreConfig);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(18.430108,-66.108856), 12));
                } else {
                    //sj_preconfig.setChecked(true);
                }
            }
        });

        vi_preconfig.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    pr_preconfig.setChecked(false);
                    sj_preconfig.setChecked(false);
                    west_preconfig.setChecked(false);
                    mapLocationPreConfig = 4;
                    updatePreSets(-1,mapLocationPreConfig);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(18.025751,-64.761658), 9));
                } else {
                    //vi_preconfig.setChecked(true);
                }
            }
        });

        Button filter_button = (Button) dialog.findViewById(R.id.presets_button);
        filter_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
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
    	try {
    		lat = object.getString("latitude");
	    	log = object.getString("longitude");
			objPlatform = object.getString("plataform");
			objName = object.getString("name");
			
			//To compare dates
			Calendar c = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String ThisDate = sdf.format(c.getTime());
            Log.i("DATE1", ""+ThisDate);

            try{
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date date1 = formatter.parse(ThisDate);
                Date date2 = formatter.parse(object.getString("date"));
                Log.i("DATE2", ""+date2.toString());

                if (date1.compareTo(date2) > 0) {
	       		     isUpdate = false;                          
	       		}
	    	} catch (ParseException e1) { e1.printStackTrace(); }

		} catch (JSONException e) {	}
    	
	    if(objPlatform.equals("wflow") || objPlatform.equals("ndbc")) {
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

	    } else if((objPlatform).equals("caricoos")){
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
    
    private String roundZero(String num){
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

    private String fixDateTime(String numDate, String numTime){
    	String[] newDate = numDate.split("-");
    	String[] newTime = numTime.split(":");

    	Calendar DateTime = Calendar.getInstance();
    	DateTime.set(Integer.parseInt(newDate[0]), Integer.parseInt(newDate[1])-1, Integer.parseInt(newDate[2]), Integer.parseInt(newTime[0]), Integer.parseInt(newTime[1]),Integer.parseInt(newTime[2]));
    	DateTime.add(Calendar.HOUR, -4);
    	Date date = new Date();
    	date = DateTime.getTime();
    	SimpleDateFormat format = new SimpleDateFormat();
    	format.applyLocalizedPattern("LL/d/yyyy kk:mm:ss");
    	
    	return format.format(date);
    }

    public void createFile(String filename, String data) {
        FileOutputStream fos;
        try {
            fos = openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(data.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updatePreSets(int mapType, int mapLocation) {
        //Sorry. is 3:54AM and we have a deadline.
        String newPreset = "";
        if(mapType > 0) {
            String presetTemp = readFile("preSet.txt");
            String[] parts = presetTemp.split(":");
            String part1 = parts[0];
            String part2 = parts[1];

            newPreset = mapType + ":" + part2 + ":";
        }

        if(mapLocation > 0) {
            String presetTemp = readFile("preSet.txt");
            String[] parts = presetTemp.split(":");
            String part1 = parts[0];
            String part2 = parts[1];

            newPreset = part1 + ":" + mapLocation + ":";
        }
        File preSet_file = getFileStreamPath("preSet.txt");
        preSet_file.delete();
        createFile("preSet.txt", newPreset);
        Log.i("NEW_PRESET", "" + newPreset);
    }
}
