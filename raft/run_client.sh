# sdk use java 11.0.10.hs-adpt

for port in 7000 7001 7002
do
  echo "Killing on port:$port"
  lsof -i tcp:$port | sed -n 2p | awk '{print $2}' | xargs kill -9 $1
done

java -jar target/raft_server-1.0-SNAPSHOT.jar \
 -type Client \
 -port 7000 \
 -key test1 \
 -mIps "localhost:6777,localhost:6778,localhost:6779" \
 -aId app-simple-test \
 -expId exp-simple-test \
 -rwRatio 5 \
 -runTimeMs 60000 \
 >> logs/client.log &

tail -f logs/client.log
