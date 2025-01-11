/**
 * 
 *  JsonNull.java - A class that holds a null in json.
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
 * A null of json.
 *
 * @author Yui Hei Choi
 * @version 2024.11.18
 */
public final class JsonNull extends JsonValue
{
    /** The null expression in json. */
    public static final String NULL_EXPRESSION = "null";

    /**
     * Constructor for objects of class JsonNull.
     */
    public JsonNull()
    {
        super(null);
    }
    
    /**
     * Creates a duplicate of this json null. <br>
     * The returned value is not a sibling of <code>val</code>, so parent of returned value is set to <code>null</code>.
     * 
     * @param val the new null value
     */
    public JsonNull(JsonNull val)
    {
        super(null);
    }
    
    /**
     * Gets the null value.
     * 
     * @return the null value
     */
    public Object getValue()
    {
        return (Object)super.getActualValue();
    }

    /**
     * Serializes this json value.
     * 
     * @param bufferedOutput the buffered output to write the serialized data to
     */
    @Override
    public void serialize(Appendable bufferedOutput)
    {
        bufferedOutput.append(NULL_EXPRESSION);
    }

    /**
     * Creates a duplicate of this json null. <br>
     * The returned value is not a sibling of <code>this</code>, so its parent is set to <code>null</code>.
     * 
     * @return a duplicate of this json null
     */
    @Override
    public JsonNull getDuplicate()
    {
        return new JsonNull(this);
    }
}
