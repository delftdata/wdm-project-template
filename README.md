# Web-scale Data Management Project Team 9

NOTE TO REVIEWERS

The transactions that are processed by RabbitMQ are demonstrably eventually consistent. However, with the current implementation, the [benchmark](https://github.com/delftdata/wdm-project-benchmark) that is provided will not agree as the evaluation starts before RabbitMQ can finish processing all messages.

Inserting a sleep statement of ~10 seconds before the evaluation step will ensure the database ends up being consistent. The logs do not give proper results based on our current implementation.

### Running the code
Run ```python generate_compose.py``` to create the docker-compose file based on the amount of consumers needed, which can be defined in ```.env```. To then start the cluster you can run: ```docker-compose -f docker-compose.yml -f consumer-compose.yml up```.