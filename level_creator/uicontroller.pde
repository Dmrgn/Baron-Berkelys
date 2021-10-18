String selected = "grass";
boolean show_areas = true;
void searchtile(String _object) {
  String object = _object;
  boolean hasfound = false;
  for (int i = 0 ; i < layers.length; i++) {
    for (int j = 0 ; j < allmanifest[i].size(); j++) {
      boolean contains = false;
      if (allmanifest[i].getString(j).equals(object)) {//if the object is found
        layer = layers[i];
        selected = object;
        hasfound = true;
      }
    }
  }
  if (!hasfound) {
    String close = "";
    for (int i = 0 ; i < layers.length; i++) {
      for (int j = 0 ; j < allmanifest[i].size(); j++) {
        String current = allmanifest[i].getString(j);
        String currenttest = current;
        String objecttest = object;
        int commonchars = 0;
        for (int k = 0 ; k < object.length(); k++) {
          if (int(currenttest.indexOf(str(object.charAt(k)))) != -1) {
            currenttest = removecharat(int(currenttest.indexOf(str(object.charAt(k)))), currenttest);
            //k--;
            commonchars++;
          }
        }
        float similarity = (float)commonchars/(float)objecttest.length();
        if (similarity >= 0.75) {
          close += current + ", ";
        }
      }
    }
    infoBox("We couldnt find your search. Did you mean: \n" + close);
  }
}
String removecharat(int index, String input) {
  String newstring = "";
  for (int i = 0 ; i < input.length(); i++) {
    if (i != index) {
      newstring += input.charAt(i);
    }
  }
  return newstring;
}
String[] removestringfrom(String[] tempstrings_, int index) {
  String[] tempstrings = tempstrings_;
  ArrayList<String> tempremoved = new ArrayList<String>();
  for (int i = 0 ; i < tempstrings.length; i++) {
    tempremoved.add(tempstrings[i]);
  }
  for (int i = 0 ; i < tempstrings.length; i++) {
    if (i == index) {
      tempremoved.remove(i);
      i--;
    }
  }
  tempstrings = new String[tempremoved.size()];
  for (int i = 0 ; i < tempremoved.size(); i++) {
    tempstrings[i] = tempremoved.get(i);
    println(tempstrings[i]);
  }
  return tempstrings;
}
void keyPressed() {
  if (key == 'g') {
    println(key);
    grid = !grid;
  }
  if (key == 's') {
    savecurrent();
  }
  if (key == 'k') {
    show_areas = !show_areas;
  }
  if (key == 'l') {
    String path = getString("What is the file called? (don't include .json)");
    loadscene("scenes/"+path+".json");
  }
  if (keyCode == ENTER) {
    String response = getString("Search for object:");
    searchtile(response);
  }
}
