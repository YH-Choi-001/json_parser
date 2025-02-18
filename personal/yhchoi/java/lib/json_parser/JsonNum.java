/**
 * 
 *  JsonNum.java - A class that holds a number in json.
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
 * A number of json.
 *
 * @author Yui Hei Choi
 * @version 2025.02.10
 */
public final class JsonNum extends JsonValue
{
    /**
     * Constructor for objects of class JsonNum.
     */
    public JsonNum()
    {
        super();
    }
    
    /**
     * Constructor for objects of class JsonNum.
     * 
     * @param newVal the new numeric value
     */
    public JsonNum(double newVal)
    {
        super(newVal);
    }
    
    /**
     * Creates a duplicate of this json number. <br>
     * The returned value is not a sibling of <code>newVal</code>, so parent of returned value is set to <code>null</code>.
     * 
     * @param newVal the new numeric value
     */
    public JsonNum(JsonNum newVal)
    {
        super();
        if (newVal != null) {
            setValue(newVal.getValue());
        }
    }
    
    /**
     * Sets the numeric value. <br>
     * No action is taken if this json number or ancestors are locked.
     * 
     * @param newVal the new numeric value
     * @throws JsonValueLockedException if this method is called when this object is locked
     */
    public void setValue(double newVal) throws JsonValueLockedException
    {
        if (isLocked()) {
            throw new JsonValueLockedException();
        }
        super.setActualValue(newVal);
        invalidateCachedHashCode();
    }
    
    /**
     * Gets the numeric value.
     * 
     * @return the numeric value
     */
    public double getValue()
    {
        return (Double)super.getActualValue();
    }

    /**
     * Creates a duplicate of this json number. <br>
     * The returned value is not a sibling of <code>this</code>, so its parent is set to <code>null</code>.
     * 
     * @return a duplicate of this json number
     */
    @Override
    public JsonNum getDuplicate()
    {
        return new JsonNum(this);
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
        return 5 + (Double.hashCode(getValue()) * 127);
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
        if (!(obj instanceof JsonNum)) {
            return false;
        }
        return this.getValue() == ((JsonNum)obj).getValue();
    }
}
