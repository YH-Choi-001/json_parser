/**
 * 
 *  JsonString.java - A class that holds a string in json.
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

/**
 * A string of json.
 *
 * @author Yui Hei Choi
 * @version 2025.02.05
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
     * @throws NullPointerException if <code>newString == null</code>
     */
    public void setValue(String newString) throws JsonValueLockedException, NullPointerException
    {
        if (isLocked()) {
            throw new JsonValueLockedException();
        }
        if (newString == null) {
            throw new NullPointerException();
        }
        super.setActualValue(newString);
        invalidateCachedHashCode();
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
        final char[] charArray = getValue().toCharArray();
        for (char c : charArray) {
            switch (c) {
                case '\"', '\\' -> bufferedOutput.append("\\" + c);
                case '\b' -> bufferedOutput.append("\\b");
                case '\f' -> bufferedOutput.append("\\f");
                case '\n' -> bufferedOutput.append("\\n");
                case '\r' -> bufferedOutput.append("\\r");
                case '\t' -> bufferedOutput.append("\\t");
                default -> {
                    if (c >= 0x0020 && c <= 0x007e) {
                        bufferedOutput.append("" + c);
                    } else {
                        bufferedOutput.append("\\u");
                        final String unicode = Integer.toString((short)c, 16);
                        final int paddingZeros = 4 - unicode.length();
                        for (int i = 0; i < paddingZeros; i++) {
                            bufferedOutput.append("0");
                        }
                        bufferedOutput.append(unicode);
                    }
                }
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

    /**
     * Re-generates the hash code.
     * This hash code value is independent of the ancestors of this json value,
     * but is dependent of descendants of this json value.
     * 
     * @return a hash code value for this object
     * @see #hashCode()
     */
    @Override
    protected int generateHashCode()
    {
        return 7 + (getValue().hashCode() * 31);
    }

    /**
     * Returns a hash code value for the object.
     * This hash code value is independent of the ancestors of this json value.
     * 
     * @return a hash code value for this object
     */
    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * This comparison is independent of the ancestors of both json values.
     * 
     * @param obj the reference object with which to compare
     * @return <code>true</code> if this object is the same as the obj argument; <code>false</code> otherwise
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof JsonString)) {
            return false;
        }
        return ((JsonString)obj).getValue().equals(this.getValue());
    }
}
