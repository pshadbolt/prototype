package com.ssj.prototype.prototype.model.Edmunds;

import com.ssj.prototype.prototype.utils.JSONHelper;

import org.json.JSONObject;

/**
 * Created by PAUL on 6/11/2016.
 */
public class Engine implements EdmundsAttribute {

    private String id;
    private String name;
    private String equipmentType;
    private String availability;
    private int compressionRatio;
    private int cylinder;
    private int size;
    private int displacement;
    private String configuration;
    private String fuelType;
    private int horsepower;
    private int torque;
    private int totalValves;
    private String manufacturerEngineCode;
    private String type;
    private String code;
    private String compressorType;

    public String getCode() {
        return code;
    }

    public int getHorsepower() {
        return horsepower;
    }

    public int getSize() {
        return size;
    }

    public Engine(String code, int horsepower, int size) {
        this.code = code;
        this.size = size;
        this.horsepower = horsepower;
    }

    public Engine(JSONObject jsonObject) {
        this.id = JSONHelper.getString(jsonObject, "id");
        this.name = JSONHelper.getString(jsonObject, "name");
        this.equipmentType = JSONHelper.getString(jsonObject, "equipmentType");
        this.availability = JSONHelper.getString(jsonObject, "availability");
        this.compressionRatio = JSONHelper.getInt(jsonObject, "compressionRatio");
        this.cylinder = JSONHelper.getInt(jsonObject, "cylinder");
        this.size = JSONHelper.getInt(jsonObject, "size");
        this.displacement = JSONHelper.getInt(jsonObject, "displacement");
        this.configuration = JSONHelper.getString(jsonObject, "configuration");
        this.fuelType = JSONHelper.getString(jsonObject, "fuelType");
        this.horsepower = JSONHelper.getInt(jsonObject, "horsepower");
        this.torque = JSONHelper.getInt(jsonObject, "torque");
        this.totalValves = JSONHelper.getInt(jsonObject, "totalValves");
        this.manufacturerEngineCode = JSONHelper.getString(jsonObject, "manufacturerEngineCode");
        this.type = JSONHelper.getString(jsonObject, "type");
        this.code = JSONHelper.getString(jsonObject, "code");
        this.compressorType = JSONHelper.getString(jsonObject, "compressorType");
    }

    @Override
    public String displayValue() {
        return String.valueOf(size);
    }

    @Override
    public String searchValue() {
        return this.code;
    }

    public String saveValue() {
        return this.code;
    }
}