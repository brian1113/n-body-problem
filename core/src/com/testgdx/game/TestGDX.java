package com.testgdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

public class TestGDX extends ApplicationAdapter {
	private Texture planetImage;
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Array<Circle> planets;

	@Override
	public void create () {
		// load the images for the planet
		planetImage = new Texture(Gdx.files.internal("planet.png"));

		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);
		batch = new SpriteBatch();

		planets = new Array<Circle>();
		createPlanet();
	}

	private void createPlanet(){
		Circle planet = new Circle();
		planet.x = MathUtils.random(0, 800-64);
		planet.y = 320;
		planet.radius = 15;
		planets.add(planet);
	}

	@Override
	public void render () {
		ScreenUtils.clear(0.08f, 0.059f, 0.114f, 1);

		camera.update();

		batch.setProjectionMatrix(camera.combined);

		batch.begin();
		for(Circle planet: planets) {
			batch.draw(planetImage, planet.x, planet.y);
		}
		batch.end();

	}
	
	@Override
	public void dispose () {
		planetImage.dispose();
		batch.dispose();
	}
}

//shapeRenderer.begin(ShapeType.Line);
//shapeRenderer.line(touchPos.x, touchPos.y, someSprite.getX(), someSprite().getY());
//shapeRenderer.end();


//batch.draw(
//    yourTexture,                                  // the texture or texture region
//    x - yourTextureWidth / 2, y,                  // x and y coordinates taking into account the origin (horizontal origin is the middle yourTextureWidth / 2, vertical origin is the bottom 0)
//    yourTextureWidth / 2, 0,                      // the origin (horizontal origin is the middle yourTextureWidth / 2, vertical origin is the bottom 0)
//    yourTextureWidth, yourTextureHeight,          // width and height (keep them the same)
//    1, 1,                                         // scale (unaffected)
//    rotation                                      // rotation in degrees
//);
