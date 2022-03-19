class baron {
  PVector pos = new PVector(0,0);
  PImage[] textures = new PImage[2];
  int curframe = 0;
  baron(float x, float y) {
    pos = new PVector(x,y);
    textures[0] = loadImage("textures/characters/baron1.png");
    textures[1] = loadImage("textures/characters/baron2.png");
  }
  void show() {
    canvas.noStroke();
    canvas.image(textures[curframe],pos.x,pos.y);
    if (frameCount % 60 == 0) {
      curframe++;
      curframe = curframe%2;
    }
  }
  void changepos(float x, float y) {
    pos = new PVector(x, y);
  }
}
