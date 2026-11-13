local kind=redis.call('TYPE',KEYS[1]).ok
if kind~='none' and kind~='zset' then return redis.error_reply('invalid window type') end
local window=tonumber(ARGV[1]);local limit=tonumber(ARGV[2])
if not window or window<1 or window>3600000 or not limit or limit<1 or limit>1000 then return redis.error_reply('invalid window arguments') end
local tm=redis.call('TIME');local now=tonumber(tm[1])*1000+math.floor(tonumber(tm[2])/1000)
redis.call('ZREMRANGEBYSCORE',KEYS[1],'-inf',now-window)
if redis.call('ZCARD',KEYS[1])>=limit then
  local oldest=redis.call('ZRANGE',KEYS[1],0,0,'WITHSCORES')
  return math.max(1,tonumber(oldest[2])+window-now)
end
redis.call('ZADD',KEYS[1],now,ARGV[3]);redis.call('PEXPIRE',KEYS[1],window+1000)
return 0
