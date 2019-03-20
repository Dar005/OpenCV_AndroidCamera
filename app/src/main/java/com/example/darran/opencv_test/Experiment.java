package com.example.darran.opencv_test;

import android.media.Image;

/**
 * Created by Darran on 18/02/2019.
 *
 * This class will create and experiment the image will
 * be stored at the will all the images taken after it
 * so it can be used as a reference if OCR does not work.
 *
 * Maybe it should just use the Experiment name or id to create a table
 * i.e. Tables will be name Rose Black Spot 12/3/19....
 *
 * I think there will have to be a new table for each experiment created
 * meaning there will be many tables with the same name the way to differentiate
 * between them will be by the datetime they where created.
 *
 * When a experiment is created it the image will need to be placed in an array which
 * will be further populated by proceeding pictures of plants. this array will hold other
 * information as well as images such as level of disease present, location, weather, time/date
 * exp name, userId, and notes from the user....
 *
 */

public class Experiment {


    // Variables for first image in experiment, this image is user to
    // create a table in the database and is the first image stored in
    // the database..

    // experiment name and id need to perist accross all objets i.e. they all need to share
    // same name and id..
    private Image experimentImg;

    private String experimentName;
    private int experimentId;
    private String userName;
    private static int counter = 0;

    // Variables for analysis..
    private double analysis;
    private String location;
    private String weather;
    private String notes;


    // constructor to create experiment
    // i.e. first image that will pass through OCR
    public Experiment(Image experimentImg, String experimentName, int experimentId, String userName){
        this.experimentImg = experimentImg;
        this.experimentName = experimentName;
        this.experimentId = experimentId;
        this.experimentId = counter++;
        this.userName = userName;
    }


    // Analysis Constructor
    // i.e all images will pass through color analysis
    public Experiment(Image experimentImg, String experimentName, int experimentId, String userName,
                      double analysis, String location, String weather, String notes){

        this.experimentImg = experimentImg;
        this.experimentName = experimentName;
        this.experimentId = experimentId;
        this.userName = userName;
        this.analysis = analysis;
        this.location = location;
        this.weather = weather;
        this.notes = notes;
    }


    public String getExperimentName(){
        return this.experimentName;
    }

}
