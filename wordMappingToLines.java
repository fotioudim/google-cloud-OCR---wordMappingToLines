// jsonString is the String value returned from the Google Vision OCR API and the words are given in random order
// We map the strings in lines, append line breaks and conclude the result to a html string
public String textLineMapping(String jsonString) throws IOException, EException {

JsonReader reader = Json.createReader(new StringReader(jsonString));
jsonObject = reader.readObject().getJsonArray("responses").getJsonObject(0); // in this example we assume we have one response, otherwise iterate through Json objects

ArrayList<WordAnnotation> wordAnottations = new ArrayList<WordAnnotation>(); 
JsonArray jsonAnnotations = jsonObject.getJsonArray("textAnnotations"); // we need only the information contained in the "textAnnotation" array inside the Json object
if (jsonAnnotations==null)
	return "No text detected";

for(JsonValue jsonAnnotation : jsonAnnotations) {
	
	if ( ((JsonObject)jsonAnnotation).containsKey("locale") ) // skip locale
		continue;
	try {	
		JsonArray vertices = ((JsonObject)jsonAnnotation).getJsonObject("boundingPoly").getJsonArray("vertices");
		if (!calculated) { //all words will have the same line gradient, so we  calculate it only once
			float dy = (((JsonObject)vertices.get(1)).getInt("y", 0) - ((JsonObject)vertices.get(0)).getInt("y", 0));
			float dx = (((JsonObject)vertices.get(1)).getInt("x") - ((JsonObject)vertices.get(0)).getInt("x", 0));
			gradient=(dy/dx);
			calculated=true;
		}
		wordAnottations.add(new WordAnnotation(
				((JsonObject)vertices.get(0)).getInt("x", 0),
				((JsonObject)vertices.get(0)).getInt("y", 0), 
				((JsonObject)vertices.get(3)).getInt("y"),
				((JsonObject)jsonAnnotation).getString("description")));
	}
	catch (NullPointerException ex){
		return "No text detected";
	}
	 catch(ArithmeticException ex){
		return "No text detected";
	}
}
Collections.sort(wordAnottations, new Sort());
StringBuilder whole = new StringBuilder();
for (int i = 0; i < wordAnottations.size(); i++) {

	// if the top left y-coordinate of the next word is bigger than the current bottom left y-coordinate, we detect a line change
	if(i < wordAnottations.size() - 1 
					&& (wordAnottations.get(i).yinaxis() >= wordAnottations.get(i+1).yinaxis() + ((2*wordAnottations.get(i+1).height())/3)
					|| wordAnottations.get(i).yinaxis() <= wordAnottations.get(i+1).yinaxis() - ((2*wordAnottations.get(i+1).height())/3))) 
		whole.append(wordAnottations.get(i).description).append("<br>");
	else
		whole.append(wordAnottations.get(i).description).append(" ");	
 }

return whole.toString();



private class Sort implements Comparator<WordAnnotation> {
	public int compare(Wordnnotation o1, WordAnnotation o2) {
		// we compare the y values in y axis for each upper line of a word
		if(o1.yinaxis() >= o2.yinaxis() + (2*o2.height())/3)// not same line, o1 lower than o2
			return 1;
		else if (o1.yinaxis() <= o2.yinaxis() - (2*o2.height())/3)// not same line, o2 lower than o1
			return -1;
		else  if (o1.x - o2.x > 0)// same line check who is left
			return 1; // o2 is to the left of o1
		else
			return -1;// o1 is to the left of o2			
	 }
}
	
private class WordAnnotation {
	int x;	//top left corner(x-coordinate)
			
	//y-coordinates are the distance from the upper side of an image
	int y1;	//top left corner(y-coordinate)
	int y2;	//bottom left corner(y-coordinate)
	
	String description;
	
	 // Constructor 
	public WordAnnotation(int x, int y1, int y2, String description) 
	{ 
		this.x = x; 
		this.y1 = y1; 
		this.y2 = y2; 
		this.description = description; 
	} 
	public int height()
	{
		return y2-y1;
	}
	public float yinaxis() // line equation: y-y1 = l * (x - x1), For x=0 (y axis) : y= y1 - l * x1
	{
		return this.y1-gradient*this.x;
	}
	public String toString() 
	{ 
		return "Description: " + this.description + "\nx: " + this.x + ", y1: " + this.y1 + ", line height: " + (this.y2- this.y1);
	} 
}	
