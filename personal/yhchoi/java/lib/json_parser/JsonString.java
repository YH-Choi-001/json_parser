/**
 * 
 *  JsonString.java - A class that holds a string in json.
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

/**
 * A string of json.
 *
 * @author Yui Hei Choi
 * @version 2024.11.18
 */
public final class JsonString extends JsonValue
{
    /**
     * Constructor for objects of class JsonString.
     */
    public JsonString()
    {
        super();
    }
    
    /**
     * Constructor for objects of class JsonString.
     * 
     * @param newString the new string value
     */
    public JsonString(String newString)
    {
        super(newString);
    }
    
    /**
     * Creates a duplicate of this json string. <br>
     * The returned value is not a sibling of <code>newString</code>, so parent of returned value is set to <code>null</code>.
     * 
     * @param newString the new string value
     */
    public JsonString(JsonString newString)
    {
        super();
        if (newString != null) {
            setValue(newString.getValue());
        }
    }
    
    /**
     * Sets the string value. <br>
     * No action is taken if this json string or ancestors are locked.
     * 
     * @param newString the new string value
     * @throws JsonValueLockedException if this method is called when this object is locked
     */
    public void setValue(String newString) throws JsonValueLockedException
    {
        if (isLocked()) {
            throw new JsonValueLockedException();
        }
        super.setActualValue(newString);
    }
    
    /**
     * Gets the string value.
     * 
     * @return the string value
     */
    public String getValue()
    {
        return (String)super.getActualValue();
    }
    
    /**
     * Serializes this json value.
     * 
     * @param bufferedOutput the buffered output to write the serialized data to
     */
    @Override
    public void serialize(Appendable bufferedOutput)
    {
        bufferedOutput.append("\"");
        for (char c : getValue().toCharArray()) {
            switch (c) {
                case '\"':
                case '\\':
                case '/':
                    bufferedOutput.append("\\" + c);
                    break;
                case '\b':
                    bufferedOutput.append("\\b");
                    break;
                case '\f':
                    bufferedOutput.append("\\f");
                    break;
                case '\n':
                    bufferedOutput.append("\\n");
                    break;
                case '\r':
                    bufferedOutput.append("\\r");
                    break;
                case '\t':
                    bufferedOutput.append("\\t");
                    break;
                default:
                    if (c >= 0x0020 && c <= 0x007e) {
                        bufferedOutput.append("" + c);
                    } else {
                        bufferedOutput.append("\\u" + Integer.toString((short)c, 16));
                    }
                    break;
            }
        }
        bufferedOutput.append("\"");
    }

    /**
     * Creates a duplicate of this json string. <br>
     * The returned value is not a sibling of <code>this</code>, so its parent is set to <code>null</code>.
     * 
     * @return a duplicate of this json string
     */
    @Override
    public JsonString getDuplicate()
    {
        return new JsonString(this);
    }
}
