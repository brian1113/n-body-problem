package com.testgdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.ArrowShapeBuilder;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.Arrays;

public class SimScreen implements Screen{
    private enum MODE {
        DEFAULT,
        RUN,
        ADD,
        SELECT,
        DELETE,
        SET_SPEED,
        SET_MASS
    }
    final double G = 65;
    private MODE currentMode = MODE.DEFAULT;
    private boolean choosing = false;
    private boolean repeatChecker = true;
    final PlanetSim game;
    Stage stage;
    Mesh arrow;
    OrthographicCamera camera;
    Texture planetImage, addImage, deleteImage, speedImage, massImage, cancelImage, playImage;
    Button addButton, deleteButton, speedButton, massButton, cancelButton, playButton;
    public String textInput;
    float massInput;
    Planet newPlanet, selectedPlanet;
    Array<Planet> planets;
    Array<Planet> savedPos;
    boolean running = false;


    public SimScreen(final PlanetSim game) {
        this.game = game;

        //initialize planet texture and array
        planetImage = new Texture(Gdx.files.internal("planet1.png"));
        planets = new Array<>();

        //initialize stage and actors
        stage = new Stage(new ScreenViewport());
        initializeButtons();
        Gdx.input.setInputProcessor(stage);

        //set camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
    }


    @Override
    public void render(float delta) {

        if(Gdx.input.isTouched()){
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);

            System.out.println("x = " + touchPos.x);
            System.out.println("touchPos.y = " + touchPos.y);
        }

        //render all the planets
        ScreenUtils.clear(0f, 0f, 0.2f, 1);
//        System.out.println("fps = " + Gdx.graphics.getFramesPerSecond());
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        for(Planet planet : planets){
            game.batch.draw(planetImage,planet.getCircle().x-planet.getCircle().radius,planet.getCircle().y-planet.getCircle().radius);
        }
        game.batch.end();

        //draw all actors
        stage.act();
        stage.draw();

        if (running) {
            run();
        }else{
            //render all velocity vectors
            for(Planet planet : planets){
                if(planet.getArrow() != null)
                    planet.getArrow().render(game.batch.getShader(), GL20.GL_TRIANGLES);
            }

            //button functionality
            loopButtons();
        }

    }

    private void run(){
        //
    }

    private void choosePlanet(){
        if(repeatChecker){
            repeatChecker = false;
            stage.addListener(new ClickListener(){
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button){
                    repeatChecker = true;
                    for(Planet planet : planets){
                        if(planet.getCircle().contains(x,y)){
                            selectedPlanet = planet;
                            choosing = false;
                            if(currentMode == MODE.SET_MASS){
                                stage.addActor(selectedPlanet.getTextField());
                            }
                        }
                    }

                }
            });
        }
    }

    private void addPlanet() {
        if (Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);

            Circle circle = new Circle(touchPos.x, touchPos.y, 16);
            newPlanet = new Planet(circle, 10, new Vector2(0,0), new Vector2(0,0));

            planets.add(newPlanet);
            currentMode = MODE.DEFAULT;
        }
    }

    private void deletePlanet(){
        planets.removeValue(selectedPlanet, true);
        selectedPlanet = null;
        currentMode = MODE.DEFAULT;
    }

    private void setSpeed(){
        Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(touchPos);

        selectedPlanet.setArrow(null);

        MeshBuilder meshbuilder = new MeshBuilder();
        meshbuilder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.ColorUnpacked, GL20.GL_TRIANGLES);
        ArrowShapeBuilder.build(
                meshbuilder,
                selectedPlanet.getCircle().x, selectedPlanet.getCircle().y, 0,
                touchPos.x, touchPos.y, 0,
                0.1f,
                0.2f,
                10
        );
        arrow = meshbuilder.end();
        arrow.render(game.batch.getShader(), GL20.GL_TRIANGLES);
        if(Gdx.input.isTouched()){
            selectedPlanet.setArrow(arrow);
            float x = (touchPos.x-selectedPlanet.getCircle().x)/25;
            float y = (touchPos.y-selectedPlanet.getCircle().y)/25;
            selectedPlanet.setVelocity(new Vector2(
                    x,y));
            System.out.println("x = " + x);
            System.out.println("y = " + y);
            currentMode = MODE.DEFAULT;
        }
    }

    private void setMass(){
        if(Gdx.input.isKeyPressed(Input.Keys.ENTER)){
            try {
                textInput = selectedPlanet.getTextField().getText();
                massInput = Float.parseFloat(textInput);
                selectedPlanet.setMass(massInput);
                selectedPlanet.getTextField().remove();
                selectedPlanet.getTextField().setText(""+selectedPlanet.getMass());
                currentMode = MODE.DEFAULT;
            } catch(Exception e) {
                //do nothing
            }
        }
    }

    private void loopButtons(){
        if(choosing){
            choosePlanet();
        }else{
            switch(currentMode) {
                case ADD:
                    addPlanet();
                    break;
                case DELETE:
                    deletePlanet();
                    break;
                case SET_SPEED:
                    setSpeed();
                    break;
                case SET_MASS:
                    setMass();
                    break;
                case DEFAULT:
                    break;
            }
        }
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
    }

    public void initializeButtons(){
        addImage = new Texture(Gdx.files.internal("addImage1.jpg"));
        deleteImage = new Texture(Gdx.files.internal("trashcan1.jpeg"));
        speedImage = new Texture(Gdx.files.internal("running1.png"));
        massImage = new Texture(Gdx.files.internal("weight2.png"));
        cancelImage = new Texture(Gdx.files.internal("cancel2.png"));
        playImage = new Texture(Gdx.files.internal("play.png"));

        addButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(addImage)));
        addButton.setSize(24,24);
        addButton.setPosition(Gdx.graphics.getWidth()/2-60,0);
        addButton.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                currentMode = MODE.ADD;
            }
        });

        deleteButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(deleteImage)));
        deleteButton.setSize(24,24);
        deleteButton.setPosition(Gdx.graphics.getWidth()/2-36,0);
        deleteButton.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                currentMode = MODE.DELETE;
                choosing = true;
            }
        });

        speedButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(speedImage)));
        speedButton.setSize(24,24);
        speedButton.setPosition(Gdx.graphics.getWidth()/2-12,0);
        speedButton.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                currentMode = MODE.SET_SPEED;
                choosing = true;
            }
        });

        massButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(massImage)));
        massButton.setSize(24,24);
        massButton.setPosition(Gdx.graphics.getWidth()/2+12,0);
        massButton.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                currentMode = MODE.SET_MASS;
                choosing = true;
            }
        });

        cancelButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(cancelImage)));
        cancelButton.setSize(24,24);
        cancelButton.setPosition(Gdx.graphics.getWidth()/2+36,0);
        cancelButton.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                currentMode = MODE.DEFAULT;
                if(selectedPlanet != null){
                    selectedPlanet.getTextField().remove();
                    selectedPlanet.getTextField().setText(""+selectedPlanet.getMass());
                }
                choosing = false;
                return true;
            }
        });

        playButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(playImage)));
        playButton.setSize(32,32);
        playButton.setPosition(768,0);
        playButton.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                currentMode = MODE.DEFAULT;
                if(selectedPlanet != null){
                    selectedPlanet.getTextField().remove();
                    selectedPlanet.getTextField().setText(""+selectedPlanet.getMass());
                }
                choosing = false;
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                addButton.remove();
                deleteButton.remove();
                speedButton.remove();
                massButton.remove();
                cancelButton.remove();
                playButton.remove();

                savedPos = new Array<>(planets);

                Thread calcThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("planets.size = " + planets.size);
                        for (Planet planet : planets) {
                            float fgx = 0;
                            float fgy = 0;
                            for (int i = 0; i < planets.size; i++) {
                                if (planet == planets.get(i)) continue;
                                double distance = Math.sqrt(Math.pow(planets.get(i).getCircle().x - planet.getCircle().x, 2)
                                        + Math.pow(planets.get(i).getCircle().y - planet.getCircle().y, 2));
                                double fg = (G * planet.getMass() * planets.get(i).getMass()) / Math.pow(distance, 2);
                                double ftheta = Math.atan2(planets.get(i).getCircle().y - planet.getCircle().y,
                                        planets.get(i).getCircle().x - planet.getCircle().x);
                                fgx += Math.cos(ftheta) * fg;
                                fgy += Math.sin(ftheta) * fg;
                                System.out.println("distance = " + distance);
                                System.out.println("fg = " + fg);
                                System.out.println("ftheta = " + ftheta);

                            }
                            planet.updatePos(fgx, fgy);
                        }
                    }
                });
                calcThread.start();
                running = true;
            }
        });

        stage.addActor(addButton);
        stage.addActor(deleteButton);
        stage.addActor(speedButton);
        stage.addActor(massButton);
        stage.addActor(cancelButton);
        stage.addActor(playButton);
    }

}
