# Consumer e2e latency interceptor

Status: incomplete

Proof-of-concept to add metrics to Kafka via Interceptors.
In this case, pick the consumer record timestamp, and comparing it with wall-clock to observe how fresh is the data consumed.
