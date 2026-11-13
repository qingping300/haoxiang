package com.haoxiang.mall.redis;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
/** 从 classpath 加载短 Lua 脚本；键值由 StringRedisTemplate 统一编码。 */
public final class LuaScripts {
    private LuaScripts() { }
    public static DefaultRedisScript<Long> load(String name) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/"+name+".lua"));
        script.setResultType(Long.class);
        return script;
    }
}
