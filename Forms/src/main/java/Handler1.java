import java.io.BufferedOutputStream;
import java.io.IOException;

@FunctionalInterface
public interface Handler1 {
    public void handle(Request request, BufferedOutputStream responseStream) throws IOException;
}