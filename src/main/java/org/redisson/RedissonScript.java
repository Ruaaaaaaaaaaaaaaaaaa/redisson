/**
 * Copyright 2014 Nikita Koksharov, Nickolay Borbit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson;

import java.util.Collections;
import java.util.List;

import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.core.RScript;

import io.netty.util.concurrent.Future;

public class RedissonScript implements RScript {

    private final CommandExecutor commandExecutor;

    protected RedissonScript(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    @Override
    public String scriptLoad(String luaScript) {
        return commandExecutor.get(scriptLoadAsync(luaScript));
    }

    public String scriptLoad(String key, String luaScript) {
        return commandExecutor.get(scriptLoadAsync(key, luaScript));
    }

    @Override
    public Future<String> scriptLoadAsync(String luaScript) {
        return commandExecutor.writeAllAsync(RedisCommands.SCRIPT_LOAD, new SlotCallback<String, String>() {
            volatile String result;
            @Override
            public void onSlotResult(String result) {
                this.result = result;
            }

            @Override
            public String onFinish() {
                return result;
            }
        }, luaScript);
    }

    public Future<String> scriptLoadAsync(String key, String luaScript) {
        return commandExecutor.writeAsync(key, RedisCommands.SCRIPT_LOAD, luaScript);
    }

    @Override
    public <R> R eval(Mode mode, String luaScript, ReturnType returnType) {
        return eval(null, mode, luaScript, returnType);
    }

    public <R> R eval(String key, Mode mode, String luaScript, ReturnType returnType) {
        return eval(key, mode, luaScript, returnType, Collections.emptyList());
    }

    @Override
    public <R> R eval(Mode mode, String luaScript, ReturnType returnType, List<Object> keys, Object... values) {
        return eval(null, mode, luaScript, returnType, keys, values);
    }

    public <R> R eval(String key, Mode mode, String luaScript, ReturnType returnType, List<Object> keys, Object... values) {
        return (R) commandExecutor.get(evalAsync(key, mode, luaScript, returnType, keys, values));
    }

    @Override
    public <R> Future<R> evalAsync(Mode mode, String luaScript, ReturnType returnType, List<Object> keys, Object... values) {
        return evalAsync(null, mode, luaScript, returnType, keys, values);
    }

    public <R> Future<R> evalAsync(String key, Mode mode, String luaScript, ReturnType returnType, List<Object> keys, Object... values) {
        if (mode == Mode.READ_ONLY) {
            return commandExecutor.evalReadAsync(key, returnType.getCommand(), luaScript, keys, values);
        }
        return commandExecutor.evalWriteAsync(key, returnType.getCommand(), luaScript, keys, values);
    }

    @Override
    public <R> R evalSha(Mode mode, String shaDigest, ReturnType returnType) {
        return evalSha(null, mode, shaDigest, returnType);
    }

    public <R> R evalSha(String key, Mode mode, String shaDigest, ReturnType returnType) {
        return evalSha(key, mode, shaDigest, returnType, Collections.emptyList());
    }

    @Override
    public <R> R evalSha(Mode mode, String shaDigest, ReturnType returnType, List<Object> keys, Object... values) {
        return evalSha(null, mode, shaDigest, returnType, keys, values);
    }

    public <R> R evalSha(String key, Mode mode, String shaDigest, ReturnType returnType, List<Object> keys, Object... values) {
        return (R) commandExecutor.get(evalShaAsync(key, mode, shaDigest, returnType, keys, values));
    }

    @Override
    public <R> Future<R> evalShaAsync(Mode mode, String shaDigest, ReturnType returnType, List<Object> keys, Object... values) {
        return evalShaAsync(null, mode, shaDigest, returnType, keys, values);
    }

    public <R> Future<R> evalShaAsync(String key, Mode mode, String shaDigest, ReturnType returnType, List<Object> keys, Object... values) {
        RedisCommand command = new RedisCommand(returnType.getCommand(), "EVALSHA");
        if (mode == Mode.READ_ONLY) {
            return commandExecutor.evalReadAsync(key, command, shaDigest, keys, values);
        }
        return commandExecutor.evalWriteAsync(key, command, shaDigest, keys, values);
    }

    @Override
    public void scriptKill() {
        commandExecutor.get(scriptKillAsync());
    }

    public void scriptKill(String key) {
        commandExecutor.get(scriptKillAsync(key));
    }

    @Override
    public Future<Void> scriptKillAsync() {
        return commandExecutor.writeAllAsync(RedisCommands.SCRIPT_KILL);
    }

    public Future<Void> scriptKillAsync(String key) {
        return commandExecutor.writeAsync(key, RedisCommands.SCRIPT_KILL);
    }

    public List<Boolean> scriptExists(String key, String ... shaDigests) {
        return commandExecutor.get(scriptExistsAsync(key, shaDigests));
    }

    public Future<List<Boolean>> scriptExistsAsync(String key, String ... shaDigests) {
        return commandExecutor.writeAsync(key, RedisCommands.SCRIPT_EXISTS, shaDigests);
    }

    @Override
    public void scriptFlush() {
        commandExecutor.get(scriptFlushAsync());
    }

    public void scriptFlush(String key) {
        commandExecutor.get(scriptFlushAsync(key));
    }

    @Override
    public Future<Void> scriptFlushAsync() {
        return commandExecutor.writeAllAsync(RedisCommands.SCRIPT_FLUSH);
    }

//    @Override
    public Future<Void> scriptFlushAsync(String key) {
        return commandExecutor.writeAsync(key, RedisCommands.SCRIPT_FLUSH);
    }

}
