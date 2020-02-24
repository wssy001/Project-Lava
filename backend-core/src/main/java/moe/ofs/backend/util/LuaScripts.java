package moe.ofs.backend.util;

import moe.ofs.backend.BackendMain;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class LuaScripts {
    public static String load(String scriptName) {
        InputStream in = BackendMain.class.getClassLoader().getResourceAsStream("scripts/" + scriptName);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        return bufferedReader.lines().collect(Collectors.joining("\n"));
    }
}