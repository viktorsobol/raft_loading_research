declare -a ips=('35.213.185.226' '35.213.190.82' '35.213.168.109' '34.124.222.158' '34.124.194.197')

COUNTER=34

for ip in "${ips[@]}"
do
    echo "$ip"
    ssh -i ~/.ssh/do_key vsobol@$ip "cd ~/app/raft; sh clean.sh"
    ssh -i ~/.ssh/do_key vsobol@$ip "cd ~/app/raft; java -jar target/raft_server-1.0-SNAPSHOT.jar \
        -type Server \
        -ip 10.148.0.$COUNTER:6777 \
        -mIps 10.148.0.34:6777,10.148.0.35:6777,10.148.0.36:6777,10.148.0.37:6777,10.148.0.38:6777 \
   >> server.log &"
   (( COUNTER++ )) || true
done
