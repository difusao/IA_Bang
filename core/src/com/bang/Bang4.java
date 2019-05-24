package com.bang;

import com.badlogic.gdx.ApplicationAdapter;
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
import com.nn.NetworkUtils;
import com.nn.NeurophStudio;

import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;

import java.util.Arrays;
import java.util.Locale;

public class Bang4 extends ApplicationAdapter{

    // Real World
    public static final int PPM = 30;
    static final int VELOCITY_ITERATIONS = 6;
    static final int POSITION_ITERATIONS = 2;

    OrthographicCamera box2DCamera;
    Box2DDebugRenderer debugRenderer;
    World world;

    int WIDTH;
    int HEIGHT;

    // Time Elapsed
    long start = 0;
    long finish = 0;
    float timeElapsed = 0;

    // AG
    int waveTotal = 3;
    int wave = 0;
    float best = 0.1f;
    int gen = 1;

    // Objects
    float angle = 01.10968124866485600f;
    float LauncherX = 5;
    float LauncherY = 2;
    float power = 34.01906204223633000f;
    float targetX = 90;
    float targetY = 1;
    float weight = 50;
    float height = 0;
    float percentsel = 0.5f;

    float maxhight = 0;

    int count = -1;
    boolean shot = true;
    double[] arrAngles = new double[waveTotal];
    double[] arrPowers = new double[waveTotal];
    double[] arrHight = new double[waveTotal];
    double tx = 0;
    double ty = 0;
    double ang = 0;

    // Objects
    //BodyDef bodyGroundDef;
    //BodyDef bodyTargetDef;
    //BodyDef bodyObjDef;
    //BodyDef bodyLauncherDef;
    //BodyDef bodyTowerDef;

    Body bodyObj;
    Body bodyLnchr;
    Body bodyTarget;
    Body circle;

    boolean collide = true;

    // Form
    SpriteBatch batch;
    BitmapFont font1;
    BitmapFont font2;
    String status;

    // NeuralNetwork
    DataSet trainingSet1 = new DataSet(1, 2);
    DataSet trainingSet2 = new DataSet(1, 2);
    String FileDataset = "DataSet.tset";
    String FileNetwork = "NewNeuralNetwork";
    String pathDataSet = "data/";
    String pathNetwork = "data/";

    NeurophStudio rna;
    NetworkUtils nnu;
    boolean neural = false;

    boolean isCollideGround = false;
    boolean isCollideTarget = false;
    boolean test = false;
    double[][] rndWeights;
    int layers[] = new int[]{1, 6, 2};

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        debugRenderer.render(world, box2DCamera.combined);
        box2DCamera.update();

        isCollideGround = CollisionBox("ground", "shot");
        isCollideTarget = CollisionBox("alvo", "shot");

        bodyTarget.setTransform(targetX, targetY, 0);

        if ( wave <= waveTotal && (isCollideGround || isCollideTarget) && !test) {
            if (!collide) {
                collide = true;

                TestWeights();
            }
        }

        PanelInfo();

        world.step(Gdx.graphics.getDeltaTime(), VELOCITY_ITERATIONS, POSITION_ITERATIONS);
    }

    private void TestWeights(){
        if(wave < waveTotal) {
            // Set Weights
            rna.setWeights(FileNetwork + ".nnet", rndWeights[wave]);
            //System.out.println(wave + ") " + Arrays.toString( rndWeights[wave] ));

            // Test Weights in shots
            double[] outputNewWeight = rna.Test(FileNetwork + ".nnet", new double[]{(targetX/100)});
            angle = (float) outputNewWeight[0];
            power = (float) outputNewWeight[1] * 100;

            //System.out.println(Arrays.toString(rna.getWeights(FileNetwork + ".nnet")) + "\n");

            // DataSet
            double[] inputs = new double[]{(bodyObj.getPosition().x/100)};
            double[] outputs = new double[]{(angle), power};
            trainingSet1.addRow(new DataSetRow(inputs, outputs));
            System.out.println(wave + ") ObjDown: " + Arrays.toString(inputs) + " Angle: " + angle + " Power: " + power );

            Shot(LauncherX, LauncherY, power, weight, angle);
            wave++;
        }else{
            world.destroyBody(bodyObj);

            // Best shot and weights
            System.out.println("\nBest shots generation");
            trainingSet2.addRow(rna.MinValueSpace(trainingSet1, (targetX/100)));
            double[][] rowWeights = new double[trainingSet2.getRows().size()][26];
            for(int i=0; i<trainingSet1.getRows().size(); i++){
                for(int j=0; j<trainingSet2.getRows().size(); j++){
                    if(trainingSet1.getRows().get(i).getInput()[0] == trainingSet2.getRows().get(j).getInput()[0]){
                        System.out.println(i + ")\n" + " Input: " + Arrays.toString(trainingSet2.getRows().get(j).getInput()) + " Output: " + Arrays.toString(trainingSet2.getRows().get(j).getDesiredOutput()) );
                        System.out.println(" Weights: " + Arrays.toString(rndWeights[i]));
                        rowWeights[j] = rndWeights[i];

                        // Set Weights
                        rna.setWeights(FileNetwork + ".nnet", rndWeights[i]);
                        //System.out.println("Set weights: " + Arrays.toString( rowWeights[j] ));
                    }
                }
            }
            System.out.println();

            System.out.println("-----------------------------------------------------------------\n");

            // Clone weights
            System.out.println("Clone " + waveTotal + " Best Weights");
            rndWeights = CloneWeights(waveTotal,26, rowWeights, 0.05f);
            for(int i=0; i< rndWeights.length; i++) {
                System.out.printf(Locale.US, "%02d)", i);
                for (int j = 0; j < rndWeights[i].length; j++)
                    System.out.printf(Locale.US, " %20.17f", rndWeights[i][j]);
                System.out.println();
            }
            System.out.println();

            // Test Weights in shots
            double[] outputNewWeight = rna.Test(FileNetwork + ".nnet", new double[]{(targetX/100)});
            angle = (float) outputNewWeight[0];
            power = (float) outputNewWeight[1] * 100;
            Shot(LauncherX, LauncherY, power, weight, angle);

            // Next generation
            wave=0;
            gen++;
            //Gdx.app.exit();

            trainingSet1.clear();
            System.out.println("Shots generation " + gen);
        }
    }

    private void TestWeights(float inObjDown, float inTargetX, boolean collideTarget){

        if(wave < waveTotal) {

            // Set Weights
            rna.setWeights(FileNetwork + ".nnet", rndWeights[wave]);
            System.out.printf(Locale.US, "%02d) ", wave);
            for (int i = 0; i < rndWeights[wave].length; i++)
                System.out.printf(Locale.US, "%20.17f ", rndWeights[wave][i]);
            //System.out.println();

            // Test input new values weights
            double[] outputNewWeight = rna.Test(FileNetwork + ".nnet", new double[]{inTargetX/100});
            System.out.println(wave + ") Input: " + Arrays.toString(new double[]{inObjDown/100}) + " Output: " + Arrays.toString(outputNewWeight));
            //System.out.println();

            angle = (float) outputNewWeight[0];
            power = (float) outputNewWeight[1] * 100;
            double[] inputs = new double[]{(inObjDown / 100)};
            double[] outputs = new double[]{angle, (power / 100)};

            // DataSet
            trainingSet1.addRow(new DataSetRow(inputs, outputs));

            Shot(LauncherX, LauncherY, power, weight, angle);
        }

        if(wave == waveTotal){
            //for(int i=0; i<trainingSet1.getRows().size(); i++)
            //    System.out.println(i + " " +
            //            "Input: " + Arrays.toString(trainingSet1.getRows().get(i).getInput()) +
            //            " Output: " + Arrays.toString(trainingSet1.getRows().get(i).getDesiredOutput())
            //    );
            //System.out.println();

            //for(int i=0; i<trainingSet1.getRows().size(); i++){
            //    System.out.println(i + " " +
            //            "Input: " + Arrays.toString(trainingSet1.getRows().get(i).getInput()) +
            //            " Output: " + Arrays.toString(trainingSet1.getRows().get(i).getDesiredOutput())
            //    );
            //}
            //System.out.println();

            // Select best shots
            //int maxi = 1; //Math.round(trainingSet1.getRows().size() * percentsel);
            double limit = (inTargetX/100);

            //trainingSet2 = rna.Best(trainingSet1, maxi, limit);
            //System.out.println("\nMaxValue: " + rna.MaxValue(trainingSet1, limit) + "\n");
            trainingSet2.addRow(rna.MinValueSpace(trainingSet1, limit));

            double[][] rowWeights = new double[trainingSet2.getRows().size()][26];
            System.out.println();
            for(int i=0; i<trainingSet1.getRows().size(); i++){
                for(int j=0; j<trainingSet2.getRows().size(); j++){
                    if(trainingSet1.getRows().get(i).getInput()[0] == trainingSet2.getRows().get(j).getInput()[0]){
                        System.out.println("Weights[" + i + "]: " + Arrays.toString(rndWeights[i]));
                        System.out.println(i + ") " + "Input: " + Arrays.toString(trainingSet2.getRows().get(j).getInput()) + " Output: " + Arrays.toString(trainingSet2.getRows().get(j).getDesiredOutput()) );
                        //System.out.println();
                        rowWeights[j] = rndWeights[i];
                    }
                }
            }
            System.out.println();

            //for(int i=0; i<trainingSet2.getRows().size(); i++){
            //    System.out.println(i + " " + "Input: " + Arrays.toString(trainingSet2.getRows().get(i).getInput()) + " Output: " + Arrays.toString(trainingSet2.getRows().get(i).getDesiredOutput()) );
            //}
            //System.out.println();

            // Clone weights
            System.out.println("Clone");
            rndWeights = CloneWeights(waveTotal,26, rowWeights, 0.05f);
            for(int i=0; i< rndWeights.length; i++) {
                System.out.printf(Locale.US, "%02d)", i);
                for (int j = 0; j < rndWeights[i].length; j++)
                    System.out.printf(Locale.US, " %20.17f", rndWeights[i][j]);
                System.out.println();
            }
            System.out.println();

            wave = 0;

            // New shot
            // Set Weights
            System.out.println("Test shot");
            System.out.printf(Locale.US, "%02d) ", wave);
            rna.setWeights(FileNetwork + ".nnet", rndWeights[wave]);
            for(int i=0; i<rndWeights[wave].length; i++)
                System.out.printf(Locale.US, "%020.17f ", rndWeights[wave][i]);
            System.out.println();
            // Test input new values weights
            double[] outputNewWeight = rna.Test(FileNetwork + ".nnet", new double[]{targetX/100});
            angle = (float) outputNewWeight[0];
            power = (float) outputNewWeight[1] * 100;

            System.out.println(wave + ") Input: " + Arrays.toString(new double[]{ inObjDown/100 }) + " Output: " + Arrays.toString(outputNewWeight));
            System.out.println();
            //double[] inputs = new double[]{ (inObjDown/100) };
            //double[] outputs = new double[]{ angle, (power/100) };

            trainingSet1.clear();
            gen++;

            Shot(LauncherX, LauncherY, 10, weight, 0.2f);
            Gdx.app.exit();
        }
    }

    @Override
    public void create() {
        WIDTH = Gdx.graphics.getWidth();
        HEIGHT = Gdx.graphics.getHeight();

        box2DCamera = new OrthographicCamera();

        //if(WIDTH == 1123) {
        if(WIDTH == 1350) {
            // Desktop
            box2DCamera.setToOrtho(false, (WIDTH) / (PPM / 3), (HEIGHT) / (PPM / 3) );
            box2DCamera.position.set(70, 25, 0);

            pathDataSet = "NeurophProject_Bang/Training Sets/";
            pathNetwork = "NeurophProject_Bang/Neural Networks/";

            targetX = 90;
        }else{
            // Smartphone
            box2DCamera.setToOrtho(false, (WIDTH) / (PPM) + 10, (HEIGHT) / (PPM) + 10 );
            box2DCamera.position.set(PPM*1.25f, PPM / 2 + 5, 0);

            pathDataSet = "/data/data/com.bang/files/";
            pathNetwork = "/data/data/com.bang/files/";

            targetX = 75;
        }

        rna = new NeurophStudio(FileDataset, FileNetwork, pathDataSet, pathNetwork);
        nnu = new NetworkUtils();

        box2DCamera.update();
        world = new World(new Vector2(0,-9.8f),true);
        debugRenderer = new Box2DDebugRenderer();

        batch = new SpriteBatch();

        font1 = new BitmapFont(Gdx.files.internal("fonts/verdana20.fnt"));
        font1.setColor(Color.WHITE);
        font1.getData().setScale(1f, 1f);

        font2 = new BitmapFont(Gdx.files.internal("fonts/verdana10.fnt"));
        font2.setColor(Color.WHITE);
        font2.getData().setScale(1f, 1f);

        // Clear NeuronNetwork and Dataset
        new NetworkUtils().DeleteFileNN(pathDataSet + FileDataset);
        new NetworkUtils().DeleteFileNN(pathNetwork + FileNetwork + ".nnet");

        // Create Neural Network
        rna.CreateMLP(layers, FileNetwork + ".nnet");

        // Objects
        bodyTarget(1.0f,1.0f, targetX,5.0f, BodyDef.BodyType.StaticBody);
        BodyTower(2, LauncherY, LauncherX, LauncherY);
        BodyLauncher(1, 2, LauncherX, LauncherY, angle);
        BodyBase(LauncherX, LauncherY, 2);
        BodyGround(WIDTH, 0, 0, 0, true);
        Rotate(LauncherX, LauncherY, 4, angle);

        // Defaults weights ramdoms
        rndWeights = RamdomWeights(waveTotal,26);

        // Start Shots
        //double w[] = new double[]{-0.993506908416748, -0.1561880111694336, 0.591668963432312, 0.11798298358917236, -0.15057921409606934, 0.29571568965911865, -0.7706863880157471, -0.3437129259109497, 0.6637395620346069, -0.20272040367126465, -0.0038902759552001953, 0.9248113632202148, 0.23871541023254395, -0.15542709827423096, 0.7762361764907837, -0.46198081970214844, 0.8833752870559692, -0.12744832038879395, 0.4875059127807617, 0.0766441822052002, -0.4453543424606323, -0.6718043088912964, -0.9585974216461182, 0.7754673957824707, -0.9345780611038208, 0.7372944355010986};
        //rna.setWeights(FileNetwork + ".nnet", w);
        rna.setWeights(FileNetwork + ".nnet", rndWeights[0]);

        double[] outputNewWeight = rna.Test(FileNetwork + ".nnet", new double[]{(targetX/100)});
        angle = (float) outputNewWeight[0];
        power = (float) outputNewWeight[1] * 100;
        Shot(LauncherX, LauncherY, power, weight, angle);

        System.out.println("Ramdom Weights");
        for(int i=0; i< rndWeights.length; i++) {
            System.out.printf(Locale.US, "%02d)", i);
            for (int j = 0; j < rndWeights[i].length; j++)
                System.out.printf(Locale.US, " %20.17f", rndWeights[i][j]);
            System.out.println();
        }
        System.out.println();
        System.out.println("Shots generation " + gen);
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
        font1.dispose();
        font2.dispose();
    }

    private double[][] RamdomWeights(int rows, int cols){
        double[][] weights = new double[rows][cols];

        for(int i=0; i<rows; i++)
            for(int j=0; j<cols; j++)
                weights[i][j] = nnu.RamdomValues(-1.0000000000f, 1.0000000000f);

        return weights;
    }

    private double[][] CloneWeights(int rows, int cols, double[][] rowWeights, double mut){
        double[][] weights = new double[rows][cols];

        for(int i=0; i<rows; i++) {
            for (int j=0; j<cols; j++) {
                //if(i==0){
                //    weights[i][j] = rowWeights[0][j];
                //}else{
                    weights[i][j] = rowWeights[0][j];//nnu.RamdomValues(-1.0000000000f, 1.0000000000f);
                //}
            }
        }

        /*
        for(int i=0; i<rows; i++) {
            int p = nnu.RamdomValuesInt(rowWeights.length);
            for (int j=0; j<cols; j++) {
                if (Math.random() > mut) {
                    weights[i][j] = rowWeights[p][j];
                }else{
                    weights[i][j] = rowWeights[p][j];//nnu.RamdomValues(-1.0000000000f, 1.0000000000f);
                }
            }
        }
        */
        return weights;
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

    private void Shot(float x, float y, float power, float weight, float ang){

        if(wave <= waveTotal) {
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
    }

    private void PanelInfo(){
        batch.begin();
        font1.draw(batch, "Angle: " + angle, 10, HEIGHT - 10);
        font1.draw(batch, "Power: " + power, 10, HEIGHT - 40);
        font1.draw(batch, "Target: " + targetX, 10, HEIGHT - 70);
        font1.draw(batch, "Status: " + status, 10, HEIGHT - 100);
        font1.draw(batch, "Wave: " + (wave) + "/" + waveTotal, 10, HEIGHT - 130);
        font1.draw(batch, "Generation: " + gen, 10, HEIGHT - 160);

        for(int i=0; i<trainingSet1.getRows().size(); i++){
            double maxObj = trainingSet1.getRows().get(i).getInput()[0];
            //double maxVal = MaxValue(trainingSet1, (targetX/100) );
            double maxVal = rna.MinValueSpace(trainingSet1, (targetX/100)).getInput()[0];

            if( maxVal == maxObj)
                font2.setColor(Color.RED);
            else
                font2.setColor(Color.WHITE);

            font2.draw(batch, String.format(Locale.US,"%02d) %020.17f, %020.17f, %020.17f",
                    i,
                    trainingSet1.getRows().get(i).getInput()[0],
                    trainingSet1.getRows().get(i).getDesiredOutput()[0],
                    trainingSet1.getRows().get(i).getDesiredOutput()[1]),
                    650, (HEIGHT - 10 - (i)*20)
            );
        }

        batch.end();
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

    private void bodyTarget(float w, float h,float x, float y, BodyDef.BodyType bodytype){
        BodyDef bodyTargetDef = new BodyDef();
        bodyTargetDef.type = bodytype;
        bodyTargetDef.position.set(x, (y*2));

        bodyTarget = world.createBody(bodyTargetDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(w, h);

        FixtureDef fixtureBoxDef = new FixtureDef();
        fixtureBoxDef.shape = shape;
        fixtureBoxDef.density = 10;

        Fixture fixtureBox = bodyTarget.createFixture(fixtureBoxDef);
        bodyTarget.setUserData("alvo");

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

    public double MaxValue(DataSet trainingSet, float limit){
        double row = 0;
        double max = 0;

        for(int i=0; i<trainingSet.getRows().size(); i++){
            double col = trainingSet.getRows().get(i).getInput()[0];
            if(col > max && col <= limit){
                max = col;
                row = trainingSet.getRows().get(i).getInput()[0];
            }
        }

        return row;
    }
}
