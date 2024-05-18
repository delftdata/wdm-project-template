from typing import Any
from pydantic import BaseModel

class AMQPMessage(BaseModel):
    id: str
    content: Any | None = None
    reply_state: str | None = None