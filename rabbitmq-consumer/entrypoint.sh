#!/bin/sh

# # Extract the consumer ID from the hostname
# hostname=$(hostname)
# consumer_id=$(echo $hostname | grep -o -E '[0-9]+$')

# # Set the CONSUMER_ID environment variable
# export CONSUMER_ID=$consumer_id
# # Print the CONSUMER_ID environment variable
# echo "CONSUMER_ID=$CONSUMER_ID"

#!/bin/sh

# Check if CONSUMER_ID is set
echo "Hostname: $HOSTNAME"  # This will print the hostname of the container
if [ -z "$CONSUMER_ID" ]; then
  echo "CONSUMER_ID is not set"
  exit 1
fi
echo "CONSUMER_ID=$CONSUMER_ID"
