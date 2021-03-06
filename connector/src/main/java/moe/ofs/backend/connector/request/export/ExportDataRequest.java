package moe.ofs.backend.connector.request.export;

import moe.ofs.backend.connector.request.Handle;
import moe.ofs.backend.domain.connector.Level;
import moe.ofs.backend.connector.request.BaseRequest;
import moe.ofs.backend.connector.response.LuaResponse;
import moe.ofs.backend.connector.response.Processable;
import moe.ofs.backend.connector.response.Resolvable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ExportDataRequest extends BaseRequest implements Resolvable, LuaResponse {

    private transient String luaString;

    private volatile String result;
    private List<Processable> list = new ArrayList<>();

    public ExportDataRequest(String luaString) {
        super(Level.EXPORT);
        port = Level.EXPORT.getPort();
        handle = Handle.EXEC;

        this.luaString = luaString;
    }

    @Override
    public void resolve(String object) {
        this.result = object;
        notifyProcessable(object);
    }

    /**
     * blocking call
     * @return result
     */
    public String get() {
        Instant entryTime = Instant.now();
        while(true) {
            if(result != null) {
                if (result.isEmpty()) {
                    return "<LUA EMPTY STRING>";
                }

                return result;
            } else {  // if result is null, but timeout is reached
                if (Instant.now().minusMillis(2000).isAfter(entryTime)) {
                    return "<NO RESULT OR LUA TIMED OUT>";
                }
            }
        }
    }

    public int getAsInt() {
        return Integer.parseInt(get());
    }

    public double getAsDouble() {
        return Double.parseDouble(get());
    }


    public ExportDataRequest addProcessable(Processable processable) {
        list.add(processable);
        return this;
    }

    public void notifyProcessable(String object) {
        list.forEach(p -> p.process(object));
    }
}
