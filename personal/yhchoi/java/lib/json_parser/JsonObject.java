/**
 * 
 *  JsonObject.java - A class that holds an object in json.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * An object of json.
 *
 * @author Yui Hei Choi
 * @version 2024.11.18
 */
public final class JsonObject extends JsonValue implements Iterable<JsonValue>
{
    private final ArrayList<String> keyList;            // list of keys
    private final ArrayList<JsonValue> valueList;       // list of values

    /**
     * Constructor for objects of class JsonObject.
     */
    public JsonObject()
    {
        super();
        keyList = new ArrayList<>();
        valueList = new ArrayList<>();
        super.setActualValue(null);
    }
    
    /**
     * Constructor for objects of class JsonObject.
     * 
     * @param newObject the new object value
     */
    public JsonObject(HashMap<String, JsonValue> newObject)
    {
        super();
        final int initSize = ((newObject != null) ? newObject.size() : 0);
        keyList = new ArrayList<>(initSize);
        valueList = new ArrayList<>(initSize);
        super.setActualValue(null);
        if (newObject == null) {
            return;
        }
        for (String key : newObject.keySet()) {
            if ((key == null) || (newObject.get(key) == null)) {
                continue;
            }
            keyList.add(key);
            final JsonValue clonedValue = newObject.get(key).getDuplicate();
            valueList.add(clonedValue);
            clonedValue.setParent(this);
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
        super();
        final int initSize = ((newObject != null) ? newObject.size() : 0);
        keyList = new ArrayList<>(initSize);
        valueList = new ArrayList<>(initSize);
        super.setActualValue(null);
        if (newObject == null) {
            return;
        }
        for (int i = 0; i < newObject.keyList.size(); i++) {
            final String key            = newObject.keyList.get(i);
            final JsonValue clonedValue = newObject.valueList.get(i).getDuplicate();
            keyList.add(key);
            valueList.add(clonedValue);
            clonedValue.setParent(this);
        }
    }
    
    /**
     * Sets a new value to the object. <br>
     * Replaces old value if the key already existed. <br>
     * No action is taken if this json object or ancestors are locked.
     * 
     * @param key the key to map to this value
     * @param newValue the new value to be added
     * @throws JsonValueLockedException if this method is called when this object is locked
     * @throws NullPointerException if <code>(key == null) || (newValue == null)</code>
     */
    public void setValue(String key, JsonValue newValue) throws JsonValueLockedException, NullPointerException
    {
        if (isLocked()) {
            throw new JsonValueLockedException();
        }
        if (key == null || newValue == null) {
            throw new NullPointerException();
        }
        final int indexOfKey = keyList.indexOf(key);
        if (indexOfKey == -1) {
            // key not found, add to last of array
            keyList.add(key);
            valueList.add(newValue);
        } else {
            // key found, replace the key to the new value
            // detach the old value from referencing to this object
            final JsonValue oldValue = valueList.get(indexOfKey);
            oldValue.setParent(null);
            // put the new value into the array
            valueList.set(indexOfKey, newValue);
        }
        newValue.setParent(this);
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
        final int indexOfKey = keyList.indexOf(key);
        if (indexOfKey == -1) {
            throw new NoSuchElementException();
        }
        valueList.get(indexOfKey).setParent(null);
        keyList.remove(indexOfKey);
        return valueList.remove(indexOfKey);
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
        final int indexOfKey = keyList.indexOf(key);
        if (indexOfKey == -1) {
            // key not found
            throw new NoSuchElementException();
        } else {
            // key found, replace the key to the new value
            return valueList.get(indexOfKey);
        }
    }
    
    /**
     * Gets the size of the object.
     * 
     * @return the size of the object
     */
    public int size()
    {
        return keyList.size();
    }
    
    /**
     * Checks whether the object contains a specific key.
     * 
     * @param key the key to be checked
     * @return true if the object contains the key, false otherwise
     */
    public boolean containsKey(String key)
    {
        return keyList.contains(key);
    }
    
    /**
     * Gets the set of keys of the object.
     * 
     * @return the set of keys of the object
     */
    public Set<String> keySet()
    {
        return new HashSet<>(keyList);
    }

    /**
     * Gets the ordered iterable of keys of the object.
     * 
     * @return the ordered iterable of keys of the object
     */
    public Iterable<String> keyIterable()
    {
        return keyList;
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
        if (size() == 0) {
            bufferedOutput.append("}");
            return;
        }
        bufferedOutput.append("\n");
        indent += singleIndent;
        bufferedOutput.append(indent);
        boolean firstVal = true;
        for (int i = 0; i < keyList.size(); i++) {
            if (firstVal) {
                firstVal = false;
            } else {
                bufferedOutput.append(",\n" + indent);
            }

            bufferedOutput.append("\"" + keyList.get(i) + "\": ");
            valueList.get(i).serialize(bufferedOutput);
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
     * Creates a duplicate of this json array and all its elements. <br>
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
     * Gets an iterator.
     * 
     * @return an iterator
     */
    @Override
    public Iterator<JsonValue> iterator()
    {
        return new Iter();
    }

    /**
     * An iterator to be used by for-each loops.
     * 
     * @author Yui Hei Choi
     * @version 2024.12.06
     */
    private class Iter implements Iterator<JsonValue>
    {
        private int idx;                // scanning index
        private boolean removable;      // if the current item is removable

        /**
         * Constructor for iterator.
         */
        public Iter()
        {
            idx = 0;
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
            return idx < size();
        }

        /**
         * Gets the next element from the iterator.
         * 
         * @return the next element from the iterator, or null if all elements are processed
         */
        @Override
        public JsonValue next()
        {
            if (!hasNext()) {
                return null;
            }
            final JsonValue value = valueList.get(idx);
            idx++;
            removable = true;
            return value;
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
            valueList.get(idx).setParent(null);
            keyList.remove(idx);
            valueList.remove(idx);
            removable = false;
        }
    }
}
