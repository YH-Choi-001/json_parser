/**
 * 
 *  JsonValue.java - A class that holds a generic value in json.
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
 * A value of json.
 *
 * @author Yui Hei Choi
 * @version 2025.02.05
 */
public abstract class JsonValue
{
    private Object value;           // the value held by this json value
    private JsonValue parent;       // the parent holding this json value
    private boolean locked;         // whether the value is locked
    private boolean lockedForever;  // whether the value is locked forever
    private int cachedHashCode;     // the cached hash code
    private boolean cachedHashCodeValid;    // whether myHashCode is valid: needs to recalculate hash code if this flag is false
    
    /**
     * Constructor for objects of class JsonValue.
     */
    protected JsonValue()
    {
        this(null);
    }
    
    /**
     * Constructor for objects of class JsonValue.
     * 
     * @param newValue the new json value
     */
    protected JsonValue(Object newValue)
    {
        value = newValue;
        parent = null;
        locked = false;
        lockedForever = false;
        cachedHashCode = super.hashCode();
        cachedHashCodeValid = false;
    }
    
    /**
     * Sets the actual value.
     * 
     * @param newValue the new actual value
     */
    protected final void setActualValue(Object newValue)
    {
        if (isLocked()) {
            return;
        }
        value = newValue;
    }
    
    /**
     * Gets the actual value.
     * 
     * @return the actual value
     */
    protected final Object getActualValue()
    {
        return value;
    }
    
    /**
     * Sets the parent of this json value.
     * 
     * @param parent the parent of this json value
     */
    protected final void setParent(JsonValue parent)
    {
        this.parent = parent;
    }
    
    /**
     * Gets the parent of this json value.
     * 
     * @return the parent of this json value, or null if this json value does not have a parent
     */
    public final JsonValue getParent()
    {
        return parent;
    }

    /**
     * Sets whether this json value (and all its elements) is locked (i.e. immutable).
     * 
     * @param lock true to set this json value (and all its elements) to immutable, false otherwise
     */
    public final void setLocked(boolean lock)
    {
        if (!lockedForever) {
            locked = lock;
        }
    }

    /**
     * Gets whether this json value (and all its elements) is locked (i.e. immutable).
     * 
     * @return true if this json value (and all its elements) is immutable, false otherwise
     */
    public final boolean isLocked()
    {
        return locked || ((parent != null) && (parent.isLocked()));
    }

    /**
     * Locks this json value (and all its elements) forever. <br>
     * This action is irreversible.
     */
    public final void lockForever()
    {
        setLocked(true);
        lockedForever = true;
    }
    
    /**
     * Gets whether this json value (and all its elements) is locked forever.
     * 
     * @return true if this json value (and all its elements) is locked forever, false otherwise
     */
    public final boolean isLockedForever()
    {
        return lockedForever || ((parent != null) && (parent.isLockedForever()));
    }
    
    /**
     * Gets the layer of this json value.
     * 
     * @return the layer of this json value, with 0 denoting root
     */
    public final int getLayer()
    {
        if (parent == null) {
            return 0;
        } else {
            return parent.getLayer() + 1;
        }
    }

    /**
     * Serializes this json value (and all child json values).
     * 
     * @param bufferedOutput the buffered output to write the serialized data to
     */
    public void serialize(Appendable bufferedOutput)
    {
        bufferedOutput.append(value.toString());
    }

    /**
     * Returns the equivalent string of the json value.
     * 
     * @return the equivalent string of the json value
     */
    @Override
    public final String toString()
    {
        /**
         * A class that appends serialization result to a string for toString methods to use.
         */
        final class ToStringResult implements Appendable {
            private String result;

            public ToStringResult() {
                result = "";
            }

            @Override
            public final void append(String s) {
                result += s;
            }

            public final String getResult() {
                return result;
            }
        }

        // makes use of serialize() method and ToStringResult class
        // to obtain serialized json values in a string
        final ToStringResult r = new ToStringResult();
        serialize(r);
        return r.getResult();
    }

    /**
     * Creates a duplicate (deep copy) of this json value (and all its elements). <br>
     * The returned value is not a sibling of <code>this</code>, so its parent is set to <code>null</code>.
     * 
     * @return a duplicate of this json value
     */
    public abstract JsonValue getDuplicate();

    /**
     * Invalidates the cached hash code.
     */
    protected final void invalidateCachedHashCode()
    {
        cachedHashCodeValid = false;
    }

    /**
     * Checks if the cached hash code is valid.
     * 
     * @return <code>true</code> if the cached hash code is valid, <code>false</code> otherwise
     */
    protected final boolean isCachedHashCodeValid()
    {
        return cachedHashCodeValid;
    }

    /**
     * Re-generates the hash code.
     * This hash code value is independent of the ancestors of this json value,
     * but is dependent of descendants of this json value.
     * 
     * @return a hash code value for this object
     * @see #hashCode()
     */
    protected abstract int generateHashCode();
    /* Current version of hash code generating formulas:
     * Warning! Subject to change.
     * These formulas are not guaranteed unchanged in the future.
     * JsonNull:
     *   31 * 31 * 31
     * JsonBool:
     *   3 + Boolean.hashCode(getValue())
     * JsonNum:
     *   5 + (Double.hashCode(getValue()) * 127)
     * JsonString:
     *   7 + (getValue().hashCode() * 31)
     * JsonArray:
     *   int hash = 11; for (v : values) { hash *= 7; hash += v.hashCode(); }
     * JsonObject:
     *   int hash = 13; for (e : entries) { hash += e.hashCode(); }
     */

    /**
     * Returns a hash code value for the object.
     * This hash code value is independent of the ancestors of this json value,
     * but is dependent of (possible) descendants of this json value.
     * 
     * @return a hash code value for this object
     */
    @Override
    public int hashCode()
    {
        if (!cachedHashCodeValid) {
            cachedHashCode = generateHashCode();
            cachedHashCodeValid = true;
        }
        return cachedHashCode;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * This comparison is independent of the ancestors of both json values,
     * but is dependent of (possible) descendants of this json value.
     * 
     * @param obj the reference object with which to compare
     * @return <code>true</code> if this object is the same as the obj argument; <code>false</code> otherwise
     */
    @Override
    public abstract boolean equals(Object obj);

    /**
     * Casts this <code>JsonValue</code> to <code>JsonNull</code>.
     * 
     * @return <code>(JsonNull)this</code>
     * @throws ClassCastException if <code>this</code> is not of type <code>JsonNull</code>
     */
    public final JsonNull castToNull() throws ClassCastException
    {
        return (JsonNull)this;
    }

    /**
     * Casts this <code>JsonValue</code> to <code>JsonBool</code>.
     * 
     * @return <code>(JsonBool)this</code>
     * @throws ClassCastException if <code>this</code> is not of type <code>JsonBool</code>
     */
    public final JsonBool castToBool() throws ClassCastException
    {
        return (JsonBool)this;
    }

    /**
     * Casts this <code>JsonValue</code> to <code>JsonNum</code>.
     * 
     * @return <code>(JsonNum)this</code>
     * @throws ClassCastException if <code>this</code> is not of type <code>JsonNum</code>
     */
    public final JsonNum castToNum() throws ClassCastException
    {
        return (JsonNum)this;
    }

    /**
     * Casts this <code>JsonValue</code> to <code>JsonString</code>.
     * 
     * @return <code>(JsonString)this</code>
     * @throws ClassCastException if <code>this</code> is not of type <code>JsonString</code>
     */
    public final JsonString castToString() throws ClassCastException
    {
        return (JsonString)this;
    }

    /**
     * Casts this <code>JsonValue</code> to <code>JsonArray</code>.
     * 
     * @return <code>(JsonArray)this</code>
     * @throws ClassCastException if <code>this</code> is not of type <code>JsonArray</code>
     */
    public final JsonArray castToArray() throws ClassCastException
    {
        return (JsonArray)this;
    }

    /**
     * Casts this <code>JsonValue</code> to <code>JsonObject</code>.
     * 
     * @return <code>(JsonObject)this</code>
     * @throws ClassCastException if <code>this</code> is not of type <code>JsonObject</code>
     */
    public final JsonObject castToObject() throws ClassCastException
    {
        return (JsonObject)this;
    }
}