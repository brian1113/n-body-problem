package com.testgdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

public class Planet{
    private Circle circle;
    private float mass;
    private Vector2 velocity;
    private Vector2 acceleration;
    private Mesh arrow;
    private TextField textField;
    private long time;

    private float xPos = 0;
    private float yPos = 0;

    public Planet(Circle circle, float mass, Vector2 velocity, Vector2 acceleration){
        this.circle = circle;
        this.mass = mass;
        this.velocity = velocity;
        this.acceleration = acceleration;
        textField = new TextField("", new Skin(Gdx.files.internal("uiskin.json")));
        textField.setMessageText(""+mass);
        textField.setPosition(circle.x,circle.y);
    }

    public void setArrow(Mesh arrow){
        this.arrow = arrow;
    }

    public void setVelocity(Vector2 velocity){
        this.velocity = velocity;
    }

    public void setMass(float mass){
        this.mass = mass;
    }

    public void updateAccel(double fgx, double fgy){
        acceleration.x += (fgx/mass);
        acceleration.y += (fgy/mass);
    }

    public void updatePos(float fgx, float fgy){



        acceleration.x = (fgx/mass);
        acceleration.y = (fgy/mass);
        velocity.x += acceleration.x;
        velocity.y += acceleration.y;
        circle.x += velocity.x;
        circle.x += velocity.y;



        System.out.println("acceleration x = " + acceleration.x);
        System.out.println("acceleration.y = " + acceleration.y);
    }

    public void resetAccel(){
        acceleration.x = 0;
        acceleration.y = 0;
    }

    public void resetTime(){
        time = System.nanoTime();
    }

    public TextField getTextField(){return textField;}
    public Circle getCircle(){return circle;}
    public float getMass(){return mass;}
    public Vector2 getVelocity(){return velocity;}
    public Vector2 getAcceleration(){return acceleration;}
    public Mesh getArrow(){return arrow;}


}
