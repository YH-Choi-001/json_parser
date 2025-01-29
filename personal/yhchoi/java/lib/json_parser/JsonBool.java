/**
 * 
 *  JsonBool.java - A class that holds a boolean in json.
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
 * A boolean of json.
 *
 * @author Yui Hei Choi
 * @version 2025.01.29
 */
public final class JsonBool extends JsonValue
{
    /** The true expression in json. */
    public static final String TRUE_EXPRESSION = "true";

    /** The false expression in json. */
    public static final String FALSE_EXPRESSION = "false";

    /**
     * Constructor for objects of class JsonBool.
     */
    public JsonBool()
    {
        super();
    }
    
    /**
     * Constructor for objects of class JsonBool.
     * 
     * @param newBool the new boolean value
     */
    public JsonBool(boolean newBool)
    {
        super(newBool);
    }
    
    /**
     * Creates a duplicate of this json boolean. <br>
     * The returned value is not a sibling of <code>newBool</code>, so parent of returned value is set to <code>null</code>.
     * 
     * @param newBool the new boolean value
     */
    public JsonBool(JsonBool newBool)
    {
        super();
        if (newBool != null) {
            setValue(newBool.getValue());
        }
    }
    
    /**
     * Sets the boolean value. <br>
     * No action is taken if this json boolean or ancestors are locked.
     * 
     * @param newBool the new boolean value
     * @throws JsonValueLockedException if this method is called when this object is locked
     */
    public void setValue(boolean newBool) throws JsonValueLockedException
    {
        if (isLocked()) {
            throw new JsonValueLockedException();
        }
        super.setActualValue(newBool);
        invalidateCachedHashCode();
    }
    
    /**
     * Gets the boolean value.
     * 
     * @return the boolean value
     */
    public boolean getValue()
    {
        return (Boolean)super.getActualValue();
    }

    /**
     * Serializes this json value.
     * 
     * @param bufferedOutput the buffered output to write the serialized data to
     */
    @Override
    public void serialize(Appendable bufferedOutput)
    {
        bufferedOutput.append(getValue() ? TRUE_EXPRESSION : FALSE_EXPRESSION);
    }

    /**
     * Creates a duplicate of this json null. <br>
     * The returned value is not a sibling of <code>this</code>, so its parent is set to <code>null</code>.
     * 
     * @return a duplicate of this json boolean
     */
    @Override
    public JsonBool getDuplicate()
    {
        return new JsonBool(this);
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
        return 3 + Boolean.hashCode(getValue());
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
        if (!(obj instanceof JsonBool)) {
            return false;
        }
        return this.getValue() == ((JsonBool)obj).getValue();
    }
}
