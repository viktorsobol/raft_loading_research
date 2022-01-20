pgrep java | xargs kill -9 $1
rm -rf raft/
rm -rf *.log