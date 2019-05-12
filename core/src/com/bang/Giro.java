package com.bang;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class Giro extends ApplicationAdapter {

    // Real World
    public static final int PPM = 30;
    static final int VELOCITY_ITERATIONS = 6;
    static final int POSITION_ITERATIONS = 2;

    OrthographicCamera box2DCamera;
    Box2DDebugRenderer debugRenderer;
    World world;
    Stage stage;

    int WIDTH;
    int HEIGHT;

    // Objects
    BodyDef bodyGroundDef;
    BodyDef bodyTargetDef;
    BodyDef bodyObjDef;
    BodyDef bodyLauncherDef;
    BodyDef bodyTowerDef;
    Body bodyGnd;
    Body bodyObj;
    Body bodyLnchr;
    Body bodyTwer;
    Body bodyTarget;
    Body circle;

    FixtureDef fixtureBallDef;
    FixtureDef fixtureBoxDef;
    Fixture fixtureBall;
    Fixture fixtureBox;
    Fixture fixtureTank;

    private Slider sldAngle;
    private Slider sldElevate;

    private float angle = 0;
    private float LauncherX = 0;
    private float LauncherY = 2;

    @Override
    public void create() {
        WIDTH = Gdx.graphics.getWidth();
        HEIGHT = Gdx.graphics.getHeight();

        box2DCamera = new OrthographicCamera();
        box2DCamera.setToOrtho(false, (WIDTH) / (PPM / 3), (HEIGHT) / (PPM / 3));
        box2DCamera.position.set(50, 20, 0);
        box2DCamera.update();
        world = new World(new Vector2(0, -9.8f), true);
        debugRenderer = new Box2DDebugRenderer();

        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

        Skin skin = new Skin(Gdx.files.internal("themes/default/skin/uiskin.json"));

        sldAngle = new Slider(0.0f, 1.5f, 0.1f, false, skin);
        sldAngle.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                angle = sldAngle.getValue();
                Rotate(LauncherX, LauncherY, 6, angle);
            }
        });

        sldElevate = new Slider(2.0f, 20.0f, 0.1f, false, skin);
        sldElevate.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if(bodyTwer != null)
                    world.destroyBody(bodyTwer);

                if(circle != null)
                    world.destroyBody(circle);

                if(bodyLnchr != null)
                    world.destroyBody(bodyLnchr);

                LauncherY =  sldElevate.getValue();

                angle = sldAngle.getValue();
                BodyTower(2, LauncherY, 0, LauncherY);
                BodyLauncher(1, 4, LauncherX, LauncherY, angle);
                BodyBase(LauncherX, LauncherY, 2);

                Rotate(LauncherX, LauncherY, 6, angle);
            }
        });

        TextButton button1 = new TextButton("Launch", skin);
        button1.addListener(new InputListener() {
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                if(bodyObj != null)
                    world.destroyBody(bodyObj);

                Shot( LauncherX, LauncherY,30, 50, angle);

                return false;
            }
        });

        Table table = new Table();
        //table.debug();
        stage.addActor(table);
        table.setSize(300, 50);
        table.setPosition(10, HEIGHT - 100);
        table.add(sldAngle).width(100).height(30);
        table.row();
        table.add(sldElevate).width(100).height(30);
        table.row();
        table.add(button1);

        angle = sldAngle.getValue();
        BodyTower(2, LauncherY, 0, LauncherY);
        BodyLauncher(1, 4, LauncherX, LauncherY, angle);
        BodyBase(LauncherX, LauncherY, 2);
        BodyGround(WIDTH / 2, 0, 0, 0, true);

        Rotate(LauncherX, LauncherY, 6, angle);
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        debugRenderer.render(world, box2DCamera.combined);
        box2DCamera.update();

        stage.draw();

        world.step(Gdx.graphics.getDeltaTime(), VELOCITY_ITERATIONS, POSITION_ITERATIONS);
    }

    @Override
    public void dispose () {

    }

    private void Shot(float x, float y, float power, float weight, float ang){

        // Rotation
        float tx = (float)Math.sin((ang - 1.5f) * (-1));
        float ty = (float)Math.cos((ang - 1.5f) * (-1));

        // Angle
        float arctan = (float)Math.atan(tx / ty);

        BodyShot(
                1,
                1,
                (tx) * power,
                (ty)  * power,
                (tx * 10.5f) + x,
                (ty * 10.5f) + y,
                arctan * (-1),
                x + 5,
                y  + 5,
                weight);
    }

    private void Rotate(float x, float y, float axe, float ang){

        // Rotation
        float tx = (float)Math.sin((ang - 1.5f) * (-1));
        float ty = (float)Math.cos((ang - 1.5f) * (-1));

        // Angle
        float arctan = (float)Math.atan(tx / ty);

        bodyLnchr.setTransform((tx * axe) + x, (ty * axe) + y, arctan * (-1));
        circle.setTransform( x, y, arctan * (-1) + (1.5f) );

    }

    private void BodyShot(float w, float h, float inpulseX, float inpulseY, float transformX, float transformY, float angle, float x, float y, float weight) {
        bodyObjDef = new BodyDef();
        bodyObjDef.type = BodyDef.BodyType.DynamicBody;
        //bodyObjDef.type = BodyDef.BodyType.StaticBody;
        bodyObjDef.position.set(x, y);
        bodyObj = world.createBody(bodyObjDef);
        bodyObj.applyLinearImpulse(inpulseX, inpulseY, x, y, true);
        bodyObj.setTransform( transformX, transformY, angle );

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(w, h);

        fixtureBallDef = new FixtureDef();
        fixtureBallDef.density = weight;
        fixtureBallDef.friction = 5.0f;
        fixtureBallDef.restitution = 0.00f;
        fixtureBallDef.shape = shape;

        fixtureBall = bodyObj.createFixture(fixtureBallDef);

        bodyObj.setUserData("shot");

        shape.dispose();
    }

    private void BodyBase(float x, float y, float radius){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x, y);
        circle = world.createBody(bodyDef);

        CircleShape dynamicCircle = new CircleShape();
        dynamicCircle.setRadius(radius);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = dynamicCircle;
        fixtureDef.density = 0.4f;
        fixtureDef.friction = 0.2f;
        fixtureDef.restitution = 0.6f;

        circle.createFixture(fixtureDef);


        dynamicCircle.dispose();
    }

    private void BodyGround(float w, float h, float x, float y, boolean collision) {
        bodyGroundDef = new BodyDef();
        bodyGroundDef.type = BodyDef.BodyType.StaticBody;
        bodyGroundDef.position.set(x, y);

        bodyGnd = world.createBody(bodyGroundDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(w, h);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1;

        Fixture fixture = bodyGnd.createFixture(fixtureDef);

        if(collision)
            bodyGnd.setUserData("ground");

        shape.dispose();
    }

    private void BodyTower(float w, float h, float x, float y){
        bodyTowerDef = new BodyDef();
        bodyTowerDef.type = BodyDef.BodyType.KinematicBody;
        bodyTowerDef.position.set(x, y/2);

        bodyTwer = world.createBody(bodyTowerDef);

        PolygonShape shape1 = new PolygonShape();
        shape1.setAsBox(w, h/2);

        FixtureDef fixtureDef1 = new FixtureDef();
        fixtureDef1.shape = shape1;
        fixtureDef1.density = 1;

        fixtureTank = bodyTwer.createFixture(fixtureDef1);
    }

    private void BodyLauncher(float w, float h, float x, float y, float ang) {

        bodyLauncherDef = new BodyDef();
        bodyLauncherDef.type = BodyDef.BodyType.KinematicBody;
        bodyLauncherDef.position.set(x, (y+w+h));

        bodyLnchr = world.createBody(bodyLauncherDef);

        PolygonShape shape1 = new PolygonShape();
        shape1.setAsBox(w, h);

        FixtureDef fixtureDef1 = new FixtureDef();
        fixtureDef1.shape = shape1;
        fixtureDef1.density = 1;

        fixtureTank = bodyLnchr.createFixture(fixtureDef1);


    }
}
