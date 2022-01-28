ip="34.80.7.234"

ssh -i ~/.ssh/do_key vsobol@$ip "
    cd ~/app/raft/experiment_results/
    zip -r ~/app/raft/experiment_results.zip .
"
scp -i ~/.ssh/do_key vsobol@$ip:~/app/raft/experiment_results*.zip _results/
mkdir -p "_results/experiment_$1"
mkdir -p "_results/experiment_$1/data"

unzip _results/experiment_results.zip -d "_results/experiment_$1/data"
touch "_results/experiment_$1/Description.md"

./results_processor/venv/bin/python results_processor/calculate_pbs.py "/Users/vsobol/personal_projects/PhD/Raft_Load_capacity_research/raft_loading_research/_results/experiment_$1/data/"
cp results.svg "_results/experiment_$1/"

./results_processor/venv/bin/python results_processor/calculate_basic_stats.py "/Users/vsobol/personal_projects/PhD/Raft_Load_capacity_research/raft_loading_research/_results/experiment_$1/data/" >> "_results/experiment_$1/Description.md"
