package com.bang;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
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

import java.util.Locale;
import java.util.Random;

public class Bang5 implements ApplicationListener, InputProcessor {

    // Real World
    public static final int PPM = 34;
    static final int VELOCITY_ITERATIONS = 6;
    static final int POSITION_ITERATIONS = 2;

    // GA
    int wavetotal = 10;
    int wave = 0;
    int gen = 0;

    OrthographicCamera[] box2DCamera = new OrthographicCamera[wavetotal];
    Box2DDebugRenderer[] debugRenderer = new Box2DDebugRenderer[wavetotal];

    World[] world = new World[wavetotal];

    SpriteBatch batch;

    int WIDTH;
    int HEIGHT;

    // Objects
    Body[] bodyObj = new Body[wavetotal];
    Body[] bodyLnchr = new Body[wavetotal];
    Body[] bodyTarget = new Body[wavetotal];
    Body[] circle = new Body[wavetotal];

    // Objects
    float[] angle = new float[wavetotal];
    float[] power = new float[wavetotal];
    float LauncherX = 5;
    float LauncherY = 2;
    float targetX = 100;
    float targetY = 1;
    float weight = 20;
    boolean[] collide = new boolean[wavetotal];

    // Form
    BitmapFont[] font1 = new BitmapFont[wavetotal];

    boolean isCollideGround = false;
    boolean isCollideTarget = false;

    @Override
    public void create() {
        WIDTH = Gdx.graphics.getWidth();
        HEIGHT = Gdx.graphics.getHeight();

        batch = new SpriteBatch();

        for(int i=0; i<wavetotal; i++){
            angle[i] = RamdomValues(0.0f, 01.3f);
            power[i] = RamdomValues(6.0f, 80.0f);

            collide[i] = true;

            box2DCamera[i] = new OrthographicCamera(WIDTH/10, HEIGHT/10);
            box2DCamera[i].setToOrtho(false, WIDTH/10, HEIGHT/10);
            box2DCamera[i].position.set(58.0f, 25.0f, 0.0f);
            box2DCamera[i].update();

            world[i] = new World(new Vector2(0.0f,-9.8f),true);

            debugRenderer[i] = new Box2DDebugRenderer();

            // Objects
            BodyTarget(1.0f, 1.0f, targetX, targetY, BodyDef.BodyType.StaticBody, i);
            BodyTower(2.0f, LauncherY, LauncherX, LauncherY, i);
            BodyLauncher(1.0f, 2.0f, LauncherX, LauncherY, angle[i], i);
            BodyBase(LauncherX, LauncherY, 2.0f, i);
            BodyGround(WIDTH, 0.0f, 0.0f, 0.0f, true, i);
            Rotate(LauncherX, LauncherY, 4.0f, angle[i], i);

            Shot(LauncherX, LauncherY, power[i], weight, angle[i], i);

            font1[i] = new BitmapFont(Gdx.files.internal("fonts/verdana10.fnt"));
            font1[i].setColor(Color.WHITE);
            font1[i].getData().setScale(1.0f, 1.0f);
        }

        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        for(int i=0; i<wavetotal; i++) {
            debugRenderer[i].render(world[i], box2DCamera[i].combined);
            box2DCamera[i].update();


            isCollideGround = CollisionBox("ground", "shot", i);
            isCollideTarget = CollisionBox("alvo", "shot", i);

            bodyTarget[i].setTransform(targetX, targetY, 0);



            PanelScore(i);

            world[i].step(Gdx.graphics.getDeltaTime(), VELOCITY_ITERATIONS, POSITION_ITERATIONS);
        }

        /*
        if ( (isCollideGround)) {
            if (!collide[id]) {
                collide[id] = true;

                //
            }
        }

        if ( (isCollideTarget)) {
            if (!collide[id]) {
                collide[id] = true;

                //
            }
        }
        */
    }

    private void PanelScore(int i){
        batch.begin();
        font1[i].draw(batch, i + ") Angle: " + String.format(Locale.US, "%10.9f", angle[i]), 10, (HEIGHT - 10) - (i * 20));
        font1[i].draw(batch, " Power: " + String.format(Locale.US, "%10.9f", power[i]), 140, (HEIGHT - 10) - (i * 20));
        font1[i].draw(batch, " Distance: " + String.format(Locale.US, "%10.9f", bodyObj[i].getPosition().x), 270, (HEIGHT - 10) - (i * 20));
        font1[i].draw(batch,String.valueOf(i) , bodyObj[i].getPosition().x*10-22, bodyObj[i].getPosition().y*10+23);
        batch.end();
    }

    public float RamdomValues(float  min, float  max){
        Random b = new Random();
        return min + (max - min) * b.nextFloat();
    }

    private void Shot(float x, float y, float power, float weight, float ang, int id){
            if(bodyObj[id] != null) {
                world[id].destroyBody(bodyObj[id]);
                collide[id] = true;
            }

            if(collide[id]) {
                // Rotation
                float tx = (float)Math.sin((ang - 1.5f) * (-1));
                float ty = (float)Math.cos((ang - 1.5f) * (-1));

                // Angle
                float arctan = (float)Math.atan(tx / ty);

                Rotate(LauncherX, LauncherY, 4, angle[id], id);

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
                        weight,
                        id,
                        world[id]);

                collide[id] = false;
            }
    }

    private void Rotate(float x, float y, float axe, float ang, int id){

        // Rotation
        float tx = (float)Math.sin((ang - 1.5f) * (-1));
        float ty = (float)Math.cos((ang - 1.5f) * (-1));

        // Angle
        float arctan = (float)Math.atan(tx / ty);

        bodyLnchr[id].setTransform((tx * axe) + x, (ty * axe) + y, arctan * (-1));
        circle[id].setTransform( x, y, arctan * (-1) + (1.5f) );
    }

    private void BodyTarget(float w, float h, float x, float y, BodyDef.BodyType bodytype, int id){
        BodyDef bodyTargetDef = new BodyDef();
        bodyTargetDef.type = bodytype;
        bodyTargetDef.position.set(x, (y*2));

        bodyTarget[id] = world[id].createBody(bodyTargetDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(w, h);

        FixtureDef fixtureBoxDef = new FixtureDef();
        fixtureBoxDef.shape = shape;
        fixtureBoxDef.density = 10;

        Fixture fixtureBox = bodyTarget[id].createFixture(fixtureBoxDef);
        bodyTarget[id].setUserData("alvo");

        shape.dispose();
    }

    private void BodyGround(float w, float h, float x, float y, boolean collision, int id) {
        BodyDef bodyGroundDef = new BodyDef();
        bodyGroundDef.type = BodyDef.BodyType.StaticBody;
        bodyGroundDef.position.set(x, y);

        Body bodyGnd = world[id].createBody(bodyGroundDef);

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

    private void BodyShot(float w, float h, float inpulseX, float inpulseY, float transformX, float transformY, float angle, float x, float y, float weight, int id, World world) {
        BodyDef bodyObjDef = new BodyDef();
        bodyObjDef.type = BodyDef.BodyType.DynamicBody;
        bodyObjDef.position.set(x, y);

        bodyObj[id] = world.createBody(bodyObjDef);
        bodyObj[id].applyLinearImpulse(inpulseX, inpulseY, x, y, true);
        bodyObj[id].setTransform( transformX, transformY, angle );

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(w, h);

        FixtureDef fixtureBallDef = new FixtureDef();
        fixtureBallDef.density = weight;
        fixtureBallDef.friction = 5.0f;
        fixtureBallDef.restitution = 0.00f;
        fixtureBallDef.shape = shape;

        Fixture fixtureBall = bodyObj[id].createFixture(fixtureBallDef);

        bodyObj[id].setUserData("shot");

        shape.dispose();
    }

    private void BodyBase(float x, float y, float radius, int id){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x, y);

        circle[id] = world[id].createBody(bodyDef);

        CircleShape dynamicCircle = new CircleShape();
        dynamicCircle.setRadius(radius);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = dynamicCircle;
        fixtureDef.density = 0.4f;
        fixtureDef.friction = 0.2f;
        fixtureDef.restitution = 0.6f;

        circle[id].createFixture(fixtureDef);


        dynamicCircle.dispose();
    }

    private void BodyTower(float w, float h, float x, float y, int id){
        BodyDef bodyTowerDef = new BodyDef();
        bodyTowerDef.type = BodyDef.BodyType.KinematicBody;
        bodyTowerDef.position.set(x, y/2);

        Body bodyTwer = world[id].createBody(bodyTowerDef);

        PolygonShape shape1 = new PolygonShape();
        shape1.setAsBox(w, h/2);

        FixtureDef fixtureDef1 = new FixtureDef();
        fixtureDef1.shape = shape1;
        fixtureDef1.density = 1;

        Fixture fixtureTank = bodyTwer.createFixture(fixtureDef1);
    }

    private void BodyLauncher(float w, float h, float x, float y, float ang, int id) {

        BodyDef bodyLauncherDef = new BodyDef();
        bodyLauncherDef.type = BodyDef.BodyType.KinematicBody;
        bodyLauncherDef.position.set(x, (y+w+h));

        bodyLnchr[id] = world[id].createBody(bodyLauncherDef);

        PolygonShape shape1 = new PolygonShape();
        shape1.setAsBox(w, h);

        FixtureDef fixtureDef1 = new FixtureDef();
        fixtureDef1.shape = shape1;
        fixtureDef1.density = 1;

        Fixture fixtureTank = bodyLnchr[id].createFixture(fixtureDef1);
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
        batch.dispose();

        for(int i=0; i<wavetotal; i++)
            font1[i].dispose();
    }

    private boolean CollisionBox(String a, String b, int id) {
        boolean touch = false;

        for (int i = 0; i < world[id].getContactCount(); i++) {
            Contact contact = world[id].getContactList().get(i);

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

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        for(int i=0; i<wavetotal; i++) {
            angle[i] = RamdomValues(0.0f, 01.3f);
            power[i] = RamdomValues(6.0f, 80.0f);

            Shot(LauncherX, LauncherY, power[i], weight, angle[i], i);
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
