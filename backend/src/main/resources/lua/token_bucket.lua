local kind=redis.call('TYPE',KEYS[1]).ok
if kind~='none' and kind~='hash' then return redis.error_reply('invalid bucket type') end
local rate=tonumber(ARGV[1]);local cap=tonumber(ARGV[2])
if not rate or rate<1 or rate>10000 or not cap or cap<1 or cap>10000 then return redis.error_reply('invalid bucket arguments') end
local tm=redis.call('TIME');local now=tonumber(tm[1])*1000+math.floor(tonumber(tm[2])/1000)
local last=tonumber(redis.call('HGET',KEYS[1],'time') or now)
local tokens=tonumber(redis.call('HGET',KEYS[1],'tokens') or cap*1000)
if not last or not tokens then return redis.error_reply('invalid bucket data') end
now=math.max(now,last)
tokens=math.min(cap*1000,tokens+(now-last)*rate)
local wait=0
if tokens>=1000 then tokens=tokens-1000 else wait=math.ceil((1000-tokens)/rate) end
redis.call('HSET',KEYS[1],'time',now,'tokens',tokens)
redis.call('PEXPIRE',KEYS[1],math.max(1000,math.ceil(cap*1000/rate)*2)+1000)
return wait
