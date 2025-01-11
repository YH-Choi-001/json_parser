# json_parser  
## A powerful tool to serialize and deserialize structured data.  
  
Title: JSON Parser  
Brief: A library that facilitates data exchange between java programs and json files.  
Author: YH Choi  
Build: This is a library under `package personal.yhchoi.java.lib.json_parser;`. See details below for how to use.  
  
### How to Use  
  
#### Read json  
import statement:  
```
import personal.yhchoi.java.lib.json_parser.*;                // for using JsonParser  
```  
  
constructor:  
`JsonParser parser = new JsonParser(new File("./testing.txt"));`  
or  
`JsonParser parser = new JsonParser(" { "Name": "David", "Age": 65 } ");`  
  
read:  
```
try {
    try {
        parser.parse();
        JsonValue rootValue = parser.getRootValue();
    } catch (JsonFormatException e) {
        e.printStackTrace();
    }
} catch (FileNotFoundException e) {
    e.printStackTrace();
}
```
  
#### Write json  
import statement:  
```
import personal.yhchoi.java.lib.json_parser.*;                // for using JsonParser  
```  
  
write:  
```
// Create a root value. Can be either object or array.
JsonValue rootObject = new JsonObject();
// or
JsonValue rootArray = new JsonArray();

// Create JSON values under the root value.
rootObject.setValue("name", new JsonString("Chris Wong"));
rootObject.setValue("age", new JsonNum(18.5));
rootObject.setValue("isSingle", new JsonBool(true));
rootObject.setValue("father", new JsonNull());
rootObject.setValue("mother", new JsonNull());
JsonString name = (JsonString)rootObject.getValue("name");      // get the value from key "name" and cast it into JsonString type
rootObject.removeValue("isSingle");                             // remove the key-value pair of key "isSingle"
int mapSize = rootObject.size();                                // get the number of key-value pairs in the object
boolean heightRecorded = rootObject.containsKey("height");      // checks if the object contains a specific key
Set<String> keySet = rootObject.keySet();                       // get the complete set of keys of the object

rootArray.addValue(new JsonString("Andy"));         // append "Andy" to end of array
rootArray.addValue(new JsonString("Bond"));         // append "Bond" to end of array
rootArray.addValue(new JsonString("Chloe"));        // append "Chloe" to end of array
rootArray.addValue(new JsonString("Daisy"));        // append "Daisy" to end of array
rootArray.addValue(0, new JsonNum(4));              // insert 4 before index 0 of array
rootArray.addValue(new JsonBool(false));            // append false to end of array
rootArray.addValue(new JsonNull());                 // append null to end of array
JsonNum number = (JsonNum)rootArray.getValue(0);    // get the value at index 0 and cast it into JsonNum type
rootArray.removeValue(5);                           // remove value at index 5
int arrayLength = rootArray.size();                 // get the array length

// Convert JSON values into a JSON file.
String jsonString = rootArray.toString();
// OutputStream out = new OutputStream(new File("output.txt"));
// out.print(jsonString);
```
  
#### [More about this library](https://YH-Choi-001.github.io/json_parser/)