# sdk use java 11.0.10.hs-adpt

for port in 6777 6778 6779
do
  echo "Killing on port:$port"
  lsof -i tcp:$port | sed -n 2p | awk '{print $2}' | xargs kill -9 $1
done

java -jar target/raft_server-1.0-SNAPSHOT.jar \
  -type Server \
  -ip localhost:6777 \
  -mIps localhost:6777,localhost:6778,localhost:6779 \
   >> logs/server.log &

java -jar target/raft_server-1.0-SNAPSHOT.jar \
  -type Server \
  -ip localhost:6778 \
  -mIps localhost:6777,localhost:6778,localhost:6779 \
  >> logs/server.log &

java -jar target/raft_server-1.0-SNAPSHOT.jar \
  -type Server \
  -ip localhost:6779 \
  -mIps localhost:6777,localhost:6778,localhost:6779 \
  >> logs/server.log &

tail -f logs/server.log