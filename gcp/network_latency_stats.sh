
netperf -H 35.213.179.108  -l -1000 -t TCP_RR -w 10ms -b 1 -v 2 -- -O min_latency,mean_latency,p90_latency,p99_latency,max_latency,stddev_latency,transaction_rate

netperf -H 10.148.0.36 -p 5000 -l -1000 -t TCP_RR -w 10ms -b 1 -v 2 -- -O min_latency,mean_latency,p90_latency,p99_latency,max_latency,stddev_latency,transaction_rate