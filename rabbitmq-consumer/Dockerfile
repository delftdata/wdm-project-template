FROM python:3.12-slim

WORKDIR /app

COPY ./requirements.txt .

RUN pip install -r requirements.txt

COPY . .
# COPY entrypoint.sh /app/entrypoint.sh
# RUN chmod +x /app/entrypoint.sh

CMD python -u app.py