package com.wchgogo.utils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class JsUtil {
    private static ScriptEngineManager sem = new ScriptEngineManager();
    private static ScriptEngine engine = sem.getEngineByExtension("js");

    public static Object eval(String script) throws ScriptException {
        return engine.eval(script);
    }
}