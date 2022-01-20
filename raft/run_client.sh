# sdk use java 11.0.10.hs-adpt

for port in 7000 7001 7002
do
  echo "Killing on port:$port"
  lsof -i tcp:$port | sed -n 2p | awk '{print $2}' | xargs kill -9 $1
done

java -jar target/raft_server-1.0-SNAPSHOT.jar \
 -type Client \
 -mIps "localhost:6777,localhost:6778,localhost:6779" \
 -expId exp-simple-test \
 -totalThreads 12 \
 -writeThreads 3 \
 -runTimeMs 120000 \
 >> logs/client.log &

tail -f logs/client.log
