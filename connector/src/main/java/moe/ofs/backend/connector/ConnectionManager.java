package moe.ofs.backend.connector;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import moe.ofs.backend.connector.request.BaseRequest;
import moe.ofs.backend.connector.request.FillerRequest;
import moe.ofs.backend.connector.response.JsonRpcRequest;
import moe.ofs.backend.connector.response.JsonRpcResponse;
import moe.ofs.backend.domain.connector.Level;
//import moe.ofs.backend.LavaLog;
import moe.ofs.backend.connector.request.export.ExportResetRequest;
import moe.ofs.backend.connector.services.RequestTransmissionService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Connection Manager class represents a collection of methods that can be used to deal with connection with dcs server.
 * It also holds a reference to singleton RequestHandler which handles all request sent to server.
 *
 * TCP ports used to make connections to DCS Lua server can be changed and saved to an XML file
 * utilizing Configurable interface.
 */

@Slf4j
@Component
public final class ConnectionManager implements Configurable {

    private static final Gson gson = new Gson();

    private final RequestHandler requestHandler;

    private final RequestTransmissionService requestTransmissionService;

    private static ConnectionManager instance;

    private Map<Level, Integer> portOverrideMap = new HashMap<>();

//    private static final LavaLog.Logger logger = LavaLog.getLogger(ConnectionManager.class);

    public ConnectionManager(RequestHandler requestHandler, RequestTransmissionService requestTransmissionService) {
        this.requestHandler = requestHandler;
        this.requestTransmissionService = requestTransmissionService;

        instance = this;

        refreshConfig();

        // check whether an overridden port map exists, if not, set request handler port map to default port map
        requestHandler.updatePortMap(getPortOverrideMap());
    }

    public static ConnectionManager getInstance() {
        if (instance != null) {
            return instance;
        }
        return null;
    }

    public boolean isBackendConnected() {
        return requestHandler.isTrouble();
    }

    public Map<Level, Integer> getPortOverrideMap() {
        return portOverrideMap.isEmpty() ? Arrays.stream(Level.values())
                .collect(Collectors.toMap(Function.identity(), Level::getPort)) : portOverrideMap;
    }

    public void setPortOverrideMap(Map<Level, Integer> portOverrideMap) {
        this.portOverrideMap = portOverrideMap;
        requestHandler.updatePortMap(this.portOverrideMap);  // force update request handler port mapping
//        logger.info(String.format("Backend connection ports re-mapped to the followings: %s",
//                this.portOverrideMap.values()
//                        .stream().map(String::valueOf).collect(Collectors.joining(", "))));
    }

    public void restoreDefaultPortMap() {
        portOverrideMap.clear();
        requestHandler.updatePortMap(getPortOverrideMap());
    }

    public void refreshConfig() {
        if(xmlConfigExists()) {
            portOverrideMap
                    .put(Level.EXPORT, Integer.parseInt(readConfiguration("EXPORT")));
            portOverrideMap
                    .put(Level.EXPORT_POLL, Integer.parseInt(readConfiguration("EXPORT_POLL")));
            portOverrideMap
                    .put(Level.SERVER, Integer.parseInt(readConfiguration("SERVER")));
            portOverrideMap
                    .put(Level.SERVER_POLL, Integer.parseInt(readConfiguration("SERVER_POLL")));
        }
    }

    @Override
    public void writeConfiguration(Map<String, String> map) {
        Configurable.super.writeConfiguration(map);

        refreshConfig();
    }

    @Override
    public String getName() {
        return "tcp_ports";
    }

    /**
     * use to sanitize remaining data on lua side after backend restart
     */
    public void sanitizeDataPipeline() throws IOException {

        requestTransmissionService.send(new FillerRequest(Level.SERVER));
        requestTransmissionService.send(new FillerRequest(Level.SERVER_POLL));

        requestTransmissionService.send(new FillerRequest(Level.EXPORT));
        requestTransmissionService.send(new FillerRequest(Level.EXPORT_POLL));

        fastPackThenSendAndGet(new FillerRequest(Level.EXPORT));
        fastPackThenSendAndGet(new FillerRequest(Level.EXPORT_POLL));
        fastPackThenSendAndCheck(new ExportResetRequest());
    }

    /**
     * pack a single base request into a container ready to sent to dcs server as a json data array.
     * @param request an instance of BaseRequest.
     * @return String value of the final json string to be sent.
     */
    public static String fastPack(BaseRequest request) {
        List<JsonRpcRequest> container = new ArrayList<>();
        container.add(request.toJsonRpcCall());
        return gson.toJson(container);
    }

    /**
     * pack a single BaseRequest into a container and check if is request has a response from server.
     * @param request an instance of BaseRequest.
     * @return boolean value indicating whether server responds to this request.
     */
    public boolean fastPackThenSendAndCheck(BaseRequest request) {
        int port;
        Integer portOverridden = portOverrideMap.get(request.getLevel());
        if(portOverridden != null) {
            port = portOverridden;
        } else {
            port = request.getPort();
        }

        try {
            return requestHandler.sendAndGet(port, fastPack(request)) != null;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * pack a single BaseRequest into a container and get result from dcs lua server.
     * @param request an instance of BaseRequest
     * @return A JSON string representing the result of this request
     * @throws IOException if tcp connection fails
     */
    public String fastPackThenSendAndGet(BaseRequest request) throws IOException {
        int port;
        Integer portOverridden = portOverrideMap.get(request.getLevel());
        if(portOverridden != null) {
            port = portOverridden;
        } else {
            port = request.getPort();
        }

        return requestHandler.sendAndGet(port, fastPack(request));
    }

    /**
     * This method extracts a List of Response result from parsed Json String object
     * @param jsonRpcResponseList an instance of JsonPrcResponse class.
     * @param <T> Generic Type of the object the result data to be convert into.
     * @return List of said generic type.
     */
    public static <T> List<T> flattenResponse(List<JsonRpcResponse<List<T>>> jsonRpcResponseList) {
        return jsonRpcResponseList.stream()
                .filter(r -> r.getResult().getData() != null)
                .flatMap(r -> r.getResult().getData().stream())
                .collect(Collectors.toList());
    }

    public static <T, R> List<R> flattenResponse(List<JsonRpcResponse<List<T>>> jsonRpcResponseList,
                                                                    Function<T, R> mappingFunction) {
        return jsonRpcResponseList.stream()
                .flatMap(r -> r.getResult().getData().stream())
                .map(mappingFunction)
                .collect(Collectors.toList());
    }

    public synchronized static <T> List<JsonRpcResponse<T>> parseJsonResponseToRaw(String jsonString, Class<T> targetClass) {
        Gson gson = new Gson();  // FIXME: potential concurrent issue fix?

        // TODO --> this is so sad
        Type jsonRpcResponseListType = TypeToken.getParameterized(List.class,
                TypeToken.getParameterized(JsonRpcResponse.class, targetClass).getType()).getType();

        // TODO -> why? java.lang.IllegalStateException: Expected a string but was BEGIN_ARRAY at line 1 column 81 path $[0].result.data
        // polling and query should have been separated completely
        return gson.fromJson(jsonString, jsonRpcResponseListType);
    }

    public synchronized static <T> List<JsonRpcResponse<List<T>>> parseJsonResponse(String jsonString, Class<T> targetClass) {
        // TODO --> this is so sad
        Gson gson = new Gson();

        Type jsonRpcResponseListType = TypeToken.getParameterized(List.class,
                TypeToken.getParameterized(JsonRpcResponse.class,
                        TypeToken.getParameterized(List.class, targetClass).getType()).getType()).getType();

        return gson.fromJson(jsonString, jsonRpcResponseListType);

//        return list.stream()
//                .flatMap(r -> r.getResult().getData().stream())
//                .map(targetClass::cast)
//                .collect(Collectors.toList());
//
//        return gson.fromJson(jsonString, jsonRpcResponseListType);
    }


}
