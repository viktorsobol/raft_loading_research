Latency is measured using netperf. 

Google Cloud Engineers are using it as well. Has to be used as a reference. 
https://cloud.google.com/blog/products/networking/using-netperf-and-ping-to-measure-network-latency


netperf -H 35.213.155.242 -l -1000 -t TCP_RR -w 10ms -b 1 -v 2 -- -O min_latency,mean_latency,max_latency,stddev_latency,transaction_rate

netperf -H 10.148.0.34 -l 100 -t TCP_RR -v 2 -- -o min_latency,mean_latency,max_latency,stddev_latency,transaction_rate

netperf -H 10.148.0.34 -l 60 -t TCP_RR -w 10ms -b 1 -v 2 -- -O min_latency,mean_latency,max_latency,stddev_latency,transaction_rate


## Server to Server latency 
Minimum      Mean         Maximum      Stddev       Transaction
Latency      Latency      Latency      Latency      Rate
Microseconds Microseconds Microseconds Microseconds Tran/s

7            49.06        219299       2169.27      20340.653

## Client to Server latency
Minimum      Mean         Maximum      Stddev       Transaction
Latency      Latency      Latency      Latency      Rate
Microseconds Microseconds Microseconds Microseconds Tran/s

40234        40533.00     800910        1064.56      24.654