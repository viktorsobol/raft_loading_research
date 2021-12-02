
for port in 6777 6778 6779
do
  echo "Killing on port:$port"
  lsof -i tcp:$port | sed -n 2p | awk '{print $2}' | xargs kill -9 $1
done

