/**
 * 
 *  JsonParser.java - A parser to parse json files and load them into java as various objects.
 *  Copyright (C) 2024 - 2025 YH Choi
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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * A parser for json files.
 *
 * @author Yui Hei Choi
 * @version 2025.01.29
 */
public class JsonParser
{
    private JsonValue rootValue;            // the root value holding all the data
    private JsonValue actualRootValue;      // the actual root value
    
    private JsonValue processingContainer;      // the value currently being processed
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

    private String fileAbsPath;             // the json file absolute path
    private BufferedReader reader;          // the json file reader
    private String contents;                // the json file contents

    private boolean parsed;                 // whether the parser already parsed the file

    /**
     * Constructor for objects of class JsonParser.
     * 
     * @param file the json file to be parsed
     * @throws FileNotFoundException if file is not found
     */
    public JsonParser(File file) throws FileNotFoundException
    {
        commonInit();
        this.reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        fileAbsPath = file.getAbsolutePath();
    }
    
    /**
     * Constructor for objects of class JsonParser.
     * 
     * @param inStream the input stream of the json file to be parsed
     */
    public JsonParser(InputStream inStream)
    {
        commonInit();
        this.reader = new BufferedReader(new InputStreamReader(inStream));
    }
    
    /**
     * Constructor for objects of class JsonParser.
     * 
     * @param reader the reader of the json file to be parsed
     */
    public JsonParser(Reader reader)
    {
        commonInit();
        this.reader = new BufferedReader(reader);
    }
    
    /**
     * Constructor for objects of class JsonParser.
     * 
     * @param contents the contents to be parsed
     */
    public JsonParser(String contents)
    {
        commonInit();
        this.contents = contents;
    }
    
    /**
     * Initializes fields.
     */
    private void commonInit()
    {
        rootValue = null;
        actualRootValue = null;
        processingContainer = null;
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
        currentColumn = 0;
        fileAbsPath = null;
        reader = null;
        contents = null;
        parsed = false;
    }

    /**
     * Parses the entire file.
     * Could only be called once. <br>
     * To parse the same file multiple times,
     * create more <code>JsonParser</code> objects instead. <br>
     * 
     * @throws IOException thrown if an I/O error occurs
     * @throws JsonFormatException thrown when illegal json format is found
     */
    public final void parse() throws IOException, JsonFormatException
    {
        if (parsed) {
            return;
        }
        // Due to design flaws, the outermost datatype processed by parseByChar() must be JsonArray of JsonObject.
        // Hence, we wrap an outer JsonArray on the original json data.
        if (reader != null) {
            parsed = true;
            parseByChar("[\n");
            currentLine = 1;
            currentColumn = 0;
            String thisLine;
            try {
                while ((thisLine = reader.readLine()) != null) {
                    parseByChar(thisLine + "\n");
                }
            } finally {
                reader.close();
            }
            parseByChar("\n]");
            actualRootValue = getActualRootValue();
        } else if (contents != null) {
            parsed = true;
            parseByChar("[\n");
            parseByChar(contents);
            parseByChar("\n]");
            actualRootValue = getActualRootValue();
        } else {
            actualRootValue = null;
        }
    }

    /**
     * Gets the file path of the file reading by json parser.
     * 
     * @return the file path of the file reading by json parser, or <code>null</code> if a file is not used
     */
    private String getFilePath()
    {
        return fileAbsPath;
    }

    /**
     * Extracts the error line with squiggles and pointer to indicate the error.
     * 
     * @param str the json string
     * @param idx the index of error in <code>str</code>
     */
    private String errorWrapLine(String str, int idx)
    {
        int lineStartIdx = str.lastIndexOf("\n", idx) + 1;

        int lineEndIdx = str.indexOf("\n", idx);
        if (lineEndIdx == -1) {
            lineEndIdx = str.length();
        }

        final String entireLine = str.substring(lineStartIdx, lineEndIdx);

        String toReturn = entireLine + "\n";

        if (entireLine.isEmpty()) {
            return toReturn;
        }

        final int errorColumnIndex = idx - lineStartIdx;

        for (int i = 0; i < entireLine.length(); i++) {
            toReturn += ((i == errorColumnIndex) ? "!" : "~");
        }
        return toReturn;
    }
    
    /**
     * Parse the string input.
     * 
     * @param the string
     * @throws JsonFormatException thrown when illegal json format is found
     */
    private void parseByChar(String string) throws JsonFormatException
    {
        if (string == null) {
            throw new JsonFormatException(currentLine, currentColumn, getFilePath(),
                "Json content is null.");
        }
        for (int i = 0; i < string.length(); i++) {
            final char c = string.charAt(i);
            if (c == '\n') {
                currentLine++;
                currentColumn = 0;
            } else {
                currentColumn++;
            }
            // Checks if we are inside a string.
            // this string could be a key inside JsonObject or just a JsonString.
            if (insideString) {
                if (inUnicode) {
                    // unicodes should use exactly 4 hex digits
                    if (unicodeBuf.length() < 4) {
                        if (!isHexDigit(c)) {
                            throw new JsonFormatException(currentLine, currentColumn, getFilePath(),
                                "Unicode with string content \"\\u" + unicodeBuf + "\" contains invalid HEX character.\n" + errorWrapLine(string, i));
                        }
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
                            throw new JsonFormatException(currentLine, currentColumn, getFilePath(),
                                "Unicode with string content \"\\u" + unicodeBuf + "\" could not be converted to HEX.\n" + errorWrapLine(string, i));
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
                        case '\"', '\\', '/' -> stringBuf += c;
                        case 'b' -> stringBuf += '\b';
                        case 'f' -> stringBuf += '\f';
                        case 'n' -> stringBuf += '\n';
                        case 'r' -> stringBuf += '\r';
                        case 't' -> stringBuf += '\t';
                        case 'u' -> {
                            inUnicode = true;
                            unicodeBuf = "";
                        }
                        default -> throw new JsonFormatException(currentLine, currentColumn, getFilePath(),
                                "\\" + c + " could not be recognized as a valid backslash character.\n" + errorWrapLine(string, i));
                    }
                    inEscape = false;
                } else if (c == '\\') {
                    inEscape = true;
                } else if (c == '\"') {
                    // wrap up the buf
                    if (inKey) {
                        objectKeyBuf = stringBuf;
                    } else {
                        addNewValueToProcessingContainer(new JsonString(stringBuf), string, i);
                    }
                    stringBuf = null;
                    insideString = false;
                } else {
                    stringBuf += c;
                }
            } else {
                switch (c) {
                    case '\"' -> {
                        insideString = true;
                        stringBuf = "";
                    }
                    case '{' -> {
                        // enter a new object
                        if (inKey) {
                            throw new JsonFormatException(currentLine, currentColumn, getFilePath(),
                                    "Object being declared as a key.\n" + errorWrapLine(string, i));
                        }
                        {
                            final JsonObject newObj = new JsonObject();
                            if (processingContainer != null) {
                                addNewValueToProcessingContainer(newObj, string, i);
                            }
                            setProcessingContainer(newObj);
                            
                            // parser has to treat next string as a key in an object
                            inKey = true;
                        }
                    }
                    case '[' -> {
                        // enter a new array
                        if (inKey) {
                            throw new JsonFormatException(currentLine, currentColumn, getFilePath(),
                                    "Array being declared as a key.\n" + errorWrapLine(string, i));
                        }
                        {
                            final JsonArray newArr = new JsonArray();
                            if (processingContainer != null) {
                                addNewValueToProcessingContainer(newArr, string, i);
                            }
                            setProcessingContainer(newArr);
                        }
                    }
                    case ']' -> {
                        // exit this array
                        if (!(processingContainer instanceof JsonArray)) {
                            throw new JsonFormatException(currentLine, currentColumn, getFilePath(),
                                    "Symbol \']\' is unexpected because this layer is not recognized as an array.\n" + errorWrapLine(string, i));
                        }
                        // wrap up the buf for array
                        wrapUpSymbolBuf(string, i);

                        if (processingContainer == rootValue) {
                            // we are exiting the outermost structure in the json file
                            processingContainer = null;
                            break;
                        }
                        // exit this array i.e. switch processing value back to parent
                        processingContainer = processingContainer.getParent();
                    }
                    case '}' -> {
                        // exit this object
                        if (!(processingContainer instanceof JsonObject)) {
                            throw new JsonFormatException(currentLine, currentColumn, getFilePath(),
                                    "Symbol \'}\' is unexpected because this layer is not recognized as an object.\n" + errorWrapLine(string, i));
                        }
                        // wrap up the buf for object
                        wrapUpSymbolBuf(string, i);

                        inKey = false;

                        if (processingContainer == rootValue) {
                            // we are exiting the outermost structure in the json file
                            processingContainer = null;
                            break;
                        }
                        // exit this object i.e. switch processing value back to parent
                        processingContainer = processingContainer.getParent();
                    }
                    case ',' -> {
                        if (processingContainer instanceof JsonArray) {
                            // wrap up the buf for array
                            wrapUpSymbolBuf(string, i);
                        } else if (processingContainer instanceof JsonObject) {
                            if (!inKey) {
                                // wrap up the buf for object
                                wrapUpSymbolBuf(string, i);
                                
                                // parser has to treat next string as a key in an object
                                inKey = true;
                            } else {
                                throw new JsonFormatException(currentLine, currentColumn, getFilePath(),
                                        "Symbol \',\' is unexpected because the key is not given.\n" + errorWrapLine(string, i));
                            }
                        } else {
                            throw new JsonFormatException(currentLine, currentColumn, getFilePath(),
                                    "Symbol \',\' is unexpected because this layer is neither array nor object.\n" + errorWrapLine(string, i));
                        }
                    }
                    case ':' -> {
                        if (!(processingContainer instanceof JsonObject)) {
                            throw new JsonFormatException(currentLine, currentColumn, getFilePath(),
                                    "The symbol \':\' is not inside an object.\n" + errorWrapLine(string, i));
                        }
                        if (!inKey) {
                            throw new JsonFormatException(currentLine, currentColumn, getFilePath(),
                                    "The symbol \':\' could not be recognized to separate key and value in an object.\n" + errorWrapLine(string, i));
                        }
                        inKey = false;
                    }
                    case ' ', '\t', '\r', '\n' -> {
                        if (!symbolBuf.isEmpty()) {
                            symbolCompletelyRead = true;
                        }
                    }
                    default -> {
                        if (symbolBuf.isEmpty()) {
                            symbolCompletelyRead = false;
                        }
                        if (symbolCompletelyRead) {
                            throw new JsonFormatException(currentLine, currentColumn, getFilePath(),
                                    "Unexpected symbol \'" + string.substring(i > 100 ? i-100 : 0, i+1) + "\'.\n" + errorWrapLine(string, i));
                        }
                        symbolBuf += c;
                    }
                }
            }
        }
    }

    /**
     * Checks if a character is a valid hexadecimal digit.
     * 
     * @param c the character to be checked
     * @return <code>true</code> if the character is a valid hexadecimal digit, <code>false</code> otherwise
     */
    private static boolean isHexDigit(char c)
    {
        return switch (c) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D', 'E', 'F' -> true;
            default -> false;
        };
    }

    /**
     * Adds a new value to the current processing value.
     * 
     * @param newValue the new value to be added
     * @param string the full string being processed, for exception throwing only
     * @param i the index of the character being processed, for exception throwing only
     * @throws JsonFormatException thrown when processingContainer is not a container
     */
    private void addNewValueToProcessingContainer(JsonValue newValue, String string, int i) throws JsonFormatException
    {
        switch (processingContainer) {
            case null -> throw new JsonFormatException(currentLine, currentColumn, getFilePath(),
                "Control flow error: JsonParser.addNewValueToProcessingContainer() invoked when processingContainer is null.\n" + errorWrapLine(string, i));
            case JsonArray processingArray -> processingArray.addValue(newValue);
            case JsonObject processingObject -> processingObject.setValue(objectKeyBuf, newValue);
            default -> throw new JsonFormatException(currentLine, currentColumn, getFilePath(),
                "Control flow error: JsonParser.addNewValueToProcessingContainer() invoked when processingContainer is not a container.\n" + errorWrapLine(string, i));
        }
    }

    /**
     * Sets the processing container.
     * 
     * @param newProcessingContainer the new processing value
     */
    private void setProcessingContainer(JsonValue newProcessingContainer)
    {
        processingContainer = newProcessingContainer;
        if (rootValue == null) {
            rootValue = processingContainer;
        }
    }
    
    /**
     * Concludes the symbol held by symbolBuf, and attempts to convert it into one of the JsonValue types.
     * 
     * @param string the full string being processed, for exception throwing only
     * @param i the index of the character being processed, for exception throwing only
     * @throws JsonFormatException thrown when illegal json format is found
     */
    private void wrapUpSymbolBuf(String string, int i) throws JsonFormatException
    {
        if (symbolBuf.isEmpty()) {
            return;
        }

        JsonValue newValue = null;
        
        switch (symbolBuf) {
            case JsonBool.TRUE_EXPRESSION -> newValue = new JsonBool(true);
            case JsonBool.FALSE_EXPRESSION -> newValue = new JsonBool(false);
            case JsonNull.NULL_EXPRESSION -> newValue = new JsonNull();
            default -> {
                try {
                    double doubleVal = Double.parseDouble(symbolBuf);
                    newValue = new JsonNum(doubleVal);
                } catch (NumberFormatException e) {
                    throw new JsonFormatException(currentLine, currentColumn, getFilePath(),
                            "Symbol \"" + symbolBuf + "\" is unrecognized (neither true, false, null nor number).\n" + errorWrapLine(string, i));
                }
            }
        }
        
        addNewValueToProcessingContainer(newValue, string, i);
        
        symbolBuf = "";
    }

    /**
     * Gets the root json value.
     * 
     * @return the root json value, or null if an error has occured
     */
    private JsonValue getActualRootValue()
    {
        if ((rootValue == null) || !(rootValue instanceof JsonArray)) {
            return null;
        }
        final JsonArray wrappingArray = (JsonArray)rootValue;
        if (wrappingArray.size() == 1) {
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
    public final JsonValue getRootValue()
    {
        return actualRootValue;
    }
}
