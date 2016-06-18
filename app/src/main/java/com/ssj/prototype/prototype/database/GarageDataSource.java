package com.ssj.prototype.prototype.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ssj.prototype.prototype.model.Vehicle;

import java.util.ArrayList;

/**
 * Created by shadbolt on 5/14/2016.
 */
public class GarageDataSource {

    private Context context;

    // Database fields
    private SQLiteDatabase database;
    private GarageDataOpenHelper dbHelper;
    private String[] allGarageColumns = {GarageDataOpenHelper.COLUMN_ID, GarageDataOpenHelper.COLUMN_YEAR, GarageDataOpenHelper.COLUMN_MAKE, GarageDataOpenHelper.COLUMN_MODEL, GarageDataOpenHelper.COLUMN_STYLE, GarageDataOpenHelper.COLUMN_ENGINE, GarageDataOpenHelper.COLUMN_TRANSMISSION, GarageDataOpenHelper.COLUMN_MILEAGE_TOTAL, GarageDataOpenHelper.COLUMN_MILEAGE_ANNUAL};
    private String[] allMaintenanceColumns = {GarageDataOpenHelper.COLUMN_VEHICLE_ID, GarageDataOpenHelper.COLUMN_ENGINE_CODE, GarageDataOpenHelper.COLUMN_TRANSMISSION_CODE, GarageDataOpenHelper.COLUMN_FREQUENCY, GarageDataOpenHelper.COLUMN_INTERVAL_MILEAGE, GarageDataOpenHelper.COLUMN_ACTION, GarageDataOpenHelper.COLUMN_ITEM, GarageDataOpenHelper.COLUMN_ITEM_DESCRIPTION};

    public GarageDataSource(Context context) {
        this.context = context;
        dbHelper = new GarageDataOpenHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void drop() throws SQLException {
        dbHelper.drop(database);
    }

    public Vehicle insertVehicle(String year, String make, String model, String style, String engine, String transmission, String mileageTotal, String mileageAnnual) {

        //Error catching for empty fields
        if (mileageTotal.length() == 0)
            mileageTotal = "0";
        if (mileageAnnual.length() == 0)
            mileageAnnual = "0";

        ContentValues values = new ContentValues();
        values.put(GarageDataOpenHelper.COLUMN_YEAR, year);
        values.put(GarageDataOpenHelper.COLUMN_MAKE, make);
        values.put(GarageDataOpenHelper.COLUMN_MODEL, model);
        values.put(GarageDataOpenHelper.COLUMN_STYLE, style);
        values.put(GarageDataOpenHelper.COLUMN_ENGINE, engine);
        values.put(GarageDataOpenHelper.COLUMN_TRANSMISSION, transmission);
        values.put(GarageDataOpenHelper.COLUMN_MILEAGE_TOTAL, Integer.parseInt(mileageTotal));
        values.put(GarageDataOpenHelper.COLUMN_MILEAGE_ANNUAL, Integer.parseInt(mileageAnnual));
        Log.d("INSERT", values.toString());
        long id = database.insert(GarageDataOpenHelper.TABLE_NAME_GARAGE, null, values);
        return new Vehicle(id, year, make, model, style, engine, transmission);
    }

    public void insertMaintenance(Vehicle vehicle, String engineCode, String transmissionCode, String mileage, String frequency, String action, String item, String itemDescription) {
        ContentValues values = new ContentValues();
        values.put(GarageDataOpenHelper.COLUMN_VEHICLE_ID, vehicle.getId());
        values.put(GarageDataOpenHelper.COLUMN_ENGINE_CODE, engineCode);
        values.put(GarageDataOpenHelper.COLUMN_TRANSMISSION_CODE, transmissionCode);
        values.put(GarageDataOpenHelper.COLUMN_INTERVAL_MILEAGE, Integer.parseInt(mileage));
        values.put(GarageDataOpenHelper.COLUMN_FREQUENCY, Integer.parseInt(frequency));
        values.put(GarageDataOpenHelper.COLUMN_ACTION, action);
        values.put(GarageDataOpenHelper.COLUMN_ITEM, item);
        values.put(GarageDataOpenHelper.COLUMN_ITEM_DESCRIPTION, itemDescription);
        database.insert(GarageDataOpenHelper.TABLE_NAME_MAINTENANCE, null, values);
    }

    public void deleteVehicle(long id) {
        database.delete(GarageDataOpenHelper.TABLE_NAME_GARAGE, GarageDataOpenHelper.COLUMN_ID
                + " = " + id, null);
    }

    public String getVehicle(long id) {
        String response = null;
        Cursor cursor = database.query(GarageDataOpenHelper.TABLE_NAME_GARAGE, allGarageColumns, GarageDataOpenHelper.COLUMN_ID + "=\'" + id + "\'", null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            response = cursor.getString(0) + " " + cursor.getString(1) + " " + cursor.getString(2) + " " + cursor.getString(3) + " " + cursor.getString(4) + " " + cursor.getString(5) + " " + cursor.getString(6);
            cursor.moveToNext();
        }
        cursor.close();
        return response;
    }

    public ArrayList<Vehicle> getAllVehicles() {
        ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();

        Cursor cursor = database.query(GarageDataOpenHelper.TABLE_NAME_GARAGE, allGarageColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Vehicle vehicle = new Vehicle(cursor);
            vehicles.add(vehicle);
            cursor.moveToNext();
        }
        cursor.close();
        return vehicles;
    }

    public String getMileage(long id) {
        String response = "";

        //query(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit)
        Cursor cursor = database.query(GarageDataOpenHelper.TABLE_NAME_GARAGE, new String[]{GarageDataOpenHelper.COLUMN_MILEAGE_TOTAL, GarageDataOpenHelper.COLUMN_MILEAGE_ANNUAL}, GarageDataOpenHelper.COLUMN_VEHICLE_ID + "=\'" + id + "\'", null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            response += "MILEAGE: " + cursor.getInt(0) + System.getProperty("line.separator");
            response += "ANNUAL: " + cursor.getInt(1) + System.getProperty("line.separator");
            cursor.moveToNext();
        }
        cursor.close();
        return response;
    }

    public String getMaintenance(long id) {
        String response = "";

        //Query engine and transmission type of vehicle ID

        //query(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit)
        Cursor cursor = database.query(GarageDataOpenHelper.TABLE_NAME_MAINTENANCE, allMaintenanceColumns, GarageDataOpenHelper.COLUMN_VEHICLE_ID + "=\'" + id + "\'", null, null, null, GarageDataOpenHelper.COLUMN_INTERVAL_MILEAGE + " ASC", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            response += "MILEAGE:\t\t\t\t\t\t" + cursor.getString(4) + System.getProperty("line.separator");
            response += "FREQUENCY:\t\t\t\t" + cursor.getString(3) + System.getProperty("line.separator");
            response += "ENGINE:\t\t\t\t\t\t\t\t" + cursor.getString(1) + System.getProperty("line.separator");
            response += "TRANSMISSION:\t" + cursor.getString(2) + System.getProperty("line.separator");
            response += cursor.getString(5) + ": " + cursor.getString(6) + System.getProperty("line.separator");
            response += cursor.getString(7) + System.getProperty("line.separator") + System.getProperty("line.separator");
            cursor.moveToNext();
        }
        cursor.close();
        return response;
    }

    /**
     * @return
     */
    public String[] getMaintenanceActivities() {
        ArrayList<String> responses = new ArrayList<>();
        ArrayList<Integer> sort = new ArrayList<>();

        ArrayList<Vehicle> vehicles = getAllVehicles();
        for (Vehicle vehicle : vehicles) {

            //Set the mileage threshold to search
            final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            int threshold = settings.getInt("threshold", 10000);
            int low = vehicle.getMileageTotal() - threshold;
            int high = vehicle.getMileageTotal() + threshold;

            Log.d("QUERY", GarageDataOpenHelper.COLUMN_INTERVAL_MILEAGE + " between " + low + " and " + high + " and " + GarageDataOpenHelper.COLUMN_VEHICLE_ID + "=\'" + vehicle.getId() + "\'" + " and " + GarageDataOpenHelper.COLUMN_ENGINE_CODE + "=\'" + vehicle.getEngine() + "\'" + " and (" + GarageDataOpenHelper.COLUMN_TRANSMISSION_CODE + "=\'" + vehicle.getTransmission() + "\' OR " + GarageDataOpenHelper.COLUMN_TRANSMISSION_CODE + "=\'ALL\'" + ")");
            Cursor cursor = database.query(GarageDataOpenHelper.TABLE_NAME_MAINTENANCE, allMaintenanceColumns, GarageDataOpenHelper.COLUMN_INTERVAL_MILEAGE + " between " + low + " and " + high + " and " + GarageDataOpenHelper.COLUMN_VEHICLE_ID + "=\'" + vehicle.getId() + "\'" + " and " + GarageDataOpenHelper.COLUMN_ENGINE_CODE + "=\'" + vehicle.getEngine() + "\'" + " and (" + GarageDataOpenHelper.COLUMN_TRANSMISSION_CODE + "=\'" + vehicle.getTransmission() + "\' OR " + GarageDataOpenHelper.COLUMN_TRANSMISSION_CODE + "=\'ALL\'" + ")", null, null, null, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                int intervalMileage = cursor.getInt(4);
                int dueIn = intervalMileage - vehicle.getMileageTotal();
                String action = cursor.getString(5);
                String item = cursor.getString(6);

                //Build the response string with , delimiter
                String response = action + ": " + item + ",,";
                response += vehicle.getYear() + " " + vehicle.getMake() + " " + vehicle.getModel() + ",,";
                if (dueIn < 0)
                    response += "OVERDUE:\t\t\t" + (dueIn * -1);
                else
                    response += "DUE:\t\t\t\t\t\t" + dueIn;
                cursor.moveToNext();

                //Sort based on dueIn metric
                boolean added = false;
                for (int i = 0; i < responses.size(); i++) {
                    if (sort.get(i) < dueIn) continue;
                    sort.add(i, dueIn);
                    responses.add(i, response);
                    added = true;
                    break;
                }
                if (!added) {
                    sort.add(dueIn);
                    responses.add(response);
                }
            }
            cursor.close();
        }
        return responses.toArray(new String[responses.size()]);
    }
}