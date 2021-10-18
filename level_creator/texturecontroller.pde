void load_json() {
  //load all tile json
  tilemanifest = loadJSONArray("tiles/manifest.json");
  println("Loaded tiles tilemanifest");
  dtiles = new JSONObject();
  for (int i = 0 ; i < tilemanifest.size(); i++) {
    JSONObject dtile = loadJSONObject("tiles/"+tilemanifest.getString(i)+".json");
    println("\t-Loaded " + tilemanifest.getString(i) + ".json");
    dtiles.setJSONObject(tilemanifest.getString(i),dtile);
  }
  //load all decoration json
  decorationmanifest = loadJSONArray("decorations/manifest.json");
  println("Loaded tiles decorations manifest");
  ddecorations = new JSONObject();
  for (int i = 0 ; i < decorationmanifest.size(); i++) {
    println(decorationmanifest.getString(i));
    JSONObject ddecoration = loadJSONObject("decorations/"+decorationmanifest.getString(i)+".json");
    println("\t-Loaded " + decorationmanifest.getString(i) + ".json");
    ddecorations.setJSONObject(decorationmanifest.getString(i),ddecoration);
  }
  //load all area json
  areamanifest = loadJSONArray("areas/manifest.json");
  println("Loaded areas areamanifest");
  dareas = new JSONObject();
  for (int i = 0 ; i < areamanifest.size(); i++) {
    JSONObject darea = loadJSONObject("areas/"+areamanifest.getString(i)+".json");
    println("\t-Loaded " + areamanifest.getString(i) + ".json");
    dareas.setJSONObject(areamanifest.getString(i),darea);
  }
  //load enemies
  enemymanifest = loadJSONArray("enemies/manifest.json");
  println("Loaded enemy manifest");
  denemys = new JSONObject();
  for (int i = 0 ; i < enemymanifest.size(); i++) {
    JSONObject denemy = loadJSONObject("enemies/"+enemymanifest.getString(i)+".json");
    println("\t-Loaded " + enemymanifest.getString(i) + ".json");
    denemys.setJSONObject(enemymanifest.getString(i),denemy);
  }
  allmanifest[0] = tilemanifest;
  allmanifest[1] = decorationmanifest;
  allmanifest[2] = areamanifest;
  allmanifest[3] = enemymanifest;
}
