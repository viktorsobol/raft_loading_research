declare -a ips=('35.213.187.60' '35.213.173.19' '35.213.168.109')

for ip in "${ips[@]}"
do
    echo "$ip"
    scp -i ~/.ssh/do_key server.zip vsobol@$ip:~/
    ssh -i ~/.ssh/do_key vsobol@$ip "mkdir app; unzip server.zip -d app;"
done


scp -i ~/.ssh/do_key clean.sh vsobol@$ip:~/
ssh -i ~/.ssh/do_key vsobol@$ip "cp -rf ~/clean.sh ~/app/raft/"
