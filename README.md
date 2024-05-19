# Web-scale Data Management Project Team 9

NOTE TO REVIEWERS

The transactions that are processed by RabbitMQ are demonstrably eventually consistent. However, with the current implementation, the [benchmark](https://github.com/delftdata/wdm-project-benchmark) that is provided will not agree as the evaluation starts before RabbitMQ can finish processing all messages.
