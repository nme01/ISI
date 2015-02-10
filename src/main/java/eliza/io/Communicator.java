/**
 * @author <a href="mailto:jacek.witkowski@gmail.com">Jacek Witkowski</a>
 * Created on 2015-02-2015.
 */
package eliza.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Communicator implements AutoCloseable {
    private final BufferedReader reader;

    public Communicator() {
        this.reader = new BufferedReader(new InputStreamReader(System.in));
    }

    public String read() throws IOException {
        return reader.readLine();
    }

    public void print(final String message) {
        System.out.println(message);
    }

    @Override
    public void close() throws Exception {
        this.reader.close();
    }
}
