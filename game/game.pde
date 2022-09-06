//Daniel Morgan 2021-06-23
//This is the main 'game' of the project which includes a story and sandbox mode.
//The story mode loads levels stored in data/scenes and plays hard coded story telling instructions for 3 levels (levelcontroller.pde)
//The sandbox mode allows you to load levels created with the level creator tool by pressing 'l'

//baron berkely's best bounty hunting hierarchy for menacing magical creatures and contaminant
//uses pieces of code from the processing reference at processing.org/reference

//CONTROLS
//Portal:
//the portal is used to move entities like chickencorns
//you can hold left click while hovering over an entity to hide it
//you can hold right click to make the hiden entities reappear at the mouse position
//Push Blocks
//you can push 'push blocks' out of the way by walking into it in the direction you want to push it
//push blocks are grey with a black arrow indicator above them
//Movement:
// w/up arrow: player forward
// s/down arrow: player down
// d/right arrow: player right
// a/left arrow: player left
//Debugging:
// g: tile grid
// k: area grid

//STORY
boolean ingame = false;
boolean story = false;

//PERFORMANCE
//if you are having issues with performance, you can set the following to a HIGHER value (recommended 2) or use in game presets
float quality = 2;

//RESOLUTION
//By default the screen is 1920 by 1080, if this is too big you can adjust size() below (maintain 1920,1080 aspect ratio)

//VARIABLE NAMING
//variable prefix 'i' stands for import
//variable prefix 'ex' stands for export
//variable prefix 'd' stands for data
//variable prefix 'j' stands for json
//variable prefix 'temp' stands for temporary (used for changing length of array etc.)

//if a variable has an underscore (it generally will have a long name) it is an important variable
//for example camera_position controls where EVERYTHING is drawn or draw_canvas_layer() draws EVERYTHING

//camera globals
PVector camera_position = new PVector(0, 0);
float camera_distance = 2;

//player global
player drudge;
baron berkely;

//graphics globals
PGraphics canvas;
PGraphics litscreen;
PGraphics postprocessing;
PShader fragshader;
PImage perlin; //perlin noise
float sratio; //screen ratio width/height
float time = 200;
JSONArray lights = new JSONArray();

//layer and scene globals
String layer = "base";
String[] layers = {
    "base",
    "decorations",
    "areas"
};
JSONObject dareas;
JSONObject dtiles;
JSONObject ddecorations;
JSONObject denemys;
JSONArray tilemanifest;
JSONArray areamanifest;
JSONArray decorationmanifest;
JSONArray enemymanifest;
PImage[] tileimages = new PImage[0]; //a list of previously loaded tile images
String[] dtileimages = new String[0]; //manifest for tileimages
PImage[] decorationimages = new PImage[0]; //a list of previously loaded decoration images
String[] ddecorationimages = new String[0]; //manifest for decorationimages
JSONArray[] allmanifest = new JSONArray[4];
String menutext = "";

//sprite globals
tile[][] tiles; //tiles array
enemy[] enemies; //enemies
portal pportal; //player portal

void setup() {
    size(1920, 1080, P2D);
    noSmooth();

    sratio = ((float) width / (float) quality) / ((float) height / (float) quality); //screen ratio with quality
    fragshader = loadShader("fragshader.glsl");
    canvas = createGraphics(width, height, P2D);
    postprocessing = createGraphics(width, height, P2D);
    //postprocessing.smooth(8);
    litscreen = createGraphics(int(width / quality), int(height / quality), P2D);
    //litscreen.smooth(8);

    perlin = loadImage("textures/perlin.png");

    File dir = new File(dataPath("scenes"));
    files = dir.listFiles(); //load json levels list from scenes

    drudge = new player(128 * 3, 128 * 3);
    enemies = new enemy[0];
    pportal = new portal(0, 0);
    berkely = new baron(0, 0);

    //load tile json
    load_json();

    //load menu ui
    load_menu_assets();

    tiles = new tile[30][30];
    for (int i = 0; i < tiles.length; i++) {
        for (int j = 0; j < tiles[i].length; j++) {
            if (floor(random(0, 2)) == 1) {
                tiles[i][j] = new tile(i, j, "grass");
            } else {
                tiles[i][j] = new tile(i, j, "plains");
            }
        }
    }
}

void draw() {
    //show framerate in window bar
    surface.setTitle("FPS: " + frameRate);
    if (ingamemenu && !isdying) { //if in the menu while in a game
        drawingamemenu();
    } else if (story && !ingame && !isdying) { //if the story is being run
        play_story(); //(levelcontroller.pde)
    }
    if (ingame && !ingamemenu) { //if the game is actually running

        //update player coords if not dying
        if (!isdying) drudge.update(); //(player.pde)

        //draw
        draw_canvas_layer(); //everything (graphicscontroller.pde)
        draw_litscreen_layer(); //all things that interact with light (graphicscontroller.pde)
        draw_postprocessing_layer(); //fog/special effects (graphicscontroller.pde)

        //update portal and enemies
        if (!isdying) pportal.update(); //(player.pde)
        for (int i = 0; i < enemies.length; i++) {
            if (!isdying) enemies[i].update();
        }

        //handle lights and lighting
        handle_lights(); //(graphicscontroller.pde)
        lights = new JSONArray();
        //
        pushMatrix();
        scale(camera_distance, camera_distance);
        background(0);
        blendMode(NORMAL);
            image(canvas, 0, 0, width, height);
            blendMode(MULTIPLY);
        image(litscreen, 0, 0, width, height);
            blendMode(ADD);
            image(postprocessing, 0, 0, width, height);
        blendMode(NORMAL);
        popMatrix();

        //hard coded win conditions and mechanics
        if (!isdying) do_hard_coded(); //(levelcontroller.pde)

        //draw ui as top layer
        if (!isdying) draw_ui(); //(uicontroller.pde)

        //if the player should be playing their death animation
        if (isdying) displaydeath();
    } else if (!story && !ingame) {
        draw_menu(); //(uicontroller.pde)
    }
}
