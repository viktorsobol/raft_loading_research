ip="34.80.7.234"

cd raft

mvn clean package
cd ..
zip -vr server.zip raft/target/
cp server.zip raft_client/

cd raft_client

scp -i ~/.ssh/do_key server.zip vsobol@$ip:~/
scp -i ~/.ssh/do_key kill_all_clients.sh vsobol@$ip:~/
scp -i ~/.ssh/do_key run_client.sh vsobol@$ip:~/

ssh -i ~/.ssh/do_key vsobol@$ip "mkdir -p app; unzip -o server.zip -d app;"
ssh -i ~/.ssh/do_key vsobol@$ip "cp -rf ~/kill_all_clients.sh ~/app/raft/"
ssh -i ~/.ssh/do_key vsobol@$ip "cp -rf ~/run_client.sh ~/app/raft/"
