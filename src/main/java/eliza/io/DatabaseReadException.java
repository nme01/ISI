/**
 * @author <a href="mailto:jacek.witkowski@gmail.com">Jacek Witkowski</a>
 * Created on 2015-02-2015.
 */
package eliza.io;

/**
 * Exception thrown when errors occured during reading database.
 */
public class DatabaseReadException extends Exception {
    public DatabaseReadException(final Throwable cause) {
        super(cause);
    }
}
