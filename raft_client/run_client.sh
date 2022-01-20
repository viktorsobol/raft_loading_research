# sdk use java 11.0.10.hs-adpt

rm -rf experiment_results*
echo "Killing processes..."
pgrep java | xargs kill -9 $1
mkdir -p experiment_results
mkdir -p logs

java -jar target/raft_server-1.0-SNAPSHOT.jar \
  -type Client \
  -mIps "10.148.0.34:6777,10.148.0.35:6777,10.148.0.36:6777" \
  -expId Test-run \
  -totalThreads 120 \
  -writeThreads 60 \
  -runTimeMs 120000 \
  >> logs/client.log 2>&1 &

tail -f logs/client.log
