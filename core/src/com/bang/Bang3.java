package com.bang;

import com.ag.AlgoritmoGenetico;
import com.ag.Shot;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
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
import org.neuroph.util.TransferFunctionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Bang3 extends ApplicationAdapter{

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
    int waveTotal = 5;
    float best = 0.1f;
    int wave = 0;
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

                TestWeights(bodyObj.getPosition().x, targetX, isCollideTarget);
                wave++;
            }
        }

        PanelInfo();

        world.step(Gdx.graphics.getDeltaTime(), VELOCITY_ITERATIONS, POSITION_ITERATIONS);
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

        //Gdx.input.setInputProcessor(this);

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

        // Start Shtos
        //double[] testW = new double[]{0.16596972942352295, -0.6451693773269653, 0.3397611379623413, -0.5836633443832397, -0.2681492567062378, 0.407647967338562, -0.7288495302200317, -0.901965856552124, 0.9155457019805908, 0.15820980072021484, 0.3644449710845947, -0.9228442907333374, -0.5861966609954834, 0.3753688335418701, 0.49698948860168457, 0.5794743299484253, 0.05732583999633789, 0.20679700374603271, 0.854007363319397, 0.8561968803405762, 0.8100053071975708, 0.1521681547164917, -0.9717065095901489, -0.9068574905395508, -0.9560844898223877, -0.13161170482635498};
        rna.setWeights(FileNetwork + ".nnet", rndWeights[wave]);
        //rna.setWeights(FileNetwork + ".nnet", testW);

        double[] outputNewWeight = rna.Test(FileNetwork + ".nnet", new double[]{targetX/100});
        angle = (float) outputNewWeight[0];
        power = (float) outputNewWeight[1] * 100;
        //System.out.println("Target:                      " + (targetX1/100) );
        //System.out.println("First Shot TestW:           " + Arrays.toString(rndWeights[wave]));
        //System.out.println("First Shot outputNewWeight: " + Arrays.toString(outputNewWeight));
        Shot(LauncherX, LauncherY, power, weight, angle);

        /*
        trainingSet1.addRow(new DataSetRow(new double[]{ 0.6788194179534912 }, new double[]{ 000.64899182, 000.16062449 }));
        trainingSet1.addRow(new DataSetRow(new double[]{ 0.5454935431480408 }, new double[]{ 001.26450837, 000.31918386 }));
        trainingSet1.addRow(new DataSetRow(new double[]{ 3.324920892715454 }, new double[]{ 001.05931032, 000.25987432 }));
        trainingSet1.addRow(new DataSetRow(new double[]{ 0.8014276027679443 }, new double[]{ 000.59274757, 000.08821141 }));
        trainingSet1.addRow(new DataSetRow(new double[]{ 2.0827553272247314 }, new double[]{ 000.21328776, 000.13846851 }));
        trainingSet1.addRow(new DataSetRow(new double[]{ 0.42206576466560364 }, new double[]{ 000.41088295, 000.23817520 }));
        trainingSet1.addRow(new DataSetRow(new double[]{ 3.6001546382904053 }, new double[]{ 000.86217695, 000.17242796 }));
        trainingSet1.addRow(new DataSetRow(new double[]{ 0.45471516251564026 }, new double[]{ 000.23241945, 000.15341789 }));
        trainingSet1.addRow(new DataSetRow(new double[]{ 0.889315128326416 }, new double[]{ 000.19333835, 000.10510767 }));
        trainingSet1.addRow(new DataSetRow(new double[]{ 0.9622412323951721 }, new double[]{ 001.10853362, 000.21803848 }));

        for(int i=0; i<trainingSet1.getRows().size(); i++){
            System.out.println(i + " " +
                    "Input: " + Arrays.toString(trainingSet1.getRows().get(i).getInput()) +
                    " Output: " + Arrays.toString(trainingSet1.getRows().get(i).getDesiredOutput())
            );
        }
        System.out.println();

        trainingSet2 = rna.Best(trainingSet1, Math.round(trainingSet1.getRows().size() * percentsel), 0.9);
        for(int i=0; i<trainingSet2.getRows().size(); i++){
            System.out.println(i + " " +
                    "Input: " + Arrays.toString(trainingSet2.getRows().get(i).getInput()) +
                    " Output: " + Arrays.toString(trainingSet2.getRows().get(i).getDesiredOutput())
            );
        }
        System.out.println();

        for(int i=0; i<trainingSet1.getRows().size(); i++){
            System.out.println(i + " " +
                    "Input: " + Arrays.toString(trainingSet1.getRows().get(i).getInput()) +
                    " Output: " + Arrays.toString(trainingSet1.getRows().get(i).getDesiredOutput())
            );
        }
        System.out.println();
        */
        //System.out.println(">>>>>>> " + nnu.RamdomValuesInt(10));
        //Gdx.app.exit();
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

            // New shot, Set Weights
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
            //double maxVal = MaxValue(trainingSet1, (targetX1/100) );
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
