if redis.call('GET',KEYS[1]) ~= ARGV[1] then return 0 end
if (redis.call('GET',KEYS[2]) or '0') ~= ARGV[2] then return 0 end
redis.call('SET',KEYS[3],ARGV[3],'PX',ARGV[4])
return 1
