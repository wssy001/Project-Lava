package moe.ofs.backend.connector.services;

import com.google.gson.Gson;
import moe.ofs.backend.connector.request.BaseRequest;
import moe.ofs.backend.connector.response.JsonRpcRequest;
import moe.ofs.backend.connector.RequestHandler;
import moe.ofs.backend.connector.exceptions.RequestInvalidStateException;
import moe.ofs.backend.connector.util.LuaScripts;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Consumer;

@Service
public class RequestTransmissionServiceImpl implements RequestTransmissionService {
    private final RequestHandler requestHandler;

    public RequestTransmissionServiceImpl(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @PostConstruct
    public void injectLuaScriptsStaticReference() {
        LuaScripts.setRequestTransmissionService(this);
    }

    @Override
    public BaseRequest send(BaseRequest request) {
        if(request.isSent()) {
            throw new RequestInvalidStateException();
        } else {
            prepareParameters(request);
            requestHandler.take(request);
            request.setSent(true);
        }
        return request;
    }

    @Override
    public JsonRpcRequest toJsonRpcCall(BaseRequest request) {
        prepareParameters(request);
        return new JsonRpcRequest (request.getUuid(), request.getHandle().name(), request.getParams());
    }

    @Override
    public void prepareParameters(BaseRequest request) {
        if(request.isSent()) {
            return;
        }

        Field[] fields = request.getClass().getDeclaredFields();

        Consumer<String> addField = s -> {
            try {
                Field field = request.getClass().getDeclaredField(s);
                field.setAccessible(true);
                request.getParams().add(field.get(request));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        };

        Arrays.stream(fields)
                .filter(f -> {
                    String modifiers = Modifier.toString(f.getModifiers());
                    String fieldName = f.getName();

                    return modifiers.contains("transient") && !fieldName.equals("sent");
                })
                .map(Field::getName)
                .forEach(addField);
    }

    @Override
    public String toMessageString(BaseRequest request) {
        prepareParameters(request);
        Gson gson = new Gson();
        return request.toString() + "|" + gson.toJson(request);
    }
}
