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
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private JSONArray data_forecast;
    private JSONArray data_forecast2;
    private int mapLocationPreConfig;

	ArrayList<Marker> markers = new ArrayList<Marker>();
    ArrayList<String> markers_keys = new ArrayList<String>();
    ArrayList<Marker> markers_forecast = new ArrayList<Marker>();
    ArrayList<String> markers_keys_forecast= new ArrayList<String>();
    ArrayList<Marker> markers_forecast2 = new ArrayList<Marker>();
    ArrayList<String> markers_keys_forecast2 = new ArrayList<String>();
	ArrayList<String> filters = new ArrayList<String>();

	CheckBox caricoos_check;
	CheckBox wflow_check;
    CheckBox forecast_check;
    CheckBox forecast2_check;

	boolean caricoos_checked = false;
	boolean wflow_checked = false;
    boolean forecast_checked = false;
    boolean forecast2_checked = false;

    private CheckBox pr_preconfig;
    private CheckBox west_preconfig;
    private CheckBox sj_preconfig;
    private CheckBox vi_preconfig;

    private LinearLayout leyend_tab;
    private LinearLayout leyend_body;
    private ImageView leyend_arrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            Tracker t = ((Analytics) getApplication()).getTracker(
                    TrackerName.APP_TRACKER);
            t.setScreenName("Main Activity");
            t.send(new HitBuilders.AppViewBuilder().build());
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

        try {
            data_forecast = new JSONArray(readFile("data_forecast.json"));
        } catch (JSONException e) { e.printStackTrace(); }

        try {
            for(int i = 0 ; i< data_forecast.length() ; i++){
                createMarkersForecast(data_forecast.getJSONObject(i));
            }


        } catch (Exception e) { e.printStackTrace(); }

        try {
            data_forecast2 = new JSONArray(readFile("data_forecast_ofs.json"));
        } catch (JSONException e) { e.printStackTrace(); }

        try {
            for(int i = 0 ; i< data_forecast2.length() ; i++){
                createMarkersForecast2(data_forecast2.getJSONObject(i));
            }


        } catch (Exception e) { e.printStackTrace(); }


        leyend_tab = (LinearLayout) findViewById(R.id.leyend_header);
        leyend_body = (LinearLayout) findViewById(R.id.leyend);
        leyend_arrow = (ImageView) findViewById(R.id.leyend_arrow);

        leyend_tab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (leyend_body.getVisibility() == View.GONE) {
                    leyend_body.setVisibility(View.VISIBLE);
                    leyend_arrow.setImageResource(R.drawable.up);
                } else {
                    leyend_body.setVisibility(View.GONE);
                    leyend_arrow.setImageResource(R.drawable.down);
                }
            }
        });

        setInfoWindow();
        generateInitialFilters();
        setPreSetFilters();
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
        } else if(itemId == R.id.refresh) {
            doRefresh();
            return true;
        } else if(itemId == R.id.about) {
            showAbout();
            return true;
        } else if(itemId == R.id.disclaimer) {
            showDisclaimer();
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
            "CariCOOS Coastal Weather app available for Android is an ad-free app providing users an " +
            "integrated view of ocean conditions in the US Caribbean.\n\n" +
            "This app provides reliable information including: \n" +
            "\t\t - Real time life buoy data (waves, wind and currents). \n" +
            "\t\t - Coastal Mesonet wind stations\n\n" +
            "Excellent tool for surfing, boating, fishing, sailing and marine operations. \n" +
            "Includes all US Caribbean coastal regions (Puerto Rico and US Virgin Islands)."
    	);

        Button close_about_btn = (Button) dialog.findViewById(R.id.close_about);
        close_about_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

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

    public void showDisclaimer() {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.disclaimer);
        dialog.setTitle("Disclaimer");

        TextView about_content = (TextView) dialog.findViewById(R.id.disclaimer_content);
        about_content.setText(
                "This information is presented as a good faith service to the scientific community, " +
                        "the public in general and to our colleagues and friends. The information, " +
                        "views and opinions herein provided should not be viewed as formally" +
                        " accurate scientific data and/or advice that can be relied upon without " +
                        "proper verification and validation. This service should not be construed " +
                        "as a substitute for specific data that could be obtained though official " +
                        "sources. If any inaccuracy is observed, please inform CaRA as soon as " +
                        "possible for verification and correction, as necessary. Use of and " +
                        "reliance upon the information provided in this web site signifies that its " +
                        "user(s) understands and has(ve) accepted of the above mentioned " +
                        "caveat and conditions. The location of each observation instrument in " +
                        "this application does not necessarily represent actual location."
        );

        Button close_about_btn = (Button) dialog.findViewById(R.id.close_disclaimer);
        close_about_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
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
            String preSetVar = "1:1:";
            createFile("preSet.txt", preSetVar);
            return preSetVar;
        } else {
            return readFile("preSet.txt");
        }
    }

    private String setPreSetFilters(){
        File preSetFilter_file = getFileStreamPath("preSetFilters.txt");
        if(!preSetFilter_file.exists()){
            String preSetVarFilter = "1:1:0:1:";
            createFile("preSetFilters.txt", preSetVarFilter);
            return preSetVarFilter;
        } else {
            return readFile("preSetFilters.txt");
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
    	final Dialog dialog = new Dialog(MainActivity.this);
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

        ImageView wave_icon = (ImageView) dialog.findViewById(R.id.wave_direction_img);
        ImageView wind_icon = (ImageView) dialog.findViewById(R.id.wind_direction_img);
        ImageView current_icon = (ImageView) dialog.findViewById(R.id.current_direction_img);

        Button closeButton = (Button) dialog.findViewById(R.id.close_popup);
        closeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    	
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
                String wave_dir = roundZero(String.format(Locale.US,"%.2f", Double.parseDouble(Bouy.getString("Mean Wave Direction (0m)"))));
                wave_direction.setText(wave_dir);
                generateDirectionIcon(Integer.parseInt(wave_dir), wave_icon);
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
                String wind_dir = roundZero(String.format(Locale.US,"%.2f", Double.parseDouble(Bouy.getString("Wind Direction (-4m)"))));
                wind_direction.setText(wind_dir);
                generateDirectionIcon(Integer.parseInt(wind_dir), wind_icon);
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
                String current_dir = roundZero(String.format(Locale.US,"%.2f", Double.parseDouble(Bouy.getString("Current Direction (2m)"))));
                current_direction.setText(current_dir);
                generateDirectionIcon(Integer.parseInt(current_dir), current_icon);
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
        forecast_check = (CheckBox) dialog.findViewById(R.id.forecast_check);
        forecast2_check = (CheckBox) dialog.findViewById(R.id.forecast2_check);

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

        if(forecast_checked) {
            forecast_check.setChecked(true);
        } else {
            forecast_check.setChecked(false);
        }

        if(forecast2_checked) {
            forecast2_check.setChecked(true);
        } else {
            forecast2_check.setChecked(false);
        }

    	Button filter_button = (Button) dialog.findViewById(R.id.filter_button);
    	filter_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				filters.clear();

                int coos = 0, wind = 0, forecast1 = 0, forecast2 = 0;

				if(caricoos_check.isChecked()) {
		        	filters.add("caricoos");
		        	caricoos_checked = true;
                    coos = 1;
		        } else {
		        	caricoos_checked = false;
		        }

				if(wflow_check.isChecked()) {
		        	filters.add("wflow");
		    		wflow_checked = true;
		    		filters.add("ndbc");
                    wind = 1;
		    	} else {
		    		wflow_checked = false;
		    	}

                if(forecast_check.isChecked()) {
                    filters.add("forecast");
                    forecast_checked = true;
                    forecast1 = 1;
                } else {
                    forecast_checked = false;
                }

                if(forecast2_check.isChecked()) {
                    filters.add("forecast2");
                    forecast2_checked = true;
                    forecast2 = 1;
                } else {
                    forecast2_checked = false;
                }

                updatePreSetsFilters(coos, wind, forecast1, forecast2);

                setMarkersInvisible();
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
                    dialog.dismiss();
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
                    dialog.dismiss();
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
                    dialog.dismiss();
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
                    dialog.dismiss();
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

            if(filter.equals("forecast")) {
                for(int j = 0 ; j < markers_forecast.size() ; j++) {
                    markers_forecast.get(j).setVisible(true);
                }
            }

            if(filter.equals("forecast2")) {
                for(int j = 0 ; j < markers_forecast2.size() ; j++) {
                    markers_forecast2.get(j).setVisible(true);
                }
            }

    		for(int j = 0 ; j < markers.size() ; j++) {
    			if(((markers_keys.get(j)).toString()).equals(filter)) {
    				markers.get(j).setVisible(true);
    			}
    		}
    	}
    }

    private void generateInitialFilters() {

        String filtersPreSet = setPreSetFilters();

        String[] parts = filtersPreSet.split(":");
        String coos = parts[0];
        String wind = parts[1];
        String forecast1 = parts[2];
        String forecast2 = parts[3];

        if(Integer.parseInt(coos) == 1) {
            caricoos_checked = true;
        }
        if(Integer.parseInt(wind) == 1) {
            wflow_checked = true;
        }
        if(Integer.parseInt(forecast1) == 1) {
            forecast_checked = true;
        }
        if(Integer.parseInt(forecast2) == 1) {
            forecast2_checked = true;
        }

        filters.clear();

        if(caricoos_checked) {
            filters.add("caricoos");
        }

        if(wflow_checked) {
            filters.add("wflow");
            filters.add("ndbc");
        }

        if(forecast_checked) {
            filters.add("forecast");
        }

        if(forecast2_checked) {
            filters.add("forecast2");
        }

        setMarkersInvisible();
        createFilter();
    }

    public void setMarkersInvisible() {
		for(int i = 0 ; i < markers.size() ; i++) {
			(markers.get(i)).setVisible(false);
		}
        for(int i = 0 ; i < markers_forecast.size() ; i++) {
            (markers_forecast.get(i)).setVisible(false);
        }
        for(int i = 0 ; i < markers_forecast2.size() ; i++) {
            (markers_forecast2.get(i)).setVisible(false);
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

            try{
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date date1 = formatter.parse(ThisDate);
                Date date2 = formatter.parse(object.getString("date"));

                if (date1.compareTo(date2) > 0) {
	       		     isUpdate = false;                          
	       		}
	    	} catch (ParseException e1) { e1.printStackTrace(); }

		} catch (JSONException e) {	}
    	
	    if(objPlatform.equals("wflow") || objPlatform.equals("ndbc")) {

            if(!isUpdate) {
                icon = R.drawable.travel_warning;
                LatLng location = new LatLng(Double.valueOf(lat), Double.valueOf(log));
                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title(objName)
                        .snippet("Instrument out of order")
                        .icon(BitmapDescriptorFactory.fromResource(icon)));
                markers.add(marker);
                markers_keys.add(objPlatform);
            } else {
                icon = R.drawable.buoy_red;
                LatLng location = new LatLng(Double.valueOf(lat), Double.valueOf(log));
                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title(objName)
                        .snippet("Press for more info")
                        .icon(BitmapDescriptorFactory.fromResource(icon)));
                markers.add(marker);
                markers_keys.add(objPlatform);

            }

	    } else if((objPlatform).equals("caricoos")){

            if(!isUpdate) {
                icon = R.drawable.travel_warning;
                LatLng location = new LatLng(Double.valueOf(lat), Double.valueOf(log));
                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title(objName)
                        .snippet("Instrument out of order")
                        .icon(BitmapDescriptorFactory.fromResource(icon)));
                markers.add(marker);
                markers_keys.add(objPlatform);
            } else {
                icon = R.drawable.buoy_coos;
                LatLng location = new LatLng(Double.valueOf(lat), Double.valueOf(log));
                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title(objName)
                        .snippet("Press for more info")
                        .icon(BitmapDescriptorFactory.fromResource(icon)));
                markers.add(marker);
                markers_keys.add(objPlatform);
            }
	    }

    }

    public void createMarkersForecast(JSONObject object) {
        String NAME = "";
        String FANCYNAME = "";
        String LAT = "";
        String LON = "";

        try {
            NAME = object.getString("NAME");
            FANCYNAME = object.getString("FANCYNAME");
            LAT = object.getString("LAT");
            LON = object.getString("LON");

            LatLng location = new LatLng(Double.valueOf(LAT), Double.valueOf(LON));
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(FANCYNAME + " ("+ NAME +")")
                    .snippet("Press to get forecast")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.buoy_blue)));

            markers_forecast.add(marker);
            markers_keys_forecast.add("forecast");

        } catch (JSONException e) {	}
    }

    public void createMarkersForecast2(JSONObject object) {
        String NAME = "";
        String FANCYNAME = "";
        String LAT = "";
        String LON = "";

        try {
            NAME = object.getString("NAME");
            FANCYNAME = object.getString("FANCYNAME");
            LAT = object.getString("LAT");
            LON = object.getString("LON");

            LatLng location = new LatLng(Double.valueOf(LAT), Double.valueOf(LON));
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(FANCYNAME + " ("+ NAME +")")
                    .snippet("Press to get forecast")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.buoy_blue)));

            markers_forecast2.add(marker);
            markers_keys_forecast2.add("forecast2");

        } catch (JSONException e) {	}
    }

    private void setInfoWindow() {
        googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(com.google.android.gms.maps.model.Marker marker) {
                if(marker.getSnippet().toString().equals("Press to get forecast")) {
                    String[] name = ((marker.getTitle()).toString()).split("\\(|\\)");
                    Intent i = new Intent(getApplicationContext(), forecast.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("url", name[1]);
                    i.putExtras(bundle);
                    startActivity(i);
                } else {
                    if (!(marker.getSnippet().toString()).equals("Instrument out of order")) {
                        createPopUp(marker.getTitle(), 0);
                    }
                }
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

    private void generateDirectionIcon(int dir, ImageView icon) {
        if(dir > 315 + 22.5 && dir <= 360 ||
                dir >= 0 && dir <= 45 - 22.5) {                 //North
            icon.setImageResource(R.drawable.north);
        } else if(dir > 0 + 22.5 && dir <= 90 - 22.5) {         //North East
            icon.setImageResource(R.drawable.north_east);
        } else if(dir > 45 + 22.5 && dir <= 135 - 22.5) {       //East
            icon.setImageResource(R.drawable.east);
        } else if(dir > 90 + 22.5 && dir <= 180 - 22.5) {       //South East
            icon.setImageResource(R.drawable.south_east);
        } else if(dir > 135 + 22.5 && dir <= 225 - 22.5) {      //South
            icon.setImageResource(R.drawable.south);
        } else if(dir > 180 + 22.5 && dir <= 270 - 22.5) {      //South West
            icon.setImageResource(R.drawable.south_west);
        } else if(dir > 225 + 22.5 && dir <= 3.15 - 22.5 ) {    //West
            icon.setImageResource(R.drawable.west);
        } else {                                                //North West
            icon.setImageResource(R.drawable.north_west);
        }
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
        //Documentation of this function can be found in http://www.marroneo.org/sonlas3am

        String newPreset = "";

        if(mapType > 0) {
            String presetTemp = readFile("preSet.txt");
            String[] parts = presetTemp.split(":");
            String part2 = parts[1];

            newPreset = mapType + ":" + part2 + ":";
        }

        if(mapLocation > 0) {
            String presetTemp = readFile("preSet.txt");
            String[] parts = presetTemp.split(":");
            String part1 = parts[0];

            newPreset = part1 + ":" + mapLocation + ":";

        }

        File preSet_file = getFileStreamPath("preSet.txt");
        preSet_file.delete();
        createFile("preSet.txt", newPreset);
    }

    public void updatePreSetsFilters(int coos, int wind, int forecast1, int forecast2) {
        String newPreset = coos + ":" + wind + ":" + forecast1 + ":" + forecast2 + ":";
        File preSet_file = getFileStreamPath("preSetFilters.txt");
        preSet_file.delete();
        createFile("preSetFilters.txt", newPreset);
    }

    private void doRefresh(){
        Intent i = new Intent(getApplicationContext(), splashActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("delay", "0");
        i.putExtras(bundle);
        startActivity(i);
    }
}
