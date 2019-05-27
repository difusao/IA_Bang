package com.bang;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import java.util.Random;

public class Bang5 implements ApplicationListener {

    // Real World
    public static final int PPM = 34;
    static final int VELOCITY_ITERATIONS = 6;
    static final int POSITION_ITERATIONS = 2;

    //
    int wavetotal = 4;
    int wave = 0;

    OrthographicCamera box2DCamera1;
    OrthographicCamera box2DCamera2;
    OrthographicCamera box2DCamera3;
    OrthographicCamera box2DCamera4;
    Box2DDebugRenderer debugRenderer1;
    Box2DDebugRenderer debugRenderer2;
    Box2DDebugRenderer debugRenderer3;
    Box2DDebugRenderer debugRenderer4;
    World world;
    SpriteBatch batch;

    int WIDTH;
    int HEIGHT;

    // Objects
    Body bodyObj;
    Body bodyLnchr;
    Body[] bodyTarget = new Body[wavetotal];
    Body circle;

    // Objects
    float angle = 01.10968124866485600f;
    float power = 34.01906204223633000f;
    float LauncherX = 5;
    float LauncherY = 2;
    float targetX1 = 95;
    float targetX2 = 75;
    float targetX3 = 55;
    float targetX4 = 35;
    float targetY = 1;
    float weight = 20;
    boolean collide = true;

    // Form
    BitmapFont font1;
    BitmapFont font2;

    boolean isCollideGround = false;
    boolean isCollideTarget = false;

    @Override
    public void create() {
        WIDTH = Gdx.graphics.getWidth();
        HEIGHT = Gdx.graphics.getHeight();

        box2DCamera1 = new OrthographicCamera(WIDTH, HEIGHT);
        box2DCamera1.setToOrtho(false, WIDTH/4, HEIGHT/4);
        box2DCamera1.position.set(100, 0, 0);
        box2DCamera1.update();

        box2DCamera2 = new OrthographicCamera(WIDTH, HEIGHT);
        box2DCamera2.setToOrtho(false, WIDTH/4, HEIGHT/4);
        box2DCamera2.position.set(0, 0, 0);
        box2DCamera2.update();

        box2DCamera3 = new OrthographicCamera(WIDTH, HEIGHT);
        box2DCamera3.setToOrtho(false, WIDTH/4, HEIGHT/4);
        box2DCamera3.position.set(100, 100, 0);
        box2DCamera3.update();

        box2DCamera4 = new OrthographicCamera(WIDTH, HEIGHT);
        box2DCamera4.setToOrtho(false, WIDTH/4, HEIGHT/4);
        box2DCamera4.position.set(0, 100, 0);
        box2DCamera4.update();

        world = new World(new Vector2(0,-9.8f),true);

        debugRenderer1 = new Box2DDebugRenderer();
        debugRenderer2 = new Box2DDebugRenderer();
        debugRenderer3 = new Box2DDebugRenderer();
        debugRenderer4 = new Box2DDebugRenderer();

        batch = new SpriteBatch();

        font1 = new BitmapFont(Gdx.files.internal("fonts/verdana10.fnt"));
        font1.setColor(Color.WHITE);
        font1.getData().setScale(1f, 1f);

        font2 = new BitmapFont(Gdx.files.internal("fonts/verdana10.fnt"));
        font2.setColor(Color.WHITE);
        font2.getData().setScale(1f, 1f);

        // Objects
        BodyTarget(1.0f, 1.0f, targetX1, 5.0f, BodyDef.BodyType.StaticBody, 0);
        BodyTarget(1.0f, 1.0f, targetX2, 5.0f, BodyDef.BodyType.StaticBody, 0);
        BodyTarget(1.0f, 1.0f, targetX3, 5.0f, BodyDef.BodyType.StaticBody, 0);
        BodyTarget(1.0f, 1.0f, targetX4, 5.0f, BodyDef.BodyType.StaticBody, 0);

        //BodyTower(2, LauncherY, LauncherX, LauncherY);
        //BodyLauncher(1, 2, LauncherX, LauncherY, angle);
        //BodyBase(LauncherX, LauncherY, 2);
        //BodyGround(WIDTH, 0, 0, 0, true);
        //Rotate(LauncherX, LauncherY, 4, angle);

        //Shot(LauncherX, LauncherY, power, weight, angle, 0);
        //Shot(LauncherX, LauncherY, power, weight, angle, 1);
        //Shot(LauncherX, LauncherY, power, weight, angle, 2);
        //Shot(LauncherX, LauncherY, power, weight, angle, 3);
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        debugRenderer1.render(world, box2DCamera1.combined);
        debugRenderer2.render(world, box2DCamera2.combined);
        debugRenderer3.render(world, box2DCamera3.combined);
        debugRenderer4.render(world, box2DCamera4.combined);

        box2DCamera1.update();
        box2DCamera2.update();
        box2DCamera3.update();
        box2DCamera4.update();

        isCollideGround = CollisionBox("ground", "shot");
        isCollideTarget = CollisionBox("alvo", "shot");

        bodyTarget[0].setTransform(targetX1, targetY, 0);
        bodyTarget[1].setTransform(targetX2, targetY, 0);
        bodyTarget[2].setTransform(targetX3, targetY, 0);
        bodyTarget[3].setTransform(targetX4, targetY, 0);

        if ( (isCollideGround)) {
            if (!collide) {
                collide = true;

                //
            }
        }

        if ( (isCollideTarget)) {
            if (!collide) {
                collide = true;

                //
            }
        }

        world.step(Gdx.graphics.getDeltaTime(), VELOCITY_ITERATIONS, POSITION_ITERATIONS);
    }

    public float RamdomValues(float  min, float  max){
        Random b = new Random();
        return min + (max - min) * b.nextFloat();
    }

    private void Shot(float x, float y, float power, float weight, float ang){
            if(bodyObj != null) {
                world.destroyBody(bodyObj);
                collide = true;
            }

            if(collide) {
                // Rotation
                float tx = (float)Math.sin((ang - 1.5f) * (-1));
                float ty = (float)Math.cos((ang - 1.5f) * (-1));

                // Angle
                float arctan = (float)Math.atan(tx / ty);

                Rotate(LauncherX, LauncherY, 4, angle);

                BodyShot(
                        1,
                        1,
                        (tx) * power,
                        (ty) * power,
                        (tx * 7.5f) + x,
                        (ty * 7.5f) + y,
                        arctan * (-1),
                        x + 5,
                        y + 5,
                        weight);

                collide = false;
            }
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

    private void BodyTarget(float w, float h, float x, float y, BodyDef.BodyType bodytype, int id){
        BodyDef bodyTargetDef = new BodyDef();
        bodyTargetDef.type = bodytype;
        bodyTargetDef.position.set(x, (y*2));

        bodyTarget[id] = world.createBody(bodyTargetDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(w, h);

        FixtureDef fixtureBoxDef = new FixtureDef();
        fixtureBoxDef.shape = shape;
        fixtureBoxDef.density = 10;

        Fixture fixtureBox = bodyTarget[id].createFixture(fixtureBoxDef);
        bodyTarget[id].setUserData("alvo");

        shape.dispose();
    }

    private void BodyGround(float w, float h, float x, float y, boolean collision) {
        BodyDef bodyGroundDef = new BodyDef();
        bodyGroundDef.type = BodyDef.BodyType.StaticBody;
        bodyGroundDef.position.set(x, y);

        Body bodyGnd = world.createBody(bodyGroundDef);

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

    private void BodyShot(float w, float h, float inpulseX, float inpulseY, float transformX, float transformY, float angle, float x, float y, float weight) {
        BodyDef bodyObjDef = new BodyDef();
        bodyObjDef.type = BodyDef.BodyType.DynamicBody;
        bodyObjDef.position.set(x, y);
        bodyObj = world.createBody(bodyObjDef);
        bodyObj.applyLinearImpulse(inpulseX, inpulseY, x, y, true);
        bodyObj.setTransform( transformX, transformY, angle );

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(w, h);

        FixtureDef fixtureBallDef = new FixtureDef();
        fixtureBallDef.density = weight;
        fixtureBallDef.friction = 5.0f;
        fixtureBallDef.restitution = 0.00f;
        fixtureBallDef.shape = shape;

        Fixture fixtureBall = bodyObj.createFixture(fixtureBallDef);

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

    private void BodyTower(float w, float h, float x, float y){
        BodyDef bodyTowerDef = new BodyDef();
        bodyTowerDef.type = BodyDef.BodyType.KinematicBody;
        bodyTowerDef.position.set(x, y/2);

        Body bodyTwer = world.createBody(bodyTowerDef);

        PolygonShape shape1 = new PolygonShape();
        shape1.setAsBox(w, h/2);

        FixtureDef fixtureDef1 = new FixtureDef();
        fixtureDef1.shape = shape1;
        fixtureDef1.density = 1;

        Fixture fixtureTank = bodyTwer.createFixture(fixtureDef1);
    }

    private void BodyLauncher(float w, float h, float x, float y, float ang) {

        BodyDef bodyLauncherDef = new BodyDef();
        bodyLauncherDef.type = BodyDef.BodyType.KinematicBody;
        bodyLauncherDef.position.set(x, (y+w+h));

        bodyLnchr = world.createBody(bodyLauncherDef);

        PolygonShape shape1 = new PolygonShape();
        shape1.setAsBox(w, h);

        FixtureDef fixtureDef1 = new FixtureDef();
        fixtureDef1.shape = shape1;
        fixtureDef1.density = 1;

        Fixture fixtureTank = bodyLnchr.createFixture(fixtureDef1);
    }

    @Override
    public void resize(int width, int height) {

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

    private boolean CollisionBox(String a, String b) {
        boolean touch = false;

        for (int i = 0; i < world.getContactCount(); i++) {
            Contact contact = world.getContactList().get(i);

            if (contact.isTouching()) {

                Body contactA = contact.getFixtureA().getBody();
                Body contactB = contact.getFixtureB().getBody();

                if(contactA.getUserData().equals(a) && contactB.getUserData().equals(b)) {
                    touch = true;
                }
            }
        }

        return touch;
    }
}
