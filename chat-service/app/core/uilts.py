import time
import logging
from functools import wraps
from app.core.logger import setup_logger

logging = setup_logger("api_timer")


# API의 실행 시간을 기록
def log_api_time(func):
    @wraps(func)
    async def wrapper(*args, **kwargs):
        start_time = time.time()
        result = await func(*args, **kwargs)

        route_path = func.__dict__.get("path", func.__name__)

        logging.info(
            f"API: {route_path} executed in {time.time() - start_time:.3f} seconds"
        )
        return result

    return wrapper
