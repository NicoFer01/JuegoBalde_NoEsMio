package com.meteor.test1;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class Test extends ApplicationAdapter {
	private Texture dropImage;
	private Texture acidDropImage;
	private Texture goldenDropImage;
	private Texture bucketImage;
	private Sound dropSound;
	private Music rainMusic;
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private Rectangle bucket;
	private Array<Rectangle> raindrops;
	private Array<Rectangle> acidRaindrops;
	private Array<Rectangle> goldenRaindrops;
	private long lastDropTime;
	private long lastAcidDropTime;
	private long lastGoldenDropTime;
	private int score;
	private int scoreToShrink;

	@Override
	public void create() {
		// load the images for the droplet and the bucket, 64x64 pixels each
		//and the gameOverBall
		dropImage = new Texture(Gdx.files.internal("droplet.png"));
		acidDropImage = new Texture(Gdx.files.internal("acidRaindrop.png"));
		goldenDropImage = new Texture(Gdx.files.internal("goldenRaindrop.jpg"));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));

		// load the drop sound effect and the rain background "music"
		dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

		// start the playback of the background music immediately
		rainMusic.setLooping(true);
		rainMusic.play();

		// create the camera and the SpriteBatch
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 650, 960);
		batch = new SpriteBatch();

		// create a Rectangle to logically represent the bucket
		bucket = new Rectangle();
		bucket.x = 650 / 2 - 64 / 2; // center the bucket horizontally
		bucket.y = 20; // bottom left corner of the bucket is 20 pixels above the bottom screen edge
		bucket.width = 64;
		bucket.height = 64;

		// create the raindrops array and spawn the first raindrop
		raindrops = new Array<Rectangle>();
		spawnRaindrop();

		// create the acidRaindrops array and spawn the first ball
		acidRaindrops = new Array<Rectangle>();
		spawnAcidRaindrop();

		// create the goldenRaindrops array and spawn the first ball
		goldenRaindrops = new Array<Rectangle>();
		spawnGoldenRaindrop();

		score = 0;

		scoreToShrink = 10;
	}

	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, 650-64);
		raindrop.y = 960;
		raindrop.width = 64;
		raindrop.height = 64;
		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
	}

	private void spawnAcidRaindrop() {
		Rectangle acidRaindrop = new Rectangle();
		acidRaindrop.x = MathUtils.random(0, 650-64);
		acidRaindrop.y = 960;
		acidRaindrop.width = 64;
		acidRaindrop.height = 64;
		acidRaindrops.add(acidRaindrop);
		lastAcidDropTime = TimeUtils.nanoTime();
	}

	private void spawnGoldenRaindrop() {
		Rectangle goldenRaindrop = new Rectangle();
		goldenRaindrop.x = MathUtils.random(0, 650-64);
		goldenRaindrop.y = 960;
		goldenRaindrop.width = 64;
		goldenRaindrop.height = 64;
		goldenRaindrops.add(goldenRaindrop);
		lastGoldenDropTime = TimeUtils.nanoTime();
	}

	private void checkScore() {
		//the bucket becomes smaller if player catches 10 raindrops
		if (score >= scoreToShrink) {
			bucket.width -= 4;
			bucket.height -= 4;
			//bucketImage.draw();
			//code to make the image smaller

			if (scoreToShrink <= 100) {
				scoreToShrink += 10;
			}
		}
	}

	@Override
	public void render() {
		// clear the screen with a dark blue color. The
		// arguments to clear are the red, green
		// blue and alpha component in the range [0,1]
		// of the color to be used to clear the screen.
		ScreenUtils.clear(0, 0, 0.2f, 1);

		// tell the camera to update its matrices.
		camera.update();

		// tell the SpriteBatch to render in the
		// coordinate system specified by the camera.
		batch.setProjectionMatrix(camera.combined);

		// begin a new batch and draw the bucket and
		// all drops and balls
		batch.begin();
		batch.draw(bucketImage, bucket.x, bucket.y);
		for(Rectangle raindrop: raindrops) {
			batch.draw(dropImage, raindrop.x, raindrop.y);
		}
		for(Rectangle acidRaindrop: acidRaindrops) {
			batch.draw(acidDropImage, acidRaindrop.x, acidRaindrop.y);
		}
		for(Rectangle goldenRaindrop: goldenRaindrops) {
			batch.draw(goldenDropImage, goldenRaindrop.x, goldenRaindrop.y);
		}
		batch.end();

		// process user input
		if(Gdx.input.isTouched()) {
			Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			bucket.x = touchPos.x - 64 / 2;
			bucket.y = touchPos.y - 64 / 2;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) bucket.x -= 200 * Gdx.graphics.getDeltaTime();
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) bucket.x += 200 * Gdx.graphics.getDeltaTime();

		// make sure the bucket stays within the screen bounds
		if(bucket.x < 0) bucket.x = 0;
		if(bucket.x > 800 - 64) bucket.x = 650 - 64;
		if(bucket.y < 0) bucket.y = 0;
		if(bucket.y > 800 - 64) bucket.y = 960 - 64;

		// check if we need to create a new raindrop, acidRaindrop or goldenRaindrop
		if(TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRaindrop();
		if(TimeUtils.nanoTime() - lastAcidDropTime > 2140000000) spawnAcidRaindrop();
		if(TimeUtils.nanoTime() - lastGoldenDropTime > 2147483000) spawnGoldenRaindrop();

		// move the raindrops, remove any that are beneath the bottom edge of
		// the screen or that hit the bucket. In the latter case we play back
		// a sound effect as well.
		for (Iterator<Rectangle> iter = raindrops.iterator(); iter.hasNext(); ) {
			Rectangle raindrop = iter.next();
			raindrop.y -= 250 * Gdx.graphics.getDeltaTime();
			if(raindrop.y + 64 < 0) iter.remove();
			if(raindrop.overlaps(bucket)) {
				dropSound.play();
				score += 1;
				iter.remove();
			}
		}

		// move the gameOverBalls, remove any that are beneath the bottom edge of
		// the screen. If any hits the bucket, the game ends.
		for (Iterator<Rectangle> iter = acidRaindrops.iterator(); iter.hasNext(); ) {
			Rectangle acidRaindrop = iter.next();
			acidRaindrop.y -= 300 * Gdx.graphics.getDeltaTime();
			if(acidRaindrop.y + 64 < 0) iter.remove();
			if(acidRaindrop.overlaps(bucket)) {
				System.exit(0);
			}
		}

		for (Iterator<Rectangle> iter = goldenRaindrops.iterator(); iter.hasNext(); ) {
			Rectangle goldenRaindrop = iter.next();
			goldenRaindrop.y -= 100 * Gdx.graphics.getDeltaTime();
			if(goldenRaindrop.y + 64 < 0) iter.remove();
			if(goldenRaindrop.overlaps(bucket)) {
				dropSound.play();
				score += 2;
				iter.remove();
			}
		}

		//checkScore();
	}

	@Override
	public void dispose() {
		// dispose of all the native resources
		dropImage.dispose();
		acidDropImage.dispose();
		goldenDropImage.dispose();
		bucketImage.dispose();
		dropSound.dispose();
		rainMusic.dispose();
		batch.dispose();
	}
}
