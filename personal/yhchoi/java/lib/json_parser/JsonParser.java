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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * A parser for json files.
 *
 * @author Yui Hei Choi
 * @version 2025.01.14
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

    private String fileAbsPath;             // the json file absolute path
    private BufferedInputStream inStream;   // the json file input stream
    private String contents;                // the json file contents

    private boolean parsed;                 // whether the parser already parsed the file

    private static final int IN_STREAM_BUF_SIZE = 1024;    // the input stream buffer size

    /**
     * Constructor for objects of class JsonParser.
     * 
     * @param file the json file to be parsed
     * @throws FileNotFoundException if file is not found
     */
    public JsonParser(File file) throws FileNotFoundException
    {
        commonInit();
        this.inStream = new BufferedInputStream(new FileInputStream(file), IN_STREAM_BUF_SIZE);
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
        this.inStream = new BufferedInputStream(inStream, IN_STREAM_BUF_SIZE);
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
        fileAbsPath = null;
        inStream = null;
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
        if (inStream != null) {
            parsed = true;
            parseByChar("[\n");
            final byte[] bytesBuffer = new byte[IN_STREAM_BUF_SIZE];
            int bytesRead;
            while ((bytesRead = inStream.read(bytesBuffer)) > 0) {
                parseByChar(new String(bytesBuffer, 0, bytesRead));
            }
            parseByChar("\n]");
            inStream.close();
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
                            throw new JsonFormatException(currentLine, currentColumn, getFilePath(),
                                "Unicode with string content \"" + unicodeBuf + "\" could not be converted to HEX.\n" + errorWrapLine(string, i));
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
                            throw new JsonFormatException(currentLine, currentColumn, getFilePath(),
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
                            throw new JsonFormatException(currentLine, currentColumn, getFilePath(),
                                "Object being declared as a key.\n" + errorWrapLine(string, i));
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
                            throw new JsonFormatException(currentLine, currentColumn, getFilePath(),
                                "Array being declared as a key.\n" + errorWrapLine(string, i));
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
                            throw new JsonFormatException(currentLine, currentColumn, getFilePath(),
                                "Symbol \']\' is unexpected because this layer is not recognized as an array.\n" + errorWrapLine(string, i));
                        }
                        // wrap up the buf for array
                        wrapUpSymbolBuf(currentLine, currentColumn, string, i);
                        {
                            JsonValue parent = processingValue.getParent();
                            if ((processingValue == rootValue) || (parent == null)) {
                                // we are exiting the outermost structure in the json file
                                processingValue = null;
                                return;
                            }
                            // exit this array i.e. switch processing value back to parent
                            processingValue = parent;
                        }
                        break;
                    case '}':
                        // exit this object
                        if (!(processingValue instanceof JsonObject)) {
                            throw new JsonFormatException(currentLine, currentColumn, getFilePath(),
                                "Symbol \'}\' is unexpected because this layer is not recognized as an object.\n" + errorWrapLine(string, i));
                        }
                        // wrap up the buf for object
                        wrapUpSymbolBuf(currentLine, currentColumn, string, i);
                        {
                            JsonValue parent = processingValue.getParent();
                            if ((processingValue == rootValue) || (parent == null)) {
                                // we are exiting the outermost structure in the json file
                                processingValue = null;
                                return;
                            }
                            // exit this object i.e. switch processing value back to parent
                            processingValue = parent;
                        }
                        inKey = false;
                        break;
                    case ',':
                        if (processingValue instanceof JsonArray) {
                            // wrap up the buf for array
                            wrapUpSymbolBuf(currentLine, currentColumn, string, i);
                        } else if (processingValue instanceof JsonObject) {
                            if (!inKey) {
                                // wrap up the buf for object
                                wrapUpSymbolBuf(currentLine, currentColumn, string, i);
                                
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
                        break;
                    case ':':
                        if (!(processingValue instanceof JsonObject)) {
                            throw new JsonFormatException(currentLine, currentColumn, getFilePath(),
                                "The symbol \':\' is not inside an object.\n" + errorWrapLine(string, i));
                        }
                        if (!inKey) {
                            throw new JsonFormatException(currentLine, currentColumn, getFilePath(),
                                "The symbol \':\' could not be recognized to separate key and value in an object.\n" + errorWrapLine(string, i));
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
                            throw new JsonFormatException(currentLine, currentColumn, getFilePath(),
                                "Unexpected symbol \'" + string.substring(i-100, i+1) + "\'.\n" + errorWrapLine(string, i));
                        }
                        symbolBuf += c;
                        break;
                }
            }
        }
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
     * @param currentLine the line being processed
     * @param currentColumn the column being processed
     * @param string the full string being processed
     * @param i the index of the character being processed
     * @throws JsonFormatException thrown when illegal json format is found
     */
    private void wrapUpSymbolBuf(int currentLine, int currentColumn, String string, int i) throws JsonFormatException
    {
        if (symbolBuf.isEmpty()) {
            return;
        }

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
                throw new JsonFormatException(currentLine, currentColumn, getFilePath(),
                    "Symbol \"" + symbolBuf + "\" is unrecognized (neither true, false, null nor number).\n" + errorWrapLine(string, i));
            }
        }
        
        if (!addNewValueToProcessingValue(newValue)) {
            throw new JsonFormatException(currentLine, currentColumn, getFilePath(),
                "Control flow error: JsonParser.wrapUpSymbolBuf() invoked when processingValue is not a container.\n" + errorWrapLine(string, i));
        }
        
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
