package com.testgdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.shapebuilders.ArrowShapeBuilder;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
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

public class SimScreen implements Screen {
    private enum MODE {
        DEFAULT,
        RUN,
        ADD,
        SELECT,
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
    private boolean choosing = false;
    private boolean booleanCuzIDKAGoodWayToDoThis = true;
    final PlanetSim game;
    Stage stage;
    Mesh arrow;
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
    Array<Mesh> arrows;
    Circle selectedPlanet;
    Array<double[]> parameters;
    long lastDropTime;
    int dropsGathered;

    public SimScreen(final PlanetSim game) {
        this.game = game;

        // load the images for the droplet and the bucket, 64x64 pixels each
        planetImage = new Texture(Gdx.files.internal("planet1.png"));
        addImage = new Texture(Gdx.files.internal("addImage1.jpg"));
        deleteImage = new Texture(Gdx.files.internal("trashcan1.jpeg"));
        speedImage = new Texture(Gdx.files.internal("running1.png"));
        massImage = new Texture(Gdx.files.internal("weight2.png"));
        cancelImage = new Texture(Gdx.files.internal("cancel2.png"));



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
                choosing = true;
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
                choosing = true;
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
                choosing = false;
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
        parameters = new Array<double[]>();
        arrows = new Array<Mesh>();

    }

    private void addPlanet() {
        if (Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            newPlanet.radius = 16;
            newPlanet.x = touchPos.x;
            newPlanet.y = touchPos.y;

            planets.add(newPlanet);
            parameters.add(new double[]{10, 0, 0, 0, 0});
            currentMode = MODE.DEFAULT;
        }
    }
    private void choosePlanet(){
        if(booleanCuzIDKAGoodWayToDoThis){
            booleanCuzIDKAGoodWayToDoThis = false;
            System.out.println("yep");
            stage.addListener(new ClickListener(){
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button){
                    booleanCuzIDKAGoodWayToDoThis = true;
                    for(Circle planet : planets){
                        if(planet.contains(x,y)){
                            selectedPlanet = planet;
                            choosing = false;
                        }
                    }

                }
            });
        }

    }
    private void deletePlanet(){
        parameters.removeIndex(planets.indexOf(selectedPlanet, true));
        planets.removeValue(selectedPlanet, true);
        selectedPlanet = null;
        currentMode = MODE.DEFAULT;
    }

    private void setSpeed(){
        Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(touchPos);
        MeshBuilder meshbuilder = new MeshBuilder();
        meshbuilder.setColor(Color.GREEN);
        meshbuilder.setUVRange(0.5f, 0f, 0f, 0.5f);
        meshbuilder.begin(VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.ColorUnpacked, GL20.GL_TRIANGLES);
        ArrowShapeBuilder.build(
                meshbuilder,
                selectedPlanet.x, selectedPlanet.y, 0,
                touchPos.x, touchPos.y, 0,
                0.1f,
                0.2f,
                10
);
        arrow = meshbuilder.end();
        arrow.render(game.batch.getShader(), GL20.GL_TRIANGLES);
        if(Gdx.input.isTouched()){
            arrows.add(arrow);
            double[] planetParameters = parameters.get(planets.indexOf(selectedPlanet, true));
            planetParameters[1] = touchPos.x-selectedPlanet.x;
            planetParameters[2] = touchPos.y-selectedPlanet.y;
            currentMode = MODE.DEFAULT;
        }
    }


    @Override
    public void render(float delta) {
        ScreenUtils.clear(1f, 1f, 1f, 0);

        camera.update();

        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        for(Circle planet : planets){
            game.batch.draw(planetImage,planet.x-planet.radius,planet.y-planet.radius);
        }
        game.batch.end();

        for(Mesh arrow : arrows){
            arrow.render(game.batch.getShader(), GL20.GL_TRIANGLES);
        }
        stage.act();
        stage.draw();

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

}
