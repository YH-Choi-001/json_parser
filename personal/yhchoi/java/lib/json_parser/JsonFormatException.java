/**
 * 
 *  JsonFormatException.java - An exception to be thrown when JsonParser finds an illegal json format.
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
 * Exception when JsonParser finds an illegal json format.
 * 
 * @author Yui Hei Choi
 * @version 2025.02.05
 */
public final class JsonFormatException extends Exception
{
    /**
     * The line the illegal format is found.
     */
    private final int line;

    /**
     * The column the illegal format is found.
     */
    private final int column;

    /**
     * The filename, or null if no file is used.
     */
    private final String filename;
    /**
     * Constructor for JsonFormatException.
     * 
     * @param line the line the illegal format is found
     * @param column the column the illegal format is found
     * @param filename the filename, or null if no file is used
     * @param message the error message
     */
    public JsonFormatException(int line, int column, String filename, String message)
    {
        super(
            "Illegal json format at " +
            ((filename != null && !filename.isEmpty()) ? "file: " + filename + " at " : "")
            + "line " + line + ":" + column +
            ((message != null && !message.isEmpty()) ? "\n" + message : ""));
        
        this.line = line;
        this.column = column;
        this.filename = filename;
    }

    /**
     * Gets the line the illegal format is found.
     * 
     * @return the line the illegal format is found
     */
    public final int getLine()
    {
        return line;
    }

    /**
     * Gets the column the illegal format is found.
     * 
     * @return the column the illegal format is found
     */
    public final int getColumn()
    {
        return column;
    }

    /**
     * Gets the file name the illegal format is found.
     * 
     * @return the filename, or null if no file is used
     */
    public final String getFileName()
    {
        return filename;
    }
}