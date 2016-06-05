package com.ssj.prototype.prototype.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.ssj.prototype.prototype.R;
import com.ssj.prototype.prototype.database.GarageDataSource;
import com.ssj.prototype.prototype.model.EdmundsCodes;
import com.ssj.prototype.prototype.model.Vehicle;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class AddVehicleActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private int state = 1;

    protected Spinner make;
    protected Spinner model;
    protected Spinner year;
    protected Spinner style;
    protected Spinner engine;
    protected Spinner transmission;

    private HashMap<Spinner, HashMap<String, String>> spinnerMap;

    protected EditText mileageTotal;
    protected EditText mileageAnnual;

    protected Button cancel;
    protected Button confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);

        // Prepare Spinners
        make = (Spinner) findViewById(R.id.spinner1);
        model = (Spinner) findViewById(R.id.spinner2);
        year = (Spinner) findViewById(R.id.spinner3);
        style = (Spinner) findViewById(R.id.spinner4);
        engine = (Spinner) findViewById(R.id.spinner5);
        transmission = (Spinner) findViewById(R.id.spinner6);

        spinnerMap = new HashMap<Spinner, HashMap<String, String>>();
        spinnerMap.put(make, new HashMap<String, String>());
        spinnerMap.put(model, new HashMap<String, String>());
        spinnerMap.put(year, new HashMap<String, String>());
        spinnerMap.put(style, new HashMap<String, String>());
        spinnerMap.put(engine, new HashMap<String, String>());
        spinnerMap.put(transmission, new HashMap<String, String>());

        mileageTotal = (EditText) findViewById(R.id.editText1);
        mileageAnnual = (EditText) findViewById(R.id.editText2);

        cancel = (Button) findViewById(R.id.cancel);
        confirm = (Button) findViewById(R.id.confirm);

        //Initiate populating the spinner values
        if (this.getIntent().hasExtra("VIN") && this.getIntent().getExtras().getString("VIN").length() > 11) {
            toggleSpinners(false);
            String VIN = this.getIntent().getExtras().getString("VIN");
            new VINQuery().execute(VIN.subSequence(0, 8).toString().toUpperCase() + VIN.subSequence(9, 11).toString().toUpperCase());
        } else {
            enableListeners();
            toggleSpinners(true);
            query(EdmundsCodes.MAKES_QUERY, EdmundsCodes.MAKES_ARRAY, EdmundsCodes.MAKES_NAME, EdmundsCodes.MAKES_ID, make);
        }
    }

    private void toggleSpinners(boolean enabled) {
        make.setEnabled(enabled);
        model.setEnabled(enabled);
        year.setEnabled(enabled);
        style.setEnabled(enabled);
        engine.setEnabled(enabled);
        transmission.setEnabled(enabled);
    }

    private void enableListeners() {
        make.setOnItemSelectedListener(this);
        model.setOnItemSelectedListener(this);
        year.setOnItemSelectedListener(this);
        style.setOnItemSelectedListener(this);
        engine.setOnItemSelectedListener(this);
        transmission.setOnItemSelectedListener(this);
    }

    // Spinner Selector
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

        findViewById(R.id.confirm).setEnabled(false);

        if (parent.getId() == make.getId()) {
            model.setVisibility(View.GONE);
            year.setVisibility(View.GONE);
            style.setVisibility(View.GONE);
            engine.setVisibility(View.GONE);
            transmission.setVisibility(View.GONE);
            query(lookup(make) + "/" + EdmundsCodes.MODELS_QUERY, EdmundsCodes.MODELS_ARRAY, EdmundsCodes.MODELS_NAME, EdmundsCodes.MODELS_ID, model);
        }
        if (parent.getId() == model.getId()) {
            year.setVisibility(View.GONE);
            style.setVisibility(View.GONE);
            engine.setVisibility(View.GONE);
            transmission.setVisibility(View.GONE);
            query(lookup(make) + "/" + lookup(model) + "/" + EdmundsCodes.YEARS_QUERY, EdmundsCodes.YEARS_ARRAY, EdmundsCodes.YEARS_NAME, EdmundsCodes.YEARS_ID, year);
        }
        if (parent.getId() == year.getId()) {
            style.setVisibility(View.GONE);
            engine.setVisibility(View.GONE);
            transmission.setVisibility(View.GONE);
            query(lookup(make) + "/" + lookup(model) + "/" + year.getSelectedItem() + "/" + EdmundsCodes.STYLES_QUERY, EdmundsCodes.STYLES_ARRAY, EdmundsCodes.STYLES_NAME, EdmundsCodes.STYLES_ID, style);
        }
        if (parent.getId() == style.getId()) {
            engine.setVisibility(View.GONE);
            transmission.setVisibility(View.GONE);
            query("styles/" + lookup(style) + "/" + EdmundsCodes.ENGINES_QUERY, EdmundsCodes.ENGINES_ARRAY, EdmundsCodes.ENGINES_NAME, EdmundsCodes.ENGINES_ID, engine);
            query("styles/" + lookup(style) + "/" + EdmundsCodes.TRANSMISSIONS_QUERY, EdmundsCodes.TRANSMISSIONS_ARRAY, EdmundsCodes.TRANSMISSIONS_NAME, EdmundsCodes.TRANSMISSIONS_ID, transmission);
        }
        if (parent.getId() == engine.getId()) {
            findViewById(R.id.confirm).setEnabled(true);
        }
        if (parent.getId() == transmission.getId()) {
            findViewById(R.id.confirm).setEnabled(true);
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    //Query information
    private String endpointMaintenance = "https://api.edmunds.com/v1/api/maintenance/";
    private String endpointVehicle = "https://api.edmunds.com/api/vehicle/v2/";
    private String format = "fmt=json";
    private String api_key = "&api_key=m6vz5qajjyxbctbehqtnguz2";

    /**
     * Perform the call to the REST API and update spinner with retrieved values
     */
    private class VINQuery extends AsyncTask<String, Void, String[]> {

        @Override
        protected void onPreExecute() {
            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        protected String[] doInBackground(String... params) {
            try {
                URL url = new URL(endpointVehicle + "squishvins/" + params[0] + "/?" + format + api_key);
                Log.d("REST", url.toString());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    //Send request to REST API
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    Log.d("INFO", stringBuilder.toString());

                    //Parse JSON response
                    String[] responses = new String[6];
                    JSONObject jsonObject = new JSONObject(stringBuilder.toString());

                    if (jsonObject.getJSONObject("make").has(EdmundsCodes.MAKES_NAME)) {
                        responses[0] = jsonObject.getJSONObject("make").getString(EdmundsCodes.MAKES_NAME);
                        spinnerMap.get(make).put(responses[0], jsonObject.getJSONObject("make").getString(EdmundsCodes.MAKES_ID));
                    }
                    if (jsonObject.getJSONObject("model").has(EdmundsCodes.MODELS_NAME)) {
                        responses[1] = jsonObject.getJSONObject("model").getString(EdmundsCodes.MODELS_NAME);
                        spinnerMap.get(model).put(responses[1], jsonObject.getJSONObject("model").getString(EdmundsCodes.MODELS_ID));
                    }
                    if (jsonObject.getJSONArray("years").getJSONObject(0).has(EdmundsCodes.YEARS_NAME)) {
                        responses[2] = jsonObject.getJSONArray("years").getJSONObject(0).getString(EdmundsCodes.YEARS_NAME);
                        spinnerMap.get(year).put(responses[2], jsonObject.getJSONArray("years").getJSONObject(0).getString(EdmundsCodes.YEARS_ID));
                    }
                    if (jsonObject.getJSONArray("years").getJSONObject(0).getJSONArray("styles").getJSONObject(0).has(EdmundsCodes.STYLES_NAME) && jsonObject.getJSONArray("years").getJSONObject(0).getJSONArray("styles").length() == 1) {
                        responses[3] = jsonObject.getJSONArray("years").getJSONObject(0).getJSONArray("styles").getJSONObject(0).getString(EdmundsCodes.STYLES_NAME);
                        spinnerMap.get(style).put(responses[3], jsonObject.getJSONArray("years").getJSONObject(0).getJSONArray("styles").getJSONObject(0).getString(EdmundsCodes.STYLES_ID));
                    }
                    if (jsonObject.getJSONObject("engine").has(EdmundsCodes.ENGINES_NAME)) {
                        responses[4] = jsonObject.getJSONObject("engine").getString(EdmundsCodes.ENGINES_NAME);
                        spinnerMap.get(engine).put(responses[4], jsonObject.getJSONObject("engine").getString(EdmundsCodes.ENGINES_ID));
                    }
                    if (jsonObject.getJSONObject("transmission").has(EdmundsCodes.TRANSMISSIONS_NAME)) {
                        responses[5] = jsonObject.getJSONObject("transmission").getString(EdmundsCodes.TRANSMISSIONS_NAME);
                        spinnerMap.get(transmission).put(responses[5], jsonObject.getJSONObject("transmission").getString(EdmundsCodes.TRANSMISSIONS_ID));
                    }
                    return responses;
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        @Override
        /**
         * Set the spinner values to the strings obtained from the REST API call
         */
        protected void onPostExecute(String[] responses) {
            super.onPostExecute(responses);

            if (responses == null) {
                Toast.makeText(AddVehicleActivity.this, "VIN Not Found.", Toast.LENGTH_LONG).show();
                enableListeners();
                toggleSpinners(true);
                query(EdmundsCodes.MAKES_QUERY, EdmundsCodes.MAKES_ARRAY, EdmundsCodes.MAKES_NAME, EdmundsCodes.MAKES_ID, make);
            } else {
                make.setAdapter(new ArrayAdapter<String>(AddVehicleActivity.this, android.R.layout.simple_spinner_dropdown_item, new String[]{responses[0]}));
                make.setVisibility(View.VISIBLE);

                model.setAdapter(new ArrayAdapter<String>(AddVehicleActivity.this, android.R.layout.simple_spinner_dropdown_item, new String[]{responses[1]}));
                model.setVisibility(View.VISIBLE);

                year.setAdapter(new ArrayAdapter<String>(AddVehicleActivity.this, android.R.layout.simple_spinner_dropdown_item, new String[]{responses[2]}));
                year.setVisibility(View.VISIBLE);

                if (responses[3] == null) {
                    query(lookup(make) + "/" + lookup(model) + "/" + year.getSelectedItem() + "/" + EdmundsCodes.STYLES_QUERY, EdmundsCodes.STYLES_ARRAY, EdmundsCodes.STYLES_NAME, EdmundsCodes.STYLES_ID, style);
                    style.setEnabled(true);
                } else {
                    style.setAdapter(new ArrayAdapter<String>(AddVehicleActivity.this, android.R.layout.simple_spinner_dropdown_item, new String[]{responses[3]}));
                    style.setVisibility(View.VISIBLE);
                }

                if (responses[4] == null) {
                    query("styles/" + lookup(style) + "/" + EdmundsCodes.ENGINES_QUERY, EdmundsCodes.ENGINES_ARRAY, EdmundsCodes.ENGINES_NAME, EdmundsCodes.ENGINES_ID, engine);
                } else {
                    engine.setAdapter(new ArrayAdapter<String>(AddVehicleActivity.this, android.R.layout.simple_spinner_dropdown_item, new String[]{responses[4]}));
                    engine.setVisibility(View.VISIBLE);
                }

                if (responses[5] == null) {
                    query("styles/" + lookup(style) + "/" + EdmundsCodes.TRANSMISSIONS_QUERY, EdmundsCodes.TRANSMISSIONS_ARRAY, EdmundsCodes.TRANSMISSIONS_NAME, EdmundsCodes.TRANSMISSIONS_ID, transmission);
                } else {
                    transmission.setAdapter(new ArrayAdapter<String>(AddVehicleActivity.this, android.R.layout.simple_spinner_dropdown_item, new String[]{responses[5]}));
                    transmission.setVisibility(View.VISIBLE);
                }

                confirm.setEnabled(true);
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            }
        }
    }

    /**
     * Perform the call to the REST API and update spinner with retrieved values
     */
    private class SpinnerQuery extends AsyncTask<String, Void, ArrayList<String>> {

        private Spinner spinner;

        public SpinnerQuery(Spinner spinner) {
            this.spinner = spinner;
        }

        @Override
        protected void onPreExecute() {
            spinner.setVisibility(View.GONE);
            spinnerMap.put(spinner, new HashMap<String, String>());
            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        /**
         * Execute the rest api call in a background thread
         *
         * @param params [0] - the query to be executed
         *               [1] - the expected json array value
         *               [2] - the attribute to parse for the display name
         *               [3] - the attribute to parse for the search name
         * @return Arraylist of string objects
         */
        protected ArrayList<String> doInBackground(String... params) {
            try {
                URL url = new URL(endpointVehicle + params[0] + "?" + format + api_key);
                Log.d("REST", url.toString());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    //Send request to REST API
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    Log.d("INFO", stringBuilder.toString());

                    //Parse JSON response
                    ArrayList<String> responses = new ArrayList<String>();
                    JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                    JSONArray responsesArray = jsonObject.getJSONArray(params[1]);
                    for (int i = 0; i < responsesArray.length(); i++) {
                        JSONObject jsonObject1 = responsesArray.getJSONObject(i);
                        responses.add(jsonObject1.getString(params[2]));
                        //Store the mapping of niceName to name for lookup on next search
                        spinnerMap.get(spinner).put(jsonObject1.getString(params[2]), jsonObject1.getString(params[3]));
                    }
                    return responses;
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        @Override
        /**
         * Set the spinner values to the strings obtained from the REST API call
         */
        protected void onPostExecute(ArrayList<String> responses) {
            super.onPostExecute(responses);
            if (responses == null)
                responses = new ArrayList<String>();
            spinner.setAdapter(new ArrayAdapter<String>(AddVehicleActivity.this, android.R.layout.simple_spinner_dropdown_item, responses));
            if (responses.size() > 0)
                spinner.setVisibility(View.VISIBLE);
            if (responses.size() > 1)
                spinner.setEnabled(true);
            else
                spinner.setEnabled(false);
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        }
    }

    /**
     *
     */
    private class VehicleMaintenanceQuery extends AsyncTask<String, Void, ArrayList<String[]>> {

        GarageDataSource garageDataSource;
        Vehicle vehicle;

        public VehicleMaintenanceQuery(GarageDataSource garageDataSource, Vehicle vehicle) {
            this.garageDataSource = garageDataSource;
            this.vehicle = vehicle;
        }

        @Override
        protected void onPreExecute() {
            findViewById(R.id.cancel).setEnabled(false);
            findViewById(R.id.confirm).setEnabled(false);
            findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        /**
         *
         */
        protected ArrayList<String[]> doInBackground(String... params) {
            try {
                URL url = new URL(endpointVehicle + params[0] + "?" + format + api_key);
                Log.d("REST", url.toString());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    //Send request to REST API
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    Log.d("INFO", stringBuilder.toString());

                    //Query style info to get yearid value to query maintenance info
                    String yearid = "";
                    JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                    JSONArray responsesArray = jsonObject.getJSONArray("styles");
                    for (int i = 0; i < responsesArray.length(); i++) {
                        JSONObject jsonObject1 = responsesArray.getJSONObject(i);
                        if (jsonObject1.getString("name").equals(params[1])) {
                            yearid = jsonObject1.getJSONObject("year").getString("id");
                        }
                    }
                    url = new URL(endpointMaintenance + "actionrepository/findbymodelyearid?modelyearid=" + yearid + "&" + format + api_key);
                    Log.d("REST", url.toString());
                    urlConnection = (HttpURLConnection) url.openConnection();
                    //Send request to REST API
                    bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    stringBuilder = new StringBuilder();
                    line = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    Log.d("INFO", stringBuilder.toString());

                    ArrayList<String[]> response = new ArrayList<String[]>();
                    jsonObject = new JSONObject(stringBuilder.toString());
                    responsesArray = jsonObject.getJSONArray("actionHolder");
                    for (int i = 0; i < responsesArray.length(); i++) {
                        JSONObject jsonObject1 = responsesArray.getJSONObject(i);
                        String engineCode = "";
                        String transmissionCode = "";
                        String intervalMileage = "0";
                        String frequency = "0";
                        String action = "";
                        String item = "";
                        String itemDescription = "";
                        if (jsonObject1.has("engineCode"))
                            engineCode = jsonObject1.getString("engineCode");
                        if (jsonObject1.has("transmissionCode"))
                            transmissionCode = jsonObject1.getString("transmissionCode");
                        if (jsonObject1.has("intervalMileage"))
                            intervalMileage = jsonObject1.getString("intervalMileage");
                        if (jsonObject1.has("frequency"))
                            frequency = jsonObject1.getString("frequency");
                        if (jsonObject1.has("action"))
                            action = jsonObject1.getString("action");
                        if (jsonObject1.has("item"))
                            item = jsonObject1.getString("item");
                        if (jsonObject1.has("itemDescription"))
                            itemDescription = jsonObject1.getString("itemDescription");
                        response.add(new String[]{engineCode, transmissionCode, intervalMileage, frequency, action, item, itemDescription});
                    }
                    return response;
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        @Override
        /**
         */
        protected void onPostExecute(ArrayList<String[]> response) {
            super.onPostExecute(response);
            for (int i = 0; i < response.size(); i++) {
                String[] entry = response.get(i);
                garageDataSource.insertMaintenance(vehicle, entry[0], entry[1], entry[2], entry[3], entry[4], entry[5], entry[6]);
            }
            garageDataSource.close();
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            Toast.makeText(AddVehicleActivity.this, "Vehicle Added To Garage", Toast.LENGTH_LONG).show();
            //TODO Should finish be used?
            finish();
        }
    }

    /**
     * Return the niceName value used for searching REST API from display name value for a given spinner
     */
    public String lookup(Spinner spinner) {
        return spinnerMap.get(spinner).get(spinner.getSelectedItem());
    }

    /**
     * Execute the query to populate a spinner value
     */
    public void query(String query, String array, String name, String niceName, Spinner spinner) {
        new SpinnerQuery(spinner).execute(new String[]{query, array, name, niceName});
    }

    /**
     * Cancel Button
     */
    public void cancel(View view) {

        if (state == 1) {
            //TODO Should finish be used?
            finish();
        } else if (state == 2) {
            state = 1;
            findViewById(R.id.layout_spinners).setVisibility(View.VISIBLE);
            findViewById(R.id.layout_mileage).setVisibility(View.GONE);
            cancel.setText("CANCEL");
            confirm.setText("NEXT");
        }
    }

    /**
     * Confirm Button
     */
    public void confirm(View view) {

        if (state == 1) {
            state = 2;
            findViewById(R.id.layout_spinners).setVisibility(View.GONE);
            findViewById(R.id.layout_mileage).setVisibility(View.VISIBLE);
            cancel.setText("BACK");
            confirm.setText("CONFIRM");
        } else if (state == 2) {
            // Create the database connections
            GarageDataSource garageDataSource = new GarageDataSource(this);
            garageDataSource.open();

            Vehicle vehicle = garageDataSource.insertVehicle((String) year.getSelectedItem(), (String) make.getSelectedItem(), (String) model.getSelectedItem(), (String) style.getSelectedItem(), (String) engine.getSelectedItem(), (String) transmission.getSelectedItem(), mileageTotal.getText().toString(), mileageAnnual.getText().toString());
            VehicleMaintenanceQuery vehicleMaintenanceQuery = new VehicleMaintenanceQuery(garageDataSource, vehicle);
            vehicleMaintenanceQuery.execute(new String[]{lookup(make) + "/" + lookup(model) + "/" + year.getSelectedItem() + "/" + EdmundsCodes.STYLES_QUERY, (String) style.getSelectedItem()});
        }
    }
}