declare -a ips=('35.213.185.226' '35.213.190.82' '35.213.168.109' '34.124.222.158' '34.124.194.197')

COUNTER=34


cd raft

mvn clean package
cd ..
zip -vr server.zip raft/target/
cp server.zip raft_server/

cd raft_server


for ip in "${ips[@]}"
do
    echo "$ip"
    scp -i ~/.ssh/do_key server.zip vsobol@$ip:~/
    ssh -i ~/.ssh/do_key vsobol@$ip "mkdir app; unzip -o server.zip -d app;"

    scp -i ~/.ssh/do_key clean.sh vsobol@$ip:~/
    ssh -i ~/.ssh/do_key vsobol@$ip "cp -rf ~/clean.sh ~/app/raft/"

   (( COUNTER++ )) || true
done

