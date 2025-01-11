/**
 * 
 *  JsonValue.java - A class that holds a generic value in json.
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
 * A value of json.
 *
 * @author Yui Hei Choi
 * @version 2024.11.18
 */
public abstract class JsonValue
{
    private Object value;           // the value held by this json value
    private JsonValue parent;       // the parent holding this json value
    private boolean locked;         // whether the value is locked
    private boolean lockedForever;  // whether the value is locked forever
    
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
        class ToStringResult implements Appendable {
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
        ToStringResult r = new ToStringResult();
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
}