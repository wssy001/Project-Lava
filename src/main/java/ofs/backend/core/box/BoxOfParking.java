package ofs.backend.core.box;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import ofs.backend.LuaScripts;
import ofs.backend.core.object.Parking;
import ofs.backend.core.request.BaseRequest;
import ofs.backend.core.request.JsonRpcRequest;
import ofs.backend.core.request.JsonRpcResponse;
import ofs.backend.core.request.RequestHandler;
import ofs.backend.core.request.server.ServerDataRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class BoxOfParking {
    public static List<Parking> box = new ArrayList<>();

    @SuppressWarnings("unchecked")
    private static void loadData(String theaterName) throws IOException, ClassNotFoundException {
        box.clear();

        InputStream inputStream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(String.format("data/%s.apron", theaterName));
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        Object object = objectInputStream.readObject();
        objectInputStream.close();

        if(object instanceof ArrayList) {
            List<Parking> list = (ArrayList<Parking>) object;
            box.addAll(list);
        }
    }

    private static void parse(String playableJsonString) {
        Gson gson = new Gson();
        Type jsonRpcResponseListType = new TypeToken<ArrayList<JsonRpcResponse<String>>>(){}.getType();
        List<JsonRpcResponse<String>> jsonRpcResponseList =
                gson.fromJson(playableJsonString, jsonRpcResponseListType);

        jsonRpcResponseList.stream().findAny().ifPresent(
                r -> {
                    String dataString = r.getResult().getData();
                    String theater = null;
                    try {
                        theater = gson.fromJson(dataString, String.class);
                    } catch (JsonSyntaxException e) {
                        e.printStackTrace();
                        System.out.println("bad data -> " + gson.fromJson(dataString, LinkedTreeMap.class));
                    }
                    try {
                        loadData(theater);
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
        );
    }

    public static void init() {
        String luaString = LuaScripts.load("get_map_theater_name.lua");

        try {
            ServerDataRequest serverExecRequest = new ServerDataRequest(luaString);

            ArrayList<JsonRpcRequest> wrapList = new ArrayList<>();
            wrapList.add(serverExecRequest.toJsonRpcCall());
            RequestHandler.sendAndGet(BaseRequest.Level.SERVER.getPort(), new Gson().toJson(wrapList));
            String theaterDataJson = RequestHandler.sendAndGet(
                    BaseRequest.Level.SERVER.getPort(), "");  // TODO --> make proper jsonrpc
            parse(theaterDataJson);

        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        }


//        String theaterName = ((ServerDataRequest) new ServerDataRequest(luaString).send()).get();
//        try {
//            loadData(theaterName);
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }
    }

    public static Parking get(int airdromeId, int parkingId) {
        Optional<Parking> parkingOptional = box.parallelStream().filter(p -> p.getAirdromeId() == airdromeId && p.getId() == parkingId)
                .findAny();
        return parkingOptional.orElse(null);
    }
}
