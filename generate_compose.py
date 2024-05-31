def get_replicas_from_env():
    try:
        with open(".env", "r") as env_file:
            for line in env_file:
                key, value = line.strip().split("=")
                if key.strip() == "REPLICAS":
                    return int(value.strip())
    except FileNotFoundError:
        return None

def generate_consumer_compose(num_consumers):
    compose_content = "services:\n"
    for i in range(num_consumers):
        consumer_service = f"""
  rabbitmq-consumer-{i}:
    build: ./rabbitmq-consumer
    image: rabbitmq-consumer:latest
    restart: always
    environment: 
      - GATEWAY_URL=http://gateway:80
      - MQ_REPLICAS=${{REPLICAS}}
      - REPLICA_INDEX={i}
    depends_on:
      - rabbitmq
"""
        compose_content += consumer_service

    return compose_content


def write_to_file(file_path, content):
    with open(file_path, 'w') as file:
        file.write(content)


if __name__ == "__main__":
    num_consumers = get_replicas_from_env()  # Change this to the desired number of consumers
    compose_content = generate_consumer_compose(num_consumers)
    write_to_file("consumer-compose.yml", compose_content)