package com.testgdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class SimScreen implements Screen {
    private enum MODE {
        DEFAULT,
        RUN,
        ADD,
        DELETE,
        SET_SPEED,
        SET_MASS
    }
    private enum SCUFFED_BUTTON{
        ADD,
        DELETE,
        SPEED,
        MASS,
        DEFAULT
    }
    private MODE currentMode = MODE.DEFAULT;
    private SCUFFED_BUTTON buttonState = SCUFFED_BUTTON.DEFAULT;
    final PlanetSim game;
    Stage stage;
    OrthographicCamera camera;
    Texture planetImage;
    Texture addImage;
    Texture deleteImage;
    Texture speedImage;
    Texture massImage;
    Texture cancelImage;
    Button addButton;
    Button deleteButton;
    Button speedButton;
    Button massButton;
    Button cancelButton;
    Sound dropSound;
    Circle newPlanet;
    Array<Circle> planets;
    Array<int[]> parameters;
    long lastDropTime;
    int dropsGathered;

    public SimScreen(final PlanetSim game) {
        this.game = game;

        // load the images for the droplet and the bucket, 64x64 pixels each
        planetImage = new Texture(Gdx.files.internal("planet1.png"));
        addImage = new Texture(Gdx.files.internal("addImage1.jpg"));
        deleteImage = new Texture(Gdx.files.internal("trashcan1.jpeg"));
        speedImage = new Texture(Gdx.files.internal("running1.png"));
        massImage = new Texture(Gdx.files.internal("weight1.png"));
        cancelImage = new Texture(Gdx.files.internal("cancel1.png"));



        addButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(addImage)));
        addButton.setSize(24,24);
        addButton.setPosition(Gdx.graphics.getWidth()/2-60,0);
        addButton.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                buttonState = SCUFFED_BUTTON.ADD;
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                newPlanet = new Circle();
                currentMode = MODE.ADD;
                buttonState = SCUFFED_BUTTON.DEFAULT;
            }
        });

        deleteButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(deleteImage)));
        deleteButton.setSize(24,24);
        deleteButton.setPosition(Gdx.graphics.getWidth()/2-36,0);
        deleteButton.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                buttonState = SCUFFED_BUTTON.DELETE;
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                currentMode = MODE.DELETE;
                buttonState = SCUFFED_BUTTON.DEFAULT;
            }
        });

        speedButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(speedImage)));
        speedButton.setSize(24,24);
        speedButton.setPosition(Gdx.graphics.getWidth()/2-12,0);
        speedButton.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                buttonState = SCUFFED_BUTTON.SPEED;
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                currentMode = MODE.SET_SPEED;
                buttonState = SCUFFED_BUTTON.DEFAULT;
            }
        });

        massButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(massImage)));
        massButton.setSize(24,24);
        massButton.setPosition(Gdx.graphics.getWidth()/2+12,0);
        massButton.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                buttonState = SCUFFED_BUTTON.MASS;
                return true;
            }
            @Override
            public void touchUp (InputEvent event, float x, float y, int pointer, int button){
                currentMode = MODE.SET_MASS;
                buttonState = SCUFFED_BUTTON.DEFAULT;
            }
        });

        cancelButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(cancelImage)));
        cancelButton.setSize(24,24);
        cancelButton.setPosition(Gdx.graphics.getWidth()/2+36,0);
        cancelButton.addListener(new InputListener(){
            @Override
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                buttonState = SCUFFED_BUTTON.DEFAULT;
                currentMode = MODE.DEFAULT;
                return true;
            }
        });

        stage = new Stage(new ScreenViewport());
        stage.addActor(addButton);
        stage.addActor(deleteButton);
        stage.addActor(speedButton);
        stage.addActor(massButton);
        stage.addActor(cancelButton);
        Gdx.input.setInputProcessor(stage);


        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        planets = new Array<Circle>();
        parameters = new Array<int[]>();

    }

    private void addPlanet() {
        if (Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            newPlanet.radius = 32;
            newPlanet.x = touchPos.x-planetImage.getWidth()/2;
            newPlanet.y = touchPos.y-planetImage.getHeight()/2;

            planets.add(newPlanet);
            parameters.add(new int[5]);
            currentMode = MODE.DEFAULT;
        }
    }
    private void choosePlanet(){
        if(Gdx.input.isTouched()){
            Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            for(Circle planet : planets){
                if(planet.contains(touchPos.x,touchPos.y)){
                    switch(currentMode){
                        case DELETE:
                            deletePlanet(planet);
                            break;
                        case SET_SPEED:
                            break;
                        case SET_MASS:
                            break;
                    }

                }
            }
        }
    }
    private void deletePlanet(Circle planet){
        parameters.removeIndex(planets.indexOf(planet,true));
        planets.removeValue(planet,true);
        currentMode = MODE.DEFAULT;
    }


    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);

        camera.update();

        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        for(Circle planet : planets){
            game.batch.draw(planetImage,planet.x,planet.y);
        }
        game.batch.end();

        stage.act();
        stage.draw();

        switch(currentMode){
            case ADD:
                addPlanet();
                break;
            case DELETE:
                choosePlanet();
                break;
            case SET_SPEED:
                break;
            case DEFAULT:
                break;
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

}
