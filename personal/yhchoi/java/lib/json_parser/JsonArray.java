/**
 * 
 *  JsonArray.java - A class that holds an array in json.
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
import java.util.Iterator;
import java.util.List;

/**
 * An array of json.
 *
 * @author Yui Hei Choi
 * @version 2024.11.18
 */
public final class JsonArray extends JsonValue implements Iterable<JsonValue>
{
    /**
     * Constructor for objects of class JsonArray.
     */
    public JsonArray()
    {
        super();
        super.setActualValue(new ArrayList<JsonValue>());
    }
    
    /**
     * Constructor for objects of class JsonArray.
     * 
     * @param newArray the new array value
     */
    public JsonArray(List<JsonValue> newArray)
    {
        super();
        super.setActualValue(new ArrayList<JsonValue>());
        if (newArray != null) {
            for (JsonValue jsonValue : newArray) {
                addValue(jsonValue.getDuplicate());
            }
        }
    }
    
    /**
     * Creates a duplicate of this json array and all its elements. <br>
     * The returned value is not a sibling of <code>newArray</code>, so parent of returned value is set to <code>null</code>.
     * 
     * @param newArray the new array value
     */
    public JsonArray(JsonArray newArray)
    {
        this(newArray.getArray());
    }
    
    /**
     * Gets the original underlying array list.
     * 
     * @return the original array list
     */
    private ArrayList<JsonValue> getArray()
    {
        return (ArrayList<JsonValue>)super.getActualValue();
    }
    
    /**
     * Adds a new value to the end of the array. <br>
     * No action is taken if this value or ancestors are locked.
     * 
     * @param newValue the new value to be added
     * @throws JsonValueLockedException if this method is called when this object is locked
     * @throws NullPointerException if <code>newValue == null</code>
     */
    public void addValue(JsonValue newValue) throws JsonValueLockedException, NullPointerException
    {
        if (isLocked()) {
            throw new JsonValueLockedException();
        }
        if (newValue == null) {
            throw new NullPointerException();
        }
        getArray().add(newValue);
        newValue.setParent(this);
    }
    
    /**
     * Inserts a new value to a specific index of the array. <br>
     * New value will be appended to the end of array of index is invalid. <br>
     * No action is taken if this json array or ancestors are locked.
     * 
     * @param index the index to insert
     * @param newValue the new value to be inserted
     * @throws JsonValueLockedException if this method is called when this object is locked
     * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index &gt;= size())
     * @throws NullPointerException if <code>newValue == null</code>
     */
    public void addValue(int index, JsonValue newValue) throws JsonValueLockedException, IndexOutOfBoundsException, NullPointerException
    {
        if (isLocked()) {
            throw new JsonValueLockedException();
        }
        if (newValue == null) {
            throw new NullPointerException();
        }
        getArray().add(index, newValue);
        newValue.setParent(this);
    }

    /**
     * Removes a value from the array. <br>
     * Does not remove anything if index is invalid. <br>
     * No action is taken if this json array or ancestors are locked.
     * 
     * @param index the index to insert
     * @return the value being removed
     * @throws JsonValueLockedException if this method is called when this object is locked
     * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index &gt;= size())
     */
    public JsonValue removeValue(int index) throws JsonValueLockedException, IndexOutOfBoundsException
    {
        if (isLocked()) {
            throw new JsonValueLockedException();
        }
        getArray().get(index).setParent(null);
        return getArray().remove(index);
    }
    
    /**
     * Gets one value from the array of values.
     * 
     * @param index the index of the value
     * @return the value, or null if index is invalid
     * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index &gt;= size())
     */
    public JsonValue getValue(int index) throws IndexOutOfBoundsException
    {
        return getArray().get(index);
    }
    
    /**
     * Sets one value from the array of values.
     * 
     * @param index the index
     * @param newValue the new value to be assigned to
     * @return the previous value at that index
     * @throws JsonValueLockedException if this method is called when this object is locked
     * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index &gt;= size())
     * @throws NullPointerException if <code>newValue == null</code>
     */
    public JsonValue setValue(int index, JsonValue newValue) throws JsonValueLockedException, IndexOutOfBoundsException, NullPointerException
    {
        if (isLocked()) {
            throw new JsonValueLockedException();
        }
        if (newValue == null) {
            throw new NullPointerException();
        }
        final JsonValue oldValue = getArray().get(index);
        oldValue.setParent(null);
        getArray().set(index, newValue);
        return oldValue;
    }
    
    /**
     * Gets the size of the array.
     * 
     * @return the size of the array
     */
    public int size()
    {
        return getArray().size();
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
        bufferedOutput.append("[");
        if (size() == 0) {
            bufferedOutput.append("]");
            return;
        }
        bufferedOutput.append("\n");
        indent += singleIndent;
        bufferedOutput.append(indent);
        boolean firstVal = true;
        for (JsonValue val : getArray()) {
            if (firstVal) {
                firstVal = false;
            } else {
                bufferedOutput.append(",\n" + indent);
            }
            val.serialize(bufferedOutput);
        }
        indent = indent.substring(0, indent.length() - singleIndent.length());
        bufferedOutput.append("\n" + indent + "]");
    }

    // /**
    //  * Returns the equivalent string of the json array.
    //  * 
    //  * @return the equivalent string of the json array
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
    //     String returnString = (((parent != null) && (parent instanceof JsonArray || parent instanceof JsonObject)) ? "" : indent) + "[";
    //     if (size() == 0) {
    //         return returnString + "]";
    //     }
    //     returnString += "\n";
    //     indent += singleIndent;
    //     returnString += indent;
    //     boolean firstVal = true;
    //     for (JsonValue val : getArray()) {
    //         if (firstVal) {
    //             firstVal = false;
    //         } else {
    //             returnString += ",\n" + indent;
    //         }
    //         returnString += val.toString();
    //     }
    //     indent = indent.substring(0, indent.length() - singleIndent.length());
    //     returnString += "\n" + indent + "]";
    //     return returnString;
    // }

    /**
     * Creates a duplicate of this json array and all its elements. <br>
     * The returned value is not a sibling of <code>this</code>, so its parent is set to <code>null</code>.
     * 
     * @return a duplicate of this json array
     */
    @Override
    public JsonArray getDuplicate()
    {
        return new JsonArray(this);
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
            final JsonValue value = getValue(idx);
            idx++;
            removable = true;
            return value;
        }

        /**
         * Removes current element from this json array.
         * 
         * @throws IllegalStateException if the <code>next</code> method has not yet been called, or the <code>remove</code> method has already been called after the last call to the <code>next</code> method
         */
        @Override
        public void remove() throws IllegalStateException
        {
            if (!removable) {
                throw new IllegalStateException();
            }
            removeValue(idx);
            removable = false;
        }
    }
}
