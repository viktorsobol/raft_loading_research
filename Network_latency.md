**netperf -H 10.148.0.34 -l 10 -t TCP_RR -w 10ms -b 1 -v 2 -- -O min_latency,mean_latency,max_latency,stddev_latency,transaction_rate
latency is measured by netperf. Google cloud is using it for latency testing. 

## Request response testing
### Client to server
netperf -H 10.148.0.34 -l 120 -t TCP_RR -w 10ms -b 1 -v 2 -- -O min_latency,mean_latency,max_latency,stddev_latency,transaction_rate
Minimum      Mean         Maximum      Stddev       Transaction
Latency      Latency      Latency      Latency      Rate
Microseconds Microseconds Microseconds Microseconds Tran/s

71863        72098.11     144800       1785.96      13.862

### Server to server 
Minimum      Mean         Maximum      Stddev       Transaction
Latency      Latency      Latency      Latency      Rate
Microseconds Microseconds Microseconds Microseconds Tran/s

108          285.05       8379         93.68        3503.595


## TCP stream testing (one way latency)
## Client to server
Minimum      Mean         Maximum      Stddev       Transaction
Latency      Latency      Latency      Latency      Rate
Microseconds Microseconds Microseconds Microseconds Tran/s

40261        40450.83     80975        756.45       24.713

## Server to server
Minimum      Mean         Maximum      Stddev       Transaction
Latency      Latency      Latency      Latency      Rate
Microseconds Microseconds Microseconds Microseconds Tran/s

0            133.75       13823        1198.94      1.000