/**
 * 
 *  JsonParser.java - A parser to parse json files and load them into java as various objects.
 *  Copyright (C) 2024 YH Choi
 *
 *  This program is licensed under BSD 3-Clause License.
 *  See LICENSE.txt for details.
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 *  FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *  CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package personal.yhchoi.java.lib.json_parser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.Scanner;

/**
 * A parser for json files.
 *
 * @author Yui Hei Choi
 * @version 2024.11.18
 */
public class JsonParser
{
    private JsonValue rootValue;            // the root value holding all the data
    private JsonValue actualRootValue;      // the actual root value
    
    private JsonValue processingValue;      // the value currently being processed
    private boolean insideString;           // whether the parser is inside a string
    private boolean inEscape;               // whether the parser is inside an escape
    private boolean inUnicode;              // whether the parser is inside a unicode character
    private String stringBuf;               // the buffer to hold a string
    private String unicodeBuf;              // the buffer to hold unicode characters in a string
    private boolean inKey;                  // whether the parser is in a key or a value
    private String objectKeyBuf;            // the buffer to hold a key in a json object
    private String symbolBuf;               // the buffer to hold a symbol (e.g. true, false, null, 3.14, 2.71828, 18)
    private boolean symbolCompletelyRead;   // whether the parser completely read the symbol or not
    
    private int currentLine;                // the line being processed
    private int currentColumn;              // the column being processed
    private boolean parseError;             // whether an error has occured when parsing the json

    /**
     * Constructor for objects of class JsonParser.
     * 
     * @param file the json file to be parsed
     */
    public JsonParser(File file)
    {
        commonInit();
        if (!file.exists()) {
            return;
        }
        try {
            parseError = (parseByChar("[\n") != 0);
            String contents = "";
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                contents += scanner.nextLine() + '\n';
                if (contents.length() >= 1024) {
                    parseError = (parseByChar(contents) != 0);
                    if (parseError) {
                        return;
                    }
                    contents = "";
                }
            }
            parseError = (parseByChar(contents) != 0);
            if (parseError) {
                return;
            }
            parseError = (parseByChar("\n]") != 0);
            actualRootValue = getActualRootValue();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Constructor for objects of class JsonParser.
     * 
     * @param contents the contents to be parsed
     */
    public JsonParser(String contents)
    {
        commonInit();
        parseError = (parseByChar("[\n") != 0);
        parseError = (parseByChar(contents) != 0);
        if (parseError) {
            return;
        }
        parseError = (parseByChar("\n]") != 0);
        actualRootValue = getActualRootValue();
    }
    
    /**
     * Initializes fields.
     */
    private void commonInit()
    {
        rootValue = null;
        actualRootValue = null;
        processingValue = null;
        insideString = false;
        inEscape = false;
        inUnicode = false;
        stringBuf = null;
        unicodeBuf = "";
        inKey = false;
        objectKeyBuf = "";
        symbolBuf = "";
        symbolCompletelyRead = false;
        currentLine = 1;
        currentColumn = 1;
        parseError = false;
    }
    
    /**
     * Redirects an error message to the appropriate output.
     * 
     * @param msg the error message
     */
    private void errMsg(String msg)
    {
        System.err.println(msg);
    }
    
    /**
     * Parse the string input.
     * 
     * @param the string
     * @return the index of the input that failed, or 0 if successful
     */
    private int parseByChar(String string)
    {
        if (string == null) {
            return -1;
        }
        for (int i = 0; i < string.length(); i++) {
            final char c = string.charAt(i);
            if (c == '\n') {
                currentLine++;
                currentColumn = 1;
            } else {
                currentColumn++;
            }
            if (insideString) {
                if (inUnicode) {
                    if (isHexDigit(c)) {
                        unicodeBuf += c;
                        continue;
                    } else {
                        // if (unicodeBuf.length() != 2) {
                        //     errMsg("Unicode at " + i + " is not exactly 4 hex digits.");
                        //     return i;
                        // }
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        int unicode = 0;
                        try {
                            unicode = Integer.parseInt(unicodeBuf, 16);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            errMsg("Unicode at " + i + " with string content \"" + unicodeBuf + "\" could not be converted to HEX.");
                            return i;
                        }
                        baos.write(unicode);
                        String utfEncoded = new String(baos.toByteArray(), Charset.forName("UTF-8"));
                        stringBuf += utfEncoded;
                        unicodeBuf = "";
                        inUnicode = false;
                    }
                }
                if (inEscape) {
                    switch (c) {
                        case '\"':
                        case '\\':
                        case '/':
                            stringBuf += c;
                            break;
                        case 'b':
                            stringBuf += '\b';
                            break;
                        case 'f':
                            stringBuf += '\f';
                            break;
                        case 'n':
                            stringBuf += '\n';
                            break;
                        case 'r':
                            stringBuf += '\r';
                            break;
                        case 't':
                            stringBuf += '\t';
                            break;
                        case 'u':
                            inUnicode = true;
                            unicodeBuf = "";
                            break;
                        default:
                            errMsg("\\" + c + " at " + i + " could not be recognized as a valid backslash character.");
                            return i;
                    }
                    inEscape = false;
                } else if (c == '\\') {
                    inEscape = true;
                } else if (c == '\"') {
                    // wrap up the buf
                    if (inKey) {
                        objectKeyBuf = stringBuf;
                    } else {
                        addNewValueToProcessingValue(new JsonString(stringBuf));
                    }
                    stringBuf = null;
                    insideString = false;
                } else {
                    stringBuf += c;
                }
            } else {
                switch (c) {
                    case '\"':
                        insideString = true;
                        stringBuf = "";
                        break;
                    case '{':
                        // enter a new object
                        if (inKey) {
                            errMsg("Object being declared as a key at " + i + ".");
                            return i;
                        }
                        {
                            JsonObject newObj = new JsonObject();
                            addNewValueToProcessingValue(newObj);
                            processingValue = newObj;
                            if (rootValue == null) {
                                rootValue = processingValue;
                            }
                            
                            // parser has to treat next string as a key in an object
                            inKey = true;
                        }
                        break;
                    case '[':
                        // enter a new array
                        if (inKey) {
                            errMsg("Array being declared as a key at " + i + ".");
                            return i;
                        }
                        {
                            JsonArray newArr = new JsonArray();
                            addNewValueToProcessingValue(newArr);
                            processingValue = newArr;
                            if (rootValue == null) {
                                rootValue = processingValue;
                            }
                        }
                        break;
                    case ']':
                        // exit this array
                        if (!(processingValue instanceof JsonArray)) {
                            errMsg("Symbol \']\' is unexpected at " + i + " because this layer is not recognized as an array.");
                            return i;
                        }
                        // wrap up the buf for array
                        if (!wrapUpSymbolBuf(i)) {
                            return i;
                        }
                        {
                            JsonValue parent = processingValue.getParent();
                            if ((processingValue == rootValue) || (parent == null)) {
                                // we are exiting the outermost structure in the json file
                                processingValue = null;
                                return 0;
                            }
                            // exit this array i.e. switch processing value back to parent
                            processingValue = parent;
                        }
                        break;
                    case '}':
                        // exit this object
                        if (!(processingValue instanceof JsonObject)) {
                            errMsg("Symbol \'}\' is unexpected at " + i + " because this layer is not recognized as an object.");
                            return i;
                        }
                        // wrap up the buf for object
                        if (!wrapUpSymbolBuf(i)) {
                            return i;
                        }
                        {
                            JsonValue parent = processingValue.getParent();
                            if ((processingValue == rootValue) || (parent == null)) {
                                // we are exiting the outermost structure in the json file
                                processingValue = null;
                                return 0;
                            }
                            // exit this object i.e. switch processing value back to parent
                            processingValue = parent;
                        }
                        inKey = false;
                        break;
                    case ',':
                        if (processingValue instanceof JsonArray) {
                            // wrap up the buf for array
                            if (!wrapUpSymbolBuf(i)) {
                                return i;
                            }
                        } else if (processingValue instanceof JsonObject) {
                            if (!inKey) {
                                // wrap up the buf for object
                                if (!wrapUpSymbolBuf(i)) {
                                    return i;
                                }
                                
                                // parser has to treat next string as a key in an object
                                inKey = true;
                            } else {
                                errMsg("Symbol \',\' is unexpected at " + i + " because the key is not given.");
                                return i;
                            }
                        } else {
                            errMsg("Symbol \',\' is unexpected at " + i + " because this layer is neither array nor object.");
                            return i;
                        }
                        break;
                    case ':':
                        if (!(processingValue instanceof JsonObject)) {
                            errMsg("The symbol \':\' appeared at " + i + " is not inside an object.");
                            return i;
                        }
                        if (!inKey) {
                            errMsg("The symbol \':\' appeared at " + i + " could not be recognized to separate key and value in an object.");
                            return i;
                        }
                        inKey = false;
                        break;
                    case ' ':
                    case '\t':
                    case '\r':
                    case '\n':
                        if (!symbolBuf.isEmpty()) {
                            symbolCompletelyRead = true;
                        }
                        break;
                    default:
                        if (symbolBuf.isEmpty()) {
                            symbolCompletelyRead = false;
                        }
                        if (symbolCompletelyRead) {
                            errMsg("Unexpected symbol \'" + string.substring(i-100, i+1) + "\' at " + i + ".");
                            return i;
                        }
                        symbolBuf += c;
                        break;
                }
            }
        }
        return 0;
    }

    private static boolean isHexDigit(char c)
    {
        switch (c) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
                return true;
            default:
                return false;
        }
    }

    /**
     * Adds a new value to the current processing value.
     * 
     * @param newValue the new value to be added
     * @return true if new value is added to processing value, false if processing value is null or not one of the container types
     */
    private boolean addNewValueToProcessingValue(JsonValue newValue)
    {
        if (processingValue == null) {
            return false;
        }
        if (processingValue instanceof JsonArray) {
            ((JsonArray)processingValue).addValue(newValue);
        } else if (processingValue instanceof JsonObject) {
            ((JsonObject)processingValue).setValue(objectKeyBuf, newValue);
        } else {
            return false;
        }
        return true;
    }
    
    /**
     * Concludes the symbol held by symbolBuf, and attempts to convert it into one of the JsonValue types.
     * 
     * @param index the index of the character being processed
     * @return true if wrap up is successful, false otherwise
     */
    private boolean wrapUpSymbolBuf(int index)
    {
        if (symbolBuf.isEmpty()) {
            return true;
        }
        
        boolean toReturn = true;
        JsonValue newValue = null;
        
        if (symbolBuf.equals(JsonBool.TRUE_EXPRESSION)) {
            newValue = new JsonBool(true);
        } else if (symbolBuf.equals(JsonBool.FALSE_EXPRESSION)) {
            newValue = new JsonBool(false);
        } else if (symbolBuf.equals(JsonNull.NULL_EXPRESSION)) {
            newValue = new JsonNull();
        } else {
            try {
                double doubleVal = Double.parseDouble(symbolBuf);
                newValue = new JsonNum(doubleVal);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                errMsg("Symbol \"" + symbolBuf + "\" is unrecognized at " + index + " (neither true, false, null nor number).");
                toReturn = false;
            }
        }
        
        if (newValue != null) {
            if (!addNewValueToProcessingValue(newValue)) {
                errMsg("control flow error: JsonParser.wrapUpSymbolBuf() invoked when processingValue is not a container.");
                toReturn = false;
            }
        }
        
        symbolBuf = "";
        return toReturn;
    }

    /**
     * Gets whether an error has occured or not when parsing the json.
     * 
     * @return true if an error has occured, false otherwise
     */
    public boolean isParseError()
    {
        return parseError;
    }

    /**
     * Gets the line where an error has occured.
     * 
     * @return the line (starting from 1) where an error has occured, or 0 if no error has occured
     */
    public int getErrorLine()
    {
        return parseError ? (currentLine - 1) : 0;
    }

    /**
     * Gets the column where an error has occured.
     * 
     * @return the column (starting from 1) where an error has occured, or 0 if no error has occured
     */
    public int getErrorColumn()
    {
        return parseError ? currentColumn : 0;
    }

    /**
     * Gets the root json value.
     * 
     * @return the root json value, or null if an error has occured
     */
    private JsonValue getActualRootValue()
    {
        if (parseError) {
            return null;
        }
        final JsonArray wrappingArray = (JsonArray)rootValue;
        if (wrappingArray.size() > 0) {
            return wrappingArray.getValue(0).getDuplicate();
        } else {
            return null;
        }
    }
    
    /**
     * Gets the root json value.
     * 
     * @return the root json value, or null if an error has occured
     */
    public JsonValue getRootValue()
    {
        return actualRootValue;
    }
}
