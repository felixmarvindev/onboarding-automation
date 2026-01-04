# Load Testing with JMeter

This directory contains JMeter test plans for load testing the onboarding API.

## Prerequisites

- JMeter 5.6 or later
- Running onboarding system (via Docker Compose or locally)

## Running the Load Test

### Option 1: Command Line (Non-GUI Mode)

```bash
# Run the test plan
jmeter -n -t load-test.jmx -l results.jtl

# Generate HTML report
jmeter -g results.jtl -o report/
```

### Option 2: GUI Mode

1. Open JMeter GUI
2. File -> Open -> Select `load-test.jmx`
3. Click Run -> Start

## Test Configuration

- **Threads**: 100 concurrent users
- **Ramp-up time**: 10 seconds
- **Loop count**: 1 (100 requests total)
- **Endpoint**: POST /api/onboarding/request

## Expected Results

- All requests should return HTTP 200
- Response time should be under 2 seconds (p95)
- Error rate should be 0%

## Customizing the Test

Edit `load-test.jmx` to modify:
- Number of threads (`ThreadGroup.num_threads`)
- Ramp-up time (`ThreadGroup.ramp_time`)
- Loop count (`LoopController.loops`)
- Base URL (`baseUrl` variable)
