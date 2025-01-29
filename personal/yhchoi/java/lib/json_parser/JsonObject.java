/**
 * 
 *  JsonObject.java - A class that holds an object in json.
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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SequencedMap;
import java.util.Set;
import java.util.TreeMap;

/**
 * An object of json.
 *
 * @author Yui Hei Choi
 * @version 2025.01.29
 */
public final class JsonObject extends JsonValue implements Iterable<Map.Entry<String, JsonValue>>
{
    /**
     * The order of json values arranged.
     * 
     * @see #setOrder(Order)
     */
    public static enum ElementOrder
    {
        /**
         * Arranges the json values by the order they are inserted to this JsonObject.
         */
        INSERT_ORDER,

        /**
         * Arranges the json values by the lexicographical order of keys.
         */
        ASCENDING_ORDER;
    }
    
    private ElementOrder elementOrder;
    private static final ElementOrder DEFAULT_ELEMENT_ORDER = ElementOrder.INSERT_ORDER;

    /**
     * Constructor for objects of class JsonObject.
     */
    public JsonObject()
    {
        this((Map<String, JsonValue>)null);
    }
    
    /**
     * Constructor for objects of class JsonObject.
     * 
     * @param map the map to initialize this JsonObject
     * @throws NullPointerException if <code>(key == null) || (value == null)</code> for any entry in <code>map</code>
     */
    public JsonObject(Map<String, JsonValue> map) throws NullPointerException
    {
        super();
        super.setActualValue(null);
        elementOrder = null;
        setOrder(DEFAULT_ELEMENT_ORDER);
        if (map == null) {
            return;
        }
        for (Map.Entry<String, JsonValue> entry : map.entrySet()) {
            final String key = entry.getKey();
            final JsonValue value = entry.getValue();
            if (key == null || value == null) {
                throw new NullPointerException();
            }
            final JsonValue clonedValue = value.getDuplicate();
            try {
                setValue(key, clonedValue);
            } catch (NullPointerException | JsonValueLockedException e) {
                // NullPointerException should never occur at this point,
                // because they are intercepted by the if clause above.

                // JsonValueLockedException should never occur at this point,
                // because we construct a JsonValue without lock.
            }
        }
    }
    
    /**
     * Creates a duplicate of this json array and all its elements. <br>
     * The returned value is not a sibling of <code>newObject</code>, so parent of returned value is set to <code>null</code>.
     * 
     * @param newObject the new object value
     */
    public JsonObject(JsonObject newObject)
    {
        this(newObject.getMap());
    }
    
    /**
     * Gets the original underlying map.
     * 
     * @return the original map
     */
    private SequencedMap<String, JsonValue> getMap()
    {
        return (SequencedMap<String, JsonValue>)super.getActualValue();
    }

    /**
     * Sets the order of elements being arranged.
     * 
     * @param newElementOrder the new order of elements being arranged
     */
    public void setOrder(ElementOrder newElementOrder)
    {
        if (newElementOrder == null || this.elementOrder == newElementOrder) {
            return;
        }
        this.elementOrder = newElementOrder;
        final SequencedMap currentMap = getMap();
        SequencedMap newMap;
        if (currentMap != null) {
            newMap =
                switch (elementOrder) {
                case INSERT_ORDER -> new LinkedHashMap<>(currentMap);
                case ASCENDING_ORDER -> new TreeMap<>(currentMap);
                default -> new LinkedHashMap<>(currentMap);
            };
        } else {
            newMap =
                switch (elementOrder) {
                case INSERT_ORDER -> new LinkedHashMap<String, JsonValue>();
                case ASCENDING_ORDER -> new TreeMap<String, JsonValue>();
                default -> new LinkedHashMap<String, JsonValue>();
            };
        }
        super.setActualValue(newMap);
    }
    
    /**
     * Sets a new value to the object. <br>
     * Replaces old value if the key already existed. <br>
     * No action is taken if this json object or ancestors are locked.
     * 
     * @param key the key to map to this value
     * @param newValue the new value to be added
     * @return the value added
     * @throws JsonValueLockedException if this method is called when this object is locked
     * @throws NullPointerException if <code>(key == null) || (newValue == null)</code>
     */
    public JsonValue setValue(String key, JsonValue newValue) throws JsonValueLockedException, NullPointerException
    {
        if (isLocked()) {
            throw new JsonValueLockedException();
        }
        if (key == null || newValue == null) {
            throw new NullPointerException();
        }
        final JsonValue oldValue = getMap().get(key);
        if (oldValue != null) {
            oldValue.setParent(null);
        }
        final JsonValue toReturn = getMap().put(key, newValue);
        newValue.setParent(this);
        invalidateCachedHashCode();
        return toReturn;
    }

    /**
     * Removea a value from the object. <br>
     * Does not remove anything if key does not exist. <br>
     * No action is taken if this json object or ancestors are locked.
     * 
     * @param key the key to the value to be removed
     * @return the value removed
     * @throws JsonValueLockedException if this method is called when this object is locked
     * @throws NoSuchElementException if <code>key</code> is not found in this json object
     */
    public JsonValue removeValue(String key) throws JsonValueLockedException, NoSuchElementException
    {
        if (isLocked()) {
            throw new JsonValueLockedException();
        }
        if (!getMap().containsKey(key)) {
            throw new NoSuchElementException();
        }
        final JsonValue toReturn = getMap().remove(key);
        toReturn.setParent(null);
        invalidateCachedHashCode();
        return toReturn;
    }
    
    /**
     * Gets one value from the object of values.
     * 
     * @param key the key
     * @return the requested value
     * @throws NoSuchElementException if <code>key</code> is not found in this json object
     */
    public JsonValue getValue(final String key) throws NoSuchElementException
    {
        if (!getMap().containsKey(key)) {
            throw new NoSuchElementException();
        }
        return getMap().get(key);
    }
    
    /**
     * Gets the size of the object.
     * 
     * @return the size of the object
     */
    public int size()
    {
        return getMap().size();
    }

    /**
     * Checks whether this container has any objects.
     * 
     * @return true if this container has no object, false otherwise
     */
    public boolean isEmpty()
    {
        return getMap().isEmpty();
    }
    
    /**
     * Checks whether the object contains a specific key.
     * 
     * @param key the key to be checked
     * @return true if the object contains the key, false otherwise
     */
    public boolean containsKey(String key)
    {
        return getMap().containsKey(key);
    }
    
    /**
     * Serializes this json value (and all child json values).
     * 
     * @param bufferedOutput the buffered output to write the serialized data to
     */
    @Override
    public void serialize(Appendable bufferedOutput)
    {
        final int layer = getLayer();
        final String singleIndent = "    ";
        String indent = "";
        for (int i = 0; i < layer; i++) {
            indent += singleIndent;
        }
        final JsonValue parent = getParent();
        if (parent == null || !(parent instanceof JsonArray || parent instanceof JsonObject)) {
            bufferedOutput.append(indent);
        }
        bufferedOutput.append("{");
        if (isEmpty()) {
            bufferedOutput.append("}");
            return;
        }
        bufferedOutput.append("\n");
        indent += singleIndent;
        bufferedOutput.append(indent);
        boolean firstVal = true;
        for (Map.Entry<String, JsonValue> entry : getMap().entrySet()) {
            if (firstVal) {
                firstVal = false;
            } else {
                bufferedOutput.append(",\n" + indent);
            }

            // we just use to serialization from JsonString class : we're lazy
            new JsonString(entry.getKey()).serialize(bufferedOutput);
            bufferedOutput.append(": ");
            entry.getValue().serialize(bufferedOutput);
        }
        indent = indent.substring(0, indent.length() - singleIndent.length());
        bufferedOutput.append("\n" + indent + "}");
    }

    // /**
    //  * Returns the equivalent string of the json object.
    //  * 
    //  * @return the equivalent string of the json object
    //  */
    // @Override
    // public String toString()
    // {
    //     final int layer = getLayer();
    //     final String singleIndent = "    ";
    //     String indent = "";
    //     for (int i = 0; i < layer; i++) {
    //         indent += singleIndent;
    //     }
    //     final JsonValue parent = getParent();
    //     String returnString = (((parent != null) && (parent instanceof JsonArray || parent instanceof JsonObject)) ? "" : indent) + "{";
    //     if (size() == 0) {
    //         return returnString + "}";
    //     }
    //     returnString += "\n";
    //     indent += singleIndent;
    //     returnString += indent;
    //     boolean firstVal = true;
    //     for (int i = 0; i < keyList.size(); i++) {
    //         if (firstVal) {
    //             firstVal = false;
    //         } else {
    //             returnString += ",\n" + indent;
    //         }
    //         returnString += "\"" + keyList.get(i) + "\": ";
    //         returnString += valueList.get(i).toString();
    //     }
    //     indent = indent.substring(0, indent.length() - singleIndent.length());
    //     returnString += "\n" + indent + "}";
    //     return returnString;
    // }

    /**
     * Creates a duplicate (deep copy) of this json array and all its elements. <br>
     * The returned value is not a sibling of <code>this</code>, so its parent is set to <code>null</code>.
     * 
     * @return a duplicate of this json object
     */
    @Override
    public JsonObject getDuplicate()
    {
        return new JsonObject(this);
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
        int hashCode = 13;
        for (Map.Entry e : this) {
            hashCode += e.hashCode();
        }
        return hashCode;
    }

    /**
     * Returns a hash code value for the object.
     * This hash code value is independent of the ancestors of this json value,
     * but is dependent of descendants of this json value.
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
     * This comparison is independent of the ancestors of both json values,
     * but is dependent of descendants of both json values.
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
        if (!(obj instanceof JsonObject)) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        final JsonObject jsonObject = (JsonObject)obj;

        if (size() != jsonObject.size()) {
            return false;
        }

        // return this.getMap().equals(jsonObject.getMap());
        final Map rhsMap = jsonObject.getMap();
        for (Map.Entry<String, JsonValue> entry : this) {
            if (!entry.getValue().equals(rhsMap.get(entry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets an iterator. <br>
     * This iterator gives entries ordered based on the <code>setOrder()</code> method. <br>
     * This iterator supports removal. <br>
     * 
     * @return an iterator
     * @see #setOrder(ElementOrder)
     */
    @Override
    public Iterator<Map.Entry<String, JsonValue>> iterator()
    {
        return new Iter();
    }

    /**
     * An iterator to be used by for-each loops.
     * 
     * @author Yui Hei Choi
     * @version 2024.12.06
     */
    private class Iter implements Iterator<Map.Entry<String, JsonValue>>
    {
        private final Iterator<Map.Entry<String, JsonValue>> iterator;
        private String currentKey;
        private boolean removable;

        /**
         * Constructor for iterator.
         */
        public Iter()
        {
            final Map<String, JsonValue> map = getMap();
            final Set<Map.Entry<String, JsonValue>> entrySet = map.entrySet();
            iterator = entrySet.iterator();
            currentKey = null;
            removable = false;
        }

        /**
         * Checks whether the iterator still has unprocessed elements.
         * 
         * @return true if there are still unprocessed elements, false otherwise
         */
        @Override
        public boolean hasNext()
        {
            return iterator.hasNext();
        }

        /**
         * Gets the next element from the iterator.
         * 
         * @return the next element from the iterator, or null if all elements are processed
         */
        @Override
        public Map.Entry<String, JsonValue> next()
        {
            if (!hasNext()) {
                return null;
            }
            final Map.Entry<String, JsonValue> currentEntry = iterator.next();
            currentKey = currentEntry.getKey();
            removable = true;
            return currentEntry;
        }

        /**
         * Removes current element from this json object.
         * 
         * @throws IllegalStateException if the <code>next</code> method has not yet been called, or the <code>remove</code> method has already been called after the last call to the <code>next</code> method
         */
        @Override
        public void remove() throws IllegalStateException
        {
            if (!removable) {
                throw new IllegalStateException();
            }
            removeValue(currentKey);
            removable = false;
        }
    }
}
