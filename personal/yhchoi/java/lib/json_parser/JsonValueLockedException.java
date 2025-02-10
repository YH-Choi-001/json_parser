/**
 * 
 *  JsonValueLockedException.java - An exception to be thrown when program attempts to modify a locked JsonValue.
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
 * Exception when java program attempts to modify a locked JsonValue.
 * 
 * @author Yui Hei Choi
 * @version 2025.02.10
 */
public class JsonValueLockedException extends RuntimeException
{
    /**
     * Constructor for JsonValueLockedException.
     */
    public JsonValueLockedException()
    {
        super("Java program attempts to modify a locked JsonValue.");
    }

    /**
     * Constructor for JsonValueLockedException.
     * 
     * @param message the detail message. The detail message is saved for later retrieval by the Throwable.getMessage() method.
     */
    public JsonValueLockedException(String message)
    {
        super(message);
    }
}