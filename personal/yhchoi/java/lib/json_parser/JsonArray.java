/**
 * 
 *  JsonArray.java - A class that holds an array in json.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * An array of json.
 *
 * @author Yui Hei Choi
 * @version 2025.01.29
 */
public final class JsonArray extends JsonValue implements List<JsonValue>
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
    private List<JsonValue> getArray()
    {
        return (List<JsonValue>)super.getActualValue();
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
        addValue(size(), newValue);
    }
    
    /**
     * Inserts a new value to a specific index of the array. <br>
     * New value will be appended to the end of array of index is invalid. <br>
     * No action is taken if this json array or ancestors are locked.
     * 
     * @param index the index to insert
     * @param newValue the new value to be inserted
     * @throws JsonValueLockedException if this method is called when this object is locked
     * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index &gt; size())
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
        invalidateCachedHashCode();
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
        final JsonValue toReturn = getArray().remove(index);
        invalidateCachedHashCode();
        return toReturn;
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
        invalidateCachedHashCode();
        return oldValue;
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
        if (isEmpty()) {
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
     * Creates a duplicate (deep copy) of this json array and all its elements. <br>
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
        int hashValue = 11;
        for (JsonValue v : getArray()) {
            hashValue *= 7;
            hashValue += v.hashCode();
        }
        return hashValue;
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
        if (!(obj instanceof JsonArray)) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        final JsonArray jsonArray = (JsonArray)obj;
        final int thisArraySize = size();

        if (thisArraySize != jsonArray.size()) {
            return false;
        }

        for (int i = 0; i < thisArraySize; i++) {
            final JsonValue lhs = this.getValue(i);
            final JsonValue rhs = jsonArray.getValue(i);

            // if both sides cached hash codes are valid but unequal, they are unequal
            if (
                lhs.isCachedHashCodeValid() &&
                rhs.isCachedHashCodeValid() &&
                (lhs.hashCode() != rhs.hashCode())
            )
            {
                return false;
            }
            if (!lhs.equals(rhs)) {
                return false;
            }
        }
        return true;
    }

    // =======================================================================================
    // =================================== interface List ====================================
    // =======================================================================================

    /**
     * This method is provided as an implementation of <code>interface List</code>.
     * 
     * @param index the index to insert
     * @param element the new value to be inserted
     * @throws JsonValueLockedException if this method is called when this object is locked
     * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index &gt; size())
     * @throws NullPointerException if <code>newValue == null</code>
     * @see #addValue(int, JsonValue)
     */
    @Override
    public void add(int index, JsonValue element) throws JsonValueLockedException, IndexOutOfBoundsException, NullPointerException
    {
        addValue(index, element);
    }

    /**
     * This method is provided as an implementation of <code>interface List</code>.
     * 
     * @param element the new value to be added
     * @return <code>true</code>
     * @throws JsonValueLockedException if this method is called when this object is locked
     * @throws NullPointerException if <code>newValue == null</code>
     * @see #addValue(JsonValue)
     */
    @Override
    public boolean add(JsonValue element) throws JsonValueLockedException, NullPointerException
    {
        addValue(element);
        return true;
    }

    /**
     * This method is provided as an implementation of <code>interface List</code>.
     * 
     * @param index the index to insert
     * @param c collection containing elements to be inserted to this list
     * @return <code>true</code> if this list changed as a result of the call
     * @throws JsonValueLockedException if this method is called when this object is locked
     * @throws IndexOutOfBoundsException if the index is out of range (index &lt; 0 || index &gt; size())
     * @throws NullPointerException if <code>newValue == null</code>
     * @see #add(int, JsonValue)
     */
    @Override
    public boolean addAll(int index, Collection<? extends JsonValue> c) throws JsonValueLockedException, IndexOutOfBoundsException, NullPointerException
    {
        for (JsonValue v : c) {
            add(index, v);
            index++;
        }
        return true;
    }

    /**
     * This method is provided as an implementation of <code>interface List</code>.
     * 
     * @param c collection containing elements to be added to this list
     * @return <code>true</code> if this list changed as a result of the call
     * @throws JsonValueLockedException if this method is called when this object is locked
     * @throws NullPointerException if <code>newValue == null</code>
     * @see #add(JsonValue)
     */
    @Override
    public boolean addAll(Collection<? extends JsonValue> c) throws JsonValueLockedException, NullPointerException
    {
        for (JsonValue v : c) {
            add(v);
        }
        return true;
    }

    /**
     * This method is provided as an implementation of <code>interface List</code>.
     * 
     * @throws JsonValueLockedException if this method is called when this object is locked
     */
    @Override
    public void clear() throws JsonValueLockedException
    {
        while (!isEmpty()) {
            removeValue(0);
        }
    }

    /**
     * This method is provided as an implementation of <code>interface List</code>.
     */
    @Override
    public boolean contains(Object o)
    {
        return getArray().contains(o);
    }

    /**
     * This method is provided as an implementation of <code>interface List</code>.
     */
    @Override
    public boolean containsAll(Collection<?> c)
    {
        return getArray().containsAll(c);
    }

    /**
     * This method is provided as an implementation of <code>interface List</code>.
     */
    @Override
    public JsonValue get(int index)
    {
        return getValue(index);
    }

    /**
     * This method is provided as an implementation of <code>interface List</code>.
     */
    @Override
    public int indexOf(Object o)
    {
        return getArray().indexOf(o);
    }

    /**
     * This method is provided as an implementation of <code>interface List</code>.
     */
    @Override
    public boolean isEmpty()
    {
        return getArray().isEmpty();
    }

    /**
     * This method is provided as an implementation of <code>interface List</code>.
     */
    @Override
    public Iterator<JsonValue> iterator()
    {
        return new Iter();
    }

    /**
     * This method is provided as an implementation of <code>interface List</code>.
     */
    @Override
    public int lastIndexOf(Object o)
    {
        return getArray().lastIndexOf(o);
    }

    /**
     * This method is provided as an implementation of <code>interface List</code>.
     */
    @Override
    public ListIterator<JsonValue> listIterator()
    {
        return new Iter();
    }

    /**
     * This method is provided as an implementation of <code>interface List</code>.
     */
    @Override
    public ListIterator<JsonValue> listIterator(int index)
    {
        return new Iter(index);
    }

    /**
     * This method is provided as an implementation of <code>interface List</code>.
     */
    @Override
    public JsonValue remove(int index)
    {
        return removeValue(index);
    }

    /**
     * This method is provided as an implementation of <code>interface List</code>.
     */
    @Override
    public boolean remove(Object o)
    {
        if ((o == null) || !(o instanceof JsonValue)) {
            return false;
        }
        if (contains((JsonValue)o)) {
            removeValue(indexOf(o));
            return true;
        }
        return false;
    }

    /**
     * This method is provided as an implementation of <code>interface List</code>.
     */
    @Override
    public boolean removeAll(Collection<?> c)
    {
        if (c == null) {
            throw new NullPointerException();
        }

        boolean anyElementRemoved = false;

        final Iterator<JsonValue> it = iterator();
        while (it.hasNext()) {
            final JsonValue element = it.next();
            if (c.contains(element)) {
                it.remove();
                anyElementRemoved = true;
            }
        }
        return anyElementRemoved;
    }

    /**
     * This method is provided as an implementation of <code>interface List</code>.
     */
    @Override
    public boolean retainAll(Collection<?> c)
    {
        if (c == null) {
            throw new NullPointerException();
        }

        boolean anyElementRemoved = false;

        final Iterator<JsonValue> it = iterator();
        while (it.hasNext()) {
            final JsonValue element = it.next();
            if (!c.contains(element)) {
                it.remove();
                anyElementRemoved = true;
            }
        }
        return anyElementRemoved;
    }

    /**
     * This method is provided as an implementation of <code>interface List</code>.
     * 
     * @see #setValue(int, JsonValue)
     */
    @Override
    public JsonValue set(int index, JsonValue value)
    {
        return setValue(index, value);
    }

    /**
     * This method is provided as an implementation of <code>interface List</code>.
     */
    @Override
    public int size()
    {
        return getArray().size();
    }

    /**
     * This method is provided as an implementation of <code>interface List</code> but not supported.
     * 
     * @throws UnsupportedOperationException since listeners cannot be backed for sublisting
     */
    @Override
    public List<JsonValue> subList(int fromIndex, int toIndex) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
        // if  (fromIndex < 0 || toIndex > size() || fromIndex > toIndex) {
        //     throw new IndexOutOfBoundsException();
        // }
    }
    
    /**
     * This method is provided as an implementation of <code>interface List</code>.
     */
    @Override
    public JsonValue[] toArray()
    {
        return getArray().toArray(new JsonValue[size()]);
    }

    /**
     * This method is provided as an implementation of <code>interface List</code> but not supported.
     * 
     * @throws UnsupportedOperationException since T must be JsonValue or its subtypes
     */
    @Override
    public <T> T[] toArray(T[] a) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    // =======================================================================================
    // ================================ end of interface List ================================
    // =======================================================================================


    /**
     * An iterator to be used by for-each loops.
     * 
     * @author Yui Hei Choi
     * @version 2024.12.06
     */
    private class Iter implements ListIterator<JsonValue>
    {
        private int cursor;             // cursor
        private boolean removed;        // if the current item is removed
        private boolean added;          // if the current location has items being added

        /**
         * Constructor for iterator.
         */
        public Iter()
        {
            this(0);
        }

        /**
         * Constructor for iterator.
         * 
         * @param startingIndex the index to start iterating
         */
        public Iter(int startingIndex)
        {
            cursor = startingIndex;
            resetFlagsAfterCursorMoved();
        }

        /**
         * Resets the flags after the cursor moved.
         */
        private void resetFlagsAfterCursorMoved()
        {
            removed = false;
            added = false;
        }

        /**
         * Returns <code>true</code> if this list iterator has more elements when traversing the list in the forward direction.
         * (In other words, returns <code>true</code> if <code>next()</code> would return an element rather than throwing an exception.)
         * 
         * @return <code>true</code> if the list iterator has more elements when traversing the list in the forward direction
         */
        @Override
        public boolean hasNext()
        {
            return cursor < size();
        }

        /**
         * Returns the next element in the list and advances the cursor position.
         * This method may be called repeatedly to iterate through the list,
         * or intermixed with calls to <code>previous()</code> to go back and forth.
         * (Note that alternating calls to <code>next</code> and <code>previous</code> will return the same element repeatedly.)
         * 
         * @return the next element in the list
         * @throws NoSuchElementException if the iteration has no next element
         */
        @Override
        public JsonValue next() throws NoSuchElementException
        {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            final JsonValue value = getValue(cursor);
            cursor++;
            resetFlagsAfterCursorMoved();
            return value;
        }

        /**
         * Returns <code>true</code> if this list iterator has more elements when traversing the list in the reverse direction.
         * (In other words, returns <code>true</code> if <code>previous()</code> would return an element rather than throwing an exception.)
         * 
         * @return <code>true</code> if the list iterator has more elements when traversing the list in the reverse direction
         */
        @Override
        public boolean hasPrevious()
        {
            return cursor > 0;
        }

        /**
         * Returns the previous element in the list and moves the cursor position backwards.
         * This method may be called repeatedly to iterate through the list backwards,
         * or intermixed with calls to <code>next()</code> to go back and forth.
         * (Note that alternating calls to <code>next</code> and <code>previous</code> will return the same element repeatedly.)
         */
        @Override
        public JsonValue previous() throws NoSuchElementException
        {
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }
            cursor--;
            final JsonValue value = getValue(cursor);
            resetFlagsAfterCursorMoved();
            return value;
        }

        /**
         * Returns the index of the element that would be returned by a subsequent call to <code>next()</code>.
         * (Returns list size if the list iterator is at the end of the list.)
         * 
         * @return the index of the element that would be returned by a subsequent call to <code>next</code>, or list size if the list iterator is at the end of the list
         */
        @Override
        public int nextIndex()
        {
            return cursor;
        }

        /**
         * Returns the index of the element that would be returned by a subsequent call to <code>previous()</code>.
         * (Returns -1 if the list iterator is at the beginning of the list.)
         * 
         * @return the index of the element that would be returned by a subsequent call to <code>previous</code>, or -1 if the list iterator is at the end of the list
         */
        @Override
        public int previousIndex()
        {
            return cursor - 1;
        }

        /**
         * Removes from the list the last element that was returned by <code>next()</code> or <code>previous()</code> (optional operation).
         * This call can only be made once per call to <code>next</code> or <code>previous</code>.
         * It can be made only if <code>add(E)</code> has not been called after the last call to <code>next</code> or <code>previous</code>.
         * 
         * @throws IllegalStateException if the neither <code>next</code> nor <code>previous</code> have been called, or <code>remove</code> or <code>add</code> have been called after the last call to <code>next</code> nor <code>previous</code>
         */
        @Override
        public void remove() throws IllegalStateException
        {
            if (removed || added) {
                throw new IllegalStateException();
            }
            removeValue(cursor);
            removed = true;
        }

        /**
         * Replaces the last element returned by <code>next()</code> or <code>previous()</code> with the specified element (optional operation).
         * This call can be made only if neither <code>remove()</code> nor <code>add(E)</code> have been called after the last call to <code>next</code> or <code>previous</code>.
         * 
         * @param value the json value with which to replace the last element returned by next or previous
         * @throws IllegalStateException if neither <code>next</code> nor <code>previous</code> have been called, or <code>remove</code> or <code>add</code> have been called after the last call to <code>next</code> nor <code>previous</code>
         */
        @Override
        public void set(JsonValue value) throws IllegalStateException
        {
            if (removed || added) {
                throw new IllegalStateException();
            }
            setValue(cursor, value);
        }

        /**
         * Inserts the specified element into the list (optional operation).
         * The element is inserted immediately before the element that would be returned by <code>next()</code>, if any,
         * and after the element that would be returned by <code>previous()</code>, if any.
         * (If the list contains no elements, the new element becomes the sole element on the list.)
         * The new element is inserted before the implicit cursor:
         * a subsequent call to <code>next</code> would be unaffected,
         * and a subsequent call to <code>previous</code> would return the new element.
         * (This call increases by one the value that would be returned by a call to <code>nextIndex</code> or <code>previousIndex</code>.)
         * 
         * @param value the json value to insert
         */
        @Override
        public void add(JsonValue value)
        {
            addValue(cursor, value);
            cursor++;
            added = true;
        }
    }
}
